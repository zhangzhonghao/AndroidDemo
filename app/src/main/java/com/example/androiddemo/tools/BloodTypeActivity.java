package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BloodTypeActivity extends AppCompatActivity {

    private Spinner spParent1;
    private Spinner spParent2;
    private TextView tvResult;

    private static final String[] BLOOD_TYPES = {"A", "B", "AB", "O"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_type);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("血型遗传");
        }
    }

    private void initViews() {
        spParent1 = findViewById(R.id.sp_parent1);
        spParent2 = findViewById(R.id.sp_parent2);
        tvResult = findViewById(R.id.tv_result);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, BLOOD_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spParent1.setAdapter(adapter);
        spParent2.setAdapter(adapter);

        AdapterView.OnItemSelectedListener listener = new AdapterItemSelectedListener();
        spParent1.setOnItemSelectedListener(listener);
        spParent2.setOnItemSelectedListener(listener);
    }

    private void calculateBloodType() {
        String p1 = (String) spParent1.getSelectedItem();
        String p2 = (String) spParent2.getSelectedItem();

        Set<String> possibleTypes = getPossibleBloodTypes(p1, p2);

        String result = "可能的血型: " + String.join(", ", possibleTypes);
        tvResult.setText(result);
    }

    private Set<String> getPossibleBloodTypes(String parent1, String parent2) {
        Set<String> result = new HashSet<>();

        // 血型遗传规律
        // O型血只有 ii
        // A型血可能是 AA 或 Ai
        // B型血可能是 BB 或 Bi
        // AB型血是 AB

        Set<String> alleles1 = getAlleles(parent1);
        Set<String> alleles2 = getAlleles(parent2);

        for (String a1 : alleles1) {
            for (String a2 : alleles2) {
                String childType = getBloodType(a1, a2);
                result.add(childType);
            }
        }

        return result;
    }

    private Set<String> getAlleles(String bloodType) {
        Set<String> alleles = new HashSet<>();
        switch (bloodType) {
            case "A":
                alleles.add("A");
                alleles.add("i");
                break;
            case "B":
                alleles.add("B");
                alleles.add("i");
                break;
            case "AB":
                alleles.add("A");
                alleles.add("B");
                break;
            case "O":
                alleles.add("i");
                alleles.add("i");
                break;
        }
        return alleles;
    }

    private String getBloodType(String allele1, String allele2) {
        if (allele1.equals("i") && allele2.equals("i")) {
            return "O";
        } else if (allele1.equals("A") && allele2.equals("B") ||
                   allele1.equals("B") && allele2.equals("A")) {
            return "AB";
        } else if (allele1.equals("A") || allele2.equals("A")) {
            return "A";
        } else if (allele1.equals("B") || allele2.equals("B")) {
            return "B";
        }
        return "O";
    }

    private class AdapterItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            calculateBloodType();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
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