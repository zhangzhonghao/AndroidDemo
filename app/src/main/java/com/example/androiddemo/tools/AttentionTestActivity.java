package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AttentionTestActivity extends AppCompatActivity {

    private TextView tvInstruction;
    private TextView tvResult;
    private List<View> circles = new ArrayList<>();
    private int correctCount = 0;
    private int totalCount = 0;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attention_test);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("注意力测试");
        }

        tvInstruction = findViewById(R.id.tv_instruction);
        tvResult = findViewById(R.id.tv_result);

        findViewById(R.id.btn_start).setOnClickListener(v -> startTest());
        findViewById(R.id.btn_red).setOnClickListener(v -> checkAnswer(true));
        findViewById(R.id.btn_blue).setOnClickListener(v -> checkAnswer(false));
    }

    private void startTest() {
        correctCount = 0;
        totalCount = 0;
        tvResult.setText("测试进行中...\n请在看到红色圆圈时点击红色按钮，蓝色时点击蓝色按钮");
        showNextCircle();
    }

    private void showNextCircle() {
        totalCount++;
        if (totalCount > 10) {
            showResult();
            return;
        }

        boolean showRed = random.nextBoolean();
        int color = showRed ? 0xFFF44336 : 0xFF2196F3;
        String text = showRed ? "红色" : "蓝色";

        tvInstruction.setText("第 " + totalCount + "/10 题\n\n请判断：" + text);
        tvInstruction.setTextColor(color);
    }

    private void checkAnswer(boolean choseRed) {
        boolean isRed = tvInstruction.getText().toString().contains("红色");
        if (isRed == choseRed) {
            correctCount++;
        }
        showNextCircle();
    }

    private void showResult() {
        int score = correctCount * 10;
        String level;
        if (score >= 90) level = "优秀";
        else if (score >= 70) level = "良好";
        else if (score >= 50) level = "一般";
        else level = "需加强";

        tvResult.setText("测试完成！\n\n正确率：" + correctCount + "/10\n得分：" + score + "\n注意力等级：" + level);
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