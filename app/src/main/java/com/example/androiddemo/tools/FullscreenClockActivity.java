package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FullscreenClockActivity extends AppCompatActivity {
    private TextView tvTime;
    private TextView tvDate;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 E", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_clock);

        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);

        // 全屏设置
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        updateTime();
    }

    private void updateTime() {
        long now = System.currentTimeMillis();
        tvTime.setText(timeFormat.format(new Date(now)));
        tvDate.setText(dateFormat.format(new Date(now)));

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}