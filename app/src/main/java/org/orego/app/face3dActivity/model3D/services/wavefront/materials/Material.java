package org.orego.app.face3dActivity.model3D.services.wavefront.materials;

import utils.Tuple;

public final class Material {

    private String name;

    // colour info:
    private Tuple<Float, Float, Float> ka, kd, ks; // ambient, diffuse, specular colours

    private float d; // alpha
    private float ns; // shininess

    // texture info:
    private String textureFileName;
    private String texture;

    Material(final String materialName) {
        //name:
        this.name = materialName;
        // colour info:
        this.ka = null;
        this.kd = null;
        this.ks = null;
        //alpha & shininess:
        this.d = 1.0f;
        this.ns = 0.0f;
        
        this.textureFileName = null;
        this.texture = null;
    }

    void showMaterial() {
        System.out.println(name);
        if (ka != null)
            System.out.println("  Ka: " + ka.toString());
        if (kd != null)
            System.out.println("  Kd: " + kd.toString());
        if (ks != null)
            System.out.println("  Ks: " + ks.toString());
        if (ns != 0.0f)
            System.out.println("  Ns: " + ns);
        if (d != 1.0f)
            System.out.println("  d: " + d);
        if (textureFileName != null)
            System.out.println("  Texture file: " + textureFileName);
    } // end of showMaterial()

    void setD(float val) {
        d = val;
    }

    float getD() {
        return d;
    }

    void setNs(float val) {
        ns = val;
    }

    void setKa(Tuple t) {
        ka = t;
    }

    void setKd(Tuple t) {
        kd = t;
    }

    public float[] getKdColor() {
        if (kd == null) {
            return null;
        }
        return new float[]{kd.getX(), kd.getY(), kd.getZ(), getD()};
    }

    void setKs(Tuple t) {
        ks = t;
    }

    public void setTexture(String t) {
        texture = t;
    }

    public String getTexture() {
        return texture;
    }

    String getName() {
        return name;
    }

}