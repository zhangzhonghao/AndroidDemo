package com.example.androiddemo.trace;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 性能埋点页面
 * 功能：点击按钮打开空白Activity，记录启动时间并保存到本地文件
 */
public class PerformanceTraceActivity extends AppCompatActivity {

    private TextView tvPerformanceLog;
    private Button btnStartPerformance;

    // 启动时间记录
    public static long startupTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_performance);

        // 进入页面立即记录启动时间
        startupTime = System.currentTimeMillis();

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 如果是从 PerformanceMeasureActivity 返回，检查是否有结果
    }

    private void initViews() {
        tvPerformanceLog = findViewById(R.id.tv_performance_log);
        btnStartPerformance = findViewById(R.id.btn_start_performance);
    }

    private void setupListeners() {
        btnStartPerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPerformanceTest();
            }
        });
    }

    /**
     * 启动性能测试页面
     */
    private void startPerformanceTest() {
        // 使用 startActivityForResult 启动空白Activity
        Intent intent = new Intent(this, PerformanceMeasureActivity.class);
        startActivityForResult(intent, 1001);
        
        Toast.makeText(this, "正在测试启动性能...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            long measureTime = data.getLongExtra("measure_time", 0);
            if (measureTime > 0) {
                showPerformanceLog(measureTime);
            }
        }
    }

    /**
     * 显示性能日志
     */
    public void showPerformanceLog(long measureTime) {
        long totalDuration = measureTime - startupTime;
        
        String log = "=== 性能埋点 ===\n" +
                "时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n" +
                "页面名称: PerformanceMeasureActivity\n" +
                "启动耗时: " + totalDuration + " 毫秒\n" +
                "异常类型: PerformanceInfo\n" +
                "异常信息: 页面启动耗时 " + totalDuration + "ms";
        
        tvPerformanceLog.setText(log);
        savePerformanceLog(log);
    }

    /**
     * 保存性能日志到本地文件
     */
    private void savePerformanceLog(String log) {
        String fileName = "performance_trace_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(log.getBytes());
            fos.close();
            Toast.makeText(this, "性能日志已保存: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
