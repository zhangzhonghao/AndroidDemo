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

public class LengthConverterActivity extends AppCompatActivity {

    private EditText etInput;
    private RadioGroup rgFromUnit;
    private TextView tvMeter;
    private TextView tvCentimeter;
    private TextView tvKilometer;
    private TextView tvInch;
    private TextView tvFoot;
    private TextView tvMile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_length_converter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("长度单位换算");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        rgFromUnit = findViewById(R.id.rg_from_unit);
        tvMeter = findViewById(R.id.tv_meter);
        tvCentimeter = findViewById(R.id.tv_centimeter);
        tvKilometer = findViewById(R.id.tv_kilometer);
        tvInch = findViewById(R.id.tv_inch);
        tvFoot = findViewById(R.id.tv_foot);
        tvMile = findViewById(R.id.tv_mile);
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

            double meter;

            if (checkedId == R.id.rb_meter) {
                meter = value;
            } else if (checkedId == R.id.rb_centimeter) {
                meter = value / 100;
            } else if (checkedId == R.id.rb_kilometer) {
                meter = value * 1000;
            } else if (checkedId == R.id.rb_inch) {
                meter = value * 0.0254;
            } else if (checkedId == R.id.rb_foot) {
                meter = value * 0.3048;
            } else {
                meter = value * 1609.344;
            }

            tvMeter.setText(String.format("%.4f m", meter));
            tvCentimeter.setText(String.format("%.2f cm", meter * 100));
            tvKilometer.setText(String.format("%.4f km", meter / 1000));
            tvInch.setText(String.format("%.2f in", meter / 0.0254));
            tvFoot.setText(String.format("%.4f ft", meter / 0.3048));
            tvMile.setText(String.format("%.4f mi", meter / 1609.344));
        } catch (NumberFormatException e) {
            resetDisplay();
        }
    }

    private void resetDisplay() {
        tvMeter.setText("-- m");
        tvCentimeter.setText("-- cm");
        tvKilometer.setText("-- km");
        tvInch.setText("-- in");
        tvFoot.setText("-- ft");
        tvMile.setText("-- mi");
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