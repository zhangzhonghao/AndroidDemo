package com.example.androiddemo.ai;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.*;
import okio.ByteString;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * 科大讯飞语音听写(IAT)服务
 * 使用 WebSocket 协议实时传输音频并获取识别结果
 */
public class XfyunIatService {
    private static final String TAG = "XfyunIatService";

    // 讯飞 IAT WebSocket 地址
    private static final String IAT_URL = "wss://iat-api.xfyun.cn/v2/iat";

    // 讯飞应用配置
    private static final String APP_ID = "6ddfba69";
    private static final String API_KEY = "0184ea607ba31353f9fefa328b05b85c";
    private static final String API_SECRET = "ZjM3OGQ4ZTE0NDY0MzkxMzZiODc3NjQx";

    // 音频参数：16k采样率，16bit采样深度，单声道
    private static final int SAMPLE_RATE = 16000;
    private static final int FRAME_SIZE = 1280;  // 40ms @ 16kHz = 16000 * 0.04 * 2 bytes

    private final OkHttpClient client;
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private WebSocket webSocket;
    private boolean isConnected = false;

    // 回调接口
    public interface IatCallback {
        void onStart();                            // 开始识别
        void onResult(String text);                // 接收到识别结果（可能是部分结果）
        void onComplete(String fullText);          // 识别完成，返回完整文字
        void onError(String errorMessage);        // 发生错误
    }

    private IatCallback callback;
    private StringBuilder fullResultText = new StringBuilder();

