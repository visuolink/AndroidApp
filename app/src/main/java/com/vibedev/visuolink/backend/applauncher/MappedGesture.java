package com.vibedev.visuolink.backend.applauncher;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MappedGesture extends AppCompatActivity {

    private GestureLibrary gestureLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapped_gesture);

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
            getSupportActionBar().setTitle("Mapped Gesture");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView listView = findViewById(R.id.gestureList);

        File f = new File(getFilesDir(), "gestures");
        gestureLib = GestureLibraries.fromFile(f);

        if (!gestureLib.load()) {
            HelperClass.showToast(this, "No Gesture Found");
            return;
        }

        SharedPreferences prefs = getSharedPreferences("GestureMap", MODE_PRIVATE);
        PackageManager pm = getPackageManager();

        List<HashMap<String, Object>> data = new ArrayList<>();

        for (String gestureName : gestureLib.getGestureEntries()) {
            List<Gesture> gestures = gestureLib.getGestures(gestureName);
            if (gestures == null || gestures.isEmpty()) continue;

            // Gesture preview
            Gesture g = gestures.get(0);
            Bitmap bm = g.toBitmap(100, 100, 8, ContextCompat.getColor(this, R.color.profileFullNameTextColor));

            // Mapped app
            String pkg = prefs.getString(gestureName, null);
            String appName = "Unmapped";
            if (pkg != null) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                    appName = pm.getApplicationLabel(appInfo).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    appName = "App not found";
                }
            }

            // Put data for adapter
            HashMap<String, Object> map = new HashMap<>();
            map.put("gesture", bm);
            map.put("name", appName);
            data.add(map);
        }

        // Adapter to show gesture + app name
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                R.layout.item_gesture,
                new String[]{"gesture", "name"},
                new int[]{R.id.gesturePreview, R.id.appName}
        );

        // Bind image properly
        adapter.setViewBinder((view, data1, textRepresentation) -> {
            if (view.getId() == R.id.gesturePreview && data1 instanceof Bitmap) {
                ((android.widget.ImageView) view).setImageBitmap((Bitmap) data1);
                return true;
            }
            return false;
        });

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedGestureName = (String) gestureLib.getGestureEntries().toArray()[position];

            new AlertDialog.Builder(this)
                    .setTitle("Delete Gesture")
                    .setMessage("Are you sure you want to delete this gesture?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        gestureLib.removeEntry(selectedGestureName);
                        if (gestureLib.save()) {
                            gestureLib.save();

                            data.remove(position);
                            adapter.notifyDataSetChanged();

                            HelperClass.showToast(this, "Gesture Deleted");
                        } else {
                            HelperClass.showToast(this, "Failed to delete gesture");
                        }
                    })
                    .setNegativeButton("No", null)
                    .setIcon(R.drawable.delete_icon)
                    .show();
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
        gestureLib = null;
    }
}
