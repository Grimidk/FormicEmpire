package com.grimidk.formicempire.classes.infrasctructure;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldGenerationSeedDynastiesTest {

    @Test
    void rectangularWorldGenerationWithSeedDynastiesFalseHasNoColoniesOrDynasties() {
        World world = new World();
        world.generateRectangularWorld(null, null, "GalleryMap", null, false);

        assertNotNull(world.getHexes());
        assertFalse(world.getHexes().isEmpty());
        assertTrue(world.getDynastys().isEmpty());
        for (Hex h : world.getHexes()) {
            assertNull(h.getColony());
        }
    }

    @Test
    void rectangularWorldGenerationWithSeedDynastiesTrueSpawnsDynasties() {
        World world = new World();
        world.generateRectangularWorld(null, null, "LiveMap", null, true);

        assertNotNull(world.getHexes());
        assertFalse(world.getHexes().isEmpty());
        assertFalse(world.getDynastys().isEmpty());
        boolean foundColony = false;
        for (Hex h : world.getHexes()) {
            if (h.getColony() != null) {
                foundColony = true;
                break;
            }
        }
        assertTrue(foundColony);
    }

    @Test
    void oceanElevationTransitionsFromShelfToAbyss() {
        World world = new World();
        world.generateRectangularWorld(null, null, "ShelfTest", null, false);

        boolean foundShelf = false;
        boolean foundDeep = false;

        for (Hex h : world.getHexes()) {
            if (h.getBiome() == GameConstants.BIOME_OCEAN
                    || h.getBiome() == GameConstants.BIOME_DEEP_OCEAN
                    || h.getBiome() == GameConstants.BIOME_SHALLOW_OCEAN) {
                if (h.getWaterDistance() == 1) {
                    assertEquals(2, h.getElevation());
                    foundShelf = true;
                } else if (h.getWaterDistance() >= 2 && h.getWaterDistance() <= 6) {
                    assertEquals(1, h.getElevation());
                } else if (h.getWaterDistance() >= 7) {
                    assertEquals(0, h.getElevation());
                    foundDeep = true;
                }
            }
        }

        assertTrue(foundShelf);
        assertTrue(foundDeep);
    }
}
