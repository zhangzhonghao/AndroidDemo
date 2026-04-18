package com.example.androiddemo.ai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.androiddemo.R;
import com.example.androiddemo.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiCodeActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.minimaxi.com/v1/text/chatcompletion_v2";
    private static final String MODEL = "MiniMax-M2";
    private static final int TIMEOUT_MS = 60000;

    private RadioGroup rgMode;
    private Spinner spinnerLanguage;
    private EditText etInput;
    private TextView tvOutput;
    private Button btnGenerate;
    private Button btnCopy;
    private ProgressBar progressBar;

    private int currentMode = MODE_GENERATE;
    private String currentLanguage = "Java";

    private static final int MODE_GENERATE = 0;
    private static final int MODE_EXPLAIN = 1;
    private static final int MODE_OPTIMIZE = 2;

    private final String[] languages = {"Java", "Python", "JavaScript", "Kotlin", "Swift", "Go", "C++", "PHP"};

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_code);

        initViews();
        setupToolbar();
        setupLanguageSpinner();
        setupListeners();
    }

    private void initViews() {
        rgMode = findViewById(R.id.rg_mode);
        spinnerLanguage = findViewById(R.id.spinner_language);
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        btnGenerate = findViewById(R.id.btn_generate);
        btnCopy = findViewById(R.id.btn_copy);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentLanguage = languages[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupListeners() {
        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_generate) {
                currentMode = MODE_GENERATE;
                spinnerLanguage.setEnabled(true);
                etInput.setHint("描述你的需求，例如：写一个快速排序算法");
            } else if (checkedId == R.id.rb_explain) {
                currentMode = MODE_EXPLAIN;
                spinnerLanguage.setEnabled(false);
                etInput.setHint("粘贴需要解释的代码...");
            } else if (checkedId == R.id.rb_optimize) {
                currentMode = MODE_OPTIMIZE;
                spinnerLanguage.setEnabled(false);
                etInput.setHint("粘贴需要优化的代码...");
            }
        });

        btnGenerate.setOnClickListener(v -> {
            String input = etInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }
            callAiApi(input);
        });

        btnCopy.setOnClickListener(v -> {
            String code = tvOutput.getText().toString();
            if (code.isEmpty() || code.startsWith("生成的代码") || code.startsWith("代码解释") || code.startsWith("优化建议")) {
                Toast.makeText(this, "没有可复制的内容", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });
    }

    private void callAiApi(String input) {
        showLoading(true);

        new Thread(() -> {
            try {
                String prompt = buildPrompt(input);

                JSONObject requestBody = new JSONObject();
                requestBody.put("model", MODEL);

                JSONArray messages = new JSONArray();
                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", prompt);
                messages.put(userMsg);

                requestBody.put("messages", messages);

                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setRequestProperty("Authorization", "Bearer " + BuildConfig.MINIMAX_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    String aiResponse = parseAiResponse(response.toString());
                    mainHandler.post(() -> {
                        displayResult(aiResponse);
                        showLoading(false);
                    });
                } else {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    reader.close();

                    final String error = "请求失败: " + responseCode;
                    mainHandler.post(() -> {
                        Toast.makeText(AiCodeActivity.this, error, Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
                }

                conn.disconnect();

            } catch (Exception e) {
                final String errorMsg = "网络错误: " + e.getMessage();
                mainHandler.post(() -> {
                    Toast.makeText(AiCodeActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private String buildPrompt(String input) {
        StringBuilder prompt = new StringBuilder();

        switch (currentMode) {
            case MODE_GENERATE:
                prompt.append("请用 ").append(currentLanguage).append(" 语言生成代码。\n\n需求：").append(input).append("\n\n请只输出代码，不要其他解释。");
                break;
            case MODE_EXPLAIN:
                prompt.append("请解释以下代码的功能和逻辑：\n\n").append(input).append("\n\n请用通俗易懂的方式解释。");
                break;
            case MODE_OPTIMIZE:
                prompt.append("请对以下代码提供优化建议（可包括性能、可读性、安全性等方面）：\n\n").append(input).append("\n\n请先给出优化后的代码，然后说明优化点。");
                break;
        }

        return prompt.toString();
    }

    private String parseAiResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONObject choices = root.getJSONArray("choices").getJSONObject(0);
            JSONObject message = choices.getJSONObject("message");
            return message.getString("content");
        } catch (Exception e) {
            return "解析响应失败，请稍后重试。";
        }
    }

    private void displayResult(String result) {
        tvOutput.setText(result);
        applySyntaxHighlighting(result);
    }

    private void applySyntaxHighlighting(String code) {
        if (code == null || code.isEmpty()) return;

        SpannableStringBuilder spannable = new SpannableStringBuilder(code);

        // 关键词
        String[] keywords = {"public", "private", "protected", "class", "interface", "extends", "implements",
                "static", "final", "void", "int", "String", "boolean", "double", "float", "long",
                "return", "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
                "def", "function", "var", "let", "const", "import", "export", "from", "async", "await",
                "try", "catch", "throw", "new", "this", "self", "True", "False", "None", "print"};

        // 字符串
        Pattern stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'");
        Matcher stringMatcher = stringPattern.matcher(code);
        while (stringMatcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(0xFF689F38), // Green
                    stringMatcher.start(), stringMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 单行注释
        Pattern commentPattern = Pattern.compile("//.*$|#.*$", Pattern.MULTILINE);
        Matcher commentMatcher = commentPattern.matcher(code);
        while (commentMatcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(0xFF9E9E9E), // Gray
                    commentMatcher.start(), commentMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 数字
        Pattern numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
        Matcher numberMatcher = numberPattern.matcher(code);
        while (numberMatcher.find()) {
            spannable.setSpan(new ForegroundColorSpan(0xFFE65100), // Orange
                    numberMatcher.start(), numberMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvOutput.setText(spannable);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnGenerate.setEnabled(!show);
        btnGenerate.setText(show ? "生成中..." : "生成");
    }
}