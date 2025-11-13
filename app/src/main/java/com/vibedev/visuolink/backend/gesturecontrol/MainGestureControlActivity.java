package com.vibedev.visuolink.backend.gesturecontrol;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.vibedev.visuolink.R;

public class MainGestureControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_control);

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
            getSupportActionBar().setTitle("Gesture Control");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        openHiddenLayout(findViewById(R.id.working), findViewById(R.id.hiddenInfo), true);
        openHiddenLayout(findViewById(R.id.eye_gesture_info), findViewById(R.id.hiddenInfoEyeGesture), false);
        openHiddenLayout(findViewById(R.id.screenshot_gesture_info), findViewById(R.id.hiddenInfoScreenshotGesture), false);
        openHiddenLayout(findViewById(R.id.screen_recorder_gesture_info), findViewById(R.id.hiddenInfoScreenRecorderGesture), false);

    }

    private void openHiddenLayout(TextView textView, View view, boolean isHelpingLayout){
        if (!isHelpingLayout) {
            textView.setOnClickListener(v -> {
                if (view.getVisibility() == View.GONE) {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.arrow_up, 0, 0, 0);
                    view.setVisibility(View.VISIBLE);
                } else {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.arrow_right_sided, 0, 0, 0);
                    view.setVisibility(View.GONE);
                }
            });
        } else {
            textView.setOnClickListener(v -> {
                if (view.getVisibility() == View.GONE){
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.arrow_up, 0);
                    view.setVisibility(View.VISIBLE);
                } else {
                    textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.arrow_down, 0);
                    view.setVisibility(View.GONE);
                }
            });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
