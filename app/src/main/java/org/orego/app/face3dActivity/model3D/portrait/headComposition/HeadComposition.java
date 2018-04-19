package org.orego.app.face3dActivity.model3D.portrait.headComposition;


import android.opengl.GLES31;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.portrait.personModel.PersonPart;
import org.orego.app.face3dActivity.model3D.portrait.personModel.Shader;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.HeadComponentsAreNotBindedException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.NotLoadedBufferException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.face.Face;
import org.orego.app.face3dActivity.model3D.portrait.personModel.hairStyle.HairStyle;
import org.orego.app.face3dActivity.model3D.portrait.personModel.skull.Skull;
import org.orego.app.face3dActivity.util.tuple.Tuple;

import java.io.InputStream;
import java.nio.*;
import java.util.logging.Logger;

public final class HeadComposition {

    private static final Logger LOG = Logger.getLogger(HeadComposition.class.getName());

    private Skull skull;
    private Face face;
    private HairStyle currentHairStyle;

    private Shader shader;

    private FloatBuffer vertexBufferObject;

    private IntBuffer elementBufferObject;

    private FloatBuffer colorPerVertexObject;

    private FloatBuffer totalVertexBuffer;

    private FloatBuffer totalColorPerVertexBuffer;

    private final float[] mMatrix = new float[16];
    private final float[] mvMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];

    private int mProgram;
    private double shift = -1d;

    private float[] position = new float[]{0f, 0f, 0f};
    private float[] rotation = new float[]{0f, 0f, 0f};
    private float[] scale = new float[]{1, 1, 1};
    private float[] modelMatrix = new float[16];

    public HeadComposition(final InputStream skullIS, final InputStream faceIS
            , final InputStream vertexShader, final InputStream fragmentShader) {
        this.face = new Face(faceIS);
        this.skull = new Skull(skullIS);
//        this.currentHairStyle = new HairStyle(hairStyleIS);

        this.vertexBufferObject = createNativeByteBuffer(3 * 4 * (skull.getVertexBufferSize()
                + face.getVertexBufferSize())).asFloatBuffer();
        this.colorPerVertexObject = createNativeByteBuffer(3 * 4 * (skull.getVertexBufferSize()
                + face.getVertexBufferSize())).asFloatBuffer();
        this.elementBufferObject = createNativeByteBuffer(3 * 4 * (skull.getElementBufferSize()
                + face.getElementBufferSize())).asIntBuffer();

        this.totalVertexBuffer = createNativeByteBuffer((face.getElementBufferSize()
                + skull.getElementBufferSize()) * 3 * 3 * 4).asFloatBuffer();
        this.totalColorPerVertexBuffer = createNativeByteBuffer((face.getElementBufferSize()
                + skull.getElementBufferSize()) * 3 * 3 * 4).asFloatBuffer();

        this.shader = new Shader(vertexShader, fragmentShader);

        try {
            this.fillBuffers();
        } catch (final NotLoadedBufferException e) {
            LOG.info("Неудалось создать буфферы головы");
        }

        vertexBufferObject.position(0);
        elementBufferObject.position(0);
        colorPerVertexObject.position(0);
        totalColorPerVertexBuffer.position(0);
        totalVertexBuffer.position(0);
        face.getDimension().mergeDimension(skull.getDimension());
        centerScale(face);
    }

    private void fillBuffers()
            throws NotLoadedBufferException {
        for (int i = 0; i < face.getVertexBufferSize() * 3; i++) {
            vertexBufferObject.put(face.getVertexBufferObject().get(i));
        }
        for (int i = 0; i < face.getVertexBufferSize() * 3; i++) {
            colorPerVertexObject.put(face.getColorPerVertexObject().get(i));
        }
        for (int i = 0; i < face.getElementBufferSize() * 3; i++) {
            elementBufferObject.put(face.getElementBufferObject().get(i));
        }
        for (int i = 0; i < skull.getVertexBufferSize() * 3; i++) {
            vertexBufferObject.put(skull.getVertexBufferObject().get(i));
        }
        for (int i = 0; i < skull.getVertexBufferSize() * 3; i++) {
            colorPerVertexObject.put(skull.getColorPerVertexObject().get(i));
        }
        for (int i = 0; i < skull.getElementBufferSize() * 3; i++) {
            elementBufferObject.put(skull.getElementBufferObject().get(i) + face.getVertexBufferSize());
        }
        vertexBufferObject.position(0);
        elementBufferObject.position(0);
        colorPerVertexObject.position(0);
        for (int i = 0; i < elementBufferObject.capacity(); i++) {
            totalVertexBuffer.put(vertexBufferObject.get((elementBufferObject.get(i) - 1) * 3));
            totalVertexBuffer.put(vertexBufferObject.get((elementBufferObject.get(i) - 1) * 3 + 1));
            totalVertexBuffer.put(vertexBufferObject.get((elementBufferObject.get(i) - 1) * 3 + 2));
            totalColorPerVertexBuffer.put(colorPerVertexObject.get((elementBufferObject.get(i) - 1) * 3));
            totalColorPerVertexBuffer.put(colorPerVertexObject.get((elementBufferObject.get(i) - 1) * 3 + 1));
            totalColorPerVertexBuffer.put(colorPerVertexObject.get((elementBufferObject.get(i) - 1) * 3 + 2));
        }
        totalVertexBuffer.position(0);
        totalColorPerVertexBuffer.position(0);
    }

    private ByteBuffer createNativeByteBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    public void draw(float[] pMatrix, float[] vMatrix) {
        mProgram = shader.getProgramm();
        GLES31.glUseProgram(mProgram);

        float[] mMatrix = getMMatrix();
        float[] mvMatrix = getMvMatrix(mMatrix, vMatrix);
        float[] mvpMatrix = getMvpMatrix(mvMatrix, pMatrix);

        setMvpMatrix(mvpMatrix);

        //setPosition
        int vertexHandle = GLES31.glGetAttribLocation(mProgram, "a_Position");
        GLES31.glEnableVertexAttribArray(vertexHandle);
        totalVertexBuffer.position(0);
        GLES31.glVertexAttribPointer(vertexHandle, 3, GLES31.GL_FLOAT
                , false, 12, totalVertexBuffer);

        //setColors
        totalColorPerVertexBuffer.position(0);
        int colorHandle = GLES31.glGetAttribLocation(mProgram, "vColor");
        GLES31.glEnableVertexAttribArray(colorHandle);
        GLES31.glVertexAttribPointer(colorHandle, 3, GLES31.GL_FLOAT
                , false, 12, totalColorPerVertexBuffer);

        setMvMatrix(mvMatrix);

        int drawCount = totalVertexBuffer.capacity() / 3;
        if (this.shift >= 0) {
            double rotation = ((SystemClock.uptimeMillis() % 10000) / 10000f) * (Math.PI * 2);
            if (this.shift == 0d) {
                this.shift = rotation;
            }
            drawCount = (int) ((Math.sin(rotation - this.shift + Math.PI / 2 * 3) + 1)
                    / 2f * drawCount);
        }
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, drawCount);

        GLES31.glDisableVertexAttribArray(vertexHandle);

    }

    private float[] getMMatrix() {
        // calculate object transformation
        Matrix.setIdentityM(mMatrix, 0);
        if (rotation != null) {
            Matrix.rotateM(mMatrix, 0, rotation[0], 1f, 0f, 0f);
            Matrix.rotateM(mMatrix, 0, rotation[1], 0, 1f, 0f);
            Matrix.rotateM(mMatrix, 0, rotation[2], 0, 0, 1f);
        }
        if (scale != null) {
            Matrix.scaleM(mMatrix, 0, scale[0], scale[1], scale[2]);
        }
        if (position != null) {
            Matrix.translateM(mMatrix, 0, position[0], position[1]
                    , position[2]);
        }
        return mMatrix;
    }

    private float[] getMvMatrix(float[] mMatrix, float[] vMatrix) {
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, mMatrix, 0);
        return mvMatrix;
    }

    private float[] getMvpMatrix(float[] mvMatrix, float[] pMatrix) {
        Matrix.multiplyMM(mvpMatrix, 0, pMatrix, 0, mvMatrix, 0);
        return mvpMatrix;
    }


    private void setMvpMatrix(float[] mvpMatrix) {
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "u_MVPMatrix");
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

    }

    private void setMvMatrix(float[] mvMatrix) {
        int mMVMatrixHandle = GLES31.glGetUniformLocation(mProgram, "u_MVMatrix");
        GLES31.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

    }

    private void centerScale(PersonPart personPart) {
        // calculate a scale factor
        float scaleFactor = 1.0f;
        float largest = personPart.getDimension().getLargest();
        // System.out.println("Largest dimension: " + largest);
        if (largest != 0.0f)
            scaleFactor = (1.0f / largest);

        Tuple center = personPart.getDimension().getCenter();
        // modify the model's vertices
        float x0, y0, z0;
        float x, y, z;

        for (int i = 0; i < totalVertexBuffer.capacity() / 3; i++) {
            x0 = totalVertexBuffer.get(i * 3);
            y0 = totalVertexBuffer.get(i * 3 + 1);
            z0 = totalVertexBuffer.get(i * 3 + 2);
            x = (x0 - (float) center.getX()) * scaleFactor;
            totalVertexBuffer.put(i * 3, x);
            y = (y0 - (float) center.getY()) * scaleFactor;
            totalVertexBuffer.put(i * 3 + 1, y);
            z = (z0 - (float) center.getZ()) * scaleFactor;
            totalVertexBuffer.put(i * 3 + 2, z);
        }
        totalVertexBuffer.position(0);
    }
}
