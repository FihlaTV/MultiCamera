package com.st.android.device.camera;

import android.hardware.Camera;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SeungTaek.Lim on 16. 7. 21..
 */
public class AutoFocusHandler implements Camera.AutoFocusCallback {
    private static final String TAG = AutoFocusHandler.class.getSimpleName();
    private TimerTask mTask;
    private Timer mTimer;

    @Override
    public void onAutoFocus(boolean success, final Camera camera) {
        Log.d(TAG, "onAutoFocus");

        mTask = new TimerTask() {
            @Override
            public void run() {
                camera.autoFocus(AutoFocusHandler.this);
            }
        };

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new Timer();
        mTimer.schedule(mTask, 3000);
    }

    public void start(Camera camera) {
        Log.d(TAG, "onAutoFocus start");
        camera.autoFocus(this);
    }
}
