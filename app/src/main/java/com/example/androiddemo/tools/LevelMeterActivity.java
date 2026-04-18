package com.example.androiddemo.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.example.androiddemo.R;

/**
 * 水平仪 Activity
 */
public class LevelMeterActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private LevelView levelView;
    private TextView tvAngleX;
    private TextView tvAngleY;
    private TextView tvLevelStatus;
    private SwitchCompat switchSound;

    private final float[] gravity = new float[3];

    private float calibrationX = 0;
    private float calibrationY = 0;
    private boolean soundEnabled = true;
    private boolean wasLevel = false;

    private MediaPlayer mediaPlayer = null;

    private static final float LEVEL_THRESHOLD = 2.0f;
    private static final long UPDATE_INTERVAL = 50;
    private long lastUpdateTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_level_meter);

        initViews();
        initSensors();
    }

    private void initViews() {
        levelView = findViewById(R.id.level_view);
        tvAngleX = findViewById(R.id.tv_angle_x);
        tvAngleY = findViewById(R.id.tv_angle_y);
        tvLevelStatus = findViewById(R.id.tv_level_status);
        switchSound = findViewById(R.id.switch_sound);

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEnabled = isChecked;
            if (!soundEnabled && mediaPlayer != null) {
                stopSound();
            }
        });

        // 默认开启声音
        soundEnabled = true;
        switchSound.setChecked(true);
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            if (accelerometer == null) {
                Toast.makeText(this, "该设备不支持水平仪功能", Toast.LENGTH_LONG).show();
                tvLevelStatus.setText("加速度传感器不可用");
            }
        } else {
            Toast.makeText(this, "该设备不支持水平仪功能", Toast.LENGTH_LONG).show();
            tvLevelStatus.setText("传感器服务不可用");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensors();
        stopSound();
    }

    private void registerSensors() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void unregisterSensors() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        // 低通滤波平滑数据
        lowPassFilter(event.values, gravity);

        // 计算倾斜角度（弧度转度）
        float x = gravity[0];
        float y = gravity[1];
        float z = gravity[2];

        // 计算X轴角度（左右倾斜）
        double angleX = Math.atan2(x, Math.sqrt(y * y + z * z));
        double angleY = Math.atan2(y, Math.sqrt(x * x + z * z));

        float xDegrees = (float) Math.toDegrees(angleX);
        float yDegrees = (float) Math.toDegrees(angleY);

        // 应用校准偏移
        float calibratedX = xDegrees - calibrationX;
        float calibratedY = yDegrees - calibrationY;

        // 更新UI
        updateUI(calibratedX, calibratedY);
    }

    private void lowPassFilter(float[] input, float[] output) {
        final float ALPHA = 0.15f;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
    }

    private void updateUI(float x, float y) {
        // 更新自定义视图
        levelView.setLevel(x, y);

        // 更新角度显示（保留1位小数）
        tvAngleX.setText(String.format("X: %.1f°", x));
        tvAngleY.setText(String.format("Y: %.1f°", y));

        // 判断是否水平
        boolean isLevel = levelView.isLevel(LEVEL_THRESHOLD);

        if (isLevel) {
            tvLevelStatus.setText("已水平");
            tvLevelStatus.setTextColor(getColor(R.color.level_green));
            if (soundEnabled && !wasLevel) {
                playLevelSound();
            }
        } else {
            tvLevelStatus.setText("未水平");
            tvLevelStatus.setTextColor(getColor(R.color.level_red));
            if (soundEnabled && wasLevel) {
                stopSound();
            }
        }

        wasLevel = isLevel;
    }

    private void playLevelSound() {
        try {
            if (mediaPlayer == null) {
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if (notificationUri == null) {
                    notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
                mediaPlayer = MediaPlayer.create(this, notificationUri);
                if (mediaPlayer != null) {
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopSound() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onCalibrateClick(View view) {
        // 保存当前角度作为校准偏移
        calibrationX = gravity[0] != 0 ? (float) Math.toDegrees(Math.atan2(gravity[0], Math.sqrt(gravity[1] * gravity[1] + gravity[2] * gravity[2]))) : 0;
        calibrationY = gravity[1] != 0 ? (float) Math.toDegrees(Math.atan2(gravity[1], Math.sqrt(gravity[0] * gravity[0] + gravity[2] * gravity[2]))) : 0;

        Toast.makeText(this, "已校准当前角度为零点", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不需要处理
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSound();
    }
}