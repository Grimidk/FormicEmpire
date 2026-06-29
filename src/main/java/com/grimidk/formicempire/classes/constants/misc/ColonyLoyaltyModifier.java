package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class ColonyLoyaltyModifier {
    private final int id;
    private final String nameKey;
    private final int loyaltyDelta;
    private final int minLoyaltyRequired;
    private final String exclusiveGroupKey;

    public ColonyLoyaltyModifier(int id, String nameKey, int loyaltyDelta,
            int minLoyaltyRequired, String exclusiveGroupKey) {
        this.id = id;
        this.nameKey = nameKey;
        this.loyaltyDelta = loyaltyDelta;
        this.minLoyaltyRequired = minLoyaltyRequired;
        this.exclusiveGroupKey = exclusiveGroupKey;
    }

    public int getId() {
        return id;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getName() {
        return LanguageStrings.get(nameKey);
    }

    public int getLoyaltyDelta() {
        return loyaltyDelta;
    }

    public int getMinLoyaltyRequired() {
        return minLoyaltyRequired;
    }

    public String getExclusiveGroupKey() {
        return exclusiveGroupKey;
    }

    public boolean meetsLoyaltyRequirement(int loyaltyScore) {
        return loyaltyScore >= minLoyaltyRequired;
    }
}
