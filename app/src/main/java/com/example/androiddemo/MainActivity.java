package com.example.androiddemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.location.LocationGpsActivity;
import com.example.androiddemo.location.LocationGeofenceActivity;
import com.example.androiddemo.location.LocationDistanceActivity;
import com.example.androiddemo.location.LocationMapActivity;
import com.example.androiddemo.location.LocationNavigationActivity;
import com.example.androiddemo.ai.VoiceCollectionActivity;
import com.example.androiddemo.ai.PetActivity;
import com.example.androiddemo.network.NetworkHttpActivity;
import com.example.androiddemo.network.NetworkJsonActivity;
import com.example.androiddemo.network.NetworkImageActivity;
import com.example.androiddemo.network.NetworkWebsocketActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onFeatureClick(View view) {
        Intent intent = null;
        int id = view.getId();

        // 定位
        if (id == R.id.btn_location_gps) {
            intent = new Intent(this, LocationGpsActivity.class);
        } else if (id == R.id.btn_location_geofence) {
            intent = new Intent(this, LocationGeofenceActivity.class);
        } else if (id == R.id.btn_location_distance) {
            intent = new Intent(this, LocationDistanceActivity.class);
        } else if (id == R.id.btn_location_map) {
            intent = new Intent(this, LocationMapActivity.class);
        } else if (id == R.id.btn_location_navigation) {
            intent = new Intent(this, LocationNavigationActivity.class);
        }
        // AI
        else if (id == R.id.btn_ai_voice) {
            intent = new Intent(this, VoiceCollectionActivity.class);
        } else if (id == R.id.btn_ai_pet) {
            intent = new Intent(this, PetActivity.class);
        }
        // 网络通信
        else if (id == R.id.btn_network_http) {
            intent = new Intent(this, NetworkHttpActivity.class);
        } else if (id == R.id.btn_network_json) {
            intent = new Intent(this, NetworkJsonActivity.class);
        } else if (id == R.id.btn_network_image) {
            intent = new Intent(this, NetworkImageActivity.class);
        } else if (id == R.id.btn_network_websocket) {
            intent = new Intent(this, NetworkWebsocketActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
