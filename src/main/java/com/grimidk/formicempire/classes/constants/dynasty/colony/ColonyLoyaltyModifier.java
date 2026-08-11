package com.grimidk.formicempire.classes.constants.dynasty.colony;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class ColonyLoyaltyModifier {
    private final int id;
    private final String nameKey;
    private final int loyaltyDelta;
    private final int minLoyaltyRequired;
    private final String exclusiveGroupKey;
    private final int durationDays;

    public ColonyLoyaltyModifier(int id, String nameKey, int loyaltyDelta,
            int minLoyaltyRequired, String exclusiveGroupKey) {
        this(id, nameKey, loyaltyDelta, minLoyaltyRequired, exclusiveGroupKey, 0);
    }

    public ColonyLoyaltyModifier(int id, String nameKey, int loyaltyDelta,
            int minLoyaltyRequired, String exclusiveGroupKey, int durationDays) {
        this.id = id;
        this.nameKey = nameKey;
        this.loyaltyDelta = loyaltyDelta;
        this.minLoyaltyRequired = minLoyaltyRequired;
        this.exclusiveGroupKey = exclusiveGroupKey;
        this.durationDays = Math.max(0, durationDays);
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

    public int getDurationDays() {
        return durationDays;
    }

    public boolean hasExpiration() {
        return durationDays > 0;
    }
}
