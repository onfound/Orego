package org.orego.app.face3dActivity.model3D.services.wavefront;

import java.text.DecimalFormat;

import utils.Tuple;

/**
 * Created by ilyad on 03.04.2018.
 */

public final class ModelDimensions {
    // edge coordinates
    float leftPt, rightPt; // on x-axis
    float topPt, bottomPt; // on y-axis
    float farPt, nearPt; // on z-axis

    // for reporting
    private DecimalFormat df = new DecimalFormat("0.##"); // 2 dp

    ModelDimensions() {
        leftPt = 0.0f;
        rightPt = 0.0f;
        topPt = 0.0f;
        bottomPt = 0.0f;
        farPt = 0.0f;
        nearPt = 0.0f;
    } // end of ModelDimensions()

    void set(float x, float y, float z)
    // initialize the model's edge coordinates
    {
        rightPt = x;
        leftPt = x;

        topPt = y;
        bottomPt = y;

        nearPt = z;
        farPt = z;
    } // end of set()

    void update(float x, float y, float z)
    // update the edge coordinates using vert
    {
        if (x > rightPt)
            rightPt = x;
        if (x < leftPt)
            leftPt = x;

        if (y > topPt)
            topPt = y;
        if (y < bottomPt)
            bottomPt = y;

        if (z > nearPt)
            nearPt = z;
        if (z < farPt)
            farPt = z;
    } // end of update()

    // ------------- use the edge coordinates ----------------------------

    public float getWidth() {
        return (rightPt - leftPt);
    }

    public float getHeight() {
        return (topPt - bottomPt);
    }

    float getDepth() {
        return (nearPt - farPt);
    }

    public float getLargest() {
        float height = getHeight();
        float depth = getDepth();

        float largest = getWidth();
        if (height > largest)
            largest = height;
        if (depth > largest)
            largest = depth;
        return largest;
    }

    public Tuple getCenter() {
        float xc = (rightPt + leftPt) / 2.0f;
        float yc = (topPt + bottomPt) / 2.0f;
        float zc = (nearPt + farPt) / 2.0f;
        return new Tuple(xc, yc, zc);
    }

    void reportDimensions() {
        Tuple center = getCenter();

        System.out.println("x Coords: " + df.format(leftPt) + " to " + df.format(rightPt));
        System.out.println("  Mid: " + df.format(center.getX()) + "; Width: " + df.format(getWidth()));

        System.out.println("y Coords: " + df.format(bottomPt) + " to " + df.format(topPt));
        System.out.println("  Mid: " + df.format(center.getY()) + "; Height: " + df.format(getHeight()));

        System.out.println("z Coords: " + df.format(nearPt) + " to " + df.format(farPt));
        System.out.println("  Mid: " + df.format(center.getZ()) + "; Depth: " + df.format(getDepth()));
    }

}