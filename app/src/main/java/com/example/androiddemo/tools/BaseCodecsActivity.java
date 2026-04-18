package com.example.androiddemo.tools;

import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.nio.charset.StandardCharsets;

public class BaseCodecsActivity extends AppCompatActivity {

    private EditText etInput;
    private TextView tvOutput;
    private RadioGroup rgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_codecs);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Base编码解码");
        }

        etInput = findViewById(R.id.et_input);
        tvOutput = findViewById(R.id.tv_output);
        rgType = findViewById(R.id.rg_type);
        Button btnEncode = findViewById(R.id.btn_encode);
        Button btnDecode = findViewById(R.id.btn_decode);

        btnEncode.setOnClickListener(v -> encode());
        btnDecode.setOnClickListener(v -> decode());
    }

    private void encode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入内容");
            return;
        }
        int checkedId = rgType.getCheckedRadioButtonId();
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        String result;
        if (checkedId == R.id.rb_base16) {
            result = Base64.encodeToString(data, Base64.NO_WRAP);
        } else if (checkedId == R.id.rb_base32) {
            result = Base64.encodeToString(data, Base64.DEFAULT);
        } else if (checkedId == R.id.rb_base85) {
            result = Base64.encodeToString(data, Base64.NO_WRAP);
        } else {
            result = Base64.encodeToString(data, Base64.DEFAULT);
        }
        tvOutput.setText(result);
    }

    private void decode() {
        String input = etInput.getText().toString();
        if (input.isEmpty()) {
            tvOutput.setText("请输入内容");
            return;
        }
        try {
            byte[] data = Base64.decode(input, Base64.DEFAULT);
            tvOutput.setText(new String(data, StandardCharsets.UTF_8));
        } catch (Exception e) {
            tvOutput.setText("解码失败");
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
