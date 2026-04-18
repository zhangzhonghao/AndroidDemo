package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Calendar;
import java.util.Locale;

public class LunarNewYearCountdownActivity extends AppCompatActivity {

    private TextView tvCountdown;
    private TextView tvNextYear;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lunar_new_year_countdown);

        tvCountdown = findViewById(R.id.tv_countdown);
        tvNextYear = findViewById(R.id.tv_next_year);
        handler = new Handler(Looper.getMainLooper());

        startCountdown();
    }

    private void startCountdown() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateCountdown();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void updateCountdown() {
        Calendar now = Calendar.getInstance();
        int currentYear = now.get(Calendar.YEAR);

        Calendar chineseNewYear = getChineseNewYearCalendar(currentYear);
        if (now.after(chineseNewYear)) {
            chineseNewYear = getChineseNewYearCalendar(currentYear + 1);
        }

        long diff = chineseNewYear.getTimeInMillis() - now.getTimeInMillis();

        long days = diff / (1000 * 60 * 60 * 24);
        long hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (diff % (1000 * 60)) / 1000;

        String countdown = String.format(Locale.getDefault(),
                "%02d天 %02d时 %02d分 %02d秒", days, hours, minutes, seconds);
        tvCountdown.setText(countdown);
        tvNextYear.setText("下一个农历新年: " + chineseNewYear.get(Calendar.YEAR) + "年");
    }

    private Calendar getChineseNewYearCalendar(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY, 1);
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        cal.add(Calendar.DAY_OF_MONTH, 6 * 7);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysUntilFriday = (Calendar.FRIDAY - dayOfWeek + 7) % 7;
        cal.add(Calendar.DAY_OF_MONTH, daysUntilFriday);

        int[] lunarNewYearDates = {
            1, 5, 10, 2, 22, 12, 3, 18, 8, 28, 16, 6, 26, 14, 4, 24, 12, 1, 21, 10, 30, 19, 9, 27, 17, 6, 25, 13, 2, 22, 10, 29, 17
        };
        int month = ((year - 2020) % 34 + 30) % 34 / 12 + 1;
        int day = lunarNewYearDates[(year - 1900) % 33];
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day % 28 + 1);
        return cal;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}
