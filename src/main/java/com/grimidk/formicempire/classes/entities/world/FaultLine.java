package com.grimidk.formicempire.classes.entities.world;

import java.util.ArrayList;
import java.util.List;

public class FaultLine {
    public enum FaultType {
        CONVERGENT,
        DIVERGENT,
        TRANSFORM
    }

    private final int plateAId;
    private final int plateBId;
    private final FaultType type;
    private int intensity;
    private final List<Hex> hexesA = new ArrayList<>();
    private final List<Hex> hexesB = new ArrayList<>();

    public FaultLine(int plateAId, int plateBId, FaultType type) {
        this(plateAId, plateBId, type, 1);
    }

    public FaultLine(int plateAId, int plateBId, FaultType type, int intensity) {
        this.plateAId = plateAId;
        this.plateBId = plateBId;
        this.type = type;
        this.intensity = Math.max(1, Math.min(2, intensity));
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = Math.max(1, Math.min(2, intensity));
    }

    public int getPlateAId() {
        return plateAId;
    }

    public int getPlateBId() {
        return plateBId;
    }

    public FaultType getType() {
        return type;
    }

    public List<Hex> getHexesA() {
        return hexesA;
    }

    public List<Hex> getHexesB() {
        return hexesB;
    }

    public void addBoundaryPair(Hex hexA, Hex hexB) {
        if (hexA != null && !hexesA.contains(hexA)) {
            hexesA.add(hexA);
        }
        if (hexB != null && !hexesB.contains(hexB)) {
            hexesB.add(hexB);
        }
    }

    public int size() {
        return hexesA.size();
    }
}
