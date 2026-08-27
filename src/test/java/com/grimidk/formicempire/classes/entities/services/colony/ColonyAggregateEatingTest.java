package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyAggregateEatingTest {

    @Test
    void aggregateEatingActivatesAboveThreshold() {
        Colony small = new Colony(1, "Small", true);
        assertFalse(ColonyPopulationService.shouldUseAggregateEating(small));

        Colony large = new Colony(2, "Large", true);
        for (int i = 0; i < GameNumbers.EATING_AGGREGATE_ANT_THRESHOLD + 1; i++) {
            large.getWorkers().add(new Ant(large, GameConstants.TYPE_WORKER));
        }
        assertTrue(ColonyPopulationService.shouldUseAggregateEating(large));
    }

    @Test
    void totalConsumptionCacheAvoidsRescanUntilPopulationChanges() {
        Colony colony = new Colony(3, "Cache", true);
        for (int i = 0; i < 5; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        int first = colony.getTotalConsumption();
        int second = colony.getTotalConsumption();
        assertEquals(first, second);

        colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        int afterGrowth = colony.getTotalConsumption();
        assertTrue(afterGrowth >= first);
    }

    @Test
    void sumCollectingPowerFromRoleCountUsesRoleCountWhenRateAvailable() {
        Dynasty dynasty = new Dynasty(4, "CollectDynasty", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(4, "Collect", true);
        colony.setDynasty(dynasty);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        int fromRoleCount = AntSubtypeService.sumCollectingPowerFromRoleCount(colony, 50);
        assertEquals(50, fromRoleCount);
    }
}
