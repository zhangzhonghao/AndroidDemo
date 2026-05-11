package com.example.androiddemo.ai;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * PCM 音频录制器
 * 使用 AudioRecord 直接录制 16k 16bit 单声道 PCM 原始数据
 */
public class PcmAudioRecorder {
    private static final String TAG = "PcmAudioRecorder";

    // 音频参数：16k采样率，16bit采样深度，单声道
    public static final int SAMPLE_RATE = 16000;
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // 每帧 40ms = 16000 * 0.04 * 2 = 1280 字节
    public static final int FRAME_SIZE = 1280;

    private AudioRecord audioRecord;
    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private ExecutorService recordExecutor;
    private String outputFilePath;

    // 回调接口
    public interface RecordCallback {
        void onStart();
        void onFrameData(byte[] data, int frameIndex);  // 每帧数据回调（40ms）
        void onComplete(String filePath, int totalFrames);
        void onError(String errorMessage);
    }

    private RecordCallback callback;

    public PcmAudioRecorder() {
    }

    /**
     * 开始录制
     * @param outputPath 输出 PCM 文件路径
     * @param callback 回调接口
     */
    public void startRecording(String outputPath, RecordCallback callback) {
        if (isRecording.get()) {
            Log.w(TAG, "已经在录制中");
            return;
        }

        this.outputFilePath = outputPath;
        this.callback = callback;

        // 计算缓冲区大小
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            postError("无法获取录音缓冲区大小");
            return;
        }

        // 确保缓冲区足够大（至少3帧）
        final int bufferSize = Math.max(minBufferSize, FRAME_SIZE * 3);

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                postError("AudioRecord 初始化失败");
                release();
                return;
            }

            isRecording.set(true);
            audioRecord.startRecording();

            if (callback != null) {
                callback.onStart();
            }

            // 开始录制线程
            recordExecutor = Executors.newSingleThreadExecutor();
            recordExecutor.execute(() -> recordAudio(bufferSize));

            Log.d(TAG, "开始录制 PCM: " + outputPath);

        } catch (Exception e) {
            Log.e(TAG, "启动录制失败: " + e.getMessage(), e);
            postError("启动录制失败: " + e.getMessage());
            release();
        }
    }

    /**
     * 录制线程
     */
    private void recordAudio(int bufferSize) {
        FileOutputStream fos = null;
        byte[] buffer = new byte[FRAME_SIZE];
        int frameIndex = 0;
        int totalBytesWritten = 0;

        try {
            // 确保输出目录存在
            File outputFile = new File(outputFilePath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            fos = new FileOutputStream(outputFile);

            while (isRecording.get()) {
                // 读取音频数据
                int bytesRead = audioRecord.read(buffer, 0, FRAME_SIZE);

                if (bytesRead > 0) {
                    // 写入文件
                    fos.write(buffer, 0, bytesRead);
                    totalBytesWritten += bytesRead;
                    frameIndex++;

                    // 回调每帧数据
                    if (callback != null) {
                        byte[] frameData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, frameData, 0, bytesRead);
                        callback.onFrameData(frameData, frameIndex);
                    }
                } else if (bytesRead < 0) {
                    Log.e(TAG, "读取音频数据失败: " + bytesRead);
                    break;
                }
            }

            Log.d(TAG, "录制完成，共 " + frameIndex + " 帧，" + totalBytesWritten + " 字节");

            // 回调完成
            if (callback != null) {
                final int finalFrameIndex = frameIndex;
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                mainHandler.post(() -> callback.onComplete(outputFilePath, finalFrameIndex));
            }

        } catch (Exception e) {
            Log.e(TAG, "录制过程出错: " + e.getMessage(), e);
            postError("录制出错: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (!isRecording.get()) {
            return;
        }

        Log.d(TAG, "停止录制");
        isRecording.set(false);

        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception e) {
                Log.e(TAG, "停止录音失败: " + e.getMessage());
            }
        }

        if (recordExecutor != null) {
            recordExecutor.shutdown();
            recordExecutor = null;
        }

        release();
    }

    /**
     * 释放资源
     */
    private void release() {
        if (audioRecord != null) {
            try {
                audioRecord.release();
            } catch (Exception ignored) {
            }
            audioRecord = null;
        }
    }

    /**
     * 检查是否正在录制
     */
    public boolean isRecording() {
        return isRecording.get();
    }

    /**
     * 生成 PCM 文件路径
     * @param cacheDir app cache 目录
     */
    public static String generateFilePath(java.io.File cacheDir) {
        java.io.File audioDir = new java.io.File(cacheDir, "voice");
        if (!audioDir.exists()) audioDir.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return new java.io.File(audioDir, "voice_" + timestamp + ".pcm").getAbsolutePath();
    }

    /**
     * 在主线程发送错误回调
     */
    private void postError(String message) {
        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onError(message);
            }
        });
    }
}
