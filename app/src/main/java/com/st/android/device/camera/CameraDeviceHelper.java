package com.st.android.device.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by SeungTaek.Lim on 16. 7. 21..
 */
public class CameraDeviceHelper {
    private static final String TAG = CameraDeviceHelper.class.getSimpleName();
    private static final int RC_HANDLE_GMS = 9001;

    public static boolean availableGooglePlayServices(Activity activity) {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS);
            dlg.show();

            return false;
        }

        return true;
    }

    public static boolean availableCamera(Context context, boolean useFacing) {
        if (context == null) {
            return false;
        }

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
            return false;
        }

        if (useFacing == false && Camera.getNumberOfCameras() > 0) {
            return true;
        }


        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int index = 0; index < Camera.getNumberOfCameras(); ++index) {
            Camera.getCameraInfo(index, cameraInfo);

            if (cameraInfo.facing == CameraSource.CAMERA_FACING_FRONT) {
                return true;
            }
        }

        return false;
    }
}
