package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.File;

public class JsonFormatterActivity extends AppCompatActivity {
    private EditText etInput;
    private TextView tvOutput;
    private Button btnFormat, btnCompress, btnValidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_formatter);
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        btnFormat = findViewById(R.id.btn_format);
        btnCompress = findViewById(R.id.btn_compress);
        btnValidate = findViewById(R.id.btn_validate);
    }

    public void format(View view) {
        String input = etInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            tvOutput.setText("请输入JSON内容");
            return;
        }
        try {
            if (input.trim().startsWith("[")) {
                JSONArray arr = new JSONArray(input);
                tvOutput.setText(arr.toString(2));
            } else {
                JSONObject obj = new JSONObject(input);
                tvOutput.setText(obj.toString(2));
            }
        } catch (Exception e) {
            tvOutput.setText("无效的JSON格式:\n" + e.getMessage());
        }
    }

    public void compress(View view) {
        String input = etInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            tvOutput.setText("请输入JSON内容");
            return;
        }
        try {
            Object json = new JSONArray(input).length() > 0 ? new JSONArray(input) : new JSONObject(input);
            tvOutput.setText(json.toString());
        } catch (Exception e) {
            tvOutput.setText("无效的JSON格式:\n" + e.getMessage());
        }
    }

    public void validate(View view) {
        String input = etInput.getText().toString();
        if (TextUtils.isEmpty(input)) {
            tvOutput.setText("请输入JSON内容");
            return;
        }
        try {
            if (input.startsWith("[")) {
                new JSONArray(input);
                tvOutput.setText("✓ 有效的JSON数组");
            } else {
                new JSONObject(input);
                tvOutput.setText("✓ 有效的JSON对象");
            }
        } catch (Exception e) {
            tvOutput.setText("✗ 无效的JSON:\n" + e.getMessage());
        }
    }
}