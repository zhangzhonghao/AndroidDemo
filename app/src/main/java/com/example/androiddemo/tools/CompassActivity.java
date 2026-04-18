package com.example.androiddemo.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

/**
 * 指南针 Activity
 */
public class CompassActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;

    private CompassView compassView;
    private TextView tvDegree;
    private TextView tvDirection;
    private TextView tvCalibration;

    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private float currentAzimuth = 0;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 50;

    private boolean hasAccelerometer = false;
    private boolean hasMagnetometer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_compass);

        initViews();
        initSensors();
    }

    private void initViews() {
        compassView = findViewById(R.id.compass_view);
        tvDegree = findViewById(R.id.tv_degree);
        tvDirection = findViewById(R.id.tv_direction);
        tvCalibration = findViewById(R.id.tv_calibration);
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            if (accelerometer != null) {
                hasAccelerometer = true;
            }
            if (magnetometer != null) {
                hasMagnetometer = true;
            }

            if (!hasAccelerometer && !hasMagnetometer) {
                Toast.makeText(this, "该设备不支持指南针功能", Toast.LENGTH_LONG).show();
                tvDirection.setText("传感器不可用");
            }
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
    }

    private void registerSensors() {
        if (sensorManager != null) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
            if (magnetometer != null) {
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    private void unregisterSensors() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            lowPassFilter(event.values, gravity);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            lowPassFilter(event.values, geomagnetic);
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }
        lastUpdateTime = currentTime;

        updateOrientation();
    }

    private void lowPassFilter(float[] input, float[] output) {
        final float ALPHA = 0.15f;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
    }

    private void updateOrientation() {
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic);

        if (success) {
            SensorManager.getOrientation(rotationMatrix, orientation);

            // 将弧度转换为度
            float azimuthInDegrees = (float) Math.toDegrees(orientation[0]);
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360.0f;
            }

            // 平滑处理
            currentAzimuth = smoothAngle(currentAzimuth, azimuthInDegrees, 0.3f);

            // 更新 UI
            updateUI(currentAzimuth);
        }
    }

    private float smoothAngle(float current, float target, float factor) {
        float diff = target - current;

        // 处理角度跨越 0/360 的情况
        if (diff > 180) {
            diff -= 360;
        } else if (diff < -180) {
            diff += 360;
        }

        float result = current + diff * factor;

        // 确保结果在 0-360 范围内
        if (result < 0) {
            result += 360;
        } else if (result >= 360) {
            result -= 360;
        }

        return result;
    }

    private void updateUI(float azimuth) {
        // 更新罗盘
        compassView.setAzimuth(azimuth);

        // 更新角度显示
        int degree = Math.round(azimuth);
        tvDegree.setText(degree + "°");

        // 更新方向文字
        String direction = getDirectionName(azimuth);
        tvDirection.setText(direction);
    }

    private String getDirectionName(float azimuth) {
        String direction;
        if (azimuth >= 337.5 || azimuth < 22.5) {
            direction = "北";
        } else if (azimuth >= 22.5 && azimuth < 67.5) {
            direction = "东北";
        } else if (azimuth >= 67.5 && azimuth < 112.5) {
            direction = "东";
        } else if (azimuth >= 112.5 && azimuth < 157.5) {
            direction = "东南";
        } else if (azimuth >= 157.5 && azimuth < 202.5) {
            direction = "南";
        } else if (azimuth >= 202.5 && azimuth < 247.5) {
            direction = "西南";
        } else if (azimuth >= 247.5 && azimuth < 292.5) {
            direction = "西";
        } else if (azimuth >= 292.5 && azimuth < 337.5) {
            direction = "西北";
        } else {
            direction = "北";
        }
        return direction;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    tvCalibration.setText("校准状态: 高");
                    tvCalibration.setTextColor(0xFF27AE60);
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    tvCalibration.setText("校准状态: 中 - 建议画圈校准");
                    tvCalibration.setTextColor(0xFFF39C12);
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    tvCalibration.setText("校准状态: 低 - 请进行画圈校准");
                    tvCalibration.setTextColor(0xFFE74C3C);
                    break;
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    tvCalibration.setText("校准状态: 不可靠 - 请校准设备");
                    tvCalibration.setTextColor(0xFFE74C3C);
                    break;
            }
        }
    }
}