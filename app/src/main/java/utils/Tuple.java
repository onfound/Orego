package utils;

/**
 * Created by ilya dolgushev on 03.04.2018.
 *
 */

public final class Tuple<X, Y, Z> {

    private X x;
    private Y y;
    private Z z;

    public Tuple(final X x, final Y y, final Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final String toString() {
        return "( " + x + ", " + y + ", " + z + " )";
    }

    public final X getX() {
        return x;
    }

    public final Y getY() {
        return y;
    }

    public final Z getZ() {
        return z;
    }
}

