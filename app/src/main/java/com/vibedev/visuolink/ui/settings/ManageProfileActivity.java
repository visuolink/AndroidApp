package com.vibedev.visuolink.ui.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;
import com.vibedev.visuolink.data_model.ApiMonitor;
import com.vibedev.visuolink.data_model.Authentication;
import com.vibedev.visuolink.databinding.FragmentAccountManageBinding;

import org.json.JSONException;

import java.util.Objects;

public class ManageProfileActivity extends AppCompatActivity {

    private FragmentAccountManageBinding binding;
    private TextInputEditText username;
    private TextInputEditText email;
    private TextInputEditText name;
    private TextInputEditText phoneNumber;

    private ImageView profileImage;
    private TextView editImage;
    private Button submitBtn;
    private Boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentAccountManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            getSupportActionBar().setTitle("Manage Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        profileImage = findViewById(R.id.profileImage);
        username = findViewById(R.id.edit_username);
        email = findViewById(R.id.edit_email);
        name = findViewById(R.id.edit_name);
        phoneNumber = findViewById(R.id.edit_phone_number);
        editImage = findViewById(R.id.edit_profile_pic);
        submitBtn = findViewById(R.id.submitBtn);

        Authentication.init(this);
        setData();

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(),
                        uri -> {
                            if (uri != null) {
                                Authentication.saveImageToAppStorage(this, uri, Authentication.getUsername());
                                profileImage.setImageURI(uri);
                            }
                        });

        editImage.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        submitBtn.setOnClickListener(v -> {
            if (!ApiMonitor.isApiUp()){
                HelperClass.showToast(this, "API down - please try again later");
                return;
            }

            if (submitBtn.getText() == getString(R.string.edit)){
                editImage.setEnabled(true);
                username.setEnabled(true);
                email.setEnabled(true);
                name.setEnabled(true);
                phoneNumber.setEnabled(true);
                submitBtn.setText(R.string.submit);
            } else {
                showPasswordDialog(this);
            }
        });
    }

    private void setData(){
        username.setText(Authentication.getUsername());
        email.setText(Authentication.getEmail());
        name.setText(Authentication.getName());
        phoneNumber.setText(Authentication.getPhoneNumber());
        if (!Authentication.getImageURI().isEmpty()) {
            Authentication.setImage(Authentication.getImageURI(), profileImage);
        }
    }

    private void showPasswordDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Enter your password");

        final EditText passwordInput = new EditText(context);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("Password");

        int padding = (int) (20 * context.getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(context);
        container.setPadding(padding, padding, padding, padding);
        container.addView(passwordInput);
        builder.setView(container);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String password = passwordInput.getText().toString().trim();
            if (!password.isEmpty()) {
                new Thread(() -> {
                    try {
                        success = Authentication.updateInformation(Objects.requireNonNull(username.getText()).toString(), Objects.requireNonNull(name.getText()).toString(),
                                Objects.requireNonNull(email.getText()).toString(), Objects.requireNonNull(phoneNumber.getText()).toString(), password);
                        this.runOnUiThread(() -> {
                            if (success){
                                submitBtn.setText(R.string.edit);
                                editImage.setEnabled(false);
                                username.setEnabled(false);
                                email.setEnabled(false);
                                name.setEnabled(false);
                                phoneNumber.setEnabled(false);
                                HelperClass.showToast(context, "Profile updated successfully");
                            }
                        });
                    } catch (JSONException e) {
                        HelperClass.showToast(context, "API down - please try again later");
                    }
                }).start();
            } else {
                HelperClass.showToast(context, "Password cannot be empty");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
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
        binding = null;
    }
}
