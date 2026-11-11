package com.grimidk.formicempire.classes.constants.critter;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

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
    private final Set<BugRole> bugRoles;
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
        this(id, name, critterClass, scientificNameKey, baseHealth, baseRegen, baseAttack, baseAttackSpeed,
                baseDefense, baseSpeed, icon, sprite, spriteFolder, bodySpriteFile, legWalkCycle, bugRole, baseSkills,
                legFrameCount, antennaFrameCount, (BugRole[]) null);
    }

    public Species(int id, String name, CritterClass critterClass, String scientificNameKey, float baseHealth,
            float baseRegen, float baseAttack, float baseAttackSpeed, float baseDefense, float baseSpeed,
            ImageIcon icon, ImageIcon sprite, String spriteFolder, String bodySpriteFile, boolean legWalkCycle,
            BugRole bugRole, List<Skill> baseSkills, int legFrameCount, int antennaFrameCount, BugRole... additionalRoles) {
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
        if (bugRole != null) {
            EnumSet<BugRole> roles = EnumSet.of(bugRole);
            if (additionalRoles != null) {
                for (BugRole r : additionalRoles) {
                    if (r != null) {
                        roles.add(r);
                    }
                }
            }
            this.bugRoles = Collections.unmodifiableSet(roles);
        } else {
            this.bugRoles = Collections.emptySet();
        }
        this.baseSkills = baseSkills == null || baseSkills.isEmpty() ? List.of() : List.copyOf(baseSkills);
    }

    public CritterClass getCritterClass() {
        return critterClass;
    }

    public BugRole getBugRole() {
        return bugRole;
    }

    public Set<BugRole> getBugRoles() {
        return bugRoles;
    }

    public boolean hasBugRole(BugRole role) {
        return role != null && bugRoles.contains(role);
    }

    public boolean isPet() {
        return hasBugRole(BugRole.PET);
    }

    public boolean isHunt() {
        return hasBugRole(BugRole.HUNT);
    }

    public boolean isInvasion() {
        return hasBugRole(BugRole.INVASION);
    }

    public boolean isBoss() {
        return hasBugRole(BugRole.BOSS);
    }

    public boolean isParasite() {
        return hasBugRole(BugRole.PARASITE);
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
        if (sprite != null && sprite != GameConstants.ICON_UNKNOWN) {
            return sprite;
        }
        return GameConstants.SPRITE_UNKNOWN != null ? GameConstants.SPRITE_UNKNOWN : GameConstants.ICON_UNKNOWN;
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
