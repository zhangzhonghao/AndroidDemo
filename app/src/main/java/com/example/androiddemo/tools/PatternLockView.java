package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 九宫格手势解锁视图
 */
public class PatternLockView extends View {

    private static final int DOT_COUNT = 3; // 每行3个点
    private static final float DOT_RADIUS = 20f;
    private static final float DOT_STROKE_WIDTH = 3f;
    private static final float LINE_WIDTH = 4f;
    private static final float SELECTED_DOT_RADIUS = 30f;

    private Paint dotPaint;
    private Paint selectedDotPaint;
    private Paint linePaint;
    private Paint errorPaint;

    private float dotRadius = DOT_RADIUS;
    private float spacing = 0f;
    private List<Integer> selectedDots = new ArrayList<>();
    private boolean isError = false;

    private OnPatternListener listener;

    private float lastX, lastY;

    public interface OnPatternListener {
        void onPatternStarted();
        void onPatternComplete(List<Integer> pattern);
        void onPatternCleared();
    }

    public PatternLockView(Context context) {
        super(context);
        init(context);
    }

    public PatternLockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PatternLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.STROKE);
        dotPaint.setStrokeWidth(DOT_STROKE_WIDTH);
        dotPaint.setColor(ContextCompat.getColor(context, R.color.on_surface_variant));

        selectedDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedDotPaint.setStyle(Paint.Style.FILL);
        selectedDotPaint.setColor(ContextCompat.getColor(context, R.color.primary));

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setColor(ContextCompat.getColor(context, R.color.primary));
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        errorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        errorPaint.setStyle(Paint.Style.FILL);
        errorPaint.setColor(ContextCompat.getColor(context, R.color.error));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 计算点之间的间距
        float availableSize = Math.min(w, h);
        spacing = availableSize / DOT_COUNT;
        dotRadius = spacing * 0.15f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDots(canvas);
        drawLines(canvas);
    }

    private void drawDots(Canvas canvas) {
        Paint fillPaint = isError ? errorPaint : selectedDotPaint;

        for (int i = 0; i < DOT_COUNT * DOT_COUNT; i++) {
            float cx = getCenterX(i % DOT_COUNT);
            float cy = getCenterY(i / DOT_COUNT);

            // 绘制外圈
            dotPaint.setColor(isError ? ContextCompat.getColor(getContext(), R.color.error) :
                    ContextCompat.getColor(getContext(), R.color.on_surface_variant));
            canvas.drawCircle(cx, cy, dotRadius, dotPaint);

            // 如果选中，绘制填充
            if (selectedDots.contains(i)) {
                canvas.drawCircle(cx, cy, dotRadius * 0.5f, fillPaint);
            }
        }
    }

    private void drawLines(Canvas canvas) {
        if (selectedDots.size() < 1) return;

        Paint paint = isError ? errorPaint : linePaint;
        paint.setColor(isError ? ContextCompat.getColor(getContext(), R.color.error) :
                ContextCompat.getColor(getContext(), R.color.primary));

        float lastX = getCenterX(selectedDots.get(0) % DOT_COUNT);
        float lastY = getCenterY(selectedDots.get(0) / DOT_COUNT);

        for (int i = 1; i < selectedDots.size(); i++) {
            float cx = getCenterX(selectedDots.get(i) % DOT_COUNT);
            float cy = getCenterY(selectedDots.get(i) / DOT_COUNT);
            canvas.drawLine(lastX, lastY, cx, cy, paint);
            lastX = cx;
            lastY = cy;
        }

        // 绘制到当前触摸点的线
        if (this.lastX > 0 && this.lastY > 0) {
            canvas.drawLine(lastX, lastY, this.lastX, this.lastY, paint);
        }
    }

    private float getCenterX(int col) {
        return spacing * (col + 0.5f);
    }

    private float getCenterY(int row) {
        return spacing * (row + 0.5f);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isError) return true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleTouch(x, y);
                if (listener != null) {
                    listener.onPatternStarted();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                handleTouch(x, y);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!selectedDots.isEmpty() && listener != null) {
                    listener.onPatternComplete(new ArrayList<>(selectedDots));
                }
                break;
        }

        lastX = x;
        lastY = y;
        return true;
    }

    private void handleTouch(float x, float y) {
        int dotIndex = getDotIndex(x, y);
        if (dotIndex != -1 && !selectedDots.contains(dotIndex)) {
            selectedDots.add(dotIndex);
            invalidate();
        }
    }

    private int getDotIndex(float x, float y) {
        int col = (int) (x / spacing);
        int row = (int) (y / spacing);

        if (col >= 0 && col < DOT_COUNT && row >= 0 && row < DOT_COUNT) {
            float centerX = getCenterX(col);
            float centerY = getCenterY(row);
            float distance = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));

            if (distance <= dotRadius * 2.5f) {
                return row * DOT_COUNT + col;
            }
        }
        return -1;
    }

    public void setOnPatternListener(OnPatternListener listener) {
        this.listener = listener;
    }

    public void clearPattern() {
        selectedDots.clear();
        isError = false;
        lastX = 0;
        lastY = 0;
        invalidate();
        if (listener != null) {
            listener.onPatternCleared();
        }
    }

    public void setError(boolean error) {
        isError = error;
        invalidate();
    }

    public List<Integer> getPattern() {
        return new ArrayList<>(selectedDots);
    }

    public void setPattern(List<Integer> pattern) {
        selectedDots = new ArrayList<>(pattern);
        invalidate();
    }
}