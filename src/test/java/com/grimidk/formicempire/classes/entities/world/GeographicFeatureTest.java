package com.grimidk.formicempire.classes.entities.world;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.world.GeographicFeatureNamingService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapData;
import com.grimidk.formicempire.classes.infrasctructure.maps.SavedMapService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeographicFeatureTest {

    @Test
    void geographicFeatureCentroidCalculation() {
        GeographicFeature feature = new GeographicFeature(1, GeographicFeature.FeatureType.MOUNTAIN_RANGE, "Test Range");
        Hex h1 = new Hex();
        h1.setQ(0);
        h1.setR(0);
        Hex h2 = new Hex();
        h2.setQ(2);
        h2.setR(0);

        feature.addHex(h1);
        feature.addHex(h2);
        assertEquals(2, feature.size());
        assertTrue(feature.containsHex(h1));
        assertTrue(feature.containsHex(h2));

        feature.recalculateCentroid(50);
        assertEquals(0.0, feature.getCentroidR(), 1e-4);
        Hex centroidHex = feature.getCentroidHex();
        assertNotNull(centroidHex);
    }

    @Test
    void namingServiceClaimsUniqueKeysAndCyclesRomanNumerals() {
        GeographicFeatureNamingService naming = new GeographicFeatureNamingService();
        String k1 = naming.claimNameKey(GeographicFeature.FeatureType.RIVER, 1);
        String k2 = naming.claimNameKey(GeographicFeature.FeatureType.RIVER, 2);
        assertNotNull(k1);
        assertNotNull(k2);

        String name1 = naming.formatDisplayName(k1, 1);
        assertNotNull(name1);
        assertFalse(name1.isEmpty());

        String dupName = naming.formatDisplayName(k1, 2);
        assertTrue(dupName.contains("II"));
    }

    @Test
    void worldGeneratesAndClustersGeographicFeatures() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        assertFalse(world.getGeographicFeatures().isEmpty());

        boolean hasMountain = false;
        boolean hasLake = false;
        boolean hasRiver = false;
        for (GeographicFeature gf : world.getGeographicFeatures()) {
            assertNotNull(gf.getName());
            assertNotNull(gf.getType());
            assertNotNull(gf.getCentroidHex());
            if (gf.getType() == GeographicFeature.FeatureType.MOUNTAIN_RANGE) hasMountain = true;
            if (gf.getType() == GeographicFeature.FeatureType.LAKE_BASIN) hasLake = true;
            if (gf.getType() == GeographicFeature.FeatureType.RIVER) hasRiver = true;
        }

        assertTrue(hasMountain || hasLake || hasRiver);
    }

    @Test
    void savedMapServicePreservesGeographicFeatures() {
        World world = new World();
        world.setWorldWidth(10);
        world.setWorldHeight(10);

        Map<String, Hex> hexMap = new HashMap<>();
        for (int r = -5; r < 5; r++) {
            for (int q = -5; q < 5; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome(GameConstants.BIOME_PLAINS);
                h.setElevation(2);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new java.util.ArrayList<>(hexMap.values()));

        Hex m1 = hexMap.get("0,0");
        Hex m2 = hexMap.get("1,0");
        m1.setBiome(GameConstants.BIOME_MOUNTAIN);
        m1.setElevation(8);
        m2.setBiome(GameConstants.BIOME_PEAK);
        m2.setElevation(9);

        world.generateGeographicFeatures();
        assertFalse(world.getGeographicFeatures().isEmpty());

        GeographicFeature feat = world.getFeatureForHex(m1);
        assertNotNull(feat);
        assertEquals(GeographicFeature.FeatureType.MOUNTAIN_RANGE, feat.getType());

        SavedMapData data = SavedMapService.createSavedMapData(world, "TestMap");
        assertFalse(data.features.isEmpty());

        World loaded = SavedMapService.buildWorldFromMapData(data);
        assertFalse(loaded.getGeographicFeatures().isEmpty());
        Hex loadedM1 = loaded.getHexAt(0, 0);
        assertNotNull(loadedM1);
        GeographicFeature loadedFeat = loaded.getFeatureForHex(loadedM1);
        assertNotNull(loadedFeat);
        assertEquals(GeographicFeature.FeatureType.MOUNTAIN_RANGE, loadedFeat.getType());
        assertEquals(feat.getName(), loadedFeat.getName());
    }

    @Test
    void namingServiceHandlesOceansAndPeninsulas() {
        GeographicFeatureNamingService naming = new GeographicFeatureNamingService();
        String oceanKey = naming.claimNameKey(GeographicFeature.FeatureType.OCEAN, 1);
        assertNotNull(oceanKey);
        String oceanName = naming.formatDisplayName(oceanKey, 1);
        assertNotNull(oceanName);
        assertFalse(oceanName.isEmpty());

        String penKey = naming.claimNameKey(GeographicFeature.FeatureType.PENINSULA, 1);
        assertNotNull(penKey);
        String penName = naming.formatDisplayName(penKey, 1);
        assertNotNull(penName);
        assertFalse(penName.isEmpty());
    }

    @Test
    void worldGeneratesContinentsAndOceansFeatures() {
        World world = new World();
        Colony colony = new Colony(1, "Capital", true);
        world.generateRectangularWorld(
                GameConstants.BIOME_PLAINS,
                colony,
                "Player",
                LanguageStrings.DYNASTY_TITLE_DYNASTY);

        boolean hasContinent = false;
        boolean hasOcean = false;
        for (GeographicFeature gf : world.getGeographicFeatures()) {
            if (gf.getType() == GeographicFeature.FeatureType.CONTINENT) hasContinent = true;
            if (gf.getType() == GeographicFeature.FeatureType.OCEAN) hasOcean = true;
        }
        assertTrue(hasContinent || hasOcean);
    }

    @Test
    void savedMapServiceFileOperations() throws java.io.IOException {
        World world = new World();
        world.setWorldWidth(5);
        world.setWorldHeight(5);
        world.setHexes(new java.util.ArrayList<>());
        java.io.File savedFile = SavedMapService.saveMap(world, "TestMapToDelete_" + System.currentTimeMillis());
        assertTrue(savedFile.exists());

        boolean deleted = SavedMapService.deleteMapFile(savedFile);
        assertTrue(deleted);
        assertFalse(savedFile.exists());
    }

    @Test
    void allBiomesHaveUniqueMapColors() {
        java.util.Set<String> colors = new java.util.HashSet<>();
        for (com.grimidk.formicempire.classes.constants.world.Biome biome : GameConstants.getBiomes()) {
            assertNotNull(biome.getMapColorHex());
            assertFalse(biome.getMapColorHex().isEmpty());
            assertTrue(colors.add(biome.getMapColorHex().toLowerCase(java.util.Locale.ROOT)),
                    "Duplicate map color found for biome: " + biome.getId() + " (" + biome.getMapColorHex() + ")");
        }
        assertEquals(23, colors.size());
    }

    @Test
    void namingServiceHandlesTrenchesAndArchipelagos() {
        GeographicFeatureNamingService naming = new GeographicFeatureNamingService();
        String trenchKey = naming.claimNameKey(GeographicFeature.FeatureType.TRENCH, 1);
        assertNotNull(trenchKey);
        String trenchName = naming.formatDisplayName(trenchKey, 1);
        assertNotNull(trenchName);
        assertFalse(trenchName.isEmpty());

        String archKey = naming.claimNameKey(GeographicFeature.FeatureType.ARCHIPELAGO, 1);
        assertNotNull(archKey);
        String archName = naming.formatDisplayName(archKey, 1);
        assertNotNull(archName);
        assertFalse(archName.isEmpty());
    }

    @Test
    void worldClustersAbyssHexesIntoTrenchFeatures() {
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
                h.setElevation(0);
                hexMap.put(q + "," + r, h);
            }
        }
        world.linkNeighbors(hexMap);
        world.setHexes(new java.util.ArrayList<>(hexMap.values()));

        Hex t1 = hexMap.get("0,0");
        Hex t2 = hexMap.get("1,0");
        t1.setBiome(GameConstants.BIOME_ABYSS);
        t1.setElevation(-2);
        t2.setBiome(GameConstants.BIOME_ABYSS);
        t2.setElevation(-2);

        world.generateGeographicFeatures();

        GeographicFeature trench = world.getFeatureForHex(t1);
        assertNotNull(trench);
        assertEquals(GeographicFeature.FeatureType.TRENCH, trench.getType());
        assertTrue(trench.containsHex(t1));
        assertTrue(trench.containsHex(t2));
    }
}
