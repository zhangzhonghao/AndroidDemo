package com.example.androiddemo.tools;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RelativeCalculatorActivity extends AppCompatActivity {

    private RadioGroup rgGender;
    private RadioButton rbMale;
    private RadioButton rbFemale;
    private Spinner spinnerRelationType;
    private EditText etInput;
    private TextView tvResult;
    private Button btnCalculate;

    // 我的性别
    private boolean isMale = true;
    // 关系类型: 父系(0) / 母系(1)
    private int relationType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative_calculator);

        initViews();
        setupListeners();
    }

    private void initViews() {
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        spinnerRelationType = findViewById(R.id.spinner_relation_type);
        etInput = findViewById(R.id.et_input);
        tvResult = findViewById(R.id.tv_result);
        btnCalculate = findViewById(R.id.btn_calculate);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("亲戚称呼计算器");
        }

        // 设置Spinner适配器
        String[] relationTypes = {"父系亲属", "母系亲属"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, relationTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRelationType.setAdapter(adapter);
    }

    private void setupListeners() {
        rgGender.setOnCheckedChangeListener((group, checkedId) -> {
            isMale = (checkedId == R.id.rb_male);
            calculateRelation();
        });

        spinnerRelationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                relationType = position;
                calculateRelation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calculateRelation();
            }
        });

        btnCalculate.setOnClickListener(v -> calculateRelation());
    }

    private void calculateRelation() {
        String input = etInput.getText().toString().trim();
        if (input.isEmpty()) {
            tvResult.setText("请输入亲戚关系描述\n例如：妈妈的母亲 或 外婆的女儿");
            return;
        }

        String result = parseRelation(input);
        tvResult.setText(result);
    }

    private String parseRelation(String input) {
        // 标准化输入：去掉"我"和"的"以及空格
        input = input.replace("我", "");
        input = input.replace("的", "");
        input = input.replace(" ", "");

        if (input.isEmpty()) {
            return "请输入有效的关系描述";
        }

        // 解析关系路径
        String[] parts = splitRelation(input);
        if (parts.length < 2) {
            return "无法解析该关系，请检查输入格式";
        }

        // 关系方向：父系或母系
        boolean isPaternal = relationType == 0;

        // 从起点追溯到终点的称呼
        String result = traceRelation(parts, isPaternal);

        if (result == null) {
            return "无法计算该亲戚关系";
        }

        return "称呼：「" + result + "」";
    }

    // 追溯关系的主方法
    private String traceRelation(String[] parts, boolean isPaternal) {
        // 辈分关系：向上为负，向下为正，0为自己
        // 首先确定起始称呼对应的辈分
        int generation = getGeneration(parts[0], isPaternal);
        if (generation == Integer.MAX_VALUE) {
            return null; // 无法识别
        }

        // 从第二个人开始，追踪每一层关系
        for (int i = 1; i < parts.length; i++) {
            String current = parts[i];
            // 处理同辈情况（去外婆家，妈妈的妈妈还是外婆）
            if (i > 0 && current.equals(parts[i - 1])) {
                continue;
            }
            int nextGen = getGeneration(current, isPaternal);
            if (nextGen == Integer.MAX_VALUE) {
                // 可能是同辈称呼
                String sameGen = handleSameGeneration(parts[i - 1], current, isPaternal);
                if (sameGen != null) {
                    // 同辈关系不改变辈分，但确定称谓
                } else {
                    return null;
                }
            } else {
                generation = nextGen;
            }
        }

        // 根据最终辈分和性别返回称呼
        return getTitleByGeneration(generation, isPaternal);
    }

    // 获取称呼对应的辈分（相对于"我"）
    // 向上走（比如找爷爷）辈分+1，向下走（比如找儿子）辈分-1
    private int getGeneration(String title, boolean isPaternal) {
        switch (title) {
            // 祖辈
            case "爷爷": return 2;
            case "奶奶": return 2;
            case "外公":
            case "姥爷": return isPaternal ? Integer.MAX_VALUE : 2;
            case "外婆":
            case "姥姥": return isPaternal ? Integer.MAX_VALUE : 2;
            case "公公": return isPaternal ? 2 : Integer.MAX_VALUE;
            case "婆婆": return isPaternal ? 2 : Integer.MAX_VALUE;
            // 父母辈
            case "爸爸":
            case "爸":
            case "父亲": return 1;
            case "妈妈":
            case "妈":
            case "母亲": return 1;
            case "岳父": return isPaternal ? 1 : Integer.MAX_VALUE;
            case "岳母": return isPaternal ? 1 : Integer.MAX_VALUE;
            // 叔伯姑舅姨
            case "叔叔": return 1;
            case "伯伯": return 1;
            case "姑姑": return 1;
            case "舅舅": return 1;
            case "姨": return 1;
            // 同辈
            case "哥哥":
            case "哥":
            case "弟弟":
            case "姐姐":
            case "妹妹": return 0;
            case "表哥":
            case "表姐":
            case "表弟":
            case "表妹":
            case "堂哥":
            case "堂姐":
            case "堂弟":
            case "堂妹": return 0;
            // 子辈
            case "儿子": return -1;
            case "女儿": return -1;
            case "侄子": return -1;
            case "侄女": return -1;
            case "外甥": return -1;
            case "外甥女": return -1;
            // 孙辈
            case "孙子": return -2;
            case "孙女": return -2;
            case "外孙": return -2;
            case "外孙女": return -2;
            default: return Integer.MAX_VALUE;
        }
    }

    // 根据最终辈分返回称呼
    private String getTitleByGeneration(int generation, boolean isPaternal) {
        if (generation == Integer.MAX_VALUE) return null;

        if (generation >= 2) {
            // 祖辈或更高
            if (generation == 2) {
                return isPaternal ? "爷爷/奶奶" : "外公/外婆";
            } else if (generation == 3) {
                return isPaternal ? "曾祖父母" : "曾外祖父母";
            } else {
                return "先祖";
            }
        } else if (generation == 1) {
            // 父母辈
            return isPaternal ? "爸爸/叔叔/姑姑" : "妈妈/舅舅/姨";
        } else if (generation == 0) {
            // 同辈
            return isMale ? "兄弟" : "姐妹";
        } else if (generation == -1) {
            // 子辈
            return isMale ? "儿子" : "女儿";
        } else if (generation == -2) {
            // 孙辈
            return isMale ? "孙子" : "孙女";
        } else {
            return "后代";
        }
    }

    // 处理同辈关系
    private String handleSameGeneration(String from, String to, boolean isPaternal) {
        String[] siblings = {"哥哥", "哥", "弟弟", "姐姐", "妹妹"};
        String[] paternalSiblings = {"叔叔", "伯伯", "姑姑"};
        String[] maternalSiblings = {"舅舅", "姨"};

        boolean fromIsSibling = false;
        boolean toIsSibling = false;
        boolean fromIsPaternal = false;
        boolean toIsPaternal = false;

        for (String s : siblings) {
            if (s.equals(from)) fromIsSibling = true;
            if (s.equals(to)) toIsSibling = true;
        }
        for (String s : paternalSiblings) {
            if (s.equals(from)) {
                fromIsSibling = true;
                fromIsPaternal = true;
            }
            if (s.equals(to)) {
                toIsSibling = true;
                toIsPaternal = true;
            }
        }
        for (String s : maternalSiblings) {
            if (s.equals(from)) {
                fromIsSibling = true;
            }
            if (s.equals(to)) {
                toIsSibling = true;
            }
        }

        if (fromIsSibling && toIsSibling) {
            if (fromIsPaternal && toIsPaternal) {
                return isMale ? "堂兄弟" : "堂姐妹";
            } else if (!fromIsPaternal && !toIsPaternal) {
                return isMale ? "表兄弟" : "表姐妹";
            } else {
                return isMale ? "表兄弟" : "表姐妹";
            }
        }
        return null;
    }

    private String[] splitRelation(String input) {
        // 将关系字符串分割为称呼数组
        List<String> titles = new LinkedList<>();
        String current = "";

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String twoChar = current + c;

            // 检查常见两字称呼
            if (isValidTitle(twoChar)) {
                titles.add(twoChar);
                current = "";
            } else if (isValidTitle(String.valueOf(c))) {
                titles.add(String.valueOf(c));
                current = "";
            } else {
                current += c;
            }
        }

        return titles.toArray(new String[0]);
    }

    private boolean isValidTitle(String title) {
        String[] validTitles = {
            "爸爸", "妈妈", "父亲", "母亲", "爸", "妈",
            "爷爷", "奶奶", "姥姥", "姥爷", "外公", "外婆",
            "公公", "婆婆", "岳父", "岳母",
            "叔叔", "伯伯", "舅舅", "姑姑", "姨",
            "表哥", "表姐", "表弟", "表妹",
            "堂哥", "堂姐", "堂弟", "堂妹",
            "哥哥", "姐姐", "弟弟", "妹妹",
            "儿子", "女儿", "孙子", "孙女", "外孙", "外孙女",
            "侄子", "侄女", "外甥", "外甥女",
            "丈夫", "妻子", "老公", "老婆",
            "爸爸", "妈妈", "老公", "老婆"
        };

        for (String t : validTitles) {
            if (t.equals(title)) return true;
        }
        return false;
    }

    private String getMyTitle(String startTitle, boolean isPaternal) {
        // 确定以谁为起点
        switch (startTitle) {
            case "妈妈":
            case "妈":
                return isMale ? "儿子" : "女儿";
            case "爸爸":
            case "爸":
                return isMale ? "儿子" : "女儿";
            case "爷爷":
                return isMale ? "孙子" : "孙女";
            case "奶奶":
                return isMale ? "孙子" : "孙女";
            case "姥姥":
            case "外婆":
            case "姥爷":
            case "外公":
                return isMale ? "外孙" : "外孙女";
            case "舅舅":
                return isMale ? "外甥" : "外甥女";
            case "姑姑":
                return isMale ? "侄子" : "侄女";
            case "叔叔":
                return isMale ? "侄子" : "侄女";
            case "姨":
                return isMale ? "外甥" : "外甥女";
            case "哥哥":
            case "哥":
                return isMale ? "弟弟" : "妹妹";
            case "姐姐":
                return isMale ? "弟弟" : "妹妹";
            case "弟弟":
                return isMale ? "哥哥" : "姐姐";
            case "妹妹":
                return isMale ? "哥哥" : "姐姐";
            default:
                return null;
        }
    }

    private String calculateFinalTitle(String[] parts, boolean isPaternal) {
        if (parts.length == 2) {
            // 直接关系
            return getDirectRelation(parts[0], parts[1], isPaternal);
        } else {
            // 多重关系，需要逐步推导
            String currentTitle = parts[0];
            for (int i = 1; i < parts.length; i++) {
                String nextTitle = parts[i];
                currentTitle = getNextRelation(currentTitle, nextTitle, isPaternal);
                if (currentTitle == null) return null;
            }
            return currentTitle;
        }
    }

    private String getDirectRelation(String from, String to, boolean isPaternal) {
        // 我的性别对应的称呼
        String myGenderTitle = isMale ? "我" : "我(女)";

        // 直接关系映射 (from -> to -> result)
        // 父系关系
        if (isPaternal) {
            switch (from) {
                case "爸爸":
                    if (to.equals("爷爷")) return "父亲";
                    if (to.equals("奶奶")) return "父亲";
                    if (to.equals("叔叔")) return "子女";
                    if (to.equals("姑姑")) return "子女";
                    break;
                case "妈妈":
                    if (to.equals("姥姥") || to.equals("外婆")) return "母亲";
                    if (to.equals("姥爷") || to.equals("外公")) return "母亲";
                    if (to.equals("舅舅")) return "子女";
                    if (to.equals("姨")) return "子女";
                    break;
                case "爷爷":
                    if (to.equals("爸爸")) return "父亲";
                    if (to.equals("奶奶")) return "配偶";
                    if (to.equals("叔叔")) return "子女";
                    if (to.equals("姑姑")) return "子女";
                    break;
                case "奶奶":
                    if (to.equals("爷爷")) return "配偶";
                    if (to.equals("爸爸")) return "母亲";
                    break;
                case "叔叔":
                    if (to.equals("爷爷")) return "父亲";
                    if (to.equals("奶奶")) return "母亲";
                    if (to.equals("爸爸")) return "兄弟";
                    if (to.equals("姑姑")) return "兄弟";
                    break;
                case "姑姑":
                    if (to.equals("爷爷")) return "父亲";
                    if (to.equals("奶奶")) return "母亲";
                    if (to.equals("爸爸")) return "姐妹";
                    break;
            }
        } else {
            // 母系关系
            switch (from) {
                case "妈妈":
                case "妈":
                    if (to.equals("姥姥") || to.equals("外婆")) return "母亲";
                    if (to.equals("姥爷") || to.equals("外公")) return "母亲";
                    if (to.equals("舅舅")) return "子女";
                    if (to.equals("姨")) return "子女";
                    break;
                case "姥姥":
                case "外婆":
                    if (to.equals("妈妈") || to.equals("妈")) return "子女";
                    if (to.equals("舅舅")) return "子女";
                    if (to.equals("姨")) return "子女";
                    break;
                case "姥爷":
                case "外公":
                    if (to.equals("姥姥") || to.equals("外婆")) return "配偶";
                    if (to.equals("妈妈") || to.equals("妈")) return "父亲";
                    break;
                case "舅舅":
                    if (to.equals("姥姥") || to.equals("外婆")) return "子女";
                    if (to.equals("姥爷") || to.equals("外公")) return "子女";
                    if (to.equals("妈妈") || to.equals("妈")) return "兄弟";
                    if (to.equals("姨")) return "兄弟";
                    break;
                case "姨":
                    if (to.equals("姥姥") || to.equals("外婆")) return "子女";
                    if (to.equals("姥爷") || to.equals("外公")) return "子女";
                    if (to.equals("妈妈") || to.equals("妈")) return "姐妹";
                    break;
            }
        }

        // 同辈关系
        if (isSameGeneration(from, to)) {
            if (from.equals("哥哥") || from.equals("哥") || from.equals("弟弟") ||
                from.equals("姐姐") || from.equals("妹妹")) {
                if (to.equals("哥哥") || to.equals("哥")) return "兄弟/兄妹";
                if (to.equals("姐姐")) return "兄妹/姐妹";
                if (to.equals("弟弟")) return "兄弟";
                if (to.equals("妹妹")) return "兄妹";
            }
        }

        return null;
    }

    private boolean isSameGeneration(String from, String to) {
        String[] siblings = {"哥哥", "哥", "弟弟", "姐姐", "妹妹"};
        for (String s : siblings) {
            if (from.equals(s) || to.equals(s)) return true;
        }
        return false;
    }

    private String getNextRelation(String current, String next, boolean isPaternal) {
        // 逐步追踪关系
        // 这里需要根据当前的辈分关系来推断下一步

        // 简化处理：如果无法精确计算，返回null
        if (current == null || next == null) return null;

        // 常见称呼对应的关系递进
        switch (next) {
            case "妈妈":
            case "妈":
            case "爸爸":
            case "爸":
                // 上一辈
                return current;
            case "姥姥":
            case "外婆":
            case "姥爷":
            case "外公":
            case "爷爷":
            case "奶奶":
            case "叔叔":
            case "伯伯":
            case "姑姑":
            case "舅舅":
            case "姨":
                return current;
            default:
                return null;
        }
    }

    public void onQuickQueryClick(View view) {
        String query = "";
        int id = view.getId();

        if (id == R.id.btn_query_1) {
            query = "妈妈的妈妈";
        } else if (id == R.id.btn_query_2) {
            query = "外婆的女儿";
        } else if (id == R.id.btn_query_3) {
            query = "奶奶的孙子";
        } else if (id == R.id.btn_query_4) {
            query = "舅舅的外甥";
        } else if (id == R.id.btn_query_5) {
            query = "姑姑的侄子";
        } else if (id == R.id.btn_query_6) {
            query = "姨的外甥女";
        }

        if (!query.isEmpty()) {
            etInput.setText(query);
            calculateRelation();
        }
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