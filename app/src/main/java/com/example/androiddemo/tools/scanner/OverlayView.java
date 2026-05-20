package com.example.androiddemo.tools.scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import org.opencv.core.Point;

public class OverlayView extends View {

    private final Paint paint;
    private final Paint debugPaint;
    private RectF rect;
    private Path polygonPath;
    private Point[] polygonPoints;
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
        polygonPath = null;
        polygonPoints = null;
        invalidate();
    }

    public void setPolygon(Point[] points) {
        if (points == null || points.length != 4) {
            setRect(null);
            return;
        }

        Path path = new Path();
        path.moveTo((float) points[0].x, (float) points[0].y);
        for (int i = 1; i < points.length; i++) {
            path.lineTo((float) points[i].x, (float) points[i].y);
        }
        path.close();
        polygonPath = path;
        polygonPoints = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            polygonPoints[i] = new Point(points[i].x, points[i].y);
        }

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (Point point : points) {
            float x = (float) point.x;
            float y = (float) point.y;
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }
        rect = new RectF(minX, minY, maxX, maxY);
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

    public Point[] getPolygon() {
        if (polygonPoints == null) return null;

        Point[] copy = new Point[polygonPoints.length];
        for (int i = 0; i < polygonPoints.length; i++) {
            copy[i] = new Point(polygonPoints[i].x, polygonPoints[i].y);
        }
        return copy;
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

        if (polygonPath != null) {
            canvas.drawPath(polygonPath, paint);
        } else {
            canvas.drawRect(rect, paint);
        }
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
