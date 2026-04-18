package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StickerScreenActivity extends AppCompatActivity {
    private FrameLayout container;
    private Button btnAdd;
    private List<View> stickers = new ArrayList<>();
    private Random random = new Random();
    private String[] stickerTexts = {"哈哈", "呵呵", "666", "棒", "赞", "好", "噢", "呀", "呃", "哦"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_screen);

        container = findViewById(R.id.container);
        btnAdd = findViewById(R.id.btn_add);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSticker();
            }
        });

        // 长按删除
        container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearStickers();
                return true;
            }
        });
    }

    private void addSticker() {
        final TextView sticker = new TextView(this);
        sticker.setText(stickerTexts[random.nextInt(stickerTexts.length)]);
        sticker.setTextSize(20 + random.nextInt(20));
        sticker.setPadding(20, 10, 20, 10);

        // 随机背景颜色
        int[] colors = {0xFFFF6B6B, 0xFF4ECDC4, 0xFFFFE66D, 0xFF95E1D3, 0xFFF38181,
                        0xFFAA96DA, 0xFFFCBF49, 0xFF2EC4B6, 0xFFFF9F1C, 0xFFCBF3F0};
        sticker.setBackgroundColor(colors[random.nextInt(colors.length)]);

        final int x = random.nextInt(container.getWidth() - 100);
        final int y = random.nextInt(container.getHeight() - 100);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = x;
        params.topMargin = y;
        sticker.setLayoutParams(params);

        // 拖动功能
        sticker.setOnTouchListener(new View.OnTouchListener() {
            private int dx, dy;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = (int) (v.getX() - event.getRawX());
                        dy = (int) (v.getY() - event.getRawY());
                        break;
                    case MotionEvent.ACTION_MOVE:
                        v.setX(event.getRawX() + dx);
                        v.setY(event.getRawY() + dy);
                        break;
                }
                return true;
            }
        });

        container.addView(sticker);
        stickers.add(sticker);
    }

    private void clearStickers() {
        for (View sticker : stickers) {
            container.removeView(sticker);
        }
        stickers.clear();
    }
}