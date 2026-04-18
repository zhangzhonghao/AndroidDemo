package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class NumberToChineseActivity extends AppCompatActivity {

    private EditText etNumber;
    private TextView tvResult;
    private final String[] units = {"", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟", "兆"};
    private final String[] digits = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_to_chinese);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("数字金额转中文大写");
        }

        etNumber = findViewById(R.id.et_number);
        tvResult = findViewById(R.id.tv_result);

        etNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                convertToChinese(s.toString());
            }
        });
    }

    private void convertToChinese(String numberStr) {
        if (numberStr.isEmpty()) {
            tvResult.setText("");
            return;
        }

        try {
            double number = Double.parseDouble(numberStr);
            if (number >= 1000000000000.0) {
                tvResult.setText("金额超出范围（最大仟兆）");
                return;
            }

            String[] parts = String.format("%.2f", number).split("\\.");
            String integerPart = parts[0];
            String decimalPart = parts[1];

            StringBuilder result = new StringBuilder();
            int len = integerPart.length();

            for (int i = 0; i < len; i++) {
                int digit = Character.getNumericValue(integerPart.charAt(i));
                int unitIndex = len - i - 1;

                if (digit != 0) {
                    result.append(digits[digit]);
                    result.append(units[unitIndex]);
                } else if (result.length() > 0 && !result.toString().endsWith("零")) {
                    result.append("零");
                }
            }

            // 去除末尾的零
            String str = result.toString();
            while (str.endsWith("零")) {
                str = str.substring(0, str.length() - 1);
            }

            // 添加"元"
            if (!str.isEmpty()) {
                str += "元";
            }

            // 处理小数部分
            if (!decimalPart.equals("00")) {
                int jiao = Character.getNumericValue(decimalPart.charAt(0));
                int fen = Character.getNumericValue(decimalPart.charAt(1));
                if (jiao != 0) {
                    str += digits[jiao] + "角";
                }
                if (fen != 0) {
                    str += digits[fen] + "分";
                }
            } else {
                str += "整";
            }

            tvResult.setText(str);

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