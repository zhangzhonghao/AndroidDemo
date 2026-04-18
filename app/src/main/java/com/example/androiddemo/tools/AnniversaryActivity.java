package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AnniversaryActivity extends AppCompatActivity {

    private EditText etYear;
    private EditText etMonth;
    private EditText etDay;
    private TextView tvDaysCount;
    private TextView tvYearsCount;
    private TextView tvNextAnniversary;
    private TextView tvStartDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anniversary);

        etYear = findViewById(R.id.et_year);
        etMonth = findViewById(R.id.et_month);
        etDay = findViewById(R.id.et_day);
        tvDaysCount = findViewById(R.id.tv_days_count);
        tvYearsCount = findViewById(R.id.tv_years_count);
        tvNextAnniversary = findViewById(R.id.tv_next_anniversary);
        tvStartDate = findViewById(R.id.tv_start_date);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("结婚纪念日");
        }

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateAnniversary();
            }
        };

        etYear.addTextChangedListener(watcher);
        etMonth.addTextChangedListener(watcher);
        etDay.addTextChangedListener(watcher);
    }

    private void calculateAnniversary() {
        String yearStr = etYear.getText().toString();
        String monthStr = etMonth.getText().toString();
        String dayStr = etDay.getText().toString();

        if (yearStr.isEmpty() || monthStr.isEmpty() || dayStr.isEmpty()) {
            resetDisplay();
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int month = Integer.parseInt(monthStr);
            int day = Integer.parseInt(dayStr);

            Calendar startCal = Calendar.getInstance();
            startCal.set(year, month - 1, day);

            Calendar now = Calendar.getInstance();
            Date startDate = startCal.getTime();

            long diffMillis = now.getTimeInMillis() - startDate.getTime();
            long totalDays = diffMillis / (1000 * 60 * 60 * 24);

            int years = (int) (totalDays / 365);
            int remainingDays = (int) (totalDays % 365);

            Calendar nextCal = Calendar.getInstance();
            nextCal.setTime(startDate);
            while (nextCal.before(now) || nextCal.equals(now)) {
                nextCal.add(Calendar.YEAR, 1);
            }

            long daysToNext = (nextCal.getTimeInMillis() - now.getTimeInMillis()) / (1000 * 60 * 60 * 24);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);

            tvStartDate.setText("结婚日期: " + sdf.format(startDate));
            tvDaysCount.setText("已度过 " + totalDays + " 天");
            tvYearsCount.setText("第 " + years + " 年 (" + remainingDays + " 天)");
            tvNextAnniversary.setText("距离第 " + (years + 1) + " 周年还有 " + daysToNext + " 天");
        } catch (Exception e) {
            resetDisplay();
        }
    }

    private void resetDisplay() {
        tvDaysCount.setText("请输入结婚日期");
        tvYearsCount.setText("--");
        tvNextAnniversary.setText("--");
        tvStartDate.setText("--");
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}