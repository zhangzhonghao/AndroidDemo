package com.example.androiddemo.tools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRScannerActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final String TAG = "QRScannerActivity";

    private PreviewView previewView;
    private ImageView ivQrCode;
    private TextView tvResult;
    private EditText etInput;
    private Button btnGenerate;
    private Button btnSwitchTab;
    private View scanTab;
    private View generateTab;

    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private Camera camera;
    private boolean isFlashlightOn = false;
    private boolean isScanning = true;
    private boolean isGenerateMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        initViews();
        initBarcodeScanner();
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
        ivQrCode = findViewById(R.id.iv_qr_code);
        tvResult = findViewById(R.id.tv_result);
        etInput = findViewById(R.id.et_input);
        btnGenerate = findViewById(R.id.btn_generate);
        btnSwitchTab = findViewById(R.id.btn_switch_tab);
        scanTab = findViewById(R.id.scan_tab);
        generateTab = findViewById(R.id.generate_tab);

        findViewById(R.id.btn_flashlight).setOnClickListener(v -> toggleFlashlight());
        btnGenerate.setOnClickListener(v -> generateQrCode());
        btnSwitchTab.setOnClickListener(v -> switchTab());
    }

    private void initBarcodeScanner() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_UPC_A,
                        Barcode.FORMAT_UPC_E,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_ITF,
                        Barcode.FORMAT_DATA_MATRIX
                )
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    if (!isScanning || isGenerateMode) {
                        imageProxy.close();
                        return;
                    }

                    @SuppressWarnings("UnsafeOptInAnnotationUsage")
                    android.media.Image mediaImage = imageProxy.getImage();
                    if (mediaImage != null) {
                        InputImage image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.getImageInfo().getRotationDegrees()
                        );

                        barcodeScanner.process(image)
                                .addOnSuccessListener(barcodes -> {
                                    if (!barcodes.isEmpty()) {
                                        for (Barcode barcode : barcodes) {
                                            String rawValue = barcode.getRawValue();
                                            if (rawValue != null && !rawValue.isEmpty()) {
                                                onBarcodeDetected(barcode);
                                                break;
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e ->
                                        android.util.Log.e(TAG, "Barcode scanning failed", e))
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "相机初始化失败: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void onBarcodeDetected(Barcode barcode) {
        runOnUiThread(() -> {
            isScanning = false;

            // 震动提示
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(200,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            }

            String rawValue = barcode.getRawValue();
            int format = barcode.getFormat();
            String formatName = getFormatName(format);

            String result = "格式: " + formatName + "\n内容: " + rawValue;
            tvResult.setText(result);

            // URL 自动识别跳转
            if (barcode.getValueType() == Barcode.TYPE_URL) {
                String url = barcode.getUrl().getUrl();
                tvResult.append("\n\n检测到链接，点击跳转");
                tvResult.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                });
            } else {
                tvResult.setOnClickListener(null);
            }

            Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show();
        });
    }

    private String getFormatName(int format) {
        switch (format) {
            case Barcode.FORMAT_QR_CODE:
                return "二维码";
            case Barcode.FORMAT_AZTEC:
                return "Aztec";
            case Barcode.FORMAT_EAN_13:
                return "EAN-13";
            case Barcode.FORMAT_EAN_8:
                return "EAN-8";
            case Barcode.FORMAT_UPC_A:
                return "UPC-A";
            case Barcode.FORMAT_UPC_E:
                return "UPC-E";
            case Barcode.FORMAT_CODE_128:
                return "Code-128";
            case Barcode.FORMAT_CODE_39:
                return "Code-39";
            case Barcode.FORMAT_CODE_93:
                return "Code-93";
            case Barcode.FORMAT_CODABAR:
                return "Codabar";
            case Barcode.FORMAT_ITF:
                return "ITF";
            case Barcode.FORMAT_DATA_MATRIX:
                return "Data Matrix";
            default:
                return "未知格式";
        }
    }

    private void toggleFlashlight() {
        if (camera != null) {
            if (isFlashlightOn) {
                camera.getCameraControl().enableTorch(false);
            } else {
                camera.getCameraControl().enableTorch(true);
            }
            isFlashlightOn = !isFlashlightOn;
            Toast.makeText(this, isFlashlightOn ? "闪光灯已开启" : "闪光灯已关闭",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void switchTab() {
        isGenerateMode = !isGenerateMode;
        isScanning = !isGenerateMode;

        if (isGenerateMode) {
            scanTab.setVisibility(View.GONE);
            generateTab.setVisibility(View.VISIBLE);
            btnSwitchTab.setText("扫描");
            if (camera != null) {
                camera.getCameraControl().enableTorch(false);
                isFlashlightOn = false;
            }
        } else {
            scanTab.setVisibility(View.VISIBLE);
            generateTab.setVisibility(View.GONE);
            btnSwitchTab.setText("生成");
            tvResult.setText("");
            tvResult.setOnClickListener(null);
        }
    }

    private void generateQrCode() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            int size = 512;
            com.google.zxing.common.BitMatrix bitMatrix =
                    qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ?
                            0xFF000000 : 0xFFFFFFFF);
                }
            }

            ivQrCode.setImageBitmap(bitmap);
            Toast.makeText(this, "二维码生成成功", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                Toast.makeText(this, "相机权限被拒绝，无法使用扫描功能",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        barcodeScanner.close();
        if (camera != null) {
            camera.getCameraControl().enableTorch(false);
        }
    }
}