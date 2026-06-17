package com.grimidk.formicempire.classes.infrasctructure.repositories;

public final class ColonyLogTexts {
    private ColonyLogTexts() {}

    public static String localizedDeathCause(String internalCause) {
        return switch (DeathCause.normalize(internalCause)) {
            case DeathCause.OLD_AGE -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_OLD_AGE);
            case DeathCause.LACK_OF_CARE -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_LACK_OF_CARE);
            case DeathCause.DEHYDRATION -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_DEHYDRATION);
            case DeathCause.STARVATION -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_STARVATION);
            case DeathCause.CONTAMINATION -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_CONTAMINATION);
            case DeathCause.CONFLICT -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_CONFLICT);
            case DeathCause.ILLNESS -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_ILLNESS);
            default -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_OTHER);
        };
    }
}
