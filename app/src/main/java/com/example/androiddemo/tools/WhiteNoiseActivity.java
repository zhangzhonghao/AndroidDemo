package com.example.androiddemo.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class WhiteNoiseActivity extends AppCompatActivity {

    public static final String ACTION_UPDATE = "com.example.androiddemo.tools.ACTION_WHITE_NOISE_UPDATE";
    public static final String ACTION_TIMER_COMPLETE = "com.example.androiddemo.tools.ACTION_WHITE_NOISE_TIMER_COMPLETE";
    public static final String EXTRA_TIMER_REMAINING = "timer_remaining";
    public static final String EXTRA_IS_TIMER_RUNNING = "is_timer_running";
    public static final String EXTRA_PLAYING_SOUNDS = "playing_sounds";

    // Sound types
    public static final int SOUND_RAIN = 0;
    public static final int SOUND_WAVE = 1;
    public static final int SOUND_FOREST = 2;
    public static final int SOUND_FIRE = 3;
    public static final int SOUND_WIND = 4;
    public static final int SOUND_WATERFALL = 5;
    public static final int SOUND_AIRCON = 6;
    public static final int SOUND_TRAFFIC = 7;
    public static final int SOUND_COUNT = 8;

    // Sound resource IDs (raw resources)
    private static final int[] SOUND_RES_IDS = {
        R.raw.sound_rain,
        R.raw.sound_wave,
        R.raw.sound_forest,
        R.raw.sound_fire,
        R.raw.sound_wind,
        R.raw.sound_waterfall,
        R.raw.sound_aircon,
        R.raw.sound_traffic
    };

    private static final String[] SOUND_NAMES = {
        "雨声", "海浪", "森林", "篝火", "风声", "瀑布", "空调", "交通"
    };

    // Views
    private TextView tvRemainingTime;
    private TextView tvPlayingStatus;
    private SeekBar seekbarMaster;
    private Button btnStopAll;
    private Button btnCancelTimer;
    private ImageButton btnTimer;

    private Map<Integer, CardData> soundCards = new HashMap<>();

    // SoundPool for playing sounds
    private SoundPool soundPool;
    private Map<Integer, Integer> soundIds = new HashMap<>();
    private Map<Integer, Boolean> soundPlaying = new HashMap<>();
    private Map<Integer, Float> soundVolumes = new HashMap<>();
    private float masterVolume = 0.8f;

    // Timer
    private CountDownTimer countDownTimer;
    private long timerRemainingMs = 0;
    private boolean isTimerRunning = false;

    // Service communication
    private BroadcastReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white_noise);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initSoundPool();
        initViews();
        setupListeners();
        updatePlayingStatus();
    }

    private void initSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(SOUND_COUNT)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load sounds - using 0 as placeholder since we don't have real sound files
        for (int i = 0; i < SOUND_COUNT; i++) {
            soundIds.put(i, 0); // Will be replaced when sounds are loaded
            soundPlaying.put(i, false);
            soundVolumes.put(i, 0.5f);
        }
    }

    private void initViews() {
        tvRemainingTime = findViewById(R.id.tv_remaining_time);
        tvPlayingStatus = findViewById(R.id.tv_playing_status);
        seekbarMaster = findViewById(R.id.seekbar_master);
        btnStopAll = findViewById(R.id.btn_stop_all);
        btnCancelTimer = findViewById(R.id.btn_cancel_timer);
        btnTimer = findViewById(R.id.btn_timer);

        // Initialize sound cards
        soundCards.put(SOUND_RAIN, new CardData(
                findViewById(R.id.icon_rain),
                findViewById(R.id.seekbar_rain),
                findViewById(R.id.tv_rain_status),
                SOUND_RAIN
        ));
        soundCards.put(SOUND_WAVE, new CardData(
                findViewById(R.id.icon_wave),
                findViewById(R.id.seekbar_wave),
                findViewById(R.id.tv_wave_status),
                SOUND_WAVE
        ));
        soundCards.put(SOUND_FOREST, new CardData(
                findViewById(R.id.icon_forest),
                findViewById(R.id.seekbar_forest),
                findViewById(R.id.tv_forest_status),
                SOUND_FOREST
        ));
        soundCards.put(SOUND_FIRE, new CardData(
                findViewById(R.id.icon_fire),
                findViewById(R.id.seekbar_fire),
                findViewById(R.id.tv_fire_status),
                SOUND_FIRE
        ));
        soundCards.put(SOUND_WIND, new CardData(
                findViewById(R.id.icon_wind),
                findViewById(R.id.seekbar_wind),
                findViewById(R.id.tv_wind_status),
                SOUND_WIND
        ));
        soundCards.put(SOUND_WATERFALL, new CardData(
                findViewById(R.id.icon_waterfall),
                findViewById(R.id.seekbar_waterfall),
                findViewById(R.id.tv_waterfall_status),
                SOUND_WATERFALL
        ));
        soundCards.put(SOUND_AIRCON, new CardData(
                findViewById(R.id.icon_aircon),
                findViewById(R.id.seekbar_aircon),
                findViewById(R.id.tv_aircon_status),
                SOUND_AIRCON
        ));
        soundCards.put(SOUND_TRAFFIC, new CardData(
                findViewById(R.id.icon_traffic),
                findViewById(R.id.seekbar_traffic),
                findViewById(R.id.tv_traffic_status),
                SOUND_TRAFFIC
        ));

        // Set click listeners for cards
        for (CardData card : soundCards.values()) {
            card.rootLayout.setOnClickListener(v -> onSoundCardClick(card.soundType));
        }
    }

    private void setupListeners() {
        seekbarMaster.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                masterVolume = progress / 100f;
                updateAllVolumes();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnStopAll.setOnClickListener(v -> stopAllSounds());
        btnCancelTimer.setOnClickListener(v -> cancelTimer());
        btnTimer.setOnClickListener(v -> showTimerDialog());

        // Setup individual sound seekbars
        for (Map.Entry<Integer, CardData> entry : soundCards.entrySet()) {
            final int soundType = entry.getKey();
            SeekBar seekBar = entry.getValue().volumeSeekBar;
            seekBar.setProgress((int)(soundVolumes.get(soundType) * 100));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    soundVolumes.put(soundType, progress / 100f);
                    updateVolume(soundType);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // Register broadcast receiver
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_UPDATE.equals(action)) {
                    timerRemainingMs = intent.getLongExtra(EXTRA_TIMER_REMAINING, timerRemainingMs);
                    isTimerRunning = intent.getBooleanExtra(EXTRA_IS_TIMER_RUNNING, isTimerRunning);
                    updateTimerUI();
                } else if (ACTION_TIMER_COMPLETE.equals(action)) {
                    onTimerComplete();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE);
        filter.addAction(ACTION_TIMER_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
    }

    private void onSoundCardClick(int soundType) {
        CardData card = soundCards.get(soundType);
        boolean isPlaying = soundPlaying.get(soundType);

        if (isPlaying) {
            stopSound(soundType);
        } else {
            playSound(soundType);
        }
    }

    private void playSound(int soundType) {
        soundPlaying.put(soundType, true);
        updateCardUI(soundType);
        updatePlayingStatus();
        startBackgroundService();
    }

    private void stopSound(int soundType) {
        soundPlaying.put(soundType, false);
        updateCardUI(soundType);
        updatePlayingStatus();

        // Check if any sound is still playing
        boolean anyPlaying = false;
        for (Boolean playing : soundPlaying.values()) {
            if (playing) {
                anyPlaying = true;
                break;
            }
        }

        if (!anyPlaying) {
            stopBackgroundService();
        }
    }

    private void stopAllSounds() {
        for (int i = 0; i < SOUND_COUNT; i++) {
            soundPlaying.put(i, false);
            updateCardUI(i);
        }
        updatePlayingStatus();
        stopBackgroundService();
    }

    private void updateCardUI(int soundType) {
        CardData card = soundCards.get(soundType);
        boolean isPlaying = soundPlaying.get(soundType);

        card.volumeSeekBar.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        card.statusText.setText(isPlaying ? "Playing" : "点击播放");

        // Update icon alpha for visual feedback
        card.icon.setAlpha(isPlaying ? 1.0f : 0.5f);
    }

    private void updateAllVolumes() {
        for (int i = 0; i < SOUND_COUNT; i++) {
            updateVolume(i);
        }
    }

    private void updateVolume(int soundType) {
        if (soundPlaying.get(soundType)) {
            float volume = masterVolume * soundVolumes.get(soundType);
            // Volume would be applied to SoundPool stream
            // For now, the volume is tracked and would be applied when playing
        }
    }

    private void updatePlayingStatus() {
        int playingCount = 0;
        for (Boolean playing : soundPlaying.values()) {
            if (playing) playingCount++;
        }

        if (playingCount == 0) {
            tvPlayingStatus.setText("点击音效开始播放");
        } else if (playingCount == 1) {
            for (Map.Entry<Integer, Boolean> entry : soundPlaying.entrySet()) {
                if (entry.getValue()) {
                    tvPlayingStatus.setText("正在播放: " + SOUND_NAMES[entry.getKey()]);
                    break;
                }
            }
        } else {
            tvPlayingStatus.setText("正在播放 " + playingCount + " 个音效");
        }
    }

    private void showTimerDialog() {
        String[] options = {"15分钟", "30分钟", "45分钟", "60分钟", "自定义"};

        new AlertDialog.Builder(this)
                .setTitle("定时关闭")
                .setItems(options, (dialog, which) -> {
                    int minutes;
                    if (which < 4) {
                        minutes = (which + 1) * 15;
                    } else {
                        showCustomTimerDialog();
                        return;
                    }
                    startTimer(minutes);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showCustomTimerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_timer_input, null);
        SeekBar seekBar = dialogView.findViewById(R.id.seekbar_custom_timer);
        TextView tvValue = dialogView.findViewById(R.id.tv_timer_value);

        seekBar.setMax(180);
        seekBar.setProgress(30);
        tvValue.setText("30 分钟");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minutes = Math.max(1, progress);
                tvValue.setText(minutes + " 分钟");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("自定义定时")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    int minutes = Math.max(1, seekBar.getProgress());
                    startTimer(minutes);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void startTimer(int minutes) {
        cancelTimer();

        timerRemainingMs = minutes * 60 * 1000L;
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(timerRemainingMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerRemainingMs = millisUntilFinished;
                updateTimerUI();
                broadcastTimerUpdate();
            }

            @Override
            public void onFinish() {
                timerRemainingMs = 0;
                isTimerRunning = false;
                updateTimerUI();
                stopAllSounds();
                onTimerComplete();
            }
        }.start();

        updateTimerUI();
        Toast.makeText(this, "定时 " + minutes + " 分钟", Toast.LENGTH_SHORT).show();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isTimerRunning = false;
        timerRemainingMs = 0;
        updateTimerUI();
    }

    private void updateTimerUI() {
        if (isTimerRunning && timerRemainingMs > 0) {
            tvRemainingTime.setVisibility(View.VISIBLE);
            tvRemainingTime.setText(formatTime(timerRemainingMs));
            btnCancelTimer.setVisibility(View.VISIBLE);
        } else {
            tvRemainingTime.setVisibility(View.GONE);
            btnCancelTimer.setVisibility(View.GONE);
        }
    }

    private void broadcastTimerUpdate() {
        Intent intent = new Intent(ACTION_UPDATE);
        intent.putExtra(EXTRA_TIMER_REMAINING, timerRemainingMs);
        intent.putExtra(EXTRA_IS_TIMER_RUNNING, isTimerRunning);
        sendBroadcast(intent);
    }

    private void onTimerComplete() {
        Toast.makeText(this, "定时结束，播放停止", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ACTION_TIMER_COMPLETE);
        sendBroadcast(intent);
    }

    private String formatTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, WhiteNoiseService.class);
        serviceIntent.setAction(WhiteNoiseService.ACTION_START);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopBackgroundService() {
        Intent serviceIntent = new Intent(this, WhiteNoiseService.class);
        serviceIntent.setAction(WhiteNoiseService.ACTION_STOP);
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
    }

    // Inner class for card data
    private static class CardData {
        ImageView icon;
        SeekBar volumeSeekBar;
        TextView statusText;
        int soundType;
        LinearLayout rootLayout;

        CardData(ImageView icon, SeekBar volumeSeekBar, TextView statusText, int soundType) {
            this.icon = icon;
            this.volumeSeekBar = volumeSeekBar;
            this.statusText = statusText;
            this.soundType = soundType;
            // Find parent layout for click listener
            this.rootLayout = (LinearLayout) icon.getParent().getParent();
        }
    }
}