package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyAiServiceTest {

    private DynastyAiService aiService;
    private TradeManager tradeManager;
    private World world;
    private Dynasty npc;
    private Dynasty neighbor;

    @BeforeEach
    void setUp() {
        aiService = new DynastyAiService();
        tradeManager = new TradeManager();
        world = new World();
        npc = new Dynasty(1, "NPC Dynasty", false, GameConstants.SPECIES_OMNI);
        neighbor = new Dynasty(2, "Neighbor Dynasty", false, GameConstants.SPECIES_OMNI);
        world.getDynastys().add(npc);
        world.getDynastys().add(neighbor);
    }

    @Test
    void skipsPlayerDynasty() {
        Dynasty player = new Dynasty(3, "Player Dynasty", true, GameConstants.SPECIES_OMNI);
        player.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        Colony colony = new Colony(10, "Capital", true);
        colony.setAge(10);
        colony.setLoyalty(40);
        colony.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 2);
        player.addColony(colony);

        aiService.runDailyAi(player, world, tradeManager);

        assertEquals(40, colony.getLoyalty());
    }

    @Test
    void stabilizesLowLoyaltyColony() {
        npc.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        Colony capital = new Colony(10, "Capital", false);
        capital.setAge(10);
        capital.setLoyalty(80);
        capital.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 2);
        npc.addColony(capital);
        npc.setCapital(capital);

        Colony frontier = new Colony(11, "Frontier", false);
        frontier.setAge(10);
        frontier.setLoyalty(50);
        npc.addColony(frontier);

        aiService.runDailyAi(npc, world, tradeManager);

        assertTrue(frontier.getEffectiveLoyalty(null, null) > 50);
    }

    @Test
    void formsPactWhenCordial() {
        npc.setDiplomaticReputation(neighbor.getId(), 100);
        neighbor.setDiplomaticReputation(npc.getId(), 100);
        Colony colony = new Colony(10, "Capital", false);
        colony.setAge(10);
        colony.getWorkers().add(new Ant(colony, GameConstants.TYPE_WORKER));
        npc.addColony(colony);
        Colony neighborColony = new Colony(20, "Neighbor Capital", false);
        neighborColony.getWorkers().add(new Ant(neighborColony, GameConstants.TYPE_WORKER));
        neighbor.addColony(neighborColony);

        aiService.runDailyAi(npc, world, tradeManager);

        assertTrue(npc.getDiplomacyService().hasNonAggressionPact(neighbor));
    }

    @Test
    void sendsDiplomatsToImproveNeighborReputation() {
        npc.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        npc.setDiplomaticReputation(neighbor.getId(), 50);
        neighbor.setDiplomaticReputation(npc.getId(), 50);

        Colony capital = new Colony(10, "Capital", false);
        capital.setAge(10);
        Ant diplomat = new Ant(capital, GameConstants.TYPE_PRINCESS);
        diplomat.setRole(GameConstants.ROLE_DIPLOMAT);
        capital.getPrincesses().add(diplomat);
        capital.setAssignedRoleCount(GameConstants.ROLE_DIPLOMAT, 1);
        capital.getWorkers().add(new Ant(capital, GameConstants.TYPE_WORKER));
        npc.addColony(capital);
        npc.setCapital(capital);
        Colony neighborColony = new Colony(20, "Neighbor Capital", false);
        neighborColony.getWorkers().add(new Ant(neighborColony, GameConstants.TYPE_WORKER));
        neighbor.addColony(neighborColony);

        aiService.runDailyAi(npc, world, tradeManager);

        assertTrue(npc.getDiplomacyService().getEffectiveDiplomaticReputation(neighbor, world) > 50);
        assertFalse(npc.getDiplomacyService().hasNonAggressionPact(neighbor));
    }

    @Test
    void assignsDiplomatStaffWhenPrincessesPresent() {
        npc.unlockUpgrade(GameUnlocks.ROLE_DIPLOMAT);
        Colony capital = new Colony(10, "Capital", false);
        capital.setAge(10);
        capital.getPrincesses().add(new Ant(capital, GameConstants.TYPE_PRINCESS));
        capital.getPrincesses().add(new Ant(capital, GameConstants.TYPE_PRINCESS));
        npc.addColony(capital);

        aiService.runDailyAi(npc, world, tradeManager);

        assertEquals(2, capital.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT));
    }
}
