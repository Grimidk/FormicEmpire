package com.grimidk.formicempire.classes.infrasctructure;

import java.awt.Point;

public class NeoPoint extends Point {
    private final Dimension dimension;

    public NeoPoint(int x, int y, Dimension dimension) {
        super(x, y);
        this.dimension = dimension;
    }

    public Dimension getDimension() {
        return dimension;
    }
}
