package com.example.androiddemo.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class MemeMakerActivity extends AppCompatActivity {
    private ImageView ivMeme;
    private EditText etTopText;
    private EditText etBottomText;
    private Bitmap currentBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meme_maker);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("表情包制作");
        }

        ivMeme = findViewById(R.id.image_preview);
        etTopText = findViewById(R.id.edit_top_text);
        etBottomText = findViewById(R.id.edit_bottom_text);
        Button btnGenerate = findViewById(R.id.btn_save);

        btnGenerate.setOnClickListener(v -> generateMeme());

        // Create default bitmap
        currentBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(currentBitmap);
        canvas.drawColor(Color.WHITE);
        Paint p = new Paint();
        p.setTextSize(60);
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("输入文字生成表情包", 300, 300, p);
        ivMeme.setImageBitmap(currentBitmap);
    }

    private void generateMeme() {
        String topText = etTopText.getText().toString().toUpperCase();
        String bottomText = etBottomText.getText().toString().toUpperCase();

        currentBitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(currentBitmap);
        canvas.drawColor(Color.WHITE);

        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, 600, 600, bgPaint);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(80);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        if (!topText.isEmpty()) {
            canvas.drawText(topText, 300, 100, textPaint);
        }

        if (!bottomText.isEmpty()) {
            canvas.drawText(bottomText, 300, 550, textPaint);
        }

        Paint emojiPaint = new Paint();
        emojiPaint.setTextSize(200);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("😀", 300, 350, emojiPaint);

        ivMeme.setImageBitmap(currentBitmap);
        Toast.makeText(this, "表情包已生成", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
