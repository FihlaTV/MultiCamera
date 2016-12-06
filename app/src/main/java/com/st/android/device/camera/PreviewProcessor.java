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

    public abstract void start();
    public abstract void stop();
//    public abstract void resume();
//    public abstract void pause();

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

    public abstract void setImageMap(ImageMap buffer);

    public abstract void addFrame(byte[] frame, Camera camera);
}
