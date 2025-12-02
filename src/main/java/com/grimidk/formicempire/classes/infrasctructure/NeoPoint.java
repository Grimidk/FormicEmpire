package com.grimidk.formicempire.classes.infrasctructure;

import java.awt.Point;

public class NeoPoint extends Point {
    private final int dimension;
    // 0 = Overworld, 1 = Underworld, 2 = Mainworld

    public NeoPoint(int x, int y, int dimension) {
        super(x, y);
        this.dimension = dimension;
    }

    public int getDimension() {
        return dimension;
    }
}
