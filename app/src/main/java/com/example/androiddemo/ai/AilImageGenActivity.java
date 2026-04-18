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
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AilImageGenActivity extends AppCompatActivity {

    private EditText etPrompt;
    private ImageView ivGeneratedImage;
    private View layoutBottomButtons;

    private Bitmap currentBitmap;
    private ExecutorService executor;
    private Handler mainHandler;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    saveImageToGallery();
                } else {
                    Toast.makeText(this, "需要存储权限才能保存图片", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_image_gen);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
    }

    private void initViews() {
        etPrompt = findViewById(R.id.et_prompt);
        ivGeneratedImage = findViewById(R.id.iv_generated_image);
        layoutBottomButtons = findViewById(R.id.layout_bottom_buttons);
    }

    public void onGenerateClick(View view) {
        String prompt = etPrompt.getText().toString().trim();
        if (prompt.isEmpty()) {
            Toast.makeText(this, "请输入图片描述", Toast.LENGTH_SHORT).show();
            return;
        }

        ivGeneratedImage.setImageResource(R.drawable.ic_image_placeholder);
        layoutBottomButtons.setVisibility(View.GONE);

        Toast.makeText(this, "正在生成图片...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                Bitmap bitmap = generateImage(prompt);
                if (bitmap != null) {
                    currentBitmap = bitmap;
                    mainHandler.post(() -> {
                        ivGeneratedImage.setImageBitmap(bitmap);
                        layoutBottomButtons.setVisibility(View.VISIBLE);
                    });
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(this, "图片生成失败", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "生成失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private Bitmap generateImage(String prompt) throws Exception {
        String apiKey = BuildConfig.MINIMAX_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            throw new Exception("API Key 未配置");
        }

        URL url = new URL("https://api.minimaxi.com/v1/text/chatcompletion_v2");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        String jsonRequest = "{"
                + "\"model\": \"MiniMax-M2\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}],"
                + "\"response_format\": {\"type\": \"image\"}"
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonRequest.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String fullResponse = response.toString();
                int imageStart = fullResponse.indexOf("data:image");
                if (imageStart == -1) {
                    return null;
                }

                int mimeStart = imageStart;
                int base64Start = fullResponse.indexOf(",", mimeStart) + 1;
                int base64End = fullResponse.indexOf("\"", base64Start);
                if (base64End == -1) {
                    base64End = fullResponse.length();
                }

                String base64Image = fullResponse.substring(base64Start, base64End);
                byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            }
        } else {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                throw new Exception("API错误: " + error);
            }
        }
    }

    public void onSaveClick(View view) {
        if (currentBitmap == null) {
            Toast.makeText(this, "请先生成图片", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                saveImageToGallery();
            }
        } else {
            saveImageToGallery();
        }
    }

    private void saveImageToGallery() {
        if (currentBitmap == null) return;

        executor.execute(() -> {
            try {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, "AI_Image_" + System.currentTimeMillis() + ".png");
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AIImages");
                }

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                        currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        mainHandler.post(() ->
                                Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public void onShareClick(View view) {
        if (currentBitmap == null) {
            Toast.makeText(this, "请先生成图片", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                String cachePath = getCacheDir().getPath() + "/shared_images/";
                java.io.File cacheDir = new java.io.File(cachePath);
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                java.io.File imageFile = new java.io.File(cachePath, "ai_image_" + System.currentTimeMillis() + ".png");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(imageFile)) {
                    currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

                mainHandler.post(() -> {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "分享图片"));
                });
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "分享失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public void onImageClick(View view) {
        if (currentBitmap == null) return;

        executor.execute(() -> {
            try {
                String cachePath = getCacheDir().getPath() + "/preview_images/";
                java.io.File cacheDir = new java.io.File(cachePath);
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }

                java.io.File imageFile = new java.io.File(cachePath, "preview_" + System.currentTimeMillis() + ".png");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(imageFile)) {
                    currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                }

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);

                mainHandler.post(() -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "image/png");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                });
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(this, "打开失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
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