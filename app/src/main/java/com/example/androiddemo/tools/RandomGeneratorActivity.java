package com.example.androiddemo.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androiddemo.R;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomGeneratorActivity extends AppCompatActivity {

    private RadioGroup tabGroup;
    private LinearLayout integerLayout, decimalLayout, passwordLayout, drawLayout, coinLayout, diceLayout;
    private TextView resultText, historyText;
    private EditText etMin, etMax, etPasswordLength, etNames, etDiceCount;
    private CheckBox cbUppercase, cbLowercase, cbDigits, cbSymbols;
    private Button btnGenerate, btnClearHistory;

    private Random random = new Random();
    private SecureRandom secureRandom = new SecureRandom();
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "random_generator_history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_generator);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        initViews();
        setupTabs();
        loadHistory();
    }

    private void initViews() {
        tabGroup = findViewById(R.id.tab_group);
        resultText = findViewById(R.id.tv_result);
        historyText = findViewById(R.id.tv_history);
        btnGenerate = findViewById(R.id.btn_generate);
        btnClearHistory = findViewById(R.id.btn_clear_history);

        // 各功能布局
        integerLayout = findViewById(R.id.layout_integer);
        decimalLayout = findViewById(R.id.layout_decimal);
        passwordLayout = findViewById(R.id.layout_password);
        drawLayout = findViewById(R.id.layout_draw);
        coinLayout = findViewById(R.id.layout_coin);
        diceLayout = findViewById(R.id.layout_dice);

        // 整数布局
        etMin = findViewById(R.id.et_min);
        etMax = findViewById(R.id.et_max);

        // 密码布局
        etPasswordLength = findViewById(R.id.et_password_length);
        cbUppercase = findViewById(R.id.cb_uppercase);
        cbLowercase = findViewById(R.id.cb_lowercase);
        cbDigits = findViewById(R.id.cb_digits);
        cbSymbols = findViewById(R.id.cb_symbols);

        // 抽签布局
        etNames = findViewById(R.id.et_names);

        // 骰子布局
        etDiceCount = findViewById(R.id.et_dice_count);

        btnGenerate.setOnClickListener(v -> generate());
        btnClearHistory.setOnClickListener(v -> clearHistory());
    }

    private void setupTabs() {
        tabGroup.setOnCheckedChangeListener((group, checkedId) -> {
            hideAllLayouts();
            if (checkedId == R.id.tab_integer) {
                integerLayout.setVisibility(View.VISIBLE);
                resultText.setText("--");
            } else if (checkedId == R.id.tab_decimal) {
                decimalLayout.setVisibility(View.VISIBLE);
                resultText.setText("--");
            } else if (checkedId == R.id.tab_password) {
                passwordLayout.setVisibility(View.VISIBLE);
                resultText.setText("--");
            } else if (checkedId == R.id.tab_draw) {
                drawLayout.setVisibility(View.VISIBLE);
                resultText.setText("--");
            } else if (checkedId == R.id.tab_coin) {
                coinLayout.setVisibility(View.VISIBLE);
                resultText.setText("--");
            } else if (checkedId == R.id.tab_dice) {
                diceLayout.setVisibility(View.VISIBLE);
                resultText.setText("--");
            }
        });
    }

    private void hideAllLayouts() {
        integerLayout.setVisibility(View.GONE);
        decimalLayout.setVisibility(View.GONE);
        passwordLayout.setVisibility(View.GONE);
        drawLayout.setVisibility(View.GONE);
        coinLayout.setVisibility(View.GONE);
        diceLayout.setVisibility(View.GONE);
    }

    private void generate() {
        int checkedId = tabGroup.getCheckedRadioButtonId();
        String result = "";

        if (checkedId == R.id.tab_integer) {
            result = generateInteger();
        } else if (checkedId == R.id.tab_decimal) {
            result = generateDecimal();
        } else if (checkedId == R.id.tab_password) {
            result = generatePassword();
        } else if (checkedId == R.id.tab_draw) {
            result = generateDraw();
        } else if (checkedId == R.id.tab_coin) {
            result = generateCoin();
        } else if (checkedId == R.id.tab_dice) {
            result = generateDice();
        }

        if (!result.isEmpty()) {
            resultText.setText(result);
            saveToHistory(result);
        }
    }

    private String generateInteger() {
        String minStr = etMin.getText().toString();
        String maxStr = etMax.getText().toString();

        if (minStr.isEmpty() || maxStr.isEmpty()) {
            Toast.makeText(this, "请输入最小值和最大值", Toast.LENGTH_SHORT).show();
            return "";
        }

        try {
            int min = Integer.parseInt(minStr);
            int max = Integer.parseInt(maxStr);

            if (min > max) {
                Toast.makeText(this, "最小值不能大于最大值", Toast.LENGTH_SHORT).show();
                return "";
            }

            int value = random.nextInt(max - min + 1) + min;
            return String.valueOf(value);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的整数", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private String generateDecimal() {
        double value = random.nextDouble();
        return String.format("%.6f", value);
    }

    private String generatePassword() {
        String lengthStr = etPasswordLength.getText().toString();
        if (lengthStr.isEmpty()) {
            Toast.makeText(this, "请输入密码长度", Toast.LENGTH_SHORT).show();
            return "";
        }

        int length;
        try {
            length = Integer.parseInt(lengthStr);
            if (length < 4 || length > 64) {
                Toast.makeText(this, "密码长度应在4-64之间", Toast.LENGTH_SHORT).show();
                return "";
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的长度", Toast.LENGTH_SHORT).show();
            return "";
        }

        StringBuilder chars = new StringBuilder();
        if (cbUppercase.isChecked()) chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (cbLowercase.isChecked()) chars.append("abcdefghijklmnopqrstuvwxyz");
        if (cbDigits.isChecked()) chars.append("0123456789");
        if (cbSymbols.isChecked()) chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");

        if (chars.length() == 0) {
            Toast.makeText(this, "请至少选择一种字符类型", Toast.LENGTH_SHORT).show();
            return "";
        }

        String charSet = chars.toString();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(charSet.length());
            password.append(charSet.charAt(index));
        }

        return password.toString();
    }

    private String generateDraw() {
        String namesStr = etNames.getText().toString().trim();
        if (namesStr.isEmpty()) {
            Toast.makeText(this, "请输入名单（用换行分隔）", Toast.LENGTH_SHORT).show();
            return "";
        }

        String[] names = namesStr.split("\n");
        List<String> validNames = new ArrayList<>();
        for (String name : names) {
            String trimmed = name.trim();
            if (!trimmed.isEmpty()) {
                validNames.add(trimmed);
            }
        }

        if (validNames.isEmpty()) {
            Toast.makeText(this, "请输入有效的名单", Toast.LENGTH_SHORT).show();
            return "";
        }

        int index = random.nextInt(validNames.size());
        return validNames.get(index);
    }

    private String generateCoin() {
        int value = random.nextInt(2);
        return value == 0 ? "正面" : "反面";
    }

    private String generateDice() {
        String countStr = etDiceCount.getText().toString();
        int count = 1;

        if (!countStr.isEmpty()) {
            try {
                count = Integer.parseInt(countStr);
                if (count < 1 || count > 10) {
                    Toast.makeText(this, "骰子数量应在1-10之间", Toast.LENGTH_SHORT).show();
                    return "";
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "请输入有效的骰子数量", Toast.LENGTH_SHORT).show();
                return "";
            }
        }

        StringBuilder result = new StringBuilder();
        int[] values = new int[count];
        int total = 0;

        for (int i = 0; i < count; i++) {
            values[i] = random.nextInt(6) + 1;
            total += values[i];
        }

        if (count == 1) {
            return String.valueOf(values[0]);
        } else {
            result.append("掷出: ");
            for (int i = 0; i < count; i++) {
                result.append(values[i]);
                if (i < count - 1) result.append(" + ");
            }
            result.append(" = ").append(total);
            return result.toString();
        }
    }

    private void saveToHistory(String result) {
        String history = prefs.getString("history", "");
        String timestamp = java.text.SimpleDateFormat.getDateTimeInstance().toString();
        String newEntry = result + " (" + timestamp + ")";

        if (!history.isEmpty()) {
            String[] entries = history.split("\n");
            if (entries.length >= 20) {
                history = "";
                for (int i = 0; i < 19; i++) {
                    if (i > 0) history += "\n";
                    history += entries[i];
                }
            }
        }

        if (!history.isEmpty()) {
            history = newEntry + "\n" + history;
        } else {
            history = newEntry;
        }

        prefs.edit().putString("history", history).apply();
        historyText.setText(history);
    }

    private void loadHistory() {
        String history = prefs.getString("history", "");
        historyText.setText(history);
    }

    private void clearHistory() {
        prefs.edit().clear().apply();
        historyText.setText("");
        Toast.makeText(this, "历史记录已清除", Toast.LENGTH_SHORT).show();
    }
}