package com.example.androiddemo.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PasswordManagerActivity extends AppCompatActivity {

    private ListView lvPasswords;
    private List<String> passwordList;
    private Map<String, String[]> passwords;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_manager);

        lvPasswords = findViewById(R.id.lv_passwords);
        Button btnAdd = findViewById(R.id.btn_add_password);

        passwordList = new ArrayList<>();
        passwords = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, passwordList);
        lvPasswords.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        lvPasswords.setOnItemClickListener((parent, view, position, id) -> {
            String name = passwordList.get(position).split("\n")[0].replace("🔐 ", "");
            String pwd = passwords.get(name)[0];
            copyToClipboard(pwd);
            Toast.makeText(PasswordManagerActivity.this, "密码已复制", Toast.LENGTH_SHORT).show();
        });

        lvPasswords.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_password, null);
        EditText etName = dialogView.findViewById(R.id.et_account_name);
        EditText etAccount = dialogView.findViewById(R.id.et_account);
        EditText etPassword = dialogView.findViewById(R.id.et_password);

        new AlertDialog.Builder(this)
                .setTitle("添加密码")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String account = etAccount.getText().toString().trim();
                    String pwd = etPassword.getText().toString().trim();
                    if (name.isEmpty() || pwd.isEmpty()) {
                        Toast.makeText(this, "请填写名称和密码", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    passwords.put(name, new String[]{pwd, account});
                    updateList();
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteDialog(int position) {
        String name = passwordList.get(position).split("\n")[0].replace("🔐 ", "");
        new AlertDialog.Builder(this)
                .setTitle("删除密码")
                .setMessage("确定删除 " + name + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    passwords.remove(name);
                    updateList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("password", text);
        clipboard.setPrimaryClip(clip);
    }

    private void updateList() {
        passwordList.clear();
        for (Map.Entry<String, String[]> entry : passwords.entrySet()) {
            String account = entry.getValue()[1];
            String info = "🔐 " + entry.getKey();
            if (!account.isEmpty()) {
                info += "\n   账号: " + account;
            }
            info += "\n   点击复制密码";
            passwordList.add(info);
        }
        adapter.notifyDataSetChanged();
    }
}
