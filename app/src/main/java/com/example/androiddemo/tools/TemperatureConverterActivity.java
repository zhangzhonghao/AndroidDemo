package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class TemperatureConverterActivity extends AppCompatActivity {

    private EditText etInput;
    private RadioGroup rgFromUnit;
    private TextView tvCelsius;
    private TextView tvFahrenheit;
    private TextView tvKelvin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_converter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("温度单位换算");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        rgFromUnit = findViewById(R.id.rg_from_unit);
        tvCelsius = findViewById(R.id.tv_celsius);
        tvFahrenheit = findViewById(R.id.tv_fahrenheit);
        tvKelvin = findViewById(R.id.tv_kelvin);
    }

    private void setupListeners() {
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                convert();
            }
        });

        rgFromUnit.setOnCheckedChangeListener((group, checkedId) -> convert());
    }

    private void convert() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            resetDisplay();
            return;
        }

        try {
            double value = Double.parseDouble(input);
            int checkedId = rgFromUnit.getCheckedRadioButtonId();

            double celsius, fahrenheit, kelvin;

            if (checkedId == R.id.rb_celsius) {
                celsius = value;
                fahrenheit = celsius * 9 / 5 + 32;
                kelvin = celsius + 273.15;
            } else if (checkedId == R.id.rb_fahrenheit) {
                fahrenheit = value;
                celsius = (fahrenheit - 32) * 5 / 9;
                kelvin = celsius + 273.15;
            } else {
                kelvin = value;
                celsius = kelvin - 273.15;
                fahrenheit = celsius * 9 / 5 + 32;
            }

            tvCelsius.setText(String.format("%.2f °C", celsius));
            tvFahrenheit.setText(String.format("%.2f °F", fahrenheit));
            tvKelvin.setText(String.format("%.2f K", kelvin));
        } catch (NumberFormatException e) {
            resetDisplay();
        }
    }

    private void resetDisplay() {
        tvCelsius.setText("-- °C");
        tvFahrenheit.setText("-- °F");
        tvKelvin.setText("-- K");
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