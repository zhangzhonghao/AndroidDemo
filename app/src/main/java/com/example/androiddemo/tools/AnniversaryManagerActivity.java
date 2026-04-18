package com.example.androiddemo.tools;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AnniversaryManagerActivity extends AppCompatActivity {

    private ListView lvAnniversaries;
    private List<String> anniversaryList;
    private Map<String, Long> anniversaryDates;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anniversary_manager);

        lvAnniversaries = findViewById(R.id.lv_anniversaries);
        Button btnAdd = findViewById(R.id.btn_add_anniversary);

        anniversaryList = new ArrayList<>();
        anniversaryDates = new HashMap<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, anniversaryList);
        lvAnniversaries.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });

        lvAnniversaries.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_anniversary, null);
        EditText etName = dialogView.findViewById(R.id.et_anniversary_name);
        Button btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        final long[] selectedDate = {0};

        btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedDate[0] = selected.getTimeInMillis();
                    btnPickDate.setText(new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                            .format(new Date(selectedDate[0])));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
               calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        new AlertDialog.Builder(this)
                .setTitle("添加纪念日")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedDate[0] == 0) {
                        Toast.makeText(this, "请选择日期", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    anniversaryDates.put(name, selectedDate[0]);
                    updateList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDeleteDialog(int position) {
        String name = anniversaryList.get(position).split(" - ")[0];
        new AlertDialog.Builder(this)
                .setTitle("删除纪念日")
                .setMessage("确定删除 " + name + " 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    anniversaryDates.remove(name);
                    updateList();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateList() {
        anniversaryList.clear();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, Long> entry : anniversaryDates.entrySet()) {
            long diff = entry.getValue() - now;
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            String daysText;

            if (days == 0) {
                daysText = "就是今天！";
            } else if (days > 0) {
                daysText = "还有 " + days + " 天";
            } else {
                daysText = "已过 " + (-days) + " 天";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
            anniversaryList.add(entry.getKey() + " - " + sdf.format(new Date(entry.getValue())) + "\n" + daysText);
        }
        adapter.notifyDataSetChanged();
    }
}
