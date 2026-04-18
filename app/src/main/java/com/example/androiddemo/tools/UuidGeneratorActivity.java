package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.UUID;

public class UuidGeneratorActivity extends AppCompatActivity {

    private TextView tvUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uuid_generator);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("UUID生成器");
        }

        tvUuid = findViewById(R.id.tv_uuid);
        Button btnGenerate = findViewById(R.id.btn_generate);
        Button btnCopy = findViewById(R.id.btn_copy);
        Button btnClear = findViewById(R.id.btn_clear);

        btnGenerate.setOnClickListener(v -> generate());
        btnCopy.setOnClickListener(v -> copy());
        btnClear.setOnClickListener(v -> clear());

        generate();
    }

    private void generate() {
        UUID uuid = UUID.randomUUID();
        tvUuid.setText(uuid.toString());
    }

    private void copy() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("UUID", tvUuid.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show();
    }

    private void clear() {
        tvUuid.setText("");
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
