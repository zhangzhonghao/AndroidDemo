package com.example.androiddemo.ai;

/**
 * 聊天消息数据类
 */
public class ChatMessage {
    public final boolean isUser;
    public final String text;
    public final long timestamp;

    public ChatMessage(boolean isUser, String text) {
        this.isUser = isUser;
        this.text = text;
        this.timestamp = System.currentTimeMillis();
    }
}