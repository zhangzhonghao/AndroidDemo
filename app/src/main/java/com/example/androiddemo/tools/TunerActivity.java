package com.example.androiddemo.tools;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;

/**
 * 调音器 Activity
 */
public class TunerActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final long UPDATE_INTERVAL = 50; // 50ms 更新

    // 音频参数
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_FACTOR = 2;

    // 标准音A4频率
    private static final double A4_FREQUENCY = 440.0;

    // 音符名称
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    // 频率范围（吉他E2到E6：82Hz到1319Hz）
    private static final double MIN_FREQUENCY = 60.0;
    private static final double MAX_FREQUENCY = 1500.0;

    private TunerView tunerView;
    private TextView tvNote;
    private TextView tvFrequency;
    private TextView tvCalibration;
    private TextView tvNoteOctave;

    private AudioRecord audioRecord;
    private Handler handler;
    private Runnable updateRunnable;

    private boolean isRecording = false;

    // 当前校准值（cents）
    private int calibrationCents = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_tuner);

        initViews();
        initHandler();

        checkAndRequestPermission();
    }

    private void initViews() {
        tunerView = findViewById(R.id.tuner_view);
        tvNote = findViewById(R.id.tv_note);
        tvFrequency = findViewById(R.id.tv_frequency);
        tvCalibration = findViewById(R.id.tv_calibration);
        tvNoteOctave = findViewById(R.id.tv_note_octave);

        updateCalibrationText();
    }

    private void initHandler() {
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    analyzeAudio();
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        };
    }

    private void checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        } else {
            startRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "需要录音权限才能使用调音器", Toast.LENGTH_LONG).show();
                tunerView.setNoSignal();
                tvNote.setText("--");
                tvFrequency.setText("-- Hz");
            }
        }
    }

    private void startRecording() {
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
                * BUFFER_SIZE_FACTOR;

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Toast.makeText(this, "无法创建录音缓冲区", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                Toast.makeText(this, "录音初始化失败", Toast.LENGTH_LONG).show();
                return;
            }

            audioRecord.startRecording();
            isRecording = true;
            handler.post(updateRunnable);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法启动录音，请检查权限", Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecording() {
        isRecording = false;
        handler.removeCallbacks(updateRunnable);

        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
    }

    private void analyzeAudio() {
        if (audioRecord == null) return;

        try {
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
                    * BUFFER_SIZE_FACTOR;
            short[] buffer = new short[bufferSize];
            int readSize = audioRecord.read(buffer, 0, bufferSize);

            if (readSize <= 0) {
                tunerView.setNoSignal();
                return;
            }

            // 检测声音能量
            double energy = 0;
            for (int i = 0; i < readSize; i++) {
                energy += (double) buffer[i] * buffer[i];
            }
            energy = Math.sqrt(energy / readSize);

            // 阈值判断是否有声音
            if (energy < 100) {
                tunerView.setNoSignal();
                tvNote.setText("--");
                tvFrequency.setText("-- Hz");
                return;
            }

            // 使用自相关算法计算基频
            double frequency = calculateFrequency(buffer, readSize);

            if (frequency < MIN_FREQUENCY || frequency > MAX_FREQUENCY) {
                tunerView.setNoSignal();
                return;
            }

            // 计算音符和偏移
            calculateNoteInfo(frequency);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自相关算法计算基频
     */
    private double calculateFrequency(short[] buffer, int size) {
        // 使用改进的麦克风计法（NSA - Normalized Squared Difference Autocorrelation）

        int minLag = (int) (SAMPLE_RATE / MAX_FREQUENCY);
        int maxLag = (int) (SAMPLE_RATE / MIN_FREQUENCY);

        double maxCorrelation = 0;
        int bestLag = 0;

        // 计算自相关
        for (int lag = minLag; lag <= maxLag && lag < size / 2; lag++) {
            double correlation = 0;
            double norm1 = 0;
            double norm2 = 0;

            for (int i = 0; i < size - lag; i++) {
                correlation += (double) buffer[i] * buffer[i + lag];
                norm1 += (double) buffer[i] * buffer[i];
                norm2 += (double) buffer[i + lag] * buffer[i + lag];
            }

            double normalizedCorrelation = correlation / Math.sqrt(norm1 * norm2 + 1e-10);

            if (normalizedCorrelation > maxCorrelation) {
                maxCorrelation = normalizedCorrelation;
                bestLag = lag;
            }
        }

        if (maxCorrelation < 0.3) {
            return -1; // 信号不够强
        }

        // 抛物线插值提高精度
        if (bestLag > minLag && bestLag < maxLag - 1) {
            double y0 = computeCorrelation(buffer, size, bestLag - 1);
            double y1 = computeCorrelation(buffer, size, bestLag);
            double y2 = computeCorrelation(buffer, size, bestLag + 1);

            double delta = (y2 - y0) / (2 * (2 * y1 - y0 - y2) + 1e-10);
            bestLag = bestLag + (int) Math.round(delta);
        }

        return (double) SAMPLE_RATE / bestLag;
    }

    private double computeCorrelation(short[] buffer, int size, int lag) {
        double correlation = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < size - lag; i++) {
            correlation += (double) buffer[i] * buffer[i + lag];
            norm1 += (double) buffer[i] * buffer[i];
            norm2 += (double) buffer[i + lag] * buffer[i + lag];
        }

        return correlation / Math.sqrt(norm1 * norm2 + 1e-10);
    }

    /**
     * 计算音符信息和偏移
     */
    private void calculateNoteInfo(double frequency) {
        // 应用校准：frequency * 2^(calibrationCents/1200)
        double calibratedFreq = frequency * Math.pow(2, calibrationCents / 1200.0);

        // 计算MIDI音符编号
        // A4 = 440Hz = MIDI 69
        // n = 69 + 12 * log2(f/440)
        double midiNote = 69 + 12 * Math.log(calibratedFreq / A4_FREQUENCY) / Math.log(2);
        int noteNumber = (int) Math.round(midiNote);

        // 计算音符索引（0-11）
        int noteIndex = ((noteNumber % 12) + 12) % 12;
        String noteName = NOTE_NAMES[noteIndex];

        // 计算八度
        int octave = noteNumber / 12 - 1;

        // 计算目标频率
        double targetFreq = A4_FREQUENCY * Math.pow(2, (noteNumber - 69) / 12.0);

        // 计算偏移（cents）
        // cents = 1200 * log2(f/target)
        double cents = 1200 * Math.log(calibratedFreq / targetFreq) / Math.log(2);

        // 更新UI
        tunerView.setCentsOffset((float) cents);
        tvNote.setText(noteName);
        tvNoteOctave.setText(String.format("%d", octave));
        tvFrequency.setText(String.format("%.1f Hz", frequency));
    }

    public void onCalibrationUpClick(View view) {
        calibrationCents += 5;
        if (calibrationCents > 50) calibrationCents = 50;
        updateCalibrationText();
    }

    public void onCalibrationDownClick(View view) {
        calibrationCents -= 5;
        if (calibrationCents < -50) calibrationCents = -50;
        updateCalibrationText();
    }

    public void onCalibrationResetClick(View view) {
        calibrationCents = 0;
        updateCalibrationText();
        Toast.makeText(this, "已重置为 A=440Hz", Toast.LENGTH_SHORT).show();
    }

    private void updateCalibrationText() {
        if (calibrationCents == 0) {
            tvCalibration.setText("A = 440 Hz");
        } else if (calibrationCents > 0) {
            tvCalibration.setText(String.format("A = %.0f Hz (+%d)", getCalibratedA4(), calibrationCents));
        } else {
            tvCalibration.setText(String.format("A = %.0f Hz (%d)", getCalibratedA4(), calibrationCents));
        }
    }

    private double getCalibratedA4() {
        return A4_FREQUENCY * Math.pow(2, calibrationCents / 1200.0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED && !isRecording) {
            startRecording();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRecording();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecording();
    }
}