package org.orego.app.face3dActivity.model3D.portrait.personModel.face;

import org.orego.app.face3dActivity.model3D.portrait.personModel.PersonPart;


import java.io.InputStream;

public final class Face extends PersonPart {

    public Face(final InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public final String getName() {
        return "Face";
    }
}
