package org.orego.app.face3dActivity.model3D.portrait.headComposition;


import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import org.orego.app.face3dActivity.model3D.entities.Camera;
import org.orego.app.face3dActivity.model3D.portrait.personModel.Shader;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.HeadComponentsAreNotBindedException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.NotLoadedBufferException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.ViolatedBindingSequenceOfPartsException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.face.Face;
import org.orego.app.face3dActivity.model3D.portrait.personModel.hairStyle.HairStyle;
import org.orego.app.face3dActivity.model3D.portrait.personModel.skull.Skull;

import java.io.InputStream;
import java.nio.*;
import java.util.logging.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Ilya Dolgushev && Igor Gulkin, 15.04.2018
 * <p>
 * Класс HeadComposition является главным классом приложения,
 * поскольку он полностью отображает 3D оболочку Вашего лица на экране.
 * При построении лица HeadComposition опирается на библиотеку OpenGL,
 * которая будет отрисовывать модель Вашей головы.
 */

public final class HeadComposition implements GLSurfaceView.Renderer {

    private static final Logger LOG = Logger.getLogger(HeadComposition.class.getName());

    /**
     * Чтобы OpenGL отрисовало голову, необходимо создать череп, лицо, прическа соответственно:
     */

    private final Skull skull;

    private final Face face;

    private HairStyle currentHairStyle;

    private Shader shader;

    private Camera camera;






    private int VAO = 0;



    /**
     * На самом деле OpenGL отрисовывает при помощи буфферов, поэтому мы создадим наш буффер,
     * который содержит в себе вершины: x, y, z, r, g, b.
     * Таким образом, сначала у нас идут вершины черепа, затем лица.
     */

    private FloatBuffer vertexBufferObject;

    /**
     * Второй буффер хранит в себе полигоны вершин.
     * Поскольку мы будем отрисовавыть "треугольниками",
     * то мы будем хранить номера в списке трех вершин соответственно:
     */

    private IntBuffer elementBufferObject;

    private int width;

    private int height;

    /**
     * @param skullIS InputStream .obj файла черепа человека
     * @param faceIS  InputStream .obj файла лица человека
     *                <p>
     *                В конструкторе происходит загрузка вершинного и фрагментного шейдеров, а также всех трёх частей:
     */

