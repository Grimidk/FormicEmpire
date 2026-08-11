package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.spatial.Room;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

class ColonySpatialServiceTest {

    private Colony colony;
    private ColonySpatialService spatialService;

    @BeforeEach
    void setUp() {
        colony = new Colony(1, "Test", true);
        spatialService = colony.getSpatialService();
    }

    @Test
    void entranceUsesAnchorWhenNoEntranceBounds() {
        NeoPoint entrance = spatialService.getColonyEntrance(colony);
        assertNotNull(entrance);
        assertEquals(WorldSpaces.OVERWORLD, entrance.getDimension());
        assertEquals(ColonySpatialLayout.ANCHOR_CENTER_X - 20, entrance.x);
    }

    @Test
    void yardRoomsIdentifiedById() {
        Room rancher = new Room(200, "Rancher", WorldSpaces.OVERWORLD, 0, 0, false,
                null, null, null, null, null, null, null);
        Room storage = new Room(100, "Storage", WorldSpaces.UNDERWORLD, 0, 0, false,
                null, null, null, null, null, null, null);
        assertTrue(spatialService.isYard(rancher));
        assertFalse(spatialService.isYard(storage));
    }

    @Test
    void underworldResolvedPointOffsetsByHallwayCenter() {
        Room room = new Room(101, "Room", WorldSpaces.UNDERWORLD, 0, 0, false,
                null, new java.awt.Point(10, 20), null, null, null, null, null);
        NeoPoint resolved = spatialService.getResolvedPoint(colony, room, "CENTER");
        assertNotNull(resolved);
        assertEquals(ColonySpatialLayout.ANCHOR_CENTER_X + 10, resolved.x);
        assertEquals(20, resolved.y);
    }
}
