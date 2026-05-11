package com.example.androiddemo.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER_TEXT = 0;
    private static final int VIEW_TYPE_AI_TEXT = 1;
    private static final int VIEW_TYPE_USER_VOICE = 2;
    private static final int VIEW_TYPE_AI_VOICE = 3;

    private final List<VoiceMessage> messages;
    private final OnPlayClickListener playListener;
    private final OnStopClickListener stopListener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnPlayClickListener {
        void onPlayClick(String audioPath);
    }

    public interface OnStopClickListener {
        void onStopClick();
    }

    public MessageAdapter(List<VoiceMessage> messages,
                         OnPlayClickListener playListener,
                         OnStopClickListener stopListener) {
        this.messages = messages;
        this.playListener = playListener;
        this.stopListener = stopListener;
    }

    @Override
    public int getItemViewType(int position) {
        VoiceMessage msg = messages.get(position);
        if (msg.type == VoiceMessage.TYPE_TEXT) {
            return msg.isUser ? VIEW_TYPE_USER_TEXT : VIEW_TYPE_AI_TEXT;
        } else {
            return msg.isUser ? VIEW_TYPE_USER_VOICE : VIEW_TYPE_AI_VOICE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_USER_TEXT:
                return new TextViewHolder(inflater.inflate(R.layout.item_msg_user_text, parent, false), true);
            case VIEW_TYPE_AI_TEXT:
                return new TextViewHolder(inflater.inflate(R.layout.item_msg_ai_text, parent, false), false);
            case VIEW_TYPE_USER_VOICE:
                return new VoiceViewHolder(inflater.inflate(R.layout.item_msg_user_voice, parent, false), true);
            case VIEW_TYPE_AI_VOICE:
            default:
                return new VoiceViewHolder(inflater.inflate(R.layout.item_msg_ai_voice, parent, false), false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && holder instanceof TextViewHolder) {
            // 带 payload 的局部更新，只刷新文本，不触发整个 item 重新布局
            ((TextViewHolder) holder).updateText(messages.get(position).text);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VoiceMessage msg = messages.get(position);

        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(msg);
        } else if (holder instanceof VoiceViewHolder) {
            ((VoiceViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ========== 文字消息 ViewHolder ==========

    static class TextViewHolder extends RecyclerView.ViewHolder {
        private static final SimpleDateFormat TIME_FORMAT =
                new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final TextView tvText;
        private final TextView tvTime;

        TextViewHolder(View itemView, boolean isUser) {
            super(itemView);
            this.tvText = itemView.findViewById(R.id.tv_text);
            this.tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(VoiceMessage msg) {
            tvText.setText(msg.text);
            tvTime.setText(TIME_FORMAT.format(new java.util.Date()));
        }

        void updateText(String text) {
            tvText.setText(text);
        }
    }

    // ========== 语音消息 ViewHolder ==========

    class VoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDuration;
        private final ImageView ivPlay;
        private final boolean isUser;

        VoiceViewHolder(View itemView, boolean isUser) {
            super(itemView);
            this.isUser = isUser;
            this.tvDuration = itemView.findViewById(R.id.tv_duration);
            this.ivPlay = itemView.findViewById(R.id.iv_play);
        }

        void bind(VoiceMessage msg) {
            tvDuration.setText(msg.duration + "″");
            ivPlay.setOnClickListener(v -> {
                if (playListener != null) {
                    playListener.onPlayClick(msg.audioPath);
                }
            });
        }
    }
}
