package com.grimidk.formicempire.classes.infrasctructure.maps;

import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldBuilderTest {

    @Test
    void testBlankGridInitialization() {
        World world = new World();
        world.initializeBlankGrid(16, 8, true, 0.35);

        assertEquals(16, world.getWorldWidth());
        assertEquals(8, world.getWorldHeight());
        assertTrue(world.hasIceCaps());
        assertEquals(0.35, world.getIceCapPolarLimit());
        assertEquals(16 * 8, world.getHexes().size());
        assertTrue(world.getDynastys().isEmpty());

        for (Hex h : world.getHexes()) {
            assertNotNull(h.getBiome());
            assertTrue(h.getElevation() >= 0 && h.getElevation() <= 1);
            assertEquals(0, h.getPlateId());
        }
    }

    @Test
    void testFaultLinesAndSavedMapRoundtrip(@TempDir Path tempDir) throws IOException {
        World world = new World();
        world.initializeBlankGrid(12, 6, false, 0);

        Hex midHex = world.findHex(0, 0);
        assertNotNull(midHex);
        midHex.setFaultType(FaultLine.FaultType.CONVERGENT);
        midHex.setFaultIntensity(3);

        SavedMapData mapData = SavedMapService.createSavedMapData(world, "FaultTest");
        assertEquals("FaultTest", mapData.mapName);
        SavedMapData.SavedMapHex savedHex = mapData.hexes.stream()
                .filter(sh -> sh.q == 0 && sh.r == 0)
                .findFirst().orElse(null);
        assertNotNull(savedHex);
        assertEquals("CONVERGENT", savedHex.faultType);
        assertEquals(3, savedHex.faultIntensity);

        File testFile = tempDir.resolve("FaultTest.json").toFile();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try (java.io.Writer writer = new java.io.FileWriter(testFile, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(mapData, writer);
        }

        SavedMapData loaded = SavedMapService.loadMapData(testFile);
        assertNotNull(loaded);
        SavedMapData.SavedMapHex reloadedHex = loaded.hexes.stream()
                .filter(sh -> sh.q == 0 && sh.r == 0)
                .findFirst().orElse(null);
        assertNotNull(reloadedHex);
        assertEquals("CONVERGENT", reloadedHex.faultType);
        assertEquals(3, reloadedHex.faultIntensity);
    }

    @Test
    void testWorldBuilderStagesWorkflow(@TempDir Path tempDir) throws IOException {
        World world = new World();
        world.initializeBlankGrid(12, 6, true, 0.38);

        world.generateTectonicPlates();
        boolean hasPlates = world.getHexes().stream().anyMatch(h -> h.getPlateId() > 0);
        assertTrue(hasPlates);

        world.generateDefaultPlatesAndElevation();
        boolean hasElevation = world.getHexes().stream().anyMatch(h -> h.getElevation() > 0);
        assertTrue(hasElevation);

        world.assignBiomesToLandHexes();
        boolean hasLandBiome = world.getHexes().stream().anyMatch(h ->
                h.getBiome() != null
                        && h.getBiome() != GameConstants.BIOME_OCEAN
                        && h.getBiome() != GameConstants.BIOME_DEEP_OCEAN
                        && h.getBiome() != GameConstants.BIOME_ABYSS);
        assertTrue(hasLandBiome);

        SavedMapData mapData = SavedMapService.createSavedMapData(world, "BuilderTest");
        assertEquals("BuilderTest", mapData.mapName);
        assertEquals(12, mapData.worldWidth);
        assertEquals(6, mapData.worldHeight);
        assertEquals(72, mapData.hexes.size());

        File testFile = tempDir.resolve("BuilderTest.json").toFile();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try (java.io.Writer writer = new java.io.FileWriter(testFile, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(mapData, writer);
        }

        SavedMapData reloaded = SavedMapService.loadMapData(testFile);
        assertNotNull(reloaded);
        assertEquals("BuilderTest", reloaded.mapName);
        assertEquals(12, reloaded.worldWidth);
        assertEquals(6, reloaded.worldHeight);
        assertEquals(72, reloaded.hexes.size());
    }

    @Test
    void testAlgorithmicPlateFaultLandAndBiomeSteps() {
        World world = new World();
        world.initializeBlankGrid(24, 12, true, 0.82);

        assertNotNull(world.getHexAt(0, 0));
        assertNotNull(world.findHex(0, 0));
        assertNull(world.getHexAt(999, 999));
        assertNull(world.findHex(999, 999));

        world.generateTectonicPlates();
        world.generateFaultLinesFromPlates();
        assertFalse(world.getTectonicPlates().isEmpty());
        assertFalse(world.getFaultLines().isEmpty());
        assertTrue(world.getHexes().stream().anyMatch(h -> h.getFaultType() != null));

        world.generateAlgorithmicLandmasses();
        assertTrue(world.getHexes().stream().anyMatch(h -> h.getContinentId() > 0));

        world.generateAlgorithmicBiomesAndFeatures();
        assertTrue(world.getContinentCount() >= 1);
        assertFalse(world.getGeographicFeatures().isEmpty());
    }
}
