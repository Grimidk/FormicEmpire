package com.grimidk.formicempire.classes.constants.dynasty;

public enum IntelFact {
    RANK_COLONIES(1),
    SPECIES_ASSIMILATIONS(2),
    POPULATION(3),
    MILITARY(4),
    RESOURCES(5),
    BUILDINGS(6),
    UPGRADES(7),
    ECONOMY_DIPLOMACY(8),
    COLONY_BREAKDOWN(9),
    THEFT(10);

    private final int minTier;

    IntelFact(int minTier) {
        this.minTier = minTier;
    }

    public int getMinTier() {
        return minTier;
    }

    public int getMinIntelligence() {
        return minTier * 10;
    }

    public boolean isUnlockedBy(double intelligencePercent) {
        return intelligencePercent >= getMinIntelligence();
    }

    public static int tierFor(double intelligencePercent) {
        if (intelligencePercent <= 0) {
            return 0;
        }
        return Math.min(10, (int) (intelligencePercent / 10.0));
    }
}
