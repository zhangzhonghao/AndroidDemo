package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class StressTestActivity extends AppCompatActivity {

    private TextView tvTimer;
    private TextView tvTarget;
    private TextView tvScore;
    private Button btnStart;
    private int seconds = 0;
    private boolean isRunning = false;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_test);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("压力测试");
        }

        tvTimer = findViewById(R.id.tv_timer);
        tvTarget = findViewById(R.id.tv_target);
        tvScore = findViewById(R.id.tv_score);
        btnStart = findViewById(R.id.btn_start);

        btnStart.setOnClickListener(v -> startTest());
        findViewById(R.id.btn_stop).setOnClickListener(v -> stopTest());
    }

    private void startTest() {
        isRunning = true;
        seconds = 0;
        btnStart.setEnabled(false);
        updateTimer();
        generateTarget();
    }

    private void stopTest() {
        isRunning = false;
        btnStart.setEnabled(true);

        int score = Math.max(0, 100 - seconds * 2);
        String level;
        if (score >= 80) level = "较低";
        else if (score >= 60) level = "中等";
        else if (score >= 40) level = "较高";
        else level = "过高";

        tvScore.setText("压力指数：" + score + "/100\n压力等级：" + level +
                       "\n\n正常范围：40-60\n建议：保持良好心态，适当放松");
    }

    private void updateTimer() {
        if (isRunning) {
            tvTimer.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
            handler.postDelayed(() -> {
                seconds++;
                updateTimer();
            }, 1000);
        }
    }

    private void generateTarget() {
        Random random = new Random();
        int target = random.nextInt(100) + 1;
        tvTarget.setText("请在心理默默数到 " + target + "，然后点击停止");
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}