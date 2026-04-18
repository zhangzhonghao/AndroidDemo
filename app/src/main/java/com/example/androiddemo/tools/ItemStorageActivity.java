package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStorageActivity extends AppCompatActivity {

    private ListView lvItems;
    private List<String> itemList;
    private Map<String, String[]> items;
    private ArrayAdapter<String> adapter;

    private String[] locations = {"客厅", "卧室", "厨房", "卫生间", "书房", "储藏室", "汽车", "其他"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_storage);

        lvItems = findViewById(R.id.lv_items);
        Button btnAdd = findViewById(R.id.btn_add_item);

        itemList = new ArrayList<>();
        items = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        lvItems.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        lvItems.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        EditText etName = dialogView.findViewById(R.id.et_item_name);
        EditText etDesc = dialogView.findViewById(R.id.et_item_desc);
        Spinner spinnerLocation = dialogView.findViewById(R.id.spinner_location);

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(locationAdapter);

        new AlertDialog.Builder(this)
                .setTitle("添加收纳物品")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String desc = etDesc.getText().toString().trim();
                    String location = locations[spinnerLocation.getSelectedItemPosition()];
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入物品名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    items.put(name, new String[]{desc, location});
                    updateList();
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteDialog(int position) {
        String name = itemList.get(position).split("\n")[0].replace("📦 ", "");
        new AlertDialog.Builder(this)
                .setTitle("删除物品")
                .setMessage("确定删除 " + name + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    items.remove(name);
                    updateList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateList() {
        itemList.clear();
        for (Map.Entry<String, String[]> entry : items.entrySet()) {
            String info = "📦 " + entry.getKey() + "\n   位置: " + entry.getValue()[1];
            if (!entry.getValue()[0].isEmpty()) {
                info += " | " + entry.getValue()[0];
            }
            itemList.add(info);
        }
        adapter.notifyDataSetChanged();
    }
}
