package com.example.androiddemo.tools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LedSignActivity extends AppCompatActivity {

    private LedSignView ledSignView;
    private TextInputEditText etInputText;
    private Slider sliderSpeed;
    private MaterialButton btnStart;
    private MaterialButton btnStop;
    private View colorRed, colorGreen, colorBlue, colorYellow, colorPink, colorCyan, colorWhite;

    private int currentColor = Color.RED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_led_sign);

        initViews();
        setupListeners();
    }

    private void initViews() {
        ledSignView = findViewById(R.id.led_sign_view);
        etInputText = findViewById(R.id.et_input_text);
        sliderSpeed = findViewById(R.id.slider_speed);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        colorRed = findViewById(R.id.color_red);
        colorGreen = findViewById(R.id.color_green);
        colorBlue = findViewById(R.id.color_blue);
        colorYellow = findViewById(R.id.color_yellow);
        colorPink = findViewById(R.id.color_pink);
        colorCyan = findViewById(R.id.color_cyan);
        colorWhite = findViewById(R.id.color_white);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> startScrolling());
        btnStop.setOnClickListener(v -> stopScrolling());

        sliderSpeed.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                ledSignView.setScrollSpeed((int) value);
            }
        });

        colorRed.setOnClickListener(v -> setColor(Color.RED));
        colorGreen.setOnClickListener(v -> setColor(Color.GREEN));
        colorBlue.setOnClickListener(v -> setColor(Color.parseColor("#0088FF")));
        colorYellow.setOnClickListener(v -> setColor(Color.YELLOW));
        colorPink.setOnClickListener(v -> setColor(Color.parseColor("#FF00FF")));
        colorCyan.setOnClickListener(v -> setColor(Color.CYAN));
        colorWhite.setOnClickListener(v -> setColor(Color.WHITE));
    }

    private void setColor(int color) {
        currentColor = color;
        ledSignView.setDotColor(color);
    }

    private void startScrolling() {
        String text = etInputText.getText() != null ? etInputText.getText().toString().trim() : "";
        if (text.isEmpty()) {
            text = "LED字幕";
        }
        ledSignView.setText(text);
        ledSignView.setDotColor(currentColor);
        ledSignView.setScrollSpeed((int) sliderSpeed.getValue());
        ledSignView.startScrolling();
        Toast.makeText(this, "开始滚动", Toast.LENGTH_SHORT).show();
    }

    private void stopScrolling() {
        ledSignView.stopScrolling();
        Toast.makeText(this, "停止滚动", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ledSignView.stopScrolling();
    }
}