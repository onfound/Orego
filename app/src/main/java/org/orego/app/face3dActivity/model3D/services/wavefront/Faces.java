package org.orego.app.face3dActivity.model3D.services.wavefront;

import android.util.Log;

import java.nio.IntBuffer;
import java.util.ArrayList;

/**
 * Created by ilya dolgushev on 03.04.2018.
 *
 */

public final class Faces {

    private final int totalFaces;

    private IntBuffer facesVertIdxs;

    public ArrayList<int[]> facesTexIdxs;

    public ArrayList<int[]> facesNormIdxs;

    // Total number of vertices references. That is, each face references 3 or more vectors. This is the sum for all
    // faces
    private int facesLoadCounter;
    private int faceVertexLoadCounter = 0;

    // for reporting
    // private DecimalFormat df = new DecimalFormat("0.##"); // 2 dp

    Faces(int totalFaces, IntBuffer buffer) {
        this.totalFaces = totalFaces;

        facesVertIdxs = buffer;
        facesTexIdxs = new ArrayList<>();
        facesNormIdxs = new ArrayList<>();
    }

    public int getSize() {
        return totalFaces;
    }

    public boolean loaded() {
        return facesLoadCounter == totalFaces;
    }

    boolean addFace(String line) {
        try {
            line = line.substring(2); // skip the "f "
            String[] tokens;
            if (line.contains("  ")) {
                tokens = line.split(" +");
            } else {
                tokens = line.split(" ");
            }
            int numTokens = tokens.length; // number of v/vt/vn tokens
            // create arrays to hold the v, vt, vn indicies

            int vt[] = null;
            int vn[] = null;


            for (int i = 0, faceIndex = 0; i < numTokens; i++, faceIndex++) {

                // convert to triangles all polygons
                if (faceIndex > 2) {
                    // Converting polygon to triangle
                    faceIndex = 0;

                    facesLoadCounter++;
                    if (vt != null) facesTexIdxs.add(vt);
                    if (vn != null) facesNormIdxs.add(vn);

                    vt = null;
                    vn = null;

                    i -= 2;
                }

                // convert to triangles all polygons
                String faceToken;
                if (faceIndex == 0) {
                    // In FAN mode all faces shares the initial vertex
                    faceToken = tokens[0];// get a v/vt/vn
                } else {
                    faceToken = tokens[i]; // get a v/vt/vn
                }
                // token
                // System.out.println(faceToken);

                String[] faceTokens = faceToken.split("/");
                int numSeps = faceTokens.length; // how many '/'s are there in
                // the token

                int vertIdx = Integer.parseInt(faceTokens[0]);
                if (numSeps > 1) {
                    if (vt == null) vt = new int[3];
                    try {
                        vt[faceIndex] = Integer.parseInt(faceTokens[1]);
                    } catch (NumberFormatException ex) {
                        vt[faceIndex] = 0;
                    }
                }
                if (numSeps > 2) {
                    if (vn == null) vn = new int[3];
                    try {
                        vn[faceIndex] = Integer.parseInt(faceTokens[2]);
                    } catch (NumberFormatException ex) {
                        vn[faceIndex] = 0;
                    }
                }
                // add 0's if the vt or vn index values are missing;
                // 0 is a good choice since real indices start at 1

                if (WavefrontLoader.INDEXES_START_AT_1) {
                    vertIdx--;
                    if (vt != null) vt[faceIndex] = vt[faceIndex] - 1;
                    if (vn != null) vn[faceIndex] = vn[faceIndex] - 1;
                }
                // store the indices for this face
                facesVertIdxs.put(faceVertexLoadCounter++, vertIdx);
            }
            if (vt != null) facesTexIdxs.add(vt);
            if (vn != null) facesNormIdxs.add(vn);

            facesLoadCounter++;

        } catch (NumberFormatException e) {
            Log.e("WavefrontLoader", e.getMessage(), e);
            return false;
        }
        return true;
    }


    public int getVerticesReferencesCount() {
        // we have only triangles
        return getSize() * 3;
    }

    public IntBuffer getIndexBuffer() {
        return facesVertIdxs;
    }

}