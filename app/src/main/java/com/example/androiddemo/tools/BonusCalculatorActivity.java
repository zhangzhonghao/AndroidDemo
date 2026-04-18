package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.DecimalFormat;

public class BonusCalculatorActivity extends AppCompatActivity {
    private EditText etBonus;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus_calculator);
        etBonus = findViewById(R.id.et_bonus);
        tvResult = findViewById(R.id.tv_result);
    }

    public void calculate(View view) {
        String bonusStr = etBonus.getText().toString();
        if (TextUtils.isEmpty(bonusStr)) {
            tvResult.setText("请输入年终奖金额");
            return;
        }
        double bonus = Double.parseDouble(bonusStr);
        double tax = calculateBonusTax(bonus);
        double afterTax = bonus - tax;
        DecimalFormat df = new DecimalFormat("#,##0.00");
        tvResult.setText("年终奖: ¥" + df.format(bonus) + "\n个税: ¥" + df.format(tax) + "\n税后: ¥" + df.format(afterTax));
    }

    private double calculateBonusTax(double bonus) {
        double monthlyBonus = bonus / 12;
        double tax = 0;
        if (monthlyBonus <= 3000) {
            tax = bonus * 0.03;
        } else if (monthlyBonus <= 12000) {
            tax = bonus * 0.10 - 210;
        } else if (monthlyBonus <= 25000) {
            tax = bonus * 0.20 - 1410;
        } else if (monthlyBonus <= 35000) {
            tax = bonus * 0.25 - 2660;
        } else if (monthlyBonus <= 55000) {
            tax = bonus * 0.30 - 4410;
        } else if (monthlyBonus <= 80000) {
            tax = bonus * 0.35 - 7160;
        } else {
            tax = bonus * 0.45 - 15160;
        }
        return tax;
    }
}