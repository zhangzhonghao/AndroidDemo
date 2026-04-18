package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 自定义水平仪视图 - 气泡式双轴水平仪
 */
public class LevelView extends View {

    private Paint circlePaint;
    private Paint gridPaint;
    private Paint bubblePaint;
    private Paint centerMarkPaint;
    private Paint scalePaint;

    private float bubbleX = 0;
    private float bubbleY = 0;
    private float levelRadius = 0;

    private static final float MAX_TILT_ANGLE = 45f;
    private static final float BUBBLE_RADIUS_RATIO = 0.15f;
    private static final float CENTER_CIRCLE_RADIUS_RATIO = 0.08f;
    private static final float GRID_LINE_RADIUS_RATIO = 0.25f;

    public LevelView(Context context) {
        super(context);
        init();
    }

    public LevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LevelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#34495E"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4f);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#3D566E"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.5f);

        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setColor(Color.parseColor("#2ECC71"));
        bubblePaint.setStyle(Paint.Style.FILL);

        centerMarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerMarkPaint.setColor(Color.parseColor("#E74C3C"));
        centerMarkPaint.setStyle(Paint.Style.STROKE);
        centerMarkPaint.setStrokeWidth(3f);

        scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scalePaint.setColor(Color.parseColor("#7F8C8D"));
        scalePaint.setTextSize(24f);
        scalePaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setLevel(float x, float y) {
        this.bubbleX = clamp(x, -MAX_TILT_ANGLE, MAX_TILT_ANGLE);
        this.bubbleY = clamp(y, -MAX_TILT_ANGLE, MAX_TILT_ANGLE);
        invalidate();
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean isLevel(float threshold) {
        return Math.abs(bubbleX) <= threshold && Math.abs(bubbleY) <= threshold;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        levelRadius = Math.min(centerX, centerY) - 40f;
        float bubbleRadius = levelRadius * BUBBLE_RADIUS_RATIO;
        float centerCircleRadius = levelRadius * CENTER_CIRCLE_RADIUS_RATIO;
        float gridLineRadius = levelRadius * GRID_LINE_RADIUS_RATIO;

        // 绘制背景
        canvas.drawColor(Color.parseColor("#1A252F"));

        // 绘制最外圈
        circlePaint.setColor(Color.parseColor("#34495E"));
        canvas.drawCircle(centerX, centerY, levelRadius, circlePaint);

        // 绘制中心圆（水平区域）
        centerMarkPaint.setColor(Color.parseColor("#2ECC71"));
        centerMarkPaint.setStyle(Paint.Style.STROKE);
        centerMarkPaint.setStrokeWidth(3f);
        canvas.drawCircle(centerX, centerY, centerCircleRadius, centerMarkPaint);

        // 绘制同心圆网格
        gridPaint.setColor(Color.parseColor("#3D566E"));
        for (int i = 1; i <= 3; i++) {
            canvas.drawCircle(centerX, centerY, gridLineRadius * i, gridPaint);
        }

        // 绘制十字线
        canvas.drawLine(centerX - levelRadius, centerY, centerX - levelRadius * 0.3f, centerY, gridPaint);
        canvas.drawLine(centerX + levelRadius * 0.3f, centerY, centerX + levelRadius, centerY, gridPaint);
        canvas.drawLine(centerX, centerY - levelRadius, centerX, centerY - levelRadius * 0.3f, gridPaint);
        canvas.drawLine(centerX, centerY + levelRadius * 0.3f, centerX, centerY + levelRadius, gridPaint);

        // 绘制刻度
        drawScale(canvas, centerX, centerY);

        // 计算气泡位置（根据倾斜角度）
        float maxOffset = levelRadius - bubbleRadius - 10f;
        float bubbleOffsetX = (bubbleX / MAX_TILT_ANGLE) * maxOffset;
        float bubbleOffsetY = -(bubbleY / MAX_TILT_ANGLE) * maxOffset;

        float actualBubbleX = centerX + bubbleOffsetX;
        float actualBubbleY = centerY + bubbleOffsetY;

        // 绘制气泡阴影
        bubblePaint.setColor(Color.parseColor("#1a2ECC71"));
        canvas.drawCircle(actualBubbleX + 4f, actualBubbleY + 4f, bubbleRadius, bubblePaint);

        // 绘制气泡
        bubblePaint.setColor(isLevel(3f) ? Color.parseColor("#2ECC71") : Color.parseColor("#3498DB"));
        canvas.drawCircle(actualBubbleX, actualBubbleY, bubbleRadius, bubblePaint);

        // 绘制气泡高光
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#66FFFFFF"));
        highlightPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(actualBubbleX - bubbleRadius * 0.3f, actualBubbleY - bubbleRadius * 0.3f,
                bubbleRadius * 0.3f, highlightPaint);

        // 绘制气泡边缘
        bubblePaint.setColor(Color.parseColor("#1A2ECC71"));
        bubblePaint.setStyle(Paint.Style.STROKE);
        bubblePaint.setStrokeWidth(2f);
        canvas.drawCircle(actualBubbleX, actualBubbleY, bubbleRadius, bubblePaint);
        bubblePaint.setStyle(Paint.Style.FILL);

        // 绘制中心十字标记
        centerMarkPaint.setColor(Color.parseColor("#E74C3C"));
        centerMarkPaint.setStyle(Paint.Style.STROKE);
        centerMarkPaint.setStrokeWidth(2f);
        float crossSize = 15f;
        canvas.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY, centerMarkPaint);
        canvas.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize, centerMarkPaint);
    }

    private void drawScale(Canvas canvas, float centerX, float centerY) {
        scalePaint.setColor(Color.parseColor("#7F8C8D"));
        scalePaint.setTextSize(20f);

        // 绘制上下左右的角度刻度
        String[] scaleLabels = {"0", "10", "20", "30", "40", "45"};
        float[] scaleAngles = {0, 10, 20, 30, 40, 45};

        for (int i = 0; i < scaleLabels.length; i++) {
            float angle = scaleAngles[i];
            float radian = (float) Math.toRadians(angle);

            // 上（Y轴正向）
            float upY = centerY - levelRadius + 30f;
            canvas.drawText(scaleLabels[i], centerX, upY, scalePaint);

            // 下（Y轴负向）
            float downY = centerY + levelRadius - 15f;
            canvas.drawText(scaleLabels[i], centerX, downY, scalePaint);

            // 左（X轴负向）
            float leftX = centerX - levelRadius + 25f;
            canvas.drawText(scaleLabels[i], leftX, centerY + 6f, scalePaint);

            // 右（X轴正向）
            float rightX = centerX + levelRadius - 25f;
            canvas.drawText(scaleLabels[i], rightX, centerY + 6f, scalePaint);
        }
    }
}