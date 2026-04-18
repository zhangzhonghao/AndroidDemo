package com.example.androiddemo.tools;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;

public class FamilyTreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_tree);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("亲戚称呼计算器");
        }

        LinearLayout container = findViewById(R.id.tree_container);

        String[] generations = {"祖辈", "父辈", "同辈", "子辈", "孙辈"};

        for (String gen : generations) {
            LinearLayout genLayout = new LinearLayout(this);
            genLayout.setOrientation(LinearLayout.VERTICAL);
            genLayout.setPadding(16, 8, 16, 8);

            TextView genTitle = new TextView(this);
            genTitle.setText(gen);
            genTitle.setTextSize(18);
            genTitle.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            genTitle.setPadding(0, 16, 0, 8);
            genLayout.addView(genTitle);

            String[] titles;
            String[] relations;

            switch (gen) {
                case "祖辈":
                    titles = new String[]{"爷爷", "奶奶", "外公", "外婆"};
                    relations = new String[]{"父亲的父亲", "父亲的母亲", "母亲的父亲", "母亲的母亲"};
                    break;
                case "父辈":
                    titles = new String[]{"父亲", "母亲", "伯父", "伯母", "叔叔", "婶婶", "舅舅", "舅妈", "姑姑", "姑父"};
                    relations = new String[]{"爸爸", "妈妈", "爸爸的哥哥", "爸爸的嫂嫂", "爸爸的弟弟", "爸爸的弟妇", "妈妈的哥哥", "妈妈的嫂嫂", "爸爸的姐姐", "爸爸的姐夫"};
                    break;
                case "同辈":
                    titles = new String[]{"自己", "配偶", "哥哥", "姐姐", "弟弟", "妹妹"};
                    relations = new String[]{"本人", "妻子/丈夫", "兄", "姐", "弟", "妹"};
                    break;
                case "子辈":
                    titles = new String[]{"儿子", "女儿", "侄子", "侄女", "外甥", "外甥女"};
                    relations = new String[]{"子", "女", "兄弟的儿子", "兄弟的女儿", "姐妹的儿子", "姐妹的女儿"};
                    break;
                case "孙辈":
                    titles = new String[]{"孙子", "孙女", "外孙子", "外孙女"};
                    relations = new String[]{"儿子的儿子", "儿子的女儿", "女儿的的儿子", "女儿的女儿"};
                    break;
                default:
                    titles = new String[]{};
                    relations = new String[]{};
            }

            for (int i = 0; i < titles.length; i++) {
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(0, 4, 0, 4);

                TextView titleView = new TextView(this);
                titleView.setText(titles[i]);
                titleView.setTextSize(16);
                titleView.setTextColor(0xFF333333);
                titleView.setMinWidth(200);

                TextView relView = new TextView(this);
                relView.setText(relations[i]);
                relView.setTextSize(14);
                relView.setTextColor(0xFF666666);

                itemLayout.addView(titleView);
                itemLayout.addView(relView);
                genLayout.addView(itemLayout);
            }

            container.addView(genLayout);
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