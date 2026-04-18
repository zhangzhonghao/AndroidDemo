package com.example.androiddemo.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.security.SecureRandom;

public class PasswordGeneratorActivity extends AppCompatActivity {

    private EditText etPassword;
    private TextView tvLength;
    private SeekBar seekBarLength;
    private CheckBox cbUppercase, cbLowercase, cbNumber, cbSpecial;
    private CheckBox cbShowPassword;
    private Button btnGenerate, btnCopy;
    private SecureRandom random = new SecureRandom();
    private int passwordLength = 12;

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etPassword = findViewById(R.id.et_password);
        tvLength = findViewById(R.id.tv_length);
        seekBarLength = findViewById(R.id.seek_bar_length);
        cbUppercase = findViewById(R.id.cb_uppercase);
        cbLowercase = findViewById(R.id.cb_lowercase);
        cbNumber = findViewById(R.id.cb_number);
        cbSpecial = findViewById(R.id.cb_special);
        cbShowPassword = findViewById(R.id.cb_show_password);
        btnGenerate = findViewById(R.id.btn_generate);
        btnCopy = findViewById(R.id.btn_copy);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("密码生成器");
        }

        // 默认选项
        cbUppercase.setChecked(true);
        cbLowercase.setChecked(true);
        cbNumber.setChecked(true);
        cbSpecial.setChecked(true);

        tvLength.setText("密码长度: " + passwordLength);
    }

    private void setupListeners() {
        seekBarLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordLength = progress + 6; // 6-24
                tvLength.setText("密码长度: " + passwordLength);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(null);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePassword();
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyPassword();
            }
        });
    }

    private void generatePassword() {
        StringBuilder chars = new StringBuilder();

        if (cbUppercase.isChecked()) chars.append(UPPERCASE);
        if (cbLowercase.isChecked()) chars.append(LOWERCASE);
        if (cbNumber.isChecked()) chars.append(NUMBERS);
        if (cbSpecial.isChecked()) chars.append(SPECIAL);

        if (chars.length() == 0) {
            Toast.makeText(this, "请至少选择一种字符类型", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = generate(chars.toString(), passwordLength);

        // 确保每种选中的类型都至少出现一次
        StringBuilder passwordBuilder = new StringBuilder(password);
        if (cbUppercase.isChecked()) {
            passwordBuilder.setCharAt(random.nextInt(password.length()),
                    UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (cbLowercase.isChecked()) {
            int pos = random.nextInt(passwordBuilder.length());
            passwordBuilder.setCharAt(pos,
                    LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (cbNumber.isChecked()) {
            int pos = random.nextInt(passwordBuilder.length());
            passwordBuilder.setCharAt(pos,
                    NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        if (cbSpecial.isChecked()) {
            int pos = random.nextInt(passwordBuilder.length());
            passwordBuilder.setCharAt(pos,
                    SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        etPassword.setText(passwordBuilder.toString());
    }

    private String generate(String chars, int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private void copyPassword() {
        String password = etPassword.getText().toString();
        if (password.isEmpty()) {
            Toast.makeText(this, "先生成密码", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipData clip = ClipData.newPlainText("password", password);
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "密码已复制", Toast.LENGTH_SHORT).show();
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