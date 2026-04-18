package com.example.androiddemo.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TranslatorActivity extends AppCompatActivity {

    private Spinner spinnerSourceLang;
    private Spinner spinnerTargetLang;
    private ImageButton btnSwap;
    private EditText etInput;
    private ImageButton btnClear;
    private TextView tvOutput;
    private ImageButton btnCopy;
    private Button btnTranslate;
    private ProgressBar progressBar;
    private LinearLayout historyContainer;

    private Map<String, String> languageMap;
    private List<String> languageNames;
    private SharedPreferences spHistory;
    private ExecutorService executor;
    private Handler mainHandler;

    private static final String HISTROY_KEY = "translation_history";
    private static final int MAX_HISTORY_SIZE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        spHistory = getSharedPreferences("translator_history", Context.MODE_PRIVATE);

        initLanguageMap();
        initViews();
        setupListeners();
        loadHistory();
    }

    private void initLanguageMap() {
        languageMap = new LinkedHashMap<>();
        languageMap.put("自动检测", "auto");
        languageMap.put("中文", "zh-CN");
        languageMap.put("英文", "en");
        languageMap.put("日文", "ja");
        languageMap.put("韩文", "ko");
        languageMap.put("法文", "fr");
        languageMap.put("德文", "de");
        languageMap.put("西班牙文", "es");
        languageMap.put("俄文", "ru");
        languageMap.put("葡萄牙文", "pt");
        languageMap.put("意大利文", "it");
        languageMap.put("阿拉伯文", "ar");

        languageNames = new ArrayList<>(languageMap.keySet());
    }

    private void initViews() {
        spinnerSourceLang = findViewById(R.id.spinner_source_lang);
        spinnerTargetLang = findViewById(R.id.spinner_target_lang);
        btnSwap = findViewById(R.id.btn_swap);
        etInput = findViewById(R.id.et_input);
        btnClear = findViewById(R.id.btn_clear);
        tvOutput = findViewById(R.id.tv_output);
        btnCopy = findViewById(R.id.btn_copy);
        btnTranslate = findViewById(R.id.btn_translate);
        progressBar = findViewById(R.id.progress_bar);
        historyContainer = findViewById(R.id.history_container);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("翻译官");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languageNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSourceLang.setAdapter(adapter);
        spinnerTargetLang.setAdapter(adapter);

        // 默认目标语言设为英文
        int targetIndex = languageNames.indexOf("英文");
        if (targetIndex >= 0) {
            spinnerTargetLang.setSelection(targetIndex);
        }
    }

    private void setupListeners() {
        btnSwap.setOnClickListener(v -> {
            int sourcePos = spinnerSourceLang.getSelectedItemPosition();
            int targetPos = spinnerTargetLang.getSelectedItemPosition();

            // 自动检测不能作为目标语言
            if (sourcePos == 0) {
                Toast.makeText(this, "自动检测不能作为目标语言", Toast.LENGTH_SHORT).show();
                return;
            }

            spinnerSourceLang.setSelection(targetPos);
            spinnerTargetLang.setSelection(sourcePos);
        });

        btnClear.setOnClickListener(v -> {
            etInput.setText("");
            tvOutput.setText("");
        });

        btnCopy.setOnClickListener(v -> {
            String text = tvOutput.getText().toString();
            if (!text.isEmpty() && !text.startsWith("翻译结果")) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("翻译结果", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        });

        btnTranslate.setOnClickListener(v -> performTranslation());

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private String getSelectedLangCode(Spinner spinner) {
        String selectedLang = (String) spinner.getSelectedItem();
        return languageMap.get(selectedLang);
    }

    private void performTranslation() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "请输入要翻译的文字", Toast.LENGTH_SHORT).show();
            return;
        }

        String sourceLang = getSelectedLangCode(spinnerSourceLang);
        String targetLang = getSelectedLangCode(spinnerTargetLang);

        if (sourceLang.equals(targetLang)) {
            Toast.makeText(this, "源语言和目标语言不能相同", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnTranslate.setEnabled(false);

        executor.execute(() -> {
            try {
                String result = translate(input, sourceLang, targetLang);
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTranslate.setEnabled(true);
                    if (result != null) {
                        tvOutput.setText(result);
                        saveToHistory(input, result, sourceLang, targetLang);
                    } else {
                        tvOutput.setText("翻译失败，请稍后重试");
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnTranslate.setEnabled(true);
                    tvOutput.setText("翻译失败: " + e.getMessage());
                });
            }
        });
    }

    private String translate(String text, String sourceLang, String targetLang) throws Exception {
        // 使用 MyMemory Translation API (免费，无需API Key)
        String urlStr = "https://api.mymemory.translated.net/get";
        String encodedText = URLEncoder.encode(text, "UTF-8");
        String langPair = sourceLang + "|" + targetLang;
        String fullUrl = urlStr + "?q=" + encodedText + "&langpair=" + langPair;

        URL url = new URL(fullUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONObject responseData = jsonResponse.getJSONObject("responseData");
            return responseData.getString("translatedText");
        } else {
            throw new Exception("HTTP Error: " + responseCode);
        }
    }

    private void saveToHistory(String original, String translated, String sourceLang, String targetLang) {
        String historyStr = spHistory.getString(HISTROY_KEY, "");
        List<TranslationRecord> records = parseHistory(historyStr);

        TranslationRecord newRecord = new TranslationRecord(original, translated, sourceLang, targetLang);
        records.add(0, newRecord);

        // 限制历史记录数量
        if (records.size() > MAX_HISTORY_SIZE) {
            records = records.subList(0, MAX_HISTORY_SIZE);
        }

        StringBuilder sb = new StringBuilder();
        for (TranslationRecord record : records) {
            sb.append(record.toJson()).append("\n");
        }
        spHistory.edit().putString(HISTROY_KEY, sb.toString()).apply();

        loadHistory();
    }

    private List<TranslationRecord> parseHistory(String historyStr) {
        List<TranslationRecord> records = new ArrayList<>();
        if (historyStr == null || historyStr.isEmpty()) {
            return records;
        }

        String[] lines = historyStr.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                try {
                    records.add(TranslationRecord.fromJson(line.trim()));
                } catch (Exception ignored) {}
            }
        }
        return records;
    }

    private void loadHistory() {
        historyContainer.removeAllViews();

        String historyStr = spHistory.getString(HISTROY_KEY, "");
        List<TranslationRecord> records = parseHistory(historyStr);

        if (records.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("暂无翻译历史");
            emptyView.setTextColor(0xFF888888);
            emptyView.setPadding(32, 32, 32, 32);
            historyContainer.addView(emptyView);
            return;
        }

        for (int i = 0; i < records.size() && i < 5; i++) {
            TranslationRecord record = records.get(i);
            View itemView = createHistoryItemView(record);
            historyContainer.addView(itemView);
        }
    }

    private View createHistoryItemView(TranslationRecord record) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(32, 24, 32, 24);
        itemLayout.setBackgroundResource(R.drawable.section_background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 16;
        itemLayout.setLayoutParams(params);

        TextView tvOriginal = new TextView(this);
        tvOriginal.setText(record.original);
        tvOriginal.setTextSize(14);
        tvOriginal.setTextColor(0xFF333333);
        itemLayout.addView(tvOriginal);

        TextView tvTranslated = new TextView(this);
        tvTranslated.setText(record.translated);
        tvTranslated.setTextSize(14);
        tvTranslated.setTextColor(0xFF666666);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.topMargin = 8;
        tvTranslated.setLayoutParams(tvParams);
        itemLayout.addView(tvTranslated);

        TextView tvLang = new TextView(this);
        tvLang.setText(getLanguageName(record.sourceLang) + " → " + getLanguageName(record.targetLang));
        tvLang.setTextSize(12);
        tvLang.setTextColor(0xFF999999);
        tvLang.setLayoutParams(tvParams);
        itemLayout.addView(tvLang);

        itemLayout.setOnClickListener(v -> {
            etInput.setText(record.original);
            tvOutput.setText(record.translated);
        });

        return itemLayout;
    }

    private String getLanguageName(String langCode) {
        for (Map.Entry<String, String> entry : languageMap.entrySet()) {
            if (entry.getValue().equals(langCode)) {
                return entry.getKey();
            }
        }
        return langCode;
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
        executor.shutdown();
    }

    private static class TranslationRecord {
        String original;
        String translated;
        String sourceLang;
        String targetLang;

        TranslationRecord(String original, String translated, String sourceLang, String targetLang) {
            this.original = original;
            this.translated = translated;
            this.sourceLang = sourceLang;
            this.targetLang = targetLang;
        }

        String toJson() {
            return String.format(
                    "{\"original\":\"%s\",\"translated\":\"%s\",\"sourceLang\":\"%s\",\"targetLang\":\"%s\"}",
                    escapeJson(original), escapeJson(translated), sourceLang, targetLang);
        }

        static TranslationRecord fromJson(String json) {
            try {
                JSONObject obj = new JSONObject(json);
                return new TranslationRecord(
                        obj.getString("original"),
                        obj.getString("translated"),
                        obj.getString("sourceLang"),
                        obj.getString("targetLang"));
            } catch (Exception e) {
                return null;
            }
        }

        private String escapeJson(String str) {
            return str.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
        }
    }
}