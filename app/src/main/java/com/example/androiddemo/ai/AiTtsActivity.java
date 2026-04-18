package com.example.androiddemo.ai;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.androiddemo.R;
import com.example.androiddemo.BuildConfig;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AiTtsActivity extends AppCompatActivity {

    private static final String TTS_API_URL = "https://api.minimaxi.com/v1/text_to_speech";
    private static final int TIMEOUT_MS = 30000;

    private EditText etInput;
    private Button btnPlay;
    private Button btnPause;
    private Button btnStop;
    private SeekBar seekBar;
    private Spinner spinnerSpeed;
    private ProgressBar progressBar;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPaused = false;
    private String currentAudioPath;
    private String currentText;

    // 语速选项
    private final String[] speedOptions = {"0.5x", "1.0x", "1.5x", "2.0x"};
    private final float[] speedValues = {0.5f, 1.0f, 1.5f, 2.0f};
    private float currentSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_tts);

        initViews();
        setupListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        etInput = findViewById(R.id.et_input);
        btnPlay = findViewById(R.id.btn_play);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        seekBar = findViewById(R.id.seek_bar);
        spinnerSpeed = findViewById(R.id.spinner_speed);
        progressBar = findViewById(R.id.progress_bar);

        // 设置语速选项
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, speedOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeed.setAdapter(adapter);
        spinnerSpeed.setSelection(1); // 默认 1.0x
    }

    private void setupListeners() {
        // 语速选择
        spinnerSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSpeed = speedValues[position];
                if (mediaPlayer != null) {
                    mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(currentSpeed));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 播放按钮
        btnPlay.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "请输入要朗读的文字", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mediaPlayer != null && isPaused) {
                // 继续播放
                resumePlayback();
            } else {
                // 开始新的播放
                currentText = text;
                callTtsApi(text);
            }
        });

        // 暂停按钮
        btnPause.setOnClickListener(v -> pausePlayback());

        // 停止按钮
        btnStop.setOnClickListener(v -> stopPlayback());
    }

    private void callTtsApi(String text) {
        progressBar.setVisibility(View.VISIBLE);
        setButtonsEnabled(false);

        new Thread(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", "speech-01");
                requestBody.put("text", text);
                requestBody.put("stream", false);

                URL url = new URL(TTS_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.MINIMAX_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    // 保存音频文件
                    File audioFile = new File(getCacheDir(), "tts_audio.mp3");
                    try (InputStream is = conn.getInputStream();
                         FileOutputStream fos = new FileOutputStream(audioFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        fos.flush();
                    }

                    currentAudioPath = audioFile.getAbsolutePath();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        setButtonsEnabled(true);
                        playAudio(currentAudioPath);
                    });
                } else {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    reader.close();

                    final String error = "请求失败: " + responseCode + " - " + errorResponse;
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        setButtonsEnabled(true);
                        Toast.makeText(AiTtsActivity.this, error, Toast.LENGTH_SHORT).show();
                    });
                }

                conn.disconnect();

            } catch (Exception e) {
                final String errorMsg = "网络错误: " + e.getMessage();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    setButtonsEnabled(true);
                    Toast.makeText(AiTtsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void playAudio(String audioPath) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();

            // 设置语速
            mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(currentSpeed));

            // 设置播放完成监听
            mediaPlayer.setOnCompletionListener(mp -> {
                handler.removeCallbacksAndMessages(null);
                seekBar.setProgress(seekBar.getMax());
                isPaused = false;
                updatePauseButton();
            });

            // 设置进度更新
            seekBar.setMax(mediaPlayer.getDuration());
            updateProgress();

            mediaPlayer.start();
            isPaused = false;
            updatePauseButton();

        } catch (Exception e) {
            Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this::updateProgress, 100);
        }
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            updatePauseButton();
        }
    }

    private void resumePlayback() {
        if (mediaPlayer != null && isPaused) {
            mediaPlayer.start();
            isPaused = false;
            updatePauseButton();
            updateProgress();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = false;
            seekBar.setProgress(0);
            updatePauseButton();
        }
    }

    private void updatePauseButton() {
        if (isPaused) {
            btnPause.setText("继续");
        } else {
            btnPause.setText("暂停");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnPlay.setEnabled(enabled);
        btnPause.setEnabled(enabled);
        btnStop.setEnabled(enabled);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}