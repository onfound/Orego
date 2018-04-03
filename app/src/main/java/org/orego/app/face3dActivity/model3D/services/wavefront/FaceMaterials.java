package org.orego.app.face3dActivity.model3D.services.wavefront;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ilyad on 03.04.2018.
 */

public final class FaceMaterials {
    // the face index (integer) where a material is first used
    private HashMap<Integer, String> faceMats;

    // for reporting
    private HashMap<String, Integer> matCount;

    // how many times a material (string) is used

    @SuppressLint("UseSparseArrays")
    FaceMaterials() {
        faceMats = new HashMap<>();
        matCount = new HashMap<>();
    } // end of FaceMaterials()

    void addUse(int faceIdx, String matName) {
        // store the face index and the material it uses
        if (faceMats.containsKey(faceIdx)) // face index already present
            System.out.println("Face index " + faceIdx + " changed to use material " + matName);
        faceMats.put(faceIdx, matName);

        // store how many times matName has been used by faces
        if (matCount.containsKey(matName)) {
            int i = matCount.get(matName) + 1;
            matCount.put(matName, i);
        } else
            matCount.put(matName, 1);
    } // end of addUse()

    public String findMaterial(int faceIdx) {
        return faceMats.get(faceIdx);
    }

    void showUsedMaterials()

    {
        System.out.println("No. of materials used: " + faceMats.size());

        // build an iterator of material names
        Set<String> keys = matCount.keySet();
        Iterator<String> iter = keys.iterator();

        // cycle through the hashmap showing the count for each material
        String matName;
        int count;
        while (iter.hasNext()) {
            matName = iter.next();
            count = matCount.get(matName);

            System.out.print(matName + ": " + count);
            System.out.println();
        }
    } // end of showUsedMaterials()

    public boolean isEmpty() {
        return faceMats.isEmpty() || this.matCount.isEmpty();
    }

}