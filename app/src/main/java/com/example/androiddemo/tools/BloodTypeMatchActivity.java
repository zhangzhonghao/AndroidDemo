package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class BloodTypeMatchActivity extends AppCompatActivity {

    private RadioGroup rgBloodType1;
    private RadioGroup rgBloodType2;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_type_match);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("血型配对指数");
        }

        rgBloodType1 = findViewById(R.id.rg_blood_type_1);
        rgBloodType2 = findViewById(R.id.rg_blood_type_2);
        tvResult = findViewById(R.id.tv_result);

        rgBloodType1.setOnCheckedChangeListener((group, checkedId) -> calculateMatch());
        rgBloodType2.setOnCheckedChangeListener((group, checkedId) -> calculateMatch());

        calculateMatch();
    }

    private void calculateMatch() {
        String bt1 = getSelectedBloodType(rgBloodType1);
        String bt2 = getSelectedBloodType(rgBloodType2);

        if (bt1.isEmpty() || bt2.isEmpty()) {
            tvResult.setText("请选择双方血型");
            return;
        }

        // 血型配对兼容性表
        int[][] compatibility = {
            {100, 85, 75, 70, 80, 70, 75, 65},  // A
            {85, 100, 70, 75, 65, 80, 70, 75},  // B
            {75, 70, 100, 85, 75, 70, 80, 65},  // O
            {70, 75, 85, 100, 65, 75, 70, 80},   // AB
            {80, 65, 75, 65, 100, 85, 75, 70},  // A
            {70, 80, 70, 75, 85, 100, 65, 75},   // B
            {75, 70, 80, 70, 75, 65, 100, 85},  // O
            {65, 75, 65, 80, 70, 75, 85, 100}   // AB
        };

        String[] btList = {"A", "B", "O", "AB"};
        int i1 = 0, i2 = 0;
        for (int i = 0; i < btList.length; i++) {
            if (btList[i].equals(bt1)) i1 = i;
            if (btList[i].equals(bt2)) i2 = i;
        }
        if (bt1.length() > 1) i1 = 3;
        if (bt2.length() > 1) i2 = 3;

        int matchPercent = compatibility[i1][i2];

        String[] comments = {
            "相配指数较低，需多了解对方",
            "需要双方共同努力经营感情",
            "兼容性一般，细水长流型",
            "兼容性良好，相处融洽",
            "非常相配，灵魂伴侣"
        };
        int commentIndex = matchPercent / 20;

        String result = "血型配对：" + bt1 + " × " + bt2 + "\n\n" +
                       "相配指数：" + matchPercent + "%\n\n" +
                       "分析：\n" + comments[commentIndex];

        tvResult.setText(result);
    }

    private String getSelectedBloodType(RadioGroup rg) {
        int checkedId = rg.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_a) return "A";
        if (checkedId == R.id.rb_b) return "B";
        if (checkedId == R.id.rb_o) return "O";
        if (checkedId == R.id.rb_ab) return "AB";
        return "";
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