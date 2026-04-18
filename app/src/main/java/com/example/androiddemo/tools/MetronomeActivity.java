package com.example.androiddemo.tools;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.Map;

public class MetronomeActivity extends AppCompatActivity {

    // BPM settings
    private static final int MIN_BPM = 20;
    private static final int MAX_BPM = 300;
    private static final int DEFAULT_BPM = 120;

    // Time signatures (beats per measure)
    private static final Map<Integer, Integer> TIME_SIGNATURES = new HashMap<>();
    static {
        TIME_SIGNATURES.put(R.id.rb_2_4, 2);
        TIME_SIGNATURES.put(R.id.rb_3_4, 3);
        TIME_SIGNATURES.put(R.id.rb_4_4, 4);
        TIME_SIGNATURES.put(R.id.rb_6_8, 6);
    }

    // Tempo change types
    private static final int TEMPO_NONE = 0;
    private static final int TEMPO_RIT = 1;      // Ritardando - slowing down
    private static final int TEMPO_ACC = 2;      // Accelerando - speeding up

    // Views
    private TextView tvBpm;
    private TextView tvBeatCount;
    private SeekBar seekbarBpm;
    private View beatIndicator;
    private Button btnStartStop;
    private Button btnTapTempo;
    private Button btnBpmMinus;
    private Button btnBpmPlus;
    private RadioGroup timeSignatureGroup;
    private RadioGroup tempoChangeGroup;

    // Metronome state
    private int currentBpm = DEFAULT_BPM;
    private int currentBeat = 1;
    private int beatsPerMeasure = 4;
    private int tempoChangeType = TEMPO_NONE;

    // Timing
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private long beatInterval;  // milliseconds between beats

    // Tap tempo
    private long lastTapTime = 0;
    private static final int TAP_TIMEOUT = 2000; // Reset after 2 seconds of no taps

    // Sound
    private ToneGenerator toneGenerator;

    // Original BPM for rit/acc calculations
    private int originalBpm;

