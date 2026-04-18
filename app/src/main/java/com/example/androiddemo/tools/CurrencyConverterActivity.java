package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.DecimalFormat;

public class CurrencyConverterActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private TextView tvResult;
    private TextView tvRate;

    // 常用汇率（以人民币为基准）
    private static final double[] RATES = {1.0, 6.9, 0.92, 150.5, 1350.0, 0.79, 1.36, 8.2};
    private static final String[] CURRENCIES = {"CNY 人民币", "USD 美元", "EUR 欧元", "JPY 日元", "KRW 韩元", "GBP 英镑", "AUD 澳元", "HKD 港币"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        spinnerFrom = findViewById(R.id.spinner_from);
        spinnerTo = findViewById(R.id.spinner_to);
        tvResult = findViewById(R.id.tv_result);
        tvRate = findViewById(R.id.tv_rate);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("货币换算器");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CURRENCIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
        spinnerTo.setSelection(1); // 默认选USD
    }

    private void setupListeners() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                convert();
            }
        });

        spinnerFrom.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                convert();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerTo.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                convert();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void convert() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            tvResult.setText("0.00");
            tvRate.setText("请输入金额");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            int fromIndex = spinnerFrom.getSelectedItemPosition();
            int toIndex = spinnerTo.getSelectedItemPosition();

            double fromRate = RATES[fromIndex];
            double toRate = RATES[toIndex];

            // 转换为人民币再转换为目标货币
            double result = (amount / fromRate) * toRate;

            DecimalFormat df = new DecimalFormat("#,##0.00");
            tvResult.setText(df.format(result) + " " + CURRENCIES[toIndex].split(" ")[0]);

            double exchangeRate = toRate / fromRate;
            tvRate.setText("1 " + CURRENCIES[fromIndex].split(" ")[0] + " = " +
                    new DecimalFormat("#,##0.####").format(exchangeRate) + " " +
                    CURRENCIES[toIndex].split(" ")[0]);
        } catch (NumberFormatException e) {
            tvResult.setText("无效金额");
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