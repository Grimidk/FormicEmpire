package com.grimidk.formicempire.classes.entities.services.dynasty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class DynastyDiplomacyVisibilityTest {

    @Test
    void zeroPopulationDynastyIsNotDiplomaticallyContactable() {
        Dynasty dynasty = new Dynasty(1, "Empty Dynasty", false, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Ruin", false);
        dynasty.addColony(colony);

        assertFalse(dynasty.isActiveForDiplomacy());
        assertFalse(DynastyDiplomacyService.isDiplomaticallyContactable(dynasty));
    }

    @Test
    void depopulatedDynastyIsMarkedDefeatedButRetainedOnWorld() {
        Dynasty dynasty = new Dynasty(2, "Fallen Dynasty", false, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(2, "Capital", false);
        dynasty.addColony(colony);

        World world = new World();
        world.getDynastys().add(dynasty);

        new DynastyDeathService().processDynastyDeaths(world);

        assertTrue(dynasty.isDefeated());
        assertTrue(world.getDynastys().contains(dynasty));
        assertFalse(DynastyDiplomacyService.isDiplomaticallyContactable(dynasty));
    }
}
