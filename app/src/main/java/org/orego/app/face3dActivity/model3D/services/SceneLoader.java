package org.orego.app.face3dActivity.model3D.services;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import org.orego.app.face3dActivity.model3D.model.Object3DBuilder;
import org.orego.app.face3dActivity.model3D.model.Object3DBuilder.Callback;
import org.orego.app.face3dActivity.model3D.model.Object3DData;
import org.orego.app.face3dActivity.model3D.view.ModelActivity;
import org.orego.app.face3dActivity.util.url.android.Handler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public final class SceneLoader {

    protected final ModelActivity parent;

    private List<Object3DData> objects = new ArrayList<>();

    private boolean drawWireframe = false;

    private boolean drawingPoints = false;

    private boolean drawBoundingBox = false;

    public SceneLoader(ModelActivity main) {
        this.parent = main;
    }

    public void init() {

        // Load object
        if (parent.getParamFile() != null || parent.getParamAssetDir() != null) {
            Handler.assets = parent.getAssets();
            Object3DBuilder.loadV6AsyncParallel(parent, parent.getParamFile(), parent.getParamAssetDir(),
                    parent.getParamAssetFilename(), new Callback() {
                        long startTime = SystemClock.uptimeMillis();

                        @Override
                        public void onBuildComplete(List<Object3DData> datas) {
                            for (Object3DData data : datas) {
                                loadTexture(data, parent.getParamFile(), parent.getParamAssetDir());
                            }
                            final String elapsed = (SystemClock.uptimeMillis() - startTime) / 1000 + " secs";
                            makeToastText("Load complete (" + elapsed + ")", Toast.LENGTH_LONG);
                        }

                        @Override
                        public void onLoadComplete(List<Object3DData> datas) {
                            for (Object3DData data : datas) {
                                addObject(data);
                            }
                        }

                        @Override
                        public void onLoadError(Exception ex) {
                            Log.e("SceneLoader", ex.getMessage(), ex);
                            Toast.makeText(parent.getApplicationContext(),
                                    "There was a problem building the model: " + ex.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
        }
    }

    private void makeToastText(final String text, final int toastDuration) {
        parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parent.getApplicationContext(), text, toastDuration).show();
            }
        });
    }

    private synchronized void addObject(Object3DData obj) {
        List<Object3DData> newList = new ArrayList<>(objects);
        newList.add(obj);
        this.objects = newList;
        requestRender();
    }

    private void requestRender() {
        parent.getgLView().requestRender();
    }

    public synchronized List<Object3DData> getObjects() {
        return objects;
    }

    public void toggleWireframe() {
        if (this.drawWireframe && !this.drawingPoints) {
            this.drawWireframe = false;
            this.drawingPoints = true;
            makeToastText("Points", Toast.LENGTH_SHORT);
        } else if (this.drawingPoints) {
            this.drawingPoints = false;
            makeToastText("Faces", Toast.LENGTH_SHORT);
        } else {
            makeToastText("Wireframe", Toast.LENGTH_SHORT);
            this.drawWireframe = true;
        }
        requestRender();
    }

    public boolean isDrawPoints() {
        return this.drawingPoints;
    }

    public void toggleBoundingBox() {
        this.drawBoundingBox = !drawBoundingBox;
        requestRender();
    }

    public boolean isDrawLighting() {
        return true;
    }

    private void loadTexture(Object3DData data, File file, String parentAssetsDir) {
        if (data.getTextureData() == null && data.getTextureFile() != null) {
            try {
                Log.i("SceneLoader", "Loading texture '" + data.getTextureFile() + "'...");
                InputStream stream;
                if (file != null) {
                    File textureFile = new File(file.getParent(), data.getTextureFile());
                    stream = new FileInputStream(textureFile);
                } else {
                    stream = parent.getAssets().open(parentAssetsDir + "/" + data.getTextureFile());
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copy(stream, bos);
                stream.close();

                data.setTextureData(bos.toByteArray());
            } catch (IOException ex) {
                makeToastText("Problem loading texture " + data.getTextureFile(), Toast.LENGTH_SHORT);
            }
        }
    }

}
