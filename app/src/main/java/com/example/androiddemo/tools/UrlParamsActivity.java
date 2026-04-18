package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class UrlParamsActivity extends AppCompatActivity {

    private EditText etUrl;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_params);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("URL参数解析");
        }

        etUrl = findViewById(R.id.et_url);
        tvResult = findViewById(R.id.tv_result);
        Button btnParse = findViewById(R.id.btn_parse);

        btnParse.setOnClickListener(v -> parse());
    }

    private void parse() {
        String url = etUrl.getText().toString().trim();
        if (url.isEmpty()) {
            tvResult.setText("请输入URL");
            return;
        }

        try {
            int idx = url.indexOf("?");
            if (idx == -1) {
                tvResult.setText("URL中没有参数");
                return;
            }

            String query = url.substring(idx + 1);
            String[] params = query.split("&");
            StringBuilder sb = new StringBuilder();

            for (String param : params) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name());
                    sb.append(key).append(" = ").append(value).append("\n");
                } else {
                    sb.append(param).append("\n");
                }
            }
            tvResult.setText(sb.toString());
        } catch (Exception e) {
            tvResult.setText("解析失败");
        }
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
