package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public enum WorldHistoryEventType {
    COLONY_FOUNDED(WorldHistoryCategory.COLONY),
    COLONY_DIED(WorldHistoryCategory.COLONY),
    DYNASTY_FORMED(WorldHistoryCategory.DYNASTY),
    DYNASTY_DIED(WorldHistoryCategory.DYNASTY),
    DYNASTY_ABSORBED(WorldHistoryCategory.DYNASTY),
    DYNASTY_RANK_UP(WorldHistoryCategory.DYNASTY),
    CAPITAL_MOVED(WorldHistoryCategory.DYNASTY),
    WAR_STARTED(WorldHistoryCategory.WAR),
    WAR_ENDED(WorldHistoryCategory.WAR),
    BATTLE(WorldHistoryCategory.BATTLE),
    REBELLION(WorldHistoryCategory.REBELLION),
    NUPTIAL_FLIGHT(WorldHistoryCategory.NUPTIAL),
    ASSIMILATION_COMPLETED(WorldHistoryCategory.ASSIMILATION);

    private final WorldHistoryCategory category;

    WorldHistoryEventType(WorldHistoryCategory category) {
        this.category = category;
    }

    public WorldHistoryCategory getCategory() {
        return category;
    }

    public static WorldHistoryEventType fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return WorldHistoryEventType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public enum WorldHistoryCategory {
        COLONY(LanguageStrings.HISTORY_FILTER_COLONY),
        DYNASTY(LanguageStrings.HISTORY_FILTER_DYNASTY),
        WAR(LanguageStrings.HISTORY_FILTER_WAR),
        BATTLE(LanguageStrings.HISTORY_FILTER_BATTLE),
        REBELLION(LanguageStrings.HISTORY_FILTER_REBELLION),
        NUPTIAL(LanguageStrings.HISTORY_FILTER_NUPTIAL),
        ASSIMILATION(LanguageStrings.HISTORY_FILTER_ASSIMILATION);

        private final String labelKey;

        WorldHistoryCategory(String labelKey) {
            this.labelKey = labelKey;
        }

        public String getLabelKey() {
            return labelKey;
        }
    }
}
