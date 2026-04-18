package com.example.androiddemo.tools;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ColorPickerActivity extends AppCompatActivity {

    private View colorPreview;
    private TextView tvHex;
    private TextView tvRgb;
    private ColorPickerView colorPickerView;
    private LinearLayout presetColorsContainer;

    private static final int[] PRESET_COLORS = {
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.rgb(255, 128, 0),    // 橙色
            Color.rgb(128, 0, 255),    // 紫色
            Color.rgb(255, 192, 203),  // 粉色
            Color.rgb(165, 42, 42),    // 棕色
            Color.rgb(0, 128, 0),      // 深绿色
            Color.rgb(0, 0, 128),      // 深蓝色
            Color.rgb(128, 128, 128),  // 灰色
            Color.rgb(255, 255, 0),    // 黄色
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        initViews();
        setupColorPicker();
        setupPresetColors();

        updateColorDisplay(Color.BLACK);
    }

    private void initViews() {
        colorPreview = findViewById(R.id.color_preview);
        tvHex = findViewById(R.id.tv_hex);
        tvRgb = findViewById(R.id.tv_rgb);
        colorPickerView = findViewById(R.id.color_picker_view);
        presetColorsContainer = findViewById(R.id.preset_colors_container);
    }

    private void setupColorPicker() {
        colorPickerView.setOnColorSelectedListener(color -> updateColorDisplay(color));
    }

    private void setupPresetColors() {
        int size = (int) (48 * getResources().getDisplayMetrics().density);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);

        for (int color : PRESET_COLORS) {
            View colorView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            colorView.setLayoutParams(params);
            colorView.setBackgroundColor(color);

            colorView.setOnClickListener(v -> {
                updateColorDisplay(color);
            });

            presetColorsContainer.addView(colorView);
        }
    }

    private void updateColorDisplay(int color) {
        colorPreview.setBackgroundColor(color);

        String hex = String.format("#%06X", (0xFFFFFF & color));
        tvHex.setText(hex);

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        tvRgb.setText(String.format("%d, %d, %d", r, g, b));
    }
}