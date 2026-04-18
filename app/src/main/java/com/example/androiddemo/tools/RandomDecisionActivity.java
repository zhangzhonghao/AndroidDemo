package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomDecisionActivity extends AppCompatActivity {

    private EditText etOptions;
    private TextView tvResult;
    private Button btnSelect, btnClear;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_decision);

        initViews();
    }

    private void initViews() {
        etOptions = findViewById(R.id.et_options);
        tvResult = findViewById(R.id.tv_result);
        btnSelect = findViewById(R.id.btn_select);
        btnClear = findViewById(R.id.btn_clear);

        btnSelect.setOnClickListener(v -> selectRandom());
        btnClear.setOnClickListener(v -> clear());
    }

    private void selectRandom() {
        String optionsStr = etOptions.getText().toString().trim();

        if (optionsStr.isEmpty()) {
            Toast.makeText(this, "请输入选项", Toast.LENGTH_SHORT).show();
            tvResult.setText("？");
            return;
        }

        String[] lines = optionsStr.split("\n");
        List<String> validOptions = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                validOptions.add(trimmed);
            }
        }

        if (validOptions.isEmpty()) {
            Toast.makeText(this, "请输入有效的选项", Toast.LENGTH_SHORT).show();
            tvResult.setText("？");
            return;
        }

        if (validOptions.size() == 1) {
            Toast.makeText(this, "只有一个选项，无需选择", Toast.LENGTH_SHORT).show();
            tvResult.setText(validOptions.get(0));
            return;
        }

        int index = random.nextInt(validOptions.size());
        tvResult.setText(validOptions.get(index));
    }

    private void clear() {
        etOptions.setText("");
        tvResult.setText("？");
    }
}