package com.grimidk.formicempire.classes.constants.critter;

import java.util.List;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public class Species extends Constant {
    private final CritterClass critterClass;
    private final String scientificNameKey;
    private final float baseHealth;
    private final float baseRegen;
    private final float baseAttack;
    private final float baseAttackSpeed;
    private final float baseDefense;
    private final float baseSpeed;
    private final ImageIcon sprite;
    private final String spriteFolder;
    private final String bodySpriteFile;
    private final boolean legWalkCycle;
    private final int legFrameCount;
    private final int antennaFrameCount;
    private final BugRole bugRole;
    private final List<Skill> baseSkills;

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite) {
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, null, null, false, null, List.of());
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, BugRole bugRole) {
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, null, null, false, bugRole, List.of());
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, BugRole bugRole, List<Skill> baseSkills) {
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, null, null, false, bugRole, baseSkills);
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, String spriteFolder, String bodySpriteFile, boolean legWalkCycle,
            BugRole bugRole) {
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, spriteFolder, bodySpriteFile, legWalkCycle, bugRole, List.of());
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, String spriteFolder, String bodySpriteFile, boolean legWalkCycle,
            BugRole bugRole, List<Skill> baseSkills) {
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, spriteFolder, bodySpriteFile, legWalkCycle, bugRole, baseSkills,
                legWalkCycle ? GameNumbers.ANT_LEG_FRAME_COUNT : 1, 0);
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, String spriteFolder, String bodySpriteFile, boolean legWalkCycle,
            BugRole bugRole, List<Skill> baseSkills, int legFrameCount, int antennaFrameCount) {
        super(id, name, icon);
        this.critterClass = critterClass;
        this.scientificNameKey = scientificNameKey;
        this.baseHealth = baseHealth;
        this.baseRegen = baseRegen;
        this.baseAttack = baseAttack;
        this.baseAttackSpeed = baseAttackSpeed;
        this.baseDefense = GameNumbers.clampDefensePercent(baseDefense);
        this.baseSpeed = baseSpeed;
        this.sprite = sprite;
        this.spriteFolder = spriteFolder;
        this.bodySpriteFile = bodySpriteFile;
        this.legWalkCycle = legWalkCycle;
        this.legFrameCount = Math.max(1, legFrameCount);
        this.antennaFrameCount = Math.max(0, antennaFrameCount);
        this.bugRole = bugRole;
        this.baseSkills = baseSkills == null || baseSkills.isEmpty() ? List.of() : List.copyOf(baseSkills);
    }

    public CritterClass getCritterClass() {
        return critterClass;
    }

    public BugRole getBugRole() {
        return bugRole;
    }

    public boolean hasBugRole(BugRole role) {
        return role != null && role == bugRole;
    }

    public boolean isPet() {
        return hasBugRole(BugRole.PET);
    }

    public String getScientificName() {
        return LanguageStrings.get(scientificNameKey);
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public float getBaseRegen() {
        return baseRegen;
    }

    public float getBaseAttack() {
        return baseAttack;
    }

    public float getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public float getBaseDefense() {
        return baseDefense;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public ImageIcon getSprite() {
        return sprite;
    }

    public boolean hasComposedSprite() {
        return spriteFolder != null && !spriteFolder.isEmpty()
                && bodySpriteFile != null && !bodySpriteFile.isEmpty();
    }

    public String getSpriteFolder() {
        return spriteFolder;
    }

    public String getBodySpriteFile() {
        return bodySpriteFile;
    }

    public boolean hasLegWalkCycle() {
        return legWalkCycle;
    }

    public int getLegFrameCount() {
        return legFrameCount;
    }

    public int getAntennaFrameCount() {
        return antennaFrameCount;
    }

    public boolean hasAntennaCycle() {
        return antennaFrameCount > 1;
    }

    public List<Skill> getBaseSkills() {
        return baseSkills;
    }
}
