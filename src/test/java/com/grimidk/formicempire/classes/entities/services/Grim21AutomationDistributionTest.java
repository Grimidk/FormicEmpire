package com.grimidk.formicempire.classes.entities.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

/**
 * Mirrors save slot 1 colony 65 (Grim 21): automated duchy satellite with trade upgrades.
 */
class Grim21AutomationDistributionTest {

    private Colony colony;
    private Dynasty dynasty;
    private ColonyAutomationService automationService;

    @BeforeEach
    void setUp() {
        automationService = new ColonyAutomationService();
        dynasty = new Dynasty(1, "Grim Dynasty", true, GameConstants.SPECIES_OMNI);
        unlockGrimDynastyUpgrades(dynasty);

        colony = new Colony(65, "Grim 21", true);
        colony.setDynasty(dynasty);
        colony.setAutomationEnabled(true);
        colony.setAge(51);
        colony.setMushrooms(13580);
        colony.setPlants(10000);
        colony.setWater(2280);

        dynasty.addColony(new Colony(1, "Grim Prime", true));
        dynasty.addColony(colony);
        dynasty.addColony(new Colony(2, "Grim Secundus", true));

        for (int i = 0; i < 866; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        for (int i = 0; i < 154; i++) {
            colony.getSoldiers().add(new Ant(colony, GameConstants.TYPE_SOLDIER));
        }
        for (int i = 0; i < 62; i++) {
            colony.getPrincesses().add(new Ant(colony, GameConstants.TYPE_PRINCESS));
        }
        colony.getQueens().add(new Ant(colony, GameConstants.TYPE_QUEEN));
        for (int i = 0; i < 80; i++) {
            colony.getEggs().add(new Ant(colony, GameConstants.TYPE_EGG));
        }
        for (int i = 0; i < 80; i++) {
            colony.getLarvae().add(new Ant(colony, GameConstants.TYPE_LARVA));
        }
        for (int i = 0; i < 14; i++) {
            colony.getDrones().add(new Ant(colony, GameConstants.TYPE_DRONE));
        }
    }

    private static void unlockGrimDynastyUpgrades(Dynasty dynasty) {
        for (UpgradeId id : new UpgradeId[] {
                UpgradeId.TRADE, UpgradeId.TUNNELS, UpgradeId.COURIER, UpgradeId.BORER, UpgradeId.ENGINEER,
                UpgradeId.ESCORT, UpgradeId.TRANSPORT, UpgradeId.SCOUT, UpgradeId.BUILDER, UpgradeId.HUNTER,
                UpgradeId.POLICE, UpgradeId.CATCHER, UpgradeId.SKYTRANS, UpgradeId.RESEARCHER, UpgradeId.MINER,
                UpgradeId.GRAVER, UpgradeId.FARMER, UpgradeId.FORAGER, UpgradeId.NURSE, UpgradeId.SOLDIER,
                UpgradeId.PRINCESS, UpgradeId.RESEARCH, UpgradeId.BUILD
        }) {
            unlock(dynasty, id);
        }
    }

    private enum UpgradeId {
        TRADE, TUNNELS, COURIER, BORER, ENGINEER, ESCORT, TRANSPORT, SCOUT, BUILDER, HUNTER, POLICE, CATCHER,
        SKYTRANS, RESEARCHER, MINER, GRAVER, FARMER, FORAGER, NURSE, SOLDIER, PRINCESS, RESEARCH, BUILD
    }

    private static void unlock(Dynasty dynasty, UpgradeId id) {
        switch (id) {
            case TRADE -> dynasty.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
            case TUNNELS -> dynasty.unlockUpgrade(GameUnlocks.ABILITY_TUNNELS);
            case COURIER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_COURIER);
            case BORER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_BORER);
            case ENGINEER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_ENGINEER);
            case ESCORT -> dynasty.unlockUpgrade(GameUnlocks.ROLE_ESCORT);
            case TRANSPORT -> dynasty.unlockUpgrade(GameUnlocks.ROLE_TRANSPORT);
            case SCOUT -> dynasty.unlockUpgrade(GameUnlocks.ROLE_SCOUT);
            case BUILDER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
            case HUNTER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_HUNTER);
            case POLICE -> dynasty.unlockUpgrade(GameUnlocks.ROLE_POLICE);
            case CATCHER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_CATCHER);
            case SKYTRANS -> dynasty.unlockUpgrade(GameUnlocks.ROLE_SKYTRANS);
            case RESEARCHER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);
            case MINER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_MINER);
            case GRAVER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_GRAVER);
            case FARMER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_FARMER);
            case FORAGER -> dynasty.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
            case NURSE -> dynasty.unlockUpgrade(GameUnlocks.ROLE_NURSE);
            case SOLDIER -> dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
            case PRINCESS -> dynasty.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
            case RESEARCH -> dynasty.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
            case BUILD -> dynasty.unlockUpgrade(GameUnlocks.ABILITY_BUILD);
            default -> { }
        }
    }

    @Test
    void grim21AutomationAssignsLogisticsRoles() {
        automationService.runAutomation(colony);

        assertTrue(colony.getAssignedRoleCount(GameConstants.ROLE_COURIER) >= 1);
        assertEquals(866, workerQuotaSum(colony));
        assertEquals(154, colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER)
                + colony.getAssignedRoleCount(GameConstants.ROLE_ESCORT)
                + colony.getAssignedRoleCount(GameConstants.ROLE_POLICE)
                + colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER));
        assertEquals(62, colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT)
                + colony.getAssignedRoleCount(GameConstants.ROLE_BREEDER)
                + colony.getAssignedRoleCount(GameConstants.ROLE_SKYTRANS));

        System.out.println("=== Grim 21 automation quotas (866 workers) ===");
        printRole(GameConstants.ROLE_FORAGER);
        printRole(GameConstants.ROLE_NURSE);
        printRole(GameConstants.ROLE_FARMER);
        printRole(GameConstants.ROLE_GRAVER);
        printRole(GameConstants.ROLE_SCOUT);
        printRole(GameConstants.ROLE_COURIER);
        printRole(GameConstants.ROLE_ENGINEER);
        printRole(GameConstants.ROLE_BUILDER);
        printRole(GameConstants.ROLE_MINER);
        System.out.println("=== Soldiers (154) ===");
        printRole(GameConstants.ROLE_HUNTER);
        printRole(GameConstants.ROLE_ESCORT);
        printRole(GameConstants.ROLE_POLICE);
        System.out.println("=== Princesses (62) ===");
        printRole(GameConstants.ROLE_ASSISTANT);
        printRole(GameConstants.ROLE_BREEDER);
        printRole(GameConstants.ROLE_SKYTRANS);
        System.out.println("=== Queen ===");
        printRole(GameConstants.ROLE_LAYER);
        printRole(GameConstants.ROLE_RESEARCHER);
    }

    @Test
    void inactiveMonthlyParasitationLiteSkipsAntInfectionSync() {
        colony.setActive(false);
        for (int i = 0; i < 200; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }
        colony.getResourceService().addResource(colony, GameConstants.RESOURCE_PLANT, 15000);
        colony.getResourceService().addResource(colony, GameConstants.RESOURCE_WATER, 15000);
        colony.getResourceService().addResource(colony, GameConstants.RESOURCE_FUNGI, 15000);

        colony.getSummarizationService().runMonthlyLite(colony, GameConstants.BIOME_TUNDRA, GameConstants.SEASON_WINTER);

        long infected = colony.getWorkers().stream().filter(Ant::isParasiticMiteInfected).count();
        assertEquals(0, infected);
    }

    private int workerQuotaSum(Colony colony) {
        int sum = 0;
        for (AntRole role : GameConstants.getAntRoles()) {
            if (role.getAntType() == GameConstants.TYPE_WORKER) {
                sum += colony.getAssignedRoleCount(role);
            }
        }
        return sum;
    }

    private void printRole(AntRole role) {
        System.out.println(role.getNameKey() + ": " + colony.getAssignedRoleCount(role));
    }
}
