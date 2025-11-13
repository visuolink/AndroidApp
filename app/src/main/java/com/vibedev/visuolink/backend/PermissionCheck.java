package com.vibedev.visuolink.backend;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;
import com.vibedev.visuolink.backend.accessibility.AppAccessibilityService;

public class PermissionCheck {

    public static final int REQUEST_OVERLAY_PERMISSION_CODE = 1001;

    private Activity activity;

    public PermissionCheck(Activity activity) {
        this.activity = activity;
    }

    public void checkAndRequestPermissions() {
        if (!isAccessibilityServiceEnabled(activity, AppAccessibilityService.class)) {
            promptEnableAccessibilityService();
            return;
        }

        if (!Settings.canDrawOverlays(activity)) {
            promptEnableOverlayPermission();
        }
    }

    public boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponent = new ComponentName(context, accessibilityService);
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServices == null) return false;
        TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
        splitter.setString(enabledServices);
        while (splitter.hasNext()) {
            String compName = splitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(compName);
            if (enabledService != null && enabledService.equals(expectedComponent)) return true;
        }
        return false;
    }

    private void promptEnableAccessibilityService() {
        new AlertDialog.Builder(activity)
                .setTitle("Accessibility Permission")
                .setMessage("Enable VisuoLink Accessibility Service to use gestures.")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    HelperClass.showToast(activity, "Accessibility Service is required.");
                    activity.finish();
                })
                .setIcon(R.drawable.setting_icon)
                .show();
    }

    private void promptEnableOverlayPermission() {
        new AlertDialog.Builder(activity)
                .setTitle("Overlay Permission")
                .setMessage("Allow VisuoLink to display over other apps in settings.")
                .setCancelable(false)
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION_CODE);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    HelperClass.showToast(activity, "Overlay permission is required.");
                    activity.finish();
                })
                .setIcon(R.drawable.setting_icon)
                .show();
    }

    public void getCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(this.activity, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.FOREGROUND_SERVICE_CAMERA
            }, 102);
        } else {
            ActivityCompat.requestPermissions(this.activity, new String[]{
                    Manifest.permission.CAMERA
            }, 101);
        }
    }

    public void getSystemAlertWindowPermissions(){
        ActivityCompat.requestPermissions(this.activity, new String[]{
                Manifest.permission.SYSTEM_ALERT_WINDOW
        }, 104);
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void getNotificationPermissions() {
        ActivityCompat.requestPermissions(this.activity, new String[]{
                Manifest.permission.POST_NOTIFICATIONS
        }, 100);
    }
}
