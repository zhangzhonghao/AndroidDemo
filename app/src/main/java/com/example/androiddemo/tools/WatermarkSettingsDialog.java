package com.example.androiddemo.tools;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.androiddemo.R;

public class WatermarkSettingsDialog extends DialogFragment {

    public interface OnSettingsApplyListener {
        void onSettingsApply(WatermarkSettings settings);
    }

    public static class WatermarkSettings {
        public boolean showDateTime = true;
        public boolean showLocation = true;
        public String customText = "";
        public int position = Position.BOTTOM_LEFT;
        public float fontSize = 16f;
        public int textColor = Color.WHITE;
        public float alpha = 1.0f;

        public static class Position {
            public static final int TOP_LEFT = 0;
            public static final int TOP_RIGHT = 1;
            public static final int BOTTOM_LEFT = 2;
            public static final int BOTTOM_RIGHT = 3;
        }
    }

    private OnSettingsApplyListener listener;
    private WatermarkSettings currentSettings;

    public static WatermarkSettingsDialog newInstance(WatermarkSettings settings) {
        WatermarkSettingsDialog dialog = new WatermarkSettingsDialog();
        dialog.currentSettings = settings;
        return dialog;
    }

    public void setOnSettingsApplyListener(OnSettingsApplyListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_AndroidDemo);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_watermark_settings, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.BOTTOM);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }
    }

    private void initViews(View view) {
        CheckBox cbDateTime = view.findViewById(R.id.cb_date_time);
        CheckBox cbLocation = view.findViewById(R.id.cb_location);
        EditText etCustomText = view.findViewById(R.id.et_custom_text);
        TextView tvPosition = view.findViewById(R.id.tv_position);
        SeekBar sbFontSize = view.findViewById(R.id.sb_font_size);
        SeekBar sbOpacity = view.findViewById(R.id.sb_opacity);
        View btnApply = view.findViewById(R.id.btn_apply);
        View btnCancel = view.findViewById(R.id.btn_cancel);

        // 初始化当前设置
        if (currentSettings == null) {
            currentSettings = new WatermarkSettings();
        }
        cbDateTime.setChecked(currentSettings.showDateTime);
        cbLocation.setChecked(currentSettings.showLocation);
        etCustomText.setText(currentSettings.customText);

        // 位置选择
        final int[] positions = {WatermarkSettings.Position.TOP_LEFT,
                WatermarkSettings.Position.TOP_RIGHT,
                WatermarkSettings.Position.BOTTOM_LEFT,
                WatermarkSettings.Position.BOTTOM_RIGHT};
        final String[] positionNames = {"左上", "右上", "左下", "右下"};
        final int[] currentPositionIndex = {currentSettings.position};
        tvPosition.setText(positionNames[currentPositionIndex[0]]);

        tvPosition.setOnClickListener(v -> {
            currentPositionIndex[0] = (currentPositionIndex[0] + 1) % positions.length;
            tvPosition.setText(positionNames[currentPositionIndex[0]]);
        });

        // 字体大小
        sbFontSize.setProgress((int) (currentSettings.fontSize - 10));
        sbFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 透明度
        sbOpacity.setProgress((int) (currentSettings.alpha * 100));
        sbOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 应用按钮
        btnApply.setOnClickListener(v -> {
            WatermarkSettings settings = new WatermarkSettings();
            settings.showDateTime = cbDateTime.isChecked();
            settings.showLocation = cbLocation.isChecked();
            settings.customText = etCustomText.getText().toString();
            settings.position = positions[currentPositionIndex[0]];
            settings.fontSize = 10 + sbFontSize.getProgress();
            settings.alpha = sbOpacity.getProgress() / 100f;

            if (listener != null) {
                listener.onSettingsApply(settings);
            }
            dismiss();
        });

        // 取消按钮
        btnCancel.setOnClickListener(v -> dismiss());
    }
}