package com.example.androiddemo.tools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Random;

public class ScrollingTextActivity extends AppCompatActivity {

    private TextView tvScrollingText;
    private TextInputEditText etInputText;
    private Slider sliderSpeed;
    private Slider sliderTextSize;
    private MaterialButton btnStart;
    private MaterialButton btnStop;
    private MaterialButton btnChangeColor;

    private boolean isScrolling = false;
    private int currentColorIndex = 0;
    private final int[] colors = {
            Color.YELLOW,
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.CYAN,
            Color.MAGENTA,
            Color.WHITE,
            0xFFFF9800 // Orange
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_scrolling_text);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvScrollingText = findViewById(R.id.tv_scrolling_text);
        etInputText = findViewById(R.id.et_input_text);
        sliderSpeed = findViewById(R.id.slider_speed);
        sliderTextSize = findViewById(R.id.slider_text_size);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnChangeColor = findViewById(R.id.btn_change_color);

        // 初始时文字可滚动
        tvScrollingText.setSelected(true);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> startScrolling());
        btnStop.setOnClickListener(v -> stopScrolling());
        btnChangeColor.setOnClickListener(v -> changeColor());

        sliderTextSize.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                tvScrollingText.setTextSize(value);
            }
        });

        sliderSpeed.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && isScrolling) {
                // 速度变化时重启滚动
                stopScrolling();
                startScrolling();
            }
        });
    }

    private void startScrolling() {
        String text = etInputText.getText() != null ? etInputText.getText().toString().trim() : "";
        if (text.isEmpty()) {
            text = "欢迎使用滚动字幕！";
        }
        tvScrollingText.setText(text);

        // 设置滚动速度
        float speed = 11 - sliderSpeed.getValue(); // 反转：值越大速度越快
        tvScrollingText.setMarqueeRepeatLimit(-1); // MARQUEE_FOREVER
        tvScrollingText.setFocusable(true);
        tvScrollingText.setFocusableInTouchMode(true);
        tvScrollingText.requestFocus();
        tvScrollingText.setSelected(true);

        isScrolling = true;
        Toast.makeText(this, "开始滚动", Toast.LENGTH_SHORT).show();
    }

    private void stopScrolling() {
        isScrolling = false;
        tvScrollingText.setSelected(false);
        Toast.makeText(this, "停止滚动", Toast.LENGTH_SHORT).show();
    }

    private void changeColor() {
        currentColorIndex = (currentColorIndex + 1) % colors.length;
        tvScrollingText.setTextColor(colors[currentColorIndex]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvScrollingText.setSelected(true);
    }
}