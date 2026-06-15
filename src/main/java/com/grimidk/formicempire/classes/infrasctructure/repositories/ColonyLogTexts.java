package com.grimidk.formicempire.classes.infrasctructure.repositories;

public final class ColonyLogTexts {
    private ColonyLogTexts() {}

    public static String localizedDeathCause(String internalCause) {
        if (internalCause == null) {
            return internalCause;
        }
        return switch (internalCause) {
            case "Old Age" -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_OLD_AGE);
            case "Lack of Care" -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_LACK_OF_CARE);
            case "Dehydration" -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_DEHYDRATION);
            case "Starvation" -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_STARVATION);
            case "Contamination" -> LanguageStrings.get(LanguageStrings.LOG_CAUSE_CONTAMINATION);
            default -> internalCause;
        };
    }
}
