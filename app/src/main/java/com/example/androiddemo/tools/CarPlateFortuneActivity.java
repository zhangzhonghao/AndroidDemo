package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class CarPlateFortuneActivity extends AppCompatActivity {

    private EditText etPlate;
    private TextView tvResult;
    private TextView tvAnalysis;
    private final String[] fortunes = {"大吉", "中吉", "小吉", "吉", "平", "凶"};
    private final String[] analyses = {
        "车牌号数理极佳，出行平安顺遂",
        "贵人运旺盛，旅途多有相助",
        "近期运势上升，出行顺利",
        "中规中矩，平安为本",
        "需谨慎驾驶，避免意外",
        "数理欠佳，宜多加小心"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_plate_fortune);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("车牌号吉凶");
        }

        etPlate = findViewById(R.id.et_plate);
        tvResult = findViewById(R.id.tv_result);
        tvAnalysis = findViewById(R.id.tv_analysis);

        etPlate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateFortune(s.toString().toUpperCase());
            }
        });
    }

    private void calculateFortune(String plate) {
        if (plate.isEmpty()) {
            tvResult.setText("");
            tvAnalysis.setText("请输入车牌号");
            return;
        }

        // 只计算字母和数字部分
        String cleanPlate = plate.replaceAll("[^A-Z0-9]", "");
        if (cleanPlate.length() < 5) {
            tvAnalysis.setText("请输入完整的车牌号");
            return;
        }

        // 使用车牌号计算确定结果
        int sum = 0;
        for (char c : cleanPlate.toCharArray()) {
            sum += (int) c;
        }
        int index = sum % fortunes.length;

        tvResult.setText(fortunes[index]);
        tvAnalysis.setText(analyses[index]);
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