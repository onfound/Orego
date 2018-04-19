package org.orego.app.face3dActivity.model3D.portrait.personModel;

import org.orego.app.face3dActivity.model3D.portrait.materials.Materials;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.NotLoadedBufferException;
import org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions.NotLoadedMaterialsException;
import org.orego.app.face3dActivity.util.tuple.Tuple;

import java.io.InputStream;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Класс PersonPart
 */

public abstract class PersonPart {

    private boolean isBinded;

    private ModelLoader modelLoader;

    /**
     * @param inputStream
     *        InputStream .obj файла
     *
     * В конструкторе происходит инициализация загрузчика 3D модели ModelLoader,
     * затем сразу же вызывается modelLoader.load(), что говорит нам о том,
     * что началась загрузка всех нужных нам файлов.
     */

    public PersonPart(final InputStream inputStream){
        final ModelLoader modelLoader = new ModelLoader(inputStream);
        modelLoader.load();
        this.modelLoader = modelLoader;
        this.isBinded = false;
    }

    /**
     * @return возвращает float буфер вершин;
     * @throws NotLoadedBufferException
     */

    public final FloatBuffer getVertexBufferObject() throws NotLoadedBufferException {
        final FloatBuffer buffer = modelLoader.getVertexBufferObject();
        if (buffer != null){
            return buffer;
        } else {
            throw new NotLoadedBufferException("Not loaded" + getName() + "vertex buffer object");
        }
    }


    /**
     * @return возвращает имя части головы
     */

   public abstract String getName();

    /**
     * @return возвращает int буфер вершин;
     * @throws NotLoadedBufferException
     */

    public final IntBuffer getElementBufferObject() throws NotLoadedBufferException{
        final IntBuffer buffer = modelLoader.getElementBufferObject();
        if (buffer != null){
            return buffer;
        } else {
            throw new NotLoadedBufferException("Not loaded" + getName() + "element buffer object");
        }
    }
    public final FloatBuffer getColorPerVertexObject() throws NotLoadedBufferException{
        final FloatBuffer buffer = modelLoader.getColorPerVertex();
        if (buffer != null){
            return buffer;
        } else {
            throw new NotLoadedBufferException("Not loaded" + getName() + "element buffer object");
        }
    }

    /**
     * @return возвращает материалы для 3D объекта
     * @throws NotLoadedMaterialsException
     */

    public final Materials getMaterials() throws NotLoadedMaterialsException {
        final Materials materials = modelLoader.getMaterials();
        if (materials != null){
            return materials;
        } else {
            throw new NotLoadedMaterialsException("Not loaded" + getName() + "materials");
        }
    }


    /**
     * @return указывает готовность буферов
     */

    public final boolean isBinded(){
        return isBinded;
    }

    /**
     * @return устанавливает готовность буферов
     */

    public final void setBinded(final boolean binded) {
        this.isBinded = binded;
    }

    public ModelDimensions getDimension(){
        return modelLoader.getDimension();
    }

    public final int getVertexBufferSize(){
        return modelLoader.getVertexBufferSize();
    }

    public final int getElementBufferSize(){
        return modelLoader.getElementBufferSize();
    }
}
