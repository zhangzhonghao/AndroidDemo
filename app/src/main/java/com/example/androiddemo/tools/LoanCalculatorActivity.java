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

public class LoanCalculatorActivity extends AppCompatActivity {

    private EditText etLoanAmount;
    private EditText etInterestRate;
    private EditText etYears;
    private TextView tvMonthlyPayment;
    private TextView tvTotalInterest;
    private TextView tvTotalPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_calculator);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("贷款计算器");
        }
    }

    private void initViews() {
        etLoanAmount = findViewById(R.id.et_loan_amount);
        etInterestRate = findViewById(R.id.et_interest_rate);
        etYears = findViewById(R.id.et_years);
        tvMonthlyPayment = findViewById(R.id.tv_monthly_payment);
        tvTotalInterest = findViewById(R.id.tv_total_interest);
        tvTotalPayment = findViewById(R.id.tv_total_payment);

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

        etLoanAmount.addTextChangedListener(watcher);
        etInterestRate.addTextChangedListener(watcher);
        etYears.addTextChangedListener(watcher);
    }

    private void calculate() {
        String amountStr = etLoanAmount.getText().toString();
        String rateStr = etInterestRate.getText().toString();
        String yearsStr = etYears.getText().toString();

        if (amountStr.isEmpty() || rateStr.isEmpty() || yearsStr.isEmpty()) {
            tvMonthlyPayment.setText("-- 元/月");
            tvTotalInterest.setText("-- 元");
            tvTotalPayment.setText("-- 元");
            return;
        }

        try {
            double loanAmount = Double.parseDouble(amountStr);
            double annualRate = Double.parseDouble(rateStr);
            int years = Integer.parseInt(yearsStr);

            if (loanAmount <= 0 || annualRate <= 0 || years <= 0) {
                return;
            }

            // 月利率
            double monthlyRate = annualRate / 100 / 12;
            // 还款月数
            int months = years * 12;

            // 等额本息还款计算
            // 每月还款额 = 贷款本金 × 月利率 × (1+月利率)^还款月数 / [(1+月利率)^还款月数-1]
            double monthlyPayment = loanAmount * monthlyRate * Math.pow(1 + monthlyRate, months)
                    / (Math.pow(1 + monthlyRate, months) - 1);

            double totalPayment = monthlyPayment * months;
            double totalInterest = totalPayment - loanAmount;

            tvMonthlyPayment.setText(String.format(Locale.getDefault(), "%.2f 元/月", monthlyPayment));
            tvTotalInterest.setText(String.format(Locale.getDefault(), "%.2f 元", totalInterest));
            tvTotalPayment.setText(String.format(Locale.getDefault(), "%.2f 元", totalPayment));

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