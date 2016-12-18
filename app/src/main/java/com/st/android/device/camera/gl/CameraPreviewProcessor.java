package com.st.android.device.camera.gl;

import android.hardware.Camera;

import com.st.android.device.camera.ImageMap;
import com.st.android.device.camera.PreviewProcessor;

/**
 * Created by lsit on 2016. 12. 12..
 */

public class CameraPreviewProcessor extends PreviewProcessor {
    private byte[] mPreviewData;

    public CameraPreviewProcessor() {
    }

    @Override
    public void processor(byte[] data, Camera camera) {
        mPreviewData = data;
        run();
    }

    @Override
    public void run() {
        if (mProcessListener != null) {
            mProcessListener.onCompleted(mPreviewData);
        }
    }
}
