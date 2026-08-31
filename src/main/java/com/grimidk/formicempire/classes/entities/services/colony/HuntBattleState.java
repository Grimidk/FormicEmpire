package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
public final class HuntBattleState {
    private final Colony colony;
    private final int contextId;
    private final Species species;
    private final List<HuntBattleParticipant> hunters = new ArrayList<>();
    private final List<HuntBattleFallenBody> fallenBodies = new ArrayList<>();
    private final int orbitSlotCount;
    private float bugHealth;
    private final float bugMaxHealth;
    private final float bugAttack;
    private final float bugDefense;
    private final int bugAttackSpeed;
    private int tickIndex;
    private int focusTargetIndex;

    public HuntBattleState(Colony colony, int contextId, Species species, List<HuntBattleParticipant> hunters,
            float bugMaxHealth, float bugAttack, float bugDefense, int bugAttackSpeed) {
        this.colony = colony;
        this.contextId = contextId;
        this.species = species;
        this.hunters.addAll(hunters);
        this.orbitSlotCount = hunters.size();
        for (int i = 0; i < hunters.size(); i++) {
            hunters.get(i).setOrbitSlot(i);
        }
        this.bugMaxHealth = bugMaxHealth;
        this.bugHealth = bugMaxHealth;
        this.bugAttack = bugAttack;
        this.bugDefense = bugDefense;
        this.bugAttackSpeed = bugAttackSpeed;
    }

    public static HuntBattleState restore(Colony colony, int contextId, Species species,
            List<HuntBattleParticipant> hunters, float bugHealth, float bugMaxHealth, float bugAttack,
            float bugDefense, int bugAttackSpeed, int tickIndex, int focusTargetIndex) {
        HuntBattleState state = new HuntBattleState(
                colony,
                contextId,
                species,
                hunters,
                bugMaxHealth,
                bugAttack,
                bugDefense,
                bugAttackSpeed);
        state.bugHealth = Math.max(0f, Math.min(bugMaxHealth, bugHealth));
        state.tickIndex = Math.max(0, tickIndex);
        state.focusTargetIndex = focusTargetIndex;
        return state;
    }

    public Colony getColony() {
        return colony;
    }

    public int getContextId() {
        return contextId;
    }

    public Species getSpecies() {
        return species;
    }

    public int getOrbitSlotCount() {
        return orbitSlotCount;
    }

    public List<HuntBattleFallenBody> getFallenBodies() {
        return Collections.unmodifiableList(fallenBodies);
    }

    void recordFallenBody(HuntBattleParticipant victim, float bodyRotationDegrees) {
        if (victim == null || victim.getAnt() == null) {
            return;
        }
        Ant ant = victim.getAnt();
        fallenBodies.add(new HuntBattleFallenBody(
                ant.getAntType(),
                ant.getSubtypeProfile(),
                victim.getOrbitSlot(),
                bodyRotationDegrees));
    }

    public List<HuntBattleParticipant> getHunters() {
        return Collections.unmodifiableList(hunters);
    }

    public List<HuntBattleParticipant> livingHunters() {
        List<HuntBattleParticipant> living = new ArrayList<>();
        for (HuntBattleParticipant hunter : hunters) {
            if (hunter.isAlive()) {
                living.add(hunter);
            }
        }
        return living;
    }

    public float getBugHealth() {
        return bugHealth;
    }

    public float getBugMaxHealth() {
        return bugMaxHealth;
    }

    public float getBugAttack() {
        return bugAttack;
    }

    public float getBugDefense() {
        return bugDefense;
    }

    public int getBugAttackSpeed() {
        return bugAttackSpeed;
    }

    public int getTickIndex() {
        return tickIndex;
    }

    public int getFocusTargetIndex() {
        return focusTargetIndex;
    }

    public void advanceTick() {
        tickIndex++;
    }

    public boolean isBugAlive() {
        return bugHealth > 0f;
    }

    public float applyBugDamage(float damage) {
        if (damage <= 0f) {
            return bugHealth;
        }
        bugHealth = Math.max(0f, bugHealth - damage);
        return bugHealth;
    }

    public HuntBattleParticipant getFocusTarget() {
        List<HuntBattleParticipant> living = livingHunters();
        if (living.isEmpty()) {
            return null;
        }
        focusTargetIndex = focusTargetIndex % living.size();
        if (focusTargetIndex < 0) {
            focusTargetIndex += living.size();
        }
        return living.get(focusTargetIndex);
    }

    public void rotateFocusTarget() {
        focusTargetIndex++;
    }

    public float getBugHealthRatio() {
        if (bugMaxHealth <= 0f) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, bugHealth / bugMaxHealth));
    }

    public List<Ant> livingHunterAnts() {
        List<Ant> ants = new ArrayList<>();
        for (HuntBattleParticipant hunter : livingHunters()) {
            if (hunter.getAnt() != null) {
                ants.add(hunter.getAnt());
            }
        }
        return ants;
    }

    public Ant getFocusAnt() {
        HuntBattleParticipant focus = getFocusTarget();
        return focus != null ? focus.getAnt() : null;
    }

    public int getFocusOrbitIndex() {
        HuntBattleParticipant focus = getFocusTarget();
        return focus != null ? focus.getOrbitSlot() : 0;
    }
}
