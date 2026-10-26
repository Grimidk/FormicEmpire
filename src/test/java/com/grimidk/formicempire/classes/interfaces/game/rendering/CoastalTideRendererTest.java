package com.grimidk.formicempire.classes.interfaces.game.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

class CoastalTideRendererTest {

    @Test
    void tideLevelCyclesEveryTwelveHours() {
        assertEquals(0.0, CoastalTideRenderer.getTideLevel(0, 0), 1e-4);
        assertEquals(0.5, CoastalTideRenderer.getTideLevel(3, 0), 1e-4);
        assertEquals(1.0, CoastalTideRenderer.getTideLevel(6, 0), 1e-4);
        assertEquals(0.5, CoastalTideRenderer.getTideLevel(9, 0), 1e-4);
        assertEquals(0.0, CoastalTideRenderer.getTideLevel(12, 0), 1e-4);
        assertEquals(0.5, CoastalTideRenderer.getTideLevel(15, 0), 1e-4);
        assertEquals(1.0, CoastalTideRenderer.getTideLevel(18, 0), 1e-4);
        assertEquals(0.5, CoastalTideRenderer.getTideLevel(21, 0), 1e-4);
        assertEquals(0.0, CoastalTideRenderer.getTideLevel(24, 0), 1e-4);
    }

    @Test
    void tideLevelAdvancesWithMinutes() {
        double low = CoastalTideRenderer.getTideLevel(0, 0);
        double quarterHour = CoastalTideRenderer.getTideLevel(0, 30);
        double oneHour = CoastalTideRenderer.getTideLevel(1, 0);
        assertTrue(quarterHour > low);
        assertTrue(oneHour > quarterHour);
    }

    @Test
    void tideRisingDetection() {
        assertTrue(CoastalTideRenderer.isTideRising(0));
        assertTrue(CoastalTideRenderer.isTideRising(3));
        assertFalse(CoastalTideRenderer.isTideRising(6));
        assertFalse(CoastalTideRenderer.isTideRising(9));
        assertTrue(CoastalTideRenderer.isTideRising(12));
        assertFalse(CoastalTideRenderer.isTideRising(18));
    }

    @Test
    void coastalDetection() {
        assertFalse(CoastalTideRenderer.isCoastal(null));

        Hex plainHex = new Hex();
        plainHex.setBiome(GameConstants.BIOME_PLAINS);
        plainHex.setCoastal(false);
        assertFalse(CoastalTideRenderer.isCoastal(plainHex));

        plainHex.setCoastal(true);
        assertTrue(CoastalTideRenderer.isCoastal(plainHex));

        Hex beachHex = new Hex();
        beachHex.setBiome(GameConstants.BIOME_BEACH);
        beachHex.setCoastal(false);
        assertTrue(CoastalTideRenderer.isCoastal(beachHex));

        Hex cliffsHex = new Hex();
        cliffsHex.setBiome(GameConstants.BIOME_CLIFFS);
        assertTrue(CoastalTideRenderer.isCoastal(cliffsHex));

        Hex mangroveHex = new Hex();
        mangroveHex.setBiome(GameConstants.BIOME_MANGROVE);
        assertTrue(CoastalTideRenderer.isCoastal(mangroveHex));
    }

    @Test
    void waterBorderResolution() {
        Hex inlandHex = new Hex();
        inlandHex.setBiome(GameConstants.BIOME_PLAINS);
        inlandHex.setCoastal(false);
        CoastalTideRenderer.WaterBorders inlandBorders = CoastalTideRenderer.resolveWaterBorders(inlandHex);
        assertFalse(inlandBorders.hasAny());

        Hex coastalHex = new Hex();
        coastalHex.setBiome(GameConstants.BIOME_BEACH);
        coastalHex.setCoastal(true);

        Hex oceanNorth = new Hex();
        oceanNorth.setBiome(GameConstants.BIOME_OCEAN);
        oceanNorth.setContinentId(0);

        Hex landSouth = new Hex();
        landSouth.setBiome(GameConstants.BIOME_PLAINS);
        landSouth.setContinentId(1);

        coastalHex.setNorth(oceanNorth);
        coastalHex.setSouth(landSouth);
        coastalHex.setNorthEast(landSouth);
        coastalHex.setSouthEast(landSouth);
        coastalHex.setNorthWest(landSouth);
        coastalHex.setSouthWest(landSouth);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(coastalHex);
        assertTrue(borders.north);
        assertFalse(borders.south);
        assertFalse(borders.east);
        assertFalse(borders.west);
    }

