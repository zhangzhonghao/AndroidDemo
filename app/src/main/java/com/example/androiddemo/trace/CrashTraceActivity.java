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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Crash埋点页面
 * 功能：点击按钮触发空指针异常，并将异常日志保存到本地文件
 */
public class CrashTraceActivity extends AppCompatActivity {

    private TextView tvCrashLog;
    private Button btnTriggerCrash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_crash);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvCrashLog = findViewById(R.id.tv_crash_log);
        btnTriggerCrash = findViewById(R.id.btn_trigger_crash);
    }

    private void setupListeners() {
        btnTriggerCrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggerCrash();
            }
        });
    }

    /**
     * 触发空指针异常并保存日志
     */
    private void triggerCrash() {
        try {
            // 故意触发空指针异常
            String str = null;
            int length = str.length();
        } catch (NullPointerException e) {
            // 获取栈轨迹信息
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString();

            // 显示日志
            String log = "=== Crash埋点 ===\n" +
                    "时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n" +
                    "异常类型: NullPointerException\n" +
                    "异常信息: " + e.getMessage() + "\n" +
                    "栈轨迹:\n" + stackTrace;
            tvCrashLog.setText(log);

            // 保存到文件
            saveCrashLog(log);
        }
    }

    /**
     * 保存Crash日志到本地文件
     */
    private void saveCrashLog(String log) {
        String fileName = "crash_trace_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(log.getBytes());
            fos.close();
            Toast.makeText(this, "Crash日志已保存: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
