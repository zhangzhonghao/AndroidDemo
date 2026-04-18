package com.example.androiddemo.tools;

import android.Manifest;
import android.content.pm.PackageManager;
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
import java.io.IOException;

/**
 * 噪音计 Activity
 */
public class NoiseMeterActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final long UPDATE_INTERVAL = 100; // 100ms 更新

    private NoiseMeterView noiseMeterView;
    private TextView tvCurrentDb;
    private TextView tvDbLevel;
    private TextView tvMaxDb;
    private TextView tvMinDb;
    private TextView tvAvgDb;

    private MediaRecorder mediaRecorder;
    private Handler handler;
    private Runnable updateRunnable;

    private boolean isRecording = false;

    // 历史记录
    private float maxDb = 0f;
    private float minDb = Float.MAX_VALUE;
    private float sumDb = 0f;
    private int sampleCount = 0;

    // 校准偏移量
    private float calibrationOffset = 0f;

    // 分贝等级阈值
    private static final float DB_QUIET = 40f;
    private static final float DB_NORMAL = 60f;
    private static final float DB_LOUD = 80f;
    private static final float DB_VERY_LOUD = 100f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_noise_meter);

        initViews();
        initHandler();

        checkAndRequestPermission();
    }

    private void initViews() {
        noiseMeterView = findViewById(R.id.noise_meter_view);
        tvCurrentDb = findViewById(R.id.tv_current_db);
        tvDbLevel = findViewById(R.id.tv_db_level);
        tvMaxDb = findViewById(R.id.tv_max_db);
        tvMinDb = findViewById(R.id.tv_min_db);
        tvAvgDb = findViewById(R.id.tv_avg_db);
    }

    private void initHandler() {
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRecording) {
                    updateNoiseLevel();
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
                Toast.makeText(this, "需要录音权限才能使用噪音计", Toast.LENGTH_LONG).show();
                tvCurrentDb.setText("--");
                tvDbLevel.setText("无权限");
            }
        }
    }

    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setAudioSamplingRate(44100);
            // 不需要实际输出文件
            mediaRecorder.setOutputFile("/dev/null");

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            handler.post(updateRunnable);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法启动录音，请检查权限", Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecording() {
        isRecording = false;
        handler.removeCallbacks(updateRunnable);

        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaRecorder = null;
        }
    }

    private void updateNoiseLevel() {
        if (mediaRecorder == null) return;

        try {
            int amplitude = mediaRecorder.getMaxAmplitude();

            // 转换为分贝
            float db = NoiseMeterView.amplitudeToDb(amplitude);

            // 应用校准偏移
            db += calibrationOffset;

            // 确保分贝值在合理范围
            if (db < 0) db = 0;
            if (db > 120) db = 120;

            // 添加到波形视图
            noiseMeterView.addAmplitude(amplitude);

            // 更新统计
            updateStatistics(db);

            // 更新UI
            updateUI(db);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatistics(float db) {
        sampleCount++;

        if (db > maxDb) {
            maxDb = db;
        }
        if (db < minDb) {
            minDb = db;
        }
        sumDb += db;

        float avgDb = sumDb / sampleCount;

        tvMaxDb.setText(String.format("最高: %.0f dB", maxDb));
        tvMinDb.setText(String.format("最低: %.0f dB", minDb));
        tvAvgDb.setText(String.format("平均: %.0f dB", avgDb));
    }

    private void updateUI(float db) {
        // 更新当前分贝显示
        tvCurrentDb.setText(String.format("%.0f", db));

        // 更新分贝等级
        String level;
        int color;
        if (db < DB_QUIET) {
            level = "安静";
            color = ContextCompat.getColor(this, R.color.db_quiet);
        } else if (db < DB_NORMAL) {
            level = "一般";
            color = ContextCompat.getColor(this, R.color.db_normal);
        } else if (db < DB_LOUD) {
            level = "嘈杂";
            color = ContextCompat.getColor(this, R.color.db_loud);
        } else if (db < DB_VERY_LOUD) {
            level = "非常嘈杂";
            color = ContextCompat.getColor(this, R.color.db_very_loud);
        } else {
            level = "危险";
            color = ContextCompat.getColor(this, R.color.db_danger);
        }

        tvDbLevel.setText(level);
        tvDbLevel.setTextColor(color);
    }

    public void onCalibrateClick(View view) {
        // 校准：假设当前环境为40dB安静环境
        if (mediaRecorder != null) {
            try {
                int amplitude = mediaRecorder.getMaxAmplitude();
                float currentDb = NoiseMeterView.amplitudeToDb(amplitude);
                // 设置校准偏移，使当前值接近40dB
                calibrationOffset = 40f - currentDb;

                Toast.makeText(this, "已校准（基准40dB）", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "校准失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResetClick(View view) {
        // 重置统计数据
        maxDb = 0f;
        minDb = Float.MAX_VALUE;
        sumDb = 0f;
        sampleCount = 0;

        tvMaxDb.setText("最高: -- dB");
        tvMinDb.setText("最低: -- dB");
        tvAvgDb.setText("平均: -- dB");

        // 清空波形
        noiseMeterView.clearHistory();

        Toast.makeText(this, "已重置统计数据", Toast.LENGTH_SHORT).show();
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