package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyDeathServiceTest {

    private Colony colony;
    private ColonyDeathService service;

    @BeforeEach
    void setUp() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Test", true);
        dynasty.addColony(colony);
        service = colony.getDeathService();
    }

    @Test
    void recordsDeathWithStableId() {
        service.recordDeath(DeathCause.STARVATION, colony);
        assertEquals(1, service.getDeathStatistics().get(DeathCause.STARVATION));
        assertEquals(1, colony.getDynasty().getGlobalDeathStatistics().get(DeathCause.STARVATION));
    }

    @Test
    void migratesLegacyEnglishKeysOnLoad() {
        service.loadDeathStatistics(Map.of("Old Age", 3, "Combat", 2));
        assertEquals(3, service.getDeathStatistics().get(DeathCause.OLD_AGE));
        assertEquals(2, service.getDeathStatistics().get(DeathCause.CONFLICT));
    }

    @Test
    void unknownCauseMapsToOther() {
        service.recordDeath("mystery", colony);
        assertEquals(1, service.getDeathStatistics().get(DeathCause.OTHER));
    }

    @Test
    void resetRestoresZeroCountsForAllKnownCauses() {
        service.recordDeath(DeathCause.ILLNESS, colony);
        service.resetDeathStatistics();
        for (String cause : service.getDeathStatistics().keySet()) {
            assertEquals(0, service.getDeathStatistics().get(cause));
        }
        assertTrue(service.getDeathStatistics().containsKey(DeathCause.ILLNESS));
    }
}
