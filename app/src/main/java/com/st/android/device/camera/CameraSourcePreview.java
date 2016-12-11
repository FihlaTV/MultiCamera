package com.st.android.device.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

public class CameraSourcePreview extends TextureView implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraSourcePreview";

    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceAvailable = false;

        setSurfaceTextureListener(this);
    }

    public void start(CameraSource cameraSource) throws IOException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null
                && mCameraSource.isStartePreview() == false) {
            startIfReady();
        }
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    private void startIfReady() {
        if (mCameraSource != null && mCameraSource.isStartePreview() == false && mSurfaceAvailable) {
            mCameraSource.start(getSurfaceTexture());
        }
    }

    public CameraSource getCameraSource() {
        return mCameraSource;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurfaceAvailable = true;
        startIfReady();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");

        mSurfaceAvailable = false;

        stop();
        release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mSurfaceAvailable = false;

        stop();
        release();

        Log.d(TAG, "onDetachedFromWindow");
    }
}
