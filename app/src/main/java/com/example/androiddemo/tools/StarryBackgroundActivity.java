package com.example.androiddemo.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class StarryBackgroundActivity extends AppCompatActivity {
    private StarryView starryView;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starry_background);

        starryView = new StarryView(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(starryView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        starryView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        starryView.stop();
    }

    private class StarryView extends View {
        private static final int NUM_STARS = 200;
        private static final int NUM_SHOOTING_STARS = 5;

        private Star[] stars;
        private ShootingStar[] shootingStars;
        private Paint paint;
        private Random random = new Random();
        private boolean isRunning = false;

        public StarryView(StarryBackgroundActivity context) {
            super(context);
            paint = new Paint();
            paint.setAntiAlias(true);
        }

        public void start() {
            isRunning = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        invalidate();
                        handler.postDelayed(this, 33); // ~30fps
                    }
                }
            });
        }

        public void stop() {
            isRunning = false;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            initStars(w, h);
        }

        private void initStars(int width, int height) {
            stars = new Star[NUM_STARS];
            for (int i = 0; i < NUM_STARS; i++) {
                stars[i] = new Star(width, height, random);
            }

            shootingStars = new ShootingStar[NUM_SHOOTING_STARS];
            for (int i = 0; i < NUM_SHOOTING_STARS; i++) {
                shootingStars[i] = new ShootingStar(width, height, random);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 深蓝色背景
            canvas.drawColor(0xFF0A0A2E);

            if (stars == null) return;

            // 绘制星星
            for (Star star : stars) {
                star.update();
                star.draw(canvas, paint);
            }

            // 绘制流星
            for (ShootingStar star : shootingStars) {
                star.update();
                star.draw(canvas, paint);
            }
        }

        private class Star {
            float x, y;
            float size;
            float twinkleSpeed;
            float alpha;

            Star(int width, int height, Random random) {
                reset(width, height, random);
            }

            void reset(int width, int height, Random random) {
                x = random.nextInt(width);
                y = random.nextInt(height);
                size = random.nextFloat() * 2 + 1;
                twinkleSpeed = random.nextFloat() * 0.05f + 0.02f;
                alpha = random.nextFloat();
            }

            void update() {
                alpha += twinkleSpeed;
                if (alpha > 1 || alpha < 0.2f) {
                    twinkleSpeed = -twinkleSpeed;
                }
            }

            void draw(Canvas canvas, Paint paint) {
                paint.setColor(0xFFFFFFFF);
                paint.setAlpha((int) (alpha * 255));
                canvas.drawCircle(x, y, size, paint);
            }
        }

        private class ShootingStar {
            float x, y;
            float speedX, speedY;
            float length;
            boolean active;
            int width, height;

            ShootingStar(int width, int height, Random random) {
                this.width = width;
                this.height = height;
                reset(random);
            }

            void reset(Random random) {
                x = random.nextInt(width);
                y = random.nextInt(height / 2);
                speedX = random.nextFloat() * 8 + 4;
                speedY = random.nextFloat() * 4 + 2;
                length = random.nextFloat() * 50 + 30;
                active = random.nextFloat() < 0.3f;
            }

            void update() {
                if (!active) {
                    if (Math.random() < 0.005) {
                        reset(new Random());
                    }
                    return;
                }
                x += speedX;
                y += speedY;
                if (x > width || y > height) {
                    active = false;
                }
            }

            void draw(Canvas canvas, Paint paint) {
                if (!active) return;

                paint.setColor(0xFFFFFFFF);
                paint.setStrokeWidth(2);
                paint.setAlpha(200);
                canvas.drawLine(x, y, x - length, y - length * 0.5f, paint);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        starryView.stop();
        handler.removeCallbacksAndMessages(null);
    }
}