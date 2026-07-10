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
    private final OnAiTextTtsClickListener aiTextTtsClickListener;
    private final AiTextSpeakingStateProvider aiTextSpeakingStateProvider;
    public interface OnPlayClickListener {
        void onPlayClick(String audioPath);
    }

    public interface OnStopClickListener {
        void onStopClick();
    }

    public interface OnAiTextTtsClickListener {
        void onAiTextTtsClick(VoiceMessage message);
    }

    public interface AiTextSpeakingStateProvider {
        boolean isSpeaking(VoiceMessage message);
    }

    public MessageAdapter(List<VoiceMessage> messages,
                         OnPlayClickListener playListener,
                         OnStopClickListener stopListener) {
        this(messages, playListener, stopListener, null, null);
    }

    public MessageAdapter(List<VoiceMessage> messages,
                         OnPlayClickListener playListener,
                         OnStopClickListener stopListener,
                         OnAiTextTtsClickListener aiTextTtsClickListener,
                         AiTextSpeakingStateProvider aiTextSpeakingStateProvider) {
        this.messages = messages;
        this.playListener = playListener;
        this.stopListener = stopListener;
        this.aiTextTtsClickListener = aiTextTtsClickListener;
        this.aiTextSpeakingStateProvider = aiTextSpeakingStateProvider;
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
                int userTextLayout = aiTextTtsClickListener == null
                        ? R.layout.item_msg_user_text
                        : R.layout.item_smart_qa_user_text;
                return new TextViewHolder(inflater.inflate(userTextLayout, parent, false), true);
            case VIEW_TYPE_AI_TEXT:
                int aiTextLayout = aiTextTtsClickListener == null
                        ? R.layout.item_msg_ai_text
                        : R.layout.item_smart_qa_ai_text;
                return new TextViewHolder(inflater.inflate(aiTextLayout, parent, false), false);
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
            ((TextViewHolder) holder).updateText(messages.get(position));
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

    class TextViewHolder extends RecyclerView.ViewHolder {
        private static final SimpleDateFormat TIME_FORMAT =
                new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final TextView tvText;
        private final TextView tvTime;
        private final View ttsControl;
        private final ImageView ivTts;
        private final TextView tvTtsState;
        private final boolean isUser;

        TextViewHolder(View itemView, boolean isUser) {
            super(itemView);
            this.isUser = isUser;
            this.tvText = itemView.findViewById(R.id.tv_text);
            this.tvTime = itemView.findViewById(R.id.tv_time);
            this.ttsControl = itemView.findViewById(R.id.tts_control);
            this.ivTts = itemView.findViewById(R.id.iv_tts);
            this.tvTtsState = itemView.findViewById(R.id.tv_tts_state);
        }

        void bind(VoiceMessage msg) {
            tvText.setText(msg.text);
            tvTime.setText(TIME_FORMAT.format(new java.util.Date(msg.timestamp)));
            bindTtsButton(msg);
        }

        void updateText(VoiceMessage msg) {
            tvText.setText(msg.text);
            bindTtsButton(msg);
        }

        private void bindTtsButton(VoiceMessage msg) {
            if (ttsControl == null || ivTts == null || tvTtsState == null) {
                return;
            }
            boolean canRead = !isUser
                    && aiTextTtsClickListener != null
                    && msg.text != null
                    && !msg.text.trim().isEmpty()
                    && !"...".equals(msg.text.trim());
            if (!canRead) {
                ttsControl.setVisibility(View.GONE);
                ttsControl.setOnClickListener(null);
                return;
            }
            boolean isSpeaking = aiTextSpeakingStateProvider != null
                    && aiTextSpeakingStateProvider.isSpeaking(msg);
            ttsControl.setVisibility(View.VISIBLE);
            ivTts.setImageResource(isSpeaking
                    ? R.drawable.ic_smart_qa_stop
                    : R.drawable.ic_smart_qa_volume);
            tvTtsState.setText(isSpeaking ? "播放中" : "朗读");
            ttsControl.setContentDescription(isSpeaking ? "停止朗读" : "朗读答案");
            ttsControl.setOnClickListener(v -> aiTextTtsClickListener.onAiTextTtsClick(msg));
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
