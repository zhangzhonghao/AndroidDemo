package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class ColorBlindTestActivity extends AppCompatActivity {

    private TextView tvInstruction;
    private ImageView ivColorPlate;
    private RadioGroup rgAnswer;
    private TextView tvResult;
    private int currentPlate = 0;
    private int correctCount = 0;
    private List<int[]> colorPlates;
    private String[] answers = {"12", "8", "29", "5", "3", "15", "74", "6", "45", "97"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_blind_test);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("色盲测试");
        }

        tvInstruction = findViewById(R.id.tv_instruction);
        ivColorPlate = findViewById(R.id.iv_color_plate);
        rgAnswer = findViewById(R.id.rg_answer);
        tvResult = findViewById(R.id.tv_result);

        initColorPlates();

        findViewById(R.id.btn_next).setOnClickListener(v -> nextPlate());
        findViewById(R.id.btn_start).setOnClickListener(v -> startTest());
    }

    private void initColorPlates() {
        colorPlates = new ArrayList<>();
        colorPlates.add(new int[]{0xFF728FCE, 0xFFFFFFFF, 0xFF000000}); // 12
        colorPlates.add(new int[]{0xFFA52A2A, 0xFF00FF00, 0xFFFF0000}); // 8
        colorPlates.add(new int[]{0xFF00FF00, 0xFF0000FF, 0xFFFF00FF}); // 29
        colorPlates.add(new int[]{0xFFFFD700, 0xFF000000, 0xFF00FF00}); // 5
        colorPlates.add(new int[]{0xFF00FF00, 0xFFFF0000, 0xFF0000FF}); // 3
        colorPlates.add(new int[]{0xFF0000FF, 0xFFFF0000, 0xFF00FF00}); // 15
        colorPlates.add(new int[]{0xFFFF0000, 0xFF00FF00, 0xFF0000FF}); // 74
        colorPlates.add(new int[]{0xFF00FF00, 0xFFFF00FF, 0xFF00FFFF}); // 6
        colorPlates.add(new int[]{0xFFFF00FF, 0xFF00FF00, 0xFF0000FF}); // 45
        colorPlates.add(new int[]{0xFF00FFFF, 0xFFFF0000, 0xFF00FF00}); // 97
    }

    private void startTest() {
        currentPlate = 0;
        correctCount = 0;
        showPlate();
    }

    private void showPlate() {
        if (currentPlate >= colorPlates.size()) {
            showResult();
            return;
        }

        tvInstruction.setText("第 " + (currentPlate + 1) + "/" + colorPlates.size() + " 题：请说出数字");
        rgAnswer.clearCheck();
        ivColorPlate.setBackgroundColor(colorPlates.get(currentPlate)[0]);
    }

    private void nextPlate() {
        int checkedId = rgAnswer.getCheckedRadioButtonId();
        String userAnswer = "";
        if (checkedId == R.id.rb_1) userAnswer = "1";
        else if (checkedId == R.id.rb_2) userAnswer = "2";
        else if (checkedId == R.id.rb_3) userAnswer = "3";
        else if (checkedId == R.id.rb_4) userAnswer = "4";
        else if (checkedId == R.id.rb_5) userAnswer = "5";
        else if (checkedId == R.id.rb_6) userAnswer = "6";
        else if (checkedId == R.id.rb_7) userAnswer = "7";
        else if (checkedId == R.id.rb_8) userAnswer = "8";
        else if (checkedId == R.id.rb_9) userAnswer = "9";
        else if (checkedId == R.id.rb_0) userAnswer = "0";

        if (userAnswer.equals(answers[currentPlate].substring(0, 1))) {
            correctCount++;
        }

        currentPlate++;
        showPlate();
    }

    private void showResult() {
        String result;
        if (correctCount >= 9) {
            result = "正常色觉";
        } else if (correctCount >= 6) {
            result = "轻度色弱，建议进一步检查";
        } else {
            result = "可能存在色觉异常，建议咨询专业医生";
        }

        tvResult.setText("正确率：" + correctCount + "/" + colorPlates.size() + "\n\n诊断结果：" + result);
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