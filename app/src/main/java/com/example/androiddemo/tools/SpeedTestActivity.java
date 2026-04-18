package com.example.androiddemo.tools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpeedTestActivity extends AppCompatActivity {

    private TextView tvBestScore;
    private TextView tvStatus;
    private TextView tvCurrentResult;
    private TextView tvAverageResult;
    private Button btnStart;
    private View clickArea;

    private Handler handler = new Handler();
    private Random random = new Random();

    private static final String PREF_NAME = "speed_test_prefs";
    private static final String KEY_BEST_SCORE = "best_score";
    private static final String KEY_TOTAL_TIME = "total_time";
    private static final String KEY_TEST_COUNT = "test_count";

    // Game states
    private static final int STATE_IDLE = 0;
    private static final int STATE_WAITING = 1;
    private static final int STATE_GO = 2;
    private static final int STATE_RESULT = 3;

    private int currentState = STATE_IDLE;
    private long goStartTime = 0;
    private long reactionTime = 0;

    private SharedPreferences prefs;
    private List<Long> testResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_speed_test);

        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        setupListeners();
        loadStats();
        updateUI();
    }

    private void initViews() {
        tvBestScore = findViewById(R.id.tv_best_score);
        tvStatus = findViewById(R.id.tv_status);
        tvCurrentResult = findViewById(R.id.tv_current_result);
        tvAverageResult = findViewById(R.id.tv_average_result);
        btnStart = findViewById(R.id.btn_start);
        clickArea = findViewById(R.id.click_area);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> {
            if (currentState == STATE_IDLE || currentState == STATE_RESULT) {
                startWaiting();
            }
        });

        clickArea.setOnClickListener(v -> onAreaClick());
    }

    private void startWaiting() {
        currentState = STATE_WAITING;
        tvStatus.setText("等待中...");
        tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        tvCurrentResult.setText("");
        btnStart.setEnabled(false);

        // Random delay between 1-5 seconds
        long delay = 1000 + random.nextInt(4000);
        handler.postDelayed(this::showGo, delay);
    }

    private void showGo() {
        currentState = STATE_GO;
        goStartTime = System.currentTimeMillis();
        tvStatus.setText("点击!");
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
    }

    private void onAreaClick() {
        if (currentState == STATE_WAITING) {
            // Clicked too early - false start
            handler.removeCallbacksAndMessages(null);
            currentState = STATE_IDLE;
            tvStatus.setText("太心急了! 点击 START 重试");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
            btnStart.setEnabled(true);
        } else if (currentState == STATE_GO) {
            // Calculate reaction time
            reactionTime = System.currentTimeMillis() - goStartTime;
            currentState = STATE_RESULT;
            testResults.add(reactionTime);

            // Update stats
            updateStats();

            // Show result
            tvStatus.setText("反应时间");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
            tvCurrentResult.setText(reactionTime + " 毫秒");
            btnStart.setText("再来一次");
            btnStart.setEnabled(true);
        }
    }

    private void updateStats() {
        long bestScore = prefs.getLong(KEY_BEST_SCORE, Long.MAX_VALUE);
        long totalTime = prefs.getLong(KEY_TOTAL_TIME, 0);
        int testCount = prefs.getInt(KEY_TEST_COUNT, 0);

        testCount++;
        totalTime += reactionTime;

        if (reactionTime < bestScore) {
            bestScore = reactionTime;
            prefs.edit()
                    .putLong(KEY_BEST_SCORE, bestScore)
                    .putLong(KEY_TOTAL_TIME, totalTime)
                    .putInt(KEY_TEST_COUNT, testCount)
                    .apply();
        } else {
            prefs.edit()
                    .putLong(KEY_TOTAL_TIME, totalTime)
                    .putInt(KEY_TEST_COUNT, testCount)
                    .apply();
        }

        loadStats();
    }

    private void loadStats() {
        long bestScore = prefs.getLong(KEY_BEST_SCORE, Long.MAX_VALUE);
        long totalTime = prefs.getLong(KEY_TOTAL_TIME, 0);
        int testCount = prefs.getInt(KEY_TEST_COUNT, 0);

        if (bestScore == Long.MAX_VALUE) {
            tvBestScore.setText("最佳: -- 毫秒");
        } else {
            tvBestScore.setText("最佳: " + bestScore + " 毫秒");
        }

        if (testCount > 0) {
            long average = totalTime / testCount;
            tvAverageResult.setText("历史平均: " + average + " 毫秒 (" + testCount + " 次)");
        } else {
            tvAverageResult.setText("历史平均: -- 毫秒 (0 次)");
        }
    }

    private void updateUI() {
        if (currentState == STATE_IDLE) {
            tvStatus.setText("点击 START 开始测试");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
            btnStart.setText("开始");
            btnStart.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}