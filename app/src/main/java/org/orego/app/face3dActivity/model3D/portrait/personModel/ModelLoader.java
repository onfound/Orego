package org.orego.app.face3dActivity.model3D.portrait.personModel;


import org.orego.app.face3dActivity.model3D.portrait.materials.Materials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.*;

final class ModelLoader {
    private static final int BUFFER_CAPACITY = Integer.MAX_VALUE / 128;

    private final InputStream is;

    private boolean isLoaded;

    private FloatBuffer vertexBufferObject;
    private IntBuffer elementBufferObject;

    private Materials materials;

    private int numVerts = 0;
    private int numFaces = 0;

    ModelLoader(final InputStream is) {
        this.is = is;
        this.isLoaded = false;
    }

    final void load() {
        int lineNum = 0;
        if (!isLoaded) {
            vertexBufferObject = createNativeByteBuffer(BUFFER_CAPACITY).asFloatBuffer();
            elementBufferObject = createNativeByteBuffer(BUFFER_CAPACITY).asIntBuffer();
            String line;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        if (line.startsWith("v ")) {
                            float x, y, z, r, g, b;
                            try {
                                final String[] tokens = line.split(" ");
                                x = Float.parseFloat(tokens[1]);
                                y = Float.parseFloat(tokens[2]);
                                z = Float.parseFloat(tokens[3]);
                                vertexBufferObject.put(x).put(y).put(z);
                                if (tokens.length > 4) {
                                    r = Float.parseFloat(tokens[4]);
                                    g = Float.parseFloat(tokens[5]);
                                    b = Float.parseFloat(tokens[6]);
                                    vertexBufferObject.put(r).put(g).put(b);
                                }
                                numVerts++;
                            } catch (final NumberFormatException ex) {
                                System.out.println("ErrParseLine1: " + lineNum);
                                ex.printStackTrace();
                            }
                        } else if (line.startsWith("f ")) {
                            int v1Indx, v2Indx, v3Indx;
                            try {
                                final String[] tokens = line.split(" ");
                                if (tokens[1].split("/").length > 1) {
                                    tokens[1] = tokens[1].split("/")[0];
                                    tokens[2] = tokens[2].split("/")[0];
                                    tokens[3] = tokens[3].split("/")[0];
                                }
                                v1Indx = Integer.parseInt(tokens[1]);
                                v2Indx = Integer.parseInt(tokens[2]);
                                v3Indx = Integer.parseInt(tokens[3]);
                                elementBufferObject.put(v1Indx).put(v2Indx).put(v3Indx);
                                numFaces++;
                            } catch (final NumberFormatException | IndexOutOfBoundsException ex) {
                                System.out.println("ErrParseLine2: " + lineNum);
                                ex.printStackTrace();
                            }
                        } else if (line.startsWith("mtllib ")) {
                            materials = new Materials(line.substring(7));
                        } else
                            System.out.println("Ignoring line " + lineNum + " : " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Problem reading line '" + (++lineNum) + "'");
                throw new RuntimeException(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Number of vertices:" + numVerts);
            System.out.println("Number of faces:" + numFaces);

            this.isLoaded = true;
        }
    }

    private ByteBuffer createNativeByteBuffer(int length) {
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    final FloatBuffer getVertexBufferObject() {
        return isLoaded ? vertexBufferObject : null;
    }

    final IntBuffer getElementBufferObject() {
        return isLoaded ? elementBufferObject : null;
    }

    final Materials getMaterials() {
        return isLoaded ? materials : null;
    }

    final int getVertexBufferSize() {
        return numVerts * 3 * 4;
    }

    final int getElementBufferSize() {
        return numFaces * 3 * 4;
    }
}