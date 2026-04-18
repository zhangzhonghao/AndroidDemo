package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Locale;

public class CalorieCalculatorActivity extends AppCompatActivity {

    private EditText etAge;
    private EditText etHeight;
    private EditText etWeight;
    private RadioGroup rgGender;
    private RadioGroup rgActivity;
    private TextView tvBmr;
    private TextView tvBmi;
    private TextView tvRecommend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_calculator);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("卡路里计算器");
        }
    }

    private void initViews() {
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        rgGender = findViewById(R.id.rg_gender);
        rgActivity = findViewById(R.id.rg_activity);
        tvBmr = findViewById(R.id.tv_bmr);
        tvBmi = findViewById(R.id.tv_bmi);
        tvRecommend = findViewById(R.id.tv_recommend);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculate();
            }
        };

        etAge.addTextChangedListener(watcher);
        etHeight.addTextChangedListener(watcher);
        etWeight.addTextChangedListener(watcher);
        rgGender.setOnCheckedChangeListener((group, checkedId) -> calculate());
        rgActivity.setOnCheckedChangeListener((group, checkedId) -> calculate());
    }

    private void calculate() {
        String ageStr = etAge.getText().toString();
        String heightStr = etHeight.getText().toString();
        String weightStr = etWeight.getText().toString();

        if (ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            tvBmr.setText("-- kcal/天");
            tvBmi.setText("--");
            tvRecommend.setText("-- kcal/天");
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);

            if (age <= 0 || height <= 0 || weight <= 0) {
                return;
            }

            // 计算BMI
            double heightM = height / 100;
            double bmi = weight / (heightM * heightM);
            String bmiCategory;
            if (bmi < 18.5) {
                bmiCategory = "偏瘦";
            } else if (bmi < 24) {
                bmiCategory = "正常";
            } else if (bmi < 28) {
                bmiCategory = "超重";
            } else {
                bmiCategory = "肥胖";
            }

            // 计算BMR (基础代谢率)
            // 男性: BMR = 88.362 + (13.397 × 体重kg) + (4.799 × 身高cm) - (5.677 × 年龄)
            // 女性: BMR = 447.593 + (9.247 × 体重kg) + (3.098 × 身高cm) - (4.330 × 年龄)
            boolean isMale = rgGender.getCheckedRadioButtonId() == R.id.rb_male;
            double bmr;
            if (isMale) {
                bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
            } else {
                bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            }

            // 活动系数
            double activityFactor = 1.2; // 默认久坐
            int activityId = rgActivity.getCheckedRadioButtonId();
            if (activityId == R.id.rb_light) {
                activityFactor = 1.375;
            } else if (activityId == R.id.rb_moderate) {
                activityFactor = 1.55;
            } else if (activityId == R.id.rb_active) {
                activityFactor = 1.725;
            } else if (activityId == R.id.rb_very_active) {
                activityFactor = 1.9;
            }

            double dailyCalorie = bmr * activityFactor;

            tvBmr.setText(String.format(Locale.getDefault(), "%.0f kcal/天", bmr));
            tvBmi.setText(String.format(Locale.getDefault(), "%.1f (%s)", bmi, bmiCategory));
            tvRecommend.setText(String.format(Locale.getDefault(), "%.0f kcal/天", dailyCalorie));

        } catch (NumberFormatException e) {
            // ignore
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}