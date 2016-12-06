package com.st.android.device.camera;

/**
 * Created by SeungTaek.Lim on 16. 8. 9..
 */
public class Size {
    private final int mWidth;
    private final int mHeight;

    public Size (int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
