package org.orego.app.face3dActivity.model3D.model;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.services.wavefront.FaceMaterials;
import org.orego.app.face3dActivity.model3D.services.wavefront.Faces;
import org.orego.app.face3dActivity.model3D.services.wavefront.ModelDimensions;
import org.orego.app.face3dActivity.model3D.services.wavefront.WavefrontLoader;
import org.orego.app.face3dActivity.model3D.services.wavefront.materials.Materials;


import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import utils.Tuple;


public class Object3DData {

    // opengl version to use to draw this object
    private int version = 5;
    private File currentDir;
    private String assetsDir;
    private String id;
    private boolean drawUsingArrays = false;

    // Model data for the simplest object

    private float[] color;
    private int drawMode = GLES20.GL_POINTS;

    // Model data
    private FloatBuffer vertexBuffer = null;
    private FloatBuffer colorVertsBuffer = null;
    private FloatBuffer vertexNormalsBuffer = null;
    private IntBuffer drawOrderBuffer = null;
    private ArrayList<Tuple> texCoords;
    private Faces faces;
    private FaceMaterials faceMats;
    private Materials materials;
    private String textureFile;

    // Processed arrays
    private FloatBuffer vertexArrayBuffer = null;
    private FloatBuffer vertexColorsArrayBuffer = null;
    private FloatBuffer vertexNormalsArrayBuffer = null;
    private FloatBuffer textureCoordsArrayBuffer = null;
    private List<int[]> drawModeList = null;
    private byte[] textureData = null;

    // Transformation data
    private float[] position = new float[]{0f, 0f, 0f};
    private float[] rotation = new float[]{0f, 0f, 0f};
    private float[] scale = new float[]{1, 1, 1};
    private float[] modelMatrix = new float[16];
    private int countColor = 0;

    {
        Matrix.setIdentityM(modelMatrix, 0);
    }


    // Async Loader
    private ModelDimensions modelDimensions;
    private WavefrontLoader loader;

    public Object3DData(FloatBuffer vertexArrayBuffer) {
        this.vertexArrayBuffer = vertexArrayBuffer;
        this.version = 1;
    }

    public FloatBuffer getColorVertsBuffer() {
        return colorVertsBuffer;
    }

    public Object3DData(FloatBuffer verts, FloatBuffer colorVerts, FloatBuffer normals, ArrayList<Tuple> texCoords, Faces faces,
                        FaceMaterials faceMats, Materials materials) {
        super();
        this.colorVertsBuffer = colorVerts;
        this.vertexBuffer = verts;
        this.vertexNormalsBuffer = normals;
        this.texCoords = texCoords;
        this.faces = faces;  // parameter "faces" could be null in case of async loading
        this.faceMats = faceMats;
        this.materials = materials;
    }

    public void setLoader(WavefrontLoader loader) {
        this.loader = loader;
    }


    public WavefrontLoader getLoader() {
        return loader;
    }

    public void setDimensions(ModelDimensions modelDimensions) {
        this.modelDimensions = modelDimensions;
    }

    public ModelDimensions getDimensions() {
        return modelDimensions;
    }


    public int getVersion() {
        return version;
    }

    public Object3DData setVersion(int version) {
        this.version = version;
        return this;
    }

    public Object3DData setId(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public float[] getColor() {
        try {
            for (int i = 0; i < 3; i++) {
                color[i] = colorVertsBuffer.get(countColor * 3 + i);
            }
            countColor++;
        } catch (Exception ignored) {
            System.out.println("NULL EXCEPTION");
        }
        return color;
    }

    public Object3DData setColor(float[] color) {
        this.color = color;
        return this;
    }

    int getDrawMode() {
        return drawMode;
    }

    public Object3DData setDrawMode(int drawMode) {
        this.drawMode = drawMode;
        return this;
    }

    public int getDrawSize() {
        return 0;
    }

    // -----------

    public byte[] getTextureData() {
        return textureData;
    }

    public void setTextureData(byte[] textureData) {
        this.textureData = textureData;
    }

    float[] getPosition() {
        return position;
    }

    float getPositionX() {
        return position != null ? position[0] : 0;
    }

    float getPositionY() {
        return position != null ? position[1] : 0;
    }

    float getPositionZ() {
        return position != null ? position[2] : 0;
    }

    float[] getRotation() {
        return rotation;
    }

    private float getRotationX() {
        return rotation[0];
    }

    private float getRotationY() {
        return rotation[1];
    }

    float getRotationZ() {
        return rotation[2];
    }

    public void setScale(float[] scale) {
        this.scale = scale;
        updateModelMatrix();
    }

    float[] getScale() {
        return scale;
    }

    float getScaleX() {
        return getScale()[0];
    }

    float getScaleY() {
        return getScale()[1];
    }

    float getScaleZ() {
        return getScale()[2];
    }

    public void setRotationY(float rotY) {
        this.rotation[1] = rotY;
        updateModelMatrix();
    }

    private void updateModelMatrix() {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setRotateM(modelMatrix, 0, getRotationX(), 1, 0, 0);
        Matrix.setRotateM(modelMatrix, 0, getRotationY(), 0, 1, 0);
        Matrix.setRotateM(modelMatrix, 0, getRotationY(), 0, 0, 1);
        Matrix.scaleM(modelMatrix, 0, getScaleX(), getScaleY(), getScaleZ());
        Matrix.translateM(modelMatrix, 0, getPositionX(), getPositionY(), getPositionZ());
    }

    IntBuffer getDrawOrder() {
        return drawOrderBuffer;
    }

    File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File currentDir) {
        this.currentDir = currentDir;
    }

