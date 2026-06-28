package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.infrasctructure.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColonyConvoyTransitServiceTest {

    private Colony origin;
    private Colony destination;
    private Hex originHex;
    private Hex destHex;
    private ColonyConvoyTransitService transitService;
    private Trade trade;

    @BeforeEach
    void setUp() {
        origin = new Colony(1, "Origin", true);
        destination = new Colony(2, "Destination", true);
        originHex = new Hex();
        originHex.setColony(origin);
        originHex.setQ(0);
        originHex.setR(0);
        destHex = new Hex();
        destHex.setColony(destination);
        destHex.setQ(2);
        destHex.setR(-1);

        Dynasty dynasty = new Dynasty(1, "Dynasty", true, GameConstants.SPECIES_OMNI);
        dynasty.addColony(origin);
        dynasty.addColony(destination);
        TradeManager tradeManager = new TradeManager();
        dynasty.bindTradeManager(tradeManager);

        origin.setRoomBounds(null, null, null, null, null, null, null, null, new Rectangle(400, 512, 512, 256));
        origin.setActive(true);

        origin.addAnts(GameConstants.TYPE_WORKER, 5);
        transitService = origin.getConvoyTransitService();

        Map<AntType, Integer> transport = new HashMap<>();
        transport.put(GameConstants.TYPE_WORKER, 2);
        trade = new Trade(originHex, destHex, new HashMap<>(), null, transport, false, false, GameConstants.METHOD_LAND);
        tradeManager.addTrade(trade);
    }

    @Test
    void bearingFromHexes_flipsOnReturnLeg() {
        double outbound = ColonyConvoyTransitService.bearingFromHexes(originHex, destHex, false);
        double inbound = ColonyConvoyTransitService.bearingFromHexes(originHex, destHex, true);
        assertEquals(outbound + Math.PI, inbound, 0.0001);
    }

    @Test
    void beginConvoyTransit_marksAntsUnderworldAndSetsBearing() {
        trade.startTrip();
        assertEquals(2, trade.getAntsOnTrip().size());
        for (Ant ant : trade.getAntsOnTrip()) {
            assertTrue(ant.isOnTrade());
            assertEquals(WorldSpaces.UNDERWORLD, ant.getDimension());
        }
        assertFalse(trade.isReturning());
    }

    @Test
    void convoyAntsEnterTunnelWorldNearPortal() {
        trade.startTrip();
        NeoPoint portal = origin.getSpatialService().getTransitPortal(origin);
        for (Ant ant : trade.getAntsOnTrip()) {
            ant.setPosition(new java.awt.Point(portal.x - 10, portal.y));
        }
        transitService.runConvoyPhysics(origin);
        for (Ant ant : trade.getAntsOnTrip()) {
            assertEquals(WorldSpaces.TUNNEL_WORLD, ant.getDimension());
        }
    }

    @Test
    void restoreConvoyAnts_returnsAntsToTransitRoom() {
        trade.startTrip();
        for (Ant ant : trade.getAntsOnTrip()) {
            ant.setDimension(WorldSpaces.TUNNEL_WORLD);
        }
        transitService.restoreConvoyAnts(origin, trade);
        for (Ant ant : trade.getAntsOnTrip()) {
            assertEquals(WorldSpaces.UNDERWORLD, ant.getDimension());
            Rectangle transit = origin.getTransitBounds();
            assertTrue(transit.contains(ant.getX(), ant.getY()));
        }
    }
}
