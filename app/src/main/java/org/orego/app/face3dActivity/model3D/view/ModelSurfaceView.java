package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.view.MotionEvent;

import org.orego.app.face3dActivity.model3D.controller.TouchController;
import org.orego.app.face3dActivity.model3D.loaderTask.AsyncCustomLoaderTask;
import org.orego.app.face3dActivity.model3D.portrait.headComposition.HeadComposition;

import java.util.List;


@SuppressLint("ViewConstructor")
public final class ModelSurfaceView extends GLSurfaceView {


	private TouchController touchHandler;

	public ModelSurfaceView(ModelActivity parent) {
		super(parent);
		setEGLContextClientVersion(3);
		final AsyncTask<Void, Integer, HeadComposition> asyncLoaderTask
				= new AsyncCustomLoaderTask(parent, this);
		asyncLoaderTask.execute();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return touchHandler.onTouchEvent(event);
	}

	public TouchController getTouchHandler() {
		return touchHandler;
	}

	public void setTouchHandler(TouchController touchHandler) {
		this.touchHandler = touchHandler;
	}
}