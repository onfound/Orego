package org.orego.app.face3dActivity.model3D.model;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import org.orego.app.face3dActivity.model3D.util.GLUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.microedition.khronos.opengles.GL;


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
    public void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int drawMode, int drawSize, int textureId,
                     float[] lightPos) {
        GLES20.glUseProgram(mProgram);


        float[] mMatrix = getMMatrix(obj);
        float[] mvMatrix = getMvMatrix(mMatrix, vMatrix);
        float[] mvpMatrix = getMvpMatrix(mvMatrix, pMatrix);

        setMvpMatrix(mvpMatrix);

        int vertexHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");

        GLES20.glEnableVertexAttribArray(vertexHandle);
        FloatBuffer vertexBuffer1 = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        vertexBuffer1.position(0);
        GLES20.glVertexAttribPointer(vertexHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer1);


        if (obj.getVertexBuffer() != null && obj.getLoader().getColorsVert() != null) {
            FloatBuffer colorBuffer = obj.getColorPerVertexArrayBuffer() != null ? obj.getColorPerVertexArrayBuffer() : obj.getColorVertsBuffer(); // rgba

            int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            GLES20.glEnableVertexAttribArray(colorHandle);
            GLES20.glUniform4fv(colorHandle, 1, colorBuffer);

        } else {
            int colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            GLES20.glEnableVertexAttribArray(colorHandle);
            GLES20.glUniform4fv(colorHandle, 1, DEFAULT_COLOR, 0); // посмотреть на offset
        }

        int mNormalHandle = -1;
        if (supportsNormals()) {

            mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");

            GLES20.glEnableVertexAttribArray(mNormalHandle);
            FloatBuffer buffer = obj.getVertexNormalsArrayBuffer() != null ? obj.getVertexNormalsArrayBuffer() : obj.getNormals();

            buffer.position(0);
            GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, buffer);
        }


        if (supportsMvMatrix()) {
            setMvMatrix(mvMatrix);
        }

        if (lightPos != null && supportsLighting()) {
            int mLightPosHandle = GLES20.glGetUniformLocation(mProgram, "u_LightPos");
            // Pass in the light position in eye space.
            GLES20.glUniform3f(mLightPosHandle, lightPos[0], lightPos[1], lightPos[2]);
        }

        if (drawSize <= 0) {
            int drawCount = vertexBuffer1.capacity() / COORDS_PER_VERTEX;
            if (this.shift >= 0) {
                double rotation = ((SystemClock.uptimeMillis() % 10000) / 10000f) * (Math.PI * 2);

                if (this.shift == 0d) {
                    this.shift = rotation;
                }
                drawCount = (int) ((Math.sin(rotation - this.shift + Math.PI / 2 * 3) + 1) / 2f * drawCount);
            }
            GLES20.glDrawArrays(drawMode, 0, drawCount);
        }


        GLES20.glDisableVertexAttribArray(vertexHandle);

        if (mNormalHandle != -1) {
            GLES20.glDisableVertexAttribArray(mNormalHandle);
        }

    }

    public float[] getMMatrix(Object3DData obj) {
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
            Matrix.translateM(mMatrix, 0, obj.getPositionX(), obj.getPositionY(), obj.getPositionZ());
        }
        return mMatrix;
    }

    public float[] getMvMatrix(float[] mMatrix, float[] vMatrix) {
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

    protected boolean supportsColors() {
        return false;
    }

    protected boolean supportsNormals() {
        return false;
    }

    protected boolean supportsLighting() {
        return false;
    }

    protected boolean supportsMvMatrix() {
        return false;
    }

    protected boolean supportsTextures() {
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

    @Override
    protected boolean supportsColors() {
        return false;
    }
}

class Object3DV2 extends Object3DImpl {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec4 a_Color;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  vColor = a_Color;" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    Object3DV2() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }
}

class Object3DV3 extends Object3DImpl {

    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec2 a_TexCoordinate;" + // Per-vertex texture coordinate information we will pass in.
                    "varying vec2 v_TexCoordinate;" +   // This will be passed into the fragment shader.
                    "void main() {" +
                    "  v_TexCoordinate = a_TexCoordinate;" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  gl_FragColor = vColor * texture2D(u_Texture, v_TexCoordinate);" +
                    "}";


    Object3DV3() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_TexCoordinate");
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }
}

