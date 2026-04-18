package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class BinaryCalculatorActivity extends AppCompatActivity {
    private EditText etInput;
    private TextView tvBinary;
    private TextView tvOctal;
    private TextView tvDecimal;
    private TextView tvHexadecimal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binary_calculator);
        etInput = findViewById(R.id.et_number_input);
        tvBinary = findViewById(R.id.tv_binary);
        tvOctal = findViewById(R.id.tv_octal);
        tvDecimal = findViewById(R.id.tv_decimal);
        tvHexadecimal = findViewById(R.id.tv_hexadecimal);

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                convertNumber();
            }
        });
    }

    private void convertNumber() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            clearResults();
            return;
        }
        try {
            long decimal = Long.parseLong(input);
            tvBinary.setText("二进制：" + Long.toBinaryString(decimal));
            tvOctal.setText("八进制：" + Long.toOctalString(decimal));
            tvDecimal.setText("十进制：" + decimal);
            tvHexadecimal.setText("十六进制：" + Long.toHexString(decimal).toUpperCase());
        } catch (NumberFormatException e) {
            clearResults();
            tvDecimal.setText("请输入有效的数字");
        }
    }

    private void clearResults() {
        tvBinary.setText("二进制：");
        tvOctal.setText("八进制：");
        tvDecimal.setText("十进制：");
        tvHexadecimal.setText("十六进制：");
    }
}