package com.grimidk.formicempire.classes.constants.unlocks;

import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Synergy extends Constant {
    private final String descriptionKey;
    private final List<Upgrade> requirements;
    private final Upgrade reward;

    public Synergy(int id, String nameKey, String descriptionKey, Upgrade reward, Upgrade... requirements) {
        this(id, nameKey, descriptionKey, reward, null, requirements);
    }

    public Synergy(int id, String nameKey, String descriptionKey, Upgrade reward, ImageIcon icon, Upgrade... requirements) {
        super(id, nameKey, icon);
        this.descriptionKey = descriptionKey;
        this.reward = reward;
        if (requirements == null || requirements.length < 2) {
            throw new IllegalArgumentException("Synergy requires at least two upgrade requirements");
        }
        this.requirements = List.of(requirements);
    }

    public String getDescription() {
        return LanguageStrings.get(descriptionKey);
    }

    public List<Upgrade> getRequirements() {
        return requirements;
    }

    public int getRequirementCount() {
        return requirements.size();
    }

    public Upgrade getRequirement1() {
        return requirements.get(0);
    }

    public Upgrade getRequirement2() {
        return requirements.size() > 1 ? requirements.get(1) : null;
    }

    public Upgrade getReward() {
        return reward;
    }

    public String formatRequirementFlavorNames() {
        return requirements.stream()
                .map(Upgrade::getFlavorName)
                .collect(Collectors.joining(LanguageStrings.get(LanguageStrings.SYNERGY_REQUIREMENT_SEPARATOR)));
    }

    public String getTriggerTitleKey() {
        return "TRIGGER_" + getNameKey() + "_TITLE";
    }

    public String getTriggerMessageKey() {
        return "TRIGGER_" + getNameKey() + "_MSG";
    }
}
