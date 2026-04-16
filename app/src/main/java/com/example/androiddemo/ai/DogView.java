package com.example.androiddemo.ai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.androiddemo.R;

/**
 * 自定义宠物狗 View
 * 姿态: 站起(STAND) / 蹲下(SIT) / 趴着(LIE)
 * 动作: 前(FORWARD) / 后(BACKWARD) / 左(LEFT) / 右(RIGHT) / 停(STOP)
 * 图片尺寸 262x300，View 显示尺寸为图片的 1/2（约 131x150dp）
 */
public class DogView extends View {

    // ===== 姿态 =====
    public enum Pose {
        STAND,  // 站立
        SIT,    // 蹲下/坐姿
        LIE     // 趴着
    }

    // ===== 动作方向 =====
    public enum Direction {
        LEFT,   // 向左
        RIGHT,  // 向右
        STOP    // 停止
    }

    private Pose currentPose = Pose.STAND;
    private Direction currentDirection = Direction.RIGHT;
    private boolean isMoving = false;
    private float walkPhase = 0f;

    // 图片 drawable
    private Drawable standDrawable;
    private Drawable sitDrawable;
    private Drawable lieDrawable;
    private Drawable currentDrawable;

    // 图片原始尺寸
    private final int IMG_WIDTH = 262;
    private final int IMG_HEIGHT = 300;

    // ===== 气泡系统 =====
    public enum BubbleType {
        NONE,
        GREETING,   // 头顶气泡："来自哥的问候"
        REPLY       // 底部气泡："莫挨老子"
    }

    private BubbleType activeBubble = BubbleType.NONE;
    private long bubbleShowTime = 0;
    private static final long BUBBLE_DURATION = 3000L; // 气泡显示3秒

    // 气泡画笔
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 点击监听
    private OnDogClickListener clickListener;

    // 定时器
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable greetingRunnable;
    private static final long GREETING_INTERVAL = 5000L; // 5秒一次

    public interface OnDogClickListener {
        void onDogClick();
    }

    public DogView(Context context) {
        super(context);
        init();
    }

    public DogView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DogView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        standDrawable = getContext().getDrawable(R.drawable.dog_stand);
        sitDrawable = getContext().getDrawable(R.drawable.dog_sit);
        lieDrawable = getContext().getDrawable(R.drawable.dog_lie);
        currentDrawable = standDrawable;

        // 气泡背景画笔
        bubblePaint.setColor(Color.parseColor("#FFFDE7")); // 奶白色
        bubblePaint.setStyle(Paint.Style.FILL);

        // 气泡边框
        bubbleStrokePaint.setColor(Color.parseColor("#CCCCCC"));
        bubbleStrokePaint.setStyle(Paint.Style.STROKE);
        bubbleStrokePaint.setStrokeWidth(1.5f);

        // 气泡文字
        bubbleTextPaint.setColor(Color.parseColor("#333333"));
        bubbleTextPaint.setTextSize(28f);
        bubbleTextPaint.setTextAlign(Paint.Align.CENTER);

