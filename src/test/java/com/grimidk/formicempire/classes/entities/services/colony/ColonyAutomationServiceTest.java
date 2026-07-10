package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class ColonyAutomationServiceTest {

    private ColonyAutomationService automationService;
    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    void setUp() {
        automationService = new ColonyAutomationService();
        dynasty = new Dynasty(1, "AI", false, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "Prime", false);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        colony.setAutomationEnabled(true);
        colony.setAge(7);
    }

    @Test
    void assignsCouriersWhenDynastyHasMultipleColonies() {
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        colony.unlockUpgrade(GameUnlocks.ROLE_COURIER);
        dynasty.addColony(new Colony(2, "Secundus", false));

        for (int i = 0; i < 20; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        automationService.runAutomation(colony);

        assertTrue(colony.getAssignedRoleCount(GameConstants.ROLE_COURIER) >= 1);
    }

    @Test
    void assignsBorersWhenTunnelProjectActive() {
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_TUNNELS);
        colony.unlockUpgrade(GameUnlocks.ROLE_BORER);
        for (int i = 0; i < 6; i++) {
            colony.getMajors().add(new Ant(colony, GameConstants.TYPE_MAJOR));
        }
        for (int i = 0; i < 15; i++) {
            colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        }

        Hex hexA = new Hex();
        Hex hexB = new Hex();
        colony.setCurrentTunnelProject(new com.grimidk.formicempire.classes.entities.Tunnel(hexA, hexB, 100));

        automationService.runAutomation(colony);

        assertTrue(colony.getAssignedRoleCount(GameConstants.ROLE_BORER) >= 1);
    }

    @Test
    void satelliteColonyAutomationAssignsRoyalRoleShares() {
        Colony capital = new Colony(1, "Capital", false);
        capital.setCapital(true);
        Colony satellite = new Colony(2, "Satellite", false);
        dynasty.addColony(capital);
        dynasty.addColony(satellite);
        satellite.setDynasty(dynasty);
        satellite.setAutomationEnabled(true);
        satellite.setAge(7);

        satellite.unlockUpgrade(GameUnlocks.ROLE_BREEDER);
        satellite.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        satellite.unlockUpgrade(GameUnlocks.ROLE_SKYTRANS);
        satellite.unlockUpgrade(GameUnlocks.ROLE_RESEARCHER);

        for (int i = 0; i < 10; i++) {
            satellite.getPrincesses().add(new Ant(satellite, GameConstants.TYPE_PRINCESS));
            satellite.getQueens().add(new Ant(satellite, GameConstants.TYPE_QUEEN));
        }

        automationService.runAutomation(satellite);

        assertEquals(3, satellite.getAssignedRoleCount(GameConstants.ROLE_BREEDER));
        assertEquals(3, satellite.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT));
        assertEquals(1, satellite.getAssignedRoleCount(GameConstants.ROLE_SKYTRANS));
        assertEquals(3, satellite.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT));
        assertEquals(3, satellite.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER));
        assertEquals(7, satellite.getAssignedRoleCount(GameConstants.ROLE_LAYER));
    }

    @Test
    void inactiveNpcColonyRunsContaminationInDailyLite() {
        Colony npc = new Colony(2, "NPC", false);
        npc.setAge(7);
        npc.setActive(false);
        npc.setMushrooms(0);

        ColonyJobRules.runDailyLite(npc);
        assertTrue(npc.getAge() >= 7);
    }
}
