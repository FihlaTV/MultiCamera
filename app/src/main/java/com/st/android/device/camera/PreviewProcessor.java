package com.st.android.device.camera;

import android.hardware.Camera;

/**
 * Created by SeungTaek.Lim on 16. 7. 21..
 */
public abstract class PreviewProcessor implements Runnable {
    protected final Object mMutex = new Object();

    protected int mPreviewWidth;
    protected int mPreviewHeight;
    protected int mFrameRotation;

    protected ProcessListener mProcessListener;

    public void setPreviewSize(int width, int height) {
        synchronized (mMutex) {
            mPreviewWidth = width;
            mPreviewHeight = height;
        }
    }

    public void setRotation(int rotation) {
        synchronized (mMutex) {
            mFrameRotation = rotation;
        }
    }

    public void setListener(ProcessListener listener) {
        mProcessListener = listener;
    }

    public abstract void processor(byte[] data, Camera camera);


    public static interface ProcessListener {
        public void onCompleted(byte[] data);
    }
}
