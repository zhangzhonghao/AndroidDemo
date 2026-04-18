package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ColorPickerView extends androidx.appcompat.widget.AppCompatImageView {

    private OnColorSelectedListener listener;
    private Bitmap colorBitmap;
    private Paint bitmapPaint;
    private float currentX, currentY;
    private boolean initialized = false;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_XY);
        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setImageResource(android.R.color.transparent);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            createColorPalette(w, h);
        }
    }

    private void createColorPalette(int width, int height) {
        colorBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorBitmap);

        int saturation = 255;
        int value = 255;

        int[] colors = new int[width];
        float[] hsv = new float[3];
        hsv[1] = 1.0f;
        hsv[2] = 1.0f;

        for (int x = 0; x < width; x++) {
            hsv[0] = (float) x / width * 360f;
            colors[x] = Color.HSVToColor(hsv);
        }

        LinearGradient gradient = new LinearGradient(0, 0, 0, height,
                new int[]{Color.WHITE, Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP);
        Paint gradientPaint = new Paint();
        gradientPaint.setShader(gradient);

        for (int x = 0; x < width; x++) {
            Paint p = new Paint();
            p.setColor(colors[x]);
            canvas.drawRect(x, 0, x + 1, height, p);
        }

        canvas.drawRect(0, 0, width, height, gradientPaint);

        setImageBitmap(colorBitmap);
        initialized = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!initialized || colorBitmap == null) {
            return true;
        }

        float x = event.getX();
        float y = event.getY();

        if (x >= 0 && x < colorBitmap.getWidth() && y >= 0 && y < colorBitmap.getHeight()) {
            currentX = x;
            currentY = y;

            int touchedColor = colorBitmap.getPixel((int) x, (int) y);

            if (listener != null) {
                listener.onColorSelected(touchedColor);
            }
        }

        return true;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener l) {
        this.listener = l;
    }

    public int getColorAtPoint(float x, float y) {
        if (colorBitmap != null && x >= 0 && x < colorBitmap.getWidth()
                && y >= 0 && y < colorBitmap.getHeight()) {
            return colorBitmap.getPixel((int) x, (int) y);
        }
        return Color.BLACK;
    }
}