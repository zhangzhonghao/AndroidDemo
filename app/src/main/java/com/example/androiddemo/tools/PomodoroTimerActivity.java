package com.example.androiddemo.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;

public class PomodoroTimerActivity extends AppCompatActivity {

    public static final String ACTION_UPDATE = "com.example.androiddemo.tools.ACTION_POMODORO_UPDATE";
    public static final String ACTION_COMPLETE = "com.example.androiddemo.tools.ACTION_POMODORO_COMPLETE";
    public static final String EXTRA_REMAINING_TIME = "remaining_time";
    public static final String EXTRA_IS_RUNNING = "is_running";
    public static final String EXTRA_CURRENT_TYPE = "current_type";

    // Timer states
    public static final int STATE_WORK = 0;
    public static final int STATE_SHORT_BREAK = 1;
    public static final int STATE_LONG_BREAK = 2;

    // Default durations (in minutes)
    private static final int DEFAULT_WORK_DURATION = 25;
    private static final int DEFAULT_SHORT_BREAK_DURATION = 5;
    private static final int DEFAULT_LONG_BREAK_DURATION = 15;
    private static final int POMODOROS_BEFORE_LONG_BREAK = 4;

    // Views
    private TextView tvTimer;
    private TextView tvState;
    private TextView tvPomodoroCount;
    private Button btnStartPause;
    private Button btnReset;
    private ImageButton btnSettings;
    private ImageButton btnSkip;

    // Timer state
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private int currentState = STATE_WORK;
    private int completedPomodoros = 0;
    private long remainingTimeMs = DEFAULT_WORK_DURATION * 60 * 1000L;
    private long totalTimeMs = DEFAULT_WORK_DURATION * 60 * 1000L;

    // Settings
    private int workDuration = DEFAULT_WORK_DURATION;
    private int shortBreakDuration = DEFAULT_SHORT_BREAK_DURATION;
    private int longBreakDuration = DEFAULT_LONG_BREAK_DURATION;

    // SharedPreferences for statistics
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "pomodoro_prefs";
    private static final String KEY_TODAY_POMODOROS = "today_pomodoros";
    private static final String KEY_LAST_DATE = "last_date";

