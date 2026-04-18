package com.example.androiddemo.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class TextToImageActivity extends AppCompatActivity {
    private EditText etInput;
    private Button btnGenerate;
    private ImageView ivResult;
    private EditText etTextSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_image);

        etInput = findViewById(R.id.et_input);
        etTextSize = findViewById(R.id.et_text_size);
        btnGenerate = findViewById(R.id.btn_generate);
        ivResult = findViewById(R.id.iv_result);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateImage();
            }
        });
    }

    private void generateImage() {
        String text = etInput.getText().toString();
        if (text.isEmpty()) {
            text = "请输入文字";
        }

        int textSize = 60;
        try {
            textSize = Integer.parseInt(etTextSize.getText().toString());
        } catch (NumberFormatException e) {
            textSize = 60;
        }

        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setAntiAlias(true);

        int width = (int) (paint.measureText(text) + 100);
        int height = (int) (textSize * 2.5);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 渐变背景
        int[] colors = {Color.parseColor("#667eea"), Color.parseColor("#764ba2")};
        Paint bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < width; i++) {
            float ratio = (float) i / width;
            int r = (int) (Color.red(colors[0]) * (1 - ratio) + Color.red(colors[1]) * ratio);
            int g = (int) (Color.green(colors[0]) * (1 - ratio) + Color.green(colors[1]) * ratio);
            int b = (int) (Color.blue(colors[0]) * (1 - ratio) + Color.blue(colors[1]) * ratio);
            bgPaint.setColor(Color.rgb(r, g, b));
            canvas.drawLine(i, 0, i, height, bgPaint);
        }

        // 文字
        Paint textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textY = height / 2 - (fm.ascent + fm.descent) / 2;
        canvas.drawText(text, width / 2, textY, textPaint);

        ivResult.setImageBitmap(bitmap);
    }
}