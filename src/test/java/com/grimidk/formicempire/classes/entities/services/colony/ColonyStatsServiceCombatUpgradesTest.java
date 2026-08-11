package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColonyStatsServiceCombatUpgradesTest {

    private ColonyStatsService stats;
    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    void setup() {
        stats = new ColonyStatsService();
        dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test Colony", true);
        dynasty.addColony(colony);
    }

    @Test
    void healthBonusesStackAdditivelyOnSkeletonBaseline() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        assertEquals(GameNumbers.MILITARY_BASELINE_HEALTH, stats.getBaseHealth(colony));

        dynasty.unlockUpgrade(GameUnlocks.STAT_HEALTH_1);
        assertEquals(150, stats.getBaseHealth(colony));

        dynasty.unlockUpgrade(GameUnlocks.STAT_HEALTH_2);
        assertEquals(230, stats.getBaseHealth(colony));
    }

    @Test
    void attackBonusesStackAdditivelyOnAcidBaseline() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        assertEquals(GameNumbers.MILITARY_BASELINE_ATTACK, stats.getBaseAttack(colony));

        dynasty.unlockUpgrade(GameUnlocks.STAT_ATTACK_1);
        assertEquals(13, stats.getBaseAttack(colony));

        dynasty.unlockUpgrade(GameUnlocks.STAT_ATTACK_2);
        assertEquals(18, stats.getBaseAttack(colony));
    }

    @Test
    void defenseBonusesAreFlatAndStack() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_DEFENSE_1);
        assertEquals(5, stats.getBaseDefense(colony));

        dynasty.unlockUpgrade(GameUnlocks.STAT_DEFENSE_2);
        assertEquals(10, stats.getBaseDefense(colony));
    }

    @Test
    void attackSpeedFlatBonusAppliesAfterAcidBaseline() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ATTACK_1);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ATTACK_SPEED_1);
        assertEquals(2, stats.getBaseAttackSpeed(colony));
    }

    @Test
    void workerAppliesColonyDefenseAndAttackSpeedBonuses() {
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.STAT_HEALTH_1);
        dynasty.unlockUpgrade(GameUnlocks.STAT_DEFENSE_1);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ATTACK_1);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ATTACK_SPEED_1);

        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        assertEquals(5f, worker.getDefense(), 0.0001f);
        assertEquals(2, worker.getAttackSpeed(), 0.0001f);
        assertEquals(150, worker.getMaxHealth());
        assertEquals(13, worker.getAttack());
    }
}
