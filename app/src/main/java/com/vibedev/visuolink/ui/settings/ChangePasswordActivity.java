package com.vibedev.visuolink.ui.settings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;
import com.vibedev.visuolink.data_model.ApiMonitor;
import com.vibedev.visuolink.data_model.Authentication;
import com.vibedev.visuolink.databinding.FragmentChangePasswordBinding;

import org.json.JSONException;
import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private FragmentChangePasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = FragmentChangePasswordBinding.inflate(getLayoutInflater());
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
            getSupportActionBar().setTitle("Change Password");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button btn = findViewById(R.id.Btn);
        TextInputEditText[] inputField = {findViewById(R.id.old_password), findViewById(R.id.new_password), findViewById(R.id.confirm_password)};

        btn.setOnClickListener(V -> {
            if (!ApiMonitor.isApiUp()){
                HelperClass.showToast(this, "API down - please try again later");
                return;
            }

            if (btn.getText().equals(getString(R.string.edit))) {
                btn.setText(R.string.submit);
                for (TextInputEditText field : inputField) {
                    field.setEnabled(true);
                }
            } else {

                String[] data = {Objects.requireNonNull(inputField[0].getText()).toString(),
                        Objects.requireNonNull(inputField[1].getText()).toString(),
                        Objects.requireNonNull(inputField[2].getText()).toString() };

                if (data[0].isEmpty() || data[1].isEmpty() || data[2].isEmpty()) {
                    HelperClass.showToast(this, "Please enter all fields");
                    return;
                }

                if (!data[1].equals(data[2])) {
                    HelperClass.showToast(this, "New password and confirm password does not match");
                    return;
                }

                new Thread(() ->{
                    try {
                        boolean success = Authentication.updatePassword(Objects.requireNonNull(inputField[0].getText()).toString(),
                                Objects.requireNonNull(inputField[1].getText()).toString());

                        this.runOnUiThread(() -> {
                            if (success) {
                                btn.setText(R.string.edit);
                                for (TextInputEditText field : inputField) {
                                    field.setEnabled(false);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        HelperClass.showToast(this, "API down - please try again later");
                    }
                }).start();
            }
        });

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
