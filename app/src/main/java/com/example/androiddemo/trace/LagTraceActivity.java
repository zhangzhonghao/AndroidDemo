package com.example.androiddemo.trace;

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
 * 卡顿埋点页面
 * 功能：点击按钮在主线程执行耗时操作，模拟卡顿，并将日志保存到本地文件
 */
public class LagTraceActivity extends AppCompatActivity {

    private TextView tvLagLog;
    private Button btnTriggerLag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_lag);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvLagLog = findViewById(R.id.tv_lag_log);
        btnTriggerLag = findViewById(R.id.btn_trigger_lag);
    }

    private void setupListeners() {
        btnTriggerLag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerLag();
            }
        });
    }

    /**
     * 触发卡顿（在主线程执行耗时操作）
     */
    private void triggerLag() {
        btnTriggerLag.setEnabled(false);
        
        long startTime = System.currentTimeMillis();
        
        // 在主线程执行耗时操作，模拟卡顿
        // 执行一个大约3秒的耗时计算
        try {
            performHeavyOperation();
        } catch (Exception e) {
            // 记录异常
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        // 构造卡顿日志
        String log = "=== 卡顿埋点 ===\n" +
                "时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n" +
                "卡顿时长: " + duration + " 毫秒\n" +
                "异常类型: LagException\n" +
                "异常信息: 主线程执行耗时操作导致UI卡顿 " + duration + "ms\n" +
                "栈轨迹:\n" +
                "com.example.androiddemo.trace.LagTraceActivity.triggerLag(LagTraceActivity.java:55)\n" +
                "com.example.androiddemo.trace.LagTraceActivity.performHeavyOperation(LagTraceActivity.java:70)\n" +
                "android.os.Handler.handleCallback(Handler.java:938)\n" +
                "android.os.Handler.dispatchMessage(Handler.java:99)\n" +
                "android.os.Looper.loop(Looper.java:264)\n" +
                "android.app.ActivityThread.main(ActivityThread.java:8282)\n" +
                "java.lang.reflect.Method.invoke(Native Method)\n" +
                "com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:632)\n" +
                "com.android.internal.os.ZygoteInit.main(ZygoteInit.java.java:1049)";
        
        tvLagLog.setText(log);
        saveLagLog(log);
        btnTriggerLag.setEnabled(true);
    }

    /**
     * 执行耗时操作（模拟卡顿）
     */
    private void performHeavyOperation() {
        // 模拟耗时操作：执行大量计算
        long start = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < 100000000; i++) {
            count += i;
            // 每隔一段时间让出CPU（模拟实际场景）
            if (i % 10000000 == 0) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Heavy operation completed in " + duration + "ms, count=" + count);
    }

    /**
     * 保存卡顿日志到本地文件
     */
    private void saveLagLog(String log) {
        String fileName = "lag_trace_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(log.getBytes());
            fos.close();
            Toast.makeText(this, "卡顿日志已保存: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
