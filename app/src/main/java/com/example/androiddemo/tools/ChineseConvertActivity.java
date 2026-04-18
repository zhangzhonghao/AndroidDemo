package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class ChineseConvertActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private boolean isSimplifiedToTraditional = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinese_convert);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("简繁转换");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        Button btnToTraditional = findViewById(R.id.btn_to_traditional);
        Button btnToSimplified = findViewById(R.id.btn_to_simplified);
        Button btnSwap = findViewById(R.id.btn_swap);
        Button btnCopy = findViewById(R.id.btn_copy);

        btnToTraditional.setOnClickListener(v -> convert(true));
        btnToSimplified.setOnClickListener(v -> convert(false));
        btnSwap.setOnClickListener(v -> swap());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("ChineseConvert", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void convert(boolean toTraditional) {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        // 简繁转换映射表（常用字）
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append(convertChar(c, toTraditional));
        }
        tvOutput.setText(result.toString());
    }

    private char convertChar(char c, boolean toTraditional) {
        // 常用简繁转换映射
        String simplified = "万与丑专业东丝丢两严丧个临丽举么义之乌乐乔习乡书买乱了吗亏云 상호运华瓦丝厌巩坝坛丽克划针锡阶纳纶马矿兰关兽卫帮纤织终丝纶经纬网盐缘纤纽绸绑绿罗网罚罩与";
        String traditional = "萬與醜專業東絲丟兩嚴喪個臨麗舉麼義烏樂樂喬習鄉書買亂了嗎虧雲相互運華瓦絲厭鞏壩壇麗克劃針錫階納綸馬礦蘭關獸衛幫纖織終絲綸經緯網鹽緣纖紐綢綁綠羅網罰罩與";

        if (toTraditional) {
            int idx = simplified.indexOf(c);
            if (idx >= 0) {
                return traditional.charAt(idx);
            }
        } else {
            int idx = traditional.indexOf(c);
            if (idx >= 0) {
                return simplified.charAt(idx);
            }
        }
        return c;
    }

    private void swap() {
        String currentOutput = tvOutput.getText().toString();
        if (!currentOutput.isEmpty() && !"请输入文本".equals(currentOutput)) {
            etInput.setText(currentOutput);
            convert(!isSimplifiedToTraditional);
            isSimplifiedToTraditional = !isSimplifiedToTraditional;
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