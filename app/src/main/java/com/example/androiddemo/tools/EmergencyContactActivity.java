package com.example.androiddemo.tools;

import android.content.Intent;
import android.net.Uri;
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

public class EmergencyContactActivity extends AppCompatActivity {

    private ListView lvContacts;
    private List<String> contactList;
    private Map<String, String> contacts;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        lvContacts = findViewById(R.id.lv_contacts);
        Button btnAdd = findViewById(R.id.btn_add_contact);

        contactList = new ArrayList<>();
        contacts = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        lvContacts.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        lvContacts.setOnItemClickListener((parent, view, position, id) -> {
            String name = contactList.get(position).split(":")[0].trim();
            String phone = contacts.get(name);
            if (phone != null) {
                callPhone(phone);
            }
        });

        lvContacts.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText etName = dialogView.findViewById(R.id.et_contact_name);
        EditText etPhone = dialogView.findViewById(R.id.et_contact_phone);

        new AlertDialog.Builder(this)
                .setTitle("添加紧急联系人")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    contacts.put(name, phone);
                    updateList();
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteDialog(int position) {
        String name = contactList.get(position).split(":")[0].trim();
        new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定删除 " + name + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    contacts.remove(name);
                    updateList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void callPhone(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    private void updateList() {
        contactList.clear();
        for (Map.Entry<String, String> entry : contacts.entrySet()) {
            contactList.add(entry.getKey() + " : " + entry.getValue());
        }
        adapter.notifyDataSetChanged();
    }
}
