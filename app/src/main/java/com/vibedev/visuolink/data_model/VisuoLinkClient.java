package com.vibedev.visuolink.data_model;

import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class VisuoLinkClient {
    private final String baseUrl;
    private final int timeout;

    public VisuoLinkClient(String baseUrl, int timeout) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.timeout = timeout * 1000;
    }

    public VisuoLinkClient() {
        this("https://visuolinkapi.onrender.com", 10);
    }

    private JSONObject request(String method, String endpoint, JSONObject payload) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Content-Type", "application/json");

            if (payload != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes());
                    os.flush();
                }
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }
                return new JSONObject(response.toString());
            } else {
                System.out.println("[Error] API responded with " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("[Error] Request failed: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public List<String> getUsernames() throws JSONException {
        JSONObject response = request("GET", "/users", null);
        if (response == null) return null;
        List<String> usernames = new ArrayList<>();
        JSONArray users = response.getJSONArray("users");
        for (int i = 0; i < users.length(); i++) {
            usernames.add(users.getJSONObject(i).getString("username"));
        }
        return usernames;
    }

    public Map<String, String> getUserDetail(int userId) {
        JSONObject response = request("GET", "/users/" + userId, null);
        if (response == null) return null;
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("username", response.optString("username"));
        userDetails.put("name", response.optString("name"));
        userDetails.put("email", response.optString("email"));
        userDetails.put("phone", response.optString("phone"));
        return userDetails;
    }

    public Integer doLogin(String username, String password) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("password", password);

        JSONObject response = request("POST", "/users/auth/login", payload);
        if (response != null) {
            return response.optInt("id", -1);
        }
        return null;
    }

    public boolean changePassword(String username, String password, String newPassword) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("password", password);
        payload.put("newPassword", newPassword);

        JSONObject response = request("PUT", "/users/cp", payload);
        return response != null;
    }

    public Map<String, String> modifyProfile(String username, String name, String email, String phone,
                                             String password, String oldUsername) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("name", name);
        payload.put("email", email);
        payload.put("phone", phone);
        payload.put("password", password);
        payload.put("oldUsername", oldUsername);

        JSONObject response = request("PUT", "/users/", payload);
        if (response != null) {
            Map<String, String> updated = new HashMap<>();
            updated.put("username", response.optString("username"));
            updated.put("name", response.optString("name"));
            updated.put("email", response.optString("email"));
            updated.put("phone", response.optString("phone"));
            return updated;
        }
        return null;
    }
}

