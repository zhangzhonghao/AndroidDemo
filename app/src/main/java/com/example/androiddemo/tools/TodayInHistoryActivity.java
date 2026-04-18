package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androiddemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodayInHistoryActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.apihubs.cn/holiday/today";

    private TextView tvTitle;
    private TextView tvDate;
    private ProgressBar progressBar;
    private RecyclerView rvEvents;
    private TextView tvEmpty;

    private Handler mainHandler;
    private TodayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_in_history);

        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupToolbar();
        fetchTodayEvents();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvDate = findViewById(R.id.tv_date);
        progressBar = findViewById(R.id.progress_bar);
        rvEvents = findViewById(R.id.rv_events);
        tvEmpty = findViewById(R.id.tv_empty);

        rvEvents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TodayAdapter();
        rvEvents.setAdapter(adapter);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("历史今天");
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvEvents.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        rvEvents.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty(String message) {
        progressBar.setVisibility(View.GONE);
        rvEvents.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvEmpty.setText(message);
    }

    private void fetchTodayEvents() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String dateStr = new SimpleDateFormat("M月d日", Locale.getDefault()).format(calendar.getTime());
        tvDate.setText(dateStr);

        showLoading();

        new Thread(() -> {
            try {
                String urlStr = API_URL + "?month=" + month + "&day=" + day;
                JSONObject result = fetchJson(urlStr);

                if (result.has("code") && result.getInt("code") == 200) {
                    JSONArray data = result.optJSONArray("data");
                    if (data != null && data.length() > 0) {
                        List<HistoryEvent> events = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            HistoryEvent event = new HistoryEvent();
                            event.year = item.optString("year", "");
                            event.content = item.optString("title", item.optString("holiday", ""));
                            if (item.has("lunar")) {
                                event.content = item.optString("lunar", "") + " " + event.content;
                            }
                            if (item.has("desc")) {
                                String desc = item.optString("desc", "");
                                if (!desc.isEmpty()) {
                                    event.content = event.content + "\n" + desc;
                                }
                            }
                            events.add(event);
                        }

                        final List<HistoryEvent> finalEvents = events;
                        mainHandler.post(() -> {
                            adapter.setData(finalEvents);
                            showContent();
                        });
                    } else {
                        mainHandler.post(() -> showEmpty("今天没有历史事件"));
                    }
                } else {
                    mainHandler.post(() -> showEmpty("获取数据失败"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> showEmpty("网络错误: " + e.getMessage()));
            }
        }).start();
    }

    private JSONObject fetchJson(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        conn.disconnect();

        return new JSONObject(response.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class HistoryEvent {
        String year;
        String content;
    }

    class TodayAdapter extends RecyclerView.Adapter<TodayAdapter.ViewHolder> {

        private List<HistoryEvent> items = new ArrayList<>();

        void setData(List<HistoryEvent> data) {
            items = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_today_in_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryEvent item = items.get(position);
            holder.tvYear.setText(item.year + "年");
            holder.tvContent.setText(item.content);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvYear;
            TextView tvContent;

            ViewHolder(View itemView) {
                super(itemView);
                tvYear = itemView.findViewById(R.id.tv_year);
                tvContent = itemView.findViewById(R.id.tv_content);
            }
        }
    }
}