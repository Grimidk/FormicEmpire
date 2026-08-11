package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyStatsServiceResearchRateTest {

    @Test
    void passiveLabAddsVirtualResearchersToDailyRate() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setAssignedRoleCount(GameConstants.ROLE_RESEARCHER, 1);
        colony.unlockBuilding(GameUnlocks.PASSIVE_LAB);

        ColonyStatsService stats = colony.getStatsService();
        int speed = stats.getResearchSpeed(colony);
        assertEquals(1 + 1, stats.getEffectiveResearcherCount(colony));
        assertEquals((1 + 1) * speed * 24, stats.getDailyResearchPoints(colony));
        assertEquals(stats.getDailyResearchPoints(colony), dynasty.getStatService().getGlobalResearchRateDaily(dynasty));
    }

    @Test
    void assistantsContributeAtDivisorEfficiency() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_ASSISTANT);
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setAssignedRoleCount(GameConstants.ROLE_RESEARCHER, 0);
        colony.setAssignedRoleCount(GameConstants.ROLE_ASSISTANT, GameNumbers.RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR);

        ColonyStatsService stats = colony.getStatsService();
        int speed = stats.getResearchSpeed(colony);
        assertEquals(speed * 24, stats.getDailyResearchPoints(colony));
    }

    @Test
    void multiColonyDailyRateSumsIndependently() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
        Colony a = new Colony(1, "A", true);
        Colony b = new Colony(2, "B", true);
        dynasty.addColony(a);
        dynasty.addColony(b);
        a.setAssignedRoleCount(GameConstants.ROLE_RESEARCHER, 1);
        b.setAssignedRoleCount(GameConstants.ROLE_RESEARCHER, 1);

        int expected = a.getStatsService().getDailyResearchPoints(a)
                + b.getStatsService().getDailyResearchPoints(b);
        assertEquals(expected, dynasty.getStatService().getGlobalResearchRateDaily(dynasty));
        assertEquals(2 * a.getStatsService().getResearchSpeed(a) * 24, expected);
    }
}
