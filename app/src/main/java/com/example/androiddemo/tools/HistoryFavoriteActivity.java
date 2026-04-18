package com.example.androiddemo.tools;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class HistoryFavoriteActivity extends AppCompatActivity {
    private ListView lvFavorites;
    private EditText etEvent;
    private Button btnAdd;
    private List<String> favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_favorite);
        initViews();
        loadFavorites();
    }

    private void initViews() {
        lvFavorites = findViewById(R.id.lv_favorites);
        etEvent = findViewById(R.id.et_event);
        btnAdd = findViewById(R.id.btn_add);
        favorites = new ArrayList<>();

        btnAdd.setOnClickListener(v -> addFavorite());
        lvFavorites.setAdapter(new ArrayAdapter<>(this,
            android.R.layout.simple_list_item_1, favorites));
    }

    private void loadFavorites() {
        favorites.add("1949年10月1日 - 中华人民共和国成立");
        favorites.add("1969年7月20日 - 人类首次登月");
        favorites.add("2008年8月8日 - 北京奥运会开幕");
    }

    private void addFavorite() {
        String event = etEvent.getText().toString();
        if (!event.isEmpty()) {
            favorites.add(event);
            ((ArrayAdapter) lvFavorites.getAdapter()).notifyDataSetChanged();
            etEvent.setText("");
            Toast.makeText(this, "已添加收藏", Toast.LENGTH_SHORT).show();
        }
    }
}