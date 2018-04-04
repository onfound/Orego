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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public final class SceneLoader {

    protected final ModelActivity parent;

    private List<Object3DData> objects = new ArrayList<>();

    private boolean drawWireframe = false;

    private boolean drawingPoints = false;

    private boolean drawBoundingBox = false;

    private boolean drawTextures = true;

    private boolean rotatingLight = true;

    private boolean drawLighting = true;

    private final float[] lightPosition = new float[]{0, 0, 6, 1};

    private final Object3DData lightPoint = Object3DBuilder.buildPoint(lightPosition).setId("light");

    private boolean hidden;

    public SceneLoader(ModelActivity main) {
        this.parent = main;
    }

    public void init() {

        // Load object
        if (parent.getParamFile() != null || parent.getParamAssetDir() != null) {

            // Initialize assets url handler
            Handler.assets = parent.getAssets();
            // Handler.classLoader = parent.getClassLoader(); (optional)
            // Handler.androidResources = parent.getResources(); (optional)

            // Create asset url
            final URL url;
            try {
                if (parent.getParamFile() != null) {
                    url = parent.getParamFile().toURI().toURL();
                } else {
                    url = new URL("android://org.orego.dddmodel2/assets/" + parent.getParamAssetDir() + File.separator + parent.getParamAssetFilename());

                }
            } catch (MalformedURLException e) {
                Log.e("SceneLoader", e.getMessage(), e);
                throw new RuntimeException(e);
            }
            if (!hidden) {
                Object3DBuilder.loadV6AsyncParallel(parent, url, parent.getParamFile(), parent.getParamAssetDir(),
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
    }

    private void makeToastText(final String text, final int toastDuration) {
        parent.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(parent.getApplicationContext(), text, toastDuration).show();
            }
        });
    }

    public Object3DData getLightBulb() {
        return lightPoint;
    }

    public float[] getLightPosition() {
        return lightPosition;
    }

    /**
     * Hook for animating the objects before the rendering
     */
    public void onDrawFrame() {
        animateLight();
    }

    private void animateLight() {
        if (!rotatingLight) return;

        // animate light - Do a complete rotation every 5 seconds.
        long time = SystemClock.uptimeMillis() % 5000L;
        float angleInDegrees = (360.0f / 5000.0f) * ((int) time);
        lightPoint.setRotationY(angleInDegrees);
    }

    synchronized void addObject(Object3DData obj) {
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

    public void toggleTextures() {
        this.drawTextures = !drawTextures;
    }

    public void toggleLighting() {
        if (this.drawLighting && this.rotatingLight) {
            this.rotatingLight = false;
            makeToastText("Light stopped", Toast.LENGTH_SHORT);
        } else if (this.drawLighting) {
            this.drawLighting = false;
            makeToastText("Lights off", Toast.LENGTH_SHORT);
        } else {
            this.drawLighting = true;
            this.rotatingLight = true;
            makeToastText("Light on", Toast.LENGTH_SHORT);
        }
        requestRender();
    }

    public boolean isDrawTextures() {
        return drawTextures;
    }

    public boolean isDrawLighting() {
        return drawLighting;
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

    public void loadTexture(URL path) {
        if (objects.size() != 1) {
            makeToastText("Unavailable", Toast.LENGTH_SHORT);
            return;
        }
        try {
            InputStream is = path.openStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(is, bos);
            is.close();

            Object3DData obj = objects.get(0);
            obj.setTextureData(bos.toByteArray());
        } catch (IOException ex) {
            makeToastText("Problem loading texture: " + ex.getMessage(), Toast.LENGTH_SHORT);
        }
    }

}
