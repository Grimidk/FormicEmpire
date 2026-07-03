package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonySummarizationServiceTest {

    private Colony colony;
    private ColonySummarizationService summarizationService;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test", false);
        dynasty.addColony(colony);
        colony.setActive(false);
        colony.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        colony.setAssignedRoleCount(GameConstants.ROLE_FORAGER, 5);
        summarizationService = colony.getSummarizationService();
    }

    @Test
    void hourlyLiteCanRunWithoutException() {
        summarizationService.runHourlyLite(colony, GameConstants.BIOME_PLAINS);
        assertTrue(colony.getAge() >= 0);
    }

    @Test
    void dailyLiteCanRunWithoutException() {
        colony.setAge(7);
        summarizationService.runDailyLite(colony);
        assertTrue(colony.getRank() != null);
    }
}
