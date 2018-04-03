package org.orego.app.face3dActivity.model3D.services.suppliers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;

import org.orego.app.face3dActivity.model3D.loaderTask.AsyncCustomLoaderTask;
import org.orego.app.face3dActivity.model3D.model.Object3DBuilder;
import org.orego.app.face3dActivity.model3D.model.Object3DData;

import java.io.File;
import java.util.List;


public final class Object3DSupplierUtility {

    private Object3DSupplierUtility(){}

    @SuppressLint("StaticFieldLeak")
    public static void supplyAsync(final Activity parent, final File currentDir
            , final String assetsDir, final String modelId
            , final Object3DBuilder.Callback callback) {
        final AsyncTask<Void, Integer, List<Object3DData>> asyncLoaderTask
                = new AsyncCustomLoaderTask(parent, callback, modelId, currentDir, assetsDir);
        asyncLoaderTask.execute();
    }
}

