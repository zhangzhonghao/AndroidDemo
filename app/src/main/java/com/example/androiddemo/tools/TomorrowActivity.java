package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TomorrowActivity extends AppCompatActivity {

    private TextView tvTomorrow;
    private TextView tvWeekday;
    private TextView tvDateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomorrow);

        tvTomorrow = findViewById(R.id.tv_tomorrow);
        tvWeekday = findViewById(R.id.tv_weekday);
        tvDateInfo = findViewById(R.id.tv_date_info);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("明天是几号");
        }

        calculateTomorrow();
    }

    private void calculateTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
        SimpleDateFormat weekdayFormat = new SimpleDateFormat("EEEE", Locale.CHINA);
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        tvTomorrow.setText(dateFormat.format(calendar.getTime()));
        tvWeekday.setText(weekdayFormat.format(calendar.getTime()));
        tvDateInfo.setText("日期: " + fullFormat.format(calendar.getTime()) + "\n"
                + "一年中的第 " + calendar.get(Calendar.DAY_OF_YEAR) + " 天\n"
                + "一周中的第 " + calendar.get(Calendar.DAY_OF_WEEK) + " 天");
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