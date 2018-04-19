package org.orego.app.face3dActivity.model3D.loaderTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.model.Object3DBuilder;
import org.orego.app.face3dActivity.model3D.model.Object3DData;
import org.orego.app.face3dActivity.model3D.services.wavefront.WavefrontLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public final class AsyncCustomLoaderTask extends AsyncTask<Void, Integer, List<Object3DData>> {

    private static final int MESSAGE_INDEX = 0;

    @SuppressLint("StaticFieldLeak")
    private final Activity parent;

    private final String modelId;

    private final File currentDirectory;

    private final String assetsDirectory;

    private final Object3DBuilder.Callback callback;

    private final ProgressDialog dialog;

    private Exception error;


    //+
    public AsyncCustomLoaderTask(final Activity parent, final Object3DBuilder.Callback callback
            , final String modelId, final File currentDirectory, final String assetsDirectory) {
        this.parent = parent;
        this.dialog = new ProgressDialog(parent);
        this.callback = callback;
        this.modelId = modelId;
        this.currentDirectory = currentDirectory;
        this.assetsDirectory = assetsDirectory;
    }

    //+
    @Override
    protected final void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Loading...");
        this.dialog.setCancelable(false);
        this.dialog.show();
    }

    @Override
    protected final List<Object3DData> doInBackground(final Void... params) {
        try {
            final List<Object3DData> data = build(); // инициализируем хранение объекта в DATA
            callback.onLoadComplete(data);
            build(data);
            return data;
        } catch (Exception ex) {
            error = ex;
            return null;
        }
    }

    private InputStream getInputStream() {
        Log.i("AsyncCustomLoaderTask", "Opening " + modelId + "...");
        try {
            if (currentDirectory != null) {
                return new FileInputStream(new File(currentDirectory, modelId));
            } else if (assetsDirectory != null) {
                return parent.getAssets().open(assetsDirectory + "/" + modelId);
            } else {
                throw new FileNotFoundException("Model data source not specified");
            }
        } catch (final IOException ex) {
            throw new RuntimeException(
                    "There was a problem opening file/asset '"
                            + (currentDirectory != null ? currentDirectory : assetsDirectory)
                            + "/" + modelId + "'");
        }
    }

    private void closeStream(final InputStream stream) {
        if (stream != null){
            try {
                stream.close();
            } catch (IOException ex) {
                Log.e("AsyncCustomLoaderTask", "Problem closing stream: " + ex.getMessage(), ex);
            }
        }
    }

    private List<Object3DData> build() throws IOException {
        final InputStream inputStreamObj = getInputStream();
        final WavefrontLoader wfl = new WavefrontLoader();

        // allocate memory
        publishProgress(0);
        wfl.analyzeModel(inputStreamObj); // считаем кол-во всего в файле для иничиализации
        closeStream(inputStreamObj);

        // Allocate memory. Initializing buffers [00..0] report about parsing.
        publishProgress(1);
        wfl.allocateBuffers(); // initialize
        wfl.reportOnModel(); //sout report model

        // create the 3D object
        Object3DData data3D = new Object3DData(wfl.getVerts(), wfl.getColorsVert(), wfl.getColorsVertA()
                , wfl.getNormals(), wfl.getTextureCoordinates()
                , wfl.getFaces(), wfl.getFaceMats(), wfl.getMaterials());
        data3D.setId(modelId); //помещаем в класс хранения хранения названия модели
        data3D.setCurrentDir(currentDirectory); // храним также текущую директорию
        data3D.setAssetsDir(assetsDirectory); // храним директорию assets
        data3D.setLoader(wfl); // кладем туда загрузчик-парсер
        data3D.setDrawMode(GLES20.GL_TRIANGLES); // храним режим рисования треугольников
        data3D.setDimensions(wfl.getDimensions()); // храним размеры нашего объекта
        return Collections.singletonList(data3D);
    }

    private void build(List<Object3DData> datas) throws Exception {
        InputStream stream = getInputStream();
        try {
            Object3DData data = datas.get(0); // полчили наш ящик с объектом

            // parse model
            publishProgress(2);
            data.getLoader().loadModel(stream); // wfl.loadModel
            closeStream(stream);

            // scale object
            publishProgress(3);
            data.centerScale(); // add Data vertex
            data.setScale(new float[]{5, 5, 5});

            // draw triangles instead of points
            data.setDrawMode(GLES20.GL_TRIANGLES);
            // build 3D object buffers
            publishProgress(4);
            Object3DBuilder.generateArrays(parent.getAssets(), data);
            publishProgress(5);

        } catch (Exception e) {
            Log.e("Object3DBuilder", e.getMessage(), e);
            throw e;
        } finally {
            closeStream(stream);
        }
    }

    @Override
    protected final void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        switch (values[MESSAGE_INDEX]) {
            case 0:
                this.dialog.setMessage("Analyzing model...");
                break;
            case 1:
                this.dialog.setMessage("Allocating memory...");
                break;
            case 2:
                this.dialog.setMessage("Loading data...");
                break;
            case 3:
                this.dialog.setMessage("Scaling object...");
                break;
            case 4:
                this.dialog.setMessage("Building 3D model...");
        }
    }

    @Override
    protected final void onPostExecute(List<Object3DData> data) {
        super.onPostExecute(data);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (error != null) {
            callback.onLoadError(error);
        } else {
            callback.onBuildComplete(data);
        }
    }
}