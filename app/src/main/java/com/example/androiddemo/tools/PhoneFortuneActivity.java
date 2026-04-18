package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Random;

public class PhoneFortuneActivity extends AppCompatActivity {

    private EditText etPhone;
    private TextView tvResult;
    private TextView tvAnalysis;
    private final String[] fortunes = {"大吉", "中吉", "小吉", "吉", "平", "凶"};
    private final String[] analyses = {
        "贵人运极佳，身边助力多多，运势亨通",
        "桃花运旺盛，感情方面将有美好际遇",
        "财运上升，意外之财降临",
        "事业顺利，工作上将有突破",
        "最近需注意健康，宜休养生息",
        "贵人相助，困境中自有转机"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_fortune);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("手机号码吉凶");
        }

        etPhone = findViewById(R.id.et_phone);
        tvResult = findViewById(R.id.tv_result);
        tvAnalysis = findViewById(R.id.tv_analysis);

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateFortune(s.toString());
            }
        });
    }

    private void calculateFortune(String phone) {
        if (phone.length() < 11) {
            tvResult.setText("");
            tvAnalysis.setText("请输入完整的11位手机号码");
            return;
        }

        // 使用手机号末位数字生成确定结果（模拟）
        int lastDigit = Character.getNumericValue(phone.charAt(phone.length() - 1));
        int index = lastDigit % fortunes.length;

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