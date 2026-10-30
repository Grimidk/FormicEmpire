package com.grimidk.formicempire.classes.entities.world;

import java.util.HashSet;
import java.util.Set;

public class TectonicPlate {
    private final int id;
    private final int centerQ;
    private final int centerR;
    private final double driftAngle;
    private final double driftSpeed;
    private final boolean oceanic;
    private final Set<Hex> hexes = new HashSet<>();

    public TectonicPlate(int id, int centerQ, int centerR, double driftAngle, double driftSpeed, boolean oceanic) {
        this.id = id;
        this.centerQ = centerQ;
        this.centerR = centerR;
        this.driftAngle = driftAngle;
        this.driftSpeed = driftSpeed;
        this.oceanic = oceanic;
    }

    public int getId() {
        return id;
    }

    public int getCenterQ() {
        return centerQ;
    }

    public int getCenterR() {
        return centerR;
    }

    public double getDriftAngle() {
        return driftAngle;
    }

    public double getDriftSpeed() {
        return driftSpeed;
    }

    public double getDriftVectorX() {
        return Math.cos(driftAngle) * driftSpeed;
    }

    public double getDriftVectorY() {
        return Math.sin(driftAngle) * driftSpeed;
    }

    public boolean isOceanic() {
        return oceanic;
    }

    public Set<Hex> getHexes() {
        return hexes;
    }

    public void addHex(Hex hex) {
        if (hex != null) {
            hexes.add(hex);
            hex.setPlateId(id);
        }
    }

    public int size() {
        return hexes.size();
    }
}
