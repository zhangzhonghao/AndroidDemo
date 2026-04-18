package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class DrawBoardView extends View {

    private Paint paint;
    private Path currentPath;
    private List<DrawPath> pathList;
    private List<DrawPath> redoList;
    private int currentColor = Color.BLACK;
    private float strokeWidth = 8f;
    private boolean isEraser = false;
    private float eraserWidth = 40f;

    private static class DrawPath {
        Path path;
        Paint paint;

        DrawPath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    public DrawBoardView(Context context) {
        super(context);
        init();
    }

    public DrawBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(currentColor);
        paint.setStrokeWidth(strokeWidth);

        currentPath = new Path();
        pathList = new ArrayList<>();
        redoList = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        for (DrawPath dp : pathList) {
            canvas.drawPath(dp.path, dp.paint);
        }
        canvas.drawPath(currentPath, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                if (isEraser) {
                    paint.setColor(Color.WHITE);
                    paint.setStrokeWidth(eraserWidth);
                } else {
                    paint.setColor(currentColor);
                    paint.setStrokeWidth(strokeWidth);
                }
                redoList.clear();
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                pathList.add(new DrawPath(currentPath, new Paint(paint)));
                currentPath = new Path();
                invalidate();
                return true;
        }
        return false;
    }

    public void setColor(int color) {
        this.currentColor = color;
        this.isEraser = false;
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
    }

    public void setEraser(boolean eraser) {
        this.isEraser = eraser;
    }

    public void clear() {
        pathList.clear();
        redoList.clear();
        currentPath = new Path();
        invalidate();
    }

    public void undo() {
        if (!pathList.isEmpty()) {
            redoList.add(pathList.remove(pathList.size() - 1));
            invalidate();
        }
    }

    public void redo() {
        if (!redoList.isEmpty()) {
            pathList.add(redoList.remove(redoList.size() - 1));
            invalidate();
        }
    }

    public Bitmap getBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        for (DrawPath dp : pathList) {
            canvas.drawPath(dp.path, dp.paint);
        }
        return bitmap;
    }

    public boolean hasDrawing() {
        return !pathList.isEmpty();
    }
}