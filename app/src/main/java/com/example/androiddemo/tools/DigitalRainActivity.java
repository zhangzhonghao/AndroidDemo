package com.example.androiddemo.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class DigitalRainActivity extends AppCompatActivity {
    private FrameLayout container;
    private DigitalRainView rainView;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_rain);

        container = findViewById(R.id.container);
        rainView = new DigitalRainView(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        container.addView(rainView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rainView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        rainView.stop();
    }

    private class DigitalRainView extends View {
        private static final int COLUMNS = 30;
        private static final int CHAR_SIZE = 20;

        private char[][] chars;
        private int[] yPositions;
        private Paint paint;
        private Random random = new Random();
        private boolean isRunning = false;
        private String digits = "0123456789ABCDEF";

        public DigitalRainView(DigitalRainActivity context) {
            super(context);
            paint = new Paint();
            paint.setTextSize(CHAR_SIZE);
            paint.setAntiAlias(true);
        }

        public void start() {
            isRunning = true;
            invalidate();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        update();
                        invalidate();
                        handler.postDelayed(this, 50);
                    }
                }
            }, 50);
        }

        public void stop() {
            isRunning = false;
        }

        private void update() {
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) return;

            int cols = width / CHAR_SIZE;
            if (chars == null || chars.length != cols) {
                chars = new char[cols][height / CHAR_SIZE];
                yPositions = new int[cols];
                for (int i = 0; i < cols; i++) {
                    yPositions[i] = random.nextInt(height);
                    for (int j = 0; j < chars[i].length; j++) {
                        chars[i][j] = digits.charAt(random.nextInt(digits.length()));
                    }
                }
            }

            for (int i = 0; i < cols; i++) {
                yPositions[i] += CHAR_SIZE;
                if (yPositions[i] > height) {
                    yPositions[i] = 0;
                    for (int j = 0; j < chars[i].length; j++) {
                        chars[i][j] = digits.charAt(random.nextInt(digits.length()));
                    }
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(0xFF000000);

            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) return;

            int cols = width / CHAR_SIZE;

            for (int i = 0; i < cols && i < chars.length; i++) {
                int x = i * CHAR_SIZE;
                for (int j = 0; j < chars[i].length; j++) {
                    int y = yPositions[i] - j * CHAR_SIZE;
                    if (y > 0 && y < height) {
                        int alpha = 255 - (j * 255 / chars[i].length);
                        paint.setColor(0xFF00FF00);
                        paint.setAlpha(alpha);
                        canvas.drawText(String.valueOf(chars[i][j]), x, y, paint);
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rainView.stop();
        handler.removeCallbacksAndMessages(null);
    }
}