package org.orego.app.face3dActivity.model3D.entities;

public final class BoundingBox {

    private final String id;
    private final float xMin;
    private final float xMax;
    private final float yMin;
    private final float yMax;
    private final float zMin;
    private final float zMax;

    BoundingBox(String id, float xMin, float xMax, float yMin, float yMax, float zMin, float zMax) {
        this.id = id;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.zMin = zMin;
        this.zMax = zMax;
    }


    private float getxMin() {
        return xMin;
    }

    private float getxMax() {
        return xMax;
    }

    private float getyMin() {
        return yMin;
    }

    private float getyMax() {
        return yMax;
    }

    private float getzMin() {
        return zMin;
    }

    private float getzMax() {
        return zMax;
    }

    public float[] getCenter() {
        return new float[]{(xMax + xMin) / 2, (yMax + yMin) / 2, (zMax + zMin) / 2};
    }

    boolean outOfBound(float x, float y, float z) {
        if (x > getxMax()) {
            return true;
        }
        if (x < getxMin()) {
            return true;
        }
        if (y < getyMin()) {
            return true;
        }
        if (y > getyMax()) {
            return true;
        }
        if (z < getzMin()) {
            return true;
        }
        if (z > getzMax()) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "BoundingBox{" +
                "id='" + id + '\'' +
                ", xMin=" + xMin +
                ", xMax=" + xMax +
                ", yMin=" + yMin +
                ", yMax=" + yMax +
                ", zMin=" + zMin +
                ", zMax=" + zMax +
                '}';
    }
}