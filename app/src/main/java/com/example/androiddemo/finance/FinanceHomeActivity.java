package com.example.androiddemo.finance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.AccountBookActivity;
import com.example.androiddemo.tools.TaxCalculatorActivity;
import com.example.androiddemo.tools.RelativeCalculatorActivity;
import com.example.androiddemo.tools.ExchangeRateActivity;
import com.example.androiddemo.tools.FuelCalculatorActivity;

/**
 * 财务首页
 * 包含：记账、个税计算、亲戚计算、汇率查询、油耗计算等财务功能
 */
public class FinanceHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_account_book) {
            intent = new Intent(this, AccountBookActivity.class);
        } else if (id == R.id.btn_tax_calculator) {
            intent = new Intent(this, TaxCalculatorActivity.class);
        } else if (id == R.id.btn_relative_calculator) {
            intent = new Intent(this, RelativeCalculatorActivity.class);
        } else if (id == R.id.btn_exchange_rate) {
            intent = new Intent(this, ExchangeRateActivity.class);
        } else if (id == R.id.btn_fuel_calculator) {
            intent = new Intent(this, FuelCalculatorActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
