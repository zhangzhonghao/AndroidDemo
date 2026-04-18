package com.example.androiddemo.tools;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.androiddemo.R;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;

public class OcrActivity extends AppCompatActivity {

    private static final int MAX_IMAGE_SIZE = 2048;

    private ImageView ivImage;
    private ScrollView scrollResult;
    private TextView tvResult;
    private Button btnSelectGallery;
    private Button btnTakePhoto;
    private Button btnCopyResult;

    private Bitmap selectedBitmap;
    private Uri photoUri;
    private ExecutorService executor;
    private Handler mainHandler;
    private TextRecognizer textRecognizer;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        loadImageFromUri(selectedUri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && photoUri != null) {
                    loadImageFromUri(photoUri);
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveResultToGallery();
                } else {
                    Toast.makeText(this, "需要存储权限才能保存结果", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        initViews();
    }

    private void initViews() {
        ivImage = findViewById(R.id.iv_image);
        scrollResult = findViewById(R.id.scroll_result);
        tvResult = findViewById(R.id.tv_result);
        btnSelectGallery = findViewById(R.id.btn_select_gallery);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnCopyResult = findViewById(R.id.btn_copy_result);

        ivImage.setImageResource(R.drawable.ic_image_placeholder);
    }

    public void onSelectGalleryClick(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    public void onTakePhotoClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "ocr_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            }
            photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (photoUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePhotoLauncher.launch(intent);
            }
        } else {
            Toast.makeText(this, "没有可用的相机应用", Toast.LENGTH_SHORT).show();
        }
    }

    public void onImageClick(View view) {
        if (selectedBitmap != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ImageView previewView = new ImageView(this);
            previewView.setImageBitmap(selectedBitmap);
            builder.setView(previewView);
            builder.setNegativeButton("关闭", null);
            builder.show();
        } else {
            onSelectGalleryClick(view);
        }
    }

    public void onRecognizeClick(View view) {
        if (selectedBitmap == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }

        tvResult.setText("正在识别中...");
        scrollResult.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            try {
                InputImage image = InputImage.fromBitmap(selectedBitmap, 0);
                textRecognizer.process(image)
                        .addOnSuccessListener(text -> {
                            String result = extractText(text);
                            mainHandler.post(() -> {
                                tvResult.setText(result);
                                if (result.isEmpty()) {
                                    tvResult.setText("未识别到文字");
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            mainHandler.post(() ->
                                    tvResult.setText("识别出错: " + e.getMessage())
                            );
                        });
            } catch (Exception e) {
                mainHandler.post(() ->
                        tvResult.setText("识别出错: " + e.getMessage())
                );
            }
        });
    }

    private String extractText(Text text) {
        if (text.getTextBlocks().isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (Text.TextBlock block : text.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                result.append(line.getText());
                result.append("\n");
            }
            result.append("\n");
        }
        return result.toString().trim();
    }

    public void onCopyResultClick(View view) {
        String text = tvResult.getText().toString();
        if (text.isEmpty() || text.equals("识别结果将在此显示") || text.equals("未识别到文字")) {
            Toast.makeText(this, "没有可复制的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        copyToClipboard(text);
        Toast.makeText(this, "结果已复制", Toast.LENGTH_SHORT).show();
    }

    public void onSaveResultClick(View view) {
        String text = tvResult.getText().toString();
        if (text.isEmpty() || text.equals("识别结果将在此显示") || text.equals("未识别到文字")) {
            Toast.makeText(this, "没有可保存的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                saveResultToGallery();
            }
        } else {
            saveResultToGallery();
        }
    }

    private void loadImageFromUri(Uri uri) {
        executor.execute(() -> {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                getContentResolver().openInputStream(uri).close();

                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(uri, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                if (bitmap != null) {
                    Bitmap finalBitmap = scaleBitmapIfNeeded(bitmap, MAX_IMAGE_SIZE);
                    selectedBitmap = finalBitmap;
                    mainHandler.post(() -> {
                        ivImage.setImageBitmap(finalBitmap);
                        tvResult.setText("识别结果将在此显示");
                        scrollResult.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "图片加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private int calculateInSampleSize(Uri uri, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                int halfHeight = height / 2;
                int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }
            return inSampleSize;
        } catch (Exception e) {
            return 1;
        }
    }

    private Bitmap scaleBitmapIfNeeded(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void copyToClipboard(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("ocr_result", text);
            clipboard.setPrimaryClip(clip);
        } else {
            android.text.ClipboardManager clipboard =
                    (android.text.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        }
    }

    private void saveResultToGallery() {
        String text = tvResult.getText().toString();
        if (text.isEmpty()) return;

        executor.execute(() -> {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "ocr_result_" + System.currentTimeMillis() + ".txt");
                values.put(MediaStore.Files.FileColumns.MIME_TYPE, "text/plain");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
                }

                Uri uri = getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                if (uri != null) {
                    try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                        os.write(text.getBytes("UTF-8"));
                    }
                    mainHandler.post(() ->
                            Toast.makeText(this, "结果已保存到文档", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdownNow();
        }
        if (textRecognizer != null) {
            textRecognizer.close();
        }
    }
}