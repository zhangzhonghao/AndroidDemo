package com.example.androiddemo.tools;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class VibrationTestActivity extends AppCompatActivity {

    private Vibrator vibrator;
    private TextView tvStatus;
    private Button btnShort;
    private Button btnLong;
    private Button btnPulse;
    private Button btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_vibration_test);

        initVibrator();
        initViews();
        updateStatus("请选择震动模式");
    }

    private void initVibrator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        if (vibrator == null || !vibrator.hasVibrator()) {
            Toast.makeText(this, "该设备没有震动马达", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        tvStatus = findViewById(R.id.tv_status);
        btnShort = findViewById(R.id.btn_short);
        btnLong = findViewById(R.id.btn_long);
        btnPulse = findViewById(R.id.btn_pulse);
        btnStop = findViewById(R.id.btn_stop);

        btnShort.setOnClickListener(v -> vibrateShort());
        btnLong.setOnClickListener(v -> vibrateLong());
        btnPulse.setOnClickListener(v -> vibratePulse());
        btnStop.setOnClickListener(v -> stopVibration());
    }

    private void updateStatus(String status) {
        tvStatus.setText(status);
    }

    private void vibrateShort() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            Toast.makeText(this, "该设备没有震动马达", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
        updateStatus("短震模式 (100ms)");
    }

    private void vibrateLong() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            Toast.makeText(this, "该设备没有震动马达", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(1000);
        }
        updateStatus("长震模式 (1000ms)");
    }

    private void vibratePulse() {
        if (vibrator == null || !vibrator.hasVibrator()) {
            Toast.makeText(this, "该设备没有震动马达", Toast.LENGTH_SHORT).show();
            return;
        }

        // 脉冲模式: 震动200ms, 停止100ms, 重复3次
        long[] pattern = {0, 200, 100, 200, 100, 200};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
        updateStatus("脉冲模式 (200ms x 3)");
    }

    private void stopVibration() {
        if (vibrator != null) {
            vibrator.cancel();
        }
        updateStatus("震动已停止");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}