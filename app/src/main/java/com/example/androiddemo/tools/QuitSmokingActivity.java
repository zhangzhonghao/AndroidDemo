package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class QuitSmokingActivity extends AppCompatActivity {

    private TextView tvDays;
    private TextView tvHours;
    private TextView tvMinutes;
    private TextView tvSeconds;
    private TextView tvMoneySaved;
    private TextView tvLifeSaved;
    private long quitTimeMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quit_smoking);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("戒烟倒计时");
        }

        tvDays = findViewById(R.id.tv_days);
        tvHours = findViewById(R.id.tv_hours);
        tvMinutes = findViewById(R.id.tv_minutes);
        tvSeconds = findViewById(R.id.tv_seconds);
        tvMoneySaved = findViewById(R.id.tv_money_saved);
        tvLifeSaved = findViewById(R.id.tv_life_saved);

        // 假设戒烟开始时间为2024年1月1日
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date quitDate = sdf.parse("2024-01-01 00:00:00");
            quitTimeMillis = quitDate.getTime();
        } catch (Exception e) {
            quitTimeMillis = System.currentTimeMillis();
        }

        updateCounter();
    }

    private void updateCounter() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - quitTimeMillis;

                if (diff > 0) {
                    long days = TimeUnit.MILLISECONDS.toDays(diff);
                    long hours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

                    tvDays.setText(days + "");
                    tvHours.setText(String.format("%02d", hours));
                    tvMinutes.setText(String.format("%02d", minutes));
                    tvSeconds.setText(String.format("%02d", seconds));

                    // 假设每天1包烟，每包20元
                    double moneySaved = days * 20;
                    tvMoneySaved.setText(String.format("已节省约 %.0f 元", moneySaved));

                    // 戒烟可延长寿命（每戒1天多活8分钟）
                    long lifeMinutesSaved = days * 8;
                    long lifeDaysSaved = lifeMinutesSaved / 1440;
                    tvLifeSaved.setText("估计延长寿命约 " + lifeDaysSaved + " 天");
                }

                updateCounter();
            }
        }, 1000);
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