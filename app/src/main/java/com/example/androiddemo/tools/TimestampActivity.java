package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimestampActivity extends AppCompatActivity {

    private EditText etTimestamp;
    private EditText etDateTime;
    private TextView tvResult;
    private TextView tvMilliseconds;
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timestamp);

        etTimestamp = findViewById(R.id.et_timestamp);
        etDateTime = findViewById(R.id.et_datetime);
        tvResult = findViewById(R.id.tv_result);
        tvMilliseconds = findViewById(R.id.tv_milliseconds);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("时间戳转换");
        }

        long currentTime = System.currentTimeMillis();
        etTimestamp.setText(String.valueOf(currentTime / 1000));
        etDateTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(currentTime)));

        setupListeners();
        convertTimestamp();
    }

    private void setupListeners() {
        etTimestamp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    isUpdating = true;
                    convertTimestamp();
                    isUpdating = false;
                }
            }
        });

        etDateTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    isUpdating = true;
                    convertDateTime();
                    isUpdating = false;
                }
            }
        });
    }

    private void convertTimestamp() {
        String timestampStr = etTimestamp.getText().toString();
        if (timestampStr.isEmpty()) {
            tvResult.setText("请输入时间戳");
            tvMilliseconds.setText("--");
            return;
        }

        try {
            long timestamp = Long.parseLong(timestampStr);
            long milliseconds = timestamp * 1000;
            Date date = new Date(milliseconds);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat msSdf = new SimpleDateFormat("SSS", Locale.getDefault());

            tvResult.setText(sdf.format(date));
            tvMilliseconds.setText("毫秒时间戳: " + milliseconds);
        } catch (NumberFormatException e) {
            tvResult.setText("无效的时间戳");
            tvMilliseconds.setText("--");
        }
    }

    private void convertDateTime() {
        String dateTimeStr = etDateTime.getText().toString();
        if (dateTimeStr.isEmpty()) {
            tvResult.setText("请输入日期时间");
            tvMilliseconds.setText("--");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateTimeStr);
            if (date != null) {
                long timestamp = date.getTime() / 1000;
                tvResult.setText("时间戳: " + timestamp);
                tvMilliseconds.setText("毫秒时间戳: " + date.getTime());
            }
        } catch (Exception e) {
            tvResult.setText("无效的日期格式");
            tvMilliseconds.setText("--");
        }
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}