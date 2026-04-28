package com.example.androiddemo.ai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import java.util.Arrays;
import java.util.Locale;

/**
 * 加速度计语音识别Activity
 * 
 * 功能：通过手机加速度计感知声波振动，识别数字0-9
 * 
 * 核心流程：
 * 1. 用户点击"开始录音" → 启动加速度计数据采集
 * 2. 实时显示采样率和波形数据
 * 3. 用户点击"结束录音"
 * 4. 自动进行信号处理 + MFCC特征提取 + 模板匹配
 * 5. 显示识别结果（数字序列）
 */
public class AccelVoiceActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "AccelVoiceActivity";
    private static final int REQUEST_BODY_SENSORS = 1001;
    private static final int REQUEST_HIGH_SAMPLING = 1002;

    // 采样参数
    private static final float DEFAULT_SAMPLE_RATE = 50f; // SENSOR_DELAY_GAME ≈ 50Hz
    private static final int FRAME_SAMPLES = 150; // 约3秒数据（50Hz * 3s）

    // UI控件
    private TextView tvStatus;
    private TextView tvAccelRate;
    private TextView tvGyroRate;
    private TextView tvResult;
    private TextView tvWaveform;
    private Button btnStart;

    // 传感器管理
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    // 数据缓冲（双索引，各自独立计数）
    private float[] accelYBuffer = new float[FRAME_SAMPLES];
    private float[] gyroYBuffer = new float[FRAME_SAMPLES];
    private int accelWritePos = 0;
    private int gyroWritePos = 0;
    private volatile boolean isRecording = false;

    // 采样率估算
    private long lastAccelTime = 0;
    private long lastGyroTime = 0;
    private float estimatedAccelRate = 0;
    private float estimatedGyroRate = 0;

    // 识别结果
    private StringBuilder recognizedText = new StringBuilder();

    // 信号处理组件
    private VoiceSignalPreprocessor preprocessor = new VoiceSignalPreprocessor();
    private MfccExtractor mfccExtractor = new MfccExtractor();
    private FusionMfccExtractor fusionMfcc = new FusionMfccExtractor();
    private AccelVoiceRecognizer recognizer = new AccelVoiceRecognizer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_voice);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("加速度计语音识别");
        }

        initViews();
        initSensors();
        checkPermissions();
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        tvAccelRate = findViewById(R.id.tv_accel_rate);
        tvGyroRate = findViewById(R.id.tv_gyro_rate);
        tvResult = findViewById(R.id.tv_result);
        tvWaveform = findViewById(R.id.tv_waveform);
        btnStart = findViewById(R.id.btn_start);

        btnStart.setOnClickListener(v -> toggleRecording());
        updateStatus("点击开始录音");
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    REQUEST_BODY_SENSORS);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.HIGH_SAMPLING_RATE_SENSORS},
                        REQUEST_HIGH_SAMPLING);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BODY_SENSORS || requestCode == REQUEST_HIGH_SAMPLING) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "传感器权限已授予");
            } else {
                updateStatus("需要传感器权限才能使用此功能");
            }
        }
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        if (accelerometer == null) {
            updateStatus("加速度计不可用");
            return;
        }

        // 检查传感器权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {
            updateStatus("缺少传感器权限，正在请求...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    REQUEST_BODY_SENSORS);
            return;
        }

        isRecording = true;
        btnStart.setText("结束录音");
        recognizedText.setLength(0);
        accelWritePos = 0;
        gyroWritePos = 0;
        Arrays.fill(accelYBuffer, 0f);
        Arrays.fill(gyroYBuffer, 0f);
        
        updateStatus("正在录音...");

        // 注册传感器（使用 SENSOR_DELAY_GAME ≈ 50Hz，无需特殊权限）
        sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, gyroscope,
                SensorManager.SENSOR_DELAY_GAME);

        // 启动识别循环
        startRecognitionLoop();
    }

        private void stopRecording() {
        isRecording = false;
        btnStart.setText("开始录音");
        sensorManager.unregisterListener(this);

        // 根据实际数据量决定处理方式
        if (accelWritePos < 50) {
            updateStatus("录音时间太短，无法识别");
            return;
        } else if (accelWritePos > 0) {
            // 数据足够，开始处理
            updateStatus("正在识别...");
            processBuffer();
        } else {
            updateStatus("录音时间太短，无法识别");
        }
    }

    private void startRecognitionLoop() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final int[] lastProcessedPos = {0};

        Runnable recognitionTask = new Runnable() {
            @Override
            public void run() {
                if (!isRecording) return;

                // 每0.5秒处理一次（以加速度计的实际写入位置为准）
                int currentPos = accelWritePos;
                int segmentSize = FRAME_SAMPLES / 2;
                if (currentPos - lastProcessedPos[0] >= segmentSize) {
                    processBufferSegment(lastProcessedPos[0], currentPos);
                    lastProcessedPos[0] = currentPos;
                }

                // 更新采样率UI
                tvAccelRate.setText(String.format(Locale.US, "%.1f Hz", estimatedAccelRate));
                tvGyroRate.setText(String.format(Locale.US, "%.1f Hz", estimatedGyroRate));

                handler.postDelayed(this, 500);
            }
        };

        handler.postDelayed(recognitionTask, 500);
    }

    private void processBufferSegment(int start, int end) {
        int length = end - start;
        if (length <= 0) return;

        float[] accelY = Arrays.copyOfRange(accelYBuffer, start, end);
        // 陀螺仪和加速度计采样率不同，用较短的那个
        int gyroEnd = Math.min(start + length, gyroWritePos);
        float[] gyroY = Arrays.copyOfRange(gyroYBuffer, start, gyroEnd);

        float sampleRate = estimatedAccelRate > 0 ? estimatedAccelRate : DEFAULT_SAMPLE_RATE;

        new Thread(() -> recognizeFrame(accelY, gyroY, sampleRate)).start();
    }

    private void processBuffer() {
        int accelLen = accelWritePos;
        int gyroLen = gyroWritePos;
        if (accelLen <= 0) return;

        float[] accelY = Arrays.copyOf(accelYBuffer, accelLen);
        float[] gyroY = Arrays.copyOf(gyroYBuffer, Math.min(gyroLen, accelLen));

        float sampleRate = estimatedAccelRate > 0 ? estimatedAccelRate : DEFAULT_SAMPLE_RATE;

        new Thread(() -> recognizeFrame(accelY, gyroY, sampleRate)).start();
    }

    private void recognizeFrame(float[] accelY, float[] gyroY, float sampleRate) {
        if (accelY.length < 50) {
            runOnUiThread(() -> updateStatus("录音时间太短，无法识别"));
            return; // 数据太少
        }

        // 1. 预处理：去重力 + 带通滤波
        float[] accelProcessed = preprocessor.preprocess(accelY, sampleRate);

        // 2. 提取MFCC特征
        float[] mfcc = mfccExtractor.extractMfcc(accelProcessed, sampleRate);

        // 3. 如果陀螺仪有数据，尝试融合
        if (hasValidGyroData(gyroY)) {
            float[] gyroProcessed = preprocessor.preprocess(gyroY, sampleRate);
            float[] fusedMfcc = fusionMfcc.extractFusionMfcc(accelProcessed, gyroProcessed, sampleRate);
            mfcc = fusedMfcc;
        }

        // 4. 识别数字
        int digit = recognizer.recognizeDigit(mfcc);

        // 5. 更新UI
        runOnUiThread(() -> {
            if (digit >= 0 && digit <= 9) {
                recognizedText.append(digit);
                tvResult.setText("识别结果: " + recognizedText.toString());
            }
            updateWaveform(accelProcessed);
        });
    }

    private boolean hasValidGyroData(float[] gyroY) {
        if (gyroY == null || gyroY.length == 0) return false;
        float sum = 0;
        for (float v : gyroY) sum += Math.abs(v);
        return sum > 0;
    }

    private void updateWaveform(float[] data) {
        if (data == null || data.length == 0) return;

        StringBuilder sb = new StringBuilder();
        int step = Math.max(1, data.length / 40);
        for (int i = 0; i < 40 && i * step < data.length; i++) {
            float v = data[i * step];
            int bar = (int) Math.abs(v * 5);
            bar = Math.min(bar, 10);
            for (int j = 0; j < bar; j++) {
                sb.append('|');
            }
            sb.append('\n');
        }
        tvWaveform.setText(sb.toString());
    }

    private void updateStatus(String status) {
        tvStatus.setText(status);
    }

    private void updateAccelSamplingRate(long timestamp) {
        if (lastAccelTime > 0) {
            long interval = timestamp - lastAccelTime;
            if (interval > 0) {
                estimatedAccelRate = 1_000_000_000f / interval;
            }
        }
        lastAccelTime = timestamp;
    }

    private void updateGyroSamplingRate(long timestamp) {
        if (lastGyroTime > 0) {
            long interval = timestamp - lastGyroTime;
            if (interval > 0) {
                estimatedGyroRate = 1_000_000_000f / interval;
            }
        }
        lastGyroTime = timestamp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isRecording) return;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (accelWritePos < FRAME_SAMPLES) {
                    float y = event.values[1];
                    accelYBuffer[accelWritePos] = y;
                    updateAccelSamplingRate(event.timestamp);
                    accelWritePos++;
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                if (gyroWritePos < FRAME_SAMPLES) {
                    float gyroY = event.values[1];
                    gyroYBuffer[gyroWritePos] = gyroY;
                    updateGyroSamplingRate(event.timestamp);
                    gyroWritePos++;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed: " + sensor.getName() + " -> " + accuracy);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRecording) {
            stopRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    // ==================== 内部类：信号预处理 ====================

    private class VoiceSignalPreprocessor {
        
        public float[] preprocess(float[] accelY, float sampleRate) {
            // 1. 去重力：高通滤波
            float[] filtered = highpassFilter(accelY, 0.5f, sampleRate);
            
            // 2. 带通滤波：保留 80-300 Hz
            filtered = bandpassFilter(filtered, 80f, Math.min(300f, sampleRate / 2.1f), sampleRate);
            
            // 3. 去趋势
            filtered = detrend(filtered);
            
            // 4. 归一化
            filtered = normalize(filtered);
            
            return filtered;
        }

        private float[] highpassFilter(float[] input, float cutoff, float sampleRate) {
            float rc = 1.0f / (2 * (float) Math.PI * cutoff);
            float dt = 1.0f / sampleRate;
            float alpha = rc / (rc + dt);

            float[] output = new float[input.length];
            output[0] = input[0];
            for (int i = 1; i < input.length; i++) {
                output[i] = alpha * (output[i - 1] + input[i] - input[i - 1]);
            }
            return output;
        }

        private float[] bandpassFilter(float[] input, float lowCutoff, float highCutoff, float sampleRate) {
            float[] lowPassed = lowpassFilter(input, highCutoff, sampleRate);
            return highpassFilter(lowPassed, lowCutoff, sampleRate);
        }

        private float[] lowpassFilter(float[] input, float cutoff, float sampleRate) {
            float rc = 1.0f / (2 * (float) Math.PI * cutoff);
            float dt = 1.0f / sampleRate;
            float alpha = dt / (rc + dt);

            float[] output = new float[input.length];
            output[0] = input[0];
            for (int i = 1; i < input.length; i++) {
                output[i] = output[i - 1] + alpha * (input[i] - output[i - 1]);
            }
            return output;
        }

        private float[] detrend(float[] input) {
            int n = input.length;
            if (n < 2) return input.clone();

            float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
            for (int i = 0; i < n; i++) {
                float x = i;
                float y = input[i];
                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }

            float nF = n;
            float denominator = nF * sumX2 - sumX * sumX;
            if (Math.abs(denominator) < 1e-10f) {
                // 数据无变化或完全相同，返回去均值后的结果
                float mean = sumY / nF;
                float[] output = new float[n];
                for (int i = 0; i < n; i++) {
                    output[i] = input[i] - mean;
                }
                return output;
            }
            float slope = (nF * sumXY - sumX * sumY) / denominator;
            float intercept = (sumY - slope * sumX) / nF;

            float[] output = new float[n];
            for (int i = 0; i < n; i++) {
                output[i] = input[i] - (slope * i + intercept);
            }
            return output;
        }

        private float[] normalize(float[] input) {
            float maxAbs = 0;
            for (float v : input) {
                maxAbs = Math.max(maxAbs, Math.abs(v));
            }
            if (maxAbs < 1e-10f) return input.clone();

            float[] output = new float[input.length];
            for (int i = 0; i < input.length; i++) {
                output[i] = input[i] / maxAbs;
            }
            return output;
        }
    }

    // ==================== 内部类：MFCC特征提取 ====================

    private class MfccExtractor {
        private static final int NUM_MEL_FILTERS = 26;
        private static final int NUM_CEPS = 13;
        private static final int FRAME_LENGTH_MS = 25;
        private static final int FRAME_STEP_MS = 10;
        private static final float LOWER_FREQ = 80f;
        private static final float UPPER_FREQ = 300f;

        public float[] extractMfcc(float[] signal, float sampleRate) {
            int frameLength = (int) (FRAME_LENGTH_MS * sampleRate / 1000);
            int frameStep = (int) (FRAME_STEP_MS * sampleRate / 1000);
            if (frameStep < 1) frameStep = Math.max(1, (int) (sampleRate / 100)); // 采样率太低时保证帧移至少1
            int numFrames = Math.max(1, (signal.length - frameLength) / frameStep + 1);

            // 预加重
            float[] emphasized = preEmphasis(signal, 0.97f);

            // 分帧 + 加窗 + FFT
            float[][] powerSpectrum = new float[numFrames][];
            for (int i = 0; i < numFrames; i++) {
                int start = i * frameStep;
                int end = Math.min(start + frameLength, signal.length);

                float[] frame = extractFrame(emphasized, start, end, frameLength);
                frame = applyHanningWindow(frame);

                float[] fftMag = computeFftMagnitude(frame, sampleRate);

                powerSpectrum[i] = new float[fftMag.length];
                for (int j = 0; j < fftMag.length; j++) {
                    powerSpectrum[i][j] = fftMag[j] * fftMag[j];
                }
            }

            // 梅尔滤波器组
            int nfft = nextPowerOf2(frameLength);
            float[][] melFilters = createMelFilterBank(NUM_MEL_FILTERS, nfft, sampleRate);
            float[][] melSpectrogram = new float[numFrames][];

            for (int i = 0; i < numFrames; i++) {
                melSpectrogram[i] = applyMelFilterBank(powerSpectrum[i], melFilters, nfft / 2);
            }

            // 对数压缩
            for (int i = 0; i < numFrames; i++) {
                for (int j = 0; j < melSpectrogram[i].length; j++) {
                    melSpectrogram[i][j] = (float) Math.log(melSpectrogram[i][j] + 1e-10f);
                }
            }

            // DCT得到MFCC
            float[][] mfcc = new float[numFrames][];
            for (int i = 0; i < numFrames; i++) {
                mfcc[i] = dct(melSpectrogram[i], NUM_CEPS);
            }

            // 返回中间帧
            return mfcc[Math.max(0, numFrames / 2)].clone();
        }

        public float[][] extractMfccSequence(float[] signal, float sampleRate) {
            int frameLength = (int) (FRAME_LENGTH_MS * sampleRate / 1000);
            int frameStep = (int) (FRAME_STEP_MS * sampleRate / 1000);
            if (frameStep < 1) frameStep = Math.max(1, (int) (sampleRate / 100));
            int numFrames = Math.max(1, (signal.length - frameLength) / frameStep + 1);

            float[] emphasized = preEmphasis(signal, 0.97f);
            int nfft = nextPowerOf2(frameLength);
            float[][] melFilters = createMelFilterBank(NUM_MEL_FILTERS, nfft, sampleRate);

            float[][] mfccSequence = new float[numFrames][];

            for (int i = 0; i < numFrames; i++) {
                int start = i * frameStep;
                int end = Math.min(start + frameLength, signal.length);

                float[] frame = extractFrame(emphasized, start, end, frameLength);
                frame = applyHanningWindow(frame);
                float[] fftMag = computeFftMagnitude(frame, sampleRate);

                float[] psd = new float[fftMag.length];
                for (int j = 0; j < fftMag.length; j++) psd[j] = fftMag[j] * fftMag[j];

                float[] mel = applyMelFilterBank(psd, melFilters, nfft / 2);
                for (int j = 0; j < mel.length; j++) mel[j] = (float) Math.log(mel[j] + 1e-10f);

                mfccSequence[i] = dct(mel, NUM_CEPS);
            }

            return mfccSequence;
        }

        private float[] extractFrame(float[] signal, int start, int end, int frameLength) {
            float[] frame = new float[frameLength];
            int copyLength = Math.min(end - start, frameLength);
            System.arraycopy(signal, start, frame, 0, copyLength);
            return frame;
        }

        private float[] preEmphasis(float[] signal, float alpha) {
            float[] output = new float[signal.length];
            output[0] = signal[0];
            for (int i = 1; i < signal.length; i++) {
                output[i] = signal[i] - alpha * signal[i - 1];
            }
            return output;
        }

        private float[] applyHanningWindow(float[] input) {
            int n = input.length;
            float[] output = new float[n];
            for (int i = 0; i < n; i++) {
                float window = 0.5f * (1 - (float) Math.cos(2 * Math.PI * i / (n - 1)));
                output[i] = input[i] * window;
            }
            return output;
        }

        private int nextPowerOf2(int n) {
            int p = 1;
            while (p < n) p *= 2;
            return p;
        }

        private float[] computeFftMagnitude(float[] frame, float sampleRate) {
            int n = nextPowerOf2(frame.length);

            float[] real = new float[n];
            float[] imag = new float[n];
            System.arraycopy(frame, 0, real, 0, frame.length);

            fft(real, imag);

            int halfN = n / 2;
            float[] magnitude = new float[halfN];
            for (int i = 0; i < halfN; i++) {
                magnitude[i] = (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
            }
            return magnitude;
        }

        private void fft(float[] real, float[] imag) {
            int n = real.length;
            if (n <= 1) return;

            // 位反转排序
            for (int i = 1, j = 0; i < n; i++) {
                int bit = n >> 1;
                for (; (j & bit) != 0; bit >>= 1) j ^= bit;
                j ^= bit;
                if (i > j) {
                    float tmp = real[i]; real[i] = real[j]; real[j] = tmp;
                    tmp = imag[i]; imag[i] = imag[j]; imag[j] = tmp;
                }
            }

            // 蝶形运算
            for (int len = 2; len <= n; len <<= 1) {
                float angle = (float) (-2 * Math.PI / len);
                for (int i = 0; i < n; i += len) {
                    float wReal = 1f, wImag = 0f;
                    float wAngle = angle;
                    for (int j = 0; j < len / 2; j++) {
                        float uR = real[i + j], uI = imag[i + j];
                        float vR = real[i + j + len / 2] * wReal - imag[i + j + len / 2] * wImag;
                        float vI = real[i + j + len / 2] * wImag + imag[i + j + len / 2] * wReal;
                        real[i + j] = uR + vR;
                        imag[i + j] = uI + vI;
                        real[i + j + len / 2] = uR - vR;
                        imag[i + j + len / 2] = uI - vI;

                        float newWReal = wReal * (float) Math.cos(wAngle) - wImag * (float) Math.sin(wAngle);
                        float newWImag = wReal * (float) Math.sin(wAngle) + wImag * (float) Math.cos(wAngle);
                        wReal = newWReal;
                        wImag = newWImag;
                    }
                }
            }
        }

        private float[][] createMelFilterBank(int numFilters, int nfft, float sampleRate) {
            float[][] filters = new float[numFilters][];

            float lowMel = hzToMel(LOWER_FREQ);
            float highMel = hzToMel(Math.min(UPPER_FREQ, sampleRate / 2.1f));

            float[] melPoints = new float[numFilters + 2];
            for (int i = 0; i < melPoints.length; i++) {
                melPoints[i] = lowMel + (highMel - lowMel) * i / (numFilters + 1);
            }

            float[] hzPoints = new float[melPoints.length];
            for (int i = 0; i < melPoints.length; i++) {
                hzPoints[i] = melToHz(melPoints[i]);
            }

            int[] binPoints = new int[hzPoints.length];
            for (int i = 0; i < hzPoints.length; i++) {
                binPoints[i] = (int) Math.floor((nfft + 1) * hzPoints[i] / sampleRate);
            }

            for (int i = 0; i < numFilters; i++) {
                filters[i] = new float[nfft / 2];
                int left = binPoints[i];
                int center = binPoints[i + 1];
                int right = binPoints[i + 2];

                int leftToCenter = center - left;
                int centerToRight = right - center;
                boolean leftValid = leftToCenter > 0;
                boolean rightValid = centerToRight > 0;

                for (int j = left; j < center; j++) {
                    if (j >= 0 && j < filters[i].length && leftValid) {
                        filters[i][j] = (j - left) / (float) leftToCenter;
                    }
                }
                for (int j = center; j < right; j++) {
                    if (j >= 0 && j < filters[i].length && rightValid) {
                        filters[i][j] = (right - j) / (float) centerToRight;
                    }
                }
            }
            return filters;
        }

        private float[] applyMelFilterBank(float[] powerSpectrum, float[][] filters, int maxBin) {
            float[] melEnergies = new float[filters.length];
            int len = Math.min(powerSpectrum.length, maxBin);

            for (int i = 0; i < filters.length; i++) {
                float sum = 0;
                for (int j = 0; j < len && j < filters[i].length; j++) {
                    sum += filters[i][j] * powerSpectrum[j];
                }
                melEnergies[i] = sum;
            }
            return melEnergies;
        }

        private float[] dct(float[] input, int numCeps) {
            int n = input.length;
            if (n <= 0) return new float[numCeps];
            float[] output = new float[numCeps];
            for (int i = 0; i < numCeps; i++) {
                float sum = 0;
                for (int j = 0; j < n; j++) {
                    sum += input[j] * Math.cos(Math.PI * i * (j + 0.5) / n);
                }
                output[i] = sum;
            }
            return output;
        }

        private float hzToMel(float hz) {
            return (float) (2595 * Math.log10(1 + hz / 700.0));
        }

        private float melToHz(float mel) {
            return (float) (700 * (Math.pow(10, mel / 2595.0) - 1));
        }
    }

    // ==================== 内部类：融合MFCC提取 ====================

    private class FusionMfccExtractor {
        private MfccExtractor mfccExtractor = new MfccExtractor();

        public float[] extractFusionMfcc(float[] accelY, float[] gyroY, float sampleRate) {
            float[][] accelMfccSeq = mfccExtractor.extractMfccSequence(accelY, sampleRate);
            float[][] gyroMfccSeq = mfccExtractor.extractMfccSequence(gyroY, sampleRate);

            gyroMfccSeq = normalizeMfccSequence(gyroMfccSeq, calculateMeanStd(accelMfccSeq));

            int numFrames = Math.min(accelMfccSeq.length, gyroMfccSeq.length);
            float[] fusedMfcc = new float[13];

            int midFrame = numFrames / 2;
            for (int i = 0; i < 13; i++) {
                float a = accelMfccSeq[midFrame][i];
                float g = gyroMfccSeq[midFrame][i];
                fusedMfcc[i] = 0.7f * a + 0.3f * g;
            }
            return fusedMfcc;
        }

        private float[][] normalizeMfccSequence(float[][] mfccSeq, float[] meanStd) {
            float mean = meanStd[0];
            float std = meanStd[1];
            if (std < 1e-10f) std = 1f;

            float[][] result = new float[mfccSeq.length][];
            for (int i = 0; i < mfccSeq.length; i++) {
                result[i] = new float[mfccSeq[i].length];
                for (int j = 0; j < mfccSeq[i].length; j++) {
                    result[i][j] = (mfccSeq[i][j] - mean) / std;
                }
            }
            return result;
        }

        private float[] calculateMeanStd(float[][] mfccSeq) {
            if (mfccSeq.length == 0) return new float[]{0f, 1f};
            float sum = 0, sumSq = 0;
            int count = 0;
            for (float[] frame : mfccSeq) {
                for (float v : frame) {
                    sum += v;
                    sumSq += v * v;
                    count++;
                }
            }
            if (count == 0) return new float[]{0f, 1f};
            float mean = sum / count;
            float variance = (sumSq / count) - (mean * mean);
            float std = (float) Math.sqrt(Math.max(0, variance));
            if (std < 1e-10f) std = 1f;
            return new float[]{mean, std};
        }
    }

    // ==================== 内部类：加速度计语音识别器 ====================

    private class AccelVoiceRecognizer {
        // 预训练的数字模板（简化版MFCC均值向量）
        private static final float TEMPLATE_0 = 2.1f;
        private static final float TEMPLATE_1 = 3.2f;
        private static final float TEMPLATE_2 = 2.8f;
        private static final float TEMPLATE_3 = 2.5f;
        private static final float TEMPLATE_4 = 2.3f;
        private static final float TEMPLATE_5 = 1.9f;
        private static final float TEMPLATE_6 = 1.8f;
        private static final float TEMPLATE_7 = 3.0f;
        private static final float TEMPLATE_8 = 2.7f;
        private static final float TEMPLATE_9 = 2.4f;

        // 每个数字的MFCC第一系数（用于简化匹配）
        private final float[] DIGIT_MFCC1 = {
            TEMPLATE_0, TEMPLATE_1, TEMPLATE_2, TEMPLATE_3, TEMPLATE_4,
            TEMPLATE_5, TEMPLATE_6, TEMPLATE_7, TEMPLATE_8, TEMPLATE_9
        };

        public int recognizeDigit(float[] mfcc) {
            if (mfcc == null || mfcc.length == 0) return -1;

            // 使用第一倒谱系数匹配（简化版）
            float mfcc1 = mfcc[0];
            
            float minDist = Float.MAX_VALUE;
            int recognized = 0;

            for (int digit = 0; digit < DIGIT_MFCC1.length; digit++) {
                float dist = Math.abs(mfcc1 - DIGIT_MFCC1[digit]);
                if (dist < minDist) {
                    minDist = dist;
                    recognized = digit;
                }
            }

            // 如果距离太大，返回无效
            if (minDist > 2.0f) return -1;

            return recognized;
        }
    }
}
