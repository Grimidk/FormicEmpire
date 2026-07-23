package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColonyStatsServiceAssimilationTest {

    private static Dynasty dynastyWith(Upgrade... upgrades) {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        for (Upgrade u : upgrades) {
            dynasty.unlockUpgrade(u);
        }
        return dynasty;
    }

    @Test
    void fireVenomAddsFiftyPercentDamage() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FIREVENOM);
        assertEquals(1.5f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
        Colony colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);
        assertEquals(15, new ColonyStatsService().getBaseAttack(colony));
    }

    @Test
    void fireVenomUnaffectedByStingingAssimilation() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FIREVENOM, GameUnlocks.ASSIMILATED_STINGING);
        assertEquals(1.5f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fireAndDeadlyStackAdditivelyToDoubleWithoutSuperVenom() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FIREVENOM, GameUnlocks.ASSIMILATED_DEADLYVENOM);
        assertEquals(2f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void superVenomReplacesFireAndDeadlyStackingWithTriple() {
        Dynasty dynasty = dynastyWith(
                GameUnlocks.ABILITY_SYNERGY,
                GameUnlocks.ASSIMILATED_FIREVENOM,
                GameUnlocks.ASSIMILATED_DEADLYVENOM);
        DynastySynergyService.refreshUnlocked(dynasty);
        assertEquals(3f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fireAndDeadlySynergyReplacesFiftyPlusFiftyWithTwoHundredBonus() {
        Dynasty dynasty = dynastyWith(
                GameUnlocks.ABILITY_SYNERGY,
                GameUnlocks.ASSIMILATED_FIREVENOM,
                GameUnlocks.ASSIMILATED_DEADLYVENOM,
                GameUnlocks.SYNERGY_SUPER_VENOM);
        assertEquals(3f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fireDeadlySynergyUnaffectedByStinging() {
        Dynasty dynasty = dynastyWith(
                GameUnlocks.ABILITY_SYNERGY,
                GameUnlocks.ASSIMILATED_FIREVENOM,
                GameUnlocks.ASSIMILATED_DEADLYVENOM,
                GameUnlocks.ASSIMILATED_STINGING,
                GameUnlocks.SYNERGY_SUPER_VENOM);
        assertEquals(3f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fastBiteDoublesAttackSpeed() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FASTBITE);
        assertEquals(2f, ColonyStatsService.getAssimilatedAttackSpeedMultiplier(dynasty), 0.0001f);
        Colony colony = new Colony(2, "C", true);
        colony.setDynasty(dynasty);
        assertEquals(2, new ColonyStatsService().getBaseAttackSpeed(colony));
    }
}
