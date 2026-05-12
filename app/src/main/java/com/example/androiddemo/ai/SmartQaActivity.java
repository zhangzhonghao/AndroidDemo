package com.example.androiddemo.ai;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddemo.BuildConfig;
import com.example.androiddemo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

/**
 * 智能问答 Activity
 * 支持文本输入 + DeepSeek SSE 流式回复，以及语音输入 + SparkChain ASR
 */
public class SmartQaActivity extends AppCompatActivity {

    private static final String TAG = "SmartQaActivity";
    private static final int REQUEST_AUDIO_PERMISSION = 2001;

    // DeepSeek API 配置
    private static final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String DEEPSEEK_MODEL = "deepseek-v4-flash";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    // Views
    private RecyclerView rvMessages;
    private EditText etInput;
    private Button btnModeToggle;
    private Button btnVoiceRecord;
    private Button btnSendPause;

    // Data
    private final List<VoiceMessage> messageList = new ArrayList<>();
    private MessageAdapter adapter;

    // State
    private boolean isTextMode = true;
    private boolean isStreaming = false;
    private VoiceMessage pendingAiMessage;
    private int pendingAiPosition = -1;

    // OkHttp + SSE
    private OkHttpClient okHttpClient;
    private EventSource currentEventSource;

    // Voice recording
    private PcmAudioRecorder pcmRecorder;
    private XfyunIatService xfyunIatService;
    private String currentPcmFilePath;
    private boolean isRecordingVoice = false;
    private boolean sparkChainAvailable = true;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== Lifecycle ==========

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_qa);

        initOkHttp();
        initViews();
        setupRecyclerView();
        setupListeners();
        initSparkChain();

        // 不要欢迎消息——需求说退出清空，再进入空白
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelSse();
        if (pcmRecorder != null && pcmRecorder.isRecording()) {
            pcmRecorder.stopRecording();
        }
        if (xfyunIatService != null) {
            xfyunIatService.cancel();
        }
    }

    // ========== Init ==========

    private void initOkHttp() {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rv_messages);
        etInput = findViewById(R.id.et_input);
        btnModeToggle = findViewById(R.id.btn_mode_toggle);
        btnVoiceRecord = findViewById(R.id.btn_voice_record);
        btnSendPause = findViewById(R.id.btn_send_pause);

        // 默认文本模式
        setTextMode();
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(messageList, null, null);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
        // 禁用 ItemAnimator 防止流式抖动
        rvMessages.setItemAnimator(null);
    }

    private void setupListeners() {
        // 发送/暂停按钮
        btnSendPause.setOnClickListener(v -> {
            if (isStreaming) {
                // 场景4：流式中暂停
                cancelSse();
                onStreamFinished(false);
            } else {
                // 发送文字
                sendTextMessage();
            }
        });

        // 模式切换
        btnModeToggle.setOnClickListener(v -> {
            if (isTextMode) {
                setVoiceMode();
            } else {
                setTextMode();
            }
        });

        // 语音录音按钮（按住说话）
        btnVoiceRecord.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                // 检查网络
                if (!isNetworkAvailable()) {
                    showSnackbar("网络不可用，无法录音");
                    return true;
                }
                // 检查权限
                if (!checkAudioPermission()) {
                    return true;
                }
                // 检查 SparkChain
                if (!sparkChainAvailable) {
                    Toast.makeText(this, "语音服务暂不可用", Toast.LENGTH_SHORT).show();
                    return true;
                }
                startVoiceRecording();
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                stopVoiceRecording();
            }
            return true;
        });
    }

    private void initSparkChain() {
        pcmRecorder = new PcmAudioRecorder();
        try {
            xfyunIatService = new XfyunIatService();
        } catch (Exception e) {
            Log.e(TAG, "SparkChain 初始化失败", e);
            sparkChainAvailable = false;
            btnVoiceRecord.setEnabled(false);
            btnVoiceRecord.setAlpha(0.4f);
        }
    }

    // ========== 模式切换 ==========

    private void setTextMode() {
        isTextMode = true;
        btnModeToggle.setText("语音");
        etInput.setVisibility(View.VISIBLE);
        btnVoiceRecord.setVisibility(View.GONE);
        btnSendPause.setVisibility(View.VISIBLE);
    }

    private void setVoiceMode() {
        // 检查录音权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSION);
            return;
        }
        isTextMode = false;
        btnModeToggle.setText("键盘");
        etInput.setVisibility(View.GONE);
        btnVoiceRecord.setVisibility(View.VISIBLE);
        btnSendPause.setVisibility(View.VISIBLE);
        btnSendPause.setEnabled(false);
        btnSendPause.setText("发送");

        if (!sparkChainAvailable) {
            btnVoiceRecord.setEnabled(false);
            btnVoiceRecord.setAlpha(0.4f);
        }
    }

    // ========== 文本发送 + DeepSeek SSE ==========

    private void sendTextMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // 检查网络
        if (!isNetworkAvailable()) {
            showSnackbar("网络不可用，请检查网络连接");
            return;
        }

        // 添加用户消息
        addUserMessage(text);
        // 立即清空输入框
        etInput.setText("");

        // 进入流式状态
        startDeepSeekSse(text);
    }

    private void autoSendVoiceText(String text) {
        // 语音识别完成后自动发送
        if (TextUtils.isEmpty(text) || text.equals("未识别到文字")) {
            Toast.makeText(this, "未识别到语音内容", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isNetworkAvailable()) {
            showSnackbar("网络不可用，请检查网络连接");
            return;
        }

        addUserMessage(text);
        startDeepSeekSse(text);
    }

    private void startDeepSeekSse(String userMessage) {
        // 创建占位AI消息
        pendingAiMessage = new VoiceMessage(VoiceMessage.TYPE_TEXT, false, "...", null, null);
        messageList.add(pendingAiMessage);
        pendingAiPosition = messageList.size() - 1;
        adapter.notifyItemInserted(pendingAiPosition);
        scrollToBottom();

        // 切换按钮状态
        setStreamingState(true);

        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", DEEPSEEK_MODEL);
            requestBody.put("stream", true);

            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);
            requestBody.put("messages", messages);

            Request request = new Request.Builder()
                    .url(DEEPSEEK_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + BuildConfig.DEEPSEEK_API_KEY)
                    .post(RequestBody.create(requestBody.toString(), JSON_MEDIA_TYPE))
                    .build();

            EventSource.Factory factory = EventSources.createFactory(okHttpClient);
            currentEventSource = factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(@NonNull EventSource eventSource, String id, String type,
                                    @NonNull String data) {
                    handleSseData(data);
                }

                @Override
                public void onFailure(@NonNull EventSource eventSource, Throwable t,
                                      Response response) {
                    mainHandler.post(() -> handleSseError(t, response));
                }

                @Override
                public void onClosed(@NonNull EventSource eventSource) {
                    mainHandler.post(() -> onStreamFinished(true));
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "创建 DeepSeek 请求失败", e);
            onStreamFinished(false);
            Toast.makeText(this, "请求创建失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSseData(String data) {
        Log.d("SmartQaActivity", "SSE data: " + data.substring(0, Math.min(200, data.length())));
        // DeepSeek SSE 格式: data: {"choices":[{"delta":{"content":"xxx"}}]}
        try {
            if (data.trim().equals("[DONE]")) {
                mainHandler.post(() -> onStreamFinished(true));
                return;
            }

            JSONObject json = new JSONObject(data);

            // 先检查 HTTP 状态码（DeepSeek 可能会在 SSE 里也带这些）
            if (json.has("error")) {
                JSONObject error = json.getJSONObject("error");
                String code = error.optString("code", "");
                String msg = error.optString("message", "未知错误");
                mainHandler.post(() -> handleApiError(code, msg));
                return;
            }

            if (json.has("choices")) {
                JSONArray choices = json.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);

                    // 检查 finish_reason
                    if (choice.has("finish_reason") && !choice.isNull("finish_reason")) {
                        String finishReason = choice.getString("finish_reason");
                        if ("stop".equals(finishReason) || "length".equals(finishReason)) {
                            mainHandler.post(() -> onStreamFinished(true));
                            return;
                        }
                    }

                    // 提取 delta.content（optString 安全处理 JSON null）
                    if (choice.has("delta")) {
                        JSONObject delta = choice.getJSONObject("delta");
                        String content = delta.optString("content", "");
                        if (!content.isEmpty()) {
                            final String segment = content;
                            mainHandler.post(() -> appendAiText(segment));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析 SSE 数据失败: " + e.getMessage() + ", data=" + data);
        }
    }

    private void handleSseError(Throwable t, Response response) {
        if (response != null) {
            int code = response.code();
            if (code == 429) {
                handleApiError("429", "too_many_requests");
            } else if (code == 500 || code == 502 || code == 503) {
                handleApiError(String.valueOf(code), "server_error");
            } else {
                // 场景9：SSE 意外中断
                appendAiError("[回复中断]");
                onStreamFinished(false);
            }
        } else {
            // 场景9：网络原因中断
            appendAiError("[回复中断]");
            onStreamFinished(false);
        }
    }

    private void handleApiError(String code, String message) {
        String errorText;
        if ("429".equals(code) || "too_many_requests".equals(code)) {
            errorText = "服务繁忙，请稍后重试";
        } else if ("500".equals(code) || "502".equals(code) || "503".equals(code)
                || "server_error".equals(code)) {
            errorText = "服务异常，请稍后重试";
        } else {
            errorText = "服务错误: " + message;
        }

        // 以 AI 气泡显示错误
        if (pendingAiMessage != null && pendingAiPosition >= 0) {
            if (TextUtils.isEmpty(pendingAiMessage.text)) {
                pendingAiMessage.text = errorText;
            } else {
                pendingAiMessage.text += "\n" + errorText;
            }
            adapter.notifyItemChanged(pendingAiPosition, "text_update");
        } else {
            addAiMessage(errorText);
        }
        onStreamFinished(false);
    }

    private void appendAiText(String segment) {
        if (segment == null || segment.isEmpty() || "null".equals(segment)) return;
        if (pendingAiMessage != null) {
            pendingAiMessage.text = (pendingAiMessage.text == null ? "" : pendingAiMessage.text) + segment;
            adapter.notifyItemChanged(pendingAiPosition, "text_update");
            scrollToBottom();
        }
    }

    private void appendAiError(String text) {
        if (pendingAiMessage != null) {
            pendingAiMessage.text = (pendingAiMessage.text == null ? "" : pendingAiMessage.text) + text;
            adapter.notifyItemChanged(pendingAiPosition, "text_update");
            scrollToBottom();
        }
    }

    private void onStreamFinished(boolean completed) {
        Log.d(TAG, "流式完成: completed=" + completed);
        pendingAiMessage = null;
        pendingAiPosition = -1;
        setStreamingState(false);
        if (!isTextMode) {
            btnSendPause.setEnabled(false);
        }
    }

    private void cancelSse() {
        if (currentEventSource != null) {
            currentEventSource.cancel();
            currentEventSource = null;
        }
    }

    private void setStreamingState(boolean streaming) {
        isStreaming = streaming;
        if (streaming) {
            btnSendPause.setText("暂停");
            btnSendPause.setEnabled(true);
        } else {
            btnSendPause.setText("发送");
            btnSendPause.setEnabled(true);
        }
    }

    // ========== 语音录制 ==========

    private void startVoiceRecording() {
        if (pcmRecorder == null || pcmRecorder.isRecording()) {
            return;
        }

        currentPcmFilePath = PcmAudioRecorder.generateFilePath(getCacheDir());
        isRecordingVoice = true;

        btnVoiceRecord.setText("松开结束");
        btnVoiceRecord.setEnabled(true);

        pcmRecorder.startRecording(currentPcmFilePath, new PcmAudioRecorder.RecordCallback() {
            @Override
            public void onStart() {
                runOnUiThread(() -> Toast.makeText(SmartQaActivity.this, "开始录音", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFrameData(byte[] data, int frameIndex) {
                // 可在此更新音量指示
            }

            @Override
            public void onComplete(String filePath, int totalFrames) {
                Log.d(TAG, "录音完成: " + filePath + ", 帧数: " + totalFrames);
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(SmartQaActivity.this, "录音错误: " + errorMessage, Toast.LENGTH_LONG).show();
                    resetVoiceButton();
                });
            }
        });
    }

    private void stopVoiceRecording() {
        if (!isRecordingVoice) return;
        isRecordingVoice = false;

        if (pcmRecorder != null && pcmRecorder.isRecording()) {
            pcmRecorder.stopRecording();
        }

        btnVoiceRecord.setText("识别中...");
        btnVoiceRecord.setEnabled(false);

        // 发送到 SparkChain/Xfyun 进行语音识别
        if (currentPcmFilePath != null && xfyunIatService != null) {
            xfyunIatService.recognize(currentPcmFilePath, new XfyunIatService.IatCallback() {
                @Override
                public void onStart() {
                    Log.d(TAG, "开始语音识别");
                }

                @Override
                public void onResult(String text) {
                    runOnUiThread(() -> {
                        btnVoiceRecord.setText("识别中...");
                    });
                }

                @Override
                public void onComplete(String fullText) {
                    runOnUiThread(() -> {
                        resetVoiceButton();
                        autoSendVoiceText(fullText);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(SmartQaActivity.this,
                                "语音识别失败: " + errorMessage, Toast.LENGTH_LONG).show();
                        resetVoiceButton();
                        // SparkChain 初始化失败检测
                        sparkChainAvailable = false;
                        btnVoiceRecord.setEnabled(false);
                        btnVoiceRecord.setAlpha(0.4f);
                        Toast.makeText(SmartQaActivity.this,
                                "语音服务暂不可用，请使用文本模式", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            resetVoiceButton();
        }
    }

    private void resetVoiceButton() {
        btnVoiceRecord.setText("按住说话");
        btnVoiceRecord.setEnabled(true);
        btnVoiceRecord.setAlpha(sparkChainAvailable ? 1.0f : 0.4f);
    }

    // ========== 权限处理 ==========

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
                Toast.makeText(this, "录音权限已授予", Toast.LENGTH_SHORT).show();
                // 权限被拒绝后再授予，语音按钮恢复正常
                btnVoiceRecord.setEnabled(sparkChainAvailable);
                btnVoiceRecord.setAlpha(1.0f);
            } else {
                Toast.makeText(this, "录音权限被拒绝，无法使用语音功能", Toast.LENGTH_LONG).show();
                btnVoiceRecord.setEnabled(false);
                btnVoiceRecord.setAlpha(0.4f);
            }
        }
    }

    // ========== 网络检测 ==========

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    private void showSnackbar(String message) {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            com.google.android.material.snackbar.Snackbar.make(root, message,
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    // ========== 消息操作 ==========

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
        if (messageList.isEmpty()) return;
        rvMessages.post(() -> {
            int lastPosition = messageList.size() - 1;
            RecyclerView.ViewHolder vh = rvMessages.findViewHolderForAdapterPosition(lastPosition);
            if (vh != null) {
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
