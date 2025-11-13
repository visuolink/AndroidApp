package com.vibedev.visuolink.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.vibedev.visuolink.HelperClass;
import com.vibedev.visuolink.R;
import com.vibedev.visuolink.backend.applauncher.MainAppLauncherActivity;
import com.vibedev.visuolink.backend.eyegesture.EyeGestureService;
import com.vibedev.visuolink.backend.eyegesture.MainEyeGestureActivity;
import com.vibedev.visuolink.backend.gesturecontrol.MainGestureControlActivity;
import com.vibedev.visuolink.data_model.Preferences;
import com.vibedev.visuolink.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private final boolean isGestureControlDone = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Preferences.init(requireContext());

        root.findViewById(R.id.eyeGesture).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MainEyeGestureActivity.class)));

        root.findViewById(R.id.gestureControl).setOnClickListener(v -> {
            if (!isGestureControlDone) {
                HelperClass.showToast(requireContext(), "Available in a future version.");
                return;
            }
            startActivity(new Intent(getActivity(), MainGestureControlActivity.class));
        });

        root.findViewById(R.id.gestureControl).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MainGestureControlActivity.class)));


        root.findViewById(R.id.appLauncher).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), MainAppLauncherActivity.class)));

        Button startButton = root.findViewById(R.id.startButton);
        startButton.setText(Preferences.getEyeGestureServiceRunningStatus() ? getString(R.string.stop) : getString(R.string.start));

        startButton.setOnClickListener(V -> {
            if (startButton.getText().equals(getString(R.string.start))) {
                if (Preferences.getEyeGestureSwitchStatus()) {
                    startButton.setText(getString(R.string.stop));
                    ContextCompat.startForegroundService(requireContext(), new Intent(requireContext(), EyeGestureService.class));
                    Preferences.setEyeGestureServiceRunningStatus(true);
                    HelperClass.showToast(requireContext(), "Eye Gesture daemon Started");
                } else {
                    HelperClass.showToast(requireContext(), getString(R.string.eye_gesture_not_enabled));
                }
            } else {
                startButton.setText(getString(R.string.start));
                requireActivity().stopService(new Intent(requireContext(), EyeGestureService.class));
                Preferences.setEyeGestureServiceRunningStatus(false);
                HelperClass.showToast(requireContext(), "Eye Gesture daemon Stopped");
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
