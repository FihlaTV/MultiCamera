package com.st.android.device.camera.gl;

/**
 * Created by lsit on 2016. 12. 11..
 */

public class ImageProcessor {
    public static native void test();

    static {
        System.loadLibrary("camera_preview_renderer");
    }
}
