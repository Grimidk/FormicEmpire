package com.grimidk.formicempire.classes.constants.critter.ant;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class AntSubtype extends Constant {
    /** Digit used by empty/"nothing" subtype slots in hatch-rate maps. */
    public static final int DIGIT_NONE = 1;

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
    private final float regenMult;
    private final String descKey;
    private final Skill grantedSkill;
    private final Skill replacesSkill;

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, 1f, nameKey + "_DESC",
                null, null, icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            float regenMult, ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, regenMult, nameKey + "_DESC",
                null, null, icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            Skill grantedSkill, Skill replacesSkill, ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, 1f, nameKey + "_DESC",
                grantedSkill, replacesSkill, icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            float regenMult, Skill grantedSkill, Skill replacesSkill, ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, regenMult, nameKey + "_DESC",
                grantedSkill, replacesSkill, icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            String descKey, ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, 1f, descKey, null, null, icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            float regenMult, String descKey, ImageIcon icon) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                attackMult, attackAdditive, defenseMult, speedMult, forageMult, regenMult, descKey,
                null, null, icon);
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder,
            float attackMult, boolean attackAdditive, float defenseMult, float speedMult, float forageMult,
            float regenMult, String descKey, Skill grantedSkill, Skill replacesSkill, ImageIcon icon) {
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
        this.regenMult = regenMult;
        this.descKey = descKey;
        this.grantedSkill = grantedSkill;
        this.replacesSkill = replacesSkill;
    }

    public AntSubtype(int id, String nameKey, AntSubtypeSlot slot, int digit, Upgrade requiredUpgrade,
            String spriteSpeciesDir, String spriteFolder) {
        this(id, nameKey, slot, digit, requiredUpgrade, spriteSpeciesDir, spriteFolder,
                1f, false, 1f, 1f, 1f, 1f, null, null, null, null);
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

    public float getRegenMult() {
        return regenMult;
    }

    public String getDescKey() {
        return descKey;
    }

    public String getDesc() {
        return descKey != null ? LanguageStrings.get(descKey) : "";
    }

    public Skill getGrantedSkill() {
        return grantedSkill;
    }

    public Skill getReplacesSkill() {
        return replacesSkill;
    }

    public boolean isNone() {
        return requiredUpgrade == null && digit == DIGIT_NONE;
    }

    public boolean hasSprite() {
        return spriteSpeciesDir != null && spriteFolder != null;
    }
}
