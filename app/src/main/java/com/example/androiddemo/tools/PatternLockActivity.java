package com.example.androiddemo.tools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.androiddemo.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/**
 * 九宫格手势解锁
 * 支持设置图案和验证图案两种模式
 */
public class PatternLockActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pattern_lock_prefs";
    private static final String KEY_PATTERN = "saved_pattern";

    private PatternLockView patternLockView;
    private TextView tvTitle;
    private TextView tvStatus;
    private Button btnConfirm;
    private Button btnReset;
    private Button btnToggleMode;

    private boolean isSetupMode = false; // true: 设置模式, false: 验证模式
    private List<Integer> savedPattern;
    private List<Integer> currentPattern;
    private List<Integer> firstPattern; // 用于确认时保存第一次绘制的图案

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_lock);

        initViews();
        loadSavedPattern();
        updateMode();
    }

    private void initViews() {
        patternLockView = findViewById(R.id.pattern_lock_view);
        tvTitle = findViewById(R.id.tv_title);
        tvStatus = findViewById(R.id.tv_status);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnReset = findViewById(R.id.btn_reset);
        btnToggleMode = findViewById(R.id.btn_toggle_mode);

        // 设置返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("九宫格解锁");
        }

        // 设置图案监听
        patternLockView.setOnPatternListener(new PatternLockView.OnPatternListener() {
            @Override
            public void onPatternStarted() {
                tvStatus.setText("");
                tvStatus.setTextColor(ContextCompat.getColor(PatternLockActivity.this, R.color.on_surface_variant));
            }

            @Override
            public void onPatternComplete(List<Integer> pattern) {
                currentPattern = pattern;
                handlePatternComplete(pattern);
            }

            @Override
            public void onPatternCleared() {
                tvStatus.setText("");
            }
        });

        // 确认按钮
        btnConfirm.setOnClickListener(v -> {
            if (currentPattern != null && !currentPattern.isEmpty()) {
                if (isSetupMode) {
                    confirmSetupPattern();
                } else {
                    verifyPattern();
                }
            }
        });

        // 重置按钮
        btnReset.setOnClickListener(v -> {
            patternLockView.clearPattern();
            currentPattern = null;
            if (isSetupMode && firstPattern != null) {
                firstPattern = null;
                tvStatus.setText("请再次绘制以确认图案");
            } else {
                tvStatus.setText("");
            }
            updateButtonVisibility();
        });

        // 切换模式按钮
        btnToggleMode.setOnClickListener(v -> {
            isSetupMode = !isSetupMode;
            patternLockView.clearPattern();
            currentPattern = null;
            firstPattern = null;
            updateMode();
        });
    }

    private void loadSavedPattern() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String patternStr = prefs.getString(KEY_PATTERN, "");
        if (!patternStr.isEmpty()) {
            savedPattern = stringToPattern(patternStr);
        } else {
            savedPattern = null;
        }
    }

    private void savePattern(List<Integer> pattern) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString(KEY_PATTERN, patternToString(pattern)).apply();
        savedPattern = pattern;
    }

    private String patternToString(List<Integer> pattern) {
        StringBuilder sb = new StringBuilder();
        for (int i : pattern) {
            sb.append(i).append(",");
        }
        return sb.toString();
    }

    private java.util.ArrayList<Integer> stringToPattern(String str) {
        java.util.ArrayList<Integer> pattern = new java.util.ArrayList<>();
        if (str != null && !str.isEmpty()) {
            String[] parts = str.split(",");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    pattern.add(Integer.parseInt(part));
                }
            }
        }
        return pattern;
    }

    private void updateMode() {
        if (isSetupMode) {
            tvTitle.setText("设置解锁图案");
            tvStatus.setText("请绘制您的解锁图案（至少4个点）");
            btnToggleMode.setText("切换为验证模式");
        } else {
            if (savedPattern != null && !savedPattern.isEmpty()) {
                tvTitle.setText("请绘制解锁图案");
                tvStatus.setText("");
                btnToggleMode.setText("切换为设置模式");
            } else {
                // 没有保存过图案，自动切换到设置模式
                isSetupMode = true;
                tvTitle.setText("设置解锁图案");
                tvStatus.setText("请绘制您的解锁图案（至少4个点）");
                btnToggleMode.setText("切换为验证模式");
            }
        }
        updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (isSetupMode) {
            if (currentPattern != null && currentPattern.size() >= 4) {
                btnConfirm.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
            } else {
                btnConfirm.setVisibility(View.GONE);
                btnReset.setVisibility(View.GONE);
            }
        } else {
            if (currentPattern != null && !currentPattern.isEmpty()) {
                btnConfirm.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
            } else {
                btnConfirm.setVisibility(View.GONE);
                btnReset.setVisibility(View.GONE);
            }
        }
    }

    private void handlePatternComplete(List<Integer> pattern) {
        if (isSetupMode) {
            handleSetupMode(pattern);
        } else {
            handleVerifyMode(pattern);
        }
        updateButtonVisibility();
    }

    private void handleSetupMode(List<Integer> pattern) {
        if (pattern.size() < 4) {
            tvStatus.setText("图案太短，至少需要4个点");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            patternLockView.setError(true);
            // 延迟清除错误状态
            patternLockView.postDelayed(() -> {
                patternLockView.setError(false);
                patternLockView.clearPattern();
                tvStatus.setText("请重新绘制（至少4个点）");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant));
            }, 800);
            return;
        }

        if (firstPattern == null) {
            // 第一次绘制
            firstPattern = pattern;
            tvStatus.setText("请再次绘制以确认图案");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.secondary));
        } else {
            // 第二次绘制，确认
            if (patternsEqual(firstPattern, pattern)) {
                // 两次图案相同，保存
                savePattern(pattern);
                patternLockView.clearPattern();
                firstPattern = null;
                tvStatus.setText("图案设置成功！");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.secondary));

                Toast.makeText(this, "解锁图案已保存", Toast.LENGTH_SHORT).show();

                // 延迟切换到验证模式
                patternLockView.postDelayed(() -> {
                    isSetupMode = false;
                    patternLockView.clearPattern();
                    updateMode();
                }, 1500);
            } else {
                // 两次图案不同
                tvStatus.setText("两次图案不一致，请重新设置");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
                patternLockView.setError(true);
                patternLockView.postDelayed(() -> {
                    patternLockView.setError(false);
                    patternLockView.clearPattern();
                    firstPattern = null;
                    tvStatus.setText("请重新绘制（至少4个点）");
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant));
                }, 800);
            }
        }
    }

    private void handleVerifyMode(List<Integer> pattern) {
        if (savedPattern == null) {
            // 没有保存过图案，切换到设置模式
            isSetupMode = true;
            updateMode();
            return;
        }

        if (patternsEqual(pattern, savedPattern)) {
            tvStatus.setText("验证成功！");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            Toast.makeText(this, "解锁成功", Toast.LENGTH_SHORT).show();
        } else {
            tvStatus.setText("图案错误，请重试");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            patternLockView.setError(true);
            patternLockView.postDelayed(() -> {
                patternLockView.setError(false);
                patternLockView.clearPattern();
                currentPattern = null;
                updateButtonVisibility();
            }, 800);
        }
    }

    private void confirmSetupPattern() {
        if (isSetupMode) {
            // 在设置模式下，confirm按钮的作用是完成设置
            if (currentPattern != null && currentPattern.size() >= 4) {
                if (firstPattern == null) {
                    firstPattern = currentPattern;
                    tvStatus.setText("请再次绘制以确认图案");
                    tvStatus.setTextColor(ContextCompat.getColor(this, R.color.secondary));
                    patternLockView.clearPattern();
                }
            }
        } else {
            verifyPattern();
        }
    }

    private void verifyPattern() {
        if (savedPattern == null) {
            tvStatus.setText("请先设置解锁图案");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            return;
        }

        if (patternsEqual(currentPattern, savedPattern)) {
            tvStatus.setText("验证成功！");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.secondary));
            Toast.makeText(this, "解锁成功", Toast.LENGTH_SHORT).show();
        } else {
            tvStatus.setText("图案错误，请重试");
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.error));
            patternLockView.setError(true);
            patternLockView.postDelayed(() -> {
                patternLockView.setError(false);
                patternLockView.clearPattern();
                currentPattern = null;
                updateButtonVisibility();
            }, 800);
        }
    }

    private boolean patternsEqual(List<Integer> p1, List<Integer> p2) {
        if (p1 == null || p2 == null) return false;
        if (p1.size() != p2.size()) return false;
        for (int i = 0; i < p1.size(); i++) {
            if (!p1.get(i).equals(p2.get(i))) return false;
        }
        return true;
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