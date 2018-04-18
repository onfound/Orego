package org.orego.app.face3dActivity.model3D.modelRender;

import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import org.orego.app.face3dActivity.model3D.portrait.headComposition.HeadComposition;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ModelRender implements GLSurfaceView.Renderer {
    private int width;

    private int height;
    private HeadComposition headComposition;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES32.glClearColor(0.0f, 1.0f, 1.0f, 1.0f);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);

        // Enable blending for combining colors when there is transparency
        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        GLES32.glViewport(0, 0, width, height);


    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (headComposition != null){
            GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT | GLES31.GL_DEPTH_BUFFER_BIT);
            headComposition.draw();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void installHeadComposition(HeadComposition headComposition){
        this.headComposition = headComposition;
    }
}
