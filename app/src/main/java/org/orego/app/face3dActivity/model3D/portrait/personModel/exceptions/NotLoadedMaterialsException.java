package org.orego.app.face3dActivity.model3D.portrait.personModel.exceptions;

/**
 * NotLoadedMaterialsException выпрыгивает, когда материалы не были загружены из .mtl файла
 */

public final class NotLoadedMaterialsException extends Exception{
    public NotLoadedMaterialsException(final String message){
        super(message);
    }
}
