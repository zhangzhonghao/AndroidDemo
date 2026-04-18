package com.example.androiddemo.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ShakeExitActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Vibrator vibrator;
    private TextView tvStatus;
    private TextView tvCount;
    private View rootView;
    private int shakeCount = 0;
    private long lastShakeTime = 0;
    private static final float SHAKE_THRESHOLD = 15.0f;
    private static final int SHAKES_TO_EXIT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_exit);

        rootView = findViewById(R.id.root_view);
        tvStatus = findViewById(R.id.tv_status);
        tvCount = findViewById(R.id.tv_count);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tvStatus.setText("摇晃手机 " + SHAKES_TO_EXIT + " 次退出");
        tvCount.setText("0 / " + SHAKES_TO_EXIT);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击也可以退出（备用方案）
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float acceleration = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        float actualAcceleration = (float) Math.sqrt(acceleration);

        if (actualAcceleration > SHAKE_THRESHOLD) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastShakeTime > 500) {
                lastShakeTime = currentTime;
                shakeCount++;
                tvCount.setText(shakeCount + " / " + SHAKES_TO_EXIT);

                // 震动反馈
                if (vibrator != null) {
                    vibrator.vibrate(100);
                }

                // 背景闪烁效果
                rootView.setBackgroundColor(0xFF666666);
                rootView.postDelayed(() -> rootView.setBackgroundColor(0xFF333333), 100);

                if (shakeCount >= SHAKES_TO_EXIT) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}