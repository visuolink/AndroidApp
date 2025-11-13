package com.vibedev.visuolink.backend.applauncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapGestureToApp extends AppCompatActivity {

    private List<AppInstalledInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_launcher);

        ListView listView = findViewById(R.id.appListView);
        PackageManager pm = this.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                getSupportActionBar().setElevation(0f);
            }
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Map App");
        }

        // This returns only apps that have a launcher icon (like the ones user sees)
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        // Sort alphabetically by app label
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));

        appList = new ArrayList<>();  // Initialize here BEFORE adding items

        for (ResolveInfo ri : resolveInfos) {
            String appName = ri.loadLabel(pm).toString();
            String packageName = ri.activityInfo.packageName;
            Drawable icon = ri.loadIcon(pm);

            appList.add(new AppInstalledInfo(appName, packageName, icon));
        }

        AppListAdapter adapter = new AppListAdapter(this, appList);
        listView.setAdapter(adapter);

        final String gestureName = getIntent().getStringExtra("gestureName");

        listView.setOnItemClickListener((parent, view, position, id) -> {
            AppInstalledInfo selected = appList.get(position);
            if (gestureName != null) {
                SharedPreferences prefs = getSharedPreferences("GestureMap", MODE_PRIVATE);
                prefs.edit().putString(gestureName, selected.getPackageName()).apply();
                HelperClass.showToast(this, "Mapped gesture to " + selected.getAppName());
                finish();
            } else {
                startActivity(new Intent(this, RecordGestureActivity.class));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appList != null) {
            appList.clear();
        }
    }
}
