package com.vibedev.visuolink.data_model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.vibedev.visuolink.data_model.VisuoLinkClient;

import org.json.JSONException;

public class Authentication {
    static VisuoLinkClient client = new VisuoLinkClient();

    public static void init(Context context){
        AccountDetails.init(context);
    }

    public static boolean isLoggedIn(){
        return getLoginStatus();
    }

    public static boolean login(String username, String password) throws JSONException {
        Log.d("Authentication", "login() called with " + username);
        Integer user_id = client.doLogin(username, password);
        if (user_id == null || user_id == -1){
            return false;
        }

        Map<String, String> user_details = client.getUserDetail(user_id);
        if (user_details == null){
            return false;
        }

        setProfileId(user_id);
        setProfileDetails(user_details.get("username"), user_details.get("name"),
                user_details.get("email"), user_details.get("phone"));
        setLoginStatus(true);

        return true;
    }

    public static void logout() {
        setLoginStatus(false);
        setProfileDetails("", "", "", "");
        setImageURI("");
    }

    public static boolean updatePassword(String password, String new_password) throws JSONException {
        return client.changePassword(getUsername(), password, new_password);
    }

    public static List<String> getUsernames() throws JSONException {
        return client.getUsernames();
    }

    public static void updateProfileDetail(){
        if (getProfileId() == 0) return;
        Map<String, String> data = client.getUserDetail(getProfileId());
        if (data == null) return;
        setProfileDetails(data.get("username"), data.get("name"), data.get("email"), data.get("phone"));
    }

    public static String getUsername(){
        return AccountDetails.getPreferenceString("username");
    }

    public static String getName(){
        return AccountDetails.getPreferenceString("name");
    }

    public static String getEmail(){
        return AccountDetails.getPreferenceString("email");
    }

    public static String getPhoneNumber(){
        return AccountDetails.getPreferenceString("phoneNumber");
    }

    public static String getImageURI(){
        return AccountDetails.getPreferenceString("imageURI");
    }

    public static boolean getLoginStatus(){
        return AccountDetails.getPreferenceBoolean("loginStatus");
    }

    public static boolean setLoginStatus(boolean status){
        return AccountDetails.savePreference("loginStatus", status);
    }

    public static boolean setUsername(String username){
        return AccountDetails.savePreference("username", username);
    }

    public static boolean setName(String name){
        return AccountDetails.savePreference("name", name);
    }

    public static boolean setEmail(String email){
        return AccountDetails.savePreference("email", email);
    }

    public static boolean setPhoneNumber(String phoneNumber){
        return AccountDetails.savePreference("phoneNumber", phoneNumber);
    }

    public static void setProfileId(int id){
        AccountDetails.savePreference("profileId", id);
    }

    public static int getProfileId(){
        return AccountDetails.getPreferenceInt("profileId");
    }


    public static void setProfileDetails(String username, String name, String email, String phoneNumber){
        setUsername(username);
        setName(name);
        setEmail(email);
        setPhoneNumber(phoneNumber);
    }

    public static boolean updateInformation(String username, String name, String email, String phone, String password) throws JSONException {
        Map<String, String> updated_details = client.modifyProfile(username, name, email, phone, password, getUsername());
        if (updated_details == null){
            return false;
        }

        setProfileDetails(updated_details.get("username"), updated_details.get("name"),
                updated_details.get("email"), updated_details.get("phone"));
        return true;
    }

    public static boolean setImageURI(String imageURI) {
        return AccountDetails.savePreference("imageURI", imageURI);
    }

    public static void setImage(String imagePath, ImageView imageView){
        System.out.println("path:: " + imagePath);
        File file = new File(imagePath);
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Log.e("ImageLoad", "Bitmap decoding failed for path: " + file.getAbsolutePath());
            }
        }
    }

    public static void saveImageToAppStorage(Context context, Uri imageUri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return;

            File dir = context.getFilesDir();
            File file = new File(dir, fileName + ".jpg");

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            System.out.println("path " + file.getAbsolutePath());
            setImageURI(file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
