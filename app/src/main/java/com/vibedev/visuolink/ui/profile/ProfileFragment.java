package com.vibedev.visuolink.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.vibedev.visuolink.BuildConfig;
import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;
import com.vibedev.visuolink.data_model.Authentication;
import com.vibedev.visuolink.databinding.FragmentProfileBinding;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ImageView profileImage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Authentication.init(requireContext());

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView[] textViews = {root.findViewById(R.id.username), root.findViewById(R.id.full_name), root.findViewById(R.id.email), root.findViewById(R.id.phone), root.findViewById(R.id.footer)};

        if (!Authentication.isLoggedIn()) {
            root.findViewById(R.id.profile_container).setVisibility(View.GONE);
            root.findViewById(R.id.Login_container).setVisibility(View.VISIBLE);
        }

        root.findViewById(R.id.loginBtn).setOnClickListener(v -> {
            TextInputEditText username = root.findViewById(R.id.username_input);
            TextInputEditText password = root.findViewById(R.id.password_input);

            new Thread(() -> {
                try {
                    boolean success = Authentication.login(Objects.requireNonNull(username.getText()).toString().trim(),
                            Objects.requireNonNull(password.getText()).toString().trim());

                    requireActivity().runOnUiThread(() -> {
                        if (success && Authentication.isLoggedIn()) {
                            root.findViewById(R.id.profile_container).setVisibility(View.VISIBLE);
                            root.findViewById(R.id.Login_container).setVisibility(View.GONE);

                            textViews[0].setText(Authentication.getUsername());
                            textViews[1].setText(Authentication.getName());
                            textViews[2].setText(Authentication.getEmail());
                            textViews[3].setText(Authentication.getPhoneNumber());

                        } else {
                            HelperClass.showToast(requireContext(), "Login failed");
                        }
                    });

                } catch (Exception e) {
                    HelperClass.showToast(requireContext(), "Server down - please try again later");
                }
            }).start();

            if (Authentication.isLoggedIn()) {
                root.findViewById(R.id.profile_container).setVisibility(View.VISIBLE);
                root.findViewById(R.id.Login_container).setVisibility(View.GONE);
                textViews[0].setText(Authentication.getUsername());
            }
        });

        profileImage = root.findViewById(R.id.profileImage);
        if (!Authentication.getImageURI().isEmpty()) {
            Authentication.setImage(Authentication.getImageURI(), profileImage);
        }

        textViews[0].setText(Authentication.getUsername());
        textViews[1].setText(Authentication.getName());
        textViews[2].setText(Authentication.getEmail());
        textViews[3].setText(Authentication.getPhoneNumber());

        root.findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            Authentication.logout();
            root.findViewById(R.id.profile_container).setVisibility(View.GONE);
            root.findViewById(R.id.Login_container).setVisibility(View.VISIBLE);
        });

        textViews[4].setText(getAppVersion());

        return root;
    }

    private String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}