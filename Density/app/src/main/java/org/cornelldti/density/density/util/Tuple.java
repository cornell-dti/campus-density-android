package org.cornelldti.density.density.util;

public class Tuple<S, T> {
    public final S x;
    public final T y;

    public Tuple(S x, T y) {
        this.x = x;
        this.y = y;
    }

    public S getX()
    {
        return this.x;
    }

    public T getY()
    {
        return this.y;
    }
}
