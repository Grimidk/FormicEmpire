package com.grimidk.formicempire.classes.infrasctructure.maps;

import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
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
    }
}
