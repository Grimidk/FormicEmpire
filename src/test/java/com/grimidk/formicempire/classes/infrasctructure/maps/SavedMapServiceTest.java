package com.grimidk.formicempire.classes.infrasctructure.maps;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedMapServiceTest {

    @Test
    void createAndRoundTripMapData(@TempDir Path tempDir) throws IOException {
        World world = new World();
        world.setWorldWidth(6);
        world.setWorldHeight(4);
        world.setHasIceCaps(true);
        world.setIceCapPolarLimit(0.4);
        world.setContinentalLandPercentage(0.5);
        world.setTargetContinentCount(2);
        world.setMinIslandCount(1);
        world.setTectonicPlateCount(4);

        ArrayList<Hex> hexList = new ArrayList<>();
        HashMap<String, Hex> hexMap = new HashMap<>();
        for (int r = 0; r < 4; r++) {
            for (int q = 0; q < 6; q++) {
                Hex h = new Hex();
                h.setQ(q);
                h.setR(r);
                h.setBiome((q % 2 == 0) ? GameConstants.BIOME_PLAINS : GameConstants.BIOME_OCEAN);
                h.setElevation(q + r);
                h.setHydrology(q * 2);
                h.setContinentId((q < 3) ? 1 : 2);
                h.setIsland(q == 5);
                h.setCoastal(q == 2);
                h.setRiverEdge(0, true);
                hexList.add(h);
                hexMap.put(q + "," + r, h);
            }
        }
        world.setHexes(hexList);
        world.linkNeighbors(hexMap);

        SavedMapData data = SavedMapService.createSavedMapData(world, "TestMap");
        assertEquals("TestMap", data.mapName);
        assertEquals(6, data.worldWidth);
        assertEquals(4, data.worldHeight);
        assertEquals(24, data.hexes.size());

        File tempFile = tempDir.resolve("test_map.json").toFile();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try (java.io.Writer writer = new java.io.FileWriter(tempFile, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        }

        SavedMapData loaded = SavedMapService.loadMapData(tempFile);
        assertNotNull(loaded);
        assertEquals("TestMap", loaded.mapName);
        assertEquals(24, loaded.hexes.size());
        assertEquals(world.getHexAt(0, 0).getElevation(), loaded.hexes.get(0).elevation);
        assertEquals(world.getHexAt(0, 0).getHydrology(), loaded.hexes.get(0).hydrology);

        World reconstructed = SavedMapService.buildWorldFromMapData(loaded);
        assertNotNull(reconstructed);
        assertEquals(6, reconstructed.getWorldWidth());
        assertEquals(4, reconstructed.getWorldHeight());
        assertEquals(24, reconstructed.getHexes().size());

        Hex h0 = reconstructed.getHexAt(0, 0);
        assertNotNull(h0);
        assertEquals(GameConstants.BIOME_PLAINS.getId(), h0.getBiome().getId());
        assertEquals(0, h0.getElevation());
        assertEquals(0, h0.getHydrology());
        assertTrue(h0.hasRiverEdge(0));

        Hex h1 = reconstructed.getHexAt(1, 0);
        assertNotNull(h1);
        assertEquals(GameConstants.BIOME_OCEAN.getId(), h1.getBiome().getId());
        assertEquals(1, h1.getElevation());
        assertEquals(2, h1.getHydrology());

        Savefile savefile = new Savefile(1, "TestSave");
        SavedMapService.populateSavefileFromMapData(savefile, loaded);
        assertEquals(6, savefile.getWorldWidth());
        assertEquals(4, savefile.getWorldHeight());
        assertEquals(24, savefile.getWorldHexes().size());
        assertEquals(0, savefile.getWorldHexes().get(0).elevation);
        assertEquals(0, savefile.getWorldHexes().get(0).hydrology);
        assertTrue(savefile.getDynastys().isEmpty());
        assertTrue(savefile.getColonies().isEmpty());
    }

    @Test
    void roundTripsPlacedDynastiesAndColonies() {
        World world = new World();
        world.initializeBlankGrid(8, 4, false, 0);

        Dynasty playerDyn = new Dynasty(1, GameConstants.SPECIES_OMNI.getName(), LanguageStrings.DYNASTY_TITLE_DYNASTY, true, GameConstants.SPECIES_OMNI);
        Colony playerCap = new Colony(1, "Omni Capital", true);
        playerCap.setDynasty(playerDyn);
        playerDyn.addColony(playerCap);
        playerDyn.setCapital(playerCap);

        Colony playerOutpost = new Colony(2, "Omni Outpost", true);
        playerOutpost.setDynasty(playerDyn);
        playerDyn.addColony(playerOutpost);
        world.registerDynasty(playerDyn);

        Dynasty aiDyn = new Dynasty(2, GameConstants.SPECIES_LEAFCUTTER.getName(), LanguageStrings.DYNASTY_TITLE_DYNASTY, false, GameConstants.SPECIES_LEAFCUTTER);
        Colony aiCap = new Colony(3, "Leafcutter Capital", false);
        aiCap.setDynasty(aiDyn);
        aiDyn.addColony(aiCap);
        aiDyn.setCapital(aiCap);
        world.registerDynasty(aiDyn);

        Hex h1 = world.findHex(0, 0);
        Hex h2 = world.findHex(1, 0);
        Hex h3 = world.findHex(-1, 0);
        assertNotNull(h1);
        assertNotNull(h2);
        assertNotNull(h3);
        h1.setColony(playerCap);
        h2.setColony(playerOutpost);
        h3.setColony(aiCap);

        SavedMapData mapData = SavedMapService.createSavedMapData(world, "DynastyMap");
        World rebuilt = SavedMapService.buildWorldFromMapData(mapData);
        assertEquals(2, rebuilt.getDynastys().size());

        Hex rh1 = rebuilt.findHex(0, 0);
        Hex rh2 = rebuilt.findHex(1, 0);
        Hex rh3 = rebuilt.findHex(-1, 0);
        assertNotNull(rh1.getColony());
        assertNotNull(rh2.getColony());
        assertNotNull(rh3.getColony());
        assertTrue(rh1.getColony().isPlayer());
        assertTrue(rh1.getColony().isCapital());
        assertEquals("Omni Capital", rh1.getColony().getName());
        assertEquals(GameConstants.SPECIES_OMNI.getId(), rh1.getColony().getDynasty().getSpecies().getId());

        assertTrue(rh2.getColony().isPlayer());
        assertFalse(rh2.getColony().isCapital());
        assertEquals("Omni Outpost", rh2.getColony().getName());

        assertFalse(rh3.getColony().isPlayer());
        assertTrue(rh3.getColony().isCapital());
        assertEquals("Leafcutter Capital", rh3.getColony().getName());
        assertEquals(GameConstants.SPECIES_LEAFCUTTER.getId(), rh3.getColony().getDynasty().getSpecies().getId());

        Savefile savefile = new Savefile(1, "DynastySave");
        SavedMapService.populateSavefileFromMapData(savefile, mapData);
        assertEquals(2, savefile.getDynastys().size());
        assertEquals(3, savefile.getColonies().size());

        Savefile.SavedDynasty savedPlayerDyn = savefile.getDynastys().stream()
                .filter(d -> d.isPlayer)
                .findFirst().orElse(null);
        assertNotNull(savedPlayerDyn);
        assertEquals(GameConstants.SPECIES_OMNI.getId(), savedPlayerDyn.speciesId);

        Savefile.SavedDynasty savedAiDyn = savefile.getDynastys().stream()
                .filter(d -> !d.isPlayer)
                .findFirst().orElse(null);
        assertNotNull(savedAiDyn);
        assertEquals(GameConstants.SPECIES_LEAFCUTTER.getId(), savedAiDyn.speciesId);
    }

    @Test
    void inferLastStageAndBuilderStagePersistence(@TempDir Path tempDir) throws IOException {
        World world = new World();
        world.initializeBlankGrid(8, 4, false, 0);
        assertEquals(0, SavedMapService.inferLastStage(null, world));

        Hex h = world.findHex(0, 0);
        assertNotNull(h);
        h.setPlateId(1);
        assertEquals(1, SavedMapService.inferLastStage(null, world));

        h.setElevation(5);
        h.setBiome(GameConstants.BIOME_PLAINS);
        assertEquals(2, SavedMapService.inferLastStage(null, world));

        h.setBiome(GameConstants.BIOME_FOREST);
        assertEquals(3, SavedMapService.inferLastStage(null, world));

        Dynasty playerDyn = new Dynasty(1, GameConstants.SPECIES_OMNI.getName(), LanguageStrings.DYNASTY_TITLE_DYNASTY, true, GameConstants.SPECIES_OMNI);
        Colony cap = new Colony(1, "Capital", true);
        cap.setDynasty(playerDyn);
        playerDyn.addColony(cap);
        playerDyn.setCapital(cap);
        world.registerDynasty(playerDyn);
        h.setColony(cap);
        assertEquals(4, SavedMapService.inferLastStage(null, world));

        SavedMapData explicitData = SavedMapService.createSavedMapData(world, "ExplicitStageMap", 2);
        File file = tempDir.resolve("explicit_stage.json").toFile();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        try (java.io.Writer writer = new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            gson.toJson(explicitData, writer);
        }
        SavedMapData loaded = SavedMapService.loadMapData(file);
        assertEquals(Integer.valueOf(2), loaded.builderStage);
        assertEquals(2, SavedMapService.inferLastStage(loaded, SavedMapService.buildWorldFromMapData(loaded)));
    }
}
