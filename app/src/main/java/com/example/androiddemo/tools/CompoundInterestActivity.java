package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Locale;

public class CompoundInterestActivity extends AppCompatActivity {

    private EditText etPrincipal;
    private EditText etRate;
    private EditText etTime;
    private EditText etCompoundFreq;
    private TextView tvFutureValue;
    private TextView tvInterest;
    private TextView tvEffectiveRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compound_interest);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("复利计算器");
        }
    }

    private void initViews() {
        etPrincipal = findViewById(R.id.et_principal);
        etRate = findViewById(R.id.et_rate);
        etTime = findViewById(R.id.et_time);
        etCompoundFreq = findViewById(R.id.et_compound_freq);
        tvFutureValue = findViewById(R.id.tv_future_value);
        tvInterest = findViewById(R.id.tv_interest);
        tvEffectiveRate = findViewById(R.id.tv_effective_rate);

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

        etPrincipal.addTextChangedListener(watcher);
        etRate.addTextChangedListener(watcher);
        etTime.addTextChangedListener(watcher);
        etCompoundFreq.addTextChangedListener(watcher);
    }

    private void calculate() {
        String principalStr = etPrincipal.getText().toString();
        String rateStr = etRate.getText().toString();
        String timeStr = etTime.getText().toString();
        String freqStr = etCompoundFreq.getText().toString();

        if (principalStr.isEmpty() || rateStr.isEmpty() || timeStr.isEmpty() || freqStr.isEmpty()) {
            tvFutureValue.setText("-- 元");
            tvInterest.setText("-- 元");
            tvEffectiveRate.setText("-- %");
            return;
        }

        try {
            double principal = Double.parseDouble(principalStr);
            double annualRate = Double.parseDouble(rateStr);
            double years = Double.parseDouble(timeStr);
            int freq = Integer.parseInt(freqStr);

            if (principal <= 0 || annualRate <= 0 || years <= 0 || freq <= 0) {
                return;
            }

            // 复利计算: A = P(1 + r/n)^(nt)
            double rate = annualRate / 100;
            double futureValue = principal * Math.pow(1 + rate / freq, freq * years);
            double interest = futureValue - principal;

            // 有效年利率
            double effectiveRate = (Math.pow(1 + rate / freq, freq) - 1) * 100;

            tvFutureValue.setText(String.format(Locale.getDefault(), "%.2f 元", futureValue));
            tvInterest.setText(String.format(Locale.getDefault(), "%.2f 元", interest));
            tvEffectiveRate.setText(String.format(Locale.getDefault(), "%.2f %%", effectiveRate));

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