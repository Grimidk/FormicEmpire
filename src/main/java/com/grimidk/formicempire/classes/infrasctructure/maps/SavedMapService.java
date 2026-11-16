package com.grimidk.formicempire.classes.infrasctructure.maps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.GeographicFeature;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
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

    public static boolean deleteMapFile(File file) {
        if (file != null && file.exists()) {
            return file.delete();
        }
        return false;
    }

    public static boolean clearAllSavedMaps() {
        File[] files = getSavedMapFiles();
        boolean allSuccess = true;
        for (File f : files) {
            if (f != null && f.exists()) {
                if (!f.delete()) {
                    allSuccess = false;
                }
            }
        }
        return allSuccess;
    }

    public static SavedMapData createSavedMapData(World world, String mapName) {
        return createSavedMapData(world, mapName, null);
    }

    public static SavedMapData createSavedMapData(World world, String mapName, Integer builderStage) {
        SavedMapData data = new SavedMapData();
        data.mapName = mapName;
        data.createdAt = System.currentTimeMillis();
        data.version = SaveManager.getAppVersion();
        data.builderStage = builderStage;
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
                if (h.getFaultType() != null) {
                    sh.faultType = h.getFaultType().name();
                    sh.faultIntensity = h.getFaultIntensity();
                }
                if (h.getRiverEdges() != null) {
                    System.arraycopy(h.getRiverEdges(), 0, sh.riverEdges, 0, Math.min(6, h.getRiverEdges().length));
                }
                if (h.getColony() != null) {
                    Colony c = h.getColony();
                    sh.isPlayerColony = c.isPlayer();
                    sh.isCapitalColony = c.isCapital();
                    sh.colonyName = c.getName();
                    if (c.getDynasty() != null && c.getDynasty().getSpecies() != null) {
                        sh.dynastySpeciesId = c.getDynasty().getSpecies().getId();
                    } else {
                        sh.dynastySpeciesId = GameConstants.SPECIES_OMNI.getId();
                    }
                }
                data.hexes.add(sh);
            }
        }

        if (world.getGeographicFeatures() != null) {
            for (GeographicFeature gf : world.getGeographicFeatures()) {
                if (gf == null) continue;
                SavedMapData.SavedMapFeature smf = new SavedMapData.SavedMapFeature();
                smf.id = gf.getId();
                smf.type = gf.getType().name();
                smf.name = gf.getName();
                smf.nameKey = gf.getNameKey();
                for (Hex h : gf.getHexes()) {
                    if (h != null) {
                        smf.hexCoords.add(new int[]{h.getQ(), h.getR()});
                    }
                }
                data.features.add(smf);
            }
        }
        return data;
    }

    public static File saveMap(World world, String mapName) throws IOException {
        return saveMap(world, mapName, null);
    }

    public static File saveMap(World world, String mapName, Integer builderStage) throws IOException {
        SavedMapData data = createSavedMapData(world, mapName, builderStage);
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
        Map<String, Dynasty> restoredDynasties = new HashMap<>();
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
                if (sh.faultType != null) {
                    try {
                        h.setFaultType(FaultLine.FaultType.valueOf(sh.faultType));
                        h.setFaultIntensity(sh.faultIntensity);
                    } catch (Exception ignore) {}
                }
                if (sh.riverEdges != null) {
                    for (int i = 0; i < Math.min(6, sh.riverEdges.length); i++) {
                        h.setRiverEdge(i, sh.riverEdges[i]);
                    }
                }
                if (sh.dynastySpeciesId > 0 || (sh.colonyName != null && !sh.colonyName.isEmpty())) {
                    AntSpecies species = GameConstants.getSpeciesById(sh.dynastySpeciesId);
                    if (species == null) {
                        species = GameConstants.SPECIES_OMNI;
                    }
                    String dKey = species.getId() + ":" + sh.isPlayerColony;
                    Dynasty dyn = restoredDynasties.get(dKey);
                    if (dyn == null) {
                        int dynId = world.allocateDynastyId();
                        String dName = sh.isPlayerColony ? "Player Dynasty" : species.getName() + " Dynasty";
                        dyn = new Dynasty(dynId, dName, LanguageStrings.DYNASTY_TITLE_DYNASTY, sh.isPlayerColony, species);
                        if (species.getDiet() != null) {
                            dyn.setStartingDiet(species.getDiet());
                        }
                        dyn.getStarterService().initializeDynasty(dyn);
                        world.registerDynasty(dyn);
                        restoredDynasties.put(dKey, dyn);
                    }
                    String colName = (sh.colonyName != null && !sh.colonyName.isEmpty()) ? sh.colonyName : dyn.getName();
                    Colony col = new Colony(world.getNextColonyId(), colName, sh.isPlayerColony);
                    col.setCapital(sh.isCapitalColony);
                    dyn.addColony(col);
                    if (sh.isCapitalColony || dyn.getCapital() == null) {
                        dyn.setCapital(col);
                    }
                    h.setColony(col);
                }
                hexMap.put(sh.q + "," + sh.r, h);
                hexList.add(h);
            }
        }
        world.setHexes(hexList);
        world.linkNeighbors(hexMap);
        world.classifyLandmasses();
        world.ensureFaultLinesReconstructed();

        if (data.features != null && !data.features.isEmpty()) {
            world.clearGeographicFeatures();
            for (SavedMapData.SavedMapFeature smf : data.features) {
                try {
                    GeographicFeature.FeatureType type = GeographicFeature.FeatureType.valueOf(smf.type);
                    GeographicFeature gf = new GeographicFeature(smf.id, type, smf.name, smf.nameKey);
                    if (smf.hexCoords != null) {
                        for (int[] coord : smf.hexCoords) {
                            Hex h = world.getHexAt(coord[0], coord[1]);
                            if (h != null) {
                                gf.addHex(h);
                            }
                        }
                    }
                    world.addGeographicFeature(gf);
                } catch (Exception ignore) {}
            }
        } else {
            world.ensureGeographicFeaturesReconstructed();
        }
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

        List<Savefile.SavedDynasty> savedDynasties = new ArrayList<>();
        List<Savefile.SavedColony> savedColonies = new ArrayList<>();
        Map<String, Savefile.SavedDynasty> dynastyByKey = new HashMap<>();
        int nextDynastyId = 1;
        int nextColonyId = 1;
        if (data.hexes != null) {
            for (SavedMapData.SavedMapHex sh : data.hexes) {
                if (sh.dynastySpeciesId <= 0 && (sh.colonyName == null || sh.colonyName.isEmpty())) {
                    continue;
                }
                AntSpecies species = GameConstants.getSpeciesById(sh.dynastySpeciesId);
                if (species == null) {
                    species = GameConstants.SPECIES_OMNI;
                }
                boolean effectivePlayer = !save.isSpectatorMode() && sh.isPlayerColony;
                String dKey = species.getId() + ":" + effectivePlayer;
                Savefile.SavedDynasty sd = dynastyByKey.get(dKey);
                if (sd == null) {
                    sd = new Savefile.SavedDynasty();
                    sd.id = nextDynastyId++;
                    sd.speciesId = species.getId();
                    sd.startingDietId = (species.getDiet() != null) ? species.getDiet().getId() : GameConstants.DIET_FUNGIVOROUS.getId();
                    sd.isPlayer = effectivePlayer;
                    sd.titleKey = LanguageStrings.DYNASTY_TITLE_DYNASTY;
                    sd.titleId = GameConstants.getDynastyTitleByKey(sd.titleKey).getId();
                    String baseName = effectivePlayer && save.getName() != null && !save.getName().isBlank()
                            ? save.getName().trim()
                            : species.getName();
                    sd.themeBase = baseName;
                    sd.name = LanguageStrings.formatDynastyName(baseName, sd.titleKey);
                    sd.rankName = LanguageStrings.RANK_ANT;
                    Dynasty starterDyn = new Dynasty(sd.id, sd.name, sd.titleKey, effectivePlayer, species);
                    if (species.getDiet() != null) {
                        starterDyn.setStartingDiet(species.getDiet());
                    }
                    starterDyn.getStarterService().initializeDynasty(starterDyn);
                    for (Upgrade u : starterDyn.getUnlockedUpgrades()) {
                        sd.unlockedUpgradeIds.add(u.getId());
                    }
                    dynastyByKey.put(dKey, sd);
                    savedDynasties.add(sd);
                }
                Savefile.SavedColony sc = new Savefile.SavedColony();
                sc.id = nextColonyId++;
                sc.dynastyId = sd.id;
                sc.name = (sh.colonyName != null && !sh.colonyName.isEmpty()) ? sh.colonyName : sd.name;
                sc.rankName = LanguageStrings.RANK_COLONY;
                sc.isPlayer = effectivePlayer;
                sc.isCapital = sh.isCapitalColony || sd.capitalColonyId == -1;
                sc.isAutomated = !effectivePlayer;
                sc.age = sc.isCapital ? 7 : 0;
                sc.loyalty = GameNumbers.DEFAULT_COLONY_LOYALTY;
                sc.q = sh.q;
                sc.r = sh.r;
                sc.workers = 30;
                sc.queens = 1;
                sc.totalAnts = 31;
                sc.mushrooms = 50;
                sc.plants = 50;
                sc.water = 50;
                sc.protein = 25;
                if (sc.isCapital) {
                    sd.capitalColonyId = sc.id;
                }
                savedColonies.add(sc);
            }
        }
        if (!savedColonies.isEmpty()) {
            save.setDynastys(savedDynasties);
            save.setColonies(savedColonies);
        }
    }

    public static int inferLastStage(SavedMapData data, World world) {
        if (data != null && data.builderStage != null && data.builderStage >= 0 && data.builderStage <= 4) {
            return data.builderStage;
        }
        if (world != null) {
            if (world.getDynastys() != null && !world.getDynastys().isEmpty()) {
                return 4;
            }
            if (world.getHexes() != null) {
                boolean hasColony = false;
                boolean hasLandBiome = false;
                boolean hasLandElevation = false;
                boolean hasPlatesOrFaults = false;
                for (Hex h : world.getHexes()) {
                    if (h == null) continue;
                    if (h.getColony() != null) {
                        hasColony = true;
                        break;
                    }
                    if (h.getElevation() >= 4) {
                        hasLandElevation = true;
                        if (h.getBiome() != null
                                && h.getBiome() != GameConstants.BIOME_OCEAN
                                && h.getBiome() != GameConstants.BIOME_DEEP_OCEAN
                                && h.getBiome() != GameConstants.BIOME_SHALLOW_OCEAN
                                && h.getBiome() != GameConstants.BIOME_ABYSS
                                && h.getBiome() != GameConstants.BIOME_PLAINS) {
                            hasLandBiome = true;
                        }
                    } else if (h.getBiome() != null
                            && h.getBiome() != GameConstants.BIOME_OCEAN
                            && h.getBiome() != GameConstants.BIOME_DEEP_OCEAN
                            && h.getBiome() != GameConstants.BIOME_SHALLOW_OCEAN
                            && h.getBiome() != GameConstants.BIOME_ABYSS
                            && h.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                        hasLandBiome = true;
                    }
                    if (h.getPlateId() > 0 || h.getFaultType() != null) {
                        hasPlatesOrFaults = true;
                    }
                }
                if (hasColony) {
                    return 4;
                }
                if (hasLandBiome) {
                    return 3;
                }
                if (hasLandElevation) {
                    return 2;
                }
                if (hasPlatesOrFaults) {
                    return 1;
                }
            }
        }
        return 0;
    }
}
