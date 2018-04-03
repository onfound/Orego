package org.orego.app.face3dActivity.model3D.services.wavefront.materials;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import utils.Tuple;

/**
 * Created by ilyad on 03.04.2018.
 */

public final class Materials {

    public Map<String, Material> materials;

    private String mfnm;

    Materials(String mtlFnm) {
        materials = new LinkedHashMap<>();
        this.mfnm = mtlFnm;
    }

    public void readMaterials(File currentDir, String assetsDir, AssetManager am) {
        try {
            InputStream is;
            if (currentDir != null) {
                File file = new File(currentDir, mfnm);
                System.out.println("Loading material from " + file);
                is = new FileInputStream(file);
            } else {
                System.out.println("Loading material from " + mfnm);
                is = am.open(assetsDir + "/" + mfnm);
            }
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            readMaterials(br);
            br.close();
        } catch (FileNotFoundException ex) {
            Log.w("WavefrontLoader", ex.getMessage());
        } catch (IOException e) {
            Log.e("WavefrontLoader", e.getMessage(), e);
        }

    } // end of Materials()

    private void readMaterials(BufferedReader br)
		/*
		 * Parse the MTL file line-by-line, building Material objects which are collected in the materials ArrayList.
		 */ {
        Log.v("materials", "Reading material...");
        try {
            String line;
            Material currMaterial = null; // current material

            while (((line = br.readLine()) != null)) {
                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.startsWith("newmtl ")) { // new material
                    if (currMaterial != null) // save previous material
                        materials.put(currMaterial.getName(), currMaterial);

                    // start collecting info for new material
                    String name = line.substring(7);
                    Log.d("Loader", "New material found: " + name);
                    currMaterial = new Material(name);
                } else if (line.startsWith("map_Kd ")) { // texture filename
                    // String fileName = new File(file.getParent(), line.substring(7)).getAbsolutePath();
                    String textureFilename = line.substring(7);
                    Log.d("Loader", "New texture found: " + textureFilename);
                    currMaterial.setTexture(textureFilename);
                } else if (line.startsWith("Ka ")) // ambient colour
                    currMaterial.setKa(readTuple3(line));
                else if (line.startsWith("Kd ")) // diffuse colour
                    currMaterial.setKd(readTuple3(line));
                else if (line.startsWith("Ks ")) // specular colour
                    currMaterial.setKs(readTuple3(line));
                else if (line.startsWith("Ns ")) { // shininess
                    float val = Float.valueOf(line.substring(3));
                    currMaterial.setNs(val);
                } else if (line.charAt(0) == 'd') { // alpha
                    float val = Float.valueOf(line.substring(2));
                    currMaterial.setD(val);
                } else if (line.startsWith("Tr ")) { // Transparency (inverted)
                    float val = Float.valueOf(line.substring(3));
                    if (currMaterial != null) {
                        currMaterial.setD(1 - val);
                    }
                } else
                    System.out.println("Ignoring MTL line: " + line);

            }
            if (currMaterial != null) {
                materials.put(currMaterial.getName(), currMaterial);
            }
        } catch (Exception e) {
            Log.e("materials", e.getMessage(), e);
        }
    } // end of readMaterials()

    private Tuple readTuple3(String line)
		/*
		 * The line starts with an MTL word such as Ka, Kd, Ks, and the three floats (x, y, z) separated by spaces
		 */ {
        StringTokenizer tokens = new StringTokenizer(line, " ");
        tokens.nextToken(); // skip MTL word

        try {
            float x = Float.parseFloat(tokens.nextToken());
            float y = Float.parseFloat(tokens.nextToken());
            float z = Float.parseFloat(tokens.nextToken());

            return new Tuple(x, y, z);
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }

        return null; // means an error occurred
    } // end of readTuple3()

    void showMaterials()
    // list all the Material objects
    {
        Log.i("WavefrontLoader", "No. of materials: " + materials.size());
        Material m;
        for (int i = 0; i < materials.size(); i++) {
            m = materials.get(i);
            m.showMaterial();
            // System.out.println();
        }
    } // end of showMaterials()

    public Material getMaterial(String name) {
        return materials.get(name);
    }

}