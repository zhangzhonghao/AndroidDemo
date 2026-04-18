package com.example.androiddemo.ai;

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
import android.os.Handler;
import android.os.Looper;
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

import com.example.androiddemo.BuildConfig;
import com.example.androiddemo.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiVisionActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.minimaxi.com/v1/v1/cv";
    private static final int TIMEOUT_MS = 30000;
    private static final int MAX_IMAGE_SIZE = 1024; // 最大图片尺寸
    private static final int COMPRESS_QUALITY = 80;  // 压缩质量

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
        setContentView(R.layout.activity_ai_vision);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
    }

    private void initViews() {
        ivImage = findViewById(R.id.iv_image);
        scrollResult = findViewById(R.id.scroll_result);
        tvResult = findViewById(R.id.tv_result);
        btnSelectGallery = findViewById(R.id.btn_select_gallery);
        btnTakePhoto = findViewById(R.id.btn_take_photo);
        btnCopyResult = findViewById(R.id.btn_copy_result);

        // 设置默认图片
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
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "vision_" + System.currentTimeMillis() + ".jpg");
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
            // 全屏预览
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ImageView previewView = new ImageView(this);
            previewView.setImageBitmap(selectedBitmap);
            builder.setView(previewView);
            builder.setNegativeButton("关闭", null);
            builder.show();
        } else {
            // 选择图片
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
                String result = recognizeImage(selectedBitmap);
                mainHandler.post(() -> {
                    if (result != null) {
                        tvResult.setText(result);
                    } else {
                        tvResult.setText("识别失败，请稍后重试");
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() ->
                        tvResult.setText("识别出错: " + e.getMessage())
                );
            }
        });
    }

    public void onCopyResultClick(View view) {
        String text = tvResult.getText().toString();
        if (text.isEmpty() || text.equals("识别结果将在此显示")) {
            Toast.makeText(this, "没有可复制的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        androidpressCopyToClipboard(text);
        Toast.makeText(this, "结果已复制", Toast.LENGTH_SHORT).show();
    }

    public void onSaveResultClick(View view) {
        String text = tvResult.getText().toString();
        if (text.isEmpty() || text.equals("识别结果将在此显示")) {
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
                Bitmap bitmap = null;
                // 先尝试获取缩略图
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                getContentResolver().openInputStream(uri).close();

                // 读取图片并压缩
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(uri, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);

                if (bitmap != null) {
                    // 进一步缩放到最大尺寸
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

    private String recognizeImage(Bitmap bitmap) throws Exception {
        String apiKey = BuildConfig.MINIMAX_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("API Key 未配置");
        }

        // 压缩并转Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP);

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonRequest = "{"
                + "\"model\": \"MiniMax-M2\","
                + "\"image\": \"" + base64Image + "\""
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonRequest.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parseVisionResponse(response.toString());
        } else {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                error.append(line);
            }
            reader.close();
            throw new Exception("API错误: " + responseCode + " - " + error);
        }
    }

    private String parseVisionResponse(String jsonResponse) {
        try {
            org.json.JSONObject root = new org.json.JSONObject(jsonResponse);
            // 根据实际API响应格式调整解析逻辑
            if (root.has("content")) {
                return root.getString("content");
            } else if (root.has("description")) {
                return root.getString("description");
            } else if (root.has("text")) {
                return root.getString("text");
            } else {
                // 返回原始响应供调试
                return root.toString(2);
            }
        } catch (Exception e) {
            return "解析响应失败: " + e.getMessage() + "\n\n原始响应:\n" + jsonResponse;
        }
    }

    private void androidpressCopyToClipboard(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("vision_result", text);
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
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, "vision_result_" + System.currentTimeMillis() + ".txt");
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
    }
}