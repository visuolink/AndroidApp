package com.vibedev.visuolink.ui.settings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;
import com.vibedev.visuolink.backend.PermissionCheck;
import com.vibedev.visuolink.data_model.Authentication;
import com.vibedev.visuolink.data_model.Preferences;
import com.vibedev.visuolink.databinding.FragmentDashboardBinding;

/**
 * SettingsFragment provides the UI for user settings including gesture toggles,
 * dark mode, notifications, and font selection. It allows the user to save preferences
 * which persist across app sessions using the Preferences helper class.
 */
public class SettingsFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private String[] fontFamily;
    private Spinner changeFont;
    private SwitchCompat eyeGestureSwitch;
    private SwitchCompat gestureControlSwitch;
    private SwitchCompat appLauncherSwitch;
    private SwitchCompat switchDarkMode;
    private SwitchCompat switchNotifications;
    private PermissionCheck permission;

    /**
     * Inflates the settings fragment layout, initializes UI components,
     * loads font options, and sets up event listeners for saving preferences
     * and navigating to profile management activities.
     *
     * @param inflater LayoutInflater object to inflate views
     * @param container ViewGroup parent container
     * @param savedInstanceState Bundle containing state data
     * @return inflated View for this fragment
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        changeFont = root.findViewById(R.id.changeFont);
        eyeGestureSwitch = root.findViewById(R.id.eye_gesture_switch);
        gestureControlSwitch = root.findViewById(R.id.gesture_control_switch);
        appLauncherSwitch = root.findViewById(R.id.app_launcher_switch);
        switchDarkMode = root.findViewById(R.id.switchDarkMode);
        switchNotifications = root.findViewById(R.id.switchNotifications);

        permission = new PermissionCheck(getActivity());
        Preferences.init(getContext());
        Authentication.init(getContext());

        fontFamily = getResources().getStringArray(R.array.fontFamily);
        ArrayAdapter<String> fontAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fontFamily);
        fontAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        changeFont.setPopupBackgroundResource(R.color.backgroundImgColor);
        changeFont.setAdapter(fontAdapter);

        setPreferences();

        if (appLauncherSwitch.isChecked()) {
            checkAccessibilityPermission();
        }

        appLauncherSwitch.setOnClickListener(v -> {
            if (appLauncherSwitch.isChecked()) {
                checkAccessibilityPermission();
                Preferences.setAppLauncherSwitchStatus(appLauncherSwitch.isChecked());
            }
        });

        root.findViewById(R.id.save).setOnClickListener(v -> {
            Preferences.setEyeGestureSwitchStatus(eyeGestureSwitch.isChecked());
            Preferences.setGestureControlSwitchStatus(gestureControlSwitch.isChecked());
            Preferences.setAppLauncherSwitchStatus(appLauncherSwitch.isChecked());

            HelperClass.showToast(requireContext(), getString(R.string.changesApplied));
        });

        switchNotifications.setOnClickListener(V -> {
            if (switchNotifications.isChecked()){
                checkNotificationPermission();
                Preferences.setNotificationSwitchStatus(switchNotifications.isChecked());
            }
        });

        root.findViewById(R.id.manage_profile).setOnClickListener(v -> {
            if (!Authentication.isLoggedIn()) {
                HelperClass.showToast(requireContext(), getString(R.string.accountWarning));
                return;
            }
            startActivity(new Intent(getActivity(), ManageProfileActivity.class));
        });

        root.findViewById(R.id.password_change).setOnClickListener(v -> {
            if (!Authentication.isLoggedIn()) {
                HelperClass.showToast(requireContext(), getString(R.string.unauthorizedAccess));
                return;
            }
            startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
        });

        root.findViewById(R.id.saveButton).setOnClickListener(v -> {
            Preferences.setDarkModeSwitchStatus(switchDarkMode.isChecked());
            Preferences.setNotificationSwitchStatus(switchNotifications.isChecked());
            Preferences.setFontFamilyName(changeFont.getSelectedItem().toString());

            HelperClass.showToast(requireContext(), getString(R.string.changesApplied));
        });

        root.findViewById(R.id.resetButton).setOnClickListener(v -> {
            Preferences.setFontFamilyName(getString(R.string.defaultFont));

            new Handler(Looper.getMainLooper()).post(this::setPreferences);
            HelperClass.showToast(requireContext(), getString(R.string.changesResets));
        });

        return root;
    }

    public int indexOf(String font) {
        for (int i = 0; i < fontFamily.length; i++) {
            if (fontFamily[i].equals(font)) {
                return i;
            }
        }
        return 0;
    }

    private void setPreferences() {
        eyeGestureSwitch.setChecked(Preferences.getEyeGestureSwitchStatus());
        gestureControlSwitch.setChecked(Preferences.getGestureControlSwitchStatus());
        appLauncherSwitch.setChecked(Preferences.getAppLauncherSwitchStatus());
        changeFont.setSelection(indexOf(Preferences.getFontFamilyName()));
        switchNotifications.setChecked(Preferences.getNotificationSwitchStatus());
        switchDarkMode.setChecked(Preferences.getDarkModeSwitchStatus());
    }

    private void checkAccessibilityPermission(){
        permission.checkAndRequestPermissions();
    }

    private void checkNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission.getNotificationPermissions();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        setPreferences();
        if (appLauncherSwitch.isChecked()) {
            checkAccessibilityPermission();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
