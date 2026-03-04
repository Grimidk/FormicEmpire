package com.grimidk.formicempire.classes.entities;

public class Tunnel {
    private final Hex hexA;
    private final Hex hexB;
    private double progress;
    private final double totalCost;
    private boolean isComplete;

    public Tunnel(Hex hexA, Hex hexB, double totalCost) {
        this.hexA = hexA;
        this.hexB = hexB;
        this.totalCost = totalCost;
        this.progress = 0;
        this.isComplete = false;
    }

    public Hex getHexA() { return hexA; }
    public Hex getHexB() { return hexB; }
    public double getProgress() { return progress; }
    public double getTotalCost() { return totalCost; }
    public boolean isComplete() { return isComplete; }

    public void addProgress(double amount) {
        if (isComplete) return;
        this.progress += amount;
        if (this.progress >= totalCost) {
            this.progress = totalCost;
            this.isComplete = true;
        }
    }

    public boolean connects(Hex a, Hex b) {
        return (hexA == a && hexB == b) || (hexA == b && hexB == a);
    }
}
