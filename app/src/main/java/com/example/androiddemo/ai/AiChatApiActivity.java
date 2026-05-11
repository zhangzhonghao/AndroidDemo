package com.example.androiddemo.ai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.example.androiddemo.ai.MiniMaxApiService.StreamingCallback;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiChatApiActivity extends AppCompatActivity {

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
    private final List<VoiceMessage> messageList = new ArrayList<>();
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

    // MiniMax API 服务
    private MiniMaxApiService miniMaxApi;
    private VoiceMessage pendingAiMessage;  // 正在流式接收的AI消息

    // ========== MiniMax API 流式对话 ==========

    /**
     * 发送消息到 MiniMax API 并处理流式响应
     */
    private void sendToMiniMaxApi(String userMessage) {
        // 添加一条 AI 消息占位，用于后续流式更新
        pendingAiMessage = new VoiceMessage(VoiceMessage.TYPE_TEXT, false, "", null, null);
        messageList.add(pendingAiMessage);
        int position = messageList.size() - 1;
        adapter.notifyItemInserted(position);
        scrollToBottom();

        // 调用 MiniMax API 流式对话
        miniMaxApi.sendStreamChat(userMessage, new StreamingCallback() {
            @Override
            public void onStart() {
                Log.d("VoiceCollection", "开始接收 AI 流式响应");
            }

            @Override
            public void onDelta(String text) {
                // 流式更新消息内容（追加文本）
                if (pendingAiMessage != null) {
                    pendingAiMessage.text = (pendingAiMessage.text == null ? "" : pendingAiMessage.text) + text;
                    adapter.notifyItemChanged(position, "text_update");
                    // 使用 rvMessages.post() 延迟滚动到当前布局周期完成后执行
                    // 避免 notifyItemChanged 触发的测量-布局时序冲突导致抖动
                    rvMessages.post(() -> {
                        if (!messageList.isEmpty()) {
                            int lastPosition = messageList.size() - 1;
                            rvMessages.scrollToPosition(lastPosition);
                            // scrollToPosition aligns item TOP with screen TOP.
                            // After layout, scroll down to align item bottom with screen bottom.
                            rvMessages.post(() -> {
                                RecyclerView.ViewHolder vh = rvMessages.findViewHolderForAdapterPosition(lastPosition);
                                if (vh != null) {
                                    int itemBottom = vh.itemView.getBottom();
                                    int recyclerBottom = rvMessages.getBottom();
                                    int delta = itemBottom - recyclerBottom;
                                    if (delta > 0) {
                                        rvMessages.scrollBy(0, delta);
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onComplete(String fullText) {
                Log.d("VoiceCollection", "AI 响应完成: " + fullText);
                pendingAiMessage = null;
            }

            @Override
            public void onError(String errorMessage) {
                // 显示错误消息
                if (pendingAiMessage != null) {
                    pendingAiMessage.text = "抱歉，发生了错误：" + errorMessage;
                    adapter.notifyItemChanged(position, "text_update");
                    scrollToBottom();
                    pendingAiMessage = null;
                }
                Toast.makeText(AiChatApiActivity.this, "AI 响应错误: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 处理语音消息（实际项目中应先进行语音转文字再发送）
     * 暂时模拟回复，后续可接入语音识别和TTS
     */
    private void handleVoiceMessage() {
        // 暂时模拟 AI 回复
        handler.postDelayed(() -> {
            addAiMessage("收到你的语音，AI 语音回复功能正在开发中...");
        }, 800);
    }

    // ========== 讯飞语音识别 (IAT) ==========

    private XfyunIatService xfyunIatService;

    /**
     * 开始语音识别流程
     * 1. 将 M4A 转换为 PCM
     * 2. 调用讯飞 IAT 进行语音转文字
     * 3. 转写完成后调用 sendToMiniMaxApi() 进行 AI 对话
     */
    private void startVoiceRecognition(String m4aPath) {
        // 显示识别中提示
        Toast.makeText(this, "正在识别语音...", Toast.LENGTH_SHORT).show();

        // 初始化讯飞 IAT 服务
        if (xfyunIatService == null) {
            xfyunIatService = new XfyunIatService();
        }

        // 创建 PCM 文件路径
        File audioDir = new File(getCacheDir(), "voice");
        if (!audioDir.exists()) audioDir.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String pcmPath = new File(audioDir, "voice_" + timestamp + ".pcm").getAbsolutePath();

        // 将 M4A 转换为 PCM
        XfyunIatService.convertM4aToPcm(m4aPath, pcmPath, new XfyunIatService.ConversionCallback() {
            @Override
            public void onComplete(String pcmPath) {
                Log.d("VoiceCollection", "PCM 转换完成: " + pcmPath);
                // 开始讯飞语音识别
                recognizeWithXfyun(pcmPath);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("VoiceCollection", "PCM 转换失败: " + errorMessage);
                Toast.makeText(AiChatApiActivity.this, "语音转换失败: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * 调用讯飞 IAT 进行语音识别
     */
    private void recognizeWithXfyun(String pcmPath) {
        xfyunIatService.recognize(pcmPath, new XfyunIatService.IatCallback() {
            @Override
            public void onStart() {
                Log.d("VoiceCollection", "开始讯飞语音识别");
            }

            @Override
            public void onResult(String text) {
                // 部分识别结果，可以更新 UI
                Log.d("VoiceCollection", "部分识别结果: " + text);
            }

            @Override
            public void onComplete(String fullText) {
                Log.d("VoiceCollection", "识别完成，最终文字: " + fullText);

                // 删除临时 PCM 文件
                new File(pcmPath).delete();

                // 转写完成后，显示为文字气泡并调用 sendToMiniMaxApi() 进行 AI 对话
                if (fullText != null && !fullText.trim().isEmpty() && !fullText.equals("未识别到文字")) {
                    // 手动输入使用 trim()，语音输入也应保持一致
                    String trimmedText = fullText.trim();
                    // 添加用户文字消息（和打字输入一样）
                    addUserMessage(trimmedText);
                    // 调用 AI 对话
                    sendToMiniMaxApi(trimmedText);
                } else {
                    Toast.makeText(AiChatApiActivity.this, "未识别到语音内容", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("VoiceCollection", "讯飞识别错误: " + errorMessage);
                Toast.makeText(AiChatApiActivity.this, "语音识别失败: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // 初始化 MiniMax API 服务
        miniMaxApi = new MiniMaxApiService();

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
        // 禁用 ItemAnimator，避免动画导致的中间布局状态
        rvMessages.setItemAnimator(null);

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
                sendToMiniMaxApi(text);
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
                    // 语音识别后自动触发 AI 对话
                    startVoiceRecognition(currentAudioPath);
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
        VoiceMessage msg = new VoiceMessage(VoiceMessage.TYPE_TEXT, true, text, null, null);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addUserVoiceMessage(String audioPath, int duration) {
        VoiceMessage msg = new VoiceMessage(VoiceMessage.TYPE_VOICE, true, null, audioPath, duration);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addAiMessage(String text) {
        VoiceMessage msg = new VoiceMessage(VoiceMessage.TYPE_TEXT, false, text, null, null);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addAiVoiceMessage(String audioPath, int duration) {
        VoiceMessage msg = new VoiceMessage(VoiceMessage.TYPE_VOICE, false, null, audioPath, duration);
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
            rvMessages.post(() -> {
                int lastPosition = messageList.size() - 1;
                RecyclerView.ViewHolder vh = rvMessages.findViewHolderForAdapterPosition(lastPosition);
                if (vh != null) {
                    // scrollToPosition aligns item TOP with screen TOP.
                    // When item height < screen height, item bottom is above screen bottom.
                    // We need to scroll down to align item bottom with screen bottom.
                    int itemBottom = vh.itemView.getBottom();
                    int recyclerBottom = rvMessages.getBottom();
                    int delta = itemBottom - recyclerBottom;
                    if (delta > 0) {
                        rvMessages.scrollBy(0, delta);
                    }
                } else {
                    rvMessages.smoothScrollToPosition(lastPosition);
                }
            });
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
}
