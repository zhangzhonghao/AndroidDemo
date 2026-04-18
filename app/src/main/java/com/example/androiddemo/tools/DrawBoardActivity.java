package com.example.androiddemo.tools;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.androiddemo.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DrawBoardActivity extends AppCompatActivity {

    private DrawBoardView drawBoardView;
    private Button btnEraser;
    private boolean isEraserMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_draw_board);

        drawBoardView = findViewById(R.id.draw_board_view);
        btnEraser = findViewById(R.id.btn_eraser);

        updateEraserButton();
    }

    public void onColorClick(View view) {
        int color = Color.BLACK;
        int id = view.getId();

        if (id == R.id.color_black) {
            color = Color.BLACK;
        } else if (id == R.id.color_red) {
            color = 0xFFE74C3C;
        } else if (id == R.id.color_orange) {
            color = 0xFFE67E22;
        } else if (id == R.id.color_yellow) {
            color = 0xFFF1C40F;
        } else if (id == R.id.color_green) {
            color = 0xFF2ECC71;
        } else if (id == R.id.color_blue) {
            color = 0xFF3498DB;
        } else if (id == R.id.color_purple) {
            color = 0xFF9B59B6;
        } else if (id == R.id.color_pink) {
            color = 0xFFE91E63;
        }

        drawBoardView.setColor(color);
        drawBoardView.setEraser(false);
        isEraserMode = false;
        updateEraserButton();
    }

    public void onEraserClick(View view) {
        isEraserMode = !isEraserMode;
        drawBoardView.setEraser(isEraserMode);
        updateEraserButton();
    }

    private void updateEraserButton() {
        if (isEraserMode) {
            btnEraser.setBackgroundColor(0xFF3498DB);
            btnEraser.setTextColor(Color.WHITE);
        } else {
            btnEraser.setBackgroundColor(0xFFE0E0E0);
            btnEraser.setTextColor(Color.BLACK);
        }
    }

    public void onUndoClick(View view) {
        drawBoardView.undo();
    }

    public void onRedoClick(View view) {
        drawBoardView.redo();
    }

    public void onClearClick(View view) {
        if (drawBoardView.hasDrawing()) {
            drawBoardView.clear();
            Toast.makeText(this, "已清空画布", Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveClick(View view) {
        if (!drawBoardView.hasDrawing()) {
            Toast.makeText(this, "画布为空，无法保存", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = drawBoardView.getBitmap();
        saveImage(bitmap);
    }

    private void saveImage(Bitmap bitmap) {
        String fileName = "draw_board_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";

        try {
            OutputStream outputStream;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AndroidDemo/DrawBoard");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    outputStream = getContentResolver().openOutputStream(uri);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                }
            } else {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidDemo/DrawBoard");
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                File file = new File(directory, fileName);
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                sendBroadcast(scanIntent);

                Toast.makeText(this, "图片已保存: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}