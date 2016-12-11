package com.st.android.device.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CameraSource {
    private static final String TAG = CameraSource.class.getSimpleName();

    public static final int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;
    public static final int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;

    private Context mContext;
    private final Object mMutex;
    private Camera mCamera;
    private int mCameraId;
    private int mFrameRotation;
    private Size mPreviewSize;
    private float mFps;
    private int mWidth;
    private int mHeight;
    private boolean mAvailableTextureView;

    private AutoFocusHandler mAutoFocusHendler;

    private CameraPreviewHandler mPreviewProcessor;
    private boolean mIsStartPreview = false;

    public void release() {
        synchronized(mMutex) {
            stop();

            if (mPreviewProcessor != null) {
                mPreviewProcessor.release();
            }
        }
    }

    public CameraSource start(final SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            return this;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mMutex) {
                    try {
                        mCamera = openCamera();

                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();

                        if (mPreviewProcessor != null) {
                            mPreviewProcessor.start(mCamera, mPreviewSize, mFrameRotation);
                        }

                        mAvailableTextureView = false;
                        mIsStartPreview = true;
                    } catch (Exception e) {
                    }
                }
            }
        }).start();
        return this;
    }

    @TargetApi(11)
    public CameraSource start(final SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            return this;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized(mMutex) {
                    try {
                        mCamera = openCamera();

                        mCamera.setPreviewTexture(surfaceTexture);
                        mCamera.startPreview();

                        if (mPreviewProcessor != null) {
                            mPreviewProcessor.start(mCamera, mPreviewSize, mFrameRotation);
                        }

                        mAvailableTextureView = true;
                        mIsStartPreview = true;
                    } catch (Exception e) {
                    }
                }
            }
        }).start();

        return this;
    }

    public void stop() {
        synchronized(mMutex) {
            if (mPreviewProcessor != null) {
                mPreviewProcessor.stop();
            }

            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallbackWithBuffer(null);

                try {
                    if (mAvailableTextureView) {
                        mCamera.setPreviewTexture(null);
                    } else {
                        mCamera.setPreviewDisplay(null);
                    }
                } catch (Exception e) {
                    Log.e("CameraSource", "Failed to clear camera preview: " + e);
                }

                mIsStartPreview = false;
                mCamera.release();
                mCamera = null;
            }

        }
    }

    public boolean isStartePreview() {
        return mIsStartPreview;
    }

    public final Camera getCamera() {
        return mCamera;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public void takePicture(CameraSource.ShutterCallback shutter, CameraSource.PictureCallback jpeg) {
        synchronized(mMutex) {
            if (mCamera == null) {
                return;
            }

            CameraShutterCallback cameraShutter = new CameraShutterCallback();
            cameraShutter.mCallback = shutter;

            CameraPictureCallback cameraPicture = new CameraPictureCallback();
            cameraPicture.mCallback = jpeg;

            mCamera.takePicture(cameraShutter, null, null, cameraPicture);
        }
    }

    private CameraSource() {
        mMutex = new Object();
        mCameraId = CameraInfo.CAMERA_FACING_BACK;
        mFps = 30.0F;
        mWidth = 800;
        mHeight = 600;
    }

    private Camera openCamera() throws RuntimeException {
//        int cameraId = findCameraId(mCameraFacing);
        int cameraId = mCameraId;
        Camera camera;

        if (cameraId == -1) {
            throw new RuntimeException("Could not find requested camera(" + mCameraId + ")" + ", getNumberOfCameras() = " + Camera.getNumberOfCameras());
        }

        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            throw new RuntimeException("camera open(" + cameraId + ") error : " + e.toString());
        }

        CameraSizeInfo cameraSize = getCameraSize(camera, mWidth, mHeight);
        if (cameraSize == null) {
            throw new RuntimeException("Could not find suitable preview size.");
        } else {
            Size pictureSize = cameraSize.getPictureSize();
            mPreviewSize = cameraSize.getPreviewSize();

            int[] fpsRangeList = getFpsRangeList(camera, mFps);
            if (fpsRangeList == null) {
                throw new RuntimeException("Could not find suitable preview frames per second range.");
            } else {
                Parameters parameters = camera.getParameters();
                parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
                parameters.setPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                parameters.setPreviewFpsRange(fpsRangeList[0], fpsRangeList[1]);
                parameters.setPreviewFormat(ImageFormat.NV21);

                settingParameter(camera, parameters, cameraId);
                camera.setParameters(parameters);

                return camera;
            }
        }
    }

    private static CameraSizeInfo getCameraSize(Camera camera, int width, int height) {
        List<CameraSizeInfo> camearSizeInfoList = getSupportedCameraSizeList(camera);
        CameraSizeInfo cameraSizeInfo = null;
        int minValue = Integer.MAX_VALUE;

        for (CameraSizeInfo cameraSize : camearSizeInfoList) {
            Size previewSize = cameraSize.getPreviewSize();
            int value = Math.abs(previewSize.getWidth() - width) + Math.abs(previewSize.getHeight() - height);

            if (value < minValue) {
                cameraSizeInfo = cameraSize;
                minValue = value;
            }
        }

        return cameraSizeInfo;
    }

    private static List<CameraSizeInfo> getSupportedCameraSizeList(Camera camera) {
        Parameters cameraParameters = camera.getParameters();
        List previewSizeList = cameraParameters.getSupportedPreviewSizes();
        List PictureSizeList = cameraParameters.getSupportedPictureSizes();
        ArrayList cameraSizeList = new ArrayList();
        Iterator previewSizeIterator = previewSizeList.iterator();

        while (true) {
            Camera.Size previewSize;
            while (previewSizeIterator.hasNext()) {
                previewSize = (Camera.Size)previewSizeIterator.next();
                float previewRatio = (float) previewSize.width / (float) previewSize.height;

                for(int index = PictureSizeList.size() - 1; index >= 0; --index) {
                    Camera.Size pictureSize = (Camera.Size) PictureSizeList.get(index);
                    float pictureRatio = (float) pictureSize.width / (float) pictureSize.height;

                    if(Math.abs(previewRatio - pictureRatio) < 0.01F) {
                        cameraSizeList.add(new CameraSizeInfo(previewSize, pictureSize));
                        break;
                    }
                }
            }

            if (cameraSizeList.size() == 0) {
                Log.w("CameraSource", "No preview sizes have a corresponding same-aspect-ratio picture size");
                previewSizeIterator = previewSizeList.iterator();

                while (previewSizeIterator.hasNext()) {
                    previewSize = (Camera.Size)previewSizeIterator.next();
                    cameraSizeList.add(new CameraSizeInfo(previewSize, null));
                }
            }

            return cameraSizeList;
        }
    }

    private int[] getFpsRangeList(Camera camera, float fps) {
        int requestFps = (int)(fps * 1000.0F);
        int[] previewFpsRagne = null;
        int min = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();

        for (int[] fpsRange : previewFpsRangeList) {
            int minFps = requestFps - fpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int maxFps = requestFps - fpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int approximateFps = Math.abs(minFps) + Math.abs(maxFps);

            if(approximateFps < min) {
                previewFpsRagne = fpsRange;
                min = approximateFps;
            }
        }

        return previewFpsRagne;
    }

    private void settingParameter(Camera camera, Parameters cameraParameters, int cameraId) {
        WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        short degree = 0;
        int deviceRotation = windowManager.getDefaultDisplay().getRotation();
        switch(deviceRotation) {
            case 0:
                degree = 0;
                break;
            case 1:
                degree = 90;
                break;
            case 2:
                degree = 180;
                break;
            case 3:
                degree = 270;
                break;
            default:
                Log.e("CameraSource", "Bad rotation value: " + deviceRotation);
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation;
        int orientation;

        if(cameraInfo.facing == CameraSource.CAMERA_FACING_FRONT) {
            rotation = (cameraInfo.orientation + degree) % 360;
            orientation = (360 - rotation) % 360;
        } else {
            rotation = (cameraInfo.orientation - degree + 360) % 360;
            orientation = rotation;
        }

        mFrameRotation = rotation / 90;
        camera.setDisplayOrientation(orientation);
        cameraParameters.setRotation(rotation);
    }

    private static class CameraSizeInfo {
        private Size mPreviewSize;
        private Size mPictureSize;

        public CameraSizeInfo(android.hardware.Camera.Size previewSize, android.hardware.Camera.Size pictureSize) {
            mPreviewSize = new Size(previewSize.width, previewSize.height);
            mPictureSize = new Size(pictureSize.width, pictureSize.height);
        }

        public Size getPreviewSize() {
            return mPreviewSize;
        }

        public Size getPictureSize() {
            return mPictureSize;
        }
    }

    private class CameraPictureCallback implements android.hardware.Camera.PictureCallback {
        private CameraSource.PictureCallback mCallback;

        private CameraPictureCallback() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            if(mCallback != null) {
                mCallback.onPictureTaken(data);
            }

            synchronized(CameraSource.this.mMutex) {
                if(CameraSource.this.mCamera != null) {
                    CameraSource.this.mCamera.startPreview();
                }
            }
        }
    }

    private class CameraShutterCallback implements android.hardware.Camera.ShutterCallback {
        private CameraSource.ShutterCallback mCallback;

        private CameraShutterCallback() {
        }

        public void onShutter() {
            if(mCallback != null) {
                mCallback.onShutter();
            }
        }
    }

    public interface PictureCallback {
        void onPictureTaken(byte[] data);
    }

    public interface ShutterCallback {
        void onShutter();
    }

    public static class Builder {
        private CameraSource mCameraSource = new CameraSource();
        private boolean mEnableAutoFocus = false;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("No context supplied.");

            } else {
                mCameraSource.mContext = context;
            }
        }

        public CameraSource.Builder setRequestedFps(float fps) {
            if (fps <= 0.0F) {
                throw new IllegalArgumentException("Invalid fps: " + fps);
            } else {
                mCameraSource.mFps = fps;
                return this;
            }
        }

        public CameraSource.Builder setRequestedPreviewSize(int width, int height) {
            if (width > 0 && width <= 1000000 && height > 0 && height <= 1000000) {
                mCameraSource.mWidth = width;
                mCameraSource.mHeight = height;
                return this;
            } else {
                throw new IllegalArgumentException("Invalid preview size: " + width + "x" + height);
            }
        }

        public CameraSource.Builder setPreviewHandler(CameraPreviewHandler processor) {
            mCameraSource.mPreviewProcessor = processor;
            return this;
        }

        public CameraSource.Builder enableAutoFocus(boolean isAutoFocus) {
            mEnableAutoFocus = isAutoFocus;
            return this;
        }

        public CameraSource.Builder setCameraId(int cameraId) {
            if (cameraId != CameraInfo.CAMERA_FACING_BACK && cameraId != CameraInfo.CAMERA_FACING_FRONT) {
                throw new IllegalArgumentException("setCameraId() >>> Invalid camera: " + cameraId);
            }

            mCameraSource.mCameraId = cameraId;

            if (mEnableAutoFocus) {
                mCameraSource.mAutoFocusHendler = new AutoFocusHandler();
            }
            return this;
        }

        public static int findCameraId(int facing) {
            CameraInfo cameraInfo = new CameraInfo();

            for (int index = 0; index < Camera.getNumberOfCameras(); ++index) {
                Camera.getCameraInfo(index, cameraInfo);
                if (cameraInfo.facing == facing) {
                    return index;
                }
            }

            // 찾고자 하는 카메라가 존재하지 않으면 0번째 (기본) 카메라가 open 되도록 처리.
            return (Camera.getNumberOfCameras() > 0) ? 0 : -1;
        }

        public CameraSource build() {
            return mCameraSource;
        }
    }
}
