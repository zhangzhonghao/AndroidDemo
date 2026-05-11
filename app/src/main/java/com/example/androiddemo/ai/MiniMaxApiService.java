package com.example.androiddemo.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * MiniMax 大模型 API 服务类
 * 支持流式对话（NDJSON 格式）
 */
public class MiniMaxApiService {
    private static final String TAG = "MiniMaxApiService";

    // MiniMax API 配置
    private static final String BASE_URL = "https://api.minimaxi.com";
    private static final String CHAT_API = BASE_URL + "/v1/text/chatcompletion_v2";
    private static final String API_KEY = "sk-cp-Dxtnpbk6BMs3iNj3rUpimuF6PdxrO1XWI5ZJXNH3JMeTqDtKBWC6lcy97O4reo5DCZp1KnaF84canOQOZVSHhEx0efxN2wu6UXE0dpqL1Qsa95mQ_vgFrHI";
    private static final String MODEL = "MiniMax-M2";

    private final OkHttpClient client;
    private final Handler mainHandler;
    private Call currentCall;

    // 回调接口
    public interface StreamingCallback {
        void onStart();                          // 开始接收
        void onDelta(String text);               // 接收到新的文本片段
        void onComplete(String fullText);        // 接收完成
        void onError(String errorMessage);       // 发生错误
    }

    public MiniMaxApiService() {
        // 配置 OkHttp 客户端
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * 发送流式对话请求
     * MiniMax 虽然设置了 stream=true，但返回的 Content-Type 是 application/json，
     * 而不是 text/event-stream，所以使用普通请求读取 NDJSON 流
     * @param userMessage 用户输入的文本
     * @param callback 回调接口
     */
    public void sendStreamChat(String userMessage, StreamingCallback callback) {
        // 取消之前的请求
        cancel();

        try {
            // 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.put(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("stream", true);  // 启用流式返回

            // 构建请求
            Request request = new Request.Builder()
                    .url(CHAT_API)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(RequestBody.create(
                            MediaType.parse("application/json; charset=utf-8"),
                            requestBody.toString()))
                    .build();

            // 发送异步请求
            currentCall = client.newCall(request);
            currentCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "请求失败: " + e.getMessage());
                    if (!call.isCanceled()) {
                        mainHandler.post(() -> callback.onError("网络错误: " + e.getMessage()));
                    }
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.code() != 200) {
                        String errorMsg = "请求失败: " + response.code() + " " + response.message();
                        Log.e(TAG, errorMsg);
                        mainHandler.post(() -> callback.onError(errorMsg));
                        return;
                    }

                    mainHandler.post(() -> callback.onStart());

                    // 使用 StringBuilder 收集完整响应
                    StringBuilder fullText = new StringBuilder();

                    try {
                        // 获取响应流，按行读取 NDJSON
                        BufferedReader reader = new BufferedReader(
                                response.body().charStream());

                        String line;
                        while ((line = reader.readLine()) != null) {
                            Log.d(TAG, "收到原始数据: " + line);

                            // 跳过空行
                            if (line.trim().isEmpty()) {
                                continue;
                            }

                            // 解析 NDJSON 行
                            String text = parseNDJSONLine(line);
                            if (text != null && !text.isEmpty()) {
                                fullText.append(text);
                                final String segment = text;
                                mainHandler.post(() -> callback.onDelta(segment));
                            }

                            // 检查是否结束
                            if (isDoneLine(line)) {
                                break;
                            }
                        }

                        final String result = fullText.toString();
                        mainHandler.post(() -> callback.onComplete(result));

                    } catch (Exception e) {
                        Log.e(TAG, "读取流式响应失败: " + e.getMessage());
                        mainHandler.post(() -> callback.onError("读取响应失败: " + e.getMessage()));
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "创建请求失败: " + e.getMessage());
            callback.onError("请求创建失败: " + e.getMessage());
        }
    }

    /**
     * 解析 NDJSON 行
     * MiniMax 返回格式可能是：
     * data: {"choices":[{"delta":{"content":"文本"}}]}
     * 或者直接是 {"choices":[{"delta":{"content":"文本"}}]}
     * @param line 原始行数据
     * @return 提取的文本内容，如果解析失败或结束标记返回 null
     */
    private String parseNDJSONLine(String line) {
        try {
            // 去掉 "data: " 前缀（如果存在）
            String jsonStr = line.trim();
            if (jsonStr.startsWith("data:")) {
                jsonStr = jsonStr.substring(5).trim();
            }

            // 跳过结束标记
            if (jsonStr.isEmpty() || jsonStr.equals("[DONE]")) {
                return null;
            }

            JSONObject json = new JSONObject(jsonStr);

            // 检查是否有 choices 数组
            if (json.has("choices")) {
                JSONArray choices = json.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject choice = choices.getJSONObject(0);

                    // 尝试 delta.content 格式
                    if (choice.has("delta")) {
                        JSONObject delta = choice.getJSONObject("delta");
                        if (delta.has("content")) {
                            return delta.getString("content");
                        }
                    }

                    // 尝试 content 格式
                    if (choice.has("content")) {
                        return choice.getString("content");
                    }
                }
            }

            return null;
        } catch (Exception e) {
            Log.e(TAG, "解析 NDJSON 失败: " + e.getMessage() + ", 原始数据: " + line);
            return null;
        }
    }

    /**
     * 判断是否是结束行
     * @param line 原始行数据
     * @return 是否是结束标记
     */
    private boolean isDoneLine(String line) {
        String trimmed = line.trim();
        return trimmed.equals("[DONE]") ||
               trimmed.startsWith("data:") && trimmed.substring(5).trim().equals("[DONE]");
    }

    /**
     * 取消当前请求
     */
    public void cancel() {
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
    }
}