package com.example.androiddemo.ui.bottomnav;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.androiddemo.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class BottomNavFoldableActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private List<Fragment> fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_bottom_nav_foldable);

        initFragments();
        initBottomNav();
        setupWindowInsets();
    }

    private void initFragments() {
        fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new FeatureFragment());
        fragments.add(new TodoFragment());
        fragments.add(new MineFragment());

        // 默认显示主页
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragments.get(0))
                .commit();
    }

    private void initBottomNav() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int index = 0;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                index = 0;
            } else if (itemId == R.id.nav_feature) {
                index = 1;
            } else if (itemId == R.id.nav_todo) {
                index = 2;
            } else if (itemId == R.id.nav_mine) {
                index = 3;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragments.get(index))
                    .commit();
            return true;
        });
    }

    /**
     * 设置窗口insets监听，处理折叠屏等场景的系统栏
     */
    private void setupWindowInsets() {
        View container = findViewById(R.id.fragment_container);

        ViewCompat.setOnApplyWindowInsetsListener(container, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 根据系统栏Insets调整布局
            // 折叠屏展开时系统栏可能不同
            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 底部导航栏根据系统栏调整padding
            v.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }
}
