package com.grimidk.formicempire.classes.entities.services.world;

import java.util.ArrayList;
import java.util.List;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

/**
 * One ant in an ongoing creature battle, with persistent focus targets for multi-hit skills.
 * War quotas may temporarily assign a battle role; {@link #restorePeaceRole()} undoes that.
 * Battle HP is tracked separately so ants with 0 colony HP (no Skeleton yet) can still fight.
 */
final class WarBattleParticipant {
    private final Ant ant;
    private final BattleLine battleLine;
    private final AntRole previousRole;
    private final boolean roleOverridden;
    private final List<WarBattleParticipant> focusTargets = new ArrayList<>();
    private float battleMaxHealth;
    private float battleHealth;

    WarBattleParticipant(Ant ant, BattleLine battleLine, AntRole previousRole, boolean roleOverridden) {
        this.ant = ant;
        this.battleLine = battleLine != null ? battleLine : GameConstants.BATTLE_LINE_INFANTRY;
        this.previousRole = previousRole;
        this.roleOverridden = roleOverridden;
        initBattleHealth();
    }

    private void initBattleHealth() {
        float max = ant != null ? ant.getMaxHealth() : 0f;
        float current = ant != null ? ant.getHealth() : 0f;
        if (max <= 0f) {
            max = GameNumbers.MILITARY_BASELINE_HEALTH;
            current = max;
        } else if (current <= 0f && ant != null && ant.isAlive()) {
            current = max;
        }
        this.battleMaxHealth = max;
        this.battleHealth = Math.min(current, max);
    }

    Ant getAnt() {
        return ant;
    }

    BattleLine getBattleLine() {
        return battleLine;
    }

    List<WarBattleParticipant> getFocusTargets() {
        return focusTargets;
    }

    float getBattleHealth() {
        return battleHealth;
    }

    float getBattleMaxHealth() {
        return battleMaxHealth;
    }

    /** Applies damage to battle HP; returns remaining battle HP. */
    float applyBattleDamage(float damage) {
        if (damage <= 0f) {
            return battleHealth;
        }
        battleHealth = Math.max(0f, battleHealth - damage);
        if (ant != null && ant.getMaxHealth() > 0f) {
            ant.setHealth(Math.max(0, Math.round(battleHealth)));
        }
        return battleHealth;
    }

    boolean isAlive() {
        return ant != null && ant.isAlive() && battleHealth > 0f;
    }

    boolean isQueen() {
        return ant != null && ant.getAntType() == GameConstants.TYPE_QUEEN;
    }

    boolean isDefenderRole() {
        return ant != null && ant.getRole() == GameConstants.ROLE_DEFENDER;
    }

    void restorePeaceRole() {
        if (roleOverridden && ant != null && ant.isAlive()) {
            ant.setRole(previousRole);
        }
    }
}
