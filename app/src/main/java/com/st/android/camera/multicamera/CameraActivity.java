package com.st.android.camera.multicamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.st.android.device.camera.CameraSource;
import com.st.android.device.camera.CameraSourcePreview;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private static final String TAG = CameraActivity.class.getSimpleName();

    @BindView(R.id.camera_preview) protected CameraSourcePreview mCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ButterKnife.bind(this);
        checkCameraPermission();
    }

    private void checkCameraPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (PackageManager.PERMISSION_GRANTED == permissionCheck) {
            initCamera();

            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }

    }

    private void initCamera() {
        Log.d(TAG, "initCamera");

        CameraSource cameraSource = new CameraSource.Builder(this)
                .setCameraId(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .build();

        try {
            mCameraPreview.start(cameraSource);
        } catch (IOException e) {
            Log.e(TAG, "camera start err : " + e.toString(), e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (MY_PERMISSIONS_REQUEST_CAMERA == requestCode) {
            initCamera();
        }
    }
}
