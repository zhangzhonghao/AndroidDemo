package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatchRenameActivity extends AppCompatActivity {

    private EditText etPath;
    private EditText etPattern;
    private EditText etReplacement;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_rename);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("批量重命名");
        }

        initViews();
    }

    private void initViews() {
        etPath = findViewById(R.id.et_path);
        etPattern = findViewById(R.id.et_pattern);
        etReplacement = findViewById(R.id.et_replacement);
        tvResult = findViewById(R.id.tv_result);
        Button btnPreview = findViewById(R.id.btn_preview);
        Button btnRename = findViewById(R.id.btn_rename);

        etPath.setText(Environment.getExternalStorageDirectory().getPath());

        btnPreview.setOnClickListener(v -> preview());
        btnRename.setOnClickListener(v -> rename());
    }

    private void preview() {
        String path = etPath.getText().toString().trim();
        String pattern = etPattern.getText().toString();
        String replacement = etReplacement.getText().toString();

        if (path.isEmpty()) {
            tvResult.setText("请输入路径");
            return;
        }

        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            tvResult.setText("路径不存在或不是目录");
            return;
        }

        if (pattern.isEmpty()) {
            tvResult.setText("请输入查找模式");
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            tvResult.setText("目录为空");
            return;
        }

        StringBuilder result = new StringBuilder("预览:\n\n");
        int count = 0;

        for (File file : files) {
            String oldName = file.getName();
            String newName;

            if (pattern.startsWith("^")) {
                // 正则替换
                try {
                    newName = oldName.replaceFirst(pattern, replacement);
                } catch (Exception e) {
                    newName = oldName;
                }
            } else {
                // 普通字符串替换
                newName = oldName.replace(pattern, replacement);
            }

            if (!oldName.equals(newName)) {
                result.append(oldName).append("\n  -> ").append(newName).append("\n\n");
                count++;
            }
        }

        if (count == 0) {
            result.append("没有匹配的文件");
        } else {
            result.append("共 ").append(count).append(" 个文件将被重命名");
        }

        tvResult.setText(result.toString());
    }

    private void rename() {
        String path = etPath.getText().toString().trim();
        String pattern = etPattern.getText().toString();
        String replacement = etReplacement.getText().toString();

        if (path.isEmpty() || pattern.isEmpty()) {
            tvResult.setText("请输入路径和查找模式");
            return;
        }

        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            tvResult.setText("路径不存在或不是目录");
            return;
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            tvResult.setText("目录为空");
            return;
        }

        StringBuilder result = new StringBuilder("重命名结果:\n\n");
        int success = 0;
        int failed = 0;

        for (File file : files) {
            String oldName = file.getName();
            String newName;

            try {
                if (pattern.startsWith("^")) {
                    newName = oldName.replaceFirst(pattern, replacement);
                } else {
                    newName = oldName.replace(pattern, replacement);
                }

                if (!oldName.equals(newName)) {
                    File newFile = new File(dir, newName);
                    if (file.renameTo(newFile)) {
                        result.append("✓ ").append(oldName).append(" -> ").append(newName).append("\n");
                        success++;
                    } else {
                        result.append("✗ ").append(oldName).append(" (失败)\n");
                        failed++;
                    }
                }
            } catch (Exception e) {
                result.append("✗ ").append(oldName).append(" (异常: ").append(e.getMessage()).append(")\n");
                failed++;
            }
        }

        result.append("\n成功: ").append(success).append(", 失败: ").append(failed);
        tvResult.setText(result.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}