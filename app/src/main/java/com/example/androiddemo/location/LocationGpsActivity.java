package com.example.androiddemo.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.androiddemo.R;

public class LocationGpsActivity extends AppCompatActivity implements AMapLocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private AMapLocationClient locationClient;
    private Button btnGetLocation;
    private TextView tvLocationInfo;
    private TextView tvTitle;
    private boolean isLocating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ========== v11 SDK 隐私合规初始化（必须在 setContentView 之前调用）==========
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        // =============================================================================

        setContentView(R.layout.activity_location_gps);

        tvTitle = findViewById(R.id.tv_title);
        btnGetLocation = findViewById(R.id.btn_get_location);
        tvLocationInfo = findViewById(R.id.tv_location_info);

        initLocationClient();

        btnGetLocation.setOnClickListener(v -> {
            if (checkPermissions()) {
                startLocation();
            }
        });
    }

    private void initLocationClient() {
        try {
            // v11 SDK: 构造方法可能抛出 Exception
            locationClient = new AMapLocationClient(getApplicationContext());
            locationClient.setLocationListener(this);

            AMapLocationClientOption option = new AMapLocationClientOption();
            // Hight_Accuracy = GPS + 网络混合定位
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);
            option.setNeedAddress(true);
            // 使用默认超时30秒，避免网络不佳时频繁超时
            // option.setHttpTimeOut(30000);
            option.setOnceLocationLatest(true);

            locationClient.setLocationOption(option);
        } catch (Exception e) {
            Toast.makeText(this, "定位客户端初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予，点击按钮开始定位", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "定位权限被拒绝，无法获取定位", Toast.LENGTH_LONG).show();
                tvLocationInfo.setText("定位权限被拒绝，请到设置中开启");
            }
        }
    }

    private void startLocation() {
        if (locationClient == null) {
            Toast.makeText(this, "定位客户端未初始化", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isLocating) {
            Toast.makeText(this, "正在定位中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        isLocating = true;
        tvLocationInfo.setText("正在获取定位，请稍候...");
        btnGetLocation.setEnabled(false);

        locationClient.startLocation();
    }

    private void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
        }
        isLocating = false;
        runOnUiThread(() -> btnGetLocation.setEnabled(true));
    }

    /**
     * 高德SDK定位结果回调
     * AMapLocationListener 接口实现
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        stopLocation();

        if (aMapLocation == null) {
            showLocationError("定位结果为空，请重试");
            return;
        }

        int errorCode = aMapLocation.getErrorCode();
        if (errorCode == AMapLocation.LOCATION_SUCCESS) {
            double latitude = aMapLocation.getLatitude();
            double longitude = aMapLocation.getLongitude();
            String country = aMapLocation.getCountry();
            String province = aMapLocation.getProvince();
            String city = aMapLocation.getCity();
            String district = aMapLocation.getDistrict();
            String road = aMapLocation.getRoad();
            String locationDetail = aMapLocation.getLocationDetail();
            float accuracy = aMapLocation.getAccuracy();
            int locationType = aMapLocation.getLocationType();
            int gpsAccuracyStatus = aMapLocation.getGpsAccuracyStatus();
            String aoiName = aMapLocation.getAoiName();

            StringBuilder info = new StringBuilder();
            info.append("========== 定位成功 ==========\n\n");
            info.append(String.format("经度: %.6f\n", longitude));
            info.append(String.format("纬度: %.6f\n\n", latitude));
            info.append(String.format("精度: %.1f 米\n", accuracy));
            info.append(String.format("定位方式: %s\n\n", getLocationTypeDesc(locationType)));
            info.append(String.format("GPS状态: %s\n\n", getGpsStatusDesc(gpsAccuracyStatus)));

            if (country != null && !country.isEmpty()) {
                info.append(country).append(" ");
            }
            if (province != null && !province.isEmpty()) {
                info.append(province).append(" ");
            }
            if (city != null && !city.isEmpty()) {
                info.append(city).append(" ");
            }
            if (district != null && !district.isEmpty()) {
                info.append(district).append("\n");
            }
            if (road != null && !road.isEmpty()) {
                info.append(road).append("\n");
            }
            if (aoiName != null && !aoiName.isEmpty()) {
                info.append("AOI: ").append(aoiName).append("\n");
            }
            if (locationDetail != null && !locationDetail.isEmpty()) {
                info.append("详情: ").append(locationDetail);
            }

            String infoStr = info.toString();
            tvLocationInfo.setText(infoStr);

            Toast.makeText(this,
                    String.format("定位成功\n经度: %.6f\n纬度: %.6f", longitude, latitude),
                    Toast.LENGTH_LONG).show();

        } else {
            String errorInfo = "定位失败\n错误码: " + errorCode + "\n" + aMapLocation.getErrorInfo();
            showLocationError(errorInfo);
        }
    }

    private String getLocationTypeDesc(int type) {
        switch (type) {
            case AMapLocation.LOCATION_TYPE_GPS: return "GPS定位";
            case AMapLocation.LOCATION_TYPE_WIFI: return "WiFi定位";
            case AMapLocation.LOCATION_TYPE_CELL: return "基站定位";
            case AMapLocation.LOCATION_TYPE_AMAP: return "高德融合定位";
            case AMapLocation.LOCATION_TYPE_OFFLINE: return "离线定位";
            case AMapLocation.LOCATION_TYPE_FAST: return "快速定位";
            case AMapLocation.LOCATION_TYPE_FIX_CACHE: return "缓存定位";
            case AMapLocation.LOCATION_TYPE_SAME_REQ: return "同请求定位";
            case AMapLocation.LOCATION_TYPE_LAST_LOCATION_CACHE: return "最后位置缓存";
            case AMapLocation.LOCATION_TYPE_NETWORK: return "网络定位";
            case AMapLocation.LOCATION_TYPE_COARSE_LOCATION: return "粗略定位";
            default: return "未知(" + type + ")";
        }
    }

    private String getGpsStatusDesc(int status) {
        switch (status) {
            case AMapLocation.GPS_ACCURACY_GOOD: return "良好";
            case AMapLocation.GPS_ACCURACY_BAD: return "较差";
            case AMapLocation.GPS_ACCURACY_UNKNOWN: return "未知";
            default: return "未知";
        }
    }

    private void showLocationError(String msg) {
        tvLocationInfo.setText(msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
        if (locationClient != null) {
            locationClient.onDestroy();
            locationClient = null;
        }
    }
}
