package com.example.androiddemo.tools;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.androiddemo.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ColorPickerScreenActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private PreviewView previewView;
    private View colorPreview;
    private TextView tvHex;
    private TextView tvRgb;
    private LinearLayout colorInfoBar;
    private ImageButton btnBack;
    private ImageButton btnInfo;
    private TextView tvHint;

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_color_picker_screen);

        initViews();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }

    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        colorPreview = findViewById(R.id.color_preview);
        tvHex = findViewById(R.id.tv_hex);
        tvRgb = findViewById(R.id.tv_rgb);
        colorInfoBar = findViewById(R.id.color_info_bar);
        btnBack = findViewById(R.id.btn_back);
        btnInfo = findViewById(R.id.btn_info);
        tvHint = findViewById(R.id.tv_hint);

        btnBack.setOnClickListener(v -> finish());
        btnInfo.setOnClickListener(v -> showInfoDialog());

        // 点击预览区域取色
        previewView.setOnClickListener(v -> captureColor());

        findViewById(R.id.btn_copy).setOnClickListener(v -> copyHexToClipboard());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "相机初始化失败: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureColor() {
        if (imageCapture == null) {
            Toast.makeText(this, "相机未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        imageCapture.takePicture(cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        int centerX = image.getWidth() / 2;
                        int centerY = image.getHeight() / 2;

                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        int[] pixels = new int[image.getWidth() * image.getHeight()];
                        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(),
                                Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);

                        int centerPixel = bitmap.getPixel(centerX, centerY);
                        int color = normalizeColor(centerPixel);
                        bitmap.recycle();

                        image.close();

                        runOnUiThread(() -> showColorResult(color));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() ->
                                Toast.makeText(ColorPickerScreenActivity.this,
                                        "取色失败: " + exception.getMessage(),
                                        Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    private int normalizeColor(int color) {
        return Color.argb(
                255,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
        );
    }

    private void showColorResult(int color) {
        colorInfoBar.setVisibility(View.VISIBLE);
        colorPreview.setBackgroundColor(color);

        String hex = String.format("#%06X", (0xFFFFFF & color));
        tvHex.setText(hex);

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        tvRgb.setText(String.format("%d, %d, %d", r, g, b));

        tvHint.setVisibility(View.GONE);
    }

    private void copyHexToClipboard() {
        String hex = tvHex.getText().toString();
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Color HEX", hex);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "已复制: " + hex, Toast.LENGTH_SHORT).show();
    }

    private void showInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("取色器说明")
                .setMessage("1. 点击相机预览区域的任意位置取色\n\n" +
                        "2. 取色结果会显示 HEX 和 RGB 值\n\n" +
                        "3. 点击「复制 HEX」可将颜色值复制到剪贴板")
                .setPositiveButton("知道了", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}