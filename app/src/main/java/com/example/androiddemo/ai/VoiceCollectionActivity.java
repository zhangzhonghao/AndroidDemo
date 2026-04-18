package com.example.androiddemo.ai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoiceCollectionActivity extends AppCompatActivity {

    private static final int REQUEST_AUDIO_PERMISSION = 1002;

    // Views
    private RecyclerView rvMessages;
    private EditText etInput;
    private Button btnSend;
    private Button btnKeyboard;
    private Button btnVoice;
    private Button btnVoiceRecord;
    private LinearLayout layoutInputBar;
    private LinearLayout layoutTextMode;
    private LinearLayout layoutVoiceMode;

    // Data
    private final List<Message> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    // Media
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentAudioPath;
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private long recordingStartTime;
    private Handler handler = new Handler(Looper.getMainLooper());

    // 语音模式计时
    private Runnable updateRecordingTimeRunnable;
    private TextView tvRecordingHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_collection);

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rv_messages);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        btnKeyboard = findViewById(R.id.btn_keyboard);
        btnVoice = findViewById(R.id.btn_voice);
        btnVoiceRecord = findViewById(R.id.btn_voice_record);
        layoutInputBar = findViewById(R.id.layout_input_bar);
        layoutTextMode = findViewById(R.id.layout_text_mode);
        layoutVoiceMode = findViewById(R.id.layout_voice_mode);
        tvRecordingHint = findViewById(R.id.tv_recording_hint);

        // 默认文字模式：显示键盘按钮、文本输入框、发送按钮
        setTextInputMode();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList, this::playAudio, this::stopPlaying);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        // 添加一条欢迎消息
        addAiMessage("你好！我是语音助手，可以输入文字或切换到语音输入。");
    }

    private void setupListeners() {
        // 发送文字
        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                addUserMessage(text);
                etInput.setText("");
                simulateAiResponse(text);
            }
        });

        // 点击键盘按钮 → 切换到语音输入模式
        btnKeyboard.setOnClickListener(v -> setVoiceInputMode());

        // 点击语音按钮 → 切换到文字输入模式
        btnVoice.setOnClickListener(v -> setTextInputMode());

        // 按住说话
        btnVoiceRecord.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startRecording();
                    break;
                case MotionEvent.ACTION_UP:
                    stopRecording();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    cancelRecording();
                    break;
            }
            return true;
        });
    }

    // ========== 模式切换 ==========

    // 文字输入模式：显示键盘按钮、文本输入框、发送按钮
    private void setTextInputMode() {
        layoutTextMode.setVisibility(View.VISIBLE);
        layoutVoiceMode.setVisibility(View.GONE);
        // 文字模式下显示键盘按钮
        btnKeyboard.setVisibility(View.VISIBLE);
    }

    // 语音输入模式：显示语音按钮、按住说话按钮
    private void setVoiceInputMode() {
        layoutTextMode.setVisibility(View.GONE);
        layoutVoiceMode.setVisibility(View.VISIBLE);
    }

    // ========== 权限检查 ==========

    private boolean checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "录音权限已授予，请再次点击语音按钮", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "录音权限被拒绝，无法使用语音功能", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ========== 录音相关 ==========

    private void startRecording() {
        if (!checkAudioPermission()) return;

        File audioDir = new File(getCacheDir(), "voice");
        if (!audioDir.exists()) audioDir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        currentAudioPath = new File(audioDir, "voice_" + timestamp + ".m4a").getAbsolutePath();

        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(16000);
            mediaRecorder.setAudioEncodingBitRate(64000);
            mediaRecorder.setOutputFile(currentAudioPath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            recordingStartTime = System.currentTimeMillis();

            btnVoiceRecord.setText("松开结束");
            btnVoiceRecord.setBackgroundColor(getColor(R.color.purple_700));
            tvRecordingHint.setVisibility(View.VISIBLE);
            tvRecordingHint.setText("正在录音...");

            // 更新录音时长
            updateRecordingTimeRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRecording) {
                        long seconds = (System.currentTimeMillis() - recordingStartTime) / 1000;
                        tvRecordingHint.setText("正在录音... " + seconds + "秒");
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.post(updateRecordingTimeRunnable);

        } catch (IOException | IllegalStateException e) {
            Toast.makeText(this, "录音启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            currentAudioPath = null;
        }
    }

    private void stopRecording() {
        if (!isRecording) return;

        try {
            mediaRecorder.stop();
        } catch (IllegalStateException e) {
            // 录音时间太短导致 stop 失败，视为取消
            Toast.makeText(this, "录音时间太短", Toast.LENGTH_SHORT).show();
            currentAudioPath = null;
        } finally {
            releaseRecorder();
            isRecording = false;
            handler.removeCallbacks(updateRecordingTimeRunnable);
            resetVoiceButton();

            if (currentAudioPath != null && new File(currentAudioPath).exists()) {
                long duration = (System.currentTimeMillis() - recordingStartTime) / 1000;
                if (duration < 1) {
                    Toast.makeText(this, "录音时间太短", Toast.LENGTH_SHORT).show();
                    new File(currentAudioPath).delete();
                    currentAudioPath = null;
                } else {
                    addUserVoiceMessage(currentAudioPath, (int) duration);
                    simulateAiResponse(null);
                }
            }
        }
    }

    private void cancelRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop();
            } catch (IllegalStateException ignored) {
            }
            releaseRecorder();
            isRecording = false;
            handler.removeCallbacks(updateRecordingTimeRunnable);
            resetVoiceButton();
            if (currentAudioPath != null) {
                new File(currentAudioPath).delete();
                currentAudioPath = null;
            }
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception ignored) {
            }
            mediaRecorder = null;
        }
    }

    private void resetVoiceButton() {
        btnVoiceRecord.setText("按住说话");
        btnVoiceRecord.setBackgroundColor(getColor(R.color.purple_500));
        tvRecordingHint.setVisibility(View.INVISIBLE);
        tvRecordingHint.setText("");
    }

    // ========== 音频播放 ==========

    private void playAudio(String audioPath) {
        if (isPlaying) {
            stopPlaying();
            return;
        }

        File audioFile = new File(audioPath);
        if (!audioFile.exists()) {
            Toast.makeText(this, "音频文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;

            mediaPlayer.setOnCompletionListener(mp -> {
                stopPlaying();
            });

            Toast.makeText(this, "正在播放...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "播放失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            isPlaying = false;
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception ignored) {
            }
            mediaPlayer = null;
        }
        isPlaying = false;
        adapter.notifyDataSetChanged();
    }

    // ========== 消息处理 ==========

    private void addUserMessage(String text) {
        Message msg = new Message(Message.TYPE_TEXT, true, text, null, null);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addUserVoiceMessage(String audioPath, int duration) {
        Message msg = new Message(Message.TYPE_VOICE, true, null, audioPath, duration);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addAiMessage(String text) {
        Message msg = new Message(Message.TYPE_TEXT, false, text, null, null);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addAiVoiceMessage(String audioPath, int duration) {
        Message msg = new Message(Message.TYPE_VOICE, false, null, audioPath, duration);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void simulateAiResponse(String userInput) {
        // 模拟 AI 回复（无实际 AI 能力，纯演示）
        handler.postDelayed(() -> {
            if (userInput != null) {
                addAiMessage("收到你的消息：「" + userInput + "」，AI 功能正在开发中...");
            } else {
                addAiMessage("收到你的语音，AI 语音回复功能正在开发中...");
            }
        }, 800);
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            rvMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlaying();
        if (isRecording) {
            cancelRecording();
        }
    }

    // ========== Message 数据类 ==========

    public static class Message {
        public static final int TYPE_TEXT = 0;
        public static final int TYPE_VOICE = 1;

        public final int type;        // TYPE_TEXT / TYPE_VOICE
        public final boolean isUser;  // true=用户发送，false=AI接收
        public final String text;    // 文字内容（type=TYPE_TEXT 时有值）
        public final String audioPath; // 音频路径（type=TYPE_VOICE 时有值）
        public final Integer duration; // 录音时长秒数（type=TYPE_VOICE 时有值）

        public Message(int type, boolean isUser, String text, String audioPath, Integer duration) {
            this.type = type;
            this.isUser = isUser;
            this.text = text;
            this.audioPath = audioPath;
            this.duration = duration;
        }
    }
}
