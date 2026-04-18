package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnitConverterActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private Spinner spinnerFromUnit;
    private Spinner spinnerToUnit;
    private EditText etFromValue;
    private EditText etToValue;
    private TextView tvResult;

    private Map<String, Map<String, Double>> conversionRates = new LinkedHashMap<>();
    private String currentCategory = "";
    private boolean isUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_converter);

        initViews();
        initConversionRates();
        setupCategorySpinner();
        setupListeners();
    }

    private void initViews() {
        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerFromUnit = findViewById(R.id.spinner_from_unit);
        spinnerToUnit = findViewById(R.id.spinner_to_unit);
        etFromValue = findViewById(R.id.et_from_value);
        etToValue = findViewById(R.id.et_to_value);
        tvResult = findViewById(R.id.tv_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("单位转换器");
        }
    }

    private void initConversionRates() {
        // 长度单位转换（以米为基准）
        Map<String, Double> length = new LinkedHashMap<>();
        length.put("米", 1.0);
        length.put("厘米", 0.01);
        length.put("毫米", 0.001);
        length.put("英寸", 0.0254);
        length.put("英尺", 0.3048);
        length.put("码", 0.9144);
        length.put("公里", 1000.0);
        length.put("英里", 1609.344);
        conversionRates.put("长度", length);

        // 重量单位转换（以千克为基准）
        Map<String, Double> weight = new LinkedHashMap<>();
        weight.put("千克", 1.0);
        weight.put("克", 0.001);
        weight.put("毫克", 0.000001);
        weight.put("磅", 0.453592);
        weight.put("盎司", 0.0283495);
        weight.put("吨", 1000.0);
        conversionRates.put("重量", weight);

        // 温度单位转换（特殊处理）
        Map<String, Double> temperature = new LinkedHashMap<>();
        temperature.put("摄氏度", 1.0);
        temperature.put("华氏度", 1.0);
        temperature.put("开尔文", 1.0);
        conversionRates.put("温度", temperature);

        // 面积单位转换（以平方米为基准）
        Map<String, Double> area = new LinkedHashMap<>();
        area.put("平方米", 1.0);
        area.put("平方厘米", 0.0001);
        area.put("平方公里", 1000000.0);
        area.put("平方英尺", 0.092903);
        area.put("亩", 666.6667);
        area.put("公顷", 10000.0);
        conversionRates.put("面积", area);

        // 体积单位转换（以升为基准）
        Map<String, Double> volume = new LinkedHashMap<>();
        volume.put("升", 1.0);
        volume.put("毫升", 0.001);
        volume.put("加仑（美制）", 3.78541);
        volume.put("夸脱", 0.946353);
        volume.put("杯", 0.236588);
        conversionRates.put("体积", volume);

        // 速度单位转换（以米/秒为基准）
        Map<String, Double> speed = new LinkedHashMap<>();
        speed.put("米/秒", 1.0);
        speed.put("千米/时", 0.277778);
        speed.put("英里/时", 0.44704);
        speed.put("节", 0.514444);
        conversionRates.put("速度", speed);

        // 时间单位转换（以秒为基准）
        Map<String, Double> time = new LinkedHashMap<>();
        time.put("秒", 1.0);
        time.put("分钟", 60.0);
        time.put("小时", 3600.0);
        time.put("天", 86400.0);
        time.put("周", 604800.0);
        time.put("年", 31536000.0);
        conversionRates.put("时间", time);

        // 数据存储单位转换（以字节为基准）
        Map<String, Double> data = new LinkedHashMap<>();
        data.put("字节", 1.0);
        data.put("KB", 1024.0);
        data.put("MB", 1048576.0);
        data.put("GB", 1073741824.0);
        data.put("TB", 1099511627776.0);
        data.put("PB", 1125899906842624.0);
        conversionRates.put("数据存储", data);
    }

    private void setupCategorySpinner() {
        List<String> categories = new ArrayList<>(conversionRates.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupUnitSpinners(String category) {
        Map<String, Double> units = conversionRates.get(category);
        List<String> unitList = new ArrayList<>(units.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, unitList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFromUnit.setAdapter(adapter);
        spinnerToUnit.setAdapter(adapter);

        if (unitList.size() > 1) {
            spinnerToUnit.setSelection(1);
        }
    }

    private void setupListeners() {
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCategory = parent.getItemAtPosition(position).toString();
                setupUnitSpinners(currentCategory);
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        AdapterView.OnItemSelectedListener unitListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                performConversion();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerFromUnit.setOnItemSelectedListener(unitListener);
        spinnerToUnit.setOnItemSelectedListener(unitListener);

        etFromValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    isUpdating = true;
                    performConversion();
                    isUpdating = false;
                }
            }
        });

        etToValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdating) {
                    isUpdating = true;
                    performReverseConversion();
                    isUpdating = false;
                }
            }
        });
    }

    private void performConversion() {
        if (currentCategory.isEmpty()) return;

        String fromUnit = spinnerFromUnit.getSelectedItem() != null ?
                spinnerFromUnit.getSelectedItem().toString() : "";
        String toUnit = spinnerToUnit.getSelectedItem() != null ?
                spinnerToUnit.getSelectedItem().toString() : "";
        String inputValue = etFromValue.getText().toString();

        if (fromUnit.isEmpty() || toUnit.isEmpty()) return;

        double result = convert(inputValue, fromUnit, toUnit, currentCategory);

        isUpdating = true;
        etToValue.setText(formatResult(result));
        isUpdating = false;

        updateResultDisplay(fromUnit, toUnit, inputValue, formatResult(result));
    }

    private void performReverseConversion() {
        if (currentCategory.isEmpty()) return;

        String fromUnit = spinnerFromUnit.getSelectedItem() != null ?
                spinnerFromUnit.getSelectedItem().toString() : "";
        String toUnit = spinnerToUnit.getSelectedItem() != null ?
                spinnerToUnit.getSelectedItem().toString() : "";
        String inputValue = etToValue.getText().toString();

        if (fromUnit.isEmpty() || toUnit.isEmpty()) return;

        double result = convert(inputValue, toUnit, fromUnit, currentCategory);

        isUpdating = true;
        etFromValue.setText(formatResult(result));
        isUpdating = false;

        updateResultDisplay(toUnit, fromUnit, inputValue, formatResult(result));
    }

    private double convert(String value, String fromUnit, String toUnit, String category) {
        if (value.isEmpty() || value.equals("-") || value.equals(".")) {
            return 0;
        }

        try {
            double inputValue = Double.parseDouble(value);

            // 温度需要特殊处理
            if ("温度".equals(category)) {
                return convertTemperature(inputValue, fromUnit, toUnit);
            }

            Map<String, Double> rates = conversionRates.get(category);
            double fromRate = rates.get(fromUnit);
            double toRate = rates.get(toUnit);

            // 先转换为基准单位，再转换为目标单位
            double baseValue = inputValue * fromRate;
            return baseValue / toRate;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double convertTemperature(double value, String fromUnit, String toUnit) {
        // 先转换为摄氏度
        double celsius;
        switch (fromUnit) {
            case "摄氏度":
                celsius = value;
                break;
            case "华氏度":
                celsius = (value - 32) * 5 / 9;
                break;
            case "开尔文":
                celsius = value - 273.15;
                break;
            default:
                celsius = value;
        }

        // 再从摄氏度转换为目标单位
        switch (toUnit) {
            case "摄氏度":
                return celsius;
            case "华氏度":
                return celsius * 9 / 5 + 32;
            case "开尔文":
                return celsius + 273.15;
            default:
                return celsius;
        }
    }

    private String formatResult(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        } else {
            String str = String.format("%.10f", value);
            str = str.replaceAll("0*$", "").replaceAll("\\.$", "");
            return str;
        }
    }

    private void updateResultDisplay(String fromUnit, String toUnit, String inputValue, String result) {
        if (inputValue.isEmpty()) {
            tvResult.setText("请输入数值");
        } else {
            tvResult.setText(inputValue + " " + fromUnit + " = " + result + " " + toUnit);
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