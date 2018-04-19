package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;


public final class ModelActivity extends Activity {

    private ModelSurfaceView gLView;


    @SuppressLint("ResourceAsColor")
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.gLView = new ModelSurfaceView(this);
        setContentView(gLView);
    }
    @Override
    protected void onPause() {
        super.onPause();
        gLView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        gLView.onResume();
    }

    public ModelSurfaceView getgLView() {
        return gLView;
    }
}
