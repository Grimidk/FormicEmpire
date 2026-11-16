package com.grimidk.formicempire.classes.entities.services.dynasty;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
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

    @Test
    void computeNonEssentialOutboundLoadIncludesMineralsResinAndSyrup() {
        origin.unlockBuilding(GameUnlocks.PASSIVE_APHID);
        destination.unlockBuilding(GameUnlocks.PASSIVE_APHID);
        origin.unlockBuilding(GameUnlocks.SYRUP_RESERVOIR_0);
        destination.unlockBuilding(GameUnlocks.SYRUP_RESERVOIR_0);

        int rockCap = origin.getStatsService().getMineralsCapacity(origin);
        int resinCap = origin.getStatsService().getResinsCapacity(origin);
        int syrupCap = origin.getStatsService().getSyrupsCapacity(origin);
        origin.setMinerals(rockCap * 0.9);
        origin.setResins(resinCap * 0.9);
        origin.setSyrups(syrupCap * 0.9);
        destination.setMinerals(rockCap * 0.1);
        destination.setResins(resinCap * 0.1);
        destination.setSyrups(syrupCap * 0.1);

        Map<ResourceType, Double> load = DynastyTradeAutomation.computeNonEssentialOutboundLoad(origin, destination);

        assertTrue(load.containsKey(GameConstants.RESOURCE_ROCK));
        assertTrue(load.containsKey(GameConstants.RESOURCE_RESIN));
        assertTrue(load.containsKey(GameConstants.RESOURCE_SYRUP));
    }

    @Test
    void isConvoyWorthwhileRejectsHighRiskLowProfitLongDistanceConvoy() {
        Hex hexA = new Hex();
        hexA.setQ(0);
        hexA.setR(0);
        Hex hexFar = new Hex();
        hexFar.setQ(10);
        hexFar.setR(0);

        Map<AntClass, Integer> weakTransport = new HashMap<>();
        weakTransport.put(GameConstants.CLASS_WORKER, 1);

        Map<ResourceType, Double> smallLoad = new HashMap<>();
        smallLoad.put(GameConstants.RESOURCE_ROCK, 40.0);

        assertFalse(DynastyTradeAutomation.isConvoyWorthwhile(
                origin, hexA, hexFar, GameConstants.METHOD_LAND, weakTransport, smallLoad));

        Trade nearTrade = new Trade(hexA, hexA, smallLoad, null, weakTransport, false, false, GameConstants.METHOD_LAND);
        Trade farTrade = new Trade(hexA, hexFar, smallLoad, null, weakTransport, false, false, GameConstants.METHOD_LAND);
        assertTrue(farTrade.getTotalHours() > nearTrade.getTotalHours());
        assertTrue(farTrade.getEffectiveDangerFactor() > nearTrade.getEffectiveDangerFactor());
    }

    @Test
    void nonBorderingCapitalRouteRequiresMutualKnowledgeAndRespectsWaterAndThirdPartyBlocks() {
        World world = new World();
        Hex h0 = new Hex(); h0.setQ(0); h0.setR(0); h0.setBiome(GameConstants.BIOME_PLAINS);
        Hex h1 = new Hex(); h1.setQ(1); h1.setR(0); h1.setBiome(GameConstants.BIOME_PLAINS);
        Hex h2 = new Hex(); h2.setQ(2); h2.setR(0); h2.setBiome(GameConstants.BIOME_PLAINS);
        h0.setSouthEast(h1); h1.setNorthWest(h0);
        h1.setSouthEast(h2); h2.setNorthWest(h1);
        world.getHexes().add(h0);
        world.getHexes().add(h1);
        world.getHexes().add(h2);

        Dynasty d1 = new Dynasty(1, "One", false, GameConstants.SPECIES_OMNI);
        Dynasty d2 = new Dynasty(2, "Two", false, GameConstants.SPECIES_OMNI);
        d1.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        d2.unlockUpgrade(GameUnlocks.ABILITY_TRADE);
        Colony c1 = new Colony(1, "Cap1", false);
        Colony c2 = new Colony(2, "Cap2", false);
        c1.setCapital(true);
        c2.setCapital(true);
        d1.addColony(c1);
        d2.addColony(c2);
        h0.setColony(c1);
        h2.setColony(c2);
        world.registerDynasty(d1);
        world.registerDynasty(d2);

        assertFalse(DynastyDiplomacyService.areDynastiesMutuallyKnown(d1, d2, world));
        d1.discoverHex(h2);
        d2.discoverHex(h0);
        assertTrue(DynastyDiplomacyService.areDynastiesMutuallyKnown(d1, d2, world));
        assertTrue(DynastyDiplomacyService.findCapitalTradeRouteDistance(d1, d2, world) == 2);

        h1.setBiome(GameConstants.BIOME_OCEAN);
        assertTrue(DynastyDiplomacyService.findCapitalTradeRouteDistance(d1, d2, world) < 0);

        h1.setBiome(GameConstants.BIOME_PLAINS);
        Dynasty d3 = new Dynasty(3, "Three", false, GameConstants.SPECIES_OMNI);
        Colony c3 = new Colony(3, "Blocker", false);
        d3.addColony(c3);
        h1.setColony(c3);
        assertTrue(DynastyDiplomacyService.findCapitalTradeRouteDistance(d1, d2, world) < 0);
    }
}
