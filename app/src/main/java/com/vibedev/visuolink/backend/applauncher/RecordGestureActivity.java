package com.vibedev.visuolink.backend.applauncher;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;

public class RecordGestureActivity extends AppCompatActivity implements GestureOverlayView.OnGesturePerformedListener {

    private Gesture mGesture;
    private GestureLibrary gestureLib;
    private GestureOverlayView overlay;
    private ImageView preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_launcher_gesture);

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
            getSupportActionBar().setTitle("Draw a gesture to record");
        }

        overlay = findViewById(R.id.gestureOverlay);
        overlay.addOnGesturePerformedListener(this);

        preview = findViewById(R.id.gesturePreview);

        File f = new File(getFilesDir(), "gestures");
        gestureLib = GestureLibraries.fromFile(f);
        gestureLib.load();

        Button btnRetry = findViewById(R.id.btnRetry);
        Button btnSave = findViewById(R.id.btnSave);

        btnRetry.setOnClickListener(v -> {
            overlay.clear(false);
            mGesture = null;
            preview.setImageDrawable(null);
        });

        btnSave.setOnClickListener(v -> {
            if (mGesture == null) {
                HelperClass.showToast(this, "Draw a gesture first");
                return;
            }
            String gestureName = "g" + System.currentTimeMillis();
            gestureLib.addGesture(gestureName, mGesture);
            gestureLib.save();

            // After saving, open MainAppLauncherActivity to pick the app to map
            Intent pickApp = new Intent(this, MapGestureToApp.class);
            pickApp.putExtra("gestureName", gestureName);
            startActivity(pickApp);
            finish();
        });
    }

    @Override
    public void onGesturePerformed(GestureOverlayView overlayView, Gesture gesture) {
        mGesture = gesture;
        // Create simple bitmap preview
        Bitmap bm = gesture.toBitmap(100, 100, 12, ContextCompat.getColor(this, R.color.profileFullNameTextColor));
        preview.setImageBitmap(bm);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gestureLib = null;
    }
}
