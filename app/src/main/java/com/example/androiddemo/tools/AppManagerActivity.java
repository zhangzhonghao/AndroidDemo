package com.example.androiddemo.tools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androiddemo.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppManagerActivity extends AppCompatActivity implements AppListAdapter.OnAppClickListener {

    private static final int FILTER_ALL = 0;
    private static final int FILTER_USER = 1;
    private static final int FILTER_SYSTEM = 2;

    private static final int SORT_NAME = 0;
    private static final int SORT_INSTALL_TIME = 1;
    private static final int SORT_SIZE = 2;

    private RecyclerView rvApps;
    private TextView tvAppCount;
    private EditText etSearch;
    private ChipGroup chipGroupFilter;
    private Spinner spinnerSort;

    private List<AppInfo> allApps = new ArrayList<>();
    private List<AppInfo> filteredApps = new ArrayList<>();
    private AppListAdapter adapter;

    private int currentFilter = FILTER_ALL;
    private int currentSort = SORT_NAME;
    private String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        initViews();
        loadApps();
    }

    private void initViews() {
        rvApps = findViewById(R.id.rv_apps);
        tvAppCount = findViewById(R.id.tv_app_count);
        etSearch = findViewById(R.id.et_search);
        chipGroupFilter = findViewById(R.id.chip_group_filter);
        spinnerSort = findViewById(R.id.spinner_sort);

        adapter = new AppListAdapter();
        adapter.setOnAppClickListener(this);
        rvApps.setLayoutManager(new LinearLayoutManager(this));
        rvApps.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKeyword = s.toString().trim().toLowerCase();
                applyFilterAndSort();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                currentFilter = FILTER_ALL;
            } else if (checkedId == R.id.chip_user) {
                currentFilter = FILTER_USER;
            } else if (checkedId == R.id.chip_system) {
                currentFilter = FILTER_SYSTEM;
            }
            applyFilterAndSort();
        });

        String[] sortOptions = {"按名称", "按安装时间", "按应用大小"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSort = position;
                applyFilterAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadApps() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);

            allApps.clear();
            for (PackageInfo packageInfo : packages) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

                String appName = appInfo.loadLabel(pm).toString();
                String packageName = packageInfo.packageName;
                String versionName = packageInfo.versionName != null ? packageInfo.versionName : "N/A";
                int versionCode = packageInfo.versionCode;
                long installTime = packageInfo.firstInstallTime;

                File apkFile = new File(appInfo.sourceDir);
                long apkSize = apkFile.exists() ? apkFile.length() : 0;

                allApps.add(new AppInfo(appName, packageName, versionName, versionCode,
                        installTime, apkSize, appInfo.loadIcon(pm), isSystemApp));
            }

            runOnUiThread(this::applyFilterAndSort);
        }).start();
    }

    private void applyFilterAndSort() {
        filteredApps.clear();

        for (AppInfo app : allApps) {
            boolean matchesFilter = false;
            if (currentFilter == FILTER_ALL) {
                matchesFilter = true;
            } else if (currentFilter == FILTER_USER && !app.isSystemApp) {
                matchesFilter = true;
            } else if (currentFilter == FILTER_SYSTEM && app.isSystemApp) {
                matchesFilter = true;
            }

            if (matchesFilter && !searchKeyword.isEmpty()) {
                matchesFilter = app.appName.toLowerCase().contains(searchKeyword) ||
                        app.packageName.toLowerCase().contains(searchKeyword);
            }

            if (matchesFilter) {
                filteredApps.add(app);
            }
        }

        switch (currentSort) {
            case SORT_NAME:
                Collections.sort(filteredApps, (a, b) -> a.appName.compareToIgnoreCase(b.appName));
                break;
            case SORT_INSTALL_TIME:
                Collections.sort(filteredApps, (a, b) -> Long.compare(b.installTime, a.installTime));
                break;
            case SORT_SIZE:
                Collections.sort(filteredApps, (a, b) -> Long.compare(b.apkSize, a.apkSize));
                break;
        }

        adapter.setAppList(filteredApps);
        tvAppCount.setText("应用数量：" + filteredApps.size());
    }

    @Override
    public void onAppClick(AppInfo appInfo) {
        String[] options = {"打开应用", "应用详情", "卸载应用"};

        new AlertDialog.Builder(this)
                .setTitle(appInfo.appName)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openApp(appInfo.packageName);
                    } else if (which == 1) {
                        openAppSettings(appInfo.packageName);
                    } else if (which == 2) {
                        uninstallApp(appInfo.packageName, appInfo.isSystemApp);
                    }
                })
                .show();
    }

    private void openApp(String packageName) {
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        }
    }

    private void openAppSettings(String packageName) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    private void uninstallApp(String packageName, boolean isSystemApp) {
        if (isSystemApp) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("系统应用无法卸载")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }
}