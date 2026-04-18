package com.example.androiddemo.tools;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import com.example.androiddemo.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageCompressorActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_CAMERA_PERMISSION = 1002;

    private ImageView ivOriginalImage;
    private ImageView ivCompressedPreview;
    private TextView tvOriginalInfo;
    private TextView tvCompressedSize;
    private TextView tvQualityValue;
    private SeekBar seekBarQuality;
    private RadioGroup rgResolution;
    private RadioButton rbOriginal;
    private RadioButton rb75;
    private RadioButton rb50;
    private RadioButton rb25;
    private Button btnSelectImage;
    private Button btnTakePhoto;
    private Button btnSelectMultiple;
    private Button btnCompress;
    private Button btnSave;

    private Uri originalImageUri;
    private Bitmap originalBitmap;
    private Bitmap compressedBitmap;
    private int selectedQuality = 80;
    private float selectedScale = 1.0f;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private boolean isBatchMode = false;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String[]> pickMultipleLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_compressor);

        initActivityResultLaunchers();
        initViews();
        setupListeners();
    }

    private void initActivityResultLaunchers() {
        pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        originalImageUri = uri;
                        loadImage(uri);
                    }
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && originalImageUri != null) {
                        loadImage(originalImageUri);
                    }
                });

        pickMultipleLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        selectedImageUris.clear();
                        selectedImageUris.addAll(uris);
                        isBatchMode = true;
                        Toast.makeText(this, "已选择 " + uris.size() + " 张图片", Toast.LENGTH_SHORT).show();
                        updateOriginalInfo();
                    }
                });
    }

    private void initViews() {
        ivOriginalImage = findViewById(R.id.iv_original_image);
        ivCompressedPreview = findViewById(R.id.iv_compressed_preview);
        tvOriginalInfo = findViewById(R.id.tv_original_info);
        tvCompressedSize = findViewById(R.id.tv_compressed_size);
        tvQualityValue = findViewById(R.id.tv_quality_value);
        seekBarQuality = findViewById(R.id.seekbar_quality);
        rgResolution = findViewById(R.id.rg_resolution);
        rbOriginal = findViewById(R.id.rb_original);
        rb75 = findViewById(R.id.rb_75);
        rb50 = findViewById(R.id.rb_50);
        rb25 = findViewById(R.id.rb_25);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnSelectMultiple = findViewById(R.id.btn_select_multiple);
        btnCompress = findViewById(R.id.btn_compress);
        btnSave = findViewById(R.id.btn_save);

        btnSave.setEnabled(false);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> {
            isBatchMode = false;
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        btnTakePhoto.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                isBatchMode = false;
                launchCamera();
            }
        });

        btnSelectMultiple.setOnClickListener(v -> {
            isBatchMode = true;
            pickMultipleLauncher.launch(new String[]{"image/*"});
        });

        seekBarQuality.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 10) {
                    seekBar.setProgress(10);
                    progress = 10;
                }
                selectedQuality = progress;
                tvQualityValue.setText(progress + "%");
                updateCompressedSizeEstimate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        rgResolution.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_original) {
                selectedScale = 1.0f;
            } else if (checkedId == R.id.rb_75) {
                selectedScale = 0.75f;
            } else if (checkedId == R.id.rb_50) {
                selectedScale = 0.5f;
            } else if (checkedId == R.id.rb_25) {
                selectedScale = 0.25f;
            }
            updateCompressedSizeEstimate();
        });

        btnCompress.setOnClickListener(v -> compressImage());

        btnSave.setOnClickListener(v -> saveCompressedImage());
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    private void launchCamera() {
        Uri imageUri = createImageUri();
        if (imageUri != null) {
            originalImageUri = imageUri;
            takePictureLauncher.launch(imageUri);
        }
    }

    private Uri createImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        }
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void loadImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            originalBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (originalBitmap == null) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            long size = getFileSize(uri);

            ivOriginalImage.setImageBitmap(originalBitmap);
            String info = String.format(Locale.getDefault(),
                    "尺寸: %d x %d 像素\n大小: %s",
                    width, height, formatFileSize(size));
            tvOriginalInfo.setText(info);

            updateCompressedSizeEstimate();
            btnCompress.setEnabled(true);

        } catch (Exception e) {
            Toast.makeText(this, "加载图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private long getFileSize(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                long size = inputStream.available();
                inputStream.close();
                return size;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.2f KB", size / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.2f MB", size / (1024.0 * 1024.0));
        }
    }

    private void updateOriginalInfo() {
        if (isBatchMode && !selectedImageUris.isEmpty()) {
            int totalCount = selectedImageUris.size();
            tvOriginalInfo.setText(String.format(Locale.getDefault(),
                    "已选择 %d 张图片", totalCount));
            ivOriginalImage.setImageResource(R.drawable.ic_image_placeholder);
            btnCompress.setEnabled(true);
        }
    }

    private void updateCompressedSizeEstimate() {
        if (originalBitmap == null && !isBatchMode) {
            tvCompressedSize.setText("预估大小: --");
            return;
        }

        long estimatedSize;
        if (isBatchMode) {
            long totalOriginalSize = 0;
            for (Uri uri : selectedImageUris) {
                totalOriginalSize += getFileSize(uri);
            }
            estimatedSize = (long) (totalOriginalSize * (selectedQuality / 100.0) * selectedScale * selectedScale);
            tvCompressedSize.setText(String.format(Locale.getDefault(),
                    "预估总大小: %s (批量 %d 张)",
                    formatFileSize(estimatedSize), selectedImageUris.size()));
        } else {
            long originalSize = getFileSize(originalImageUri);
            estimatedSize = (long) (originalSize * (selectedQuality / 100.0) * selectedScale * selectedScale);
            tvCompressedSize.setText(String.format(Locale.getDefault(),
                    "预估大小: %s", formatFileSize(estimatedSize)));
        }
    }

    private void compressImage() {
        if (isBatchMode) {
            compressBatchImages();
        } else {
            compressSingleImage();
        }
    }

    private void compressSingleImage() {
        if (originalBitmap == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                int originalWidth = originalBitmap.getWidth();
                int originalHeight = originalBitmap.getHeight();
                int newWidth = (int) (originalWidth * selectedScale);
                int newHeight = (int) (originalHeight * selectedScale);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);

                android.graphics.Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                String formatName = getImageFormat(originalImageUri);
                if ("png".equalsIgnoreCase(formatName)) {
                    compressFormat = Bitmap.CompressFormat.PNG;
                }

                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                scaledBitmap.compress(compressFormat, selectedQuality, baos);
                compressedBitmap = Bitmap.createBitmap(bitmapFactoryByteArray(baos.toByteArray()));

                if (scaledBitmap != originalBitmap) {
                    scaledBitmap.recycle();
                }

                runOnUiThread(() -> {
                    ivCompressedPreview.setImageBitmap(compressedBitmap);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "压缩完成", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "压缩失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void compressBatchImages() {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("批量压缩")
                .setMessage("开始压缩 " + selectedImageUris.size() + " 张图片？")
                .setPositiveButton("确定", (dialog, which) -> {
                    new Thread(() -> {
                        int successCount = 0;
                        int failCount = 0;

                        for (Uri uri : selectedImageUris) {
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(uri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                inputStream.close();

                                if (bitmap != null) {
                                    int originalWidth = bitmap.getWidth();
                                    int originalHeight = bitmap.getHeight();
                                    int newWidth = (int) (originalWidth * selectedScale);
                                    int newHeight = (int) (originalHeight * selectedScale);

                                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, selectedQuality, baos);

                                    scaledBitmap.recycle();
                                    bitmap.recycle();

                                    successCount++;
                                } else {
                                    failCount++;
                                }
                            } catch (Exception e) {
                                failCount++;
                            }
                        }

                        final int finalSuccess = successCount;
                        final int finalFail = failCount;
                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    String.format(Locale.getDefault(),
                                            "批量压缩完成\n成功: %d 张\n失败: %d 张",
                                            finalSuccess, finalFail),
                                    Toast.LENGTH_LONG).show();
                            btnSave.setEnabled(true);
                        });

                    }).start();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private Bitmap bitmapFactoryByteArray(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    private String getImageFormat(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            return options.outMimeType;
        } catch (Exception e) {
            return "jpeg";
        }
    }

    private void saveCompressedImage() {
        if (!checkStoragePermission()) {
            return;
        }

        if (isBatchMode) {
            saveBatchImages();
        } else {
            saveSingleImage();
        }
    }

    private void saveSingleImage() {
        if (compressedBitmap == null) {
            Toast.makeText(this, "请先压缩图片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date());
                String fileName = "COMPRESSED_" + timeStamp + ".jpg";

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Compressed");
                }

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, selectedQuality, outputStream);
                        outputStream.close();
                    }

                    runOnUiThread(() -> {
                        Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveBatchImages() {
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "没有可保存的图片", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            int successCount = 0;
            int failCount = 0;

            for (Uri uri : selectedImageUris) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    if (bitmap != null) {
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                .format(new Date());
                        String fileName = "COMPRESSED_" + timeStamp + "_" + System.currentTimeMillis() + ".jpg";

                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Compressed");
                        }

                        Uri saveUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        if (saveUri != null) {
                            OutputStream outputStream = getContentResolver().openOutputStream(saveUri);
                            if (outputStream != null) {
                                int originalWidth = bitmap.getWidth();
                                int originalHeight = bitmap.getHeight();
                                int newWidth = (int) (originalWidth * selectedScale);
                                int newHeight = (int) (originalHeight * selectedScale);

                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, selectedQuality, outputStream);
                                outputStream.close();
                                scaledBitmap.recycle();
                            }
                            successCount++;
                        } else {
                            failCount++;
                        }
                        bitmap.recycle();
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }

            final int finalSuccess = successCount;
            final int finalFail = failCount;
            runOnUiThread(() -> {
                Toast.makeText(this,
                        String.format(Locale.getDefault(),
                                "批量保存完成\n成功: %d 张\n失败: %d 张",
                                finalSuccess, finalFail),
                        Toast.LENGTH_LONG).show();
            });

        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveCompressedImage();
            } else {
                Toast.makeText(this, "存储权限被拒绝，无法保存图片", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (originalBitmap != null && !originalBitmap.isRecycled()) {
            originalBitmap.recycle();
        }
        if (compressedBitmap != null && !compressedBitmap.isRecycled()) {
            compressedBitmap.recycle();
        }
    }
}