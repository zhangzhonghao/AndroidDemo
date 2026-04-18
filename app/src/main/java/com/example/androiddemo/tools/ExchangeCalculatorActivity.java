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

public class ExchangeCalculatorActivity extends AppCompatActivity {
    private EditText etAmount;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private TextView tvResult;

    private static final double[] RATES = {
        1.0, 7.2, 6.9, 0.14, 0.13, 1.36
    };

    private static final String[] CURRENCIES = {
        "CNY", "USD", "EUR", "JPY", "KRW", "HKD"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exchange_calculator);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        tvResult = findViewById(R.id.tv_result);
    }

    private void setupListeners() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculate();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void calculate() {
        try {
            String amountStr = etAmount.getText().toString();
            if (amountStr.isEmpty()) {
                tvResult.setText("结果: 0");
                return;
            }
            double amount = Double.parseDouble(amountStr);
            double fromRate = RATES[spinnerFrom.getSelectedItemPosition()];
            double toRate = RATES[spinnerTo.getSelectedItemPosition()];
            double result = amount / fromRate * toRate;
            DecimalFormat df = new DecimalFormat("#,##0.00");
            tvResult.setText("结果: " + df.format(result));
        } catch (Exception e) {
            tvResult.setText("结果: 0");
        }
    }
}