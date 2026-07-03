package com.grimidk.formicempire.classes.entities.services.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

import java.lang.reflect.Field;
import java.util.List;

public class ColonyPhysicsServiceTest {

    private ColonyPhysicsService physicsService;
    private Colony colony;
    private Dynasty dynasty;

    @BeforeEach
    public void setUp() throws Exception {
        physicsService = new ColonyPhysicsService();
        
        dynasty = new Dynasty(1, "TestDynasty", true, GameConstants.SPECIES_OMNI);
        colony = new Colony(1, "TestColony", true);
        colony.setDynasty(dynasty);
    }

    @Test
    public void testGetRoomBounds_ConstructionSite_Default() {
        Rectangle bounds = physicsService.getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE);
        
        assertEquals(512, bounds.y, "Default Y should be 512");
        assertEquals(256, bounds.width, "Width should be 256");
        assertEquals(256, bounds.height, "Height should be 256");
    }

    @Test
    public void testGetRoomBounds_ConstructionSite_WithBreeder() {
        dynasty.unlockUpgrade(GameUnlocks.ROLE_BREEDER);
        
        Rectangle bounds = physicsService.getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE);
        
        assertEquals(1024, bounds.y, "Y should be 1024 with Breeder upgrade");
        assertEquals(256, bounds.width, "Width should be 256");
    }

    @Test
    public void testGetRoomBounds_ConstructionSite_WithTunnels() throws Exception {
        Field tunnelsField = Dynasty.class.getDeclaredField("tunnels");
        tunnelsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Tunnel> tunnels = (List<Tunnel>) tunnelsField.get(dynasty);
        tunnels.add(new Tunnel(new Hex(), new Hex(), 100));
        
        Rectangle bounds = physicsService.getRoomBounds(colony, WorldSpaces.CONSTRUCTION_SITE);
        
        assertEquals(1024, bounds.y, "Y should be 1024 with Tunnels");
    }
}
