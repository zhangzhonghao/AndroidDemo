package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 自定义罗盘视图
 */
public class CompassView extends View {

    private Paint circlePaint;
    private Paint tickPaint;
    private Paint textPaint;
    private Paint needlePaint;
    private Paint northPaint;
    private Paint southPaint;
    private Paint degreePaint;
    private Paint indicatorPaint;

    private Path needleNorthPath;
    private Path needleSouthPath;

    private float currentAzimuth = 0;

    private final String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
    private final int[] directionAngles = {0, 45, 90, 135, 180, 225, 270, 315};

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#2C3E50"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4f);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.parseColor("#34495E"));
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);

        northPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        northPaint.setColor(Color.parseColor("#E74C3C"));
        northPaint.setStyle(Paint.Style.FILL);

        southPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        southPaint.setColor(Color.parseColor("#ECF0F1"));
        southPaint.setStyle(Paint.Style.FILL);

        degreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        degreePaint.setColor(Color.parseColor("#95A5A6"));
        degreePaint.setTextSize(24f);
        degreePaint.setTextAlign(Paint.Align.CENTER);

        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.parseColor("#F39C12"));
        indicatorPaint.setStyle(Paint.Style.FILL);

        needleNorthPath = new Path();
        needleSouthPath = new Path();
    }

    public void setAzimuth(float azimuth) {
        this.currentAzimuth = azimuth;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 40f;

        canvas.save();
        canvas.rotate(-currentAzimuth, centerX, centerY);

        // 绘制罗盘外圈
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // 绘制刻度和方向标识
        for (int i = 0; i < 360; i += 5) {
            float angle = (float) Math.toRadians(i - 90);
            float startRadius = radius;

            if (i % 90 == 0) {
                startRadius = radius - 30f;
            } else if (i % 45 == 0) {
                startRadius = radius - 20f;
            } else if (i % 15 == 0) {
                startRadius = radius - 15f;
            }

            float startX = centerX + (float) Math.cos(angle) * startRadius;
            float startY = centerY + (float) Math.sin(angle) * startRadius;
            float endX = centerX + (float) Math.cos(angle) * (radius - 5f);
            float endY = centerY + (float) Math.sin(angle) * (radius - 5f);

            tickPaint.setStrokeWidth(i % 90 == 0 ? 4f : (i % 45 == 0 ? 3f : 2f));
            canvas.drawLine(startX, startY, endX, endY, tickPaint);
        }

        // 绘制方向文字 (N, E, S, W)
        textPaint.setTextSize(48f);
        textPaint.setFakeBoldText(true);

        // N
        textPaint.setColor(Color.parseColor("#E74C3C"));
        canvas.drawText("N", centerX, centerY - radius + 60f, textPaint);

        // E
        textPaint.setColor(Color.WHITE);
        canvas.drawText("E", centerX + radius - 40f, centerY + 15f, textPaint);

        // S
        canvas.drawText("S", centerX, centerY + radius - 30f, textPaint);

        // W
        canvas.drawText("W", centerX - radius + 40f, centerY + 15f, textPaint);

        // 绘制中间方向标识 (NE, SE, SW, NW)
        textPaint.setTextSize(28f);
        textPaint.setColor(Color.parseColor("#BDC3C7"));

        float midRadius = radius - 70f;
        canvas.drawText("NE", centerX + (float) Math.cos(Math.toRadians(-45)) * midRadius,
                centerY + (float) Math.sin(Math.toRadians(-45)) * midRadius + 10f, textPaint);
        canvas.drawText("SE", centerX + (float) Math.cos(Math.toRadians(45)) * midRadius,
                centerY + (float) Math.sin(Math.toRadians(45)) * midRadius + 10f, textPaint);
        canvas.drawText("SW", centerX + (float) Math.cos(Math.toRadians(135)) * midRadius,
                centerY + (float) Math.sin(Math.toRadians(135)) * midRadius + 10f, textPaint);
        canvas.drawText("NW", centerX + (float) Math.cos(Math.toRadians(-135)) * midRadius,
                centerY + (float) Math.sin(Math.toRadians(-135)) * midRadius + 10f, textPaint);

        // 绘制小刻度数字
        textPaint.setTextSize(20f);
        degreePaint.setTextSize(20f);
        for (int i = 0; i < 360; i += 30) {
            if (i % 90 != 0) {
                float angle = (float) Math.toRadians(i - 90);
                float textRadius = radius - 50f;
                float x = centerX + (float) Math.cos(angle) * textRadius;
                float y = centerY + (float) Math.sin(angle) * textRadius + 7f;
                canvas.drawText(String.valueOf(i), x, y, degreePaint);
            }
        }

        canvas.restore();

        // 绘制指南针指针 (不旋转，保持向上)
        float needleLength = radius * 0.6f;
        float needleWidth = 20f;

        // 北指针 (红色)
        needleNorthPath.reset();
        needleNorthPath.moveTo(centerX, centerY - needleLength);
        needleNorthPath.lineTo(centerX - needleWidth / 2, centerY);
        needleNorthPath.lineTo(centerX + needleWidth / 2, centerY);
        needleNorthPath.close();
        canvas.drawPath(needleNorthPath, northPaint);

        // 南指针 (白色)
        needleSouthPath.reset();
        needleSouthPath.moveTo(centerX, centerY + needleLength);
        needleSouthPath.lineTo(centerX - needleWidth / 2, centerY);
        needleSouthPath.lineTo(centerX + needleWidth / 2, centerY);
        needleSouthPath.close();
        canvas.drawPath(needleSouthPath, southPaint);

        // 绘制中心圆
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.parseColor("#2C3E50"));
        canvas.drawCircle(centerX, centerY, 12f, circlePaint);
        circlePaint.setStyle(Paint.Style.STROKE);

        // 绘制顶部指示器 (固定的三角形)
        indicatorPaint.setColor(Color.parseColor("#F39C12"));
        Path indicatorPath = new Path();
        indicatorPath.moveTo(centerX, 15f);
        indicatorPath.lineTo(centerX - 15f, 35f);
        indicatorPath.lineTo(centerX + 15f, 35f);
        indicatorPath.close();
        canvas.drawPath(indicatorPath, indicatorPaint);
    }
}