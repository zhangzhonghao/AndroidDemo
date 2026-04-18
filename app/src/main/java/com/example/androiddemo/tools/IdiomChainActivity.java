package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdiomChainActivity extends AppCompatActivity {

    private EditText etAnswer;
    private ListView lvResult;
    private List<String> resultList;
    private ArrayAdapter<String> adapter;

    private static final Map<Character, List<String>> IDIOM_MAP = new HashMap<>();

    static {
        IDIOM_MAP.put('一', List.of("一路平安", "一模一样", "一分为二", "一心一意", "一如既往"));
        IDIOM_MAP.put('二', List.of("二话不说", "二龙戏珠", "二八佳人", "二三其德"));
        IDIOM_MAP.put('三', List.of("三心二意", "三番五次", "三令五申", "三教九流", "三人成虎"));
        IDIOM_MAP.put('四', List.of("四季如春", "四通八达", "四平八稳", "四海为家"));
        IDIOM_MAP.put('五', List.of("五湖四海", "五光十色", "五颜六色", "五彩缤纷"));
        IDIOM_MAP.put('六', List.of("六神无主", "六亲不认", "六道轮回", "六畜兴旺"));
        IDIOM_MAP.put('七', List.of("七上八下", "七零八落", "七情六欲", "七嘴八舌"));
        IDIOM_MAP.put('八', List.of("八面玲珑", "八仙过海", "八方呼应", "八拜之交"));
        IDIOM_MAP.put('九', List.of("九死一生", "九牛一毛", "九霄云外", "九九归一"));
        IDIOM_MAP.put('十', List.of("十全十美", "十万火急", "十恶不赦", "十指连心"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idiom_chain);

        etAnswer = findViewById(R.id.et_answer);
        lvResult = findViewById(R.id.rv_history);

        resultList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, resultList);
        lvResult.setAdapter(adapter);
    }

    public void onSearchClick(View view) {
        String idiom = etAnswer.getText().toString().trim();
        if (TextUtils.isEmpty(idiom)) {
            Toast.makeText(this, "请输入成语", Toast.LENGTH_SHORT).show();
            return;
        }
        if (idiom.length() < 2) {
            Toast.makeText(this, "请输入至少2个字的成语", Toast.LENGTH_SHORT).show();
            return;
        }

        char lastChar = idiom.charAt(idiom.length() - 1);
        List<String> matches = IDIOM_MAP.get(lastChar);

        resultList.clear();
        if (matches != null && !matches.isEmpty()) {
            resultList.addAll(matches);
        } else {
            resultList.add("未找到匹配的成语");
        }
        adapter.notifyDataSetChanged();
    }
}
