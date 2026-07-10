package com.example.androiddemo.ai;

/**
 * AI对话消息数据类
 * 供 AiChatActivity 和 AiChatApiActivity 共用
 */
public class VoiceMessage {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_VOICE = 1;

    public final int type;        // TYPE_TEXT / TYPE_VOICE
    public final boolean isUser;  // true=用户发送，false=AI接收
    public String text;          // 文字内容（type=TYPE_TEXT 时有值），流式更新时可变
    public final String audioPath; // 音频路径（type=TYPE_VOICE 时有值）
    public final Integer duration; // 录音时长秒数（type=TYPE_VOICE 时有值）
    public final long timestamp;  // 消息创建时间

    public VoiceMessage(int type, boolean isUser, String text, String audioPath, Integer duration) {
        this.type = type;
        this.isUser = isUser;
        this.text = text;
        this.audioPath = audioPath;
        this.duration = duration;
        this.timestamp = System.currentTimeMillis();
    }
}
