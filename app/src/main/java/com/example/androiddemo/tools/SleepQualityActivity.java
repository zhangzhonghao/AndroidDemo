package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class SleepQualityActivity extends AppCompatActivity {

    private EditText etSleepHour;
    private EditText etWakeCount;
    private EditText etFeelLevel;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_quality);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("睡眠质量分析");
        }

        etSleepHour = findViewById(R.id.et_sleep_hour);
        etWakeCount = findViewById(R.id.et_wake_count);
        etFeelLevel = findViewById(R.id.et_feel_level);
        tvResult = findViewById(R.id.tv_result);

        findViewById(R.id.btn_analyze).setOnClickListener(v -> analyzeSleep());
    }

    private void analyzeSleep() {
        String sleepHourStr = etSleepHour.getText().toString();
        String wakeCountStr = etWakeCount.getText().toString();
        String feelLevelStr = etFeelLevel.getText().toString();

        if (sleepHourStr.isEmpty()) {
            tvResult.setText("请输入睡眠时长");
            return;
        }

        try {
            double sleepHour = Double.parseDouble(sleepHourStr);
            int wakeCount = wakeCountStr.isEmpty() ? 0 : Integer.parseInt(wakeCountStr);
            int feelLevel = feelLevelStr.isEmpty() ? 5 : Integer.parseInt(feelLevelStr);

            // 计算睡眠质量分数
            double score = 100;

            // 睡眠时长评分（最佳7-9小时）
            if (sleepHour < 6) {
                score -= (6 - sleepHour) * 10;
            } else if (sleepHour < 7) {
                score -= (7 - sleepHour) * 5;
            } else if (sleepHour > 9) {
                score -= (sleepHour - 9) * 5;
            }

            // 夜间醒来次数评分
            score -= wakeCount * 5;

            // 主观感受评分
            score -= (10 - feelLevel) * 3;

            score = Math.max(0, Math.min(100, score));

            String quality;
            String advice;
            if (score >= 80) {
                quality = "优秀";
                advice = "睡眠质量很好继续保持";
            } else if (score >= 60) {
                quality = "良好";
                advice = "睡眠质量尚可，建议保持规律作息";
            } else if (score >= 40) {
                quality = "一般";
                advice = "建议调整睡眠习惯，睡前少玩手机";
            } else {
                quality = "较差";
                advice = "建议咨询医生或改善睡眠环境";
            }

            String result = "睡眠时长：" + sleepHour + "小时\n" +
                           "夜间醒来：" + wakeCount + "次\n" +
                           "主观感受：" + feelLevel + "/10\n\n" +
                           "睡眠质量评分：" + score + "分\n" +
                           "质量等级：" + quality + "\n\n" +
                           "建议：" + advice;

            tvResult.setText(result);

        } catch (NumberFormatException e) {
            tvResult.setText("请输入有效数字");
        }
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