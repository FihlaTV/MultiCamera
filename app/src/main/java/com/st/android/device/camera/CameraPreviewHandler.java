package com.st.android.device.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

import static junit.framework.Assert.assertNotNull;

public class CameraPreviewHandler implements Camera.PreviewCallback {
    private static final String TAG = CameraPreviewHandler.class.getSimpleName();

    protected final PreviewProcessor mPreviewProcessor;

    protected Camera mCamera;
    protected Size mPreviewSize;

    protected ConcurrentHashMap<byte[], ByteBuffer> mImageMap = new ConcurrentHashMap<>();
    protected PreviewThread mPreviewThread;

    public CameraPreviewHandler(PreviewProcessor processor) {
        assertNotNull(processor);

        mPreviewProcessor = processor;
    }

    public synchronized void start(Camera camera, Size previewSize, int rotation) {
        if (mPreviewThread != null) {
            return;
        }

        mPreviewSize = previewSize;
        mCamera = camera;
        mCamera.setPreviewCallbackWithBuffer(this);

        mPreviewProcessor.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        mPreviewProcessor.setRotation(rotation);

        mPreviewThread = new PreviewThread(mPreviewProcessor);
        mPreviewThread.start();

        addAllocatedBuffer(previewSize);
        addAllocatedBuffer(previewSize);
        addAllocatedBuffer(previewSize);

        Log.i(TAG, "start()");
    }

    private void addAllocatedBuffer(Size size) {
        ByteBuffer byteBuffer = allocateMemory(size);
        mImageMap.put(byteBuffer.array(), byteBuffer);

        mCamera.addCallbackBuffer(byteBuffer.array());
    }

    public synchronized void stop() {
        if (mPreviewThread == null) {
            return;
        }

        try {
            mPreviewThread.interrupt();
            mPreviewThread.join(100);
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString(), e);
        }

        mPreviewThread = null;
        mCamera = null;

        Log.i(TAG, "stop()");
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d(TAG, "PreviewHandler >>> onPreviewFrame()");

        synchronized (mPreviewThread) {
            if (mPreviewThread == null) {
                return;
            }

            mPreviewThread.put(data);
        }
    }

    private ByteBuffer allocateMemory(Size size) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long imageSize = (long) (size.getHeight() * size.getWidth() * bitsPerPixel);
        int byteSize = (int) Math.ceil((double)imageSize / 8.0D) + 1;

        return ByteBuffer.allocateDirect(byteSize);
    }

    private final class PreviewThread extends Thread {
        private final PreviewProcessor mPreviewProcessor;
        private ConcurrentLinkedQueue<byte[]> mImageQueue = new ConcurrentLinkedQueue<>();

        public PreviewThread(PreviewProcessor processor) {
            assertNotNull(processor);
            mPreviewProcessor = processor;
        }

        public void put(byte[] data) {
            mImageQueue.add(data);
            notify();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Log.i(TAG, "PreviewThread running...");
                    synchronized (this) {
                        if (getState() != State.WAITING) {
                            wait();
                        }
                    }

                    while (mImageQueue.isEmpty() == false) {
                        byte[] data = mImageQueue.poll();

                        Log.i(TAG, "image processor");
                        mPreviewProcessor.processor(data);
                        mCamera.addCallbackBuffer(data);

                    }

                } catch (InterruptedException e) {
                    Log.i(TAG, "PreviewThread >> interrupted");
                    break;
                }
            }
        }
    }
}