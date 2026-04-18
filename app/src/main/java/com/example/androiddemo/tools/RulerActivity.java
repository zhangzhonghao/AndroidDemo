package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

public class RulerActivity extends AppCompatActivity {

    private RulerView rulerView;
    private TextView tvCurrentValue;
    private Button btnCm, btnInch, btnCalibrate, btnLock;
    private boolean isLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_ruler);

        initViews();
        setupListeners();
        updateUnitUI();
    }

    private void initViews() {
        rulerView = findViewById(R.id.ruler_view);
        tvCurrentValue = findViewById(R.id.tv_current_value);
        btnCm = findViewById(R.id.btn_cm);
        btnInch = findViewById(R.id.btn_inch);
        btnCalibrate = findViewById(R.id.btn_calibrate);
        btnLock = findViewById(R.id.btn_lock);
    }

    private void setupListeners() {
        btnCm.setOnClickListener(v -> {
            rulerView.setUnit(RulerView.UNIT_CM);
            updateUnitUI();
            updateValueDisplay();
        });

        btnInch.setOnClickListener(v -> {
            rulerView.setUnit(RulerView.UNIT_INCH);
            updateUnitUI();
            updateValueDisplay();
        });

        btnCalibrate.setOnClickListener(v -> showCalibrationDialog());

        btnLock.setOnClickListener(v -> {
            isLocked = !isLocked;
            if (isLocked) {
                btnLock.setText("解锁");
                Toast.makeText(this, "屏幕已锁定", Toast.LENGTH_SHORT).show();
            } else {
                btnLock.setText("锁定");
                Toast.makeText(this, "屏幕已解锁", Toast.LENGTH_SHORT).show();
            }
        });

        rulerView.setOnTouchListener((v, event) -> {
            if (isLocked) return true;
            updateValueDisplay();
            return false;
        });
    }

    private void updateUnitUI() {
        if (rulerView.getUnit() == RulerView.UNIT_CM) {
            btnCm.setEnabled(false);
            btnInch.setEnabled(true);
        } else {
            btnCm.setEnabled(true);
            btnInch.setEnabled(false);
        }
    }

    private void updateValueDisplay() {
        float value = rulerView.getCurrentValue();
        String unitStr = rulerView.getUnit() == RulerView.UNIT_CM ? " cm" : " inch";
        tvCurrentValue.setText(String.format("%.2f%s", Math.abs(value), unitStr));
    }

    private void showCalibrationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DPI 校准");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("请输入校准值 (默认 1.0)");
        input.setText(String.valueOf(rulerView.getDpiCalibration()));

        builder.setView(input);
        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                float calValue = Float.parseFloat(input.getText().toString());
                if (calValue > 0) {
                    rulerView.setDpiCalibration(calValue);
                    Toast.makeText(this, "校准已更新", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请输入有效数值", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效数值", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}