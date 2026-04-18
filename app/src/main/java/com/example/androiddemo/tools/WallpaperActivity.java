package com.example.androiddemo.tools;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import com.example.androiddemo.R;
import java.io.IOException;

public class WallpaperActivity extends AppCompatActivity {
    private ImageView ivPreview;
    private Button btnSetWallpaper;
    private Bitmap currentBitmap;
    private static final int REQUEST_PICK_IMAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper);
        ivPreview = findViewById(R.id.iv_preview);
        btnSetWallpaper = findViewById(R.id.btn_set_wallpaper);
    }

    public void pickImage(View view) {
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
            try {
                android.net.Uri uri = data.getData();
                DocumentFile file = DocumentFile.fromSingleUri(this, uri);
                java.io.InputStream is = getContentResolver().openInputStream(uri);
                currentBitmap = BitmapFactory.decodeStream(is);
                is.close();
                ivPreview.setImageBitmap(currentBitmap);
                btnSetWallpaper.setEnabled(true);
            } catch (Exception e) {
                Toast.makeText(this, "选择图片失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setWallpaper(View view) {
        if (currentBitmap == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                WallpaperManager wpm = WallpaperManager.getInstance(this);
                wpm.setBitmap(currentBitmap);
                runOnUiThread(() -> Toast.makeText(this, "壁纸设置成功", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "壁纸设置失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}