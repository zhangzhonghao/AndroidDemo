package com.example.androiddemo.trace;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * 自定义埋点页面
 * 功能：用户输入文本，点击记录按钮后将内容保存到本地文件
 */
public class CustomTraceActivity extends AppCompatActivity {

    private EditText etTraceContent;
    private Button btnRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trace_custom);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTraceContent = findViewById(R.id.et_trace_content);
        btnRecord = findViewById(R.id.btn_record);
    }

    private void setupListeners() {
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordTrace();
            }
        });
    }

    /**
     * 记录埋点内容到本地文件
     */
    private void recordTrace() {
        String content = etTraceContent.getText().toString().trim();
        
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        // 生成文件名：custom_trace_时间戳.txt
        String fileName = "custom_trace_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".txt";
        File file = new File(getFilesDir(), fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            String recordContent = "=== 自定义埋点 ===\n" +
                    "时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n" +
                    "内容: " + content + "\n";
            fos.write(recordContent.getBytes());
            fos.close();
            
            Toast.makeText(this, "记录成功: " + fileName, Toast.LENGTH_SHORT).show();
            etTraceContent.setText("");
        } catch (IOException e) {
            Toast.makeText(this, "记录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
