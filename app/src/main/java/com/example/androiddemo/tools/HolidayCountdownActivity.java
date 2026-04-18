package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HolidayCountdownActivity extends AppCompatActivity {

    private LinearLayout layoutHolidays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_holiday_countdown);

        layoutHolidays = findViewById(R.id.layout_holidays);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("节日倒计时");
        }

        displayHolidays();
    }

    private void displayHolidays() {
        layoutHolidays.removeAllViews();

        String[][] holidays = {
                {"元旦", "01-01"},
                {"春节", "01-01"},
                {"元宵节", "01-15"},
                {"妇女节", "03-08"},
                {"清明节", "04-05"},
                {"劳动节", "05-01"},
                {"端午节", "05-05"},
                {"儿童节", "06-01"},
                {"中秋节", "08-15"},
                {"国庆节", "10-01"},
                {"重阳节", "09-09"},
                {"圣诞节", "12-25"}
        };

        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);

        for (String[] holiday : holidays) {
            String name = holiday[0];
            String dateStr = holiday[1];

            Calendar holidayCal = Calendar.getInstance();
            holidayCal.set(Calendar.YEAR, currentYear);
            holidayCal.set(Calendar.MONTH, Integer.parseInt(dateStr.split("-")[0]) - 1);
            holidayCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateStr.split("-")[1]));
            holidayCal.set(Calendar.HOUR_OF_DAY, 0);
            holidayCal.set(Calendar.MINUTE, 0);
            holidayCal.set(Calendar.SECOND, 0);

            if (holidayCal.before(now)) {
                holidayCal.set(Calendar.YEAR, currentYear + 1);
            }

            long diffMillis = holidayCal.getTimeInMillis() - now.getTimeInMillis();
            long days = diffMillis / (1000 * 60 * 60 * 24);
            long hours = (diffMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (diffMillis % (1000 * 60 * 60)) / (1000 * 60);

            String countdown;
            if (days > 365) {
                countdown = "还有 " + (days / 365) + " 年";
            } else if (days > 30) {
                countdown = "还有 " + (days / 30) + " 个月";
            } else {
                countdown = "还有 " + days + " 天 " + hours + " 小时 " + minutes + " 分";
            }

            View itemView = getLayoutInflater().inflate(R.layout.item_holiday_countdown, layoutHolidays, false);
            TextView tvHolidayName = itemView.findViewById(R.id.tv_holiday_name);
            TextView tvHolidayDate = itemView.findViewById(R.id.tv_holiday_date);
            TextView tvCountdown = itemView.findViewById(R.id.tv_countdown);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tvHolidayName.setText(name);
            tvHolidayDate.setText("下次: " + sdf.format(holidayCal.getTime()));
            tvCountdown.setText(countdown);

            layoutHolidays.addView(itemView);
        }
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