package com.example.androiddemo.tools;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

/**
 * 音量计 Activity - 实时显示手机音量大小
 */
public class VolumeMeterActivity extends AppCompatActivity {

    private static final long UPDATE_INTERVAL = 100; // 100ms 更新

    private TextView tvCurrentVolume;
    private TextView tvVolumeUnit;
    private TextView tvVolumePercent;
    private TextView tvVolumeStatus;
    private SeekBar seekbarVolume;
    private ImageView ivVolumeIcon;
    private TextView tvMaxVolume;
    private TextView tvMinVolume;
    private TextView tvAvgVolume;

    private AudioManager audioManager;
    private Handler handler;
    private Runnable updateRunnable;

    // 历史记录
    private int maxVolume = 0;
    private int minVolume = Integer.MAX_VALUE;
    private long sumVolume = 0;
    private int sampleCount = 0;

    // 音量等级阈值（百分比）
    private static final int VOLUME_SILENT = 0;
    private static final int VOLUME_LOW = 25;
    private static final int VOLUME_MEDIUM = 50;
    private static final int VOLUME_HIGH = 75;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_volume_meter);

        initViews();
        initAudioManager();
        initHandler();

        // 设置 SeekBar 监听（允许用户调整音量）
        seekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 开始更新
        handler.post(updateRunnable);
    }

    private void initViews() {
        tvCurrentVolume = findViewById(R.id.tv_current_volume);
        tvVolumeUnit = findViewById(R.id.tv_volume_unit);
        tvVolumePercent = findViewById(R.id.tv_volume_percent);
        tvVolumeStatus = findViewById(R.id.tv_volume_status);
        seekbarVolume = findViewById(R.id.seekbar_volume);
        ivVolumeIcon = findViewById(R.id.iv_volume_icon);
        tvMaxVolume = findViewById(R.id.tv_max_volume);
        tvMinVolume = findViewById(R.id.tv_min_volume);
        tvAvgVolume = findViewById(R.id.tv_avg_volume);
    }

    private void initAudioManager() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void initHandler() {
        handler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateVolumeLevel();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };
    }

    private void updateVolumeLevel() {
        try {
            // 获取当前音量
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolumeStream = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            // 更新 SeekBar（不触发监听）
            seekbarVolume.setMax(maxVolumeStream);
            seekbarVolume.setProgress(currentVolume, true);

            // 更新音量单位显示
            tvVolumeUnit.setText("/ " + maxVolumeStream);

            // 计算百分比
            int percent = (int) ((currentVolume * 100.0f) / maxVolumeStream);

            // 更新统计
            updateStatistics(currentVolume);

            // 更新UI
            updateUI(currentVolume, percent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatistics(int volume) {
        sampleCount++;

        if (volume > maxVolume) {
            maxVolume = volume;
        }
        if (volume < minVolume) {
            minVolume = volume;
        }
        sumVolume += volume;

        int avgVolume = (int) (sumVolume / sampleCount);

        int maxVolumeStream = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        tvMaxVolume.setText(String.format("最高: %d/%d", maxVolume, maxVolumeStream));
        tvMinVolume.setText(String.format("最低: %d/%d", minVolume, maxVolumeStream));
        tvAvgVolume.setText(String.format("平均: %d", avgVolume));
    }

    private void updateUI(int volume, int percent) {
        // 更新当前音量显示
        tvCurrentVolume.setText(String.valueOf(volume));
        tvVolumePercent.setText(percent + "%");

        // 更新音量状态和图标
        String status;
        int color;
        int iconRes;

        if (volume == 0) {
            status = "静音";
            color = getColor(R.color.db_quiet);
            iconRes = android.R.drawable.ic_lock_silent_mode;
        } else if (percent < VOLUME_LOW) {
            status = "低音量";
            color = getColor(R.color.db_normal);
            iconRes = android.R.drawable.ic_lock_silent_mode_off;
        } else if (percent < VOLUME_MEDIUM) {
            status = "中等音量";
            color = getColor(R.color.db_normal);
            iconRes = android.R.drawable.ic_lock_silent_mode_off;
        } else if (percent < VOLUME_HIGH) {
            status = "高音量";
            color = getColor(R.color.db_loud);
            iconRes = android.R.drawable.ic_lock_silent_mode_off;
        } else {
            status = "最大音量";
            color = getColor(R.color.db_danger);
            iconRes = android.R.drawable.ic_lock_silent_mode_off;
        }

        tvVolumeStatus.setText(status);
        tvVolumePercent.setTextColor(color);
        tvCurrentVolume.setTextColor(color);
        ivVolumeIcon.setImageResource(iconRes);
        ivVolumeIcon.setColorFilter(color);
    }

    public void onResetClick(View view) {
        // 重置统计数据
        maxVolume = 0;
        minVolume = Integer.MAX_VALUE;
        sumVolume = 0;
        sampleCount = 0;

        int maxVolumeStream = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        tvMaxVolume.setText(String.format("最高: --/%d", maxVolumeStream));
        tvMinVolume.setText(String.format("最低: --/%d", maxVolumeStream));
        tvAvgVolume.setText("平均: --");

        Toast.makeText(this, "已重置统计数据", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}