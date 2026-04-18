package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

import java.util.Random;

public class MudTextActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private Random random = new Random();

    // 火星文映射
    private static final String[] MU_CHARS = {
        "囧", "槑", "烎", "毜", "氼", "睲", "嘦", "奆", "巭", "炛",
        "莪", "兲", "靉", "龘", "姳", "炓", "圐", "湭", "烸", "閊",
        "峣", "槞", "螡", "蟲", "迏", "叐", "迧", "邷", "溷", "蒊",
        "蓇", "趞", "轗", "雫", "霢", "黪", "鼘", "蠿", "鬲", "魕",
        "鱻", "麤", "龘", "灥", "靐", "畲", "畲", "猋", "麤", "粯",
        "糍", "籴", "耆", "耈", "耊", "虍", "螭", "蟾", "黾", "鼍",
        "龠", "鸾", "鸽", "鹰", "鹭", "鹮", "鹯", "鸱", "鸶", "鸾"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mud_text);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("火星文转换");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        Button btnToMud = findViewById(R.id.btn_to_mud);
        Button btnToNormal = findViewById(R.id.btn_to_normal);
        Button btnCopy = findViewById(R.id.btn_copy);

        btnToMud.setOnClickListener(v -> toMudText());
        btnToNormal.setOnClickListener(v -> toNormalText());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("MudText", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void toMudText() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLetter(c) && c < 0x3000) {
                int idx = Math.abs(c) % MU_CHARS.length;
                result.append(MU_CHARS[idx]);
            } else {
                result.append(c);
            }
        }
        tvOutput.setText(result.toString());
    }

    private void toNormalText() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }
        // 火星文转回正常文本（简化处理）
        tvOutput.setText(input);
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