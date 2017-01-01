package com.st.android.device.camera.gl;

import android.util.Log;

/**
 * Created by lsit on 2016. 12. 11..
 */

public class ImageProcessor {
    private static final String TAG = ImageProcessor.class.getSimpleName();

    public static native void test();
    public static native void setPreviewImage(byte[] data, int width, int height);

    private static void saveBitmap(int width, int heiht) {
        Log.d(TAG, "saveBitmap");
    }

//    private static void onReadyPutDataForJNI() {
//
//        if (mLiveFilterCallbacks == null) {
//            return;
//        }
//
//        mLiveFilterCallbacks.onReadyPutData();
//    }

    static {
        System.loadLibrary("camera_preview_renderer");
    }
}
