package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class RailFenceActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private TextView tvRails;
    private SeekBar seekBarRails;
    private int rails = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rail_fence);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("栅栏密码");
        }

        initViews();
    }

    private void initViews() {
        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        tvRails = findViewById(R.id.tv_rails);
        seekBarRails = findViewById(R.id.seek_bar_rails);
        Button btnEncrypt = findViewById(R.id.btn_encrypt);
        Button btnDecrypt = findViewById(R.id.btn_decrypt);
        Button btnCopy = findViewById(R.id.btn_copy);

        seekBarRails.setProgress(rails - 2);
        tvRails.setText("栅栏数: " + rails);

        seekBarRails.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rails = progress + 2;
                tvRails.setText("栅栏数: " + rails);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnEncrypt.setOnClickListener(v -> encrypt());
        btnDecrypt.setOnClickListener(v -> decrypt());
        btnCopy.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("RailFence", tvOutput.getText());
            clipboard.setPrimaryClip(clip);
            android.widget.Toast.makeText(this, "已复制", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void encrypt() {
        String input = etInput.getText().toString().replaceAll("\\s", "");
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        if (rails <= 0) {
            tvOutput.setText("栅栏数必须大于0");
            return;
        }

        StringBuilder[] fences = new StringBuilder[rails];
        for (int i = 0; i < rails; i++) {
            fences[i] = new StringBuilder();
        }

        int rail = 0;
        boolean direction = true; // true = down, false = up

        for (char c : input.toCharArray()) {
            fences[rail].append(c);
            if (rail == 0) {
                direction = true;
            } else if (rail == rails - 1) {
                direction = false;
            }
            rail += direction ? 1 : -1;
        }

        StringBuilder result = new StringBuilder();
        for (StringBuilder fence : fences) {
            result.append(fence);
        }
        tvOutput.setText(result.toString());
    }

    private void decrypt() {
        String input = etInput.getText().toString().replaceAll("\\s", "");
        if (input.isEmpty()) {
            tvOutput.setText("请输入文本");
            return;
        }

        if (rails <= 0 || rails > input.length()) {
            tvOutput.setText("栅栏数无效");
            return;
        }

        int length = input.length();
        int[] fenceLengths = new int[rails];
        int rail = 0;
        boolean direction = true;

        for (int i = 0; i < length; i++) {
            fenceLengths[rail]++;
            if (rail == 0) direction = true;
            else if (rail == rails - 1) direction = false;
            rail += direction ? 1 : -1;
        }

        String[] fenceTexts = new String[rails];
        int pos = 0;
        for (int i = 0; i < rails; i++) {
            fenceTexts[i] = input.substring(pos, pos + fenceLengths[i]);
            pos += fenceLengths[i];
        }

        StringBuilder result = new StringBuilder();
        rail = 0;
        direction = true;
        int[] fenceIndices = new int[rails];

        for (int i = 0; i < length; i++) {
            result.append(fenceTexts[rail].charAt(fenceIndices[rail]));
            fenceIndices[rail]++;
            if (rail == 0) direction = true;
            else if (rail == rails - 1) direction = false;
            rail += direction ? 1 : -1;
        }

        tvOutput.setText(result.toString());
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