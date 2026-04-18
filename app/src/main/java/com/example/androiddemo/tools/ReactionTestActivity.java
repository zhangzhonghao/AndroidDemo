package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReactionTestActivity extends AppCompatActivity {

    private TextView tvMessage;
    private TextView tvResult;
    private Button btnReaction;
    private List<Long> reactionTimes = new ArrayList<>();
    private long startTime = 0;
    private boolean waiting = false;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reaction_test);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("反应速度测试");
        }

        tvMessage = findViewById(R.id.tv_message);
        tvResult = findViewById(R.id.tv_result);
        btnReaction = findViewById(R.id.btn_reaction);

        btnReaction.setOnClickListener(v -> onReactionClick());
        findViewById(R.id.btn_reset).setOnClickListener(v -> resetTest());
    }

    private void onReactionClick() {
        if (!waiting) {
            // 提前点击
            tvMessage.setText("太早了！等待变绿时再点击");
            waiting = false;
            return;
        }

        long reactionTime = System.currentTimeMillis() - startTime;
        reactionTimes.add(reactionTime);
        waiting = false;

        if (reactionTimes.size() >= 5) {
            showFinalResult();
        } else {
            tvMessage.setText("反应时间：" + reactionTime + "ms\n第 " + reactionTimes.size() + "/5 次测试");
            tvResult.setText("请等待变绿...");
            handler.postDelayed(this::turnGreen, 1000 + (long)(Math.random() * 3000));
        }
    }

    private void resetTest() {
        reactionTimes.clear();
        waiting = false;
        tvMessage.setText("点击开始测试\n当屏幕变绿时尽快点击按钮");
        tvResult.setText("");
        btnReaction.setText("开始测试");
    }

    private void turnGreen() {
        startTime = System.currentTimeMillis();
        waiting = true;
        tvMessage.setText("点！");
        tvMessage.setBackgroundColor(0xFF4CAF50);
        btnReaction.setText("点击！");
    }

    private void showFinalResult() {
        long sum = 0;
        for (long t : reactionTimes) sum += t;
        long avg = sum / reactionTimes.size();

        long min = Collections.max(reactionTimes);
        long max = Collections.min(reactionTimes);

        String level;
        if (avg < 200) level = "极快（天才级）";
        else if (avg < 300) level = "很快";
        else if (avg < 400) level = "较快";
        else if (avg < 500) level = "一般";
        else level = "较慢（需练习）";

        tvResult.setText("平均反应时间：" + avg + "ms\n最快：" + min + "ms\n最慢：" + max + "ms\n\n等级：" + level);
        btnReaction.setText("重新测试");
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
