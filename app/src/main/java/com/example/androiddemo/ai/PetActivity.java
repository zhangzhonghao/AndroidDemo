package com.example.androiddemo.ai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class PetActivity extends AppCompatActivity {

    private DogView dogView;
    private TextView tvBubbleTop;
    private TextView tvBubbleBottom;
    private Handler handler = new Handler(Looper.getMainLooper());

    // 动画状态
    private DogView.Pose currentPose = DogView.Pose.STAND;
    private DogView.Direction currentDirection = DogView.Direction.STOP;

    // 随机定时器
    private Runnable randomActionRunnable;
    private Runnable walkAnimationRunnable;
    private Runnable greetingRunnable;

    // 行走动画参数
    private float dogPosX;
    private float dogPosY;
    private float velocityX = 0f;
    private float velocityY = 0f;
    private float walkPhase = 0f;
    private boolean isMoving = false;

    // 边界（运行时确定）
    private int boundLeft, boundRight, boundTop, boundBottom;
    private final float DOG_SPEED = 180f;  // dp/s
    private final float DOG_SIZE = 135f;   // dp（图片262px的1/2）

    // 气泡容器（用于定位）
    private View bubbleContainer;
    private View dogContainer; // 实际移动的是装狗的容器
    private int dogPixelW = 0;
    private int dogPixelH = 0;

    private final Random random = new Random();

    // 各状态的持续时间范围（毫秒）
    private static final long POSE_MIN_DURATION = 2000L;
    private static final long POSE_MAX_DURATION = 5000L;
    private static final long MOVE_MIN_DURATION = 1500L;
    private static final long MOVE_MAX_DURATION = 4000L;
    private static final long STOP_MIN_DURATION = 1000L;
    private static final long STOP_MAX_DURATION = 3000L;

    // 气泡显示时长
    private static final long BUBBLE_DURATION = 3000L;
    private static final long GREETING_INTERVAL = 5000L;

    // 动画帧间隔
    private static final long FRAME_INTERVAL = 16L; // ~60fps

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);

        dogView = findViewById(R.id.dog_view);
        tvBubbleTop = findViewById(R.id.tv_bubble_top);
        tvBubbleBottom = findViewById(R.id.tv_bubble_bottom);
        bubbleContainer = findViewById(R.id.dog_bubble_container);

        dogView.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                showBottomBubble();
            }
            return true;
        });

        // 等待布局完成，获取狗的实际像素尺寸
        dogView.post(() -> {
            dogPixelW = dogView.getWidth();
            dogPixelH = dogView.getHeight();

            // 用屏幕容器作为边界
            View screenContainer = findViewById(R.id.screen_container);
            boundLeft = 0;
            boundRight = screenContainer.getWidth();
            boundTop = 0;
            boundBottom = screenContainer.getHeight();

            dogPosX = boundRight / 2f;
            dogPosY = boundBottom / 2f;
            updateDogPosition();

            startGreetingLoop();
            startRandomLoop();
        });
    }

    // ===== 气泡 =====

    private void showTopBubble() {
        tvBubbleTop.setVisibility(View.VISIBLE);
        tvBubbleBottom.setVisibility(View.GONE);
        handler.removeCallbacks(greetingRunnable);
        greetingRunnable = () -> tvBubbleTop.setVisibility(View.GONE);
        handler.postDelayed(greetingRunnable, BUBBLE_DURATION);
    }

    private void showBottomBubble() {
        tvBubbleBottom.setVisibility(View.VISIBLE);
        tvBubbleTop.setVisibility(View.GONE);
        handler.removeCallbacks(greetingRunnable);
        greetingRunnable = () -> tvBubbleBottom.setVisibility(View.GONE);
        handler.postDelayed(greetingRunnable, BUBBLE_DURATION);
    }

    private void hideBubbles() {
        tvBubbleTop.setVisibility(View.GONE);
        tvBubbleBottom.setVisibility(View.GONE);
    }

    private void startGreetingLoop() {
        handler.postDelayed(() -> {
            if (!isFinishing()) {
                showTopBubble();
                handler.postDelayed(this::startGreetingLoop, GREETING_INTERVAL);
            }
        }, GREETING_INTERVAL);
    }

    // ===== 随机行为 =====

    private void startRandomLoop() {
        randomActionRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    pickNextAction();
                    scheduleNextAction();
                }
            }
        };
        handler.post(randomActionRunnable);
    }

    private void pickNextAction() {
        int roll = random.nextInt(10);
        if (roll < 3) {
            changePose();
        } else if (roll < 7) {
            startMoving();
        } else {
            stopDog();
        }
    }

    private void changePose() {
        DogView.Pose[] poses = {DogView.Pose.STAND, DogView.Pose.SIT, DogView.Pose.LIE};
        DogView.Pose newPose;
        do {
            newPose = poses[random.nextInt(poses.length)];
        } while (newPose == currentPose && random.nextBoolean());

        currentPose = newPose;
        dogView.setPose(currentPose);
        dogView.setMoving(false);
        isMoving = false;
    }

    private void startMoving() {
        DogView.Direction[] dirs = {DogView.Direction.LEFT, DogView.Direction.RIGHT};
        currentDirection = dirs[random.nextInt(dirs.length)];

        DogView.Pose[] mobilePoses = {DogView.Pose.STAND, DogView.Pose.SIT};
        currentPose = mobilePoses[random.nextInt(mobilePoses.length)];

        dogView.setPose(currentPose);
        dogView.setDirection(currentDirection);
        isMoving = true;
        dogView.setMoving(true);

        velocityX = (currentDirection == DogView.Direction.RIGHT) ? DOG_SPEED : -DOG_SPEED;
        velocityY = 0f;

        startWalkAnimation();

        long moveDuration = MOVE_MIN_DURATION + random.nextInt((int) (MOVE_MAX_DURATION - MOVE_MIN_DURATION));
        handler.postDelayed(() -> {
            if (isMoving) stopDog();
        }, moveDuration);
    }

    private void stopDog() {
        isMoving = false;
        dogView.setMoving(false);
        currentDirection = DogView.Direction.STOP;
        velocityX = 0f;
        velocityY = 0f;
        stopWalkAnimation();

        long stopDuration = STOP_MIN_DURATION + random.nextInt((int) (STOP_MAX_DURATION - STOP_MIN_DURATION));
        handler.postDelayed(() -> {
            if (!isFinishing()) pickNextAction();
        }, stopDuration);
    }

    private void scheduleNextAction() {
        long nextDelay = POSE_MIN_DURATION + random.nextInt((int) (POSE_MAX_DURATION - POSE_MIN_DURATION));
        handler.postDelayed(randomActionRunnable, nextDelay);
    }

    // ===== 行走动画 =====

    private void startWalkAnimation() {
        walkAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && isMoving) {
                    updateWalkFrame();
                    handler.postDelayed(this, FRAME_INTERVAL);
                }
            }
        };
        handler.post(walkAnimationRunnable);
    }

    private void stopWalkAnimation() {
        if (walkAnimationRunnable != null) {
            handler.removeCallbacks(walkAnimationRunnable);
            walkAnimationRunnable = null;
        }
        walkPhase = 0f;
        dogView.updateWalkCycle(0f);
    }

    private void updateWalkFrame() {
        walkPhase += 0.15f;
        if (walkPhase > (float) Math.PI * 2) {
            walkPhase -= (float) Math.PI * 2;
        }
        dogView.updateWalkCycle(walkPhase);

        float density = getResources().getDisplayMetrics().density;
        float vx = velocityX * density * (FRAME_INTERVAL / 1000f);
        float vy = velocityY * density * (FRAME_INTERVAL / 1000f);

        dogPosX += vx;
        dogPosY += vy;

        // 边界反弹（dogPixelW/2 是边界修正）
        float halfW = dogPixelW / 2f;
        float halfH = dogPixelH / 2f;
        if (dogPosX < halfW) {
            dogPosX = halfW;
            velocityX = -velocityX;
            currentDirection = DogView.Direction.RIGHT;
            dogView.setDirection(currentDirection);
        } else if (dogPosX > boundRight - halfW) {
            dogPosX = boundRight - halfW;
            velocityX = -velocityX;
            currentDirection = DogView.Direction.LEFT;
            dogView.setDirection(currentDirection);
        }
        if (dogPosY < halfH) {
            dogPosY = halfH;
            velocityY = -velocityY;
        } else if (dogPosY > boundBottom - halfH) {
            dogPosY = boundBottom - halfH;
            velocityY = -velocityY;
        }

        updateDogPosition();
    }

    private void updateDogPosition() {
        bubbleContainer.setX(dogPosX);
        bubbleContainer.setY(dogPosY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
