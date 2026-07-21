package com.grimidk.formicempire.classes.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class ColonyWarEconomyRolesTest {

    @Test
    void peaceAndWarDistributionsAreStoredSeparately() {
        Colony colony = new Colony(1, "Test", true);

        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER, 10);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_MILITIA, 0);
        colony.setWarAssignedRoleCount(GameConstants.ROLE_MILITIA, 8);
        colony.setWarAssignedRoleCount(GameConstants.ROLE_FORAGER, 2);

        assertEquals(10, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER));
        assertEquals(8, colony.getWarAssignedRoleCount(GameConstants.ROLE_MILITIA));
    }

    @Test
    void activeDistributionFollowsDynastyWarState() {
        Dynasty dynasty = new Dynasty(1, "Test Dynasty", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(2, "Test", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));

        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_HUNTER, 5);
        colony.setWarAssignedRoleCount(GameConstants.ROLE_WARRIOR, 7);

        assertFalse(dynasty.isAtWar());
        assertEquals(5, colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER));
        assertEquals(0, colony.getAssignedRoleCount(GameConstants.ROLE_WARRIOR));

        Dynasty enemy = new Dynasty(3, "Enemy", false, GameConstants.SPECIES_OMNI);
        Colony enemyColony = new Colony(3, "Enemy Capital", false);
        enemyColony.getWorkers().add(new Ant(enemyColony, GameConstants.TYPE_WORKER));
        enemy.addColony(enemyColony);
        dynasty.getDiplomacyService().applyWar(enemy);

        assertTrue(dynasty.isAtWar());
        assertEquals(0, colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER));
        assertEquals(7, colony.getAssignedRoleCount(GameConstants.ROLE_WARRIOR));
        assertEquals(5, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_HUNTER));
    }

    @Test
    void copyPeaceRolesToWarCopiesDistribution() {
        Colony colony = new Colony(3, "Test", true);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_HUNTER, 5);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_BRUTE, 4);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_MILITIA, 6);

        colony.copyPeaceRolesToWar();

        assertEquals(5, colony.getWarAssignedRoleCount(GameConstants.ROLE_HUNTER));
        assertEquals(0, colony.getWarAssignedRoleCount(GameConstants.ROLE_BRUTE));
        assertEquals(0, colony.getWarAssignedRoleCount(GameConstants.ROLE_MILITIA));
    }

    @Test
    void warEconomyExclusiveRolesCannotBeStoredInPeaceDistribution() {
        Colony colony = new Colony(6, "Test", true);

        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_WARRIOR, 5);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_DEFENDER, 3);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_BRUTE, 4);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_ARTILLERY, 2);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_SIEGE, 1);
        colony.setPeaceAssignedRoleCount(GameConstants.ROLE_MILITIA, 6);

        assertEquals(0, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_WARRIOR));
        assertEquals(0, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_DEFENDER));
        assertEquals(0, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_BRUTE));
        assertEquals(0, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_ARTILLERY));
        assertEquals(0, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_SIEGE));
        assertEquals(0, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_MILITIA));
    }

    @Test
    void loadsBothDistributionsFromSave() {
        Savefile.SavedColony saved = new Savefile.SavedColony();
        saved.id = 4;
        saved.name = "Saved";
        saved.assignedRoleCounts.put(String.valueOf(GameConstants.ROLE_FORAGER.getId()), 12);
        saved.warAssignedRoleCounts.put(String.valueOf(GameConstants.ROLE_MILITIA.getId()), 9);

        Colony colony = new Colony(saved);

        assertEquals(12, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FORAGER));
        assertEquals(9, colony.getWarAssignedRoleCount(GameConstants.ROLE_MILITIA));
    }

    @Test
    void legacySaveCopiesPeaceDistributionIntoWarProfile() {
        Savefile.SavedColony saved = new Savefile.SavedColony();
        saved.id = 5;
        saved.name = "Legacy";
        saved.assignedRoleCounts.put(String.valueOf(GameConstants.ROLE_FARMER.getId()), 3);

        Colony colony = new Colony(saved);

        assertEquals(3, colony.getPeaceAssignedRoleCount(GameConstants.ROLE_FARMER));
        assertEquals(3, colony.getWarAssignedRoleCount(GameConstants.ROLE_FARMER));
    }
}
