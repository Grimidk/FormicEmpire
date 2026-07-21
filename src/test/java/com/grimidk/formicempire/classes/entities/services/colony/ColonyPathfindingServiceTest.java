package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.spatial.Room;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

class ColonyPathfindingServiceTest {

    private Colony colony;
    private ColonyPathfindingService pathfinding;

    @BeforeEach
    void setUp() {
        colony = new Colony(1, "Test", true);
        pathfinding = colony.getPathfindingService();
    }

    @Test
    void nullDestinationReturnsEmptyRoute() {
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        Queue<NeoPoint> route = pathfinding.calculateRoute(colony, null, null, ant);
        assertNotNull(route);
        assertTrue(route.isEmpty());
    }

    @Test
    void underworldRoomRouteEndsAtDestinationCenter() {
        Ant ant = new Ant(colony, GameConstants.TYPE_WORKER);
        ant.setDimension(WorldSpaces.UNDERWORLD);
        Room from = new Room(101, "From", WorldSpaces.UNDERWORLD, 0, 0, false,
                new java.awt.Point(0, 100), new java.awt.Point(0, 80), new java.awt.Point(0, 120),
                new java.awt.Point(0, 100), null, null, null);
        Room to = new Room(102, "To", WorldSpaces.UNDERWORLD, 0, 0, false,
                new java.awt.Point(0, 300), new java.awt.Point(0, 280), new java.awt.Point(0, 320),
                new java.awt.Point(0, 300), null, null, null);

        Queue<NeoPoint> route = pathfinding.calculateRoute(colony, from, to, ant);
        assertFalse(route.isEmpty());
        NeoPoint last = null;
        while (!route.isEmpty()) {
            last = route.poll();
        }
        assertNotNull(last);
        assertTrue(last.y >= 250);
    }
}
