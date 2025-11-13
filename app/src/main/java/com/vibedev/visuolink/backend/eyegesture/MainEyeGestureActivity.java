package com.vibedev.visuolink.backend.eyegesture;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.vibedev.visuolink.R;

public class MainEyeGestureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_eye_gesture);

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
            getSupportActionBar().setTitle("Eye Gesture");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView workingArrow = findViewById(R.id.working);
        workingArrow.setOnClickListener(v -> {
            if (findViewById(R.id.hiddenInfo).getVisibility() == View.GONE){
                workingArrow.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                findViewById(R.id.hiddenInfo).setVisibility(View.VISIBLE);
            } else {
                workingArrow.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                findViewById(R.id.hiddenInfo).setVisibility(View.GONE);
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
    }

}
