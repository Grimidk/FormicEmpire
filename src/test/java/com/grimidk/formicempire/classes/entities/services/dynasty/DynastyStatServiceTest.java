package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyStatServiceTest {

    @Test
    void totalQueensSumsAcrossColonies() {
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony a = new Colony(1, "A", true);
        Colony b = new Colony(2, "B", true);
        dynasty.addColony(a);
        dynasty.addColony(b);
        a.getQueens().add(new Ant(a, GameConstants.TYPE_QUEEN));
        a.getQueens().add(new Ant(a, GameConstants.TYPE_QUEEN));
        b.getQueens().add(new Ant(b, GameConstants.TYPE_QUEEN));

        assertEquals(3, dynasty.getStatService().getTotalQueens(dynasty));
    }

    @Test
    void globalBirthRateUsesLayersAndLayingRate() {
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_LAYER);
        Colony colony = new Colony(1, "A", true);
        dynasty.addColony(colony);
        colony.setAssignedRoleCount(GameConstants.ROLE_LAYER, 2);

        int daily = dynasty.getStatService().getGlobalBirthRateDaily(dynasty);
        float laying = colony.getStatsService().getLayingRate(colony);
        assertTrue(laying > 0f);
        assertEquals((int) (2 * laying * 24), daily);
        assertTrue(daily > 0);
    }
}
