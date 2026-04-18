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

public class FuelCalculatorActivity extends AppCompatActivity {

    private EditText etDistance;
    private EditText etFuelCost;
    private TextView tvFuelConsumption;
    private TextView tvCostPerHundred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_calculator);

        initViews();
        setupListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("油耗计算器");
        }
    }

    private void initViews() {
        etDistance = findViewById(R.id.et_distance);
        etFuelCost = findViewById(R.id.et_fuel_cost);
        tvFuelConsumption = findViewById(R.id.tv_fuel_consumption);
        tvCostPerHundred = findViewById(R.id.tv_cost_per_hundred);
    }

    private void setupListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateFuel();
            }
        };

        etDistance.addTextChangedListener(watcher);
        etFuelCost.addTextChangedListener(watcher);
    }

    private void calculateFuel() {
        String distanceStr = etDistance.getText().toString();
        String fuelCostStr = etFuelCost.getText().toString();

        if (distanceStr.isEmpty() || fuelCostStr.isEmpty()) {
            tvFuelConsumption.setText("-- L/100km");
            tvCostPerHundred.setText("-- 元/百公里");
            return;
        }

        try {
            double distance = Double.parseDouble(distanceStr);
            double fuelCost = Double.parseDouble(fuelCostStr);

            if (distance <= 0) {
                tvFuelConsumption.setText("-- L/100km");
                tvCostPerHundred.setText("-- 元/百公里");
                return;
            }

            // 油耗 = 油费 / 里程 * 100 (假设油价7元/升)
            double fuelConsumption = fuelCost / distance * 100;
            double costPerHundred = fuelCost / distance * 100;

            tvFuelConsumption.setText(String.format(Locale.getDefault(), "%.2f L/100km", fuelConsumption));
            tvCostPerHundred.setText(String.format(Locale.getDefault(), "%.2f 元/百公里", costPerHundred));
        } catch (NumberFormatException e) {
            tvFuelConsumption.setText("-- L/100km");
            tvCostPerHundred.setText("-- 元/百公里");
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