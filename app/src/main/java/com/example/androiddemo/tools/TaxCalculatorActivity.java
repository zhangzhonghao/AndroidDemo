package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.DecimalFormat;

public class TaxCalculatorActivity extends AppCompatActivity {
    private EditText etSalary;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax_calculator);
        etSalary = findViewById(R.id.et_salary);
        tvResult = findViewById(R.id.tv_result);
    }

    public void calculate(View view) {
        String salaryStr = etSalary.getText().toString();
        if (TextUtils.isEmpty(salaryStr)) {
            tvResult.setText("请输入工资");
            return;
        }
        double salary = Double.parseDouble(salaryStr);
        double tax = calculateTax(salary);
        double afterTax = salary - tax;
        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvResult.setText("个税: ¥" + df.format(tax) + "\n税后: ¥" + df.format(afterTax));
    }

    private double calculateTax(double salary) {
        double taxableIncome = salary - 5000;
        if (taxableIncome <= 0) return 0;
        double tax = 0;
        if (taxableIncome <= 36000) {
            tax = taxableIncome * 0.03;
        } else if (taxableIncome <= 144000) {
            tax = taxableIncome * 0.10 - 2520;
        } else if (taxableIncome <= 300000) {
            tax = taxableIncome * 0.20 - 16920;
        } else if (taxableIncome <= 420000) {
            tax = taxableIncome * 0.25 - 31920;
        } else if (taxableIncome <= 660000) {
            tax = taxableIncome * 0.30 - 52920;
        } else if (taxableIncome <= 960000) {
            tax = taxableIncome * 0.35 - 85920;
        } else {
            tax = taxableIncome * 0.45 - 181920;
        }
        return tax;
    }
}