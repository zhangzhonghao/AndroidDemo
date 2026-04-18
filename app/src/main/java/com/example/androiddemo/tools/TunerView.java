package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * 自定义调音器视图 - 圆形指示器
 */
public class TunerView extends View {

    private Paint backgroundPaint;
    private Paint arcBackgroundPaint;
    private Paint arcInTunePaint;
    private Paint needlePaint;
    private Paint textPaint;
    private Paint noteTextPaint;
    private Paint centerCirclePaint;
    private Paint tickPaint;

    private int noteCount = 12;
    private String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    // 音高偏移（-50到+50 cents）
    private float centsOffset = 0f;
    // 是否在调音状态（接近目标音）
    private boolean isInTune = false;
    // 检测到有效声音
    private boolean hasSignal = false;

    // 颜色定义
    private static final int COLOR_BACKGROUND = Color.parseColor("#1A252F");
    private static final int COLOR_FLAT = Color.parseColor("#E74C3C");
    private static final int COLOR_SHARP = Color.parseColor("#E74C3C");
    private static final int COLOR_IN_TUNE = Color.parseColor("#2ECC71");
    private static final int COLOR_NEEDLE = Color.parseColor("#FFFFFF");
    private static final int COLOR_TEXT = Color.parseColor("#7F8C8D");
    private static final int COLOR_NOTE_TEXT = Color.parseColor("#ECF0F1");
    private static final int COLOR_ARC_BG = Color.parseColor("#34495E");

    // 阈值（cents）
    private static final float IN_TUNE_THRESHOLD = 5f;

    public TunerView(Context context) {
        super(context);
        init();
    }

    public TunerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TunerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(COLOR_BACKGROUND);
        backgroundPaint.setStyle(Paint.Style.FILL);

        arcBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcBackgroundPaint.setColor(COLOR_ARC_BG);
        arcBackgroundPaint.setStyle(Paint.Style.STROKE);
        arcBackgroundPaint.setStrokeWidth(30f);
        arcBackgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        arcInTunePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcInTunePaint.setStyle(Paint.Style.STROKE);
        arcInTunePaint.setStrokeWidth(30f);
        arcInTunePaint.setStrokeCap(Paint.Cap.ROUND);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(COLOR_NEEDLE);
        needlePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(COLOR_TEXT);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        noteTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        noteTextPaint.setColor(COLOR_NOTE_TEXT);
        noteTextPaint.setTextSize(32f);
        noteTextPaint.setTextAlign(Paint.Align.CENTER);
        noteTextPaint.setFakeBoldText(true);

        centerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerCirclePaint.setColor(COLOR_NEEDLE);
        centerCirclePaint.setStyle(Paint.Style.FILL);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(COLOR_TEXT);
        tickPaint.setStyle(Paint.Style.STROKE);
        tickPaint.setStrokeWidth(2f);
    }

    public void setCentsOffset(float cents) {
        this.centsOffset = cents;
        this.hasSignal = true;
        this.isInTune = Math.abs(cents) <= IN_TUNE_THRESHOLD;
        invalidate();
    }

    public void setNoSignal() {
        this.hasSignal = false;
        this.centsOffset = 0f;
        invalidate();
    }

    public void setInTune(boolean inTune) {
        this.isInTune = inTune;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 2f - 40f;

        // 绘制背景
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // 绘制弧形背景（-50到+50 cents范围）
        RectF arcRect = new RectF(centerX - radius, centerY - radius,
                                  centerX + radius, centerY + radius);
        canvas.drawArc(arcRect, 135f, 270f, false, arcBackgroundPaint);

        // 绘制彩色指示弧
        drawColorArc(canvas, centerX, centerY, radius);

        // 绘制刻度线
        drawTicks(canvas, centerX, centerY, radius);

        // 绘制音符标签
        drawNoteLabels(canvas, centerX, centerY, radius);

        // 绘制指针
        if (hasSignal) {
            drawNeedle(canvas, centerX, centerY, radius - 50f);
        }

        // 绘制中心圆
        canvas.drawCircle(centerX, centerY, 15f, centerCirclePaint);

        // 绘制Cents文本
        if (hasSignal) {
            drawCentsText(canvas, centerX, centerY + radius + 60f);
        } else {
            textPaint.setTextSize(28f);
            canvas.drawText("请弹奏音符", centerX, centerY + radius + 60f, textPaint);
        }
    }

    private void drawColorArc(Canvas canvas, float centerX, float centerY, float radius) {
        RectF arcRect = new RectF(centerX - radius, centerY - radius,
                                  centerX + radius, centerY + radius);

        if (!hasSignal) {
            return;
        }

        // 根据偏移量设置颜色
        int arcColor;
        if (isInTune) {
            arcColor = COLOR_IN_TUNE;
        } else if (centsOffset < 0) {
            arcColor = COLOR_FLAT;
        } else {
            arcColor = COLOR_SHARP;
        }

        arcInTunePaint.setColor(arcColor);

        // 中心位置是0 cents，从135度开始
        // 270度范围对应-50到+50 cents
        float startAngle = 135f;
        float sweepAngle = (centsOffset + 50f) / 100f * 270f;

        canvas.drawArc(arcRect, startAngle, sweepAngle - 135f, false, arcInTunePaint);
    }

    private void drawTicks(Canvas canvas, float centerX, float centerY, float radius) {
        float tickRadius = radius + 10f;
        float innerTickRadius = radius - 15f;

        // 绘制主刻度（每10 cents一个）
        for (int i = 0; i <= 10; i++) {
            float angle = (float) Math.toRadians(135 + i * 27);
            float startX = centerX + tickRadius * (float) Math.cos(angle);
            float startY = centerY - tickRadius * (float) Math.sin(angle);
            float endX = centerX + innerTickRadius * (float) Math.cos(angle);
            float endY = centerY - innerTickRadius * (float) Math.sin(angle);

            canvas.drawLine(startX, startY, endX, endY, tickPaint);
        }
    }

    private void drawNoteLabels(Canvas canvas, float centerX, float centerY, float radius) {
        float labelRadius = radius + 45f;

        // 只在特定位置显示标签
        String[] labels = {"-50", "-25", "0", "+25", "+50"};
        float[] angles = {135f, 157.5f, 180f, 202.5f, 225f};

        textPaint.setTextSize(20f);
        for (int i = 0; i < labels.length; i++) {
            float angle = (float) Math.toRadians(angles[i]);
            float x = centerX + labelRadius * (float) Math.cos(angle);
            float y = centerY - labelRadius * (float) Math.sin(angle);

            canvas.drawText(labels[i], x, y + 8f, textPaint);
        }
    }

    private void drawNeedle(Canvas canvas, float centerX, float centerY, float length) {
        // 指针角度计算：0 cents对应270度位置（顶部）
        // -50 cents = 135度，+50 cents = 225度
        float angle = (float) Math.toRadians(270 - centsOffset * 2.7f);

        float endX = centerX + length * (float) Math.cos(angle);
        float endY = centerY - length * (float) Math.sin(angle);

        // 设置指针颜色
        if (isInTune) {
            needlePaint.setColor(COLOR_IN_TUNE);
        } else {
            needlePaint.setColor(COLOR_NEEDLE);
        }

        // 绘制指针线
        needlePaint.setStrokeWidth(4f);
        canvas.drawLine(centerX, centerY, endX, endY, needlePaint);

        // 绘制指针尾部
        float tailAngle1 = (float) Math.toRadians(270 - centsOffset * 2.7f + 150);
        float tailAngle2 = (float) Math.toRadians(270 - centsOffset * 2.7f - 150);
        float tailLength = 20f;

        float tailX1 = centerX + tailLength * (float) Math.cos(tailAngle1);
        float tailY1 = centerY + tailLength * (float) Math.sin(tailAngle1);
        float tailX2 = centerX + tailLength * (float) Math.cos(tailAngle2);
        float tailY2 = centerY + tailLength * (float) Math.sin(tailAngle2);

        needlePaint.setStrokeWidth(3f);
        canvas.drawLine(centerX, centerY, tailX1, tailY1, needlePaint);
        canvas.drawLine(centerX, centerY, tailX2, tailY2, needlePaint);
    }

    private void drawCentsText(Canvas canvas, float centerX, float centerY) {
        String centsText;
        if (centsOffset < 0) {
            centsText = String.format("%.0f cents (偏低)", centsOffset);
        } else if (centsOffset > 0) {
            centsText = String.format("+%.0f cents (偏高)", centsOffset);
        } else {
            centsText = "0 cents (准确)";
        }

        textPaint.setTextSize(24f);
        if (isInTune) {
            textPaint.setColor(COLOR_IN_TUNE);
        } else {
            textPaint.setColor(COLOR_TEXT);
        }

        canvas.drawText(centsText, centerX, centerY, textPaint);
    }
}