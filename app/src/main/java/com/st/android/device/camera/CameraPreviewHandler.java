package com.st.android.device.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CameraPreviewHandler implements Camera.PreviewCallback {
    private static final String TAG = CameraPreviewHandler.class.getSimpleName();

    private Thread mPreviewProcessorThread;
    protected final PreviewProcessor mPreviewProcessor;

    protected Camera mCamera;

    private Map<byte[], ByteBuffer> mPreviewImageMap = new HashMap();
    private PreviewImageMap mImageMap = new PreviewImageMap();

    public CameraPreviewHandler(PreviewProcessor processor) {
        mPreviewProcessor = processor;
    }

    public void start(Camera camera, Size previewSize, int rotation) {
        mCamera = camera;

        mCamera.setPreviewCallbackWithBuffer(this);

        addCallbackBuffer(previewSize);
        addCallbackBuffer(previewSize);
        addCallbackBuffer(previewSize);

        if (mPreviewProcessor == null) {
            return;
        }

        mPreviewProcessorThread = new Thread(mPreviewProcessor);
        mPreviewProcessor.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        mPreviewProcessor.setRotation(rotation);
        mPreviewProcessor.setImageMap(mImageMap);
        mPreviewProcessor.start();

        mPreviewProcessorThread.start();
    }

    public void stop() {
        if (mPreviewProcessorThread != null) {
            mPreviewProcessor.stop();

            try {
                mPreviewProcessorThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }

            mPreviewProcessorThread = null;
        }

        mCamera = null;
    }

    public void release() {
        if (mPreviewProcessor != null) {
            mPreviewProcessor.stop();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mPreviewProcessor != null) {
            mPreviewProcessor.addFrame(data, camera);
        }
    }

    protected void addCallbackBuffer(Size previewSize) {
        mCamera.addCallbackBuffer(allocateMemory(previewSize));
    }

    private byte[] allocateMemory(Size size) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long imageSize = (long) (size.getHeight() * size.getWidth() * bitsPerPixel);
        int byteSize = (int) Math.ceil((double)imageSize / 8.0D) + 1;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byteSize);
        mPreviewImageMap.put(byteBuffer.array(), byteBuffer);
        return byteBuffer.array();
    }


    private class PreviewImageMap implements ImageMap {
        @Override
        public ByteBuffer get(byte[] frame) {
            return mPreviewImageMap.get(frame);
        }

        @Override
        public void addBuffer(byte[] array) {
            if (mCamera == null) {
                return;
            }

            mCamera.addCallbackBuffer(array);
        }
    }
}