    private BroadcastReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro_timer);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadStatistics();
        initViews();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        tvTimer = findViewById(R.id.tv_timer);
        tvState = findViewById(R.id.tv_state);
        tvPomodoroCount = findViewById(R.id.tv_pomodoro_count);
        btnStartPause = findViewById(R.id.btn_start_pause);
        btnReset = findViewById(R.id.btn_reset);
        btnSettings = findViewById(R.id.btn_settings);
        btnSkip = findViewById(R.id.btn_skip);
    }

    private void setupListeners() {
        btnStartPause.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnReset.setOnClickListener(v -> resetTimer());
        btnSettings.setOnClickListener(v -> showSettingsDialog());
        btnSkip.setOnClickListener(v -> skipToNext());

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_UPDATE.equals(action)) {
                    remainingTimeMs = intent.getLongExtra(EXTRA_REMAINING_TIME, remainingTimeMs);
                    isRunning = intent.getBooleanExtra(EXTRA_IS_RUNNING, isRunning);
                    currentState = intent.getIntExtra(EXTRA_CURRENT_TYPE, currentState);
                    updateUI();
                } else if (ACTION_COMPLETE.equals(action)) {
                    onTimerComplete();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);
        filter.addAction(ACTION_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
    }

    private void startTimer() {
        Intent serviceIntent = new Intent(this, PomodoroService.class);
        serviceIntent.setAction(PomodoroService.ACTION_START);
        serviceIntent.putExtra(PomodoroService.EXTRA_DURATION, remainingTimeMs);
        serviceIntent.putExtra(PomodoroService.EXTRA_STATE, currentState);
        ContextCompat.startForegroundService(this, serviceIntent);
        isRunning = true;
        updateUI();
    }

    private void pauseTimer() {
        Intent serviceIntent = new Intent(this, PomodoroService.class);
        serviceIntent.setAction(PomodoroService.ACTION_PAUSE);
        sendBroadcast(serviceIntent);
        isRunning = false;
        updateUI();
    }

    private void resetTimer() {
        Intent serviceIntent = new Intent(this, PomodoroService.class);
        serviceIntent.setAction(PomodoroService.ACTION_STOP);
        sendBroadcast(serviceIntent);
        isRunning = false;
        currentState = STATE_WORK;
        remainingTimeMs = workDuration * 60 * 1000L;
        totalTimeMs = remainingTimeMs;
        updateUI();
    }

    private void skipToNext() {
        Intent serviceIntent = new Intent(this, PomodoroService.class);
        serviceIntent.setAction(PomodoroService.ACTION_STOP);
        sendBroadcast(serviceIntent);
        moveToNextState();
    }

    private void moveToNextState() {
        if (currentState == STATE_WORK) {
            completedPomodoros++;
            saveStatistics();
            if (completedPomodoros % POMODOROS_BEFORE_LONG_BREAK == 0) {
                currentState = STATE_LONG_BREAK;
                remainingTimeMs = longBreakDuration * 60 * 1000L;
            } else {
                currentState = STATE_SHORT_BREAK;
                remainingTimeMs = shortBreakDuration * 60 * 1000L;
            }
        } else {
            currentState = STATE_WORK;
            remainingTimeMs = workDuration * 60 * 1000L;
        }
        totalTimeMs = remainingTimeMs;
        isRunning = false;
        updateUI();
    }

    private void onTimerComplete() {
        isRunning = false;
        vibrate();
        playNotificationSound();

        if (currentState == STATE_WORK) {
            Toast.makeText(this, "番茄钟完成！休息一下吧~", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "休息结束！继续工作吧~", Toast.LENGTH_SHORT).show();
        }

        moveToNextState();
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_pomodoro_settings, null);

        NumberPicker npWork = dialogView.findViewById(R.id.np_work_duration);
        NumberPicker npShortBreak = dialogView.findViewById(R.id.np_short_break);
        NumberPicker npLongBreak = dialogView.findViewById(R.id.np_long_break);

        npWork.setMinValue(1);
        npWork.setMaxValue(60);
        npWork.setValue(workDuration);

        npShortBreak.setMinValue(1);
        npShortBreak.setMaxValue(30);
        npShortBreak.setValue(shortBreakDuration);

        npLongBreak.setMinValue(5);
        npLongBreak.setMaxValue(60);
        npLongBreak.setValue(longBreakDuration);

        new AlertDialog.Builder(this)
                .setTitle("番茄钟设置")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    workDuration = npWork.getValue();
                    shortBreakDuration = npShortBreak.getValue();
                    longBreakDuration = npLongBreak.getValue();
                    if (!isRunning) {
                        remainingTimeMs = currentState == STATE_WORK ?
                                workDuration * 60 * 1000L :
                                (currentState == STATE_SHORT_BREAK ? shortBreakDuration * 60 * 1000L : longBreakDuration * 60 * 1000L);
                        totalTimeMs = remainingTimeMs;
                        updateUI();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateUI() {
        tvTimer.setText(formatTime(remainingTimeMs));
        tvPomodoroCount.setText("🍅 × " + completedPomodoros);

        switch (currentState) {
            case STATE_WORK:
                tvState.setText("工作时间");
                tvState.setTextColor(ContextCompat.getColor(this, R.color.pomodoro_work));
                break;
            case STATE_SHORT_BREAK:
                tvState.setText("短休息");
                tvState.setTextColor(ContextCompat.getColor(this, R.color.pomodoro_short_break));
                break;
            case STATE_LONG_BREAK:
                tvState.setText("长休息");
                tvState.setTextColor(ContextCompat.getColor(this, R.color.pomodoro_long_break));
                break;
        }

        btnStartPause.setText(isRunning ? "暂停" : (remainingTimeMs < totalTimeMs ? "继续" : "开始"));

        int progress = totalTimeMs > 0 ? (int) ((totalTimeMs - remainingTimeMs) * 100 / totalTimeMs) : 0;
    }

    private String formatTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void vibrate() {
        try {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    private void loadStatistics() {
        String today = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        String lastDate = prefs.getString(KEY_LAST_DATE, "");

        if (!today.equals(lastDate)) {
            prefs.edit().putInt(KEY_TODAY_POMODOROS, 0).putString(KEY_LAST_DATE, today).apply();
            completedPomodoros = 0;
        } else {
            completedPomodoros = prefs.getInt(KEY_TODAY_POMODOROS, 0);
        }
    }

    private void saveStatistics() {
        prefs.edit().putInt(KEY_TODAY_POMODOROS, completedPomodoros).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
        handler.removeCallbacksAndMessages(null);
    }
}