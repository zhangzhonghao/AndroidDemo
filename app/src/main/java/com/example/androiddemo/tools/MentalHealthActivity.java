package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class MentalHealthActivity extends AppCompatActivity {

    private TextView tvScore;
    private EditText etQ1, etQ2, etQ3, etQ4, etQ5;
    private int[] questionScores = {0, 0, 0, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mental_health);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("心理健康测试");
        }

        tvScore = findViewById(R.id.tv_score);
        etQ1 = findViewById(R.id.et_q1);
        etQ2 = findViewById(R.id.et_q2);
        etQ3 = findViewById(R.id.et_q3);
        etQ4 = findViewById(R.id.et_q4);
        etQ5 = findViewById(R.id.et_q5);

        findViewById(R.id.btn_calculate).setOnClickListener(v -> calculateScore());
    }

    private void calculateScore() {
        try {
            int score = 0;
            for (int i = 1; i <= 5; i++) {
                EditText et = (EditText) findViewById(getResources().getIdentifier("et_q" + i, "id", getPackageName()));
                if (et != null && !et.getText().toString().isEmpty()) {
                    int qScore = Integer.parseInt(et.getText().toString());
                    qScore = Math.min(5, Math.max(0, qScore));
                    score += qScore;
                }
            }

            int totalScore = score * 4; // 换算成100分制

            String level;
            String advice;
            if (totalScore >= 80) {
                level = "心理健康状态良好";
                advice = "继续保持积极乐观的心态";
            } else if (totalScore >= 60) {
                level = "轻度压力";
                advice = "建议适当放松，学会倾诉";
            } else if (totalScore >= 40) {
                level = "中度压力";
                advice = "建议寻求心理疏导，关注自我调节";
            } else {
                level = "需要关注";
                advice = "建议咨询专业心理医生";
            }

            tvScore.setText("心理健康评估\n\n得分：" + totalScore + "/100\n\n等级：" + level +
                           "\n\n建议：" + advice);

        } catch (NumberFormatException e) {
            tvScore.setText("请输入0-5的数字评分");
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