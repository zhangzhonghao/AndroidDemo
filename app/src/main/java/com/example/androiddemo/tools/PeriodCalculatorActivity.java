package com.example.androiddemo.tools;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PeriodCalculatorActivity extends AppCompatActivity {

    private Button btnSelectDate;
    private TextView tvOvulation;
    private TextView tvSafePeriodStart;
    private TextView tvSafePeriodEnd;
    private TextView tvNextPeriod;

    private Calendar lastPeriodDate = Calendar.getInstance();
    private int cycleLength = 28;
    private int periodLength = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period_calculator);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("安全期计算器");
        }
    }

    private void initViews() {
        btnSelectDate = findViewById(R.id.btn_select_date);
        tvOvulation = findViewById(R.id.tv_ovulation);
        tvSafePeriodStart = findViewById(R.id.tv_safe_period_start);
        tvSafePeriodEnd = findViewById(R.id.tv_safe_period_end);
        tvNextPeriod = findViewById(R.id.tv_next_period);

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        calculate();
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    lastPeriodDate.set(year, month, dayOfMonth);
                    calculate();
                },
                lastPeriodDate.get(Calendar.YEAR),
                lastPeriodDate.get(Calendar.MONTH),
                lastPeriodDate.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void calculate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 排卵日 = 下次月经前14天
        Calendar nextPeriod = (Calendar) lastPeriodDate.clone();
        nextPeriod.add(Calendar.DAY_OF_MONTH, cycleLength);

        Calendar ovulation = (Calendar) nextPeriod.clone();
        ovulation.add(Calendar.DAY_OF_MONTH, -14);

        // 安全期 (月经结束后1-7天, 排卵后8天到下次月经前)
        Calendar safePeriodStart = (Calendar) lastPeriodDate.clone();
        safePeriodStart.add(Calendar.DAY_OF_MONTH, periodLength + 1);

        Calendar safePeriodEnd = (Calendar) ovulation.clone();
        safePeriodEnd.add(Calendar.DAY_OF_MONTH, 8);

        tvOvulation.setText(sdf.format(ovulation.getTime()));
        tvSafePeriodStart.setText(sdf.format(safePeriodStart.getTime()));
        tvSafePeriodEnd.setText(sdf.format(safePeriodEnd.getTime()));
        tvNextPeriod.setText(sdf.format(nextPeriod.getTime()));
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