package org.orego.app.face3dActivity.model3D.model;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import org.orego.app.face3dActivity.model3D.util.GLUtil;
import java.nio.FloatBuffer;

public abstract class Object3DImpl implements Object3D {

    // Transformations
    private final float[] mMatrix = new float[16];
    // mvp matrix
    private final float[] mvMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    // OpenGL data
    private final int mProgram;
    private double shift = -1d;

    Object3DImpl(String vertexShaderCode, String fragmentShaderCode, String... variables) {
        int vertexShader = GLUtil.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLUtil.createAndLinkProgram(vertexShader, fragmentShader, variables);
    }

    @Override
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int textureId, float[] lightPos) {
        this.draw(obj, pMatrix, vMatrix, obj.getDrawMode(), obj.getDrawSize(), textureId, lightPos);
    }

    @Override
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int drawMode, int drawSize
            , int textureId, float[] lightPos) {
        GLES20.glUseProgram(mProgram);

        float[] mMatrix = getMMatrix(obj);
        float[] mvMatrix = getMvMatrix(mMatrix, vMatrix);
        float[] mvpMatrix = getMvpMatrix(mvMatrix, pMatrix);

        setMvpMatrix(mvpMatrix);

        //setPosition
        int vertexHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");
        GLES20.glEnableVertexAttribArray(vertexHandle);
        FloatBuffer vertexBuffer1 = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        vertexBuffer1.position(0);
        GLES20.glVertexAttribPointer(vertexHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT
                , false, VERTEX_STRIDE, vertexBuffer1);

        //setColors
        if (obj.getVertexBuffer() != null && obj.getLoader().getColorsVertA() != null) {
            FloatBuffer colorBuffer; // rgba
            colorBuffer = obj.getColorPerVertexArrayBuffer() != null
                    ? obj.getColorPerVertexArrayBuffer() : obj.getColorVertsBufferA();

            int colorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
            GLES20.glEnableVertexAttribArray(colorHandle);
            GLES20.glVertexAttribPointer(colorHandle, 3, GLES20.GL_FLOAT, false
                    , 16, colorBuffer);
        } else {
            int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            GLES20.glEnableVertexAttribArray(colorHandle);
            GLES20.glUniform4fv(colorHandle, 1, DEFAULT_COLOR, 0); // посмотреть на offset
        }

//        setNormals
        int mNormalHandle = -1;
        if (supportsNormals()) {

            mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");

            GLES20.glEnableVertexAttribArray(mNormalHandle);
            FloatBuffer buffer = obj.getVertexNormalsArrayBuffer() != null
                    ? obj.getVertexNormalsArrayBuffer() : obj.getNormals();

            buffer.position(0);
            GLES20.glVertexAttribPointer(mNormalHandle, 4, GLES20.GL_FLOAT, false
                    , 0, buffer);
        }

        // 
        if (supportsMvMatrix()) {
            setMvMatrix(mvMatrix);
        }

        if (drawSize <= 0) {
            int drawCount = vertexBuffer1.capacity() / COORDS_PER_VERTEX;
            if (this.shift >= 0) {
                double rotation = ((SystemClock.uptimeMillis() % 10000) / 10000f) * (Math.PI * 2);
                if (this.shift == 0d) {
                    this.shift = rotation;
                }
                drawCount = (int) ((Math.sin(rotation - this.shift + Math.PI / 2 * 3) + 1)
                        / 2f * drawCount);
            }
            GLES20.glDrawArrays(drawMode, 0, drawCount);
        }
        GLES20.glDisableVertexAttribArray(vertexHandle);
        if (mNormalHandle != -1) {
            GLES20.glDisableVertexAttribArray(mNormalHandle);
        }

    }

    private float[] getMMatrix(Object3DData obj) {
        // calculate object transformation
        Matrix.setIdentityM(mMatrix, 0);
        if (obj.getRotation() != null) {
            Matrix.rotateM(mMatrix, 0, obj.getRotation()[0], 1f, 0f, 0f);
            Matrix.rotateM(mMatrix, 0, obj.getRotation()[1], 0, 1f, 0f);
            Matrix.rotateM(mMatrix, 0, obj.getRotationZ(), 0, 0, 1f);
        }
        if (obj.getScale() != null) {
            Matrix.scaleM(mMatrix, 0, obj.getScaleX(), obj.getScaleY(), obj.getScaleZ());
        }
        if (obj.getPosition() != null) {
            Matrix.translateM(mMatrix, 0, obj.getPositionX(), obj.getPositionY()
                    , obj.getPositionZ());
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
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

    }

    private void setMvMatrix(float[] mvMatrix) {
        int mMVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0);

    }

    protected boolean supportsNormals() {
        return false;
    }


    protected boolean supportsMvMatrix() {
        return false;
    }

}


class Object3DV1 extends Object3DImpl {

    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "void main() {" +
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 20.0;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    Object3DV1() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position");
    }
}

class Object3DV7 extends Object3DImpl {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    "attribute vec4 vColor;\n" +
                    "uniform mat4 u_MVMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "attribute vec3 a_Normal;\n" +
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    "   v_Color = vColor;\n" +
                    "   v_Color[3] = vColor[3];" + // correct alpha
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 0.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = v_Color;\n" +
                    "}";

    Object3DV7() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_Normal");
    }

    @Override
    protected boolean supportsNormals() {
        return true;
    }

    @Override
    protected boolean supportsMvMatrix() {
        return true;
    }

}
