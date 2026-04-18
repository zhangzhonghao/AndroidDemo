package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class RulerView extends View {

    public static final int UNIT_CM = 0;
    public static final int UNIT_INCH = 1;

    private Paint mainPaint, subPaint, textPaint, linePaint;
    private float baseDensity;
    private float scrollOffset = 0;
    private int unit = UNIT_CM;
    private float dpiCalibration = 1.0f;

    private static final float CM_PER_INCH = 2.54f;

    public RulerView(Context context) {
        super(context);
        init();
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        baseDensity = getContext().getResources().getDisplayMetrics().densityDpi / 96f;
        dpiCalibration = baseDensity;

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setColor(0xFF2196F3);
        mainPaint.setStrokeWidth(3f);
        mainPaint.setStyle(Paint.Style.STROKE);

        subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subPaint.setColor(0xFF2196F3);
        subPaint.setStrokeWidth(1.5f);
        subPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFF2196F3);
        textPaint.setTextSize(32f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0xFF2196F3);
        linePaint.setStrokeWidth(2f);
    }

    public void setUnit(int unit) {
        this.unit = unit;
        invalidate();
    }

    public int getUnit() {
        return unit;
    }

    public void setDpiCalibration(float calibration) {
        this.dpiCalibration = calibration;
        invalidate();
    }

    public float getDpiCalibration() {
        return dpiCalibration;
    }

    public void setScrollOffset(float offset) {
        this.scrollOffset = offset;
        invalidate();
    }

    public float getScrollOffset() {
        return scrollOffset;
    }

    private float lastY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float delta = event.getY() - lastY;
                scrollOffset += delta;
                lastY = event.getY();
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        float centerX = width / 2;

        float xdpi = getContext().getResources().getDisplayMetrics().xdpi;
        float pixelsPerInch = xdpi * dpiCalibration / baseDensity;
        float pixelsPerCm = pixelsPerInch / CM_PER_INCH;

        // 绘制中心红线
        canvas.drawLine(centerX, 0, centerX, height, linePaint);

        if (unit == UNIT_CM) {
            drawCmRuler(canvas, centerX, centerY, pixelsPerCm);
        } else {
            drawInchRuler(canvas, centerX, centerY, pixelsPerInch);
        }
    }

    private void drawCmRuler(Canvas canvas, float centerX, float centerY, float pixelsPerCm) {
        float width = getWidth();
        float startCm = -scrollOffset / pixelsPerCm - 5;
        float endCm = startCm + width / pixelsPerCm + 10;
        int startMark = (int) Math.floor(startCm);
        int endMark = (int) Math.ceil(endCm);

        for (int i = startMark; i <= endMark; i++) {
            float x = centerX + i * pixelsPerCm - scrollOffset;

            // 厘米刻度
            canvas.drawLine(x, centerY - 60, x, centerY + 60, mainPaint);
            canvas.drawText(String.valueOf(i), x, centerY - 70, textPaint);

            // 毫米刻度
            if (i < endMark) {
                for (int j = 1; j < 10; j++) {
                    if (j == 5) continue;
                    float subX = x + j * pixelsPerCm / 10;
                    canvas.drawLine(subX, centerY - 30, subX, centerY + 30, subPaint);
                }
            }
        }
    }

    private void drawInchRuler(Canvas canvas, float centerX, float centerY, float pixelsPerInch) {
        float width = getWidth();
        float startInch = -scrollOffset / pixelsPerInch - 5;
        float endInch = startInch + width / pixelsPerInch + 10;
        int startMark = (int) Math.floor(startInch);
        int endMark = (int) Math.ceil(endInch);

        for (int i = startMark; i <= endMark; i++) {
            float x = centerX + i * pixelsPerInch - scrollOffset;

            // 英寸刻度
            canvas.drawLine(x, centerY - 60, x, centerY + 60, mainPaint);
            canvas.drawText(i + "\"", x, centerY - 70, textPaint);

            // 十分之一英寸刻度
            if (i < endMark) {
                for (int j = 1; j < 10; j++) {
                    float subX = x + j * pixelsPerInch / 10;
                    canvas.drawLine(subX, centerY - 30, subX, centerY + 30, subPaint);
                }
            }
        }
    }

    public float getCurrentValue() {
        float xdpi = getContext().getResources().getDisplayMetrics().xdpi;
        float pixelsPerInch = xdpi * dpiCalibration / baseDensity;
        float pixelsPerCm = pixelsPerInch / CM_PER_INCH;

        if (unit == UNIT_CM) {
            return scrollOffset / pixelsPerCm;
        } else {
            return scrollOffset / pixelsPerInch;
        }
    }
}