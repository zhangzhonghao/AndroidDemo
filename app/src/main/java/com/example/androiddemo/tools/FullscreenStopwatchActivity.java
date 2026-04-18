package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class FullscreenStopwatchActivity extends AppCompatActivity {
    private TextView tvTime;
    private Button btnStart;
    private Button btnReset;
    private long startTime = 0;
    private long elapsedTime = 0;
    private boolean isRunning = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_stopwatch);

        tvTime = findViewById(R.id.tv_time);
        btnStart = findViewById(R.id.btn_start);
        btnReset = findViewById(R.id.btn_reset);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                           WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    updateDisplay();
                    handler.postDelayed(this, 10);
                }
            }
        };

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    pause();
                } else {
                    start();
                }
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });

        updateDisplay();
    }

    private void start() {
        startTime = System.currentTimeMillis() - elapsedTime;
        isRunning = true;
        btnStart.setText("暂停");
        handler.post(updateRunnable);
    }

    private void pause() {
        isRunning = false;
        btnStart.setText("继续");
        handler.removeCallbacks(updateRunnable);
    }

    private void reset() {
        isRunning = false;
        elapsedTime = 0;
        btnStart.setText("开始");
        handler.removeCallbacks(updateRunnable);
        updateDisplay();
    }

    private void updateDisplay() {
        long millis = elapsedTime;
        int hours = (int) (millis / 3600000);
        int minutes = (int) ((millis % 3600000) / 60000);
        int seconds = (int) ((millis % 60000) / 1000);
        int ms = (int) ((millis % 1000) / 10);

        tvTime.setText(String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, ms));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}