    @Test
    void coastalHexWithNoNeighborsDefaultsToSouth() {
        Hex soloCoastal = new Hex();
        soloCoastal.setBiome(GameConstants.BIOME_BEACH);
        Hex land = new Hex();
        land.setBiome(GameConstants.BIOME_PLAINS);
        land.setContinentId(1);
        soloCoastal.setNorth(land);
        soloCoastal.setSouth(land);
        soloCoastal.setNorthEast(land);
        soloCoastal.setSouthEast(land);
        soloCoastal.setNorthWest(land);
        soloCoastal.setSouthWest(land);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(soloCoastal);
        assertTrue(borders.hasAny());
        assertTrue(borders.south);
    }

    @Test
    void renderCoastalTideDoesNotThrowOnNullOrValidContext() {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        try {
            CoastalTideRenderer.renderCoastalTide(g2d, null, 0, 0, 800, 600, 0L);

            World world = new World();
            Hex hex = new Hex();
            hex.setBiome(GameConstants.BIOME_BEACH);
            hex.setCoastal(true);
            world.changeActiveHex(hex);

            CoastalTideRenderer.renderCoastalTide(g2d, world, 100, 50, 800, 600, 123456L);
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void renderCoastalTideAllCardinalBorders() {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        try {
            World world = new World();
            Hex hex = new Hex();
            hex.setBiome(GameConstants.BIOME_BEACH);
            hex.setCoastal(true);

            Hex ocean = new Hex();
            ocean.setBiome(GameConstants.BIOME_OCEAN);
            ocean.setContinentId(0);

            hex.setNorth(ocean);
            hex.setSouth(ocean);
            hex.setNorthEast(ocean);
            hex.setSouthEast(ocean);
            hex.setNorthWest(ocean);
            hex.setSouthWest(ocean);

            CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
            assertTrue(borders.north);
            assertTrue(borders.south);
            assertTrue(borders.east);
            assertTrue(borders.west);

            CoastalTideRenderer.renderCoastalTide(g2d, world, hex, 100, 50, 800, 600, null, 0L);
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void renderCoastalTideCliffsBiome() {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        try {
            World world = new World();
            Hex hex = new Hex();
            hex.setBiome(GameConstants.BIOME_CLIFFS);
            hex.setCoastal(true);

            Hex ocean = new Hex();
            ocean.setBiome(GameConstants.BIOME_OCEAN);
            ocean.setContinentId(0);
            hex.setSouth(ocean);

            CoastalTideRenderer.renderCoastalTide(g2d, world, hex, 0, 0, 800, 600, null, 0L);
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void waterBordersKeyDiffersForCoastalAndInland() {
        Hex inland = new Hex();
        inland.setBiome(GameConstants.BIOME_PLAINS);
        inland.setCoastal(false);
        assertEquals(0, CoastalTideRenderer.getWaterBordersKey(inland, GameConstants.BIOME_PLAINS.getId()));

        Hex beach = new Hex();
        beach.setBiome(GameConstants.BIOME_BEACH);
        beach.setCoastal(true);
        int beachKey = CoastalTideRenderer.getWaterBordersKey(beach, GameConstants.BIOME_BEACH.getId());
        assertTrue(beachKey != 0);

        Hex cliffs = new Hex();
        cliffs.setBiome(GameConstants.BIOME_CLIFFS);
        cliffs.setCoastal(true);
        int cliffsKey = CoastalTideRenderer.getWaterBordersKey(cliffs, GameConstants.BIOME_CLIFFS.getId());
        assertTrue(cliffsKey != 0);
    }

    @Test
    void bakeCoastalBackgroundModifiesImageOnBeachAndCliffs() {
        BufferedImage beachImg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g1 = beachImg.createGraphics();
        try {
            g1.setColor(new java.awt.Color(200, 180, 140));
            g1.fillRect(0, 0, 800, 600);
            CoastalTideRenderer.bakeCoastalBackground(g1, GameConstants.BIOME_BEACH.getId(), null, 0, 0, 800, 600);
        } finally {
            g1.dispose();
        }
        int bottomPixel = beachImg.getRGB(400, 550);
        assertTrue(bottomPixel != new java.awt.Color(200, 180, 140).getRGB());

        BufferedImage cliffsImg = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = cliffsImg.createGraphics();
        try {
            g2.setColor(new java.awt.Color(120, 120, 120));
            g2.fillRect(0, 0, 800, 600);
            CoastalTideRenderer.bakeCoastalBackground(g2, GameConstants.BIOME_CLIFFS.getId(), null, 0, 0, 800, 600);
        } finally {
            g2.dispose();
        }
        int cliffsBottomPixel = cliffsImg.getRGB(400, 550);
        assertTrue(cliffsBottomPixel != new java.awt.Color(120, 120, 120).getRGB());
    }

    @Test
    void coastlineIsPositionedNearViewportEdgesAndNotCenter() {
        BufferedImage img = new BufferedImage(3800, 3350, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        int baseColor = new java.awt.Color(200, 180, 140).getRGB();
        try {
            g.setColor(new java.awt.Color(200, 180, 140));
            g.fillRect(0, 0, 3800, 3350);
            Rectangle vp = new Rectangle(1175, 1375, 1000, 550);
            CoastalTideRenderer.bakeCoastalBackground(g, GameConstants.BIOME_BEACH.getId(), null, 1400, 1400, 3800, 3350, vp);
        } finally {
            g.dispose();
        }
        int centerPixel = img.getRGB(1675, 1650);
        assertEquals(baseColor, centerPixel);

        int nearBottomPixel = img.getRGB(1900, 3300);
        assertTrue(nearBottomPixel != baseColor);
    }

    @Test
    void peninsulaFiveOfSixNeighborsOceanNorthEastLandLeavesEastWithoutCoast() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex neLand = new Hex();
        neLand.setBiome(GameConstants.BIOME_PLAINS);
        neLand.setContinentId(1);
        hex.setNorthEast(neLand);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setNorth(ocean);
        hex.setSouth(ocean);
        hex.setSouthEast(ocean);
        hex.setNorthWest(ocean);
        hex.setSouthWest(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertTrue(borders.north);
        assertTrue(borders.south);
        assertFalse(borders.east);
        assertTrue(borders.west);
    }

    @Test
    void islandHexHasAllFourBordersWithWater() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);
        hex.setIsland(true);

        Hex land = new Hex();
        land.setBiome(GameConstants.BIOME_PLAINS);
        land.setContinentId(1);
        hex.setNorth(land);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);
        hex.setSouth(ocean);
        hex.setNorthEast(ocean);
        hex.setSouthEast(ocean);
        hex.setNorthWest(ocean);
        hex.setSouthWest(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertTrue(borders.north);
        assertTrue(borders.south);
        assertTrue(borders.east);
        assertTrue(borders.west);
    }

    @Test
    void peninsulaFiveOfSixNeighborsOceanNorthLandLeavesNorthWithoutCoast() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex northLand = new Hex();
        northLand.setBiome(GameConstants.BIOME_PLAINS);
        northLand.setContinentId(1);
        hex.setNorth(northLand);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setSouth(ocean);
        hex.setNorthEast(ocean);
        hex.setSouthEast(ocean);
        hex.setNorthWest(ocean);
        hex.setSouthWest(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertFalse(borders.north);
        assertTrue(borders.south);
        assertTrue(borders.east);
        assertTrue(borders.west);
    }

    @Test
    void peninsulaFiveOfSixNeighborsOceanSouthLandLeavesSouthWithoutCoast() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex southLand = new Hex();
        southLand.setBiome(GameConstants.BIOME_PLAINS);
        southLand.setContinentId(1);
        hex.setSouth(southLand);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setNorth(ocean);
        hex.setNorthEast(ocean);
        hex.setSouthEast(ocean);
        hex.setNorthWest(ocean);
        hex.setSouthWest(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertTrue(borders.north);
        assertFalse(borders.south);
        assertTrue(borders.east);
        assertTrue(borders.west);
    }

    @Test
    void directSouthOnlyWaterDoesNotTriggerEastAndWest() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex land = new Hex();
        land.setBiome(GameConstants.BIOME_PLAINS);
        land.setContinentId(1);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setNorth(land);
        hex.setNorthEast(land);
        hex.setNorthWest(land);
        hex.setSouthEast(land);
        hex.setSouthWest(land);
        hex.setSouth(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertFalse(borders.north);
        assertTrue(borders.south);
        assertFalse(borders.east);
        assertFalse(borders.west);
    }

    @Test
    void oceanEastOrWestAlwaysTriggersEastOrWestOverlay() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex land = new Hex();
        land.setBiome(GameConstants.BIOME_PLAINS);
        land.setContinentId(1);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setNorth(land);
        hex.setSouth(land);
        hex.setNorthWest(land);
        hex.setSouthWest(land);
        hex.setNorthEast(land);
        hex.setSouthEast(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertTrue(borders.east);
        assertFalse(borders.west);
    }

    @Test
    void conditionalSouthAddedWhenSeXorSwAndNorthHasLandPath() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex land = new Hex();
        land.setBiome(GameConstants.BIOME_PLAINS);
        land.setContinentId(1);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setNorth(land);
        hex.setNorthEast(land);
        hex.setNorthWest(land);
        hex.setSouth(land);
        hex.setSouthWest(land);
        hex.setSouthEast(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertTrue(borders.east);
        assertTrue(borders.south);
        assertFalse(borders.north);
        assertFalse(borders.west);
    }

    @Test
    void conditionalSouthNotAddedWhenNorthHasNoLandPath() {
        Hex hex = new Hex();
        hex.setBiome(GameConstants.BIOME_BEACH);
        hex.setCoastal(true);

        Hex land = new Hex();
        land.setBiome(GameConstants.BIOME_PLAINS);
        land.setContinentId(1);

        Hex ocean = new Hex();
        ocean.setBiome(GameConstants.BIOME_OCEAN);
        ocean.setContinentId(0);

        hex.setNorth(ocean);
        hex.setNorthEast(land);
        hex.setNorthWest(land);
        hex.setSouth(land);
        hex.setSouthWest(land);
        hex.setSouthEast(ocean);

        CoastalTideRenderer.WaterBorders borders = CoastalTideRenderer.resolveWaterBorders(hex);
        assertTrue(borders.north);
        assertTrue(borders.east);
        assertFalse(borders.south);
        assertFalse(borders.west);
    }

    @Test
    void cornerTileIsRenderedAtIntersectionOfTwoWaterBorders() {
        BufferedImage img = new BufferedImage(1000, 700, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        int baseColor = new java.awt.Color(200, 180, 140).getRGB();
        try {
            g.setColor(new java.awt.Color(200, 180, 140));
            g.fillRect(0, 0, 1000, 700);

            Hex hex = new Hex();
            hex.setBiome(GameConstants.BIOME_BEACH);
            hex.setCoastal(true);

            Hex land = new Hex();
            land.setBiome(GameConstants.BIOME_PLAINS);
            land.setContinentId(1);

            Hex ocean = new Hex();
            ocean.setBiome(GameConstants.BIOME_OCEAN);
            ocean.setContinentId(0);

            hex.setNorth(land);
            hex.setNorthWest(land);
            hex.setSouthWest(land);
            hex.setSouth(ocean);
            hex.setSouthEast(ocean);
            hex.setNorthEast(ocean);

            CoastalTideRenderer.bakeCoastalBackground(g, GameConstants.BIOME_BEACH.getId(), hex, 0, 0, 1000, 700, new Rectangle(0, 0, 1000, 700));
        } finally {
            g.dispose();
        }

        int cornerX = 1000 - 3 * 64 + 32;
        int cornerY = 700 - 3 * 64 + 32;
        int cornerPixel = img.getRGB(cornerX, cornerY);
        assertTrue(cornerPixel != baseColor);
    }
}


