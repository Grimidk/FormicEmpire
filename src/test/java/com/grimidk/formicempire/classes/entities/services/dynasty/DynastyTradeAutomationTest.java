package com.grimidk.formicempire.classes.entities.services.dynasty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class DynastyTradeAutomationTest {

    private Colony origin;
    private Colony destination;

    @BeforeEach
    void setUp() {
        origin = new Colony(1, "Origin", true);
        destination = new Colony(2, "Dest", true);
        origin.unlockUpgrade(GameUnlocks.ROLE_MINER);
        destination.unlockUpgrade(GameUnlocks.ROLE_MINER);
        origin.unlockUpgrade(GameUnlocks.ABILITY_RESIN);
        destination.unlockUpgrade(GameUnlocks.ABILITY_RESIN);
        origin.unlockBuilding(GameUnlocks.ROCK_WAREHOUSE_0);
        destination.unlockBuilding(GameUnlocks.ROCK_WAREHOUSE_0);
        origin.unlockBuilding(GameUnlocks.RESIN_RESERVOIR_0);
        destination.unlockBuilding(GameUnlocks.RESIN_RESERVOIR_0);
    }

    @Test
    void computeOutboundLoadIncludesResinAndMineralsWhenSurplusExists() {
        int rockCap = origin.getStatsService().getMineralsCapacity(origin);
        int resinCap = origin.getStatsService().getResinsCapacity(origin);
        origin.setMinerals(rockCap * 0.9);
        origin.setResins(resinCap * 0.9);
        destination.setMinerals(rockCap * 0.1);
        destination.setResins(resinCap * 0.1);

        Map<ResourceType, Double> load = DynastyTradeAutomation.computeOutboundLoad(origin, destination);

        assertTrue(load.containsKey(GameConstants.RESOURCE_ROCK));
        assertTrue(load.containsKey(GameConstants.RESOURCE_RESIN));
    }

    @Test
    void loadsDifferDetectsResourceChanges() {
        Map<ResourceType, Double> current = new HashMap<>();
        current.put(GameConstants.RESOURCE_ROCK, 100.0);
        Map<ResourceType, Double> proposed = new HashMap<>();
        proposed.put(GameConstants.RESOURCE_ROCK, 150.0);

        assertTrue(DynastyTradeAutomation.loadsDiffer(current, proposed));
    }

    @Test
    void loadsDifferIgnoresSmallChanges() {
        Map<ResourceType, Double> current = new HashMap<>();
        current.put(GameConstants.RESOURCE_RESIN, 100.0);
        Map<ResourceType, Double> proposed = new HashMap<>();
        proposed.put(GameConstants.RESOURCE_RESIN, 100.5);

        assertFalse(DynastyTradeAutomation.loadsDiffer(current, proposed));
    }
}
