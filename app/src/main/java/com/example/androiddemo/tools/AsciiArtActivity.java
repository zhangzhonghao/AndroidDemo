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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class AsciiArtActivity extends AppCompatActivity {
    private TextView tvAscii;
    private Button btnConvert;
    private EditText etInput;
    private int scale = 10;

    private final char[] asciiChars = "@%#*+=-:. ".toCharArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ascii_art);

        tvAscii = findViewById(R.id.tv_ascii);
        btnConvert = findViewById(R.id.btn_convert);
        etInput = findViewById(R.id.et_input);

        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertToAscii();
            }
        });
    }

    private void convertToAscii() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            input = "ASCII";
        }

        StringBuilder sb = new StringBuilder();
        int width = 40;
        int height = input.length() * 2;

        // 创建简单的字符画效果
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int charIndex = (int) ((Math.sin(x * 0.1) * Math.cos(y * 0.1) + 1) * (asciiChars.length - 1) / 2);
                char c = input.charAt((x + y) % input.length());
                sb.append(c);
            }
            sb.append("\n");
        }

        tvAscii.setText(sb.toString());
        tvAscii.setTextSize(8);
        tvAscii.setTypeface(Typeface.MONOSPACE);
    }
}