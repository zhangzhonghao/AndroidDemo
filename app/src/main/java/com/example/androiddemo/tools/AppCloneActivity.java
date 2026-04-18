package com.example.androiddemo.tools;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androiddemo.R;
import java.util.ArrayList;
import java.util.List;

public class AppCloneActivity extends AppCompatActivity {
    private ListView lvApps;
    private SearchView svSearch;
    private List<AppInfo> appList = new ArrayList<>();
    private List<AppInfo> filteredList = new ArrayList<>();
    private AppCloneAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_clone);
        lvApps = findViewById(R.id.lv_apps);
        svSearch = findViewById(R.id.sv_search);
        loadApps();
        adapter = new AppCloneAdapter(this, filteredList);
        lvApps.setAdapter(adapter);
        lvApps.setOnItemClickListener((parent, view, position, id) -> {
            AppInfo app = filteredList.get(position);
            createShortcut(app);
        });
        svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterApps(newText);
                return true;
            }
        });
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : apps) {
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appList.add(new AppInfo(info.loadLabel(pm).toString(), info.packageName, info.loadIcon(pm)));
            }
        }
        filteredList.addAll(appList);
    }

    private void filterApps(String query) {
        filteredList.clear();
        for (AppInfo app : appList) {
            if (app.label.contains(query) || app.packageName.contains(query)) {
                filteredList.add(app);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void createShortcut(AppInfo app) {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(app.packageName, app.packageName + ".MainActivity");
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, app.label);
        Bitmap bitmap = null;
        if (app.icon instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) app.icon).getBitmap();
        }
        if (bitmap != null) {
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        }
        addIntent.setAction("com.android.launcher.INSTALL_SHORTCUT");
        startActivity(addIntent);
    }

    public static class AppInfo {
        String label;
        String packageName;
        Drawable icon;
        AppInfo(String label, String packageName, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
}