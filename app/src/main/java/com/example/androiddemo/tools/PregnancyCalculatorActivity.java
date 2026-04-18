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

public class PregnancyCalculatorActivity extends AppCompatActivity {

    private Button btnSelectDate;
    private TextView tvDueDate;
    private TextView tvCurrentWeek;
    private TextView tvCurrentDay;
    private TextView tvDaysRemaining;

    private Calendar lastPeriodDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy_calculator);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("孕期计算器");
        }
    }

    private void initViews() {
        btnSelectDate = findViewById(R.id.btn_select_date);
        tvDueDate = findViewById(R.id.tv_due_date);
        tvCurrentWeek = findViewById(R.id.tv_current_week);
        tvCurrentDay = findViewById(R.id.tv_current_day);
        tvDaysRemaining = findViewById(R.id.tv_days_remaining);

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    lastPeriodDate.set(year, month, dayOfMonth);
                    calculatePregnancy();
                },
                lastPeriodDate.get(Calendar.YEAR),
                lastPeriodDate.get(Calendar.MONTH),
                lastPeriodDate.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void calculatePregnancy() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // 预产期 = 末次月经 + 280天
        Calendar dueDate = (Calendar) lastPeriodDate.clone();
        dueDate.add(Calendar.DAY_OF_MONTH, 280);

        tvDueDate.setText(sdf.format(dueDate.getTime()));

        // 计算孕周
        Calendar today = Calendar.getInstance();
        long diffMillis = today.getTimeInMillis() - lastPeriodDate.getTimeInMillis();
        int daysPassed = (int) (diffMillis / (1000 * 60 * 60 * 24));

        int weeks = daysPassed / 7;
        int days = daysPassed % 7;

        tvCurrentWeek.setText(weeks + " 周");
        tvCurrentDay.setText(days + " 天");

        // 剩余天数
        int daysRemaining = 280 - daysPassed;
        if (daysRemaining < 0) {
            daysRemaining = 0;
        }
        tvDaysRemaining.setText(daysRemaining + " 天");
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