package com.grimidk.formicempire.classes.constants.critter.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class AntSubtype extends Constant {
    private final AntSubtypeSlot slot;
    private final int digit;
    private final Upgrade requiredUpgrade;
    private final String spriteSpeciesDir;
    private final String spriteFolder;
    private final float attackMult;
    private final boolean attackAdditive;
    private final float defenseMult;
    private final float speedMult;
    private final float forageMult;
    private final String descKey;

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, nameKey + "_DESC", icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            String descKey, ImageIcon icon) {
        super(id, nameKey, icon);
        this.slot = slot;
        this.digit = digit;
        this.requiredUpgrade = requiredUpgrade;
        this.spriteSpeciesDir = spriteSpeciesDir;
        this.spriteFolder = spriteFolder;
        this.attackMult = attackMult;
        this.attackAdditive = attackAdditive;
        this.defenseMult = defenseMult;
        this.speedMult = speedMult;
        this.forageMult = forageMult;
        this.descKey = descKey;
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                1f, false, 1f, 1f, 1f, null);
    }

    public AntSubtypeSlot getSlot() {
        return slot;
    }

    public int getDigit() {
        return digit;
    }

    public Upgrade getRequiredUpgrade() {
        return requiredUpgrade;
    }

    public String getSpriteSpeciesDir() {
        return spriteSpeciesDir;
    }

    public String getSpriteFolder() {
        return spriteFolder;
    }

    public float getAttackMult() {
        return attackMult;
    }

    public boolean isAttackAdditive() {
        return attackAdditive;
    }

    public float getDefenseMult() {
        return defenseMult;
    }

    public float getSpeedMult() {
        return speedMult;
    }

    public float getForageMult() {
        return forageMult;
    }

    public String getDescKey() {
        return descKey;
    }

    public String getDesc() {
        return descKey != null ? LanguageStrings.get(descKey) : "";
    }

    public boolean isNone() {
        return requiredUpgrade == null && digit == 1;
    }

    public boolean hasSprite() {
        return spriteSpeciesDir != null && spriteFolder != null;
    }
}
