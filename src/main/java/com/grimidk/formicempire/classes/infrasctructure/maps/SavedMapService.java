package com.grimidk.formicempire.classes.infrasctructure.maps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.util.GamePaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedMapService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static File getSavedMapsDirectory() {
        File dir = new File(GamePaths.resolveSavesDirectory(), "saved_maps");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean hasSavedMaps() {
        File[] files = getSavedMapFiles();
        return files != null && files.length > 0;
    }

    public static File[] getSavedMapFiles() {
        File dir = getSavedMapsDirectory();
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".json"));
        if (files == null) {
            return new File[0];
        }
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return files;
    }

    public static SavedMapData createSavedMapData(World world, String mapName) {
        SavedMapData data = new SavedMapData();
        data.mapName = mapName;
        data.createdAt = System.currentTimeMillis();
        data.version = SaveManager.getAppVersion();
        data.worldWidth = world.getWorldWidth();
        data.worldHeight = world.getWorldHeight();
        data.hasIceCaps = world.hasIceCaps();
        data.iceCapPolarLimit = world.getIceCapPolarLimit();
        data.continentalLandPercentage = world.getContinentalLandPercentage();
        data.targetContinentCount = world.getTargetContinentCount();
        data.minIslandCount = world.getMinIslandCount();
        data.tectonicPlateCount = world.getTectonicPlateCount();
        data.continentCount = world.getContinentCount();
        data.islandCount = world.getIslandCount();

        if (world.getHexes() != null) {
            for (Hex h : world.getHexes()) {
                if (h == null) continue;
                SavedMapData.SavedMapHex sh = new SavedMapData.SavedMapHex();
                sh.q = h.getQ();
                sh.r = h.getR();
                sh.biomeId = (h.getBiome() != null) ? h.getBiome().getId() : 1;
                sh.elevation = h.getElevation();
                sh.hydrology = h.getHydrology();
                sh.timeOffset = h.getTimeOffset();
                sh.weatherId = (h.getLocalWeather() != null) ? h.getLocalWeather().getId() : 1;
                sh.continentId = h.getContinentId();
                sh.island = h.isIsland();
                sh.waterDistance = h.getWaterDistance();
                sh.coastal = h.isCoastal();
                sh.nonWaterResourceSourcesGenerated = h.getNonWaterResourceSourcesGenerated();
                sh.plateId = h.getPlateId();
                if (h.getRiverEdges() != null) {
                    System.arraycopy(h.getRiverEdges(), 0, sh.riverEdges, 0, Math.min(6, h.getRiverEdges().length));
                }
                data.hexes.add(sh);
            }
        }
        return data;
    }

    public static File saveMap(World world, String mapName) throws IOException {
        SavedMapData data = createSavedMapData(world, mapName);
        File dir = getSavedMapsDirectory();
        String safeName = mapName.replaceAll("[^a-zA-Z0-9._\\- ]", "_").trim();
        if (safeName.isEmpty()) {
            safeName = "Map_" + System.currentTimeMillis();
        }
        File targetFile = new File(dir, safeName + ".json");
        try (Writer writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            GSON.toJson(data, writer);
        }
        return targetFile;
    }

    public static SavedMapData loadMapData(File file) throws IOException {
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, SavedMapData.class);
        }
    }

    public static World buildWorldFromMapData(SavedMapData data) {
        World world = new World();
        world.setWorldWidth(data.worldWidth);
        world.setWorldHeight(data.worldHeight);
        world.setHasIceCaps(data.hasIceCaps);
        world.setIceCapPolarLimit(data.iceCapPolarLimit);
        world.setContinentalLandPercentage(data.continentalLandPercentage);
        world.setTargetContinentCount(data.targetContinentCount);
        world.setMinIslandCount(data.minIslandCount);
        world.setTectonicPlateCount(data.tectonicPlateCount);

        Map<String, Hex> hexMap = new HashMap<>();
        ArrayList<Hex> hexList = new ArrayList<>();
        if (data.hexes != null) {
            for (SavedMapData.SavedMapHex sh : data.hexes) {
                Hex h = new Hex();
                h.setQ(sh.q);
                h.setR(sh.r);
                h.setBiome(world.getBiomeById(sh.biomeId));
                h.setElevation(sh.elevation);
                h.setHydrology(sh.hydrology);
                h.setTimeOffset(sh.timeOffset);
                h.setLocalWeather(world.getWeatherById(sh.weatherId));
                h.setContinentId(sh.continentId);
                h.setIsland(sh.island);
                h.setWaterDistance(sh.waterDistance);
                h.setCoastal(sh.coastal);
                h.setNonWaterResourceSourcesGenerated(sh.nonWaterResourceSourcesGenerated);
                h.setPlateId(sh.plateId);
                if (sh.riverEdges != null) {
                    for (int i = 0; i < Math.min(6, sh.riverEdges.length); i++) {
                        h.setRiverEdge(i, sh.riverEdges[i]);
                    }
                }
                hexMap.put(sh.q + "," + sh.r, h);
                hexList.add(h);
            }
        }
        world.setHexes(hexList);
        world.linkNeighbors(hexMap);
        world.classifyLandmasses();
        world.ensureFaultLinesReconstructed();
        return world;
    }

    public static void populateSavefileFromMapData(Savefile save, SavedMapData data) {
        save.setWorldWidth(data.worldWidth);
        save.setWorldHeight(data.worldHeight);
        save.setWorldRadius(data.worldWidth / 2);
        List<Savefile.SavedHex> hexes = new ArrayList<>();
        if (data.hexes != null) {
            for (SavedMapData.SavedMapHex sh : data.hexes) {
                Savefile.SavedHex hex = new Savefile.SavedHex(sh.q, sh.r, sh.biomeId, false, sh.timeOffset, sh.weatherId);
                hex.elevation = sh.elevation;
                hex.hydrology = sh.hydrology;
                hex.island = sh.island;
                hex.continentId = sh.continentId;
                hex.waterDistance = sh.waterDistance;
                hex.coastal = sh.coastal;
                hex.nonWaterResourceSourcesGenerated = sh.nonWaterResourceSourcesGenerated;
                hex.plateId = sh.plateId;
                if (sh.riverEdges != null) {
                    System.arraycopy(sh.riverEdges, 0, hex.riverEdges, 0, Math.min(6, sh.riverEdges.length));
                }
                hexes.add(hex);
            }
        }
        save.setWorldHexes(hexes);
    }
}
