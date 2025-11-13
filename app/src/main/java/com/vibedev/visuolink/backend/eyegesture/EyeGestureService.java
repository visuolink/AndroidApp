package com.vibedev.visuolink.backend.eyegesture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ExperimentalGetImage;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import com.vibedev.visuolink.R;
import com.vibedev.visuolink.backend.accessibility.AppAccessibilityService;
import com.vibedev.visuolink.backend.camera.CameraManager;

import java.util.LinkedList;
import java.util.Queue;

public class EyeGestureService extends LifecycleService implements CameraManager.FrameListener {

    private static final String CHANNEL_ID = "EyeGestureChannel";

    private WindowManager windowManager;
    private View eyeCircle;
    private WindowManager.LayoutParams params;
    private Size screenSize;

    private static final int SMOOTHING_SAMPLES = 5;
    private Queue<Point> positionHistory = new LinkedList<>();

    private boolean wasBlinking = false;
    private long lastBlinkTime = 0;
    private static final long BLINK_COOLDOWN = 500;
    private static final float BLINK_THRESHOLD = 0.5f;

    private boolean wasLeftWinking = false;
    private long lastLeftWinkTime = 0;
    private boolean wasRightWinking = false;
    private long lastRightWinkTime = 0;
    private static final long WINK_COOLDOWN = 1000;
    private static final float WINK_THRESHOLD = 0.3f;

    private FaceDetector detector;
    private CameraManager cameraManager;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        getScreenSize();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()
                .build();

        detector = FaceDetection.getClient(options);

        cameraManager = new CameraManager(this, this);
        cameraManager.startCamera(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Eye Gesture Running")
                .setContentText("Detecting Eye Movement...")
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA);

        createOverlapCircle();

