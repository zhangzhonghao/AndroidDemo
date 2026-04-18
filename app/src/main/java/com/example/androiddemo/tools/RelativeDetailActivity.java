package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class RelativeDetailActivity extends AppCompatActivity {
    private Spinner spinnerRelation;
    private EditText etFromName;
    private EditText etToName;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative_detail);
        initViews();
    }

    private void initViews() {
        spinnerRelation = findViewById(R.id.spinner_relation);
        etFromName = findViewById(R.id.et_from_name);
        etToName = findViewById(R.id.et_to_name);
        tvResult = findViewById(R.id.tv_result);
        Button btnQuery = findViewById(R.id.btn_query);

        String[] relations = {"父亲-儿子", "父亲-女儿", "母亲-儿子", "母亲-女儿",
                              "哥哥-弟弟", "哥哥-妹妹", "姐姐-弟弟", "姐姐-妹妹"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, relations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelation.setAdapter(adapter);

        btnQuery.setOnClickListener(v -> queryRelation());
    }

    private void queryRelation() {
        String fromName = etFromName.getText().toString();
        String toName = etToName.getText().toString();
        if (fromName.isEmpty() || toName.isEmpty()) {
            tvResult.setText("请输入称呼人姓名");
            return;
        }
        String relation = (String) spinnerRelation.getSelectedItem();
        tvResult.setText(fromName + "的" + relation.split("-")[1] + "是" + toName);
    }
}