package com.example.androiddemo.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WorldClockActivity extends AppCompatActivity {

    private LinearLayout containerCities;
    private Spinner spinnerCity;
    private Button btnAdd;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<TimeZone> addedTimeZones = new ArrayList<>();
    private List<View> cityViews = new ArrayList<>();

    private static final Map<String, String> CITY_MAP = new HashMap<>();

    static {
        CITY_MAP.put("北京", "Asia/Shanghai");
        CITY_MAP.put("上海", "Asia/Shanghai");
        CITY_MAP.put("香港", "Asia/Hong_Kong");
        CITY_MAP.put("东京", "Asia/Tokyo");
        CITY_MAP.put("首尔", "Asia/Seoul");
        CITY_MAP.put("新加坡", "Asia/Singapore");
        CITY_MAP.put("曼谷", "Asia/Bangkok");
        CITY_MAP.put("迪拜", "Asia/Dubai");
        CITY_MAP.put("莫斯科", "Europe/Moscow");
        CITY_MAP.put("巴黎", "Europe/Paris");
        CITY_MAP.put("伦敦", "Europe/London");
        CITY_MAP.put("柏林", "Europe/Berlin");
        CITY_MAP.put("纽约", "America/New_York");
        CITY_MAP.put("洛杉矶", "America/Los_Angeles");
        CITY_MAP.put("旧金山", "America/Los_Angeles");
        CITY_MAP.put("芝加哥", "America/Chicago");
        CITY_MAP.put("多伦多", "America/Toronto");
        CITY_MAP.put("温哥华", "America/Vancouver");
        CITY_MAP.put("悉尼", "Australia/Sydney");
        CITY_MAP.put("墨尔本", "Australia/Melbourne");
        CITY_MAP.put("奥克兰", "Pacific/Auckland");
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateAllClocks();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_clock);

        initViews();
        setupListeners();
        addDefaultCities();
    }

    private void initViews() {
        containerCities = findViewById(R.id.container_cities);
        spinnerCity = findViewById(R.id.spinner_city);
        btnAdd = findViewById(R.id.btn_add);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("世界时钟");
        }

        String[] cities = CITY_MAP.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
    }

    private void setupListeners() {
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = (String) spinnerCity.getSelectedItem();
                String tzId = CITY_MAP.get(city);
                TimeZone tz = TimeZone.getTimeZone(tzId);

                if (!addedTimeZones.contains(tz)) {
                    addedTimeZones.add(tz);
                    addCityView(city, tz);
                }
            }
        });
    }

    private void addDefaultCities() {
        addCityView("北京", TimeZone.getTimeZone("Asia/Shanghai"));
        addCityView("东京", TimeZone.getTimeZone("Asia/Tokyo"));
        addCityView("伦敦", TimeZone.getTimeZone("Europe/London"));
        addCityView("纽约", TimeZone.getTimeZone("America/New_York"));
    }

    private void addCityView(String cityName, TimeZone tz) {
        View view = getLayoutInflater().inflate(R.layout.item_world_clock_city, containerCities, false);
        TextView tvCity = view.findViewById(R.id.tv_city);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvDate = view.findViewById(R.id.tv_date);
        Button btnRemove = view.findViewById(R.id.btn_remove);

        tvCity.setText(cityName);
        tvTime.setTag(tz);
        tvDate.setTag(tz);
        btnRemove.setTag(view);

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View parentView = (View) v.getTag();
                containerCities.removeView(parentView);
                cityViews.remove(parentView);
            }
        });

        containerCities.addView(view);
        cityViews.add(view);
        updateClock(view, cityName, tz);
    }

    private void updateAllClocks() {
        for (View view : cityViews) {
            TextView tvCity = view.findViewById(R.id.tv_city);
            TextView tvTime = view.findViewById(R.id.tv_time);
            TextView tvDate = view.findViewById(R.id.tv_date);
            TimeZone tz = (TimeZone) tvTime.getTag();
            String cityName = tvCity.getText().toString();
            updateClock(view, cityName, tz);
        }
    }

    private void updateClock(View view, String cityName, TimeZone tz) {
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvDate = view.findViewById(R.id.tv_date);

        Calendar cal = Calendar.getInstance(tz);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd E", Locale.getDefault());

        tvTime.setText(timeFormat.format(cal.getTime()));
        tvDate.setText(dateFormat.format(cal.getTime()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
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