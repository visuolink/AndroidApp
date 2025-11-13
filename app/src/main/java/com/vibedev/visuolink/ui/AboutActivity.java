package com.vibedev.visuolink.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.vibedev.visuolink.BuildConfig;
import com.vibedev.visuolink.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

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
            getSupportActionBar().setTitle("About");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView version = findViewById(R.id.version);
        String v = "V " + BuildConfig.VERSION_NAME;
        version.setText(v);

        findViewById(R.id.visuolink_website_link).setOnClickListener(V -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/visuolink/")));
        });

        TextView developerLabel = findViewById(R.id.about_developer);
        developerLabel.setOnClickListener(V -> {
            if (!developerLabel.getText().equals("X")) {
                developerLabel.setText("X");
                findViewById(R.id.about_developer_layout).setVisibility(View.VISIBLE);
            }else {
                developerLabel.setText("Developer?");
                findViewById(R.id.about_developer_layout).setVisibility(View.GONE);
            }
        });

        findViewById(R.id.developer_portfolio_link).setOnClickListener(V -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://sumit0ubey.github.io/Portfolio/")));
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
    }
}