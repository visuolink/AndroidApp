package com.vibedev.visuolink.backend.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import com.vibedev.visuolink.backend.applauncher.GestureCaptureActivity;

public class AppAccessibilityService extends AccessibilityService {

    private static final long DOUBLE_PRESS_MS = 450;
    private long lastUpTime = 0;
    private int upCount = 0;
    private static AppAccessibilityService instance;

    @Override
    protected void onServiceConnected() {
        instance = this;
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            long now = System.currentTimeMillis();
            if (now - lastUpTime <= DOUBLE_PRESS_MS) {
                upCount++;
            } else {
                upCount = 1;
            }
            lastUpTime = now;

            if (upCount == 2) {
                upCount = 0;
                triggerGestureCanvas();
                return true;
            }
            return true;
        }
        return super.onKeyEvent(event);
    }

    private void triggerGestureCanvas() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "Visuolink:Wake");
            try {
                wl.acquire(3000);
            } catch (Exception ignored) {}
        }
        startActivity(new Intent(this, GestureCaptureActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}

    public static AppAccessibilityService getInstance() {
        return instance;
    }

    public void performClickAt(Point point) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Path clickPath = new Path();
            clickPath.moveTo(point.x, point.y);

            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50));

            dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
        }
    }

    public void showRecentAction(){
        instance.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS);
    }

    public void goTOHomeScreen(){
        instance.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }


}
