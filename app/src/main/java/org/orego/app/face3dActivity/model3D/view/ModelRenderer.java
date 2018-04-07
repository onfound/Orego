package org.orego.app.face3dActivity.model3D.view;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.entities.Camera;
import org.orego.app.face3dActivity.model3D.model.Object3D;
import org.orego.app.face3dActivity.model3D.model.Object3DBuilder;
import org.orego.app.face3dActivity.model3D.model.Object3DData;
import org.orego.app.face3dActivity.model3D.model.Object3DImpl;
import org.orego.app.face3dActivity.model3D.services.SceneLoader;
import org.orego.app.face3dActivity.model3D.util.GLUtil;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ModelRenderer implements GLSurfaceView.Renderer {

    private final static String TAG = ModelRenderer.class.getName();

    // 3D window (parent component)
    private ModelSurfaceView main;
    // width of the screen
    private int width;
    // height of the screen
    private int height;
    // Out point of view handler
    private Camera camera;

    private Object3DBuilder drawer;
    // The loaded textures
    private Map<byte[], Integer> textures = new HashMap<>();

    // 3D matrices to project our 3D world
    private final float[] modelProjectionMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    // mvpMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mvpMatrix = new float[16];

    // light position required to render with lighting
    private final float[] lightPosInEyeSpace = new float[4];

    ModelRenderer(ModelSurfaceView modelSurfaceView) {
        this.main = modelSurfaceView;
    }

    private float getNear() {
        return 1f;
    }

    public float getFar() {
        return 100f;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        System.out.println("onSurfaceCreated");
        // Set the background frame color
        float[] backgroundColor = main.getModelActivity().getBackgroundColor();
        GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], backgroundColor[3]);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Enable blending for combining colors when there is transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Lets create our 3D world components
        camera = new Camera();

        // This component will draw the actual models using OpenGL
        drawer = new Object3DBuilder();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        System.out.println("onSurfaceChanged");
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);
        Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
                camera.zView, camera.xUp, camera.yUp, camera.zUp);
        float ratio = (float) width / height;
        Log.d(TAG, "projection: [" + -ratio + "," + ratio + ",-1,1]-near/far[1,10]");
        Matrix.frustumM(modelProjectionMatrix, 0, -ratio, ratio, -1, 1, getNear(), getFar());

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        System.out.println("onDrawFrame");
        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // recalculate mvp matrix according to where we are looking at now
        camera.animate();
        if (camera.hasChanged()) {
            Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
                    camera.zView, camera.xUp, camera.yUp, camera.zUp);
            // Log.d("Camera", "Changed! :"+camera.ToStringVector());
            Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0, modelViewMatrix, 0);
            camera.setChanged(false);
        }

        SceneLoader scene = main.getModelActivity().getScene();
        if (scene == null) {
            // scene not ready
            return;
        }

        // animate scene
        scene.onDrawFrame();

        // draw light
        if (scene.isDrawLighting()) {

            Object3DImpl lightBulbDrawer = (Object3DImpl) drawer.getPointDrawer();

            float[] lightModelViewMatrix = lightBulbDrawer.getMvMatrix(lightBulbDrawer.getMMatrix(scene.getLightBulb()), modelViewMatrix);

            // Calculate position of the light in eye space to support lighting
            Matrix.multiplyMV(lightPosInEyeSpace, 0, lightModelViewMatrix, 0, scene.getLightPosition(), 0);

            // Draw a point that represents the light bulb
            System.out.println("Рисуем свет");
            System.out.println(lightBulbDrawer.getClass().getSimpleName());
            lightBulbDrawer.draw(scene.getLightBulb(), modelProjectionMatrix, modelViewMatrix, -1, lightPosInEyeSpace);
        }

        List<Object3DData> objects = scene.getObjects();
        for (int i = 0; i < objects.size(); i++) {
            Object3DData objData = null;
            try {
                objData = objects.get(i);

                Object3D drawerObject = drawer.getDrawer(objData, scene.isDrawTextures(), scene.isDrawLighting());
                System.out.println(drawerObject.getClass().getSimpleName());
                // Log.d("ModelRenderer","Drawing object using '"+drawerObject.getClass()+"'");

                Integer textureId = textures.get(objData.getTextureData());
                if (textureId == null && objData.getTextureData() != null) {
                    ByteArrayInputStream textureIs = new ByteArrayInputStream(objData.getTextureData());
                    textureId = GLUtil.loadTexture(textureIs);
                    textureIs.close();
                    textures.put(objData.getTextureData(), textureId);
                } else if (scene.isDrawPoints() || objData.getFaces() == null || !objData.getFaces().loaded()) {
                    System.out.println("Рисуем хз");
                    drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix
                            , GLES20.GL_POINTS, objData.getDrawSize(),
                            textureId != null ? textureId : -1, lightPosInEyeSpace);

                } else {
                    System.out.println("Рисуем объект");
                    drawerObject.draw(objData, modelProjectionMatrix, modelViewMatrix,
                            textureId != null ? textureId : -1, lightPosInEyeSpace);

                }
            } catch (Exception ex) {
                Log.e("ModelRenderer", "There was a problem rendering the object '"
                        + objData.getId() + "':" + ex.getMessage(), ex);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Camera getCamera() {
        return camera;
    }
}