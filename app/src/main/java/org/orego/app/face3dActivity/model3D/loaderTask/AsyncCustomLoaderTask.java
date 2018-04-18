package org.orego.app.face3dActivity.model3D.loaderTask;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.controller.TouchController;
import org.orego.app.face3dActivity.model3D.portrait.headComposition.HeadComposition;
import org.orego.app.face3dActivity.model3D.view.ModelActivity;
import org.orego.app.face3dActivity.model3D.view.ModelSurfaceView;

import java.io.IOException;
import java.io.InputStream;

public final class AsyncCustomLoaderTask extends AsyncTask<Void, Integer, HeadComposition> {

    private static final int MESSAGE_INDEX = 0;

    @SuppressLint("StaticFieldLeak")
    private final ModelActivity parent;

    private final ProgressDialog dialog;

    private String modelId;

    private TouchController touchController;

    private HeadComposition headComposition = null;

    public AsyncCustomLoaderTask(final ModelActivity parent
            , final ModelSurfaceView modelSurfaceView) {
        this.parent = parent;
        this.modelId = parent.getParamAssetFilename();
        this.dialog = new ProgressDialog(parent, 2);
        modelSurfaceView.setTouchHandler(new TouchController(modelSurfaceView));
        this.touchController = modelSurfaceView.getTouchHandler();
        this.touchController.installHeadComposition(headComposition);
    }


    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Loading...");
        this.dialog.setCancelable(false);
        this.dialog.show();
    }

    @Override
    protected final HeadComposition doInBackground(final Void... params) {
        return build();
    }

    private void closeStream(final InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
                Log.e("AsyncCustomLoaderTask", "Problem closing stream: " + ex.getMessage(), ex);
            }
        }
    }

    private HeadComposition build() {
        try {
            final InputStream inputStreamFace = parent.getAssets().open("faces/"
                    + modelId);
            final InputStream inputStramHead = parent.getAssets().open("faces/head.obj");

            final InputStream inputStreamVertexShader = parent.getAssets()
                    .open("shaderFile/vertex.shader");
            final InputStream inputStreamFragmentShader = parent.getAssets()
                    .open("shaderFile/fragment.shader");
            return new HeadComposition(inputStramHead, inputStreamFace
                    , inputStreamVertexShader, inputStreamFragmentShader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    protected final void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        if (values[MESSAGE_INDEX] == 0) this.dialog.setMessage("make 3D model...");
    }

    @Override
    protected final void onPostExecute(HeadComposition headComposition) {
        super.onPostExecute(headComposition);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}