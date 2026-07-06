package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
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
    void fireVenomQuadruplesDamage() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FIREVENOM);
        assertEquals(4f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
        Colony colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);
        assertEquals(40, new ColonyStatsService().getBaseAttack(colony));
    }

    @Test
    void fireAndStingingStackAdditivelyToEight() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FIREVENOM, GameUnlocks.ASSIMILATED_STINGING);
        assertEquals(8f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fireAndDeadlySynergyReplacesFourPlusFourWithSixteen() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FIREVENOM, GameUnlocks.ASSIMILATED_DEADLYVENOM);
        assertEquals(16f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fireDeadlySynergyPlusStingingIsTwenty() {
        Dynasty dynasty = dynastyWith(
                GameUnlocks.ASSIMILATED_FIREVENOM,
                GameUnlocks.ASSIMILATED_DEADLYVENOM,
                GameUnlocks.ASSIMILATED_STINGING);
        assertEquals(20f, ColonyStatsService.getAssimilatedDamageMultiplier(dynasty), 0.0001f);
    }

    @Test
    void fastBiteTriplesAttackSpeed() {
        Dynasty dynasty = dynastyWith(GameUnlocks.ASSIMILATED_FASTBITE);
        assertEquals(3f, ColonyStatsService.getAssimilatedAttackSpeedMultiplier(dynasty), 0.0001f);
        Colony colony = new Colony(2, "C", true);
        colony.setDynasty(dynasty);
        assertEquals(3, new ColonyStatsService().getBaseAttackSpeed(colony));
    }
}
