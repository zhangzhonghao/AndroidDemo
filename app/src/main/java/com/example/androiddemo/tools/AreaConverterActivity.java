package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.DecimalFormat;

public class AreaConverterActivity extends AppCompatActivity {
    private EditText etInput;
    private Spinner spinnerFrom;
    private TextView tvResult;
    private TextView tvDetails;

    private static final double[] AREA_RATES = {
        1.0, 0.0001, 0.0015, 0.00025, 0.0001
    };

    private static final String[] AREA_UNITS = {
        "平方米", "平方千米", "亩", "英亩", "公顷"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_converter);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        spinnerFrom = findViewById(R.id.spinner_from);
        tvResult = findViewById(R.id.tv_result);
        tvDetails = findViewById(R.id.tv_details);
    }

    private void setupListeners() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                convert();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void convert() {
        try {
            String inputStr = etInput.getText().toString();
            if (inputStr.isEmpty()) {
                tvResult.setText("请输入数值");
                tvDetails.setText("");
                return;
            }
            double input = Double.parseDouble(inputStr);
            int fromIndex = spinnerFrom.getSelectedItemPosition();
            double fromRate = AREA_RATES[fromIndex];

            DecimalFormat df = new DecimalFormat("#,##0.0000");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < AREA_UNITS.length; i++) {
                if (i != fromIndex) {
                    double result = input / fromRate * AREA_RATES[i];
                    sb.append(AREA_UNITS[i]).append(": ").append(df.format(result)).append("\n");
                }
            }
            tvDetails.setText(sb.toString());
            tvResult.setText("结果");
        } catch (Exception e) {
            tvResult.setText("输入错误");
        }
    }
}