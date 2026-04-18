package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;

public class IdCardValidatorActivity extends AppCompatActivity {

    private EditText etIdCard;
    private TextView tvResult;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_card_validator);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("身份证号码验证");
        }

        etIdCard = findViewById(R.id.et_id_card);
        tvResult = findViewById(R.id.tv_result);
        tvInfo = findViewById(R.id.tv_info);

        etIdCard.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateIdCard(s.toString());
            }
        });
    }

    private void validateIdCard(String idCard) {
        if (idCard.isEmpty()) {
            tvResult.setText("");
            tvInfo.setText("");
            return;
        }

        if (idCard.length() != 18) {
            tvResult.setText("无效");
            tvResult.setTextColor(0xFFF44336);
            tvInfo.setText("身份证号码必须为18位");
            return;
        }

        // 验证校验位
        if (!checkVerifyCode(idCard)) {
            tvResult.setText("无效");
            tvResult.setTextColor(0xFFF44336);
            tvInfo.setText("校验位错误");
            return;
        }

        // 提取信息
        String region = idCard.substring(0, 6);
        String birthStr = idCard.substring(6, 14);
        String gender = (Integer.parseInt(idCard.substring(16, 17)) % 2 == 0) ? "女" : "男";

        StringBuilder info = new StringBuilder();
        info.append("出生日期：").append(birthStr.substring(0, 4)).append("-")
                .append(birthStr.substring(4, 6)).append("-").append(birthStr.substring(6, 8)).append("\n");
        info.append("性别：").append(gender).append("\n");
        info.append("地区代码：").append(region);

        tvResult.setText("有效");
        tvResult.setTextColor(0xFF4CAF50);
        tvInfo.setText(info.toString());
    }

    private boolean checkVerifyCode(String idCard) {
        int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] verify = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(idCard.charAt(i)) * weight[i];
        }

        char checkCode = verify[sum % 11];
        return checkCode == idCard.charAt(17) ||
               (checkCode == 'X' && idCard.charAt(17) == 'x');
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