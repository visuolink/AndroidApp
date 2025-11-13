package com.vibedev.visuolink.data_model;

import android.content.Context;

public class Preferences {

    public static void init(Context context){
        VisualLinkPreferences.init(context);
    }

    public static void save(String key, String value) {
        VisualLinkPreferences.savePreference(key, value);
    }

    public static boolean getEyeGestureSwitchStatus() {
        return VisualLinkPreferences.getPreferenceBoolean("eyeGesture");
    }

    public static boolean getGestureControlSwitchStatus() {
        return VisualLinkPreferences.getPreferenceBoolean("GestureControl");
    }

    public static boolean getAppLauncherSwitchStatus() {
        return VisualLinkPreferences.getPreferenceBoolean("appLauncherGesture");
    }

    public static boolean getNotificationSwitchStatus() {
        return VisualLinkPreferences.getPreferenceBoolean("notifications");
    }

    public static boolean getDarkModeSwitchStatus() {
        return VisualLinkPreferences.getPreferenceBoolean("darkMode");
    }

    public static String getFontFamilyName(){
        return VisualLinkPreferences.getPreferenceString("font");
    }

    public static boolean getEyeGestureServiceRunningStatus(){
        return VisualLinkPreferences.getPreferenceBoolean("isEyeGestureServiceRunning");
    }

    public static void setEyeGestureSwitchStatus(boolean value) {
        VisualLinkPreferences.savePreference("eyeGesture", value);
    }

    public static void setGestureControlSwitchStatus(boolean value) {
        VisualLinkPreferences.savePreference("GestureControl", value);
    }

    public static void setAppLauncherSwitchStatus(boolean value) {
        VisualLinkPreferences.savePreference("appLauncherGesture", value);
    }

    public static void setNotificationSwitchStatus(boolean value) {
        VisualLinkPreferences.savePreference("notifications", value);
    }

    public static void setDarkModeSwitchStatus(boolean value) {
        VisualLinkPreferences.savePreference("darkMode", value);
    }

    public static void setFontFamilyName(String value){
        VisualLinkPreferences.savePreference("font", value);
    }

    public static void setEyeGestureServiceRunningStatus(boolean value) {
        VisualLinkPreferences.savePreference("isEyeGestureServiceRunning", value);
    }
}
