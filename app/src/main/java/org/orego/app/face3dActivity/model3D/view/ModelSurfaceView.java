package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;

import android.view.MotionEvent;

import org.orego.app.face3dActivity.model3D.controller.TouchController;
import org.orego.app.face3dActivity.model3D.modelRender.ModelRender;


@SuppressLint("ViewConstructor")
public final class ModelSurfaceView extends GLSurfaceView {

    private ModelRender mRenderer;
    private ModelActivity parent;
    private TouchController touchHandler;

    public ModelSurfaceView(final ModelActivity parent) {
        super(parent);
        this.parent = parent;
        setEGLContextClientVersion(3);
        mRenderer = new ModelRender(this);
        setRenderer(mRenderer);
        touchHandler = new TouchController(this, mRenderer);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event);
    }


    public ModelActivity getModelActivity() {
        return parent;
    }

    public ModelRender getmRenderer() {
        return mRenderer;
    }
}