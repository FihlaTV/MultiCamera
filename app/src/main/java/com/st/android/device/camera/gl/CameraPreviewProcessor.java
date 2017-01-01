package com.st.android.device.camera.gl;

import android.hardware.Camera;

import com.st.android.device.camera.ImageMap;
import com.st.android.device.camera.PreviewProcessor;

/**
 * Created by lsit on 2016. 12. 12..
 */

public class CameraPreviewProcessor extends PreviewProcessor {
    public CameraPreviewProcessor() {
    }

    @Override
    public void processor(byte[] data) {
        ImageProcessor.setPreviewImage(data, mPreviewWidth, mPreviewHeight);
    }
}
