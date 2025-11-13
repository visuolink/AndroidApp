package com.vibedev.visuolink.backend.applauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;

import java.io.File;
import java.util.ArrayList;

/**
 * An Activity that captures user gestures and launches associated applications.
 * This activity is displayed on top of the lock screen.
 */
public class GestureCaptureActivity extends Activity implements GestureOverlayView.OnGesturePerformedListener {
    private GestureLibrary gestureLib;

    /**
     * Called when the activity is first created.
     * This method initializes the activity, sets up the gesture overlay, and loads the gesture library.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.activity_launcher_gesture_capture);
        GestureOverlayView overlay = findViewById(R.id.gestureOverlay);
        overlay.addOnGesturePerformedListener(this);

        File f = new File(getFilesDir(), "gestures");
        gestureLib = GestureLibraries.fromFile(f);
        gestureLib.load();
    }

    /**
     * Called when a gesture is performed on the GestureOverlayView.
     * This method recognizes the gesture and attempts to launch the associated application.
     *
     * @param overlay The GestureOverlayView where the gesture was performed.
     * @param gesture The gesture that was performed.
     */
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = gestureLib.recognize(gesture);

        if (!predictions.isEmpty() && predictions.get(0).score > 2.0) {
            String gestureName = predictions.get(0).name;

            SharedPreferences prefs = getSharedPreferences("GestureMap", MODE_PRIVATE);
            String packageName = prefs.getString(gestureName, null);

            if (packageName != null) {
                Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(launch);
                    finish();
                    return;
                } else {
                    HelperClass.showToast(this, "App not installed");
                }
            } else {
                HelperClass.showToast(this, "No app mapped");
            }
        } else {
            HelperClass.showToast(this, "Gesture not recognized");
        }
        overlay.clear(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gestureLib = null;
    }
}
