package com.grimidk.formicempire.classes.constants.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class DiplomaticReputationModifier {
    private final int id;
    private final String nameKey;
    private final int reputationDelta;
    private final int minReputationRequired;
    private final String exclusiveGroupKey;
    private final int durationDays;

    public DiplomaticReputationModifier(int id, String nameKey, int reputationDelta,
            int minReputationRequired, String exclusiveGroupKey) {
        this(id, nameKey, reputationDelta, minReputationRequired, exclusiveGroupKey, 0);
    }

    public DiplomaticReputationModifier(int id, String nameKey, int reputationDelta,
            int minReputationRequired, String exclusiveGroupKey, int durationDays) {
        this.id = id;
        this.nameKey = nameKey;
        this.reputationDelta = reputationDelta;
        this.minReputationRequired = minReputationRequired;
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

    public int getReputationDelta() {
        return reputationDelta;
    }

    public int getMinReputationRequired() {
        return minReputationRequired;
    }

    public String getExclusiveGroupKey() {
        return exclusiveGroupKey;
    }

    public boolean meetsReputationRequirement(int reputationScore) {
        return reputationScore >= minReputationRequired;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public boolean hasExpiration() {
        return durationDays > 0;
    }
}
