package com.example.androiddemo.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BmiCalculatorActivity extends AppCompatActivity {

    private EditText etHeight;
    private EditText etWeight;
    private RadioGroup rgHeightUnit;
    private RadioGroup rgWeightUnit;
    private RadioGroup rgSystem;
    private TextView tvBmiValue;
    private TextView tvBmiCategory;
    private LinearProgressIndicator progressBmi;
    private RecyclerView rvHistory;
    private BmiHistoryAdapter historyAdapter;

    private boolean isMetric = true;
    private List<BmiRecord> historyList = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        prefs = getSharedPreferences("bmi_history", Context.MODE_PRIVATE);

        initViews();
        setupListeners();
        loadHistory();
    }

    private void initViews() {
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        rgHeightUnit = findViewById(R.id.rg_height_unit);
        rgWeightUnit = findViewById(R.id.rg_weight_unit);
        rgSystem = findViewById(R.id.rg_system);
        tvBmiValue = findViewById(R.id.tv_bmi_value);
        tvBmiCategory = findViewById(R.id.tv_bmi_category);
        progressBmi = findViewById(R.id.progress_bmi);
        rvHistory = findViewById(R.id.rv_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("BMI计算器");
        }

        historyAdapter = new BmiHistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);
    }

    private void setupListeners() {
        rgSystem.setOnCheckedChangeListener((group, checkedId) -> {
            isMetric = (checkedId == R.id.rb_metric);
            updateUnitLabels();
            calculateBmi();
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateBmi();
            }
        };

        etHeight.addTextChangedListener(watcher);
        etWeight.addTextChangedListener(watcher);

        rgHeightUnit.setOnCheckedChangeListener((group, checkedId) -> calculateBmi());
        rgWeightUnit.setOnCheckedChangeListener((group, checkedId) -> calculateBmi());
    }

    private void updateUnitLabels() {
        if (isMetric) {
            findViewById(R.id.tv_height_unit_label).setVisibility(View.GONE);
            findViewById(R.id.tv_weight_unit_label).setVisibility(View.GONE);
            rgHeightUnit.setVisibility(View.GONE);
            rgWeightUnit.setVisibility(View.GONE);
        } else {
            findViewById(R.id.tv_height_unit_label).setVisibility(View.VISIBLE);
            findViewById(R.id.tv_weight_unit_label).setVisibility(View.VISIBLE);
            rgHeightUnit.setVisibility(View.VISIBLE);
            rgWeightUnit.setVisibility(View.VISIBLE);
        }
    }

    private void calculateBmi() {
        String heightStr = etHeight.getText().toString();
        String weightStr = etWeight.getText().toString();

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            resetDisplay();
            return;
        }

        try {
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);

            // 转换为公制单位进行计算
            if (isMetric) {
                // 身高cm转m，体重kg
                height = height / 100.0;
            } else {
                // 英制转换
                boolean isCm = rgHeightUnit.getCheckedRadioButtonId() == R.id.rb_height_cm;
                boolean isKg = rgWeightUnit.getCheckedRadioButtonId() == R.id.rb_weight_kg;

                height = isCm ? height / 100.0 : height * 0.3048; // cm或feet转m
                weight = isKg ? weight : weight * 0.453592; // kg或lb转kg
            }

            if (height <= 0 || weight <= 0) {
                resetDisplay();
                return;
            }

            double bmi = weight / (height * height);
            displayBmi(bmi);
            saveToHistory(bmi);
        } catch (NumberFormatException e) {
            resetDisplay();
        }
    }

    private void displayBmi(double bmi) {
        // 显示BMI值
        tvBmiValue.setText(String.format(Locale.getDefault(), "%.1f", bmi));

        // 确定分类
        String category;
        int color;
        int progress;

        if (bmi < 18.5) {
            category = "偏瘦";
            color = 0xFF2196F3; // 蓝色
            progress = (int) (bmi / 18.5 * 25);
        } else if (bmi < 24) {
            category = "正常";
            color = 0xFF4CAF50; // 绿色
            progress = (int) (25 + (bmi - 18.5) / 5.5 * 25);
        } else if (bmi < 28) {
            category = "超重";
            color = 0xFFFF9800; // 橙色
            progress = (int) (50 + (bmi - 24) / 4 * 25);
        } else {
            category = "肥胖";
            color = 0xFFF44336; // 红色
            progress = Math.min(100, (int) (75 + (bmi - 28) / 10 * 25));
        }

        tvBmiCategory.setText(category);
        tvBmiCategory.setTextColor(color);
        progressBmi.setIndicatorColor(color);
        progressBmi.setProgress(Math.max(1, Math.min(100, progress)));
    }

    private void resetDisplay() {
        tvBmiValue.setText("--");
        tvBmiCategory.setText("请输入身高体重");
        tvBmiCategory.setTextColor(0xFF757575);
        progressBmi.setIndicatorColor(0xFF757575);
        progressBmi.setProgress(0);
    }

    private void saveToHistory(double bmi) {
        String heightStr = etHeight.getText().toString();
        String weightStr = etWeight.getText().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        String record = String.format(Locale.getDefault(), "%.1f|%s|%s|%s|%s",
                bmi, heightStr, weightStr, isMetric ? "metric" : "imperial", date);

        String history = prefs.getString("history", "");
        if (!history.isEmpty()) {
            history = history + "\n" + record;
        } else {
            history = record;
        }

        // 保留最近20条记录
        String[] records = history.split("\n");
        if (records.length > 20) {
            StringBuilder sb = new StringBuilder();
            for (int i = records.length - 20; i < records.length; i++) {
                if (i > records.length - 20) sb.append("\n");
                sb.append(records[i]);
            }
            history = sb.toString();
        }

        prefs.edit().putString("history", history).apply();
        loadHistory();
    }

    private void loadHistory() {
        historyList.clear();
        String history = prefs.getString("history", "");
        if (!history.isEmpty()) {
            String[] records = history.split("\n");
            for (int i = records.length - 1; i >= 0; i--) {
                String[] parts = records[i].split("\\|");
                if (parts.length >= 5) {
                    try {
                        double bmi = Double.parseDouble(parts[0]);
                        String height = parts[1];
                        String weight = parts[2];
                        String system = parts[3];
                        String date = parts[4];
                        historyList.add(new BmiRecord(bmi, height, weight, system, date));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        historyAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class BmiRecord {
        double bmi;
        String height;
        String weight;
        String system;
        String date;

        BmiRecord(double bmi, String height, String weight, String system, String date) {
            this.bmi = bmi;
            this.height = height;
            this.weight = weight;
            this.system = system;
            this.date = date;
        }
    }

    class BmiHistoryAdapter extends RecyclerView.Adapter<BmiHistoryAdapter.ViewHolder> {
        private List<BmiRecord> records;

        BmiHistoryAdapter(List<BmiRecord> records) {
            this.records = records;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            BmiRecord record = records.get(position);
            holder.text1.setText(String.format(Locale.getDefault(), "BMI: %.1f  |  %s  |  %s",
                    record.bmi, record.height, record.weight));
            holder.text2.setText(record.date);
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1;
            TextView text2;

            ViewHolder(View view) {
                super(view);
                text1 = view.findViewById(android.R.id.text1);
                text2 = view.findViewById(android.R.id.text2);
            }
        }
    }
}