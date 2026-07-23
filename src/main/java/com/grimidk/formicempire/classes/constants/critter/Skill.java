package com.grimidk.formicempire.classes.constants.critter;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;

public class Skill extends Constant {
    private final float accuracyMult;
    private final float damageMult;
    /** When non-null, colony must own this upgrade for the skill to be available. */
    private final Upgrade requiredUpgrade;
    /** When non-null, only ants currently assigned this role may use the skill. */
    private AntRole requiredRole;

    public Skill(int id, String nameKey, float accuracyMult, float damageMult, ImageIcon icon) {
        this(id, nameKey, accuracyMult, damageMult, null, null, icon);
    }

    public Skill(int id, String nameKey, float accuracyMult, float damageMult, Upgrade requiredUpgrade,
            ImageIcon icon) {
        this(id, nameKey, accuracyMult, damageMult, requiredUpgrade, null, icon);
    }

    public Skill(int id, String nameKey, float accuracyMult, float damageMult, Upgrade requiredUpgrade,
            AntRole requiredRole, ImageIcon icon) {
        super(id, nameKey, icon);
        this.accuracyMult = accuracyMult;
        this.damageMult = damageMult;
        this.requiredUpgrade = requiredUpgrade;
        this.requiredRole = requiredRole;
    }

    public float getAccuracyMult() {
        return accuracyMult;
    }

    public float getDamageMult() {
        return damageMult;
    }

    public Upgrade getRequiredUpgrade() {
        return requiredUpgrade;
    }

    public AntRole getRequiredRole() {
        return requiredRole;
    }

    /** Wired from {@code GameConstants} after roles exist (avoids static init order issues). */
    public void setRequiredRole(AntRole requiredRole) {
        this.requiredRole = requiredRole;
    }
}
