package com.example.androiddemo.trace;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 白屏埋点页面
 * 功能：点击按钮触发白屏5秒（模拟延迟），并保存异常日志到本地文件
 */
public class WhiteScreenTraceActivity extends AppCompatActivity {

    private TextView tvWhiteScreenLog;
    private Button btnTriggerWhiteScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_white_screen);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvWhiteScreenLog = findViewById(R.id.tv_white_screen_log);
        btnTriggerWhiteScreen = findViewById(R.id.btn_trigger_white_screen);
    }

    private void setupListeners() {
        btnTriggerWhiteScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerWhiteScreen();
            }
        });
    }

    /**
     * 触发白屏（通过延迟操作模拟）
     */
    private void triggerWhiteScreen() {
        btnTriggerWhiteScreen.setEnabled(false);
        
        long startTime = System.currentTimeMillis();
        
        // 模拟白屏：使用Handler延迟5秒执行操作
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                long duration = System.currentTimeMillis() - startTime;
                
                // 构造异常信息（模拟白屏异常）
                String exceptionMessage = "WhiteScreenException: 页面白屏 " + (duration / 1000) + " 秒";
                String stackTrace = "com.example.androiddemo.trace.WhiteScreenTraceActivity.triggerWhiteScreen(WhiteScreenTraceActivity.java:55)\n" +
                        "android.os.Handler.handleCallback(Handler.java:938)\n" +
                        "android.os.Handler.dispatchMessage(Handler.java:99)\n" +
                        "android.os.Looper.loop(Looper.java:264)\n" +
                        "android.app.ActivityThread.main(ActivityThread.java:8282)\n" +
                        "java.lang.reflect.Method.invoke(Native Method)\n" +
                        "com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:632)\n" +
                        "com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1049)";
                
                String log = "=== 白屏埋点 ===\n" +
                        "时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n" +
                        "白屏时长: " + (duration / 1000) + " 秒\n" +
                        "异常类型: WhiteScreenException\n" +
                        "异常信息: " + exceptionMessage + "\n" +
                        "栈轨迹:\n" + stackTrace;
                
                tvWhiteScreenLog.setText(log);
                saveWhiteScreenLog(log);
                btnTriggerWhiteScreen.setEnabled(true);
            }
        }, 5000); // 5秒延迟模拟白屏
    }

    /**
     * 保存白屏日志到本地文件
     */
    private void saveWhiteScreenLog(String log) {
        String fileName = "white_screen_trace_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(log.getBytes());
            fos.close();
            Toast.makeText(this, "白屏日志已保存: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
