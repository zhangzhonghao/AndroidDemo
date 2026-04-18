package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HolidayQueryActivity extends AppCompatActivity {
    private CalendarView cvCalendar;
    private TextView tvHoliday, tvResult;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Set<String> holidays = new HashSet<>(Arrays.asList(
            "2026-01-01", "2026-01-28", "2026-01-29", "2026-01-30", "2026-01-31", "2026-02-01", "2026-02-02",
            "2026-02-03", "2026-02-04", "2026-02-05", "2026-02-06", "2026-02-07", "2026-02-08", "2026-02-09",
            "2026-04-04", "2026-04-05", "2026-04-06",
            "2026-05-01", "2026-05-02", "2026-05-03",
            "2026-06-01", "2026-06-07", "2026-06-08", "2026-06-09",
            "2026-09-15", "2026-09-16", "2026-09-17", "2026-09-18",
            "2026-10-01", "2026-10-02", "2026-10-03", "2026-10-04", "2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08"
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_query);
        cvCalendar = findViewById(R.id.cv_calendar);
        tvHoliday = findViewById(R.id.tv_holiday);
        tvResult = findViewById(R.id.tv_result);
        tvHoliday.setText("2026年放假安排:\n元旦: 1月1日\n春节: 1月28日-2月8日\n清明: 4月4日-6日\n劳动节: 5月1日-3日\n端午节: 6月1日-9日\n中秋节: 9月15日-18日\n国庆节: 10月1日-8日");
        cvCalendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            checkHoliday(date);
        });
    }

    private void checkHoliday(String date) {
        if (holidays.contains(date)) {
            tvResult.setText(date + " 是节假日！");
        } else {
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(5, 7)) - 1, Integer.parseInt(date.substring(8, 10)));
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            String weekDay = dayOfWeek == 1 ? "星期日" : dayOfWeek == 2 ? "星期一" : dayOfWeek == 3 ? "星期二" : dayOfWeek == 4 ? "星期三" : dayOfWeek == 5 ? "星期四" : dayOfWeek == 6 ? "星期五" : "星期六";
            tvResult.setText(date + " 是" + weekDay + "\n不是节假日");
        }
    }
}