        // 启动定时问候气泡
        startGreetingTimer();
    }

    private void startGreetingTimer() {
        greetingRunnable = new Runnable() {
            @Override
            public void run() {
                showBubble(BubbleType.GREETING);
                timerHandler.postDelayed(this, GREETING_INTERVAL);
            }
        };
        timerHandler.postDelayed(greetingRunnable, GREETING_INTERVAL);
    }

    // ===== 对外接口 =====

    public void setPose(Pose pose) {
        this.currentPose = pose;
        switch (pose) {
            case STAND: currentDrawable = standDrawable; break;
            case SIT:   currentDrawable = sitDrawable;   break;
            case LIE:   currentDrawable = lieDrawable;   break;
        }
        invalidate();
    }

    public Pose getPose() {
        return currentPose;
    }

    public void setDirection(Direction dir) {
        this.currentDirection = dir;
        invalidate();
    }

    public Direction getDirection() {
        return currentDirection;
    }

    public void setMoving(boolean moving) {
        this.isMoving = moving;
        if (!moving) walkPhase = 0f;
        invalidate();
    }

    public void updateWalkCycle(float cycle) {
        this.walkPhase = cycle;
        invalidate();
    }

    public void setOnDogClickListener(OnDogClickListener listener) {
        this.clickListener = listener;
    }

    // ===== 气泡控制 =====

    public void showBubble(BubbleType type) {
        activeBubble = type;
        bubbleShowTime = System.currentTimeMillis();
        invalidate();

        // 3秒后自动消失
        timerHandler.postDelayed(() -> {
            if (activeBubble == type) {
                activeBubble = BubbleType.NONE;
                invalidate();
            }
        }, BUBBLE_DURATION);
    }

    public void hideBubble() {
        activeBubble = BubbleType.NONE;
        invalidate();
    }

    // ===== 尺寸 =====

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // View 尺寸为图片的 1/2（放大5倍）：约 131x150dp
        int targetW = (int) (IMG_WIDTH / 2f);
        int targetH = (int) (IMG_HEIGHT / 2f);

        int w = resolveSize(targetW, widthMeasureSpec);
        int h = resolveSize(targetH, heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    // ===== 绘制 =====

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        // 1. 绘制气泡（在图片下方或上方）
        // 临时扩大 clip 区域，避免气泡被裁剪
        canvas.save();
        canvas.clipRect(-200, -200, w + 200, h + 200);

        if (activeBubble != BubbleType.NONE) {
            drawBubble(canvas, w, h);
        }

        canvas.restore();

        // 2. 绘制狗图片（带方向翻转）
        if (currentDrawable == null) return;

        canvas.save();
        if (currentDirection == Direction.LEFT) {
            canvas.scale(-1, 1, w / 2f, h / 2f);
        }

        currentDrawable.setBounds(0, 0, w, h);
        currentDrawable.draw(canvas);

        canvas.restore();
    }

    private void drawBubble(Canvas canvas, int viewW, int viewH) {
        String text;
        float bubbleW, bubbleH;
        float bubbleX, bubbleY;
        float tailX, tailY;
        boolean tailPointsUp; // 尖角朝上（气泡在狗下方）还是朝下（气泡在狗上方）

        if (activeBubble == BubbleType.GREETING) {
            // 头顶气泡，气泡在狗上方，尖角朝下
            text = "来自哥的问候";
            bubbleW = 180f;
            bubbleH = 50f;
            bubbleX = viewW / 2f - bubbleW / 2f;
            bubbleY = -bubbleH - 6f;
            tailX = viewW / 2f;
            tailY = bubbleY + bubbleH; // 尖角接在气泡底部
            tailPointsUp = false;
        } else {
            // 底部气泡，气泡在狗下方，尖角朝上
            text = "莫挨老子";
            bubbleW = 150f;
            bubbleH = 45f;
            bubbleX = viewW / 2f - bubbleW / 2f;
            bubbleY = viewH + 6f;
            tailX = viewW / 2f;
            tailY = bubbleY; // 尖角接在气泡顶部
            tailPointsUp = true;
        }

        // 气泡圆角矩形
        RectF rect = new RectF(bubbleX, bubbleY, bubbleX + bubbleW, bubbleY + bubbleH);
        float radius = 10f;
        canvas.drawRoundRect(rect, radius, radius, bubblePaint);
        canvas.drawRoundRect(rect, radius, radius, bubbleStrokePaint);

        // 气泡尖角（三角形）
        Path tail = new Path();
        if (tailPointsUp) {
            // 尖角朝上，指向狗
            tail.moveTo(tailX - 10f, tailY);
            tail.lineTo(tailX, tailY - 14f);
            tail.lineTo(tailX + 10f, tailY);
        } else {
            // 尖角朝下，指向狗
            tail.moveTo(tailX - 10f, tailY);
            tail.lineTo(tailX, tailY + 14f);
            tail.lineTo(tailX + 10f, tailY);
        }
        tail.close();
        canvas.drawPath(tail, bubblePaint);
        canvas.drawPath(tail, bubbleStrokePaint);

        // 气泡文字
        float textY = bubbleY + bubbleH / 2f + getTextPaintOffset(bubbleTextPaint);
        canvas.drawText(text, viewW / 2f, textY, bubbleTextPaint);
    }

    private float getTextPaintOffset(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (fm.descent - fm.ascent) / 2f - fm.descent;
    }

    // ===== 点击事件 =====

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (clickListener != null) {
                clickListener.onDogClick();
            }
            // 点击气泡显示"莫挨老子"
            showBubble(BubbleType.REPLY);
            return true;
        }
        return true;
    }

    // ===== 生命周期 =====

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timerHandler.removeCallbacksAndMessages(null);
    }
}
