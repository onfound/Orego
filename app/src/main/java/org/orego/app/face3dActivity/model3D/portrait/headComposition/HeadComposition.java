package org.orego.app.face3dActivity.model3D.portrait.headComposition;


import android.opengl.GLES30;
import org.orego.app.face3dActivity.model3D.portrait.personModel.Shader;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.HeadComponentsAreNotBindedException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.NotLoadedBufferException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.face.Face;
import org.orego.app.face3dActivity.model3D.portrait.personModel.hairStyle.HairStyle;
import org.orego.app.face3dActivity.model3D.portrait.personModel.skull.Skull;

import java.io.InputStream;
import java.nio.*;
import java.util.logging.Logger;


/**
 * @author Ilya Dolgushev && Igor Gulkin, 15.04.2018
 * <p>
 * Класс HeadComposition является главным классом приложения,
 * поскольку он полностью отображает 3D оболочку Вашего лица на экране.
 * При построении лица HeadComposition опирается на библиотеку OpenGL,
 * которая будет отрисовывать модель Вашей головы.
 */

public final class HeadComposition {

    private static final Logger LOG = Logger.getLogger(HeadComposition.class.getName());

    /**
     * Чтобы OpenGL отрисовало голову, необходимо создать череп, лицо, прическа соответственно:
     */

    private final Skull skull;

    private final Face face;

    private HairStyle currentHairStyle;

    private Shader shader;

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


    /**
     * @param skullIS InputStream .obj файла черепа человека
     * @param faceIS  InputStream .obj файла лица человека
     *                <p>
     *                В конструкторе происходит загрузка вершинного и фрагментного шейдеров, а также всех трёх частей:
     */

    public HeadComposition(final InputStream skullIS, final InputStream faceIS
            , final InputStream vertexShader, final InputStream fragmentShader) {
        this.face = new Face(faceIS);
        this.skull = new Skull(skullIS);
//        this.currentHairStyle = new HairStyle(hairStyleIS);
        //Неявно инциализируем буферы:
        this.vertexBufferObject = createNativeByteBuffer(6 * 4 * face.getVertexBufferSize()
                + 6 * 4 * skull.getVertexBufferSize()).asFloatBuffer();
        this.elementBufferObject = createNativeByteBuffer(3 * 4 * face.getElementBufferSize()
                + 3 * 4 * skull.getElementBufferSize()).asIntBuffer();
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
        GLES30.glGenVertexArrays(1, VAO, 0);
        GLES30.glGenBuffers(1, VBO, 0);
        GLES30.glGenBuffers(1, EBO, 0);
        //Привязывем Vertex Array Object:
        GLES30.glBindVertexArray(VAO[0]);
        this.VAO = VAO[0];
        FloatBuffer vbuffer = createNativeByteBuffer(6*4*4).asFloatBuffer();
        vbuffer.position(0);
        IntBuffer ebuffer = createNativeByteBuffer(6*4).asIntBuffer();
        ebuffer.position(0);

        float[] vertices = {
                -0.5f, 0.5f, 1.0f, 1.0f, 0.0f, 0.0f,
                0.5f, 0.5f, 1.0f, 1.0f, 1.0f, 0.0f,
                -0.5f, -0.5f, 1.0f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, 1.0f, 1.0f, 0.0f, 0.0f
        };
        int[] elements = {
                1, 3, 4,
                1, 2, 4
        };
        for (float vertice : vertices) {
            vbuffer.put(vertice);
        }
        for (int e: elements){
            ebuffer.put(e);
        }
        vbuffer.position(0);
        ebuffer.position(0);
        System.out.println(vbuffer);
        vertexBufferObject.position(0);
        elementBufferObject.position(0);
        //Привязывем Vertex Buffer Object:
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, VBO[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vbuffer.capacity()
                , vbuffer, GLES30.GL_STATIC_DRAW);
        //Привязывем Element Buffer Object:
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, EBO[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, ebuffer.capacity()
                , ebuffer, GLES30.GL_STATIC_DRAW);
        //Устанавливаем атрибуты позиции:
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false
                , 6 * 4, 0);
        GLES30.glEnableVertexAttribArray(0);
        //Устанавливаем атрибуты цвета:
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false
                , 6 * 4, 3 * 4);
        GLES30.glEnableVertexAttribArray(1);
        //Отвязали буффер:
        GLES30.glBindVertexArray(0);
    }

    private void fillBuffers()
            throws NotLoadedBufferException {
        for (int i = 0; i < face.getVertexBufferSize() * 6; i++) {
            vertexBufferObject.put(face.getVertexBufferObject().get(i));
        }
        for (int i = 0; i < face.getElementBufferSize() * 3; i++) {
            elementBufferObject.put(face.getElementBufferObject().get(i));
        }
        for (int i = 0; i < skull.getElementBufferSize() * 3; i++) {
            elementBufferObject.put(skull.getElementBufferObject().get(i) + face.getElementBufferSize());
        }
        for (int i = 0; i < skull.getVertexBufferSize() * 6; i++) {
            vertexBufferObject.put(skull.getVertexBufferObject().get(i));
        }
    }

    /**
     * @author Ilya Dolgushev 17.04.18
     *
     * метод который выделяет память буфферу и возвращет Byffer
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

    public void draw() {
        shader.use();
        elementBufferObject.position(0);
        GLES30.glBindVertexArray(VAO);
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_INT
                , 0);
        GLES30.glBindVertexArray(0);
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


}
