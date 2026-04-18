package com.example.androiddemo.ai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import com.example.androiddemo.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AiChatActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.minimaxi.com/v1/text/chatcompletion_v2";
    private static final String MODEL = "MiniMax-M2";
    private static final int TIMEOUT_MS = 30000;

    private RecyclerView rvMessages;
    private EditText etInput;
    private Button btnSend;

    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatMessageAdapter adapter;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        initViews();
        setupRecyclerView();
        setupListeners();

        // 添加欢迎消息
        addAiMessage("你好！我是 AI 助手，有什么可以帮你的吗？");
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        rvMessages = findViewById(R.id.rv_messages);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });
    }

    private void sendMessage(String text) {
        // 添加用户消息
        addUserMessage(text);
        etInput.setText("");

        // 调用 AI 接口
        callAiApi(text);
    }

    private void addUserMessage(String text) {
        ChatMessage msg = new ChatMessage(true, text);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void addAiMessage(String text) {
        ChatMessage msg = new ChatMessage(false, text);
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            rvMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void callAiApi(String userInput) {
        new Thread(() -> {
            try {
                // 构建请求体
                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL);

                JSONArray messages = new JSONArray();
                for (ChatMessage msg : messageList) {
                    JSONObject msgObj = new JSONObject();
                    msgObj.put("role", msg.isUser ? "user" : "assistant");
                    msgObj.put("content", msg.text);
                    messages.put(msgObj);
                }
                // 添加最新用户消息
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", userInput);
                messages.put(userMsg);

                requestBody.put("messages", messages);

                // 发送请求
                URL url = new URL(API_URL);
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
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String aiResponse = parseAiResponse(response.toString());
                    mainHandler.post(() -> addAiMessage(aiResponse));
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
                    mainHandler.post(() -> {
                        Toast.makeText(AiChatActivity.this, error, Toast.LENGTH_SHORT).show();
                        addAiMessage("抱歉，发生了错误，请稍后重试。");
                    });
                }

                conn.disconnect();

            } catch (Exception e) {
                final String errorMsg = "网络错误: " + e.getMessage();
                mainHandler.post(() -> {
                    Toast.makeText(AiChatActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    addAiMessage("抱歉，网络发生了问题，请检查网络连接后重试。");
                });
            }
        }).start();
    }

    private String parseAiResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject choices = root.getJSONArray("choices").getJSONObject(0);
            JSONObject message = choices.getJSONObject("message");
            return message.getString("content");
        } catch (Exception e) {
            return "抱歉，我无法理解你的问题，请稍后重试。";
        }
    }
}