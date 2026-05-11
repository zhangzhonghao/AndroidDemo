package com.example.androiddemo.efficiency;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.PomodoroTimerActivity;
import com.example.androiddemo.tools.StopwatchActivity;
import com.example.androiddemo.tools.SpeedTestActivity;
import com.example.androiddemo.tools.VideoToGifActivity;
import com.example.androiddemo.tools.WatermarkCameraActivity;
import com.example.androiddemo.tools.CountdownActivity;
import com.example.androiddemo.tools.CountdownDaysActivity;
import com.example.androiddemo.tools.AnniversaryManagerActivity;
import com.example.androiddemo.tools.FlashSaleActivity;

/**
 * 效率首页
 * 包含：番茄钟、秒表、网速测试、视频转GIF、水印相机、倒计时等效率工具
 */
public class EfficiencyHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efficiency_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_pomodoro) {
            intent = new Intent(this, PomodoroTimerActivity.class);
        } else if (id == R.id.btn_stopwatch) {
            intent = new Intent(this, StopwatchActivity.class);
        } else if (id == R.id.btn_speed_test) {
            intent = new Intent(this, SpeedTestActivity.class);
        } else if (id == R.id.btn_video_to_gif) {
            intent = new Intent(this, VideoToGifActivity.class);
        } else if (id == R.id.btn_watermark_camera) {
            intent = new Intent(this, WatermarkCameraActivity.class);
        } else if (id == R.id.btn_countdown) {
            intent = new Intent(this, CountdownActivity.class);
        } else if (id == R.id.btn_countdown_days) {
            intent = new Intent(this, CountdownDaysActivity.class);
        } else if (id == R.id.btn_anniversary_manager) {
            intent = new Intent(this, AnniversaryManagerActivity.class);
        } else if (id == R.id.btn_flash_sale) {
            intent = new Intent(this, FlashSaleActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