    public HeadComposition(final InputStream skullIS, final InputStream faceIS
            , final InputStream vertexShader, final InputStream fragmentShader) {
        this.skull = new Skull(skullIS);
        this.face = new Face(faceIS);
//        this.currentHairStyle = new HairStyle(hairStyleIS);
        //Неявно инциализируем буферы:
        this.vertexBufferObject = createNativeByteBuffer(face.getVertexBufferSize()
                + skull.getVertexBufferSize()).asFloatBuffer();
        this.elementBufferObject = createNativeByteBuffer(face.getElementBufferSize()
                + skull.getElementBufferSize()).asIntBuffer();
        //Загружаем шейдеры:
        this.shader = new Shader(vertexShader, fragmentShader);
        try {
            this.fillBuffers();
        } catch (final NotLoadedBufferException e) {
            LOG.info("Неудалось создать буфферы головы");
        }

        //Генерируем буфферы:
        int[] VBO = new int[1];
        int[] VAO = new int[1];
        int[] EBO = new int[1];
        GLES32.glGenVertexArrays(1, VAO, 0);
        GLES32.glGenBuffers(1, VBO, 0);
        GLES32.glGenBuffers(1, EBO, 0);
        //Привязывем Vertex Array Object:
        GLES32.glBindVertexArray(VAO[0]);
        this.VAO = VAO[0];
        //Привязывем Vertex Buffer Object:
        GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, VBO[0]);
        GLES32.glBufferData(GLES32.GL_ARRAY_BUFFER, vertexBufferObject.capacity()
                , vertexBufferObject, GLES32.GL_STATIC_DRAW);
        //Привязывем Element Buffer Object:
        GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, EBO[0]);
        GLES32.glBufferData(GLES32.GL_ELEMENT_ARRAY_BUFFER, elementBufferObject.capacity()
                , elementBufferObject, GLES32.GL_STATIC_DRAW);
        //Устанавливаем атрибуты позиции:
        GLES32.glVertexAttribPointer(0, 3, GLES32.GL_FLOAT, false
                , 6 * 4, 0);
        GLES32.glEnableVertexAttribArray(0);
        //Устанавливаем атрибуты цвета:
        GLES32.glVertexAttribPointer(1, 3, GLES32.GL_FLOAT, false
                , 6 * 4, 3 * 4);
        GLES32.glEnableVertexAttribArray(2);
        //Отвязали буффер:
        GLES32.glBindVertexArray(0);
    }

    private void fillBuffers()
            throws NotLoadedBufferException {
        for (int i = 0; i < face.getElementBufferSize(); i++) {
            if (i < face.getVertexBufferSize()) {
                vertexBufferObject.put(face.getVertexBufferObject().get(i));
            }
            elementBufferObject.put(face.getElementBufferObject().get(i));
        }
        for (int i = 0; i < skull.getElementBufferSize(); i++) {
            if (i < skull.getVertexBufferSize()) {
                vertexBufferObject.put(skull.getVertexBufferObject().get(i));
            }
            elementBufferObject.put(skull.getElementBufferObject().get(i) + face.getElementBufferSize());
        }
    }

    /**
     * @author Ilya Dolgushev 17.04.18
     */

    private ByteBuffer createNativeByteBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }


    /**
     * draw() - отрисовывает голову на экране.
     *
     * @throws HeadComponentsAreNotBindedException Когда у нас все части загружены в правильном порядке и шейдеры включены,
     *                                             то можно приступить к самой вкусной части - отрисовке головы.
     *                                             Мы здесь непосредственно работаем с OpenGL:
     *                                             передаем ему буффер вершин vertexBufferObject и буффер полигонов elementBufferObject.
     */

    private void draw() {
        GLES32.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);

        shader.use();

        GLES32.glBindVertexArray(VAO);
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, elementBufferObject.capacity(), GLES32.GL_INT
                , 0);
        GLES32.glBindVertexArray(0);
    }

    /**
     * setCurrentHairStyle() - меняет прическу.
     * <p>
     * Перед сменой прически отрисовка OpenGL останавливается, и прическа меняется,
     * после чего снова включится метод отрисовки, то есть вызывается метод draw().
     */

//    public final void setCurrentHairStyle(final HairStyle newHairStyle) {
//        final HairStyle previousHairStyle = this.currentHairStyle;
//        previousHairStyle.setBinded(false);
//
//        //change buffers:
//        newHairStyle.setBinded(true);
//        this.currentHairStyle = newHairStyle;
//        try {
//            this.draw();
//        } catch (final HeadComponentsAreNotBindedException e) {
//            LOG.info("Oops! Something went wrong");
//        }
//    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES32.glClearColor(1.0f, 1.0f,1.0f,1.0f);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);

        // Enable blending for combining colors when there is transparency
        GLES32.glEnable(GLES32.GL_BLEND);
        GLES32.glBlendFunc(GLES32.GL_ONE, GLES32.GL_ONE_MINUS_SRC_ALPHA);
        camera = new Camera();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        final float[] modelProjectionMatrix = new float[16];
        final float[] modelViewMatrix = new float[16];
        final float[] mvpMatrix = new float[16];
        GLES32.glViewport(0, 0, width, height);
        Matrix.setLookAtM(modelViewMatrix, 0, camera.xPos, camera.yPos, camera.zPos, camera.xView, camera.yView,
                camera.zView, camera.xUp, camera.yUp, camera.zUp);
        Matrix.multiplyMM(mvpMatrix, 0, modelProjectionMatrix, 0
                , modelViewMatrix, 0);
        camera.setChanged(false);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        draw();
    }


    public Camera getCamera() {
        return camera;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
