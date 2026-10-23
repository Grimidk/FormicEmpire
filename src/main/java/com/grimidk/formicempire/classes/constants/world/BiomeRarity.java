package com.grimidk.formicempire.classes.constants.world;

public enum BiomeRarity {
    COMMON(10),
    UNCOMMON(4),
    RARE(1),
    VERY_RARE(1);

    private final int weight;

    BiomeRarity(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
