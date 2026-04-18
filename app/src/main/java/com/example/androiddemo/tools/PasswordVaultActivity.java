package com.example.androiddemo.tools;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PasswordVaultActivity extends AppCompatActivity {
    private EditText etAccount, etPassword;
    private TextView tvSaved;
    private SharedPreferences sp;
    private Map<String, String> passwordMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_vault);
        etAccount = findViewById(R.id.et_account);
        etPassword = findViewById(R.id.et_password);
        tvSaved = findViewById(R.id.tv_saved);
        sp = getSharedPreferences("password_vault", MODE_PRIVATE);
        loadPasswords();
    }

    public void save(View view) {
        String account = etAccount.getText().toString();
        String password = etPassword.getText().toString();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        String encoded = Base64.getEncoder().encodeToString(password.getBytes());
        sp.edit().putString(account, encoded).apply();
        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        loadPasswords();
    }

    private void loadPasswords() {
        passwordMap.clear();
        for (String key : sp.getAll().keySet()) {
            passwordMap.put(key, new String(Base64.getDecoder().decode(sp.getString(key, ""))));
        }
        StringBuilder sb = new StringBuilder("已保存的密码 (").append(passwordMap.size()).append("):\n");
        for (Map.Entry<String, String> entry : passwordMap.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
        }
        tvSaved.setText(sb.toString());
    }
}