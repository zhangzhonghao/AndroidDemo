package com.example.androiddemo.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.androiddemo.R;

public class FlashlightActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashlightOn = false;

    private ImageButton btnFlashlight;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_flashlight);

        btnFlashlight = findViewById(R.id.btn_flashlight);
        tvStatus = findViewById(R.id.tv_status);

        initCameraManager();
        updateUI();

        btnFlashlight.setOnClickListener(v -> toggleFlashlight());
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

    private void toggleFlashlight() {
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

        try {
            if (isFlashlightOn) {
                cameraManager.setTorchMode(cameraId, false);
            } else {
                cameraManager.setTorchMode(cameraId, true);
            }
            isFlashlightOn = !isFlashlightOn;
            updateUI();
        } catch (CameraAccessException e) {
            Toast.makeText(this, "闪光灯控制失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (isFlashlightOn) {
            tvStatus.setText("手电筒已开启");
            btnFlashlight.setImageResource(android.R.drawable.ic_menu_compass);
            btnFlashlight.setBackgroundColor(0xFFFFEB3B); // 黄色
        } else {
            tvStatus.setText("手电筒已关闭");
            btnFlashlight.setImageResource(android.R.drawable.ic_menu_view);
            btnFlashlight.setBackgroundColor(0xFF424242); // 灰色
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleFlashlight();
            } else {
                Toast.makeText(this, "相机权限被拒绝，无法使用闪光灯", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 关闭闪光灯
        if (isFlashlightOn && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException ignored) {
            }
        }
    }
}