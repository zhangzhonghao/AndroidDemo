package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Locale;

public class HeightPredictorActivity extends AppCompatActivity {

    private EditText etFatherHeight;
    private EditText etMotherHeight;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height_predictor);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("儿童身高预测");
        }
    }

    private void initViews() {
        etFatherHeight = findViewById(R.id.et_father_height);
        etMotherHeight = findViewById(R.id.et_mother_height);
        tvResult = findViewById(R.id.tv_result);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                predictHeight();
            }
        };

        etFatherHeight.addTextChangedListener(watcher);
        etMotherHeight.addTextChangedListener(watcher);
    }

    private void predictHeight() {
        String fatherStr = etFatherHeight.getText().toString();
        String motherStr = etMotherHeight.getText().toString();

        if (fatherStr.isEmpty() || motherStr.isEmpty()) {
            tvResult.setText("请输入父母身高");
            return;
        }

        try {
            double father = Double.parseDouble(fatherStr);
            double mother = Double.parseDouble(motherStr);

            if (father <= 0 || mother <= 0) {
                tvResult.setText("请输入有效的身高");
                return;
            }

            // 男孩身高 = (父亲身高 + 母亲身高 + 13) / 2
            // 女孩身高 = (父亲身高 + 母亲身高 - 13) / 2
            double boyHeight = (father + mother + 13) / 2;
            double girlHeight = (father + mother - 13) / 2;

            String result = String.format(Locale.getDefault(),
                    "男孩预测身高: %.1f cm\n女孩预测身高: %.1f cm\n\n身高范围: ±5cm",
                    boyHeight, girlHeight);

            tvResult.setText(result);
        } catch (NumberFormatException e) {
            tvResult.setText("请输入有效的身高");
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