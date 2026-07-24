package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyCombatCapacityTest {

    @Test
    void combatCapacityStartsAtBaseWithoutCommanders() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);

        assertEquals(GameNumbers.COMBAT_CAPACITY_BASE, dynasty.getCombatCapacity());
        assertFalse(dynasty.getStatService().hasAssignedCommanders(dynasty));
    }

    @Test
    void assigningCommanderOnWarQuotaRaisesCombatCapacity() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        colony.setWarAssignedRoleCount(GameConstants.ROLE_COMMANDER, 1);

        assertTrue(GameConstants.isActiveMilitaryRole(GameConstants.ROLE_COMMANDER));
        assertTrue(GameConstants.isWarEconomyExclusiveRole(GameConstants.ROLE_COMMANDER));
        assertTrue(dynasty.getStatService().hasAssignedCommanders(dynasty));
        assertEquals(GameNumbers.COMBAT_CAPACITY_WITH_COMMANDER, dynasty.getCombatCapacity());
    }

    @Test
    void commanderCappedAtOnePerColonyAndRequiresSpareQueen() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);

        assertEquals(0, colony.getMaxAssignableCommanders());
        colony.setWarAssignedRoleCount(GameConstants.ROLE_COMMANDER, 5);
        assertEquals(0, colony.getWarAssignedRoleCount(GameConstants.ROLE_COMMANDER));

        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        assertEquals(0, colony.getMaxAssignableCommanders());

        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        assertEquals(GameNumbers.COMMANDER_MAX_PER_COLONY, colony.getMaxAssignableCommanders());

        colony.setWarAssignedRoleCount(GameConstants.ROLE_COMMANDER, 3);
        assertEquals(1, colony.getWarAssignedRoleCount(GameConstants.ROLE_COMMANDER));
    }

    @Test
    void multipleColonyCommandersDoNotStackCombatCapacity() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony a = new Colony(1, "Prime", true);
        Colony b = new Colony(2, "Outpost", false);
        dynasty.addColony(a);
        dynasty.addColony(b);
        a.setDynasty(dynasty);
        b.setDynasty(dynasty);
        a.getQueens().add(new Ant(a, GameConstants.TYPE_QUEEN));
        a.getQueens().add(new Ant(a, GameConstants.TYPE_QUEEN));
        b.getQueens().add(new Ant(b, GameConstants.TYPE_QUEEN));
        b.getQueens().add(new Ant(b, GameConstants.TYPE_QUEEN));
        a.setWarAssignedRoleCount(GameConstants.ROLE_COMMANDER, 1);
        b.setWarAssignedRoleCount(GameConstants.ROLE_COMMANDER, 1);

        assertEquals(GameNumbers.COMBAT_CAPACITY_WITH_COMMANDER, dynasty.getCombatCapacity());
    }

    @Test
    void multiQueenColonyDetected() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(1, "Prime", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);

        assertFalse(dynasty.getStatService().hasMultiQueenColony(dynasty));

        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        assertTrue(dynasty.getStatService().hasMultiQueenColony(dynasty));
    }
}
