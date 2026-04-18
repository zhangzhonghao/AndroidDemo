package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class FlashSaleActivity extends AppCompatActivity {
    private TextView tvCountdown;
    private TextView tvStatus;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sale);
        initViews();
        startCountdown();
    }

    private void initViews() {
        tvCountdown = findViewById(R.id.tv_countdown);
        tvStatus = findViewById(R.id.tv_status);
    }

    private void startCountdown() {
        tvStatus.setText("抢购进行中");
        long endTime = System.currentTimeMillis() + 3600000; // 1小时倒计时
        countDownTimer = new CountDownTimer(3600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = millisUntilFinished / 3600000;
                long minutes = (millisUntilFinished % 3600000) / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvCountdown.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("00:00:00");
                tvStatus.setText("抢购已结束");
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}