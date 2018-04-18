package org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions;

/**
 * NotLoadedBufferException выпрыгивает, когда буффер не был загружен из .obj файла
 */

public final class NotLoadedBufferException extends Exception{
    public NotLoadedBufferException(final String message){
        super(message);
    }
}
