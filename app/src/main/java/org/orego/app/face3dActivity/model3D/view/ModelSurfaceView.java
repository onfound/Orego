package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import org.orego.app.face3dActivity.model3D.controller.TouchController;


@SuppressLint("ViewConstructor")
public final class ModelSurfaceView extends GLSurfaceView {

	private ModelActivity parent;
	private TouchController touchHandler;

	public ModelSurfaceView(ModelActivity parent) {
		super(parent);
		this.parent = parent;
		setEGLContextClientVersion(2);
		ModelRenderer mRenderer = new ModelRenderer(this, parent);
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
}