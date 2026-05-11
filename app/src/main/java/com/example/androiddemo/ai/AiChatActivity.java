package com.example.androiddemo.ai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.androiddemo.ai.PcmAudioRecorder;
import com.example.androiddemo.ai.XfyunIatService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AiChatActivity extends AppCompatActivity {

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
    private TextView tvRecordingHint;

    // Data
    private final List<VoiceMessage> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    // Media
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    // MiniMax API 服务
    private MiniMaxApiService miniMaxApi;
    private VoiceMessage pendingAiMessage;  // 正在流式接收的AI消息

    // 语音录制
    private PcmAudioRecorder pcmRecorder;
    private String currentPcmFilePath;
    private boolean isRecordingVoice = false;

    // 讯飞语音识别
    private XfyunIatService xfyunIatService;

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
                Toast.makeText(AiChatActivity.this, "AI 响应错误: " + errorMessage, Toast.LENGTH_LONG).show();
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

        // 初始化语音录制和识别服务
        pcmRecorder = new PcmAudioRecorder();
        xfyunIatService = new XfyunIatService();
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

        // 按住说话 - 语音录制与识别
        btnVoiceRecord.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 检查录音权限
                    if (!checkAudioPermission()) {
                        return true;
                    }
                    // 开始录音
                    startVoiceRecording();
                    break;
                case MotionEvent.ACTION_UP:
                    // 停止录音并识别
                    stopVoiceRecording();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // 取消录音
                    cancelVoiceRecording();
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

    // ========== 语音录制与识别 ==========

    private void startVoiceRecording() {
        if (pcmRecorder.isRecording()) {
            pcmRecorder.stopRecording();
        }

        // 生成 PCM 文件路径
        currentPcmFilePath = PcmAudioRecorder.generateFilePath(getCacheDir());

        // 更新 UI
        tvRecordingHint.setText("正在录音...");
        btnVoiceRecord.setText("松开结束");
        isRecordingVoice = true;

        pcmRecorder.startRecording(currentPcmFilePath, new PcmAudioRecorder.RecordCallback() {
            @Override
            public void onStart() {
                runOnUiThread(() -> {
                    Toast.makeText(AiChatActivity.this, "开始录音", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFrameData(byte[] data, int frameIndex) {
                // 可以在这里更新音量 UI
            }

            @Override
            public void onComplete(String filePath, int totalFrames) {
                Log.d("VoiceCollection", "录音完成: " + filePath + ", 共 " + totalFrames + " 帧");
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(AiChatActivity.this, "录音错误: " + errorMessage, Toast.LENGTH_LONG).show();
                    resetVoiceButton();
                });
            }
        });
    }

    private void stopVoiceRecording() {
        if (!isRecordingVoice) return;

        isRecordingVoice = false;
        pcmRecorder.stopRecording();

        // 更新 UI
        tvRecordingHint.setText("正在识别...");
        btnVoiceRecord.setText("识别中...");
        btnVoiceRecord.setEnabled(false);

        // 发送到讯飞进行语音识别
        if (currentPcmFilePath != null) {
            xfyunIatService.recognize(currentPcmFilePath, new XfyunIatService.IatCallback() {
                @Override
                public void onStart() {
                    Log.d("VoiceCollection", "开始语音识别");
                }

                @Override
                public void onResult(String text) {
                    // 部分结果，可以实时显示
                    runOnUiThread(() -> {
                        tvRecordingHint.setText("识别中: " + text);
                    });
                }

                @Override
                public void onComplete(String fullText) {
                    runOnUiThread(() -> {
                        if (fullText != null && !fullText.isEmpty() && !"未识别到文字".equals(fullText)) {
                            Toast.makeText(AiChatActivity.this, "识别结果: " + fullText, Toast.LENGTH_SHORT).show();
                            // 自动发送识别结果
                            addUserMessage(fullText);
                            sendToMiniMaxApi(fullText);
                        } else {
                            Toast.makeText(AiChatActivity.this, "未识别到语音内容", Toast.LENGTH_SHORT).show();
                        }
                        resetVoiceButton();
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(AiChatActivity.this, "语音识别失败: " + errorMessage, Toast.LENGTH_LONG).show();
                        resetVoiceButton();
                    });
                }
            });
        } else {
            resetVoiceButton();
        }
    }

    private void cancelVoiceRecording() {
        isRecordingVoice = false;
        if (pcmRecorder != null) {
            pcmRecorder.stopRecording();
        }
        resetVoiceButton();
    }

    private void resetVoiceButton() {
        tvRecordingHint.setText("按住说话");
        btnVoiceRecord.setText("按住说话");
        btnVoiceRecord.setEnabled(true);
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

    private void addAiMessage(String text) {
        VoiceMessage msg = new VoiceMessage(VoiceMessage.TYPE_TEXT, false, text, null, null);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
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
        if (pcmRecorder != null && pcmRecorder.isRecording()) {
            pcmRecorder.stopRecording();
        }
        if (xfyunIatService != null) {
            xfyunIatService.cancel();
        }
    }
}
