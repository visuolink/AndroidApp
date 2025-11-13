package com.vibedev.visuolink;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.vibedev.visuolink.data_model.ApiMonitor;
import com.vibedev.visuolink.data_model.Preferences;
import com.vibedev.visuolink.databinding.ActivityMainBinding;
import com.vibedev.visuolink.backend.PermissionCheck;
import com.vibedev.visuolink.ui.AboutActivity;
import com.vibedev.visuolink.ui.PolicyActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainActivity.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        PermissionCheck permissions = new PermissionCheck(this);
        permissions.getCameraPermissions();
        permissions.getSystemAlertWindowPermissions();


        if (navHostFragment == null) {
            return;
        }

        NavController navController = navHostFragment.getNavController();

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_profile)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mainActivity.navView, navController);

        Preferences.init(this);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setElevation(0f);
            }
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            Preferences.setDarkModeSwitchStatus(false);
        } else {
            Preferences.setDarkModeSwitchStatus(true);
        }

        switch (Preferences.getFontFamilyName()){
            case "Lato":
                setTheme(R.style.Theme_lato);
                break;
            case "Open":
                setTheme(R.style.Theme_open);
                break;
            case "Work":
                setTheme(R.style.Theme_work);
                break;
            case "Source":
                setTheme(R.style.Theme_source);
                break;
            default:
                setTheme(R.style.Theme_VisuoLink);
                break;
        }

        ApiMonitor.startApiMonitor("https://visuolinkapi.onrender.com/", this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.action_policy) {
            startActivity(new Intent(this, PolicyActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
