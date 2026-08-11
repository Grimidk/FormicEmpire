package com.grimidk.formicempire.classes.constants.critter;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;

public class Skill extends Constant {
    private final float accuracyMult;
    private final float damageMult;
    private final int targetCount;
    private final boolean isAttack;
    private final boolean passive;
    private final float laneDamageBonus;
    private AntRole requiredRole;
    private AntSubtype requiredSubtype;
    private BattleLine battleLine;
    private boolean sacrificesSelf;
    private Skill replacesSkill;

    public Skill(int id, String nameKey, float accuracyMult, float damageMult, int targetCount, boolean isAttack,
            ImageIcon icon) {
        this(id, nameKey, accuracyMult, damageMult, targetCount, isAttack, false, 0f, null, icon);
    }

    public Skill(int id, String nameKey, float accuracyMult, float damageMult, int targetCount, boolean isAttack,
            AntRole requiredRole, ImageIcon icon) {
        this(id, nameKey, accuracyMult, damageMult, targetCount, isAttack, false, 0f, requiredRole, icon);
    }

    public Skill(int id, String nameKey, float accuracyMult, float damageMult, int targetCount, boolean isAttack,
            boolean passive, float laneDamageBonus, AntRole requiredRole, ImageIcon icon) {
        super(id, nameKey, icon);
        this.accuracyMult = accuracyMult;
        this.damageMult = damageMult;
        this.targetCount = targetCount;
        this.isAttack = isAttack;
        this.passive = passive;
        this.laneDamageBonus = Math.max(0f, laneDamageBonus);
        this.requiredRole = requiredRole;
    }

    public float getAccuracyMult() {
        return accuracyMult;
    }

    public float getDamageMult() {
        return damageMult;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public boolean isAttack() {
        return isAttack;
    }

    public boolean isPassive() {
        return passive;
    }

    public float getLaneDamageBonus() {
        return laneDamageBonus;
    }

    public AntRole getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(AntRole requiredRole) {
        this.requiredRole = requiredRole;
    }

    public AntSubtype getRequiredSubtype() {
        return requiredSubtype;
    }

    public void setRequiredSubtype(AntSubtype requiredSubtype) {
        this.requiredSubtype = requiredSubtype;
    }

    public BattleLine getBattleLine() {
        return battleLine;
    }

    public void setBattleLine(BattleLine battleLine) {
        this.battleLine = battleLine;
    }

    public boolean sacrificesSelf() {
        return sacrificesSelf;
    }

    public void setSacrificesSelf(boolean sacrificesSelf) {
        this.sacrificesSelf = sacrificesSelf;
    }

    public Skill getReplacesSkill() {
        return replacesSkill;
    }

    public void setReplacesSkill(Skill replacesSkill) {
        this.replacesSkill = replacesSkill;
    }
}
