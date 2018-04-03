package org.orego.app.face3dActivity.util.math;

import android.opengl.Matrix;


public class Math3DUtils {

    public static float[] calculateFaceNormal2(float[] v0, float[] v1, float[] v2) {
        float[] va = new float[]{v1[0] - v0[0], v1[1] - v0[1], v1[2] - v0[2]};
        float[] vb = new float[]{v2[0] - v0[0], v2[1] - v0[1], v2[2] - v0[2]};
        float[] n = new float[]{va[1] * vb[2] - va[2] * vb[1], va[2] * vb[0] - va[0] * vb[2],
                va[0] * vb[1] - va[1] * vb[0]};
        float modul = Matrix.length(n[0], n[1], n[2]);
        return new float[]{n[0] / modul, n[1] / modul, n[2] / modul};
    }

    public static float[] add(float[] a, float[] b){
        return new float[]{a[0]+b[0], a[1]+b[1], a[2]+b[2]};
    }

}
