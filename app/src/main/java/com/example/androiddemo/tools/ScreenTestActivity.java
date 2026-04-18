package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.androiddemo.R;

public class ScreenTestActivity extends AppCompatActivity {

    private View colorView;
    private int currentColorIndex = 0;

    private static final int[] COLORS = {
            0xFFFFFFFF, // 白色
            0xFFFF0000, // 红色
            0xFF00FF00, // 绿色
            0xFF0000FF, // 蓝色
            0xFF000000, // 黑色
            0xFFFFFF00, // 黄色
            0xFFFF00FF, // 洋红
            0xFF00FFFF, // 青色
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏沉浸式
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars());
            insetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_screen_test);

        colorView = findViewById(R.id.color_view);
        colorView.setBackgroundColor(COLORS[currentColorIndex]);

        colorView.setOnClickListener(v -> switchColor());
    }

    private void switchColor() {
        currentColorIndex = (currentColorIndex + 1) % COLORS.length;
        colorView.setBackgroundColor(COLORS[currentColorIndex]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 恢复状态栏
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (insetsController != null) {
            insetsController.show(WindowInsetsCompat.Type.systemBars());
        }
    }
}