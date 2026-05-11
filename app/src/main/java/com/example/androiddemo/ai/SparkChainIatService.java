package com.example.androiddemo.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * 讯飞 SparkChain SDK 语音识别服务
 * 使用 SparkChain Android SDK 进行语音转文字（IAT）
 */
import com.iflytek.sparkchain.core.asr.ASR;
import com.iflytek.sparkchain.core.asr.AsrCallbacks;
import com.iflytek.sparkchain.core.asr.ASR.ASRError;
import com.iflytek.sparkchain.core.asr.ASR.ASRResult;

public class SparkChainIatService {
    private static final String TAG = "SparkChainIatService";

    // 音频参数：16k采样率，16bit采样深度，单声道
    // 每帧 40ms = 16000 * 0.04 * 2 = 1280 字节
    public static final int SAMPLE_RATE = 16000;
    public static final int FRAME_SIZE = 1280;  // 40ms @ 16kHz

    private ASR mAsr;
    
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private IatCallback callback;
    private boolean isRecognizing = false;

    // 回调接口
    public interface IatCallback {
        void onStart();                            // 开始识别
        void onResult(String text);                // 接收到识别结果（部分结果）
        void onComplete(String fullText);          // 识别完成，返回完整文字
        void onError(String errorMessage);         // 发生错误
    }

    public SparkChainIatService() {
    }

    /**
     * 开始语音识别
     * @param callback 回调接口
     */
    public void startRecognize(IatCallback callback) {
        if (isRecognizing) {
            Log.w(TAG, "已经在识别中，先停止之前的识别");
            stopRecognize();
        }

        this.callback = callback;
        isRecognizing = true;

        // 确保 SDK 已初始化
        if (!SparkChainManager.isInitialized()) {
            mainHandler.post(() -> callback.onError("SparkChain SDK 未初始化"));
            isRecognizing = false;
            return;
        }

        try {
            // 创建 ASR 实例
            mAsr = new ASR();
            mAsr.language("zh_cn");
            mAsr.domain("iat");
            mAsr.accent("mandarin");

            // 注册回调
            mAsr.registerCallbacks(new AsrCallbacks() {
                @Override
                public void onResult(ASRResult result, Object usrTag) {
                    if (result == null) return;

                    int status = result.getStatus();
                    String text = result.getBestMatchText();

                    Log.d(TAG, "onResult: status=" + status + ", text=" + text);

                    if (status == 0) {
                        // 开始
                        mainHandler.post(() -> {
                            if (callback != null) {
                                callback.onStart();
                            }
                        });
                    } else if (status == 1) {
                        // 中间结果
                        mainHandler.post(() -> {
                            if (callback != null && text != null && !text.isEmpty()) {
                                callback.onResult(text);
                            }
                        });
                    } else if (status == 2) {
                        // 最终结果
                        final String finalText = (text != null) ? text : "";
                        mainHandler.post(() -> {
                            isRecognizing = false;
                            if (callback != null) {
                                callback.onComplete(finalText.isEmpty() ? "未识别到文字" : finalText);
                            }
                        });
                    }
                }

                @Override
                public void onError(ASRError error, Object usrTag) {
                    if (error == null) return;

                    String errorMessage = error.getErrMsg();
                    int errorCode = error.getCode();

                    Log.e(TAG, "onError: code=" + errorCode + ", msg=" + errorMessage);

                    mainHandler.post(() -> {
                        isRecognizing = false;
                        if (callback != null) {
                            callback.onError("识别错误(" + errorCode + "): " + errorMessage);
                        }
                    });
                }
            });

            // 启动识别会话
            int ret = mAsr.start(null);
            if (ret == 0) {
                Log.d(TAG, "ASR 会话已启动");
            } else {
                Log.e(TAG, "ASR 会话启动失败，错误码: " + ret);
                isRecognizing = false;
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onError("启动识别失败，错误码: " + ret);
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "启动识别失败: " + e.getMessage(), e);
            isRecognizing = false;
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onError("启动识别失败: " + e.getMessage());
                }
            });
        }
    }

    /**
     * 写入音频数据
     * @param audioData 原始 PCM 音频数据（16k 16bit 单声道）
     */
    public void writeAudio(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return;
        }

        if (mAsr == null) {
            return;
        }
        try {
            mAsr.write(audioData);
        } catch (Exception e) {
            Log.e(TAG, "写入音频数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 停止识别
     * @param immediate 是否立即停止（true=不等结果，false=等最终结果）
     */
    public void stopRecognize(boolean immediate) {
        if (mAsr == null) {
            return;
        }
        try {
            Log.d(TAG, "停止 ASR, immediate=" + immediate);
            mAsr.stop(immediate);
        } catch (Exception e) {
            Log.e(TAG, "停止识别失败: " + e.getMessage(), e);
        }

        isRecognizing = false;
        mAsr = null;
    }

    /**
     * 停止识别（默认不等结果）
     */
    public void stopRecognize() {
        stopRecognize(false);
    }

    /**
     * 释放资源
     */
    public void release() {
        stopRecognize();
        callback = null;
    }

    /**
     * 检查是否正在识别
     */
    public boolean isRecognizing() {
        return isRecognizing;
    }
}
