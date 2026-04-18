package com.example.androiddemo.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;

public class FlashRhythmActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int MIN_BPM = 30;
    private static final int MAX_BPM = 240;
    private static final int DEFAULT_BPM = 120;

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashlightOn = false;

    private TextView tvBpm;
    private TextView tvBeatCount;
    private SeekBar seekbarBpm;
    private View flashIndicator;
    private Button btnStartStop;
    private Button btnBpmMinus;
    private Button btnBpmPlus;

    private int currentBpm = DEFAULT_BPM;
    private int beatCount = 0;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private Runnable flashRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                toggleFlash();
                scheduleNextFlash();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_flash_rhythm);

        initViews();
        initCameraManager();
        setupListeners();
        updateBpmDisplay();
    }

    private void initViews() {
        tvBpm = findViewById(R.id.tv_bpm);
        tvBeatCount = findViewById(R.id.tv_beat_count);
        seekbarBpm = findViewById(R.id.seekbar_bpm);
        flashIndicator = findViewById(R.id.flash_indicator);
        btnStartStop = findViewById(R.id.btn_start_stop);
        btnBpmMinus = findViewById(R.id.btn_bpm_minus);
        btnBpmPlus = findViewById(R.id.btn_bpm_plus);

        seekbarBpm.setMax(MAX_BPM - MIN_BPM);
        seekbarBpm.setProgress(DEFAULT_BPM - MIN_BPM);
    }

    private void initCameraManager() {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (hasFlash != null && hasFlash) {
                    cameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "相机访问失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        btnBpmMinus.setOnClickListener(v -> {
            if (currentBpm > MIN_BPM) {
                currentBpm--;
                updateBpmDisplay();
            }
        });

        btnBpmPlus.setOnClickListener(v -> {
            if (currentBpm < MAX_BPM) {
                currentBpm++;
                updateBpmDisplay();
            }
        });

        seekbarBpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentBpm = progress + MIN_BPM;
                    updateBpmDisplay();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnStartStop.setOnClickListener(v -> {
            if (cameraId == null) {
                Toast.makeText(this, "该设备没有闪光灯", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }

            if (isRunning) {
                stopRhythm();
            } else {
                startRhythm();
            }
        });
    }

    private void updateBpmDisplay() {
        tvBpm.setText(String.valueOf(currentBpm));
    }

    private void startRhythm() {
        isRunning = true;
        beatCount = 0;
        btnStartStop.setText("停止");
        seekbarBpm.setEnabled(false);
        findViewById(R.id.btn_bpm_minus).setEnabled(false);
        findViewById(R.id.btn_bpm_plus).setEnabled(false);
        updateBeatCountDisplay();
        scheduleNextFlash();
    }

    private void stopRhythm() {
        isRunning = false;
        handler.removeCallbacks(flashRunnable);
        btnStartStop.setText("开始");
        seekbarBpm.setEnabled(true);
        findViewById(R.id.btn_bpm_minus).setEnabled(true);
        findViewById(R.id.btn_bpm_plus).setEnabled(true);

        // Turn off flash if it's on
        if (isFlashlightOn && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
                isFlashlightOn = false;
            } catch (CameraAccessException ignored) {
            }
        }

        // Reset indicator
        flashIndicator.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_button_background));
        beatCount = 0;
        updateBeatCountDisplay();
    }

    private void scheduleNextFlash() {
        long interval = (long) (60000.0 / currentBpm);
        handler.postDelayed(flashRunnable, interval);
    }

    private void toggleFlash() {
        try {
            if (isFlashlightOn) {
                cameraManager.setTorchMode(cameraId, false);
                isFlashlightOn = false;
                flashIndicator.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_button_background));
            } else {
                cameraManager.setTorchMode(cameraId, true);
                isFlashlightOn = true;
                flashIndicator.setBackgroundColor(0xFFFFEB3B); // Yellow
            }
            beatCount++;
            updateBeatCountDisplay();
        } catch (CameraAccessException e) {
            Toast.makeText(this, "闪光灯控制失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBeatCountDisplay() {
        tvBeatCount.setText("节拍: " + beatCount);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isRunning) {
                    startRhythm();
                }
            } else {
                Toast.makeText(this, "相机权限被拒绝，无法使用闪光灯", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacks(flashRunnable);
        if (isFlashlightOn && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException ignored) {
            }
        }
    }
}