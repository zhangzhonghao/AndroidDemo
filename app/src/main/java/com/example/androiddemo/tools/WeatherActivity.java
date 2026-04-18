package com.example.androiddemo.tools;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.example.androiddemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {

    private static final String API_KEY = "d83a97c40b4e4e4db8d3d93c66e88888";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    private EditText etCitySearch;
    private Button btnSearch;
    private Button btnLocation;

    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvWeatherCondition;
    private ImageView ivWeatherIcon;
    private TextView tvHumidity;
    private TextView tvWindSpeed;
    private TextView tvPressure;
    private TextView tvFeelsLike;

    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;

    private ProgressBar progressBar;
    private LinearLayout layoutCurrentWeather;
    private LinearLayout layoutForecast;

    private AMapLocationClient locationClient;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        mainHandler = new Handler(Looper.getMainLooper());

        initViews();
        setupToolbar();
        setupListeners();

        // 默认加载北京天气
        fetchWeather("Beijing");
    }

    private void initViews() {
        etCitySearch = findViewById(R.id.et_city_search);
        btnSearch = findViewById(R.id.btn_search);
        btnLocation = findViewById(R.id.btn_location);

        tvCityName = findViewById(R.id.tv_city_name);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvWeatherCondition = findViewById(R.id.tv_weather_condition);
        ivWeatherIcon = findViewById(R.id.iv_weather_icon);
        tvHumidity = findViewById(R.id.tv_humidity);
        tvWindSpeed = findViewById(R.id.tv_wind_speed);
        tvPressure = findViewById(R.id.tv_pressure);
        tvFeelsLike = findViewById(R.id.tv_feels_like);

        progressBar = findViewById(R.id.progress_bar);
        layoutCurrentWeather = findViewById(R.id.layout_current_weather);
        layoutForecast = findViewById(R.id.layout_forecast);

        rvForecast = findViewById(R.id.rv_forecast);
        rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        forecastAdapter = new ForecastAdapter();
        rvForecast.setAdapter(forecastAdapter);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("天气查询");
        }
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> {
            String city = etCitySearch.getText().toString().trim();
            if (!TextUtils.isEmpty(city)) {
                fetchWeather(city);
            } else {
                Toast.makeText(this, "请输入城市名称", Toast.LENGTH_SHORT).show();
            }
        });

        btnLocation.setOnClickListener(v -> {
            if (checkLocationPermission()) {
                getCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        try {
            locationClient = new AMapLocationClient(this);
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);
            locationClient.setLocationOption(option);
            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                        double lat = aMapLocation.getLatitude();
                        double lon = aMapLocation.getLongitude();
                        fetchWeatherByCoords(lat, lon);
                    } else {
                        mainHandler.post(() -> Toast.makeText(WeatherActivity.this,
                                "获取位置失败", Toast.LENGTH_SHORT).show());
                    }
                    if (locationClient != null) {
                        locationClient.stopLocation();
                    }
                }
            });
            locationClient.startLocation();
        } catch (Exception e) {
            Toast.makeText(this, "定位服务不可用", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        layoutCurrentWeather.setVisibility(View.GONE);
        layoutForecast.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        layoutCurrentWeather.setVisibility(View.VISIBLE);
        layoutForecast.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void fetchWeather(String city) {
        showLoading();
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "/weather?q=" + URLEncoder.encode(city, "UTF-8")
                        + "&appid=" + API_KEY + "&units=metric&lang=zh_cn";
                JSONObject result = fetchJson(urlStr);

                if (result.has("cod") && result.getInt("cod") == 200) {
                    parseAndDisplayCurrentWeather(result);
                    fetchForecast(city);
                } else {
                    mainHandler.post(() -> showError("未找到该城市"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> showError("网络错误: " + e.getMessage()));
            }
        }).start();
    }

    private void fetchWeatherByCoords(double lat, double lon) {
        showLoading();
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "/weather?lat=" + lat + "&lon=" + lon
                        + "&appid=" + API_KEY + "&units=metric&lang=zh_cn";
                JSONObject result = fetchJson(urlStr);

                if (result.has("cod") && result.getInt("cod") == 200) {
                    parseAndDisplayCurrentWeather(result);
                    String cityName = result.optString("name", "");
                    if (!TextUtils.isEmpty(cityName)) {
                        fetchForecastByCoords(lat, lon);
                    }
                } else {
                    mainHandler.post(() -> showError("获取天气失败"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> showError("网络错误: " + e.getMessage()));
            }
        }).start();
    }

    private void fetchForecast(String city) {
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "/forecast?q=" + URLEncoder.encode(city, "UTF-8")
                        + "&appid=" + API_KEY + "&units=metric&lang=zh_cn";
                JSONObject result = fetchJson(urlStr);
                parseAndDisplayForecast(result);
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(WeatherActivity.this,
                        "获取预报失败", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchForecastByCoords(double lat, double lon) {
        new Thread(() -> {
            try {
                String urlStr = BASE_URL + "/forecast?lat=" + lat + "&lon=" + lon
                        + "&appid=" + API_KEY + "&units=metric&lang=zh_cn";
                JSONObject result = fetchJson(urlStr);
                parseAndDisplayForecast(result);
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(WeatherActivity.this,
                        "获取预报失败", Toast.LENGTH_SHORT).show());
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

    private void parseAndDisplayCurrentWeather(JSONObject result) {
        try {
            JSONObject main = result.getJSONObject("main");
            JSONArray weather = result.getJSONArray("weather");
            JSONObject wind = result.optJSONObject("wind");
            JSONObject sys = result.optJSONObject("sys");

            String cityName = result.optString("name", "");
            String country = "";
            if (sys != null) {
                country = sys.optString("country", "");
            }

            double temp = main.optDouble("temp", 0);
            double feelsLike = main.optDouble("feels_like", 0);
            int humidity = main.optInt("humidity", 0);
            int pressure = main.optInt("pressure", 0);
            double windSpeed = wind != null ? wind.optDouble("speed", 0) : 0;

            String condition = "";
            String icon = "";
            if (weather.length() > 0) {
                JSONObject weatherObj = weather.getJSONObject(0);
                condition = weatherObj.optString("description", "");
                icon = weatherObj.optString("icon", "");
            }

            final String finalCityName = cityName + (TextUtils.isEmpty(country) ? "" : ", " + country);
            final String finalCondition = condition;
            final String finalIcon = icon;

            mainHandler.post(() -> {
                tvCityName.setText(finalCityName);
                tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°", temp));
                tvWeatherCondition.setText(finalCondition);
                tvHumidity.setText("湿度: " + humidity + "%");
                tvWindSpeed.setText("风速: " + String.format(Locale.getDefault(), "%.1f m/s", windSpeed));
                tvPressure.setText("气压: " + pressure + " hPa");
                tvFeelsLike.setText("体感: " + String.format(Locale.getDefault(), "%.0f°", feelsLike));

                loadWeatherIcon(finalIcon);
                showContent();
            });
        } catch (JSONException e) {
            mainHandler.post(() -> showError("解析天气数据失败"));
        }
    }

    private void parseAndDisplayForecast(JSONObject result) {
        try {
            List<ForecastItem> forecastList = new ArrayList<>();
            JSONArray list = result.optJSONArray("list");

            if (list != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat daySdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
                SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

                String lastDate = "";
                for (int i = 0; i < list.length(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    String dtTxt = item.optString("dt_txt", "");

                    try {
                        Date date = sdf.parse(dtTxt);
                        if (date != null) {
                            String dateStr = daySdf.format(date);
                            String timeStr = timeSdf.format(date);

                            // 只取每天中午12点的预报
                            if ("12:00".equals(timeStr) && !dateStr.equals(lastDate)) {
                                lastDate = dateStr;

                                JSONObject main = item.getJSONObject("main");
                                JSONArray weather = item.getJSONArray("weather");

                                double temp = main.optDouble("temp", 0);
                                String condition = "";
                                String icon = "";
                                if (weather.length() > 0) {
                                    JSONObject weatherObj = weather.getJSONObject(0);
                                    condition = weatherObj.optString("description", "");
                                    icon = weatherObj.optString("icon", "");
                                }

                                ForecastItem forecastItem = new ForecastItem();
                                forecastItem.date = dateStr;
                                forecastItem.dayOfWeek = getDayOfWeek(date);
                                forecastItem.temp = temp;
                                forecastItem.condition = condition;
                                forecastItem.icon = icon;
                                forecastList.add(forecastItem);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            final List<ForecastItem> finalList = forecastList;
            mainHandler.post(() -> forecastAdapter.setData(finalList));

        } catch (JSONException e) {
            mainHandler.post(() -> Toast.makeText(this, "解析预报数据失败", Toast.LENGTH_SHORT).show());
        }
    }

    private String getDayOfWeek(Date date) {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        int dayOfWeek = date.getDay();
        return weekDays[dayOfWeek];
    }

    private void loadWeatherIcon(String iconCode) {
        // 使用 OpenWeatherMap 图标
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        new Thread(() -> {
            try {
                URL url = new URL(iconUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                android.graphics.drawable.Drawable drawable =
                        android.graphics.drawable.Drawable.createFromStream(
                                conn.getInputStream(), "weather_icon");

                mainHandler.post(() -> {
                    if (drawable != null) {
                        ivWeatherIcon.setImageDrawable(drawable);
                    }
                });
                conn.disconnect();
            } catch (Exception e) {
                // 图标加载失败，使用默认
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
    }

    // 预报数据项
    static class ForecastItem {
        String date;
        String dayOfWeek;
        double temp;
        String condition;
        String icon;
    }

    // 预报适配器
    class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

        private List<ForecastItem> items = new ArrayList<>();

        void setData(List<ForecastItem> data) {
            items = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_forecast, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ForecastItem item = items.get(position);
            holder.tvDate.setText(item.date);
            holder.tvDayOfWeek.setText(item.dayOfWeek);
            holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.temp));
            holder.tvCondition.setText(item.condition);
            loadForecastIcon(holder.ivIcon, item.icon);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate;
            TextView tvDayOfWeek;
            TextView tvTemp;
            TextView tvCondition;
            ImageView ivIcon;

            ViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date);
                tvDayOfWeek = itemView.findViewById(R.id.tv_day_of_week);
                tvTemp = itemView.findViewById(R.id.tv_temp);
                tvCondition = itemView.findViewById(R.id.tv_condition);
                ivIcon = itemView.findViewById(R.id.iv_icon);
            }
        }

        private void loadForecastIcon(ImageView imageView, String iconCode) {
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
            new Thread(() -> {
                try {
                    URL url = new URL(iconUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    android.graphics.drawable.Drawable drawable =
                            android.graphics.drawable.Drawable.createFromStream(
                                    conn.getInputStream(), "forecast_icon");

                    mainHandler.post(() -> {
                        if (drawable != null) {
                            imageView.setImageDrawable(drawable);
                        }
                    });
                    conn.disconnect();
                } catch (Exception ignored) {
                }
            }).start();
        }
    }
}