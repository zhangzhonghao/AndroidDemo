package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Locale;

public class CountdownChallengeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText etNumber;
    private TextView tvCurrentNumber;
    private TextView tvProgress;
    private Button btnStart;
    private Button btnPause;
    private Button btnStop;

    private TextToSpeech tts;
    private Handler handler = new Handler(Looper.getMainLooper());

    private int targetNumber = 0;
    private int currentNumber = 0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    private static final int COUNTDOWN_INTERVAL = 1000; // 1 second per number

    private Runnable countdownRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning && !isPaused) {
                if (currentNumber > 0) {
                    tvCurrentNumber.setText(String.valueOf(currentNumber));
                    tvProgress.setText(String.format(Locale.getDefault(), "还剩 %d 个数字", currentNumber));

                    // Speak the current number
                    speakNumber(currentNumber);

                    currentNumber--;
                    handler.postDelayed(this, COUNTDOWN_INTERVAL);
                } else {
                    // Countdown finished
                    onCountdownFinished();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_countdown_challenge);

        // Initialize TTS
        tts = new TextToSpeech(this, this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etNumber = findViewById(R.id.et_number);
        tvCurrentNumber = findViewById(R.id.tv_current_number);
        tvProgress = findViewById(R.id.tv_progress);
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
    }

    private void setupListeners() {
        btnStart.setOnClickListener(v -> startCountdown());
        btnPause.setOnClickListener(v -> togglePause());
        btnStop.setOnClickListener(v -> stopCountdown());
    }

    private void startCountdown() {
        String input = etNumber.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(this, "请输入数字", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            targetNumber = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetNumber < 1 || targetNumber > 100) {
            Toast.makeText(this, "数字范围: 1-100", Toast.LENGTH_SHORT).show();
            return;
        }

        // Start countdown
        currentNumber = targetNumber;
        isRunning = true;
        isPaused = false;

        // Disable input
        etNumber.setEnabled(false);

        // Update UI
        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
        btnStop.setEnabled(true);
        btnPause.setText("暂停");

        // Start the countdown
        handler.post(countdownRunnable);
    }

    private void togglePause() {
        if (!isRunning) return;

        isPaused = !isPaused;

        if (isPaused) {
            btnPause.setText("继续");
            tvProgress.setText("已暂停");
        } else {
            btnPause.setText("暂停");
            handler.post(countdownRunnable);
        }
    }

    private void stopCountdown() {
        isRunning = false;
        isPaused = false;
        handler.removeCallbacks(countdownRunnable);

        // Reset UI
        tvCurrentNumber.setText("");
        tvProgress.setText("已停止");
        etNumber.setEnabled(true);
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);
        btnPause.setText("暂停");
    }

    private void onCountdownFinished() {
        isRunning = false;
        isPaused = false;
        handler.removeCallbacks(countdownRunnable);

        // Speak completion
        tts.speak("挑战完成！", TextToSpeech.QUEUE_FLUSH, null, "finish");

        // Reset UI
        tvCurrentNumber.setText("");
        tvProgress.setText("挑战完成！");
        etNumber.setEnabled(true);
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);

        Toast.makeText(this, "恭喜完成挑战！", Toast.LENGTH_LONG).show();
    }

    private void speakNumber(int number) {
        if (tts != null && tts.isLanguageAvailable(Locale.CHINESE) >= 0) {
            tts.speak(String.valueOf(number), TextToSpeech.QUEUE_FLUSH, null, "num_" + number);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English
                tts.setLanguage(Locale.US);
            }
        } else {
            Toast.makeText(this, "TTS 初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(countdownRunnable);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}