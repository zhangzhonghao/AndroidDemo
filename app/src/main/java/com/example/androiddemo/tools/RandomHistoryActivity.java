package com.example.androiddemo.tools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RandomHistoryActivity extends AppCompatActivity {
    private ListView lvHistory;
    private TextView tvEmpty;
    private SharedPreferences sp;
    private List<String> historyList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_history);
        lvHistory = findViewById(R.id.lv_history);
        tvEmpty = findViewById(R.id.tv_empty);
        sp = getSharedPreferences("random_history", MODE_PRIVATE);
        loadHistory();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        lvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        int count = sp.getInt("count", 0);
        if (count == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvHistory.setVisibility(View.GONE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);
        lvHistory.setVisibility(View.VISIBLE);
        for (int i = count - 1; i >= 0; i--) {
            historyList.add(sp.getString("item_" + i, ""));
        }
    }

    public void clearHistory(View view) {
        sp.edit().clear().apply();
        historyList.clear();
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(View.VISIBLE);
        lvHistory.setVisibility(View.GONE);
    }
}