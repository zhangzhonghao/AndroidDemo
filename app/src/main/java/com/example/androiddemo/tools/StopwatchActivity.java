package com.example.androiddemo.tools;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class StopwatchActivity extends AppCompatActivity {

    // Stopwatch mode
    private TextView tvStopwatch;
    private Button btnStartStopwatch;
    private Button btnResetStopwatch;
    private Button btnLap;
    private LinearLayout lapListContainer;
    private ScrollView lapScrollView;

    // Countdown mode
    private TimePicker timePicker;
    private TextView tvCountdown;
    private LinearLayout quickTimeContainer;
    private Button btnStartCountdown;
    private Button btnPauseCountdown;
    private Button btnResetCountdown;

    // Mode selection
    private RadioGroup modeRadioGroup;
    private View stopwatchLayout;
    private View countdownLayout;

    // Timer logic
    private Handler handler = new Handler();
    private boolean isStopwatchRunning = false;
    private boolean isCountdownRunning = false;
    private long stopwatchStartTime = 0;
    private long stopwatchElapsedTime = 0;
    private long countdownDuration = 0;
    private long countdownRemainingTime = 0;
    private List<Long> lapTimes = new ArrayList<>();
    private long lastLapTime = 0;

    private static final int UPDATE_INTERVAL = 10; // 10ms update

    private Runnable stopwatchRunnable = new Runnable() {
        @Override
        public void run() {
            if (isStopwatchRunning) {
                long currentTime = System.currentTimeMillis();
                long elapsed = stopwatchElapsedTime + (currentTime - stopwatchStartTime);
                tvStopwatch.setText(formatTime(elapsed));
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCountdownRunning) {
                long currentTime = System.currentTimeMillis();
                countdownRemainingTime -= UPDATE_INTERVAL;
                if (countdownRemainingTime <= 0) {
                    countdownRemainingTime = 0;
                    tvCountdown.setText(formatTime(0));
                    onCountdownFinished();
                } else {
                    tvCountdown.setText(formatTime(countdownRemainingTime));
                    handler.postDelayed(this, UPDATE_INTERVAL);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_stopwatch);

        initViews();
        setupListeners();
        updateStopwatchUI();
        updateCountdownUI();
    }

    private void initViews() {
        // Mode selection
        modeRadioGroup = findViewById(R.id.mode_radio_group);
        stopwatchLayout = findViewById(R.id.stopwatch_layout);
        countdownLayout = findViewById(R.id.countdown_layout);

        // Stopwatch views
        tvStopwatch = findViewById(R.id.tv_stopwatch);
        btnStartStopwatch = findViewById(R.id.btn_start_stopwatch);
        btnResetStopwatch = findViewById(R.id.btn_reset_stopwatch);
        btnLap = findViewById(R.id.btn_lap);
        lapListContainer = findViewById(R.id.lap_list_container);
        lapScrollView = findViewById(R.id.lap_scroll_view);

        // Countdown views
        timePicker = findViewById(R.id.time_picker);
        tvCountdown = findViewById(R.id.tv_countdown);
        quickTimeContainer = findViewById(R.id.quick_time_container);
        btnStartCountdown = findViewById(R.id.btn_start_countdown);
        btnPauseCountdown = findViewById(R.id.btn_pause_countdown);
        btnResetCountdown = findViewById(R.id.btn_reset_countdown);

        // Setup time picker
        timePicker.setIs24HourView(true);
    }

    private void setupListeners() {
        // Mode switching
        modeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_stopwatch) {
                stopwatchLayout.setVisibility(View.VISIBLE);
                countdownLayout.setVisibility(View.GONE);
            } else {
                stopwatchLayout.setVisibility(View.GONE);
                countdownLayout.setVisibility(View.VISIBLE);
            }
        });

        // Stopwatch buttons
        btnStartStopwatch.setOnClickListener(v -> {
            if (isStopwatchRunning) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnResetStopwatch.setOnClickListener(v -> resetStopwatch());
        btnLap.setOnClickListener(v -> recordLap());

        // Quick time buttons
        quickTimeContainer.findViewById(R.id.btn_1_min).setOnClickListener(v -> setQuickTime(1));
        quickTimeContainer.findViewById(R.id.btn_5_min).setOnClickListener(v -> setQuickTime(5));
        quickTimeContainer.findViewById(R.id.btn_10_min).setOnClickListener(v -> setQuickTime(10));
        quickTimeContainer.findViewById(R.id.btn_30_min).setOnClickListener(v -> setQuickTime(30));

        // Countdown buttons
        btnStartCountdown.setOnClickListener(v -> startCountdown());
        btnPauseCountdown.setOnClickListener(v -> pauseCountdown());
        btnResetCountdown.setOnClickListener(v -> resetCountdown());
    }

    // Stopwatch methods
    private void startStopwatch() {
        isStopwatchRunning = true;
        stopwatchStartTime = System.currentTimeMillis();
        handler.post(stopwatchRunnable);
        updateStopwatchUI();
    }

    private void pauseStopwatch() {
        isStopwatchRunning = false;
        stopwatchElapsedTime += System.currentTimeMillis() - stopwatchStartTime;
        handler.removeCallbacks(stopwatchRunnable);
        updateStopwatchUI();
    }

    private void resetStopwatch() {
        isStopwatchRunning = false;
        stopwatchElapsedTime = 0;
        stopwatchStartTime = 0;
        lapTimes.clear();
        lastLapTime = 0;
        handler.removeCallbacks(stopwatchRunnable);
        tvStopwatch.setText(formatTime(0));
        lapListContainer.removeAllViews();
        updateStopwatchUI();
    }

    private void recordLap() {
        if (isStopwatchRunning || stopwatchElapsedTime > 0) {
            long currentLapTime = stopwatchElapsedTime + (isStopwatchRunning ? System.currentTimeMillis() - stopwatchStartTime : 0);
            long lapDuration = currentLapTime - lastLapTime;
            lapTimes.add(currentLapTime);
            lastLapTime = currentLapTime;

            // Add lap to list
            TextView lapText = new TextView(this);
            lapText.setText(String.format("第 %d 圈: %s (+%s)",
                    lapTimes.size(),
                    formatTime(currentLapTime),
                    formatTime(lapDuration)));
            lapText.setTextSize(14);
            lapText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            lapText.setPadding(16, 8, 16, 8);
            lapListContainer.addView(lapText);

            // Scroll to bottom
            handler.post(() -> lapScrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void updateStopwatchUI() {
        if (isStopwatchRunning) {
            btnStartStopwatch.setText("暂停");
            btnResetStopwatch.setEnabled(true);
            btnLap.setEnabled(true);
        } else {
            btnStartStopwatch.setText(stopwatchElapsedTime > 0 ? "继续" : "开始");
            btnResetStopwatch.setEnabled(stopwatchElapsedTime > 0);
            btnLap.setEnabled(isStopwatchRunning);
        }
    }

    // Countdown methods
    private void setQuickTime(int minutes) {
        if (!isCountdownRunning) {
            timePicker.setHour(0);
            timePicker.setMinute(minutes);
        }
    }

    private void startCountdown() {
        if (!isCountdownRunning) {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            if (hour == 0 && minute == 0) {
                Toast.makeText(this, "请设置倒计时时间", Toast.LENGTH_SHORT).show();
                return;
            }

            countdownDuration = (hour * 3600 + minute * 60) * 1000L;
            countdownRemainingTime = countdownDuration;
            isCountdownRunning = true;
            handler.post(countdownRunnable);
            updateCountdownUI();
        }
    }

    private void pauseCountdown() {
        isCountdownRunning = false;
        handler.removeCallbacks(countdownRunnable);
        updateCountdownUI();
    }

    private void resetCountdown() {
        isCountdownRunning = false;
        countdownRemainingTime = 0;
        countdownDuration = 0;
        handler.removeCallbacks(countdownRunnable);
        tvCountdown.setText(formatTime(0));
        updateCountdownUI();
    }

    private void onCountdownFinished() {
        isCountdownRunning = false;
        handler.removeCallbacks(countdownRunnable);
        updateCountdownUI();

        // Vibrate
        vibrate();

        // Play notification sound
        playNotificationSound();

        Toast.makeText(this, "倒计时结束！", Toast.LENGTH_LONG).show();
    }

    private void vibrate() {
        try {
            Vibrator vibrator;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vibratorManager.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                VibrationEffect effect = VibrationEffect.createWaveform(
                        new long[]{0, 500, 200, 500, 200, 500}, -1);
                vibrator.vibrate(effect);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playNotificationSound() {
        try {
            Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, notificationUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCountdownUI() {
        if (isCountdownRunning) {
            btnStartCountdown.setEnabled(false);
            btnPauseCountdown.setEnabled(true);
            btnResetCountdown.setEnabled(true);
            timePicker.setEnabled(false);
        } else {
            btnStartCountdown.setEnabled(countdownRemainingTime == 0 || countdownRemainingTime == countdownDuration);
            btnPauseCountdown.setEnabled(isCountdownRunning);
            btnResetCountdown.setEnabled(countdownRemainingTime > 0);
            timePicker.setEnabled(countdownRemainingTime == 0);
        }
    }

    // Format time as HH:MM:SS.mm
    private String formatTime(long milliseconds) {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds % 3600000) / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        long ms = (milliseconds % 1000) / 10;

        return String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, ms);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(stopwatchRunnable);
        handler.removeCallbacks(countdownRunnable);
    }
}