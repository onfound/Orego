package org.orego.app.face3dActivity.model3D.portrait.personModel.hairStyle;


import org.orego.app.face3dActivity.model3D.portrait.personModel.PersonPart;

import java.io.InputStream;

public final class HairStyle extends PersonPart {

    public HairStyle(final InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public final String getName() {
        return "HairStyle";
    }
}
