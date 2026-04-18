package com.example.androiddemo.tools;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AccountBookActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvTotalIncome, tvTotalExpense, tvBalance;
    private FloatingActionButton fabAdd;
    private List<Map<String, String>> records = new ArrayList<>();
    private AccountBookAdapter adapter;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_book);

        initViews();
        loadRecords();
        updateSummary();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        tvTotalIncome = findViewById(R.id.tv_total_income);
        tvTotalExpense = findViewById(R.id.tv_total_expense);
        tvBalance = findViewById(R.id.tv_balance);
        fabAdd = findViewById(R.id.fab_add);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("记账本");
        }

        adapter = new AccountBookAdapter(records);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog();
            }
        });
    }

    private void loadRecords() {
        sp = getSharedPreferences("account_book", MODE_PRIVATE);
        String data = sp.getString("records", "");
        records.clear();

        if (!data.isEmpty()) {
            String[] items = data.split(";");
            for (String item : items) {
                String[] parts = item.split(",");
                if (parts.length >= 4) {
                    Map<String, String> record = new HashMap<>();
                    record.put("type", parts[0]);
                    record.put("amount", parts[1]);
                    record.put("note", parts[2]);
                    record.put("date", parts[3]);
                    records.add(record);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void saveRecords() {
        StringBuilder sb = new StringBuilder();
        for (Map<String, String> record : records) {
            if (sb.length() > 0) sb.append(";");
            sb.append(record.get("type")).append(",")
              .append(record.get("amount")).append(",")
              .append(record.get("note")).append(",")
              .append(record.get("date"));
        }
        sp.edit().putString("records", sb.toString()).apply();
    }

    private void updateSummary() {
        double income = 0, expense = 0;
        for (Map<String, String> record : records) {
            double amount = Double.parseDouble(record.get("amount"));
            if ("income".equals(record.get("type"))) {
                income += amount;
            } else {
                expense += amount;
            }
        }

        tvTotalIncome.setText(String.format(Locale.getDefault(), "收入: ¥%.2f", income));
        tvTotalExpense.setText(String.format(Locale.getDefault(), "支出: ¥%.2f", expense));
        tvBalance.setText(String.format(Locale.getDefault(), "余额: ¥%.2f", income - expense));
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_record, null);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_type);
        EditText etAmount = dialogView.findViewById(R.id.et_amount);
        EditText etNote = dialogView.findViewById(R.id.et_note);
        RadioButton rbExpense = dialogView.findViewById(R.id.rb_expense);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("添加记录")
                .setView(dialogView)
                .setPositiveButton("添加", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String amountStr = etAmount.getText().toString().trim();
                String note = etNote.getText().toString().trim();

                if (amountStr.isEmpty()) {
                    etAmount.setError("请输入金额");
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    String type = rbExpense.isChecked() ? "expense" : "income";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                    Map<String, String> record = new HashMap<>();
                    record.put("type", type);
                    record.put("amount", String.valueOf(amount));
                    record.put("note", note.isEmpty() ? (type.equals("income") ? "收入" : "支出") : note);
                    record.put("date", sdf.format(new Date()));

                    records.add(0, record);
                    adapter.notifyItemInserted(0);
                    recyclerView.scrollToPosition(0);
                    saveRecords();
                    updateSummary();
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    etAmount.setError("无效金额");
                }
            });
        });

        dialog.show();
    }

    public void deleteRecord(int position) {
        records.remove(position);
        adapter.notifyItemRemoved(position);
        saveRecords();
        updateSummary();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class AccountBookAdapter extends RecyclerView.Adapter<AccountBookAdapter.ViewHolder> {
        private List<Map<String, String>> data;

        AccountBookAdapter(List<Map<String, String>> data) {
            this.data = data;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvAmount, tvNote, tvDate;
            Button btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tv_type);
                tvAmount = itemView.findViewById(R.id.tv_amount);
                tvNote = itemView.findViewById(R.id.tv_note);
                tvDate = itemView.findViewById(R.id.tv_date);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_account_record, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Map<String, String> record = data.get(position);
            boolean isIncome = "income".equals(record.get("type"));

            holder.tvType.setText(isIncome ? "收入" : "支出");
            holder.tvType.setTextColor(isIncome ?
                    android.graphics.Color.parseColor("#4CAF50") :
                    android.graphics.Color.parseColor("#F44336"));
            holder.tvAmount.setText((isIncome ? "+" : "-") + "¥" + record.get("amount"));
            holder.tvNote.setText(record.get("note"));
            holder.tvDate.setText(record.get("date"));

            holder.btnDelete.setOnClickListener(v -> deleteRecord(holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}