package com.vibedev.visuolink.data_model;

import android.content.Context;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class ApiMonitor{
    private static volatile boolean API_UP = false;


    public static void checkApiInBackground(String url, int interval, int maxRetries, Context context) {
        for (int i = 1; i <= maxRetries; i++) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                int status = conn.getResponseCode();

                if (status == 200) {
                    API_UP = true;
                    Authentication.init(context);
                    Authentication.updateProfileDetail();
                    Log.d("VisuolinkAPI", "✅ API is up!");
                    return;
                } else {
                    Log.d("VisuolinkAPI", "⚠️ API responded with " + status);
                }
            } catch (Exception e) {
                Log.d("VisuolinkAPI", "❌ API Error (" + i + "/" + maxRetries + "): " + e.getMessage());
            }

            try {
                Thread.sleep(interval * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        Log.d("VisuolinkAPI", "❌ Could not connect to API after " + maxRetries + " attempts.");
    }

    public static void startApiMonitor(String url, Context context) {
        Thread thread = new Thread(() -> checkApiInBackground(url, 8, 8, context));
        thread.setDaemon(true);
        thread.start();
    }

    public static boolean isApiUp() {
        return API_UP;
    }
}

