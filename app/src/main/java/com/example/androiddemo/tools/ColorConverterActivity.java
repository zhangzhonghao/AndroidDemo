package com.example.androiddemo.tools;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Locale;

public class ColorConverterActivity extends AppCompatActivity {

    private View colorPreview;
    private EditText etHex;
    private SeekBar sbRed, sbGreen, sbBlue;
    private TextView tvRed, tvGreen, tvBlue;
    private TextView tvHsl;
    private TextView tvRgb;

    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_converter);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("颜色转换器");
        }
    }

    private void initViews() {
        colorPreview = findViewById(R.id.color_preview);
        etHex = findViewById(R.id.et_hex);
        sbRed = findViewById(R.id.sb_red);
        sbGreen = findViewById(R.id.sb_green);
        sbBlue = findViewById(R.id.sb_blue);
        tvRed = findViewById(R.id.tv_red);
        tvGreen = findViewById(R.id.tv_green);
        tvBlue = findViewById(R.id.tv_blue);
        tvHsl = findViewById(R.id.tv_hsl);
        tvRgb = findViewById(R.id.tv_rgb);

        sbRed.setMax(255);
        sbGreen.setMax(255);
        sbBlue.setMax(255);

        sbRed.setProgress(65);
        sbGreen.setProgress(105);
        sbBlue.setProgress(225);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !isUpdating) {
                    updateFromRgb();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        sbRed.setOnSeekBarChangeListener(listener);
        sbGreen.setOnSeekBarChangeListener(listener);
        sbBlue.setOnSeekBarChangeListener(listener);

        etHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    String hex = s.toString().replace("#", "");
                    if (hex.length() == 6) {
                        try {
                            int r = Integer.parseInt(hex.substring(0, 2), 16);
                            int g = Integer.parseInt(hex.substring(2, 4), 16);
                            int b = Integer.parseInt(hex.substring(4, 6), 16);
                            isUpdating = true;
                            sbRed.setProgress(r);
                            sbGreen.setProgress(g);
                            sbBlue.setProgress(b);
                            isUpdating = false;
                            updateDisplay(r, g, b);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        });

        updateFromRgb();
    }

    private void updateFromRgb() {
        int r = sbRed.getProgress();
        int g = sbGreen.getProgress();
        int b = sbBlue.getProgress();
        updateDisplay(r, g, b);
    }

    private void updateDisplay(int r, int g, int b) {
        tvRed.setText(String.format(Locale.getDefault(), "R: %d", r));
        tvGreen.setText(String.format(Locale.getDefault(), "G: %d", g));
        tvBlue.setText(String.format(Locale.getDefault(), "B: %d", b));

        String hex = String.format(Locale.getDefault(), "#%02X%02X%02X", r, g, b);
        isUpdating = true;
        etHex.setText(hex);
        isUpdating = false;

        colorPreview.setBackgroundColor(Color.rgb(r, g, b));

        // 计算HSL
        float[] hsl = new float[3];
        Color.RGBToHSV(r, g, b, hsl);
        String hslStr = String.format(Locale.getDefault(), "HSL: %.0f, %.0f%%, %.0f%%",
                hsl[0], hsl[1] * 100, hsl[2] * 100);
        tvHsl.setText(hslStr);

        tvRgb.setText(String.format(Locale.getDefault(), "RGB: %d, %d, %d", r, g, b));
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