package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

public final class HuntBattleParticipant {
    private final Ant ant;
    private float battleMaxHealth;
    private float battleHealth;
    private int orbitSlot;

    HuntBattleParticipant(Ant ant) {
        this.ant = ant;
        initBattleHealth();
    }

    public int getOrbitSlot() {
        return orbitSlot;
    }

    void setOrbitSlot(int orbitSlot) {
        this.orbitSlot = orbitSlot;
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

    public Ant getAnt() {
        return ant;
    }

    float getBattleHealth() {
        return battleHealth;
    }

    float getBattleMaxHealth() {
        return battleMaxHealth;
    }

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

    void restoreBattleHealth(float battleMaxHealth, float battleHealth) {
        this.battleMaxHealth = Math.max(0f, battleMaxHealth);
        this.battleHealth = Math.max(0f, Math.min(this.battleMaxHealth, battleHealth));
        if (ant != null && ant.getMaxHealth() > 0f) {
            ant.setHealth(Math.max(0, Math.round(this.battleHealth)));
        }
    }

    static HuntBattleParticipant restore(Ant ant, float battleMaxHealth, float battleHealth) {
        HuntBattleParticipant participant = new HuntBattleParticipant(ant);
        participant.restoreBattleHealth(battleMaxHealth, battleHealth);
        return participant;
    }
}
