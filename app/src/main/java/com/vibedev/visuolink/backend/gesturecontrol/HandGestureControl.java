//package com.vibedev.visuolink.backend.gesturecontrol;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.ImageFormat;
//import android.media.Image;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.camera.core.ImageProxy;
//
//import com.google.mediapipe.components.FrameProcessor;
//import com.google.mediapipe.framework.AndroidAssetUtil;
//import com.google.mediapipe.framework.Packet;
//import com.google.mediapipe.framework.PacketGetter;
//
//import java.nio.ByteBuffer;
//import java.util.List;
//
///**
// * Connects CameraX frames to MediaPipe Hands graph and extracts index/middle fingertip landmarks.
// *
// * Usage:
// * - Call initialize(context, eglContext) once.
// * - Call processImageProxy(imageProxy) for every camera frame (from CameraManager).
// * - Call close() on shutdown.
// */
//public class HandGestureDetector {
//
//    private static final String TAG = "HandGestureDetector";
//    private FrameProcessor processor;
//
//    public void initialize(Context context, long eglContext) {
//        AndroidAssetUtil.initializeNativeAssetManager(context);
//
//        String graphName = "mediapipe/graphs/hand_tracking/hand_tracking_desktop_live.pbtxt";
//        processor = new FrameProcessor(
//                context,
//                eglContext,
//                graphName,
//                "input_video",
//                "output_video"
//        );
//
//        processor.addPacketCallback("hand_landmarks", packet -> {
//            List<com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList> handLandmarks =
//                    PacketGetter.getProtoVector(packet, com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList.parser());
//
//            for (com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList landmarkList : handLandmarks) {
//                if (landmarkList.getLandmarkCount() >= 13) {
//                    // Index finger tip: landmark[8]
//                    float indexX = landmarkList.getLandmark(8).getX();
//                    float indexY = landmarkList.getLandmark(8).getY();
//                    float indexZ = landmarkList.getLandmark(8).getZ();
//
//                    // Middle finger tip: landmark[12]
//                    float middleX = landmarkList.getLandmark(12).getX();
//                    float middleY = landmarkList.getLandmark(12).getY();
//                    float middleZ = landmarkList.getLandmark(12).getZ();
//
//                    Log.i(TAG, "Index tip: (" + indexX + ", " + indexY + ", " + indexZ + ")");
//                    Log.i(TAG, "Middle tip: (" + middleX + ", " + middleY + ", " + middleZ + ")");
//                    // ... you can add your gesture logic here ...
//                }
//            }
//        });
//    }
//
//    /**
//     * Converts ImageProxy (CameraX) to Bitmap for MediaPipe Hands (CPU input).
//     * Alternative: You can adapt for GPU/GL input if needed, but this works for CPU graph.
//     */
//    public void processImageProxy(@NonNull ImageProxy imageProxy) {
//        Bitmap bitmap = imageProxyToBitmap(imageProxy);
//        if (bitmap != null && processor != null) {
//            processor.getVideoInput().putFrame(convertBitmapToTexture(bitmap));
//        }
//        imageProxy.close(); // Always close after processing
//    }
//
//    /**
//     * Converts ImageProxy to Bitmap.
//     * (Works for YUV_420_888, you may optimize this method as needed.)
//     */
//    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
//        Image image = imageProxy.getImage();
//        if (image == null || image.getFormat() != ImageFormat.YUV_420_888) {
//            return null;
//        }
//        // You need a robust YUV-to-RGB conversion; use ScriptIntrinsicYuvToRGB or third-party util here
//        // For brevity, you may use any Android library or provide your existing conversion utility
//        // Example:
//        // return YuvToRgbConverter.convert(context, image); // Implement this!
//        return null; // Place actual conversion code here
//    }
//
//    /**
//     * Converts Bitmap to MediaPipe texture frame.
//     * Fill this in using EGL/OpenGL if using GPU.
//     * For CPU graph, can use bitmap directly or send as pixel buffer.
//     */
//    private Object convertBitmapToTexture(Bitmap bitmap) {
//        // This would be an implementation specific to your GL context and MediaPipe version
//        // If using CPU-only, processor may accept bitmap directly
//        return bitmap;
//    }
//
//    public void close() {
//        if (processor != null) {
//            processor.close();
//            processor = null;
//        }
//    }
//}
