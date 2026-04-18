package com.example.androiddemo.tools;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.example.androiddemo.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WatermarkCameraActivity extends AppCompatActivity
        implements WatermarkSettingsDialog.OnSettingsApplyListener {

    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;
    private static final int REQUEST_LOCATION_PERMISSION = 1003;
    private static final int REQUEST_GALLERY = 1004;

    private PreviewView previewView;
    private TextView tvWatermarkPreview;
    private ProgressBar progressBar;
    private ImageButton btnCapture;
    private ImageButton btnSwitchCamera;
    private ImageButton btnGallery;

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private Camera camera;
    private boolean isBackCamera = true;

    private WatermarkSettingsDialog.WatermarkSettings watermarkSettings =
            new WatermarkSettingsDialog.WatermarkSettings();
    private LocationManager locationManager;
    private Location currentLocation;
    private String currentDateTime;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        processGalleryImage(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_watermark_camera);

        initViews();
        initLocation();
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }

        updateWatermarkPreview();
    }

    private void initViews() {
        previewView = findViewById(R.id.preview_view);
        tvWatermarkPreview = findViewById(R.id.tv_watermark_preview);
        progressBar = findViewById(R.id.progress_bar);
        btnCapture = findViewById(R.id.btn_capture);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        btnGallery = findViewById(R.id.btn_gallery);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_settings).setOnClickListener(v -> showSettingsDialog());
        btnCapture.setOnClickListener(v -> capturePhoto());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
        btnGallery.setOnClickListener(v -> openGallery());
    }

    private void initLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10,
                    locationListener
            );
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            currentLocation = location;
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }
    };

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build();

                CameraSelector cameraSelector = isBackCamera ?
                        CameraSelector.DEFAULT_BACK_CAMERA :
                        CameraSelector.DEFAULT_FRONT_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "相机初始化失败: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void switchCamera() {
        isBackCamera = !isBackCamera;
        startCamera();
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "相机未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "WM_" + dateFormat.format(new Date()) + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/WatermarkCamera");

            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(
                            getContentResolver(),
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                    ).build();

            imageCapture.takePicture(outputOptions, cameraExecutor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                Toast.makeText(WatermarkCameraActivity.this,
                                        "照片已保存", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                Toast.makeText(WatermarkCameraActivity.this,
                                        "保存失败: " + exception.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } else {
            File outputDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "WatermarkCamera");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, fileName);

            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(outputFile).build();

            imageCapture.takePicture(outputOptions, cameraExecutor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                Toast.makeText(WatermarkCameraActivity.this,
                                        "照片已保存", Toast.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            runOnUiThread(() -> {
                                showProgress(false);
                                Toast.makeText(WatermarkCameraActivity.this,
                                        "保存失败: " + exception.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void processGalleryImage(Uri imageUri) {
        showProgress(true);

        cameraExecutor.execute(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (originalBitmap == null) {
                    runOnUiThread(() -> {
                        showProgress(false);
                        Toast.makeText(this, "无法读取图片", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Bitmap watermarkedBitmap = addWatermarkToBitmap(originalBitmap);
                saveBitmapToGallery(watermarkedBitmap);

                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "水印图片已保存", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    showProgress(false);
                    Toast.makeText(this, "处理失败: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Bitmap addWatermarkToBitmap(Bitmap originalBitmap) {
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(watermarkSettings.textColor);
        paint.setTextSize(watermarkSettings.fontSize * 3);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setAlpha((int) (watermarkSettings.alpha * 255));

        String watermarkText = buildWatermarkText();
        if (watermarkText.isEmpty()) {
            return mutableBitmap;
        }

        float padding = 30;
        float textWidth = paint.measureText(watermarkText);
        float textHeight = paint.getTextSize();

        float x, y;
        switch (watermarkSettings.position) {
            case WatermarkSettingsDialog.WatermarkSettings.Position.TOP_LEFT:
                x = padding;
                y = textHeight + padding;
                break;
            case WatermarkSettingsDialog.WatermarkSettings.Position.TOP_RIGHT:
                x = mutableBitmap.getWidth() - textWidth - padding;
                y = textHeight + padding;
                break;
            case WatermarkSettingsDialog.WatermarkSettings.Position.BOTTOM_RIGHT:
                x = mutableBitmap.getWidth() - textWidth - padding;
                y = mutableBitmap.getHeight() - padding;
                break;
            case WatermarkSettingsDialog.WatermarkSettings.Position.BOTTOM_LEFT:
            default:
                x = padding;
                y = mutableBitmap.getHeight() - padding;
                break;
        }

        // 绘制阴影
        Paint shadowPaint = new Paint(paint);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(100);
        canvas.drawText(watermarkText, x + 2, y + 2, shadowPaint);

        // 绘制文字
        canvas.drawText(watermarkText, x, y, paint);

        return mutableBitmap;
    }

    private String buildWatermarkText() {
        StringBuilder sb = new StringBuilder();

        if (watermarkSettings.showDateTime) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            currentDateTime = df.format(new Date());
            sb.append(currentDateTime);
        }

        if (watermarkSettings.showLocation && currentLocation != null) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(String.format(Locale.getDefault(), "%.6f, %.6f",
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()));
        }

        if (watermarkSettings.customText != null &&
                !watermarkSettings.customText.isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(watermarkSettings.customText);
        }

        return sb.toString();
    }

    private void updateWatermarkPreview() {
        String previewText = buildWatermarkText();
        tvWatermarkPreview.setText(previewText);
        tvWatermarkPreview.setTextSize(watermarkSettings.fontSize);
        tvWatermarkPreview.setTextColor(watermarkSettings.textColor);
        tvWatermarkPreview.setAlpha(watermarkSettings.alpha);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                tvWatermarkPreview.getLayoutParams();
        switch (watermarkSettings.position) {
            case WatermarkSettingsDialog.WatermarkSettings.Position.TOP_LEFT:
                params.gravity = Gravity.TOP | Gravity.START;
                params.leftMargin = 16;
                params.topMargin = 72;
                break;
            case WatermarkSettingsDialog.WatermarkSettings.Position.TOP_RIGHT:
                params.gravity = Gravity.TOP | Gravity.END;
                params.rightMargin = 16;
                params.topMargin = 72;
                break;
            case WatermarkSettingsDialog.WatermarkSettings.Position.BOTTOM_RIGHT:
                params.gravity = Gravity.BOTTOM | Gravity.END;
                params.rightMargin = 16;
                params.bottomMargin = 140;
                break;
            case WatermarkSettingsDialog.WatermarkSettings.Position.BOTTOM_LEFT:
            default:
                params.gravity = Gravity.BOTTOM | Gravity.START;
                params.leftMargin = 16;
                params.bottomMargin = 140;
                break;
        }
        tvWatermarkPreview.setLayoutParams(params);
    }

    private void showSettingsDialog() {
        WatermarkSettingsDialog dialog =
                WatermarkSettingsDialog.newInstance(watermarkSettings);
        dialog.setOnSettingsApplyListener(this);
        dialog.show(getSupportFragmentManager(), "watermark_settings");
    }

    @Override
    public void onSettingsApply(WatermarkSettingsDialog.WatermarkSettings settings) {
        this.watermarkSettings = settings;
        updateWatermarkPreview();
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String fileName = "WM_" + dateFormat.format(new Date()) + ".jpg";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/WatermarkCamera");

            Uri uri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                try (FileOutputStream fos = (FileOutputStream)
                        getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                } catch (Exception e) {
                    Log.e("WatermarkCamera", "Failed to save bitmap", e);
                }
            }
        } else {
            File outputDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "WatermarkCamera");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            } catch (Exception e) {
                Log.e("WatermarkCamera", "Failed to save bitmap", e);
            }
        }
    }

    private void showProgress(boolean show) {
        runOnUiThread(() -> {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            btnCapture.setEnabled(!show);
            btnSwitchCamera.setEnabled(!show);
            btnGallery.setEnabled(!show);
        });
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
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}