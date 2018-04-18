package org.orego.app.face3dActivity.model3D.portrait.personModel.skull;

import org.orego.app.face3dActivity.model3D.portrait.personModel.PersonPart;


import java.io.InputStream;

public final class Skull extends PersonPart {

    public Skull(final InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public final String getName() {
        return "Skull";
    }
}
