package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConvoySceneBuilderTest {

    private World world;
    private Hex originHex;
    private Hex destHex;
    private Colony origin;
    private Trade trade;

    @BeforeEach
    void setUp() {
        origin = new Colony(1, "Origin", true);
        Colony destination = new Colony(2, "Destination", true);
        originHex = new Hex();
        originHex.setColony(origin);
        originHex.setQ(0);
        originHex.setR(0);
        originHex.setBiome(GameConstants.BIOME_PLAINS);
        destHex = new Hex();
        destHex.setColony(destination);
        destHex.setQ(2);
        destHex.setR(-1);
        destHex.setBiome(GameConstants.BIOME_FOREST);

        Dynasty dynasty = new Dynasty(1, "Dynasty", true, GameConstants.SPECIES_OMNI);
        dynasty.addColony(origin);
        dynasty.addColony(destination);
        TradeManager tradeManager = new TradeManager();
        dynasty.bindTradeManager(tradeManager);

        origin.setActive(true);
        origin.addAnts(GameConstants.TYPE_WORKER, 3);

        Map<AntType, Integer> transport = new HashMap<>();
        transport.put(GameConstants.TYPE_WORKER, 2);
        trade = new Trade(originHex, destHex, new HashMap<>(), null, transport, false, false, GameConstants.METHOD_LAND);
        tradeManager.addTrade(trade);
        trade.startTrip();

        world = new World();
        ArrayList<Hex> hexes = new ArrayList<>();
        hexes.add(originHex);
        hexes.add(destHex);
        world.setHexes(hexes);
    }

    @Test
    void buildsSceneForActiveConvoyWithDestinationBiomeOnOutboundLeg() {
        ConvoyScene scene = ConvoySceneBuilder.build(world, trade);
        assertTrue(scene.isAvailable());
        assertEquals(GameConstants.BIOME_FOREST, scene.getLandBiome());
        assertEquals(ConvoyScene.BackgroundKind.LAND_BIOME, scene.getBackgroundKind());
        assertFalse(scene.isReturning());
        assertFalse(scene.typeCounts().isEmpty());
    }

    @Test
    void usesOriginBiomeOnReturnLeg() {
        trade.restoreTripState(trade.getTotalHours(), trade.getTotalHours() / 2, true);
        ConvoyScene scene = ConvoySceneBuilder.build(world, trade);
        assertTrue(scene.isAvailable());
        assertTrue(scene.isReturning());
        assertEquals(GameConstants.BIOME_PLAINS, scene.getLandBiome());
    }

    @Test
    void usesDedicatedTilesForSeaAndTunnelMethods() {
        trade = new Trade(originHex, destHex, new HashMap<>(), null,
                Map.of(GameConstants.TYPE_WORKER, 1), false, false, GameConstants.METHOD_SEA);
        Ant ant = origin.getAntsByType(GameConstants.TYPE_WORKER).get(0);
        ant.setOnTrade(true);
        trade.getAntsOnTrip().add(ant);
        trade.setActive(true);

        ConvoyScene seaScene = ConvoySceneBuilder.build(world, trade);
        assertEquals(ConvoyScene.BackgroundKind.SEA, seaScene.getBackgroundKind());

        trade = new Trade(originHex, destHex, new HashMap<>(), null,
                Map.of(GameConstants.TYPE_WORKER, 1), false, false, GameConstants.METHOD_TUNNEL);
        trade.getAntsOnTrip().clear();
        ant.setOnTrade(true);
        trade.getAntsOnTrip().add(ant);
        trade.setActive(true);

        ConvoyScene tunnelScene = ConvoySceneBuilder.build(world, trade);
        assertEquals(ConvoyScene.BackgroundKind.TUNNEL, tunnelScene.getBackgroundKind());
    }

    @Test
    void inTransitDuringReturnWithoutAntsOnTrip() {
        trade.getAntsOnTrip().clear();
        trade.restoreTripState(trade.getTotalHours(), trade.getTotalHours() / 2, true);
        assertTrue(ConvoySceneBuilder.isInTransit(trade));
        ConvoyScene scene = ConvoySceneBuilder.build(world, trade);
        assertTrue(scene.isAvailable());
        assertTrue(scene.isReturning());
    }

    @Test
    void unavailableWithoutAntsOnTrip() {
        trade.getAntsOnTrip().clear();
        assertFalse(ConvoySceneBuilder.isInTransit(trade));
        ConvoyScene scene = ConvoySceneBuilder.build(world, trade);
        assertFalse(scene.isAvailable());
    }
}
