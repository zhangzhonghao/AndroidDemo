package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class AdvancedBaseConverterActivity extends AppCompatActivity {

    private EditText etInput;
    private Spinner spFromBase;
    private Spinner spToBase;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_base_converter);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("进制转换器");
        }
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        spFromBase = findViewById(R.id.sp_from_base);
        spToBase = findViewById(R.id.sp_to_base);
        tvResult = findViewById(R.id.tv_result);

        String[] bases = {"2 (二进制)", "8 (八进制)", "10 (十进制)", "16 (十六进制)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bases);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spFromBase.setAdapter(adapter);
        spToBase.setAdapter(adapter);
        spToBase.setSelection(3); // 默认转到16进制

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                convert();
            }
        });

        spFromBase.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convert();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spToBase.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                convert();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void convert() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            tvResult.setText("");
            return;
        }

        int fromBase = getBase(spFromBase.getSelectedItemPosition());
        int toBase = getBase(spToBase.getSelectedItemPosition());

        try {
            // 解析输入 (支持负数和小数)
            String number = input;
            boolean negative = false;
            if (number.startsWith("-")) {
                negative = true;
                number = number.substring(1);
            }

            String[] parts = number.split("\\.");
            String intPart = parts[0];
            String fracPart = parts.length > 1 ? parts[1] : "";

            // 转成十进制
            long intValue = Long.parseLong(intPart, fromBase);
            double result = intValue;

            // 处理小数部分
            if (!fracPart.isEmpty()) {
                for (int i = 0; i < fracPart.length(); i++) {
                    int digit = Character.digit(fracPart.charAt(i), fromBase);
                    result += digit / Math.pow(fromBase, i + 1);
                }
            }

            // 转成目标进制
            String resultStr;
            if (negative) {
                resultStr = "-";
            } else {
                resultStr = "";
            }

            if (toBase == 10) {
                resultStr += String.format(java.util.Locale.getDefault(), "%.10g", result);
            } else if (toBase == 2) {
                resultStr += Long.toBinaryString((long) result);
            } else if (toBase == 8) {
                resultStr += Long.toOctalString((long) result);
            } else if (toBase == 16) {
                resultStr += Long.toHexString((long) result).toUpperCase();
            }

            tvResult.setText(resultStr);

        } catch (NumberFormatException e) {
            tvResult.setText("无效的输入");
        }
    }

    private int getBase(int position) {
        switch (position) {
            case 0: return 2;
            case 1: return 8;
            case 2: return 10;
            case 3: return 16;
            default: return 10;
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