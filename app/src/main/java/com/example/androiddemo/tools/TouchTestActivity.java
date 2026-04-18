package com.example.androiddemo.tools;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class TouchTestActivity extends AppCompatActivity {

    private TouchTestView touchTestView;

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

        setContentView(R.layout.activity_touch_test);

        FrameLayout container = findViewById(R.id.touch_container);
        touchTestView = new TouchTestView(this);
        container.addView(touchTestView);
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

    // 自定义触摸测试视图
    private static class TouchTestView extends View {
        private final Paint pointPaint;
        private final Paint textPaint;
        private final List<TouchPoint> touchPoints = new ArrayList<>();
        private static final int POINT_RADIUS = 60;

        public TouchTestView(android.content.Context context) {
            super(context);
            pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pointPaint.setColor(Color.RED);
            pointPaint.setStyle(Paint.Style.FILL);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(36);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(Color.BLACK);

            // 绘制触摸点
            for (TouchPoint point : touchPoints) {
                canvas.drawCircle(point.x, point.y, POINT_RADIUS, pointPaint);
                // 绘制触摸点编号
                canvas.drawText(point.id + "", point.x, point.y + 12, textPaint);
            }

            // 绘制说明文字
            textPaint.setTextSize(40);
            textPaint.setColor(Color.GRAY);
            canvas.drawText("触摸屏幕测试触点", getWidth() / 2f, 80, textPaint);
            canvas.drawText("多点触控: " + touchPoints.size() + " 点", getWidth() / 2f, 140, textPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            touchPoints.clear();

            // 支持多点触控
            for (int i = 0; i < event.getPointerCount(); i++) {
                int pointerId = event.getPointerId(i);
                float x = event.getX(i);
                float y = event.getY(i);
                touchPoints.add(new TouchPoint(pointerId, x, y));
            }

            invalidate();
            return true;
        }

        private static class TouchPoint {
            int id;
            float x;
            float y;

            TouchPoint(int id, float x, float y) {
                this.id = id;
                this.x = x;
                this.y = y;
            }
        }
    }
}
