package com.st.android.device.camera.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by lsit on 2016. 12. 11..
 */
public class GLCameraPreview extends GLSurfaceView {
    private GLCameraRenderer mRenderer;

    public GLCameraPreview(Context context) {
        super(context);
        init();
    }

    public GLCameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new GLCameraRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
