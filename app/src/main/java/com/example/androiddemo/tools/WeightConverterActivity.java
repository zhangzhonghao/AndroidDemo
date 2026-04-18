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

public class WeightConverterActivity extends AppCompatActivity {

    private EditText etInput;
    private RadioGroup rgFromUnit;
    private TextView tvKilogram;
    private TextView tvGram;
    private TextView tvMilligram;
    private TextView tvPound;
    private TextView tvOunce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight_converter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("重量单位换算");
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        rgFromUnit = findViewById(R.id.rg_from_unit);
        tvKilogram = findViewById(R.id.tv_kilogram);
        tvGram = findViewById(R.id.tv_gram);
        tvMilligram = findViewById(R.id.tv_milligram);
        tvPound = findViewById(R.id.tv_pound);
        tvOunce = findViewById(R.id.tv_ounce);
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

            double kilogram;

            if (checkedId == R.id.rb_kilogram) {
                kilogram = value;
            } else if (checkedId == R.id.rb_gram) {
                kilogram = value / 1000;
            } else if (checkedId == R.id.rb_milligram) {
                kilogram = value / 1000000;
            } else if (checkedId == R.id.rb_pound) {
                kilogram = value * 0.453592;
            } else {
                kilogram = value * 0.0283495;
            }

            tvKilogram.setText(String.format("%.4f kg", kilogram));
            tvGram.setText(String.format("%.2f g", kilogram * 1000));
            tvMilligram.setText(String.format("%.2f mg", kilogram * 1000000));
            tvPound.setText(String.format("%.4f lb", kilogram / 0.453592));
            tvOunce.setText(String.format("%.4f oz", kilogram / 0.0283495));
        } catch (NumberFormatException e) {
            resetDisplay();
        }
    }

    private void resetDisplay() {
        tvKilogram.setText("-- kg");
        tvGram.setText("-- g");
        tvMilligram.setText("-- mg");
        tvPound.setText("-- lb");
        tvOunce.setText("-- oz");
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