class Object3DV4 extends Object3DImpl {

    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "attribute vec4 a_Color;" +
                    "varying vec4 vColor;" +
                    "attribute vec2 a_TexCoordinate;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  vColor = a_Color;" +
                    "  v_TexCoordinate = a_TexCoordinate;" +
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";

    private final static String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    "void main() {" +
                    "  gl_FragColor = vColor * texture2D(u_Texture, v_TexCoordinate);" +
                    "}";

    Object3DV4() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color", "a_TexCoordinate");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }

}

class Object3DV5 extends Object3DImpl {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    // light variables
                    "uniform mat4 u_MVMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "attribute vec4 a_Color;\n" +
                    "attribute vec3 a_Normal;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    // Transform the vertex into eye space.
                    "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n          " +
                    // Get a lighting direction vector from the light to the vertex.
                    "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n    " +
                    // Transform the normal's orientation into eye space.
                    "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n " +
                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);\n   " +
                    // Attenuate the light based on distance.
                    "   float distance = length(u_LightPos - modelViewVertex);\n         " +
                    "   diffuse = diffuse * (1.0 / (1.0 + (0.05 * distance * distance)));\n" +
                    //  Add ambient lighting
                    "  diffuse = diffuse + 0.5;" +
                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    "   v_Color = a_Color * diffuse;\n" +
                    "   v_Color[3] = a_Color[3];" + // correct alpha
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";

    private final static String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = v_Color;\n" +
                    "}";


    Object3DV5() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color", "a_Normal");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }

    @Override
    protected boolean supportsNormals() {
        return true;
    }

    @Override
    protected boolean supportsLighting() {
        return true;
    }

    @Override
    protected boolean supportsMvMatrix() {
        return true;
    }

}

class Object3DV6 extends Object3DImpl {

    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    // texture variables
                    "attribute vec2 a_TexCoordinate;" +
                    "varying vec2 v_TexCoordinate;" +
                    // light variables
                    "uniform mat4 u_MVMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "attribute vec4 a_Color;\n" +
                    "attribute vec3 a_Normal;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    // texture
                    "  v_TexCoordinate = a_TexCoordinate;" +
                    // Transform the vertex into eye space.
                    "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n          " +
                    // Get a lighting direction vector from the light to the vertex.
                    "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n    " +
                    // Transform the normal's orientation into eye space.
                    "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n " +
                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);\n   " +
                    // Attenuate the light based on distance.
                    "   float distance = length(u_LightPos - modelViewVertex);\n         " +
                    "   diffuse = diffuse * (1.0 / (1.0 + (0.05 * distance * distance)));\n" +
                    //  Add ambient lighting
                    "  diffuse = diffuse + 0.5;" +
                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    "   v_Color = a_Color * diffuse;\n" +
                    "   v_Color[3] = a_Color[3];" + // correct alpha
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 v_Color;\n" +
                    // textures
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    //
                    "void main() {\n" +
                    "  gl_FragColor = v_Color * texture2D(u_Texture, v_TexCoordinate);" +
                    "}";

    Object3DV6() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_Color", "a_TexCoordinate", "a_Normal");
    }

    @Override
    protected boolean supportsColors() {
        return true;
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }

    @Override
    protected boolean supportsNormals() {
        return true;
    }

    @Override
    protected boolean supportsLighting() {
        return true;
    }

    @Override
    protected boolean supportsMvMatrix() {
        return true;
    }

}

