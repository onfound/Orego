package org.orego.app.face3dActivity.model3D.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;


public final class ModelActivity extends Activity {

    private String paramAssetDir; // faces
    private String paramAssetFilename; // modelId
    private ModelSurfaceView gLView;


    @SuppressLint("ResourceAsColor")
    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get input parameters
        final Bundle b = getIntent().getExtras();
        if (b != null) {
            this.paramAssetDir = b.getString("assetDir");
            this.paramAssetFilename = b.getString("assetFilename");
        }
        this.gLView = new ModelSurfaceView(this);
        setContentView(gLView);
    }


    public String getParamAssetDir() {
        return paramAssetDir;
    }

    public String getParamAssetFilename() {
        return paramAssetFilename;
    }

    public ModelSurfaceView getgLView() {
        return gLView;
    }
}
