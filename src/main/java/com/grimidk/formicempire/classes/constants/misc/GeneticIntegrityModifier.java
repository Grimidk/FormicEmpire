package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class GeneticIntegrityModifier {
    private final int id;
    private final String nameKey;
    private final double integrityDelta;
    private final String linkedDiplomaticModifierKey;

    public GeneticIntegrityModifier(int id, String nameKey, double integrityDelta,
            String linkedDiplomaticModifierKey) {
        this.id = id;
        this.nameKey = nameKey;
        this.integrityDelta = integrityDelta;
        this.linkedDiplomaticModifierKey = linkedDiplomaticModifierKey;
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

    public double getIntegrityDelta() {
        return integrityDelta;
    }

    public String getLinkedDiplomaticModifierKey() {
        return linkedDiplomaticModifierKey;
    }
}
