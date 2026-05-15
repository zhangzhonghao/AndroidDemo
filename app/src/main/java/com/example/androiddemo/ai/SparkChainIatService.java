package com.example.androiddemo.ai;

import android.util.Log;

import com.iflytek.sparkchain.core.asr.ASR;
import com.iflytek.sparkchain.core.asr.AsrCallbacks;
import com.iflytek.sparkchain.core.asr.AudioAttributes;
import com.iflytek.sparkchain.core.asr.Segment;
import com.iflytek.sparkchain.core.asr.Transcription;

public class SparkChainIatService {

    private static final String TAG = "SparkChainIatService";

    private ASR asr;
    private IatCallback callback;
    private boolean recognizing = false;
    private final StringBuilder textBuilder = new StringBuilder();

    public interface IatCallback {
        void onStart();
        void onResult(String text);
        void onComplete(String fullText);
        void onError(String errorMessage);
    }

    public void startRecognize(IatCallback cb) {
        this.callback = cb;
        this.textBuilder.setLength(0);

        asr = new ASR();
        asr.language("zh_cn");
        asr.domain("iat");
        asr.accent("mandarin");

        asr.registerCallbacks(new AsrCallbacks() {
            @Override
            public void onResult(ASR.ASRResult result, Object o) {
                if (result == null) return;

                if (result.getStatus() == 1) {
                    // 中间结果：累积
                    String text = extractText(result);
                    textBuilder.append(text);
                    if (callback != null) {
                        callback.onResult(textBuilder.toString());
                    }
                } else if (result.getStatus() == 2) {
                    // 最终结果
                    String text = extractText(result);
                    textBuilder.append(text);
                    String fullText = textBuilder.toString().trim();
                    if (fullText.isEmpty()) {
                        fullText = "未识别到文字";
                    }
                    recognizing = false;
                    if (callback != null) {
                        callback.onComplete(fullText);
                    }
                }
            }

            @Override
            public void onError(ASR.ASRError error, Object o) {
                recognizing = false;
                String errMsg = error != null ? error.getErrMsg() : "未知错误";
                Log.e(TAG, "ASR error: " + errMsg);
                if (callback != null) {
                    callback.onError(errMsg);
                }
            }
        });

        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setSampleRate(16000);
        audioAttributes.setEncoding("raw");
        audioAttributes.setChannels(1);
        audioAttributes.setBitdepth(16);

        asr.start(audioAttributes, null);
        recognizing = true;

        if (callback != null) {
            callback.onStart();
        }
    }

    private String extractText(ASR.ASRResult result) {
        StringBuilder sb = new StringBuilder();
        if (result.getTranscriptions() != null) {
            for (Transcription trans : result.getTranscriptions()) {
                if (trans.getSegments() != null) {
                    for (Segment seg : trans.getSegments()) {
                        String text = seg.getText();
                        if (text != null) {
                            sb.append(text);
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public void writeAudio(byte[] data) {
        if (asr != null && recognizing) {
            asr.write(data);
        }
    }

    public void stopRecognize(boolean cancel) {
        if (asr != null) {
            asr.stop(cancel);
            recognizing = false;
        }
    }

    public boolean isRecognizing() {
        return recognizing;
    }

    public void release() {
        if (asr != null) {
            asr.stop(true);
            asr = null;
        }
        recognizing = false;
        callback = null;
    }
}
