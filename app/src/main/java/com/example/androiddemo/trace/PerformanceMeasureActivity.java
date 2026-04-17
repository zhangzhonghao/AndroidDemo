package com.example.androiddemo.trace;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.R;

/**
 * 性能测试空白Activity
 * 用于测量页面启动时间
 */
public class PerformanceMeasureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance_measure);
        
        // 记录进入此页面的时间
        final long measureTime = System.currentTimeMillis();
        
        // 延迟一点时间确保页面完全加载后再返回结果
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // 返回结果给 PerformanceTraceActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("measure_time", measureTime);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }, 500);
    }
}
