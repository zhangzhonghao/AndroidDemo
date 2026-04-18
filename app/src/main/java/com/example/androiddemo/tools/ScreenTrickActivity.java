package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ScreenTrickActivity extends AppCompatActivity {
    private View rootView;
    private TextView tvInstruction;
    private Button btnStart;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private float offsetX = 0, offsetY = 0;
    private float velocityX = 10, velocityY = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_trick);

        rootView = findViewById(R.id.trick_view);
        tvInstruction = findViewById(R.id.tv_instruction);
        btnStart = findViewById(R.id.btn_start);

        // 全屏显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrick();
            }
        });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isRunning) {
                    // 干扰效果
                    tvInstruction.setX(event.getX() - tvInstruction.getWidth() / 2);
                    tvInstruction.setY(event.getY() - tvInstruction.getHeight() / 2);
                }
                return true;
            }
        });
    }

    private void startTrick() {
        isRunning = true;
        tvInstruction.setVisibility(View.VISIBLE);
        btnStart.setVisibility(View.GONE);
        tvInstruction.setText("这是你的屏幕，点击任意位置！");

        animateText();
    }

    private void animateText() {
        if (!isRunning) return;

        offsetX += velocityX;
        offsetY += velocityY;

        // 边界反弹
        float maxX = rootView.getWidth() - tvInstruction.getWidth();
        float maxY = rootView.getHeight() - tvInstruction.getHeight();

        if (offsetX < 0 || offsetX > maxX) {
            velocityX = -velocityX;
            offsetX = Math.max(0, Math.min(offsetX, maxX));
        }
        if (offsetY < 0 || offsetY > maxY) {
            velocityY = -velocityY;
            offsetY = Math.max(0, Math.min(offsetY, maxY));
        }

        // 随机速度变化
        if (Math.random() < 0.1) {
            velocityX += (Math.random() - 0.5) * 4;
            velocityY += (Math.random() - 0.5) * 4;
        }

        tvInstruction.setX(offsetX);
        tvInstruction.setY(offsetY);

        handler.postDelayed(this::animateText, 50);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }
}