    public XfyunIatService() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 识别音频文件（PCM格式）
     * @param pcmFilePath PCM 文件路径
     * @param callback 回调接口
     */
    public void recognize(String pcmFilePath, IatCallback callback) {
        this.callback = callback;
        this.fullResultText = new StringBuilder();

        File pcmFile = new File(pcmFilePath);
        if (!pcmFile.exists()) {
            mainHandler.post(() -> callback.onError("音频文件不存在: " + pcmFilePath));
            return;
        }

        // 生成鉴权 URL
        String authUrl;
        try {
            authUrl = assembleAuthUrl(IAT_URL, API_KEY, API_SECRET);
        } catch (Exception e) {
            mainHandler.post(() -> callback.onError("生成鉴权URL失败: " + e.getMessage()));
            return;
        }

        // 构建 WebSocket 请求
        Request request = new Request.Builder()
                .url(authUrl)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                Log.d(TAG, "WebSocket 连接成功");
                isConnected = true;
                mainHandler.post(() -> callback.onStart());

                // 连接建立后，发送音频数据
                sendAudioData(pcmFilePath);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                Log.d(TAG, "收到消息: " + text);
                handleIatResult(text);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
                Log.d(TAG, "收到二进制消息: " + bytes.hex());
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                Log.e(TAG, "WebSocket 失败: " + t.getMessage());
                isConnected = false;
                mainHandler.post(() -> callback.onError("网络错误: " + t.getMessage()));
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                Log.d(TAG, "WebSocket 关闭: code=" + code + ", reason=" + reason);
                isConnected = false;
            }
        });
    }

    /**
     * 发送音频数据
     * @param pcmFilePath PCM 文件路径
     */
    private void sendAudioData(String pcmFilePath) {
        new Thread(() -> {
            try {
                FileInputStream fis = new FileInputStream(pcmFilePath);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[FRAME_SIZE];
                int bytesRead;

                // 按帧发送音频数据，每帧 40ms
                while ((bytesRead = fis.read(chunk)) != -1) {
                    if (!isConnected) {
                        break;
                    }

                    if (bytesRead == FRAME_SIZE) {
                        // 发送完整帧
                        sendFrame(chunk);
                    } else if (bytesRead > 0) {
                        // 发送最后一帧（不完整）
                        byte[] lastFrame = new byte[bytesRead];
                        System.arraycopy(chunk, 0, lastFrame, 0, bytesRead);
                        sendFrame(lastFrame);
                    }

                    // 每帧间隔 40ms
                    Thread.sleep(40);
                }

                fis.close();

                // 发送结束标识
                sendEndSignal();
                Log.d(TAG, "音频数据发送完成");

            } catch (Exception e) {
                Log.e(TAG, "发送音频数据失败: " + e.getMessage());
                mainHandler.post(() -> callback.onError("发送音频失败: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * 发送一帧音频数据
     */
    private void sendFrame(byte[] frameData) {
        if (webSocket != null && isConnected) {
            // 讯飞 IAT 协议：帧数据需要进行 Base64 编码
            String base64Data = Base64.encodeToString(frameData, Base64.NO_WRAP);
            String frameJson = buildFrameJson(base64Data, "continue");
            webSocket.send(frameJson);
        }
    }

    /**
     * 发送结束信号
     */
    private void sendEndSignal() {
        if (webSocket != null && isConnected) {
            // 发送结束帧
            String endJson = buildFrameJson("", "last");
            webSocket.send(endJson);
            Log.d(TAG, "已发送结束标识");
        }
    }

    /**
     * 构建讯飞 IAT 协议帧
     * @param data Base64 编码的音频数据
     * @param status 帧状态：first/continue/last
     * @return JSON 格式的帧数据
     */
    private String buildFrameJson(String data, String status) {
        // 讯飞 IAT v2 协议帧格式
        // status: 0=第一帧，1=中间帧，2=最后一帧
        int statusCode;
        switch (status) {
            case "first":
                statusCode = 0;
                break;
            case "last":
                statusCode = 2;
                break;
            default:
                statusCode = 1;
                break;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"common\":{");
        sb.append("\"app_id\":\"").append(APP_ID).append("\"");
        sb.append("},");
        sb.append("\"business\":{");
        sb.append("\"language\":\"zh_cn\",");
        sb.append("\"domain\":\"iat\",");
        sb.append("\"accent\":\"mandarin\",");
        sb.append("\"sample_rate\":\"16000\"");
        sb.append("},");
        sb.append("\"data\":{");
        sb.append("\"status\":").append(statusCode).append(",");
        sb.append("\"audio\":\"").append(data).append("\"");
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 处理识别结果
     * @param text JSON 格式的识别结果
     */
    private void handleIatResult(String text) {
        try {
            // 解析讯飞 IAT 响应
            // 格式: {"code":0,"data":{"status":2,"result":{"ws":[{"cw":[{"w":"文字"}]}],"ls":true}}}
            JSONObject json = new JSONObject(text);
            int code = json.getInt("code");

            if (code != 0) {
                String desc = json.getString("desc");
                mainHandler.post(() -> callback.onError("识别错误: " + desc));
                return;
            }

            JSONObject data = json.getJSONObject("data");
            if (data == null) {
                return;
            }

            int status = data.getInt("status");

            JSONObject result = data.optJSONObject("result");
            if (result != null) {
                // 解析 ws 数组，每个元素包含 cw 数组，cw 中每个元素有 w 字段
                JSONArray wsArray = result.optJSONArray("ws");
                if (wsArray != null && wsArray.length() > 0) {
                    StringBuilder textBuilder = new StringBuilder();
                    for (int i = 0; i < wsArray.length(); i++) {
                        JSONObject wsItem = wsArray.getJSONObject(i);
                        JSONArray cwArray = wsItem.optJSONArray("cw");
                        if (cwArray != null) {
                            for (int j = 0; j < cwArray.length(); j++) {
                                JSONObject cwItem = cwArray.getJSONObject(j);
                                String word = cwItem.optString("w");
                                if (word != null && !word.isEmpty()) {
                                    textBuilder.append(word);
                                }
                            }
                        }
                    }
                    String recognizedText = textBuilder.toString();
                    if (!recognizedText.isEmpty()) {
                        fullResultText.append(recognizedText);
                        mainHandler.post(() -> callback.onResult(recognizedText));
                    }
                }
            }

            // status=2 表示识别完成
            if (status == 2) {
                final String finalText = fullResultText.toString();
                mainHandler.post(() -> callback.onComplete(finalText.isEmpty() ? "未识别到文字" : finalText));
            }

        } catch (Exception e) {
            Log.e(TAG, "解析识别结果失败: " + e.getMessage());
        }
    }

    /**
     * 取消识别
     */
    public void cancel() {
        if (webSocket != null) {
            webSocket.close(1000, "用户取消");
            webSocket = null;
            isConnected = false;
        }
    }

    /**
     * 生成讯飞鉴权 URL
     * 参考讯飞文档 assembleAuthUrl 逻辑
     * @param url 原始 WebSocket URL
     * @param apiKey API Key
     * @param apiSecret API Secret
     * @return 鉴权后的 URL
     * @throws Exception 签名计算异常
     */
    private String assembleAuthUrl(String url, String apiKey, String apiSecret) throws Exception {
        // 生成 RFC1123 格式的日期
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        // 构建待签名字符串
        // host + date + request-line (使用 \n 连接)
        String host = "iat-api.xfyun.cn";
        String requestLine = "GET /v2/iat HTTP/1.1";

        String signatureOrigin = "host: " + host + "\n" +
                "date: " + date + "\n" +
                requestLine;

        // 使用 HMAC-SHA256 计算签名
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(spec);
        byte[] signatureBytes = mac.doFinal(signatureOrigin.getBytes(StandardCharsets.UTF_8));

        // Base64 编码签名
        String signature = Base64.encodeToString(signatureBytes, Base64.NO_WRAP);

        // 构建 authorization 签名信息
        // 讯飞要求的格式: api_key="$api_key", algorithm="hmac-sha256", headers="host date request-line", signature="$signature"
        String authorizationOrigin = "api_key=\"" + apiKey + "\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"" + signature + "\"";

        // Base64 编码 authorization
        String authorization = Base64.encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

        // 拼接鉴权 URL
        // wss://iat-api.xfyun.cn/v2/iat?authorization=xxx&date=xxx&host=xxx
        String authUrl = url + "?" +
                "authorization=" + URLEncoder.encode(authorization, "UTF-8") + "&" +
                "date=" + URLEncoder.encode(date, "UTF-8") + "&" +
                "host=" + URLEncoder.encode(host, "UTF-8");

        Log.d(TAG, "鉴权 URL 生成完成");
        return authUrl;
    }

    /**
     * 将 M4A 音频文件转换为 PCM 文件
     * 使用 Android MediaDecoder 进行解码
     * @param m4aPath M4A 文件路径
     * @param pcmPath 输出 PCM 文件路径
     * @param callback 转换完成回调
     */
    public static void convertM4aToPcm(String m4aPath, String pcmPath, ConversionCallback callback) {
        new Thread(() -> {
            try {
                android.media.MediaExtractor extractor = new android.media.MediaExtractor();
                extractor.setDataSource(m4aPath);

                int audioTrackIndex = -1;
                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio/")) {
                        audioTrackIndex = i;
                        break;
                    }
                }

                if (audioTrackIndex == -1) {
                    mainHandler.post(() -> callback.onError("未找到音频轨道"));
                    extractor.release();
                    return;
                }

                extractor.selectTrack(audioTrackIndex);
                MediaFormat inputFormat = extractor.getTrackFormat(audioTrackIndex);

                int sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                int channelCount = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);

                // 创建解码器
                String mime = inputFormat.getString(MediaFormat.KEY_MIME);
                MediaCodec decoder = MediaCodec.createDecoderByType(mime);
                decoder.configure(inputFormat, null, null, 0);
                decoder.start();

                // 创建 PCM 输出文件
                java.io.FileOutputStream pcmOut = new java.io.FileOutputStream(pcmPath);

                android.media.MediaCodec.BufferInfo bufferInfo = new android.media.MediaCodec.BufferInfo();
                boolean isInputDone = false;
                boolean isOutputDone = false;

                ByteBuffer[] inputBuffers = decoder.getInputBuffers();
                ByteBuffer[] outputBuffers = decoder.getOutputBuffers();

                while (!isOutputDone) {
                    // 填充输入数据
                    if (!isInputDone) {
                        int inputBufferIndex = decoder.dequeueInputBuffer(10000);
                        if (inputBufferIndex >= 0) {
                            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);

                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                isInputDone = true;
                            } else {
                                long presentationTime = extractor.getSampleTime();
                                decoder.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTime, 0);
                                extractor.advance();
                            }
                        }
                    }

                    // 获取输出数据
                    int outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputBufferIndex >= 0) {
                        if (bufferInfo.flags == android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            isOutputDone = true;
                        } else {
                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                            byte[] chunk = new byte[bufferInfo.size];
                            outputBuffer.get(chunk);
                            pcmOut.write(chunk);
                        }
                        decoder.releaseOutputBuffer(outputBufferIndex, false);
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = decoder.getOutputBuffers();
                    }
                }

                pcmOut.close();
                decoder.stop();
                decoder.release();
                extractor.release();

                mainHandler.post(() -> callback.onComplete(pcmPath));

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("转换失败: " + e.getMessage()));
            }
        }).start();
    }

    public interface ConversionCallback {
        void onComplete(String pcmPath);
        void onError(String errorMessage);
    }
}
