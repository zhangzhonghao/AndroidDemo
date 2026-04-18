package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class WaterReminderActivity extends AppCompatActivity {

    private TextView tvGlassCount;
    private TextView tvReminder;
    private int glassCount = 0;
    private final Handler handler = new Handler();
    private boolean reminderRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_reminder);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("喝水提醒");
        }

        tvGlassCount = findViewById(R.id.tv_glass_count);
        tvReminder = findViewById(R.id.tv_reminder);

        findViewById(R.id.btn_add_glass).setOnClickListener(v -> addGlass());
        findViewById(R.id.btn_reset).setOnClickListener(v -> resetCount());

        startReminder();
    }

    private void addGlass() {
        glassCount++;
        tvGlassCount.setText(glassCount + " 杯");

        if (glassCount >= 8) {
            tvReminder.setText("今日饮水量已达标！继续保持");
        } else {
            int remaining = 8 - glassCount;
            tvReminder.setText("还差 " + remaining + " 杯水");
        }
    }

    private void resetCount() {
        glassCount = 0;
        tvGlassCount.setText("0 杯");
        tvReminder.setText("建议每日8杯水");
    }

    private void startReminder() {
        reminderRunning = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (reminderRunning) {
                    if (glassCount < 8) {
                        tvReminder.setText("该喝水了！当前 " + glassCount + " 杯，还差 " + (8 - glassCount) + " 杯");
                    }
                    handler.postDelayed(this, 3600000); // 每小时提醒一次
                }
            }
        }, 3600000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reminderRunning = false;
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