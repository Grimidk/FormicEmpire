package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.entities.services.shared.HexWaterCrossing;
import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapHeightAndIceTest {

    @Test
    void modeButtonGlyphAndTooltipTranslations() {
        assertEquals("<>", LanguageStrings.get(LanguageStrings.MAP_MODE_BUTTON));
        assertTrue(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_HEIGHT).contains("Height:"));
        assertTrue(LanguageStrings.get(LanguageStrings.MULTI_MAP_PARAM_LAND_SHARE).contains("Cont. Land"));
    }

    @Test
    void iceFieldsConfinedToIceCapsAndHeightHasVariations() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        Set<Integer> continentalElevations = new HashSet<>();
        int continentalHexCount = 0;
        int iceCapCount = 0;

        for (Hex h : world.getHexes()) {
            if (h == null) continue;
            boolean isLand = (h.getContinentId() > 0 || h.isIsland());
            if (isLand) {
                assertFalse(
                        h.getBiome() == GameConstants.BIOME_ICE_FIELD,
                        "Continental hex or island must never have BIOME_ICE_FIELD");
                if (h.getBiome() != GameConstants.BIOME_LAKE) {
                    continentalElevations.add(h.getElevation());
                    continentalHexCount++;
                }
            } else if (h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                iceCapCount++;
                assertEquals(0, h.getContinentId());
                assertFalse(h.isIsland());
            }
        }

        assertTrue(continentalHexCount > 50, "World should generate a substantial continental landmass");
        assertTrue(
                continentalElevations.size() >= 3,
                "Continental land hexes must exhibit multiple distinct elevation levels instead of a flat slab");
        assertTrue(iceCapCount > 0, "Polar ice caps should have ice field hexes");
    }

    @Test
    void tooltipContainsHeightForHex() throws Exception {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                dialog.setSize(800, 600);
                dialog.doLayout();
                MouseEvent evt = new MouseEvent(dialog, MouseEvent.MOUSE_MOVED, System.currentTimeMillis(), 0, 400, 300, 0, false);
                String tooltip = dialog.getHexToolTip(evt);
                if (tooltip != null) {
                    assertTrue(tooltip.contains(LanguageStrings.get(LanguageStrings.MAP_TOOLTIP_HEIGHT)));
                }
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void islandsHaveDiverseNormalBiomes() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        int nonPolarIslands = 0;
        int normalBiomeIslands = 0;
        int maxLat = world.getWorldHeight() / 2;
        int polarLimit = Math.max(2, (int) Math.round(maxLat * 0.70));

        for (Hex h : world.getHexes()) {
            if (h != null && h.isIsland()) {
                if (Math.abs(h.getR()) < polarLimit) {
                    nonPolarIslands++;
                    if (h.getBiome() != GameConstants.BIOME_ICE_FIELD && h.getBiome() != GameConstants.BIOME_TUNDRA) {
                        normalBiomeIslands++;
                    }
                }
            }
        }

        assertTrue(nonPolarIslands > 0, "World should generate non-polar islands");
        assertTrue(normalBiomeIslands > 0, "Islands must have normal biomes instead of all being ice/tundra");
        assertTrue((double) normalBiomeIslands / nonPolarIslands >= 0.7,
                "Most non-polar islands should have normal biomes");
    }

    @Test
    void deepOceanHasLowestElevationAndRegularOceanDominates() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        int regularOceanCount = 0;
        int deepOceanCount = 0;

        for (Hex h : world.getHexes()) {
            if (h == null) continue;
            if (h.getBiome() == GameConstants.BIOME_DEEP_OCEAN) {
                deepOceanCount++;
                assertEquals(0, h.getElevation(), "Deep ocean must strictly have the lowest elevation (0)");
            } else if (h.getBiome() == GameConstants.BIOME_OCEAN) {
                regularOceanCount++;
                assertEquals(1, h.getElevation(), "Regular ocean should have elevation 1");
            }
        }

        assertTrue(regularOceanCount > deepOceanCount,
                "Regular ocean should be more common than deep ocean (regular: " + regularOceanCount + ", deep: " + deepOceanCount + ")");
    }

    @Test
    void riversFlowDownhillOrStayFlat() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        int riverEdgeCount = 0;
        for (Hex h : world.getHexes()) {
            if (h == null) continue;
            for (int d = 0; d < 6; d++) {
                if (h.hasRiverEdge(d)) {
                    riverEdgeCount++;
                    Hex neighbor = h.getNeighbor(d);
                    if (neighbor != null) {
                        int diff = Math.abs(h.getElevation() - neighbor.getElevation());
                        assertTrue(diff <= 8, "Adjacent hexes sharing a river edge should have reasonable elevation difference (got " + diff + " between " + h.getElevation() + " and " + neighbor.getElevation() + ")");
                    }
                }
            }
        }
        assertTrue(riverEdgeCount > 0, "World should have generated river networks");
    }

    @Test
    void allBiomesAreSpawnedInGeneratedWorld() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        java.util.Map<com.grimidk.formicempire.classes.constants.world.Biome, Integer> counts = new java.util.HashMap<>();
        for (Hex h : world.getHexes()) {
            if (h != null && h.getBiome() != null) {
                counts.merge(h.getBiome(), 1, Integer::sum);
            }
        }
        for (com.grimidk.formicempire.classes.constants.world.Biome b : GameConstants.getBiomes()) {
            assertTrue(counts.getOrDefault(b, 0) > 0,
                    "Expected biome " + b.getId() + " (" + b.getName() + ") to be present in generated world, but count was 0");
        }
    }

    @Test
    void peaksOriginateRiversAndRiversFork() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "TestPlayer",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        int peakRiverCount = 0;
        int peakTotal = 0;
        int forkingHexCount = 0;

        for (Hex h : world.getHexes()) {
            if (h == null) continue;
            if (h.getBiome() == GameConstants.BIOME_PEAK) {
                peakTotal++;
                if (h.getRiverEdgeCount() > 0) {
                    peakRiverCount++;
                }
            }
            if (h.getRiverEdgeCount() >= 3) {
                forkingHexCount++;
            }
        }

        assertTrue(peakTotal > 0, "World should contain peak hexes");
        assertTrue(peakRiverCount > 0, "At least some peak hexes must have river edges originating on them");
        assertTrue(forkingHexCount > 0, "River networks should exhibit forks (hexes with 3 river edges)");
    }

    @Test
    void mapDialogLegendCanBeHiddenForGallery() throws Exception {
        World world = new World();
        SwingUtilities.invokeAndWait(() -> {
            MapDialog dialog = new MapDialog(null, null, world, null, null);
            try {
                assertTrue(dialog.isLegendVisible());
                dialog.setLegendVisible(false);
                assertFalse(dialog.isLegendVisible());
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void savedMapSelectionDialogInitializesWithFullScreenBounds() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            SavedMapSelectionDialog dialog = new SavedMapSelectionDialog(null);
            try {
                java.awt.Rectangle maxBounds = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                assertEquals(maxBounds.width, dialog.getWidth(), "Dialog width should match maximum usable window width");
                assertEquals(maxBounds.height, dialog.getHeight(), "Dialog height should match maximum usable window height");
            } finally {
                dialog.dispose();
            }
        });
    }

    @Test
    void robustConnectionFusesLandmassesIntoSingleContinent() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex c1a = hexMap.get("0,0");
        Hex c1b = hexMap.get("0,1");
        c1a.setBiome(GameConstants.BIOME_PLAINS);
        c1a.setElevation(3);
        c1a.setContinentId(1);
        c1b.setBiome(GameConstants.BIOME_PLAINS);
        c1b.setElevation(3);
        c1b.setContinentId(1);

        Hex c2a = hexMap.get("2,0");
        Hex c2b = hexMap.get("2,1");
        c2a.setBiome(GameConstants.BIOME_FOREST);
        c2a.setElevation(4);
        c2a.setContinentId(2);
        c2b.setBiome(GameConstants.BIOME_FOREST);
        c2b.setElevation(4);
        c2b.setContinentId(2);

        Hex strait1 = hexMap.get("1,0");
        Hex strait2 = hexMap.get("1,1");
        strait1.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
        strait1.setElevation(2);
        strait2.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
        strait2.setElevation(2);

        world.fuseRobustlyConnectedLandmasses();
        world.classifyLandmasses();

        assertEquals(1, world.getContinentCount(), "Two continents with 2 straits must fuse to a single continent");
        assertEquals(1, c1a.getContinentId());
        assertEquals(1, c2a.getContinentId(), "Continent 2 must take the fused continentId (1)");
        assertFalse(c2a.isIsland());
        assertFalse(HexWaterCrossing.isWaterBiome(strait1.getBiome()), "Strait 1 must be bridged to land");
        assertFalse(HexWaterCrossing.isWaterBiome(strait2.getBiome()), "Strait 2 must be bridged to land");
        assertEquals(1, strait1.getContinentId());
        assertEquals(1, strait2.getContinentId());
    }

    @Test
    void singleStraitConnectionPreservesSeparateLandmasses() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex c1 = hexMap.get("0,0");
        c1.setBiome(GameConstants.BIOME_PLAINS);
        c1.setElevation(3);
        c1.setContinentId(1);

        Hex c2 = hexMap.get("2,0");
        c2.setBiome(GameConstants.BIOME_FOREST);
        c2.setElevation(3);
        c2.setContinentId(2);

        Hex strait = hexMap.get("1,0");
        strait.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
        strait.setElevation(2);

        world.fuseRobustlyConnectedLandmasses();
        world.classifyLandmasses();

        assertEquals(2, world.getContinentCount(), "A single strait connection must preserve separate continents");
        assertEquals(1, c1.getContinentId());
        assertEquals(2, c2.getContinentId());
        assertTrue(HexWaterCrossing.isWaterBiome(strait.getBiome()), "Single strait water hex must remain water");
    }

    @Test
    void directLandContactFusesLandmassesIntoSingleContinent() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex c1 = hexMap.get("0,0");
        c1.setBiome(GameConstants.BIOME_PLAINS);
        c1.setElevation(3);
        c1.setContinentId(1);

        Hex c2 = hexMap.get("1,0");
        c2.setBiome(GameConstants.BIOME_FOREST);
        c2.setElevation(4);
        c2.setContinentId(2);

        world.fuseRobustlyConnectedLandmasses();
        world.classifyLandmasses();

        assertEquals(1, world.getContinentCount(), "Direct land contact must fuse landmasses into 1 continent");
        assertEquals(1, c1.getContinentId());
        assertEquals(1, c2.getContinentId());
    }

    @Test
    void iceCapsDoNotFuseWithNonIceLandmasses() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setElevation(1);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new ArrayList<>(hexMap.values()));

        Hex ice1 = hexMap.get("0,4");
        Hex ice2 = hexMap.get("1,4");
        ice1.setBiome(GameConstants.BIOME_ICE_FIELD);
        ice1.setElevation(4);
        ice2.setBiome(GameConstants.BIOME_ICE_FIELD);
        ice2.setElevation(4);

        Hex cont1 = hexMap.get("0,2");
        Hex cont2 = hexMap.get("1,2");
        cont1.setBiome(GameConstants.BIOME_PLAINS);
        cont1.setElevation(3);
        cont1.setContinentId(1);
        cont2.setBiome(GameConstants.BIOME_PLAINS);
        cont2.setElevation(3);
        cont2.setContinentId(1);

        Hex bridge1 = hexMap.get("0,3");
        Hex bridge2 = hexMap.get("1,3");
        bridge1.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
        bridge1.setElevation(2);
        bridge2.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
        bridge2.setElevation(2);

        world.fuseRobustlyConnectedLandmasses();
        world.classifyLandmasses();

        assertEquals(1, world.getContinentCount(), "Continent count should only count non-ice-cap continent");
        assertEquals(1, cont1.getContinentId());
        assertEquals(0, ice1.getContinentId(), "Ice cap hex must not be fused to continent");
        assertTrue(HexWaterCrossing.isWaterBiome(bridge1.getBiome()), "Strait next to ice cap must not be bridged to fuse with ice cap");
    }
}