class Object3DV7 extends Object3DImpl {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    // color
                    "uniform vec4 vColor;\n" +
                    // light variables
                    "uniform mat4 u_MVMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "attribute vec3 a_Normal;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +
                    "varying vec4 v_Colors;\n" +
                    "void main() {\n" +
                    // Transform the vertex into eye space.
                    "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n          " +
                    // Get a lighting direction vector from the light to the vertex.
                    "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n    " +
                    // Transform the normal's orientation into eye space.
                    "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n " +
                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);\n   " +
                    // Attenuate the light based on distance.
                    "   float distance = length(u_LightPos - modelViewVertex);\n         " +
                    "   diffuse = diffuse * (1.0 / (1.0 + (0.05 * distance * distance)));\n" +
                    //  Add ambient lighting
                    "  diffuse = diffuse + 0.5;" +
                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    "   v_Color = vColor * diffuse;\n" +
                    "   v_Color[3] = vColor[3];" + // correct alpha

                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 0.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +

                    "void main() {\n" +
                    "  gl_FragColor = v_Color;\n" +
                    "}";

    Object3DV7() {
        super(vertexShaderCode, fragmentShaderCode, "a_Position", "a_Normal");
    }

    @Override
    protected boolean supportsColors() {
        return false;
    }

    @Override
    protected boolean supportsNormals() {
        return true;
    }

    @Override
    protected boolean supportsLighting() {
        return true;
    }

    @Override
    protected boolean supportsMvMatrix() {
        return true;
    }

}

class Object3DV8 extends Object3DImpl {
    private final static String vertexShaderCode =
            "uniform mat4 u_MVPMatrix;\n" +
                    "attribute vec4 a_Position;\n" +
                    // color
                    "uniform vec4 vColor;\n" +
                    // texture variables
                    "attribute vec2 a_TexCoordinate;" +
                    "varying vec2 v_TexCoordinate;" +
                    // light variables
                    "uniform mat4 u_MVMatrix;\n" +
                    "uniform vec3 u_LightPos;\n" +
                    "attribute vec3 a_Normal;\n" +
                    // calculated color
                    "varying vec4 v_Color;\n" +
                    "void main() {\n" +
                    // texture
                    "  v_TexCoordinate = a_TexCoordinate;" +
                    // Transform the vertex into eye space.
                    "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);\n          " +
                    // Get a lighting direction vector from the light to the vertex.
                    "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);\n    " +
                    // Transform the normal's orientation into eye space.
                    "   vec3 modelViewNormal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));\n " +
                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);\n   " +
                    // Attenuate the light based on distance.
                    "   float distance = length(u_LightPos - modelViewVertex);\n         " +
                    "   diffuse = diffuse * (1.0 / (1.0 + (0.05 * distance * distance)));\n" +
                    //  Add ambient lighting
                    "  diffuse = diffuse + 0.5;" +
                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    "   v_Color = vColor * diffuse;\n" +
                    "   v_Color[3] = vColor[3];" + // correct alpha
                    "  gl_Position = u_MVPMatrix * a_Position;\n" +
                    "  gl_PointSize = 2.5;  \n" +
                    "}";
    private final static String fragmentShaderCode =
            "precision mediump float;\n" +
                    "varying vec4 v_Color;\n" +
                    // textures
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoordinate;" +
                    //
                    "void main() {\n" +
                    "  gl_FragColor = v_Color * texture2D(u_Texture, v_TexCoordinate);" +
                    "}";

    Object3DV8() {
        super(vertexShaderCode, fragmentShaderCode, "vColor", "a_Position", "a_TexCoordinate", "a_Normal");
    }

    @Override
    protected boolean supportsColors() {
        return false;
    }

    @Override
    protected boolean supportsTextures() {
        return true;
    }

    @Override
    protected boolean supportsNormals() {
        return true;
    }

    @Override
    protected boolean supportsLighting() {
        return true;
    }

    @Override
    protected boolean supportsMvMatrix() {
        return true;
    }

}