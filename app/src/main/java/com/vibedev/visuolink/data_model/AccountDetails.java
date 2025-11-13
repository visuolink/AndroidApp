package com.vibedev.visuolink.data_model;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountDetails {

    private static SharedPreferences prefs = null;
    private static SharedPreferences.Editor editor = null;

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("visual_link_account_details", Context.MODE_PRIVATE);
            editor = prefs.edit();
        }
    }

    public static boolean savePreference(String key, boolean value) {
        if (editor != null) {
            editor.putBoolean(key, value);
            editor.apply();
            return true;
        }
        return false;
    }

    public static boolean savePreference(String key, String value) {
        if (editor != null) {
            editor.putString(key, value);
            editor.apply();
            return true;
        }
        return false;
    }

    public static boolean savePreference(String key, int value) {
        if (editor != null) {
            editor.putInt(key, value);
            editor.apply();
            return true;
        }
        return false;
    }

    public static boolean getPreferenceBoolean(String key) {
        if (prefs != null) {
            return prefs.getBoolean(key, false);
        }
        return false;
    }

    public static String getPreferenceString(String key) {
        if (prefs != null) {
            return prefs.getString(key, "");
        }
        return "";
    }

    public static int getPreferenceInt(String key) {
        if (prefs != null) {
            return prefs.getInt(key, 0);
        }
        return 0;
    }
}

