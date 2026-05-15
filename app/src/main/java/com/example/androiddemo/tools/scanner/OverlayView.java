package com.example.androiddemo.tools.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class OverlayView extends View {

    private final Paint paint;
    private final Paint debugPaint;
    private RectF rect;
    private String debugText = "";
    private int analysisCount = 0;
    private int detectCount = 0;

    private static final float DEFAULT_RATIO = 1.414f; // A4 ratio

    public OverlayView(Context context) {
        this(context, null);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.parseColor("#2196F3"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dpToPx(3));
        paint.setAntiAlias(true);

        debugPaint = new Paint();
        debugPaint.setColor(Color.parseColor("#FFEB3B"));
        debugPaint.setTextSize(dpToPx(12));
        debugPaint.setAntiAlias(true);
        debugPaint.setShadowLayer(2, 0, 1, Color.BLACK);
    }

    public void setRect(RectF r) {
        if (r != null) {
            rect = new RectF(r);
        } else {
            rect = null;
        }
        invalidate();
    }

    public void onAnalysisFrame(boolean found) {
        analysisCount++;
        if (found) detectCount++;
        debugText = "scan:" + analysisCount + " hit:" + detectCount;
        invalidate();
    }

    public RectF getRect() {
        if (rect != null) return rect;
        return getDefaultRect();
    }

    public RectF getDefaultRect() {
        float w = getWidth() * 0.85f;
        float h = w * DEFAULT_RATIO;
        float left = (getWidth() - w) / 2;
        float top = (getHeight() - h) / 2;
        return new RectF(left, top, left + w, top + h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw debug text
        if (!debugText.isEmpty()) {
            canvas.drawText(debugText, dpToPx(16), dpToPx(40), debugPaint);
        }

        if (rect == null) {
            drawDefaultGuideRect(canvas);
            return;
        }

        canvas.drawRect(rect, paint);
    }

    private void drawDefaultGuideRect(Canvas canvas) {
        float w = getWidth() * 0.85f;
        float h = w * DEFAULT_RATIO;
        float left = (getWidth() - w) / 2;
        float top = (getHeight() - h) / 2;
        canvas.drawRect(left, top, left + w, top + h, paint);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