    private Runnable beatRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                playBeat();
                scheduleNextBeat();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_metronome);

        initViews();
        initToneGenerator();
        setupListeners();
        updateBpmDisplay();
    }

    private void initViews() {
        tvBpm = findViewById(R.id.tv_bpm);
        tvBeatCount = findViewById(R.id.tv_beat_count);
        seekbarBpm = findViewById(R.id.seekbar_bpm);
        beatIndicator = findViewById(R.id.beat_indicator);
        btnStartStop = findViewById(R.id.btn_start_stop);
        btnTapTempo = findViewById(R.id.btn_tap_tempo);
        btnBpmMinus = findViewById(R.id.btn_bpm_minus);
        btnBpmPlus = findViewById(R.id.btn_bpm_plus);
        timeSignatureGroup = findViewById(R.id.time_signature_group);
        tempoChangeGroup = findViewById(R.id.tempo_change_group);

        seekbarBpm.setMax(MAX_BPM - MIN_BPM);
        seekbarBpm.setProgress(DEFAULT_BPM - MIN_BPM);
    }

    private void initToneGenerator() {
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    private void setupListeners() {
        // BPM control buttons
        btnBpmMinus.setOnClickListener(v -> {
            if (currentBpm > MIN_BPM) {
                currentBpm--;
                updateBpmDisplay();
            }
        });

        btnBpmPlus.setOnClickListener(v -> {
            if (currentBpm < MAX_BPM) {
                currentBpm++;
                updateBpmDisplay();
            }
        });

        // BPM SeekBar
        seekbarBpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentBpm = progress + MIN_BPM;
                    updateBpmDisplay();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Time signature selection
        timeSignatureGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Integer beats = TIME_SIGNATURES.get(checkedId);
            if (beats != null) {
                beatsPerMeasure = beats;
                currentBeat = 1;
                updateBeatCountDisplay();
            }
        });

        // Tempo change selection
        tempoChangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_rit) {
                tempoChangeType = TEMPO_RIT;
            } else if (checkedId == R.id.rb_acc) {
                tempoChangeType = TEMPO_ACC;
            } else {
                tempoChangeType = TEMPO_NONE;
            }
            originalBpm = currentBpm;
        });

        // Start/Stop button
        btnStartStop.setOnClickListener(v -> {
            if (isRunning) {
                stopMetronome();
            } else {
                startMetronome();
            }
        });

        // Tap Tempo button
        btnTapTempo.setOnClickListener(v -> handleTapTempo());
    }

    private void updateBpmDisplay() {
        tvBpm.setText(String.valueOf(currentBpm));
        if (!isRunning) {
            originalBpm = currentBpm;
        }
    }

    private void updateBeatCountDisplay() {
        tvBeatCount.setText(String.format("节拍: %d/%d", currentBeat, beatsPerMeasure));
    }

    private void startMetronome() {
        isRunning = true;
        currentBeat = 1;
        originalBpm = currentBpm;
        btnStartStop.setText("停止");
        btnTapTempo.setEnabled(false);

        // Enable/disable controls during playback
        seekbarBpm.setEnabled(false);
        btnBpmMinus.setEnabled(false);
        btnBpmPlus.setEnabled(false);
        timeSignatureGroup.setEnabled(false);

        scheduleNextBeat();
    }

    private void stopMetronome() {
        isRunning = false;
        handler.removeCallbacks(beatRunnable);
        btnStartStop.setText("开始");
        btnTapTempo.setEnabled(true);

        // Re-enable controls
        seekbarBpm.setEnabled(true);
        btnBpmMinus.setEnabled(true);
        btnBpmPlus.setEnabled(true);
        timeSignatureGroup.setEnabled(true);

        // Reset beat indicator
        beatIndicator.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_button_background));

        currentBeat = 1;
        updateBeatCountDisplay();

        // Reset BPM if rit/acc was used
        if (tempoChangeType != TEMPO_NONE) {
            currentBpm = originalBpm;
            updateBpmDisplay();
            seekbarBpm.setProgress(currentBpm - MIN_BPM);
        }
    }

    private void scheduleNextBeat() {
        beatInterval = (long) (60000.0 / currentBpm);
        handler.postDelayed(beatRunnable, beatInterval);
    }

    private void playBeat() {
        // Play sound using ToneGenerator
        if (toneGenerator != null) {
            if (currentBeat == 1) {
                // Accent on first beat - higher tone
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
            } else {
                // Normal beat - lower tone
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 30);
            }
        }

        // Visual feedback
        flashBeatIndicator();

        // Update beat count display
        updateBeatCountDisplay();

        // Advance beat counter
        currentBeat++;
        if (currentBeat > beatsPerMeasure) {
            currentBeat = 1;

            // Apply tempo change at the end of each measure
            applyTempoChange();
        }
    }

    private void flashBeatIndicator() {
        // Different colors for accent beat vs normal beat
        int color = (currentBeat == 1) ?
                ContextCompat.getColor(this, R.color.primary) :
                ContextCompat.getColor(this, android.R.color.white);

        beatIndicator.setBackgroundColor(color);

        // Reset after a short delay
        handler.postDelayed(() -> {
            if (isRunning) {
                beatIndicator.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_button_background));
            }
        }, 100);
    }

    private void applyTempoChange() {
        if (tempoChangeType == TEMPO_RIT) {
            // Slow down by 1 BPM every 4 measures
            if (currentBpm > MIN_BPM) {
                currentBpm--;
                updateBpmDisplay();
                seekbarBpm.setProgress(currentBpm - MIN_BPM);
            }
        } else if (tempoChangeType == TEMPO_ACC) {
            // Speed up by 1 BPM every 4 measures
            if (currentBpm < MAX_BPM) {
                currentBpm++;
                updateBpmDisplay();
                seekbarBpm.setProgress(currentBpm - MIN_BPM);
            }
        }
    }

    private void handleTapTempo() {
        long currentTime = System.currentTimeMillis();

        // Reset if too much time has passed since last tap
        if (lastTapTime > 0 && (currentTime - lastTapTime) > TAP_TIMEOUT) {
            lastTapTime = currentTime;
            return;
        }

        // Calculate BPM from tap intervals
        if (lastTapTime > 0) {
            long interval = currentTime - lastTapTime;
            int tappedBpm = (int) Math.round(60000.0 / interval);

            // Clamp to valid range
            if (tappedBpm >= MIN_BPM && tappedBpm <= MAX_BPM) {
                currentBpm = tappedBpm;
                originalBpm = currentBpm;
                updateBpmDisplay();
                seekbarBpm.setProgress(currentBpm - MIN_BPM);
            }
        }

        lastTapTime = currentTime;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacks(beatRunnable);
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }
}