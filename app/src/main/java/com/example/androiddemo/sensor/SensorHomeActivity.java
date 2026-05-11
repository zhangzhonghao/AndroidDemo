package com.example.androiddemo.sensor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import com.example.androiddemo.tools.CompassActivity;
import com.example.androiddemo.tools.LevelMeterActivity;
import com.example.androiddemo.tools.NoiseMeterActivity;
import com.example.androiddemo.tools.VolumeMeterActivity;
import com.example.androiddemo.tools.TunerActivity;
import com.example.androiddemo.tools.MetronomeActivity;

/**
 * 传感器首页
 * 包含：指北针、量角器、噪音计、分贝仪、调音师、节拍器
 */
public class SensorHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_home);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        if (id == R.id.btn_compass) {
            intent = new Intent(this, CompassActivity.class);
        } else if (id == R.id.btn_level_meter) {
            intent = new Intent(this, LevelMeterActivity.class);
        } else if (id == R.id.btn_noise_meter) {
            intent = new Intent(this, NoiseMeterActivity.class);
        } else if (id == R.id.btn_volume_meter) {
            intent = new Intent(this, VolumeMeterActivity.class);
        } else if (id == R.id.btn_tuner) {
            intent = new Intent(this, TunerActivity.class);
        } else if (id == R.id.btn_metronome) {
            intent = new Intent(this, MetronomeActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
