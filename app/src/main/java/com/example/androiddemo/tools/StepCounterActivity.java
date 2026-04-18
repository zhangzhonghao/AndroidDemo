package com.example.androiddemo.tools;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvStepCount;
    private TextView tvDistance;
    private TextView tvCalories;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private int initialSteps = -1;
    private int currentSteps = 0;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("运动记步");
        }

        tvStepCount = findViewById(R.id.tv_step_count);
        tvDistance = findViewById(R.id.tv_distance);
        tvCalories = findViewById(R.id.tv_calories);

        prefs = getSharedPreferences("step_data", MODE_PRIVATE);
        currentSteps = prefs.getInt("today_steps", 0);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        updateDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int totalSteps = (int) event.values[0];

            if (initialSteps < 0) {
                initialSteps = totalSteps - currentSteps;
            }

            currentSteps = totalSteps - initialSteps;
            prefs.edit().putInt("today_steps", currentSteps).apply();
            updateDisplay();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void updateDisplay() {
        tvStepCount.setText(currentSteps + " 步");

        // 假设步幅约0.6米
        double distance = currentSteps * 0.6 / 1000;
        tvDistance.setText(String.format("%.2f 公里", distance));

        // 假设每步消耗约0.04卡路里
        double calories = currentSteps * 0.04;
        tvCalories.setText(String.format("%.1f 千卡", calories));
    }

    public void resetSteps(View view) {
        currentSteps = 0;
        initialSteps = -1;
        prefs.edit().putInt("today_steps", 0).apply();
        updateDisplay();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}