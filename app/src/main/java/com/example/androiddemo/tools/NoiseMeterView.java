package com.example.androiddemo.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义噪音计视图 - 分贝指示器和波形显示
 */
public class NoiseMeterView extends View {

    private Paint backgroundPaint;
    private Paint waveformPaint;
    private Paint levelIndicatorPaint;
    private Paint textPaint;
    private Paint gridPaint;

    private List<Float> amplitudeHistory = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 50;

    // 分贝等级阈值
    private static final float DB_QUIET = 40f;
    private static final float DB_NORMAL = 60f;
    private static final float DB_LOUD = 80f;
    private static final float DB_VERY_LOUD = 100f;

    public NoiseMeterView(Context context) {
        super(context);
        init();
    }

    public NoiseMeterView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoiseMeterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.parseColor("#1A252F"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        waveformPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waveformPaint.setColor(Color.parseColor("#2ECC71"));
        waveformPaint.setStyle(Paint.Style.STROKE);
        waveformPaint.setStrokeWidth(3f);

        levelIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        levelIndicatorPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#7F8C8D"));
        textPaint.setTextSize(20f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#34495E"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
    }

    public void addAmplitude(float amplitude) {
        amplitudeHistory.add(amplitude);
        if (amplitudeHistory.size() > MAX_HISTORY_SIZE) {
            amplitudeHistory.remove(0);
        }
        invalidate();
    }

    public void clearHistory() {
        amplitudeHistory.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        // 绘制背景
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // 绘制网格
        drawGrid(canvas, width, height);

        // 绘制分贝等级指示条
        drawLevelIndicator(canvas, width, height);

        // 绘制波形
        drawWaveform(canvas, width, height);
    }

    private void drawGrid(Canvas canvas, float width, float height) {
        // 绘制水平网格线
        for (int i = 1; i < 5; i++) {
            float y = height * i / 5;
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        // 绘制垂直网格线
        for (int i = 1; i < 10; i++) {
            float x = width * i / 10;
            canvas.drawLine(x, 0, x, height, gridPaint);
        }
    }

    private void drawLevelIndicator(Canvas canvas, float width, float height) {
        // 绘制底部渐变色等级条
        float indicatorHeight = height * 0.15f;
        float indicatorTop = height - indicatorHeight - 30f;

        // 渐变色：绿->黄->橙->红
        LinearGradient gradient = new LinearGradient(
                0, indicatorTop, width, indicatorTop,
                new int[]{
                        Color.parseColor("#2ECC71"),  // 安静-绿
                        Color.parseColor("#F1C40F"),  // 一般-黄
                        Color.parseColor("#E67E22"),  // 嘈杂-橙
                        Color.parseColor("#E74C3C")   // 危险-红
                },
                new float[]{0f, 0.25f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );

        levelIndicatorPaint.setShader(gradient);
        canvas.drawRect(0, indicatorTop, width, indicatorTop + indicatorHeight, levelIndicatorPaint);

        // 绘制等级刻度文字
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(16f);
        canvas.drawText("0", 20f, indicatorTop + indicatorHeight - 5f, textPaint);
        canvas.drawText("40", width * 0.25f, indicatorTop + indicatorHeight - 5f, textPaint);
        canvas.drawText("60", width * 0.5f, indicatorTop + indicatorHeight - 5f, textPaint);
        canvas.drawText("80", width * 0.75f, indicatorTop + indicatorHeight - 5f, textPaint);
        canvas.drawText("100+", width - 30f, indicatorTop + indicatorHeight - 5f, textPaint);
    }

    private void drawWaveform(Canvas canvas, float width, float height) {
        if (amplitudeHistory.isEmpty()) {
            return;
        }

        float waveformHeight = height * 0.65f;
        float waveformTop = 40f;

        // 根据当前分贝设置波形颜色
        float currentDb = getCurrentDb();
        int waveColor = getColorForDb(currentDb);
        waveformPaint.setColor(waveColor);

        // 绘制波形
        float step = width / (MAX_HISTORY_SIZE - 1);
        int startIndex = MAX_HISTORY_SIZE - amplitudeHistory.size();

        for (int i = 0; i < amplitudeHistory.size() - 1; i++) {
            int index = startIndex + i;
            if (index >= MAX_HISTORY_SIZE - 1) continue;

            float x1 = index * step;
            float x2 = (index + 1) * step;

            // 将振幅转换为波形高度（归一化到0-1）
            float amp1 = amplitudeHistory.get(i) / 32767f;
            float amp2 = amplitudeHistory.get(i + 1) / 32767f;

            float y1 = waveformTop + waveformHeight * (1 - amp1);
            float y2 = waveformTop + waveformHeight * (1 - amp2);

            // 渐变透明度
            float alpha = 0.3f + 0.7f * (i / (float) amplitudeHistory.size());
            waveformPaint.setAlpha((int) (255 * alpha));
            waveformPaint.setStrokeWidth(2f + 2f * alpha);

            canvas.drawLine(x1, y1, x2, y2, waveformPaint);
        }
    }

    private float getCurrentDb() {
        if (amplitudeHistory.isEmpty()) return 0f;
        float latest = amplitudeHistory.get(amplitudeHistory.size() - 1);
        return amplitudeToDb(latest);
    }

    private int getColorForDb(float db) {
        if (db < DB_QUIET) {
            return Color.parseColor("#2ECC71"); // 安静-绿
        } else if (db < DB_NORMAL) {
            return Color.parseColor("#F1C40F"); // 一般-黄
        } else if (db < DB_LOUD) {
            return Color.parseColor("#E67E22"); // 嘈杂-橙
        } else if (db < DB_VERY_LOUD) {
            return Color.parseColor("#E74C3C"); // 非常嘈杂-红
        } else {
            return Color.parseColor("#9B59B6"); // 危险-紫
        }
    }

    /**
     * 将振幅转换为分贝值
     * 基准参考：最大振幅 32767 对应约 90dB
     */
    public static float amplitudeToDb(float amplitude) {
        if (amplitude <= 0) return 0f;
        // 使用 20 * log10(amplitude / reference)
        // 基准参考取 1，对应 0dB
        // 但实际需要校准到环境噪音
        // 这里用一个经验公式：最大振幅 32767 -> ~90dB
        double db = 20 * Math.log10(amplitude / 1.0);
        // 调整为实际环境分贝范围
        // 20 * log10(32767) ≈ 90dB
        // 所以实际显示时需要加上一个偏移量（约40dB）来匹配环境噪音
        return (float) (db + 40);
    }
}