package com.example.androiddemo.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QrScanHistoryActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> historyList = new ArrayList<>();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan_history);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("二维码扫描历史");
        }

        prefs = getSharedPreferences("qr_scan_history", Context.MODE_PRIVATE);
        initViews();
        loadHistory();
    }

    private void initViews() {
        listView = findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String content = historyList.get(position);
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("QR", content);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "已复制: " + content, Toast.LENGTH_SHORT).show();
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            historyList.remove(position);
            saveHistory();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            return true;
        });

        Button btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(v -> {
            historyList.clear();
            saveHistory();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadHistory() {
        String history = prefs.getString("history", "");
        if (!history.isEmpty()) {
            String[] items = history.split("|||");
            Collections.addAll(historyList, items);
            adapter.notifyDataSetChanged();
        }
    }

    private void saveHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < historyList.size(); i++) {
            if (i > 0) sb.append("|||");
            sb.append(historyList.get(i));
        }
        prefs.edit().putString("history", sb.toString()).apply();
    }

    public void addScanResult(String content) {
        historyList.add(0, content);
        if (historyList.size() > 100) {
            historyList.remove(historyList.size() - 1);
        }
        saveHistory();
        adapter.notifyDataSetChanged();
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