package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DayOfWeekActivity extends AppCompatActivity {
    private DatePicker dpDate;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_of_week);
        dpDate = findViewById(R.id.dp_date);
        tvResult = findViewById(R.id.tv_result);
        dpDate.init(2026, 3, 18, null);
    }

    public void query(View view) {
        int year = dpDate.getYear();
        int month = dpDate.getMonth();
        int day = dpDate.getDayOfMonth();
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfChinese = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        String weekDay = weekDays[dayOfWeek - 1];
        tvResult.setText(sdfChinese.format(c.getTime()) + "\n" + weekDay);
    }
}