        return START_STICKY;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    @Override
    public void onFrame(androidx.camera.core.ImageProxy imageProxy) {
        if (imageProxy.getImage() != null) {
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees());

            detector.process(image)
                    .addOnSuccessListener(faces -> {
                        try {
                            if (faces == null || faces.isEmpty()) {
                                return;
                            }

                            Face face = faces.get(0);
                            if (face == null) {
                                return;
                            }

                            Float leftEyeCloseProbability = face.getLeftEyeOpenProbability();
                            Float rightEyeCloseProbability = face.getRightEyeOpenProbability();

                            FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                            FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);

                            if (leftEye == null || rightEye == null) {
                                return;
                            }

                            Float eulerY = face.getHeadEulerAngleY();
                            Float eulerX = face.getHeadEulerAngleX();

                            if (eulerY == null || eulerX == null) {
                                return;
                            }

                            int centerX = screenSize.getWidth() / 2;
                            int centerY = screenSize.getHeight() / 2;

                            float horizontalSensitivity = 40f;
                            float verticalSensitivity = 50f;

                            int screenX = centerX - (int)(eulerY * horizontalSensitivity);
                            int screenY = centerY - (int)(eulerX * verticalSensitivity);

                            screenX = Math.max(0, Math.min(screenX, screenSize.getWidth() - 50));
                            screenY = Math.max(0, Math.min(screenY, screenSize.getHeight() - 50));

                            Point gazePosition = new Point(screenX, screenY);
                            Point smoothedPosition = smoothPosition(gazePosition);
                            updateEyePositions(smoothedPosition);

                            if (leftEyeCloseProbability != null && rightEyeCloseProbability != null) {
                                detectBlinkAndClick(leftEyeCloseProbability, rightEyeCloseProbability, smoothedPosition);
                                detectLeftWinkAndOpenRecents(leftEyeCloseProbability, rightEyeCloseProbability);
                                detectRightWinkAndGoHome(leftEyeCloseProbability, rightEyeCloseProbability);
                            }

                        } catch (Exception e) {
                            Log.e("EyeGestureService", "Error in face processing", e);
                        }
                    })
                    .addOnFailureListener(e -> Log.e("EyeGestureService", "Detection error", e))
                    .addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
        } else {
            imageProxy.close();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Eye Gesture Service Channel",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void createOverlapCircle(){
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            eyeCircle = inflater.inflate(R.layout.floatingcircle, null);
            eyeCircle.setVisibility(View.VISIBLE);

            int layoutFlag;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
            }

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 500;
            params.y = 500;
            params.alpha = 1.0f;

            if (windowManager != null) {
                windowManager.addView(eyeCircle, params);
            }
        } catch (Exception e) {
            Log.e("EyeGestureService", "Failed to create overlay circle", e);
            eyeCircle = null;
        }
    }

    private void detectBlinkAndClick(float leftEyeOpen, float rightEyeOpen, Point clickPosition) {
        boolean isBlinking = (leftEyeOpen < BLINK_THRESHOLD && rightEyeOpen < BLINK_THRESHOLD);

        long currentTime = System.currentTimeMillis();

        if (!wasBlinking && isBlinking) {
            wasBlinking = true;
        } else if (wasBlinking && !isBlinking) {
            if (currentTime - lastBlinkTime > BLINK_COOLDOWN) {
                performClick(clickPosition);
                lastBlinkTime = currentTime;
            }
            wasBlinking = false;
        }
    }

    private void detectLeftWinkAndOpenRecents(float leftEyeOpen, float rightEyeOpen) {
        boolean isLeftWinking = (leftEyeOpen < WINK_THRESHOLD && rightEyeOpen > 0.8f);

        long currentTime = System.currentTimeMillis();

        if (!wasLeftWinking && isLeftWinking) {
            wasLeftWinking = true;
        } else if (wasLeftWinking && !isLeftWinking) {
            if (currentTime - lastLeftWinkTime > WINK_COOLDOWN) {
                openRecentApps();
                lastLeftWinkTime = currentTime;
            }
            wasLeftWinking = false;
        }
    }

    private void detectRightWinkAndGoHome(float leftEyeOpen, float rightEyeOpen) {
        boolean isRightWinking = (rightEyeOpen < WINK_THRESHOLD && leftEyeOpen > 0.8f);

        long currentTime = System.currentTimeMillis();

        if (!wasRightWinking && isRightWinking) {
            wasRightWinking = true;
            if (currentTime - lastRightWinkTime > WINK_COOLDOWN) {
                goToHome();
                lastRightWinkTime = currentTime;
            }
            wasRightWinking = false;
        }
    }

    private void performClick(Point position) {
        AppAccessibilityService service = AppAccessibilityService.getInstance();
        if (service != null) {
            service.performClickAt(position);
        } else {
            Log.e("EyeGestureService", "AccessibilityService not enabled - please enable in Settings");
        }
    }

    private void openRecentApps() {
        AppAccessibilityService service = AppAccessibilityService.getInstance();
        if (service != null) {
            service.showRecentAction();
        } else {
            Log.e("EyeGestureService", "AccessibilityService not available");
        }
    }

    private void goToHome(){
        AppAccessibilityService service = AppAccessibilityService.getInstance();
        if (service != null) {
            service.goTOHomeScreen();
        } else {
            Log.e("EyeGestureService", "AccessibilityService not available");
        }
    }

    private Point smoothPosition(Point newPosition) {
        positionHistory.offer(newPosition);

        if (positionHistory.size() > SMOOTHING_SAMPLES) {
            positionHistory.poll();
        }

        int avgX = 0, avgY = 0;
        for (Point p : positionHistory) {
            avgX += p.x;
            avgY += p.y;
        }

        int count = positionHistory.size();
        return new Point(avgX / count, avgY / count);
    }

    private void updateEyePositions(Point eyePos) {
        if (eyePos != null && eyeCircle != null && eyeCircle.isAttachedToWindow()) {
            try {
                params.x = eyePos.x;
                params.y = eyePos.y;
                windowManager.updateViewLayout(eyeCircle, params);
            } catch (IllegalArgumentException e) {
                Log.e("EyeGestureService", "Failed to update view layout", e);
            }
        }
    }

    private void getScreenSize() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                android.view.WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
                screenSize = new Size(
                        metrics.getBounds().width(),
                        metrics.getBounds().height()
                );
            } else {
                android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                screenSize = new Size(
                        displayMetrics.widthPixels,
                        displayMetrics.heightPixels
                );
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (cameraManager != null) {
            cameraManager.stopCamera();
            cameraManager = null;
        }

        if (eyeCircle != null && windowManager != null) {
            try {
                if (eyeCircle.isAttachedToWindow()) {
                    windowManager.removeView(eyeCircle);
                }
            } catch (IllegalArgumentException e) {
                Log.e("EyeGestureService", "Error removing view", e);
            } finally {
                eyeCircle = null;
            }
        }
    }
}
