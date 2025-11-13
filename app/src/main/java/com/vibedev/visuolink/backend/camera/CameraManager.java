package com.vibedev.visuolink.backend.camera;

import android.content.Context;
import android.util.Log;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

/**
 * CameraManager handles CameraX lifecycle and frame acquisition,
 * and delivers ImageProxy frames asynchronously to a registered listener.
 */
public class CameraManager {

    private static final String TAG = "CameraManager";

    public interface FrameListener {
        /**
         * Called on main thread when a new camera frame is available.
         * Caller is responsible for closing ImageProxy when done or forwarding to downstream.
         *
         * @param imageProxy The current camera frame.
         */
        void onFrame(ImageProxy imageProxy);
    }

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private ProcessCameraProvider cameraProvider;
    private ImageAnalysis imageAnalysis;
    private FrameListener frameListener;

    private boolean isCameraRunning = false;

    public CameraManager(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context.getApplicationContext();
        this.lifecycleOwner = lifecycleOwner;
    }

    /**
     * Start camera and deliver frames to given listener asynchronously on main thread.
     *
     * @param listener Listener to receive ImageProxy frames.
     */
    public void startCamera(FrameListener listener) {
        if (isCameraRunning) {
            Log.d(TAG, "Camera already running.");
            return;
        }

        this.frameListener = listener;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                if (cameraProvider == null) {
                    Log.e(TAG, "Unable to get ProcessCameraProvider");
                    return;
                }

                cameraProvider.unbindAll();

                imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), imageProxy -> {
                    if (frameListener != null) {
                        try {
                            frameListener.onFrame(imageProxy);
                        } catch (Exception e) {
                            Log.e(TAG, "Error in frame listener", e);
                            imageProxy.close();
                        }
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis);

                isCameraRunning = true;
                Log.d(TAG, "Camera started.");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    /**
     * Stop the camera and release resources.
     */
    public void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            isCameraRunning = false;
            Log.d(TAG, "Camera stopped.");
        }
    }
}
