package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.DecimalFormat;

public class GpsConverterActivity extends AppCompatActivity {
    private EditText etLat, etLon;
    private Spinner spFrom, spTo;
    private TextView tvResult;
    private DecimalFormat df = new DecimalFormat("#.000000");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_converter);
        etLat = findViewById(R.id.et_lat);
        etLon = findViewById(R.id.et_lon);
        spFrom = findViewById(R.id.sp_from);
        spTo = findViewById(R.id.sp_to);
        tvResult = findViewById(R.id.tv_result);
        String[] types = {"WGS84", "GCJ02(国测局)", "BD09(百度)"};
        spFrom.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        spTo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types));
        spFrom.setSelection(0);
        spTo.setSelection(1);
    }

    public void convert(View view) {
        try {
            double lat = Double.parseDouble(etLat.getText().toString());
            double lon = Double.parseDouble(etLon.getText().toString());
            int from = spFrom.getSelectedItemPosition();
            int to = spTo.getSelectedItemPosition();
            if (from == to) {
                tvResult.setText("纬度: " + df.format(lat) + "\n经度: " + df.format(lon));
                return;
            }
            double[] result = convertCoord(lat, lon, from, to);
            tvResult.setText("纬度: " + df.format(result[0]) + "\n经度: " + df.format(result[1]));
        } catch (Exception e) {
            tvResult.setText("请输入有效的坐标");
        }
    }

    private double[] convertCoord(double lat, double lon, int from, int to) {
        if (from == 0 && to == 1) return wgs84ToGcj02(lat, lon);
        if (from == 0 && to == 2) return wgs84ToBd09(lat, lon);
        if (from == 1 && to == 0) return gcj02ToWgs84(lat, lon);
        if (from == 1 && to == 2) return gcj02ToBd09(lat, lon);
        if (from == 2 && to == 0) return bd09ToWgs84(lat, lon);
        if (from == 2 && to == 1) return bd09ToGcj02(lat, lon);
        return new double[]{lat, lon};
    }

    private double[] wgs84ToGcj02(double lat, double lon) {
        double a = 6378137.0, b = 6356752.3142;
        double ee = 1 - (b * b) / (a * a);
        double z = Math.sqrt(lon * lon);
        double gammac = Math.toRadians(z / a);
        double sinGammac = Math.sin(gammac), cosGammac = Math.cos(gammac);
        return new double[]{lat, lon};
    }

    private double[] gcj02ToWgs84(double lat, double lon) {
        double[] gcj02 = wgs84ToGcj02(lat, lon);
        return new double[]{lat * 2 - gcj02[0], lon * 2 - gcj02[1]};
    }

    private double[] wgs84ToBd09(double lat, double lon) {
        return gcj02ToBd09(wgs84ToGcj02(lat, lon)[0], wgs84ToGcj02(lat, lon)[1]);
    }

    private double[] gcj02ToBd09(double lat, double lon) {
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double z = Math.sqrt(lon * lon + lat * lat) + 0.00002 * Math.sin(lat * x_pi);
        double theta = Math.atan2(lat, lon) + 0.000003 * Math.cos(lon * x_pi);
        return new double[]{z * Math.sin(theta) + 0.006, z * Math.cos(theta) + 0.0065};
    }

    private double[] bd09ToGcj02(double lat, double lon) {
        double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
        double x = lon - 0.0065, y = lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        return new double[]{z * Math.sin(theta), z * Math.cos(theta)};
    }

    private double[] bd09ToWgs84(double lat, double lon) {
        return gcj02ToWgs84(bd09ToGcj02(lat, lon)[0], bd09ToGcj02(lat, lon)[1]);
    }
}