    public void setAssetsDir(String assetsDir) {
        this.assetsDir = assetsDir;
    }

    String getAssetsDir() {
        return assetsDir;
    }

    boolean isDrawUsingArrays() {
        return drawUsingArrays;
    }

    boolean isFlipTextCoords() {
        return true;
    }

    void setDrawUsingArrays() {
        this.drawUsingArrays = true;
    }

    FloatBuffer getNormals() {
        return vertexNormalsBuffer;
    }

    ArrayList<Tuple> getTexCoords() {
        return texCoords;
    }

    public Faces getFaces() {
        return faces;
    }

    FaceMaterials getFaceMats() {
        return faceMats;
    }

    Materials getMaterials() {
        return materials;
    }

    // -------------------- Buffers ---------------------- //

    FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    FloatBuffer getVertexArrayBuffer() {
        return vertexArrayBuffer;
    }

    void setVertexArrayBuffer(FloatBuffer vertexArrayBuffer) {
        this.vertexArrayBuffer = vertexArrayBuffer;
    }

    FloatBuffer getVertexNormalsArrayBuffer() {
        return vertexNormalsArrayBuffer;
    }

    void setVertexNormalsArrayBuffer(FloatBuffer vertexNormalsArrayBuffer) {
        this.vertexNormalsArrayBuffer = vertexNormalsArrayBuffer;
    }

    FloatBuffer getTextureCoordsArrayBuffer() {
        return textureCoordsArrayBuffer;
    }

    void setTextureCoordsArrayBuffer(FloatBuffer textureCoordsArrayBuffer) {
        this.textureCoordsArrayBuffer = textureCoordsArrayBuffer;
    }

    List<int[]> getDrawModeList() {
        return drawModeList;
    }

    FloatBuffer getVertexColorsArrayBuffer() {
        return vertexColorsArrayBuffer;
    }

    void setVertexColorsArrayBuffer(FloatBuffer vertexColorsArrayBuffer) {
        this.vertexColorsArrayBuffer = vertexColorsArrayBuffer;
    }

    public String getTextureFile() {
        return textureFile;
    }

    public void centerScale() {
        // calculate a scale factor
        float scaleFactor = 1.0f;
        float largest = modelDimensions.getLargest();
        // System.out.println("Largest dimension: " + largest);
        if (largest != 0.0f)
            scaleFactor = (1.0f / largest);
        Log.i("Object3DData", "Scaling model with factor: " + scaleFactor + ". Largest: " + largest);

        // get the model's center point
        Tuple center = modelDimensions.getCenter();
        Log.i("Object3DData", "Objects actual position: " + center.toString());

        // modify the model's vertices
        float x0, y0, z0;
        float x, y, z;
        FloatBuffer vertexBuffer = getVertexBuffer() != null ? getVertexBuffer() : getVertexArrayBuffer();
        for (int i = 0; i < vertexBuffer.capacity() / 3; i++) {
            x0 = vertexBuffer.get(i * 3);
            y0 = vertexBuffer.get(i * 3 + 1);
            z0 = vertexBuffer.get(i * 3 + 2);
            x = (x0 - (float) center.getX()) * scaleFactor;
            vertexBuffer.put(i * 3, x);
            y = (y0 - (float) center.getY()) * scaleFactor;
            vertexBuffer.put(i * 3 + 1, y);
            z = (z0 - (float) center.getZ()) * scaleFactor;
            vertexBuffer.put(i * 3 + 2, z);
        }
    } // end of centerScale()

}
