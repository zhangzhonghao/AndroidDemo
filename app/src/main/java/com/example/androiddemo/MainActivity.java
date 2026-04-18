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
import com.example.androiddemo.ai.AilImageGenActivity;
import com.example.androiddemo.ai.AiChatActivity;
import com.example.androiddemo.ai.AiCodeActivity;
import com.example.androiddemo.ai.AiTtsActivity;
import com.example.androiddemo.ai.AiVisionActivity;
import com.example.androiddemo.network.NetworkHttpActivity;
import com.example.androiddemo.network.NetworkJsonActivity;
import com.example.androiddemo.network.NetworkImageActivity;
import com.example.androiddemo.network.NetworkWebsocketActivity;
import com.example.androiddemo.ui.bottomnav.BottomNavFoldableActivity;
import com.example.androiddemo.tools.ToolsHomeActivity;
import com.example.androiddemo.tools.SecurityHomeActivity;
import com.example.androiddemo.trace.CustomTraceActivity;
import com.example.androiddemo.trace.CrashTraceActivity;
import com.example.androiddemo.trace.WhiteScreenTraceActivity;
import com.example.androiddemo.trace.LagTraceActivity;
import com.example.androiddemo.trace.PerformanceTraceActivity;

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
        } else if (id == R.id.btn_ai_image_gen) {
            intent = new Intent(this, AilImageGenActivity.class);
        } else if (id == R.id.btn_ai_tts) {
            intent = new Intent(this, AiTtsActivity.class);
        } else if (id == R.id.btn_ai_vision) {
            intent = new Intent(this, AiVisionActivity.class);
        } else if (id == R.id.btn_ai_code) {
            intent = new Intent(this, AiCodeActivity.class);
        }
        // UI
        else if (id == R.id.btn_ui_bottom_nav_foldable) {
            intent = new Intent(this, BottomNavFoldableActivity.class);
        }
        // 应用埋点
        else if (id == R.id.btn_tools) {
            intent = new Intent(this, ToolsHomeActivity.class);
        } else if (id == R.id.btn_trace_custom) {
            intent = new Intent(this, CustomTraceActivity.class);
        } else if (id == R.id.btn_trace_crash) {
            intent = new Intent(this, CrashTraceActivity.class);
        } else if (id == R.id.btn_trace_white_screen) {
            intent = new Intent(this, WhiteScreenTraceActivity.class);
        } else if (id == R.id.btn_trace_lag) {
            intent = new Intent(this, LagTraceActivity.class);
        } else if (id == R.id.btn_trace_performance) {
            intent = new Intent(this, PerformanceTraceActivity.class);
        }
        // 安全中心
        else if (id == R.id.btn_security_center) {
            intent = new Intent(this, SecurityHomeActivity.class);
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
