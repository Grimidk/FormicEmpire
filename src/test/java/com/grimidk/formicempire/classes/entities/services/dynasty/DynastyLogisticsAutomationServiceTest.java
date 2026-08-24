package com.grimidk.formicempire.classes.entities.services.dynasty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class DynastyLogisticsAutomationServiceTest {

    private DynastyLogisticsAutomationService logisticsService;
    private TradeManager tradeManager;
    private Dynasty dynasty;
    private World world;
    private Hex hexA;
    private Hex hexB;
    private Colony colonyA;
    private Colony colonyB;

    @BeforeEach
    void setUp() {
        logisticsService = new DynastyLogisticsAutomationService();
        tradeManager = new TradeManager();
        dynasty = new Dynasty(2, "Rival", false, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        dynasty.unlockUpgrade(GameUnlocks.ROLE_COURIER);

        world = new World();
        hexA = new Hex();
        hexB = new Hex();
        hexA.setBiome(GameConstants.BIOME_PLAINS);
        hexB.setBiome(GameConstants.BIOME_PLAINS);
        hexA.setQ(0);
        hexA.setR(0);
        hexB.setQ(1);
        hexB.setR(0);
        hexA.setNorthEast(hexB);
        hexB.setSouthWest(hexA);

        colonyA = new Colony(10, "Alpha", false);
        colonyB = new Colony(11, "Beta", false);
        dynasty.addColony(colonyA);
        dynasty.addColony(colonyB);
        colonyA.setAutomationEnabled(true);
        colonyB.setAutomationEnabled(true);
        colonyA.setAge(7);
        colonyB.setAge(7);
        colonyA.unlockUpgrade(GameUnlocks.ROLE_COURIER);
        colonyA.setAssignedRoleCount(GameConstants.ROLE_COURIER, 2);

        for (int i = 0; i < 5; i++) {
            colonyA.getWorkers().add(new Ant(colonyA, GameConstants.TYPE_WORKER));
        }

        int mushCap = colonyA.getStatsService().getMushroomsCapacity(colonyA);
        colonyA.setMushrooms(mushCap * 0.8);
        colonyB.setMushrooms(mushCap * 0.1);

        hexA.setColony(colonyA);
        hexB.setColony(colonyB);

        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(hexA);
        hexes.add(hexB);
        world.setHexes(hexes);
        world.getDynastys().add(dynasty);
    }

    @Test
    void npcDynastyOpensTradeBetweenAdjacentColonies() {
        logisticsService.runDailyLogistics(dynasty, world, tradeManager);

        assertEquals(1, tradeManager.getActiveTrades().size());
        assertNotNull(dynasty.getTradeService().findTrade(colonyA, colonyB));
    }

    @Test
    void playerDynastyDoesNotAutoTradeWithoutUpgrade() {
        dynasty.setPlayer(true);
        logisticsService.runDailyLogistics(dynasty, world, tradeManager);
        assertEquals(0, tradeManager.getActiveTrades().size());
    }

    @Test
    void playerDynastyAutoTradesWithUpgradeAndToggle() {
        dynasty.setPlayer(true);
        dynasty.unlockUpgrade(GameUnlocks.ABILITY_AUTO_LOGISTICS);
        colonyA.setAutoLogisticsEnabled(true);
        logisticsService.runDailyLogistics(dynasty, world, tradeManager);
        assertEquals(1, tradeManager.getActiveTrades().size());
    }
}
