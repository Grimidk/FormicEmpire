package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.dynasty.DynastyTitle;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.ClimateZone;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.HydrologyLevel;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.world.Continent;
import com.grimidk.formicempire.classes.entities.world.FaultLine;
import com.grimidk.formicempire.classes.entities.world.FaultLine.FaultType;
import com.grimidk.formicempire.classes.entities.world.GeographicFeature;
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.world.TectonicPlate;
import com.grimidk.formicempire.classes.entities.world.Tunnel;
import com.grimidk.formicempire.classes.entities.services.world.GeographicFeatureNamingService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SimulationDiagnostics;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SimulationDiagnostics.Counter;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.SimulationDiagnostics.Scope;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDeathService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntelligenceService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyNamingService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntegrationService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyRebellionService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEvent;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryEventType;
import com.grimidk.formicempire.classes.entities.services.world.WorldHistoryService;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class World {

    private static final DynastyDeathService DYNASTY_DEATH_SERVICE = new DynastyDeathService();
    private static final ExecutorService CONTINENT_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "continent-worker");
        thread.setDaemon(true);
        return thread;
    });
    private static final List<Weather> STANDARD_RANDOM_WEATHERS = buildStandardRandomWeathers();

    private static List<Weather> buildStandardRandomWeathers() {
        List<Weather> weathers = new ArrayList<>(GameConstants.getWeathers());
        weathers.remove(GameConstants.WEATHER_SAND_STORM);
        weathers.remove(GameConstants.WEATHER_PYROCLASTIC_FOG);
        weathers.remove(GameConstants.WEATHER_ACID_RAIN);
        return Collections.unmodifiableList(weathers);
    }

    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    private TimeOfDay timeOfDay;
    private MoonPhase moonPhase;
    private Season season;
    private Weather weather;
    private int temperature;
    private int humidity;
    private ArrayList<Hex> hexes;
    private final Map<Long, Hex> hexCoordCache = new ConcurrentHashMap<>();
    private ArrayList<Hex> colonizedHexes;
    private boolean colonizedHexesDirty = true;
    private List<Dynasty> dynastys;
    private Hex activeHex; 
    private int saveSlotId = 0; // 0 = no slot (ad-hoc)
    private Engine engine;
    private boolean allowsAchievements = true;
    private boolean spectatorMode = false;
    private int worldRadius = GameNumbers.worldRadiusForContinentCore(
            GameNumbers.WORLD_DEFAULT_CONTINENT_CORE_RADIUS);
    private int continentCoreRadius = GameNumbers.WORLD_DEFAULT_CONTINENT_CORE_RADIUS;
    private int continentCount;
    private int islandCount;
    private final List<Continent> continents = new ArrayList<>();
    private final List<Continent> archipelagos = new ArrayList<>();
    private final List<Continent> iceCaps = new ArrayList<>();
    private int worldWidth = 0;
    private int worldHeight = 0;
    private boolean hasIceCaps = GameNumbers.WORLD_DEFAULT_HAS_ICE_CAPS;
    private double iceCapPolarLimit = GameNumbers.WORLD_DEFAULT_ICECAP_POLAR_LIMIT;
    private double continentalLandPercentage = GameNumbers.WORLD_DEFAULT_CONTINENTAL_LAND_PERCENTAGE;
    private int targetContinentCount = 0;
    private int minIslandCount = -1;
    private int targetRivalDynastyCount = -1;
    private int tectonicPlateCount = 6;
    private double faultUpliftFactor = 1.0;
    private int peakValleyCount = 8;
    private final List<TectonicPlate> tectonicPlates = new ArrayList<>();
    private final List<FaultLine> faultLines = new ArrayList<>();
    private final List<GeographicFeature> geographicFeatures = new ArrayList<>();
    private final Map<Hex, GeographicFeature> hexToFeature = new HashMap<>();
    private final Map<Hex, GeographicFeature> hexToRiverFeature = new HashMap<>();
    private final Map<RiverEdgeKey, GeographicFeature> riverEdgeToFeature = new HashMap<>();
    private final GeographicFeatureNamingService geographicNamingService = new GeographicFeatureNamingService();
    private WorldGenStepListener worldGenStepListener;
    private int colonyIdCounter = 1;
    private int dynastyIdCounter = 1;
    private final DynastyNamingService namingService;
    private final WarService warService;
    private final WorldHistoryService historyService;

    public World() {
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0;
        this.temperature = 25;
        this.humidity = 2;
        this.hexes = new ArrayList<>();
        this.colonizedHexes = new ArrayList<>();
        this.colonizedHexesDirty = true;
        this.dynastys = new ArrayList<>();
        this.timeOfDay = GameConstants.TIME_DAWN;
        this.moonPhase = GameConstants.PHASE_NEW_MOON;
        this.season = GameConstants.SEASON_SPRING;
        this.weather = GameConstants.WEATHER_CLEAR;
        this.namingService = new DynastyNamingService();
        this.warService = new WarService(this);
        this.historyService = new WorldHistoryService(this);
    }
    
    public void registerDynasty(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        dynasty.setOwningWorld(this);
        if (!dynastys.contains(dynasty)) {
            dynastys.add(dynasty);
        }
    }

    public WarService getWarService() {
        return warService;
    }

    public WorldHistoryService getHistoryService() {
        return historyService;
    }
    
    public Engine getEngine() {
        return engine;
    }

    public synchronized int getNextColonyId() {
        return colonyIdCounter++;
    }

    public synchronized int allocateDynastyId() {
        return dynastyIdCounter++;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
        if (engine != null && engine.hasAchievementTaintingSandbox()) {
            disableAchievements();
        }
    }

    public boolean allowsAchievements() {
        return allowsAchievements;
    }

    public void setAllowsAchievements(boolean allowsAchievements) {
        if (!this.allowsAchievements) {
            return;
        }
        this.allowsAchievements = allowsAchievements;
    }

    public void disableAchievements() {
        this.allowsAchievements = false;
    }

    public boolean isSpectatorMode() {
        return spectatorMode;
    }

    public void setSpectatorMode(boolean spectatorMode) {
        this.spectatorMode = spectatorMode;
    }

    public int getSaveSlotId() {
        return saveSlotId;
    }

    public void setSaveSlotId(int saveSlotId) {
        this.saveSlotId = saveSlotId;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
        updateEnvironmentalConditions();
    }

    public int getLocalHour(Hex hex) {
        if (hex == null || this.worldWidth <= 0) {
            return this.hour;
        }
        int eff = this.hour + hex.getTimeOffset();
        return ((eff % 24) + 24) % 24;
    }

    public int getLocalDay(Hex hex) {
        if (hex == null || this.worldWidth <= 0) {
            return this.day;
        }
        int eff = this.hour + hex.getTimeOffset();
        if (eff >= 24) {
            int d = this.day + 1;
            return d > 30 ? 1 : d;
        } else if (eff < 0) {
            int d = this.day - 1;
            return d < 1 ? 30 : d;
        }
        return this.day;
    }

    public int getLocalMonth(Hex hex) {
        if (hex == null || this.worldWidth <= 0) {
            return this.month;
        }
        int eff = this.hour + hex.getTimeOffset();
        if (eff >= 24 && this.day + 1 > 30) {
            int m = this.month + 1;
            return m > 12 ? 1 : m;
        } else if (eff < 0 && this.day - 1 < 1) {
            int m = this.month - 1;
            return m < 1 ? 12 : m;
        }
        return this.month;
    }

    public int getLocalYear(Hex hex) {
        if (hex == null || this.worldWidth <= 0) {
            return this.year;
        }
        int eff = this.hour + hex.getTimeOffset();
        if (eff >= 24 && this.day + 1 > 30 && this.month + 1 > 12) {
            return this.year + 1;
        } else if (eff < 0 && this.day - 1 < 1 && this.month - 1 < 1) {
            return this.year - 1;
        }
        return this.year;
    }

    public TimeOfDay getTimeOfDay(Hex hex) {
        if (this.timeOfDay == GameConstants.TIME_SOLAR_ECLIPSE || this.timeOfDay == GameConstants.TIME_LUNAR_ECLIPSE) {
            return this.timeOfDay;
        }
        return GameConstants.getTimeOfDayForHour(getLocalHour(hex));
    }

    public MoonPhase getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(MoonPhase moonPhase) {
        this.moonPhase = moonPhase;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
        updateEnvironmentalConditions();
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
        updateEnvironmentalConditions();
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public ArrayList<Hex> getHexes() {
        return hexes;
    }

    public void markColonizedHexIndexDirty() {
        colonizedHexesDirty = true;
    }

    private synchronized void rebuildColonizedHexIndexIfNeeded() {
        if (!colonizedHexesDirty) {
            return;
        }
        colonizedHexes.clear();
        if (hexes != null) {
            for (Hex hex : hexes) {
                if (hex != null && hex.getColony() != null) {
                    colonizedHexes.add(hex);
                }
            }
        }
        for (Continent c : getAllLandmasses()) {
            if (c != null) {
                c.refreshPopulatedHexes();
            }
        }
        colonizedHexesDirty = false;
    }

    private synchronized ArrayList<Hex> colonizedHexesForTicks() {
        rebuildColonizedHexIndexIfNeeded();
        return new ArrayList<>(colonizedHexes);
    }

    public synchronized ArrayList<Hex> getColonizedHexes() {
        return colonizedHexesForTicks();
    }

    public void setHexes(ArrayList<Hex> hexes) {
        this.hexes = hexes;
        this.hexCoordCache.clear();
        markColonizedHexIndexDirty();
    }

    public Hex getHexOfColony(Colony colony) {
        if (colony == null) return null;
        for (Hex h : colonizedHexesForTicks()) {
            if (h.getColony() == colony) return h;
        }
        if (this.hexes != null) {
            for (Hex h : this.hexes) {
                if (h.getColony() == colony) {
                    markColonizedHexIndexDirty();
                    return h;
                }
            }
        }
        return null;
    }

    public int colonyHexDistance(Colony from, Colony to) {
        Hex fromHex = getHexOfColony(from);
        Hex toHex = getHexOfColony(to);
        if (fromHex == null || toHex == null) {
            return 0;
        }
        return GameNumbers.axialHexDistance(fromHex.getQ(), fromHex.getR(), toHex.getQ(), toHex.getR());
    }

    public int minDynastyHexDistance(Dynasty a, Dynasty b) {
        if (a == null || b == null) {
            return Integer.MAX_VALUE;
        }
        List<Colony> aColonies = a.getColonies();
        List<Colony> bColonies = b.getColonies();
        if (aColonies == null || bColonies == null || aColonies.isEmpty() || bColonies.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        int min = Integer.MAX_VALUE;
        for (Colony from : aColonies) {
            Hex fromHex = getHexOfColony(from);
            if (fromHex == null) {
                continue;
            }
            for (Colony to : bColonies) {
                Hex toHex = getHexOfColony(to);
                if (toHex == null) {
                    continue;
                }
                int distance = GameNumbers.axialHexDistance(
                        fromHex.getQ(), fromHex.getR(), toHex.getQ(), toHex.getR());
                if (distance < min) {
                    min = distance;
                }
            }
        }
        return min;
    }
    
    public List<Dynasty> getDynastys() { return dynastys; }

    public Dynasty findDynastyById(int dynastyId) {
        for (Dynasty dynasty : dynastys) {
            if (dynasty.getId() == dynastyId) {
                return dynasty;
            }
        }
        return null;
    }

    public Dynasty findPlayerDynasty() {
        if (this.spectatorMode) {
            return null;
        }
        if (dynastys != null) {
            for (Dynasty dynasty : dynastys) {
                if (dynasty != null && dynasty.isPlayer()) {
                    return dynasty;
                }
            }
        }
        Hex spawn = getSpawnHex();
        if (spawn != null && spawn.getColony() != null && spawn.getColony().isPlayer()) {
            return spawn.getColony().getDynasty();
        }
        return null;
    }

    public void bindDynastyTradeServices() {
        if (engine == null || engine.getTradeManager() == null) {
            return;
        }
        TradeManager tradeManager = engine.getTradeManager();
        for (Dynasty dynasty : dynastys) {
            dynasty.bindTradeManager(tradeManager);
        }
    }
    
    public int getWorldRadius() {
        return worldRadius;
    }
    
    public void setWorldRadius(int worldRadius) {
        this.worldRadius = worldRadius;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public void setWorldWidth(int worldWidth) {
        this.worldWidth = worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public void setWorldHeight(int worldHeight) {
        this.worldHeight = worldHeight;
    }

    public boolean isRectangular() {
        return worldWidth > 0 && worldHeight > 0;
    }

    public int getContinentCoreRadius() {
        return continentCoreRadius;
    }

    public void setContinentCoreRadius(int continentCoreRadius) {
        this.continentCoreRadius = Math.max(0, continentCoreRadius);
        this.worldWidth = 0;
        this.worldHeight = 0;
    }

    public int getContinentCount() {
        return continentCount;
    }

    public int getIslandCount() {
        return islandCount;
    }

    public List<Continent> getContinents() {
        return Collections.unmodifiableList(continents);
    }

    public List<Continent> getArchipelagos() {
        return Collections.unmodifiableList(archipelagos);
    }

    public List<Continent> getIceCaps() {
        return Collections.unmodifiableList(iceCaps);
    }

    public List<Continent> getAllLandmasses() {
        List<Continent> all = new ArrayList<>(this.continents);
        all.addAll(this.archipelagos);
        all.addAll(this.iceCaps);
        return Collections.unmodifiableList(all);
    }

    public Continent getContinentById(int id) {
        for (Continent c : continents) {
            if (c.getId() == id) {
                return c;
            }
        }
        for (Continent arch : archipelagos) {
            if (arch.getId() == id) {
                return arch;
            }
        }
        for (Continent cap : iceCaps) {
            if (cap.getId() == id) {
                return cap;
            }
        }
        return null;
    }

    public Continent getContinentForHex(Hex hex) {
        if (hex == null || !isLandHex(hex)) {
            return null;
        }
        if (!hex.isIsland() && hex.getContinentId() > 0) {
            Continent c = getContinentById(hex.getContinentId());
            if (c != null) return c;
        }
        for (Continent cap : this.iceCaps) {
            if (cap.containsHex(hex)) return cap;
        }
        for (Continent arch : this.archipelagos) {
            if (arch.containsHex(hex) || arch.getId() == hex.getContinentId()) return arch;
        }
        return null;
    }

    public void rebuildContinentsFromHexes() {
        Map<Integer, String> existingNames = new HashMap<>();
        Map<Integer, String> existingKeys = new HashMap<>();
        for (Continent c : this.continents) {
            if (c != null && c.getName() != null) {
                existingNames.put(c.getId(), c.getName());
                if (c.getNameKey() != null) {
                    existingKeys.put(c.getId(), c.getNameKey());
                }
            }
        }
        Set<String> usedNamesInWorld = new HashSet<>();
        Set<String> usedKeysInWorld = new HashSet<>();
        usedNamesInWorld.add(LanguageStrings.get(LanguageStrings.ICE_CAP_ARTICA));
        usedNamesInWorld.add(LanguageStrings.get(LanguageStrings.ICE_CAP_ANTARTICA));

        this.continents.clear();
        Map<Integer, Continent> map = new HashMap<>();
        List<Hex> landHexes = new ArrayList<>();
        for (Hex hex : this.hexes) {
            if (hex == null || hex.isIsland() || !isLandHex(hex)) {
                continue;
            }
            int cid = hex.getContinentId();
            if (cid <= 0) {
                continue;
            }
            landHexes.add(hex);
        }

        List<Integer> sortedCids = new ArrayList<>();
        for (Hex hex : landHexes) {
            int cid = hex.getContinentId();
            if (!sortedCids.contains(cid)) {
                sortedCids.add(cid);
            }
        }
        sortedCids.sort(Integer::compareTo);

        for (int cid : sortedCids) {
            String key = existingKeys.get(cid);
            String name = existingNames.get(cid);
            if (key != null && !key.isEmpty() && name != null && !name.isEmpty() && !usedNamesInWorld.contains(name)) {
                usedKeysInWorld.add(key);
                usedNamesInWorld.add(name);
            } else {
                key = null;
                name = null;
                for (String k : LanguageStrings.CONTINENT_NAMES) {
                    String candidateName = LanguageStrings.get(k);
                    if (!usedKeysInWorld.contains(k) && !usedNamesInWorld.contains(candidateName)) {
                        key = k;
                        name = candidateName;
                        break;
                    }
                }
                if (key == null) {
                    int idx = (cid - 1) % LanguageStrings.CONTINENT_NAMES.length;
                    if (idx < 0) idx = 0;
                    key = LanguageStrings.CONTINENT_NAMES[idx];
                    String base = LanguageStrings.get(key);
                    int num = 2;
                    String candidate = base + " " + toRomanNumeral(num);
                    while (usedNamesInWorld.contains(candidate)) {
                        num++;
                        candidate = base + " " + toRomanNumeral(num);
                    }
                    name = candidate;
                }
                usedKeysInWorld.add(key);
                usedNamesInWorld.add(name);
            }
            Continent cont = new Continent(cid, name, key);
            cont.setArchipelago(false);
            cont.setIceCap(false);
            map.put(cid, cont);
        }

        for (Hex hex : landHexes) {
            Continent c = map.get(hex.getContinentId());
            if (c != null) {
                c.addHex(hex);
            }
        }
        this.continents.addAll(map.values());
        this.continents.sort((a, b) -> Integer.compare(a.getId(), b.getId()));
        this.continentCount = this.continents.size();

        this.iceCaps.clear();
        this.archipelagos.clear();

        Set<Hex> assignedLand = new HashSet<>();
        for (Continent c : this.continents) {
            assignedLand.addAll(c.getHexes());
        }

        if (this.hasIceCaps && this.worldHeight > 0) {
            Continent artica = new Continent(-1, LanguageStrings.get(LanguageStrings.ICE_CAP_ARTICA), LanguageStrings.ICE_CAP_ARTICA);
            artica.setIceCap(true);
            Continent antartica = new Continent(-2, LanguageStrings.get(LanguageStrings.ICE_CAP_ANTARTICA), LanguageStrings.ICE_CAP_ANTARTICA);
            antartica.setIceCap(true);

            ArrayDeque<Hex> articaQueue = new ArrayDeque<>();
            ArrayDeque<Hex> antarticaQueue = new ArrayDeque<>();

            for (Hex hex : this.hexes) {
                if (hex != null && isLandHex(hex) && !assignedLand.contains(hex)) {
                    if (hex.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                        if (hex.getR() < 0) {
                            artica.addHex(hex);
                            articaQueue.add(hex);
                        } else {
                            antartica.addHex(hex);
                            antarticaQueue.add(hex);
                        }
                        assignedLand.add(hex);
                    }
                }
            }

            while (!articaQueue.isEmpty()) {
                Hex curr = articaQueue.poll();
                for (Hex n : curr.getAdjacentNeighbors()) {
                    if (n != null && isLandHex(n) && n.getR() < 0 && assignedLand.add(n)) {
                        artica.addHex(n);
                        articaQueue.add(n);
                    }
                }
            }

            while (!antarticaQueue.isEmpty()) {
                Hex curr = antarticaQueue.poll();
                for (Hex n : curr.getAdjacentNeighbors()) {
                    if (n != null && isLandHex(n) && n.getR() >= 0 && assignedLand.add(n)) {
                        antartica.addHex(n);
                        antarticaQueue.add(n);
                    }
                }
            }

            int polarLimit = Math.max(2, (int) Math.round(this.worldHeight * 0.38));
            for (Hex hex : this.hexes) {
                if (hex != null && isLandHex(hex) && !assignedLand.contains(hex)) {
                    if (Math.abs(hex.getR()) >= polarLimit) {
                        if (hex.getR() < 0) {
                            artica.addHex(hex);
                        } else {
                            antartica.addHex(hex);
                        }
                        assignedLand.add(hex);
                    }
                }
            }

            if (artica.getHexCount() > 0) {
                for (Hex h : artica.getHexes()) {
                    h.setIsland(false);
                    h.setContinentId(0);
                }
                this.iceCaps.add(artica);
            }
            if (antartica.getHexCount() > 0) {
                for (Hex h : antartica.getHexes()) {
                    h.setIsland(false);
                    h.setContinentId(0);
                }
                this.iceCaps.add(antartica);
            }
        }

        List<List<Hex>> islandComponents = new ArrayList<>();
        for (Hex hex : this.hexes) {
            if (hex == null || !isLandHex(hex) || assignedLand.contains(hex) || hex.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                continue;
            }

            List<Hex> comp = new ArrayList<>();
            ArrayDeque<Hex> q = new ArrayDeque<>();
            q.add(hex);
            assignedLand.add(hex);
            while (!q.isEmpty()) {
                Hex curr = q.poll();
                comp.add(curr);
                for (Hex n : curr.getAdjacentNeighbors()) {
                    if (n != null && isLandHex(n) && assignedLand.add(n)) {
                        q.add(n);
                    }
                }
            }
            islandComponents.add(comp);
        }

        int nComp = islandComponents.size();
        int[] parent = new int[nComp];
        for (int i = 0; i < nComp; i++) {
            parent[i] = i;
        }

        for (int i = 0; i < nComp; i++) {
            List<Hex> compA = islandComponents.get(i);
            for (int j = i + 1; j < nComp; j++) {
                if (findRoot(parent, i) == findRoot(parent, j)) continue;
                List<Hex> compB = islandComponents.get(j);
                boolean close = false;
                for (Hex ha : compA) {
                    for (Hex hb : compB) {
                        if (distanceBetweenHexes(ha, hb) <= 2) {
                            close = true;
                            break;
                        }
                    }
                    if (close) break;
                }
                if (close) {
                    unionRoots(parent, i, j);
                }
            }
        }

        Map<Integer, List<Integer>> clusters = new LinkedHashMap<>();
        for (int i = 0; i < nComp; i++) {
            int root = findRoot(parent, i);
            clusters.computeIfAbsent(root, ignored -> new ArrayList<>()).add(i);
        }

        int archIndex = 0;
        int islandIndex = 0;
        for (List<Integer> compIndices : clusters.values()) {
            List<Hex> clusterHexes = new ArrayList<>();
            for (int idx : compIndices) {
                clusterHexes.addAll(islandComponents.get(idx));
            }

            boolean isSolitary = (compIndices.size() == 1 && clusterHexes.size() <= 3);
            int archId = 1000 + archIndex + islandIndex + 1;
            String landmassKey = null;
            String landmassName = null;

            if (isSolitary) {
                islandIndex++;
                for (String k : LanguageStrings.ISLAND_NAMES) {
                    String candidateName = LanguageStrings.get(k);
                    if (!usedKeysInWorld.contains(k) && !usedNamesInWorld.contains(candidateName)) {
                        landmassKey = k;
                        landmassName = candidateName;
                        break;
                    }
                }
                if (landmassKey == null) {
                    int idx = (islandIndex - 1) % LanguageStrings.ISLAND_NAMES.length;
                    if (idx < 0) idx = 0;
                    landmassKey = LanguageStrings.ISLAND_NAMES[idx];
                    String base = LanguageStrings.get(landmassKey);
                    int num = 2;
                    String candidate = base + " " + toRomanNumeral(num);
                    while (usedNamesInWorld.contains(candidate)) {
                        num++;
                        candidate = base + " " + toRomanNumeral(num);
                    }
                    landmassName = candidate;
                }
            } else {
                archIndex++;
                for (String k : LanguageStrings.ARCHIPELAGO_NAMES) {
                    String candidateName = LanguageStrings.get(k);
                    if (!usedKeysInWorld.contains(k) && !usedNamesInWorld.contains(candidateName)) {
                        landmassKey = k;
                        landmassName = candidateName;
                        break;
                    }
                }
                if (landmassKey == null) {
                    int idx = (archIndex - 1) % LanguageStrings.ARCHIPELAGO_NAMES.length;
                    if (idx < 0) idx = 0;
                    landmassKey = LanguageStrings.ARCHIPELAGO_NAMES[idx];
                    String base = LanguageStrings.get(landmassKey);
                    int num = 2;
                    String candidate = base + " " + toRomanNumeral(num);
                    while (usedNamesInWorld.contains(candidate)) {
                        num++;
                        candidate = base + " " + toRomanNumeral(num);
                    }
                    landmassName = candidate;
                }
            }

            usedKeysInWorld.add(landmassKey);
            usedNamesInWorld.add(landmassName);

            Continent landmass = new Continent(archId, landmassName, landmassKey);
            landmass.setArchipelago(true);
            for (Hex h : clusterHexes) {
                h.setIsland(true);
                h.setContinentId(0);
                landmass.addHex(h);
            }
            this.archipelagos.add(landmass);
        }

        for (Continent c : getAllLandmasses()) {
            if (c != null) {
                c.refreshPopulatedHexes();
            }
        }
    }

    private static String toRomanNumeral(int n) {
        String[] romans = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV"};
        if (n >= 1 && n < romans.length) return romans[n];
        return String.valueOf(n);
    }

    public Hex getSpawnHex() {
        if (this.hexes == null || this.hexes.isEmpty()) return null;
        
        for (Hex h : this.hexes) {
            if (h.getColony() != null && h.getColony().isPlayer()) {
                return h;
            }
        }
        if (this.spectatorMode) {
            for (Hex h : this.hexes) {
                if (h.getColony() != null) {
                    return h;
                }
            }
        }
        for (Hex h : this.hexes) {
            if (h.getQ() == 0 && h.getR() == 0) {
                return h;
            }
        }
        
        return this.hexes.get(0);
    }
    
    public Hex getActiveHex() {
        return activeHex;
    }
    
    public void changeActiveHex(Hex newHex) {
        if (newHex == null) {
            return;
        }
        Hex resolved = resolveHex(newHex);
        if (resolved == null) {
            return;
        }

        if (this.activeHex != null) {
            this.activeHex.setActive(false);
            if (this.activeHex.getColony() != null) {
                this.activeHex.getColony().setActive(false);
            }
        }

        this.activeHex = resolved;
        this.activeHex.setActive(true);
        if (this.activeHex.getColony() != null && !this.spectatorMode && this.activeHex.getColony().isPlayer()) {
            this.activeHex.getColony().setActive(true);
        }
        
        updateEnvironmentalConditions();
    }

    public Hex resolveHex(Hex hex) {
        if (hex == null || hexes == null) {
            return null;
        }
        if (hexes.contains(hex)) {
            return hex;
        }
        return findHex(hex.getQ(), hex.getR());
    }

    public Hex findHex(int q, int r) {
        if (hexes == null) {
            return null;
        }
        for (Hex hex : hexes) {
            if (hex != null && hex.getQ() == q && hex.getR() == r) {
                return hex;
            }
        }
        return null;
    }

    public Hex findHexForColony(Colony colony) {
        if (colony == null || hexes == null) {
            return null;
        }
        for (Hex hex : hexes) {
            if (hex != null && hex.getColony() == colony) {
                return hex;
            }
        }
        return null;
    }

    public Hex findHexForColonyId(int colonyId) {
        if (colonyId <= 0 || hexes == null) {
            return null;
        }
        for (Hex hex : hexes) {
            if (hex != null && hex.getColony() != null && hex.getColony().getId() == colonyId) {
                return hex;
            }
        }
        return null;
    }

    public Temperature getTemperatureIcon() {
        if (temperature <= GameConstants.TEMP_FREEZING.getMaxTemp()) {
            return GameConstants.TEMP_FREEZING;
        } else if (temperature <= GameConstants.TEMP_COLD.getMaxTemp()) {
            return GameConstants.TEMP_COLD;
        } else if (temperature <= GameConstants.TEMP_CHILLY.getMaxTemp()) {
            return GameConstants.TEMP_CHILLY;
        } else if (temperature <= GameConstants.TEMP_GOOD.getMaxTemp()) {
            return GameConstants.TEMP_GOOD;
        } else if (temperature <= GameConstants.TEMP_WARM.getMaxTemp()) {
            return GameConstants.TEMP_WARM;
        } else if (temperature <= GameConstants.TEMP_HOT.getMaxTemp()) {
            return GameConstants.TEMP_HOT;
        } else {
            return GameConstants.TEMP_BURNING;
        }
    }

    public Humidity getHumidityIcon() {
        if (humidity <= 0) {
            return GameConstants.HUMID_0;
        } else if (humidity == 1) {
            return GameConstants.HUMID_1;
        } else if (humidity == 2) {
            return GameConstants.HUMID_2;
        } else if (humidity == 3) {
            return GameConstants.HUMID_3;
        } else if (humidity == 4) {
            return GameConstants.HUMID_4;
        } else {
            return GameConstants.HUMID_5;
        }
    }
    
    private String formatName(String name) {
        if (name == null || name.trim().isEmpty()) return "Player";
        name = name.trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public boolean hasIceCaps() {
        return hasIceCaps;
    }

    public void setHasIceCaps(boolean hasIceCaps) {
        this.hasIceCaps = hasIceCaps;
    }

    public double getIceCapPolarLimit() {
        return iceCapPolarLimit;
    }

    public void setIceCapPolarLimit(double iceCapPolarLimit) {
        this.iceCapPolarLimit = Math.max(0.40, Math.min(1.0, iceCapPolarLimit));
    }

    public DynastyNamingService getNamingService() {
        return namingService;
    }

    public double getContinentalLandPercentage() {
        return continentalLandPercentage;
    }

    public void setContinentalLandPercentage(double continentalLandPercentage) {
        this.continentalLandPercentage = Math.max(0.05, Math.min(0.90, continentalLandPercentage));
    }

    public int getTargetContinentCount() {
        return targetContinentCount;
    }

    public void setTargetContinentCount(int targetContinentCount) {
        this.targetContinentCount = Math.max(0, targetContinentCount);
    }

    public int getMinIslandCount() {
        return minIslandCount;
    }

    public void setMinIslandCount(int minIslandCount) {
        this.minIslandCount = minIslandCount;
    }

    public int getTargetRivalDynastyCount() {
        return targetRivalDynastyCount;
    }

    public void setTargetRivalDynastyCount(int targetRivalDynastyCount) {
        this.targetRivalDynastyCount = targetRivalDynastyCount;
    }

    public int getTectonicPlateCount() {
        return tectonicPlateCount;
    }

    public void setTectonicPlateCount(int tectonicPlateCount) {
        this.tectonicPlateCount = Math.max(0, tectonicPlateCount);
    }

    public double getFaultUpliftFactor() {
        return faultUpliftFactor;
    }

    public void setFaultUpliftFactor(double faultUpliftFactor) {
        this.faultUpliftFactor = Math.max(0.1, faultUpliftFactor);
    }

    public int getPeakValleyCount() {
        return peakValleyCount;
    }

    public void setPeakValleyCount(int peakValleyCount) {
        this.peakValleyCount = Math.max(0, peakValleyCount);
    }

    public List<TectonicPlate> getTectonicPlates() {
        return Collections.unmodifiableList(tectonicPlates);
    }

    public List<FaultLine> getFaultLines() {
        return Collections.unmodifiableList(faultLines);
    }

    public void addFaultLine(FaultLine faultLine) {
        if (faultLine != null) {
            this.faultLines.add(faultLine);
        }
    }

    public FaultLine getFaultLineBetweenPlates(int plateAId, int plateBId) {
        if (plateAId <= 0 || plateBId <= 0 || plateAId == plateBId) {
            return null;
        }
        for (FaultLine f : this.faultLines) {
            if (f != null && ((f.getPlateAId() == plateAId && f.getPlateBId() == plateBId)
                    || (f.getPlateAId() == plateBId && f.getPlateBId() == plateAId))) {
                return f;
            }
        }
        return null;
    }

    public FaultLine.FaultType getFaultTypeBetweenPlates(int plateAId, int plateBId) {
        FaultLine line = getFaultLineBetweenPlates(plateAId, plateBId);
        if (line != null) {
            return line.getType();
        }
        TectonicPlate plateA = getPlateById(plateAId);
        TectonicPlate plateB = getPlateById(plateBId);
        if (plateA != null && plateB != null) {
            double vRelX = plateA.getDriftVectorX() - plateB.getDriftVectorX();
            double vRelY = plateA.getDriftVectorY() - plateB.getDriftVectorY();
            int signCol = (plateB.getCenterQ() + (plateB.getCenterR() >> 1)) - (plateA.getCenterQ() + (plateA.getCenterR() >> 1));
            if (this.worldWidth > 0) {
                if (signCol > this.worldWidth / 2) signCol -= this.worldWidth;
                else if (signCol < -this.worldWidth / 2) signCol += this.worldWidth;
            }
            double dx = signCol * Math.sqrt(3.0);
            double dy = (plateB.getCenterR() - plateA.getCenterR()) * 1.5;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 0.0001) {
                dx /= len;
                dy /= len;
            }
            double dot = vRelX * dx + vRelY * dy;
            if (dot > 0.15) {
                return FaultLine.FaultType.CONVERGENT;
            } else if (dot < -0.15) {
                return FaultLine.FaultType.DIVERGENT;
            } else {
                return FaultLine.FaultType.TRANSFORM;
            }
        }
        return FaultLine.FaultType.TRANSFORM;
    }

    public int getFaultIntensityBetweenPlates(int plateAId, int plateBId) {
        FaultLine line = getFaultLineBetweenPlates(plateAId, plateBId);
        if (line != null) {
            return line.getIntensity();
        }
        TectonicPlate plateA = getPlateById(plateAId);
        TectonicPlate plateB = getPlateById(plateBId);
        if (plateA != null && plateB != null) {
            double vRelX = plateA.getDriftVectorX() - plateB.getDriftVectorX();
            double vRelY = plateA.getDriftVectorY() - plateB.getDriftVectorY();
            int signCol = (plateB.getCenterQ() + (plateB.getCenterR() >> 1)) - (plateA.getCenterQ() + (plateA.getCenterR() >> 1));
            if (this.worldWidth > 0) {
                if (signCol > this.worldWidth / 2) signCol -= this.worldWidth;
                else if (signCol < -this.worldWidth / 2) signCol += this.worldWidth;
            }
            double dx = signCol * Math.sqrt(3.0);
            double dy = (plateB.getCenterR() - plateA.getCenterR()) * 1.5;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 0.0001) {
                dx /= len;
                dy /= len;
            }
            double dot = vRelX * dx + vRelY * dy;
            if (Math.abs(dot) >= 0.35) {
                return 2;
            }
        }
        return 1;
    }

    public void ensureFaultLinesReconstructed() {
        if (!this.faultLines.isEmpty() || this.hexes == null || this.hexes.isEmpty()) {
            return;
        }
        Map<Long, FaultLine> faultMap = new HashMap<>();
        for (Hex h : this.hexes) {
            if (h == null || h.getPlateId() <= 0) continue;
            int idA = h.getPlateId();
            for (Hex n : h.getAdjacentNeighbors()) {
                if (n == null || n.getPlateId() <= 0) continue;
                int idB = n.getPlateId();
                if (idA == idB) continue;
                long key = (((long) Math.min(idA, idB)) << 32) | (Math.max(idA, idB) & 0xffffffffL);
                FaultLine fault = faultMap.get(key);
                if (fault == null) {
                    FaultLine.FaultType type = getFaultTypeBetweenPlates(idA, idB);
                    if (type == null) {
                        type = FaultLine.FaultType.TRANSFORM;
                    }
                    int intensity = getFaultIntensityBetweenPlates(idA, idB);
                    fault = new FaultLine(Math.min(idA, idB), Math.max(idA, idB), type, intensity);
                    faultMap.put(key, fault);
                    this.faultLines.add(fault);
                }
                fault.addBoundaryPair(h, n);
            }
        }
    }

    public TectonicPlate getPlateById(int id) {
        for (TectonicPlate p : tectonicPlates) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public List<GeographicFeature> getGeographicFeatures() {
        return Collections.unmodifiableList(geographicFeatures);
    }

    public GeographicFeature getFeatureForHex(Hex hex) {
        return hex != null ? hexToFeature.get(hex) : null;
    }

    public GeographicFeature getRiverFeatureForHex(Hex hex) {
        return hex != null ? hexToRiverFeature.get(hex) : null;
    }

    public GeographicFeature getFeatureForRiverEdge(Hex a, Hex b) {
        return (a != null && b != null) ? riverEdgeToFeature.get(new RiverEdgeKey(a, b)) : null;
    }

    public void clearGeographicFeatures() {
        this.geographicFeatures.clear();
        this.hexToFeature.clear();
        this.hexToRiverFeature.clear();
        this.riverEdgeToFeature.clear();
        this.geographicNamingService.reset();
    }

    public void addGeographicFeature(GeographicFeature feature) {
        if (feature == null) return;
        this.geographicFeatures.add(feature);
        for (Hex h : feature.getHexes()) {
            if (feature.getType() == GeographicFeature.FeatureType.RIVER) {
                this.hexToRiverFeature.putIfAbsent(h, feature);
            } else if (feature.getType() != GeographicFeature.FeatureType.CONTINENT
                    && feature.getType() != GeographicFeature.FeatureType.ICE_CAP
                    && feature.getType() != GeographicFeature.FeatureType.ARCHIPELAGO
                    && feature.getType() != GeographicFeature.FeatureType.OCEAN) {
                this.hexToFeature.putIfAbsent(h, feature);
            }
        }
        if (feature.getType() == GeographicFeature.FeatureType.RIVER) {
            for (Hex h : feature.getHexes()) {
                for (int d = 0; d < 6; d++) {
                    if (h.hasRiverEdge(d)) {
                        Hex n = h.getNeighbor(d);
                        if (n != null && feature.containsHex(n)) {
                            this.riverEdgeToFeature.put(new RiverEdgeKey(h, n), feature);
                        }
                    }
                }
            }
        }
        feature.recalculateCentroid(this.worldWidth);
    }

    public void generateGeographicFeatures() {
        clearGeographicFeatures();
        if (this.hexes == null || this.hexes.isEmpty()) {
            return;
        }
        int featureIdCounter = 1;

        Set<Hex> visitedMountains = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || visitedMountains.contains(h)) continue;
            boolean isMtn = (h.getElevation() >= 7
                    || h.getBiome() == GameConstants.BIOME_MOUNTAIN
                    || h.getBiome() == GameConstants.BIOME_PEAK)
                    && !isWaterBiome(h.getBiome());
            if (!isMtn) continue;

            List<Hex> cluster = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            visitedMountains.add(h);
            queue.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                cluster.add(curr);
                for (int d = 0; d < 6; d++) {
                    Hex n = curr.getNeighbor(d);
                    if (n != null && !visitedMountains.contains(n) && !isWaterBiome(n.getBiome())) {
                        boolean nMtn = (n.getElevation() >= 7
                                || n.getBiome() == GameConstants.BIOME_MOUNTAIN
                                || n.getBiome() == GameConstants.BIOME_PEAK);
                        if (nMtn) {
                            visitedMountains.add(n);
                            queue.add(n);
                        }
                    }
                }
            }

            if (cluster.size() >= 2) {
                int fid = featureIdCounter++;
                String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.MOUNTAIN_RANGE, fid);
                String dispName = geographicNamingService.formatDisplayName(key, fid);
                GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.MOUNTAIN_RANGE, dispName, key);
                for (Hex ch : cluster) {
                    feat.addHex(ch);
                }
                addGeographicFeature(feat);
            }
        }

        Set<Hex> visitedLakes = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || visitedLakes.contains(h)) continue;
            if (h.getBiome() != GameConstants.BIOME_LAKE) continue;

            List<Hex> cluster = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            visitedLakes.add(h);
            queue.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                cluster.add(curr);
                for (int d = 0; d < 6; d++) {
                    Hex n = curr.getNeighbor(d);
                    if (n != null && !visitedLakes.contains(n) && n.getBiome() == GameConstants.BIOME_LAKE) {
                        visitedLakes.add(n);
                        queue.add(n);
                    }
                }
            }

            int fid = featureIdCounter++;
            String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.LAKE_BASIN, fid);
            String dispName = geographicNamingService.formatDisplayName(key, fid);
            GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.LAKE_BASIN, dispName, key);
            for (Hex ch : cluster) {
                feat.addHex(ch);
            }
            addGeographicFeature(feat);
        }

        Set<Hex> visitedValleys = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || visitedValleys.contains(h)) continue;
            if (isWaterBiome(h.getBiome()) || h.getBiome() == GameConstants.BIOME_LAKE || h.getElevation() > 3) continue;

            List<Hex> cluster = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            visitedValleys.add(h);
            queue.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                cluster.add(curr);
                if (cluster.size() >= 35) {
                    break;
                }
                for (int d = 0; d < 6; d++) {
                    Hex n = curr.getNeighbor(d);
                    if (n != null && !visitedValleys.contains(n) && !isWaterBiome(n.getBiome())
                            && n.getBiome() != GameConstants.BIOME_LAKE && n.getElevation() <= 3) {
                        visitedValleys.add(n);
                        queue.add(n);
                    }
                }
            }

            if (cluster.size() >= 3) {
                int fid = featureIdCounter++;
                String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.VALLEY, fid);
                String dispName = geographicNamingService.formatDisplayName(key, fid);
                GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.VALLEY, dispName, key);
                for (Hex ch : cluster) {
                    feat.addHex(ch);
                }
                addGeographicFeature(feat);
            }
        }

        Set<RiverEdgeKey> visitedEdges = new HashSet<>();
        List<RiverEdgeKey> allEdges = new ArrayList<>();
        for (Hex h : this.hexes) {
            if (h == null) continue;
            for (int d = 0; d < 6; d++) {
                if (h.hasRiverEdge(d)) {
                    Hex n = h.getNeighbor(d);
                    if (n != null) {
                        RiverEdgeKey ek = new RiverEdgeKey(h, n);
                        if (!visitedEdges.contains(ek)) {
                            visitedEdges.add(ek);
                            allEdges.add(ek);
                        }
                    }
                }
            }
        }
        visitedEdges.clear();

        Map<Hex, List<RiverEdgeKey>> hexToEdges = new HashMap<>();
        for (RiverEdgeKey ek : allEdges) {
            hexToEdges.computeIfAbsent(ek.h1, k -> new ArrayList<>()).add(ek);
            hexToEdges.computeIfAbsent(ek.h2, k -> new ArrayList<>()).add(ek);
        }

        for (RiverEdgeKey edge : allEdges) {
            if (visitedEdges.contains(edge)) continue;

            List<RiverEdgeKey> edgeCluster = new ArrayList<>();
            Set<Hex> riverHexes = new HashSet<>();
            ArrayDeque<RiverEdgeKey> queue = new ArrayDeque<>();
            visitedEdges.add(edge);
            queue.add(edge);

            while (!queue.isEmpty()) {
                RiverEdgeKey curr = queue.poll();
                edgeCluster.add(curr);
                riverHexes.add(curr.h1);
                riverHexes.add(curr.h2);

                List<RiverEdgeKey> adj1 = hexToEdges.get(curr.h1);
                if (adj1 != null) {
                    for (RiverEdgeKey adj : adj1) {
                        if (!visitedEdges.contains(adj)) {
                            visitedEdges.add(adj);
                            queue.add(adj);
                        }
                    }
                }
                List<RiverEdgeKey> adj2 = hexToEdges.get(curr.h2);
                if (adj2 != null) {
                    for (RiverEdgeKey adj : adj2) {
                        if (!visitedEdges.contains(adj)) {
                            visitedEdges.add(adj);
                            queue.add(adj);
                        }
                    }
                }
            }

            if (edgeCluster.size() >= 2) {
                int fid = featureIdCounter++;
                String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.RIVER, fid);
                String dispName = geographicNamingService.formatDisplayName(key, fid);
                GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.RIVER, dispName, key);
                for (Hex rh : riverHexes) {
                    feat.addHex(rh);
                }
                addGeographicFeature(feat);
            }
        }

        Set<Hex> visitedPeninsulas = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || visitedPeninsulas.contains(h)) continue;
            if (isWaterBiome(h.getBiome()) || h.getBiome() == GameConstants.BIOME_LAKE) continue;

            int waterCount = 0;
            for (int d = 0; d < 6; d++) {
                Hex n = h.getNeighbor(d);
                if (n == null || isWaterBiome(n.getBiome())) {
                    waterCount++;
                }
            }
            if (waterCount < 3) continue;

            List<Hex> cluster = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            visitedPeninsulas.add(h);
            queue.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                cluster.add(curr);
                for (int d = 0; d < 6; d++) {
                    Hex n = curr.getNeighbor(d);
                    if (n != null && !visitedPeninsulas.contains(n) && !isWaterBiome(n.getBiome())
                            && n.getBiome() != GameConstants.BIOME_LAKE) {
                        int nWater = 0;
                        for (int nd = 0; nd < 6; nd++) {
                            Hex nn = n.getNeighbor(nd);
                            if (nn == null || isWaterBiome(nn.getBiome())) {
                                nWater++;
                            }
                        }
                        if (nWater >= 2) {
                            visitedPeninsulas.add(n);
                            queue.add(n);
                        }
                    }
                }
            }

            if (cluster.size() >= 2 && cluster.size() <= 10) {
                boolean connectsToMainland = false;
                for (Hex ph : cluster) {
                    for (int d = 0; d < 6; d++) {
                        Hex n = ph.getNeighbor(d);
                        if (n != null && !cluster.contains(n) && !isWaterBiome(n.getBiome())
                                && n.getBiome() != GameConstants.BIOME_LAKE) {
                            connectsToMainland = true;
                            break;
                        }
                    }
                    if (connectsToMainland) break;
                }

                if (connectsToMainland) {
                    int fid = featureIdCounter++;
                    String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.PENINSULA, fid);
                    String dispName = geographicNamingService.formatDisplayName(key, fid);
                    GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.PENINSULA, dispName, key);
                    for (Hex pch : cluster) {
                        feat.addHex(pch);
                    }
                    addGeographicFeature(feat);
                }
            }
        }

        Set<Hex> visitedOceans = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || visitedOceans.contains(h)) continue;
            if (!isWaterBiome(h.getBiome())) continue;

            List<Hex> cluster = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            visitedOceans.add(h);
            queue.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                cluster.add(curr);
                for (int d = 0; d < 6; d++) {
                    Hex n = curr.getNeighbor(d);
                    if (n != null && !visitedOceans.contains(n) && isWaterBiome(n.getBiome())) {
                        visitedOceans.add(n);
                        queue.add(n);
                    }
                }
            }

            if (cluster.size() >= 10) {
                int fid = featureIdCounter++;
                String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.OCEAN, fid);
                String dispName = geographicNamingService.formatDisplayName(key, fid);
                GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.OCEAN, dispName, key);
                for (Hex och : cluster) {
                    feat.addHex(och);
                }
                addGeographicFeature(feat);
            }
        }

        Set<Hex> visitedTrenches = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || visitedTrenches.contains(h) || h.getBiome() != GameConstants.BIOME_ABYSS) continue;

            List<Hex> cluster = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            visitedTrenches.add(h);
            queue.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                cluster.add(curr);
                for (int d = 0; d < 6; d++) {
                    Hex n = curr.getNeighbor(d);
                    if (n != null && !visitedTrenches.contains(n) && n.getBiome() == GameConstants.BIOME_ABYSS) {
                        visitedTrenches.add(n);
                        queue.add(n);
                    }
                }
            }

            if (!cluster.isEmpty()) {
                int fid = featureIdCounter++;
                String key = geographicNamingService.claimNameKey(GeographicFeature.FeatureType.TRENCH, fid);
                String dispName = geographicNamingService.formatDisplayName(key, fid);
                GeographicFeature feat = new GeographicFeature(fid, GeographicFeature.FeatureType.TRENCH, dispName, key);
                for (Hex tch : cluster) {
                    feat.addHex(tch);
                }
                addGeographicFeature(feat);
            }
        }

        List<Continent> allLandmasses = getAllLandmasses();
        if (allLandmasses != null) {
            for (Continent cont : allLandmasses) {
                if (cont == null || cont.getHexes().isEmpty()) continue;
                GeographicFeature.FeatureType ctype;
                if (cont.isIceCap()) {
                    ctype = GeographicFeature.FeatureType.ICE_CAP;
                } else if (cont.isArchipelago()) {
                    ctype = GeographicFeature.FeatureType.ARCHIPELAGO;
                } else {
                    ctype = GeographicFeature.FeatureType.CONTINENT;
                }
                int fid = featureIdCounter++;
                GeographicFeature feat = new GeographicFeature(fid, ctype, cont.getName());
                for (Hex ch : cont.getHexes()) {
                    feat.addHex(ch);
                }
                addGeographicFeature(feat);
            }
        }
    }

    public void ensureGeographicFeaturesReconstructed() {
        if (this.geographicFeatures.isEmpty() && this.hexes != null && !this.hexes.isEmpty()) {
            generateGeographicFeatures();
        }
    }

    public WorldGenStepListener getWorldGenStepListener() {
        return worldGenStepListener;
    }

    public void setWorldGenStepListener(WorldGenStepListener worldGenStepListener) {
        this.worldGenStepListener = worldGenStepListener;
    }

    private void notifyStep(int stepNumber, String stepName) {
        if (this.worldGenStepListener != null) {
            this.worldGenStepListener.onStepCompleted(stepNumber, stepName, this);
        }
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, GameNumbers.WORLD_RECT_HEIGHT, true);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, boolean seedDynasties) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, GameNumbers.WORLD_RECT_HEIGHT, seedDynasties);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, int height) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, 2 * height, height, true);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, int height, boolean seedDynasties) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, 2 * height, height, seedDynasties);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, int width, int height) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, width, height, true);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, int width, int height, boolean seedDynasties) {
        int h = (height > 0) ? height : GameNumbers.WORLD_RECT_HEIGHT;
        int w = (width > 0) ? width : 2 * h;
        this.continentCoreRadius = 0;
        this.worldHeight = h;
        this.worldWidth = w;
        this.worldRadius = h;
        initAndGenerateWorld(startBiome, startColony, baseName, playerTitleKey, seedDynasties);
    }

    public void generateWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey);
    }

    public void generateWorld(Biome startBiome, int height, Colony startColony, String baseName, String playerTitleKey) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, height);
    }

    public void generateWorld(Biome startBiome, int width, int height, Colony startColony, String baseName, String playerTitleKey) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, width, height);
    }

    private void initAndGenerateWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, boolean seedDynasties) {
        System.out.println("Generating World... Globe size: " + this.worldWidth + "x" + this.worldHeight + ".");
        if (startBiome == null) {
            startBiome = GameConstants.BIOME_PLAINS;
        }
        if (baseName == null || baseName.isEmpty()) {
            baseName = "Player";
        }
        if (playerTitleKey == null || playerTitleKey.isEmpty()) {
            playerTitleKey = GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey();
        }
        if (!this.spectatorMode && startColony == null) {
            startColony = new Colony(1, baseName + " Prime", true);
        }
        // Step 1: Initialize world state and player dynasty
        this.hexes.clear();
        this.hexCoordCache.clear();
        markColonizedHexIndexDirty();
        this.continents.clear();
        this.dynastys.clear();
        this.continentCount = 0;
        this.islandCount = 0;
        Map<String, Hex> hexMap = new HashMap<>();
        if (seedDynasties && !this.spectatorMode) {
            ColonyStarterService starterService = ColonyStarterService.shared();
            if (startColony.getAntTotal() == 0) {
                starterService.initializeNewColony(startColony);
            }
            
            baseName = formatName(baseName);
            DynastyTitle playerTitle = GameConstants.getDynastyTitleByKey(playerTitleKey);
            String dynName = LanguageStrings.formatDynastyName(baseName, playerTitle);
            namingService.registerUsedName(dynName);
            
            Dynasty playerDynasty = new Dynasty(this.dynastyIdCounter++, dynName, playerTitle.getNameKey(), true, GameConstants.SPECIES_OMNI);
            playerDynasty.setThemeBase(baseName);
            playerDynasty.getStarterService().initializeDynasty(playerDynasty);
            
            String capName = namingService.generateCapitalName(baseName);
            startColony.setName(capName);
            
            playerDynasty.addColony(startColony);
            playerDynasty.setCapital(startColony);
            registerDynasty(playerDynasty);
            historyService.record(WorldHistoryEventType.DYNASTY_FORMED, LanguageStrings.HISTORY_DYNASTY_FORMED_FMT,
                    playerDynasty.getId(), startColony.getId(), -1,
                    WorldHistoryEvent.dynastyArg(playerDynasty.getId()));
            historyService.record(WorldHistoryEventType.COLONY_FOUNDED, LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                    playerDynasty.getId(), startColony.getId(), -1,
                    WorldHistoryEvent.colonyArg(startColony.getId()),
                    WorldHistoryEvent.dynastyArg(playerDynasty.getId()));
            
            this.colonyIdCounter = startColony.getId() + 1;
        }
        notifyStep(1, "Initialize World");

        // Step 2: Allocate hex grid and polar boundaries
        int minR = -this.worldHeight / 2;
        int maxR = this.worldHeight / 2 - 1;
        int minCol = -this.worldWidth / 2;
        int maxCol = this.worldWidth / 2 - 1;

        for (int r = minR; r <= maxR; r++) {
            double absLat = Math.abs((double) r) / (this.worldHeight / 2.0);

            for (int col = minCol; col <= maxCol; col++) {
                int q = col - (r >> 1);
                Hex hex = new Hex();
                hex.setQ(q);
                hex.setR(r);
                hex.setTimeOffset(GameNumbers.calculateTimeZoneOffset(col, this.worldWidth));
                hex.setActive(false);
                hex.setColony(null);
                hex.setIsland(false);

                boolean isIce = false;
                if (this.hasIceCaps) {
                    double wave = 0.07 * Math.sin(col * 0.18 + (r > 0 ? 1.0 : -1.0))
                            + 0.04 * Math.cos(col * 0.38)
                            + 0.02 * Math.sin(col * 0.75);
                    double oneRow = 1.0 / Math.max(1, this.worldHeight / 2.0);
                    double polarLimit = this.iceCapPolarLimit + oneRow + wave;
                    double transitionLimit = polarLimit - 0.08;

                    if (absLat >= polarLimit) {
                        isIce = true;
                    } else if (absLat >= transitionLimit) {
                        double fraction = (absLat - transitionLimit) / (polarLimit - transitionLimit);
                        int chance = (int) Math.round(fraction * 70.0);
                        isIce = (GameRandom.nextInt(100) < chance);
                    }
                }

                if (isIce) {
                    hex.setBiome(GameConstants.BIOME_ICE_FIELD);
                    hex.setLocalWeather(getRandomWeather(GameConstants.BIOME_ICE_FIELD));
                } else {
                    hex.setBiome(GameConstants.BIOME_OCEAN);
                    hex.setLocalWeather(getRandomWeather(GameConstants.BIOME_OCEAN));
                }

                hexMap.put(q + "," + r, hex);
                this.hexes.add(hex);
            }
        }
        notifyStep(2, "Allocate Hex Grid");
        // Step 3: Link hex neighbor adjacency
        linkNeighbors(hexMap);
        notifyStep(3, "Link Neighbors");

        generateTectonicPlates();
        notifyStep(4, "Generate Tectonic Plates");

        generateFaultLinesAndElevation();
        notifyStep(5, "Generate Fault Lines & Elevation");

        generateMultiContinents(startBiome, startColony, seedDynasties);

        // Step 16: Finalize indexing and bind dynasty trade services
        markColonizedHexIndexDirty();
        rebuildColonizedHexIndexIfNeeded();
        bindDynastyTradeServices();
        notifyStep(16, "Finalize World");
        System.out.println("[World] Landmasses: " + this.continentCount + " continent(s), "
                + this.islandCount + " island(s).");
    }

    private void generateTectonicPlates() {
        this.tectonicPlates.clear();
        this.faultLines.clear();
        if (this.hexes == null || this.hexes.isEmpty()) {
            return;
        }

        int numPlates = (this.tectonicPlateCount > 0)
                ? this.tectonicPlateCount
                : ((this.worldWidth > 0 && this.worldHeight > 0)
                        ? Math.max(4, Math.min(12, (this.worldWidth * this.worldHeight) / 100))
                        : 6);
        numPlates = Math.max(2, numPlates);

        List<Hex> plateCenters = new ArrayList<>();
        if (this.worldWidth > 0 && this.worldHeight > 0) {
            int sectorWidth = Math.max(1, this.worldWidth / numPlates);
            int latSpan = Math.max(2, (int) Math.round(this.worldHeight * 0.35));
            for (int i = 0; i < numPlates; i++) {
                int colCenter = -this.worldWidth / 2 + i * sectorWidth + sectorWidth / 2;
                int colVar = (sectorWidth > 2) ? GameRandom.nextInt(sectorWidth / 2) - sectorWidth / 4 : 0;
                int col = colCenter + colVar;
                int rCenter = (i % 2 == 0) ? -latSpan / 2 : latSpan / 2;
                int rVar = (latSpan > 2) ? GameRandom.nextInt(latSpan / 2) - latSpan / 4 : 0;
                int r = rCenter + rVar;
                int q = col - (r >> 1);
                Hex h = getHexAt(q, r);
                if (h != null && !plateCenters.contains(h)) {
                    plateCenters.add(h);
                }
            }
        }

        int attempts = 0;
        while (plateCenters.size() < numPlates && attempts++ < 300) {
            Hex candidate = this.hexes.get(GameRandom.nextInt(this.hexes.size()));
            if (candidate != null && !plateCenters.contains(candidate)) {
                plateCenters.add(candidate);
            }
        }

        for (int i = 0; i < plateCenters.size(); i++) {
            Hex center = plateCenters.get(i);
            int plateId = i + 1;
            double driftAngle = GameRandom.nextDouble() * 2.0 * Math.PI;
            double driftSpeed = 0.5 + GameRandom.nextDouble() * 1.5;
            boolean oceanic = (plateCenters.size() > 2)
                    ? ((i % 2 == 0) || (GameRandom.nextInt(100) < 55))
                    : (i == 0);
            TectonicPlate plate = new TectonicPlate(plateId, center.getQ(), center.getR(), driftAngle, driftSpeed, oceanic);
            plate.addHex(center);
            this.tectonicPlates.add(plate);
        }

        ArrayDeque<Hex> queue = new ArrayDeque<>(plateCenters);
        while (!queue.isEmpty()) {
            Hex cur = queue.poll();
            TectonicPlate plate = getPlateById(cur.getPlateId());
            if (plate == null) continue;
            for (Hex neighbor : cur.getAdjacentNeighbors()) {
                if (neighbor != null && neighbor.getPlateId() == 0) {
                    plate.addHex(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }

    private void generateFaultLinesAndElevation() {
        this.faultLines.clear();
        if (this.hexes == null || this.hexes.isEmpty() || this.tectonicPlates.isEmpty()) {
            return;
        }

        Map<Long, FaultLine> faultMap = new HashMap<>();
        List<Hex> boundaryHexes = new ArrayList<>();
        Map<Hex, FaultLine.FaultType> boundaryFaultTypes = new HashMap<>();
        Map<Hex, Integer> boundaryFaultIntensity = new HashMap<>();

        for (Hex h : this.hexes) {
            if (h == null || h.getPlateId() <= 0) continue;
            int idA = h.getPlateId();
            TectonicPlate plateA = getPlateById(idA);
            if (plateA == null) continue;

            for (Hex n : h.getAdjacentNeighbors()) {
                if (n == null || n.getPlateId() <= 0) continue;
                int idB = n.getPlateId();
                if (idA == idB) continue;

                TectonicPlate plateB = getPlateById(idB);
                if (plateB == null) continue;

                long key = (((long) Math.min(idA, idB)) << 32) | (Math.max(idA, idB) & 0xffffffffL);
                FaultLine fault = faultMap.get(key);
                if (fault == null) {
                    double vRelX = plateA.getDriftVectorX() - plateB.getDriftVectorX();
                    double vRelY = plateA.getDriftVectorY() - plateB.getDriftVectorY();

                    int signCol = (n.getQ() + (n.getR() >> 1)) - (h.getQ() + (h.getR() >> 1));
                    if (this.worldWidth > 0) {
                        if (signCol > this.worldWidth / 2) signCol -= this.worldWidth;
                        else if (signCol < -this.worldWidth / 2) signCol += this.worldWidth;
                    }
                    double dx = signCol * Math.sqrt(3.0);
                    double dy = (n.getR() - h.getR()) * 1.5;
                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len > 0.0001) {
                        dx /= len;
                        dy /= len;
                    }
                    double dot = vRelX * dx + vRelY * dy;
                    FaultLine.FaultType type;
                    if (dot > 0.15) {
                        type = FaultLine.FaultType.CONVERGENT;
                    } else if (dot < -0.15) {
                        type = FaultLine.FaultType.DIVERGENT;
                    } else {
                        type = FaultLine.FaultType.TRANSFORM;
                    }
                    int intensity = (Math.abs(dot) >= 0.35 || GameRandom.nextInt(100) < 35) ? 2 : 1;
                    fault = new FaultLine(Math.min(idA, idB), Math.max(idA, idB), type, intensity);
                    faultMap.put(key, fault);
                    this.faultLines.add(fault);
                }
                fault.addBoundaryPair(h, n);
                if (!boundaryHexes.contains(h)) {
                    boundaryHexes.add(h);
                    boundaryFaultTypes.put(h, fault.getType());
                    boundaryFaultIntensity.put(h, fault.getIntensity());
                } else if (fault.getIntensity() > boundaryFaultIntensity.getOrDefault(h, 1)) {
                    boundaryFaultTypes.put(h, fault.getType());
                    boundaryFaultIntensity.put(h, fault.getIntensity());
                }
            }
        }

        for (Hex h : this.hexes) {
            if (h == null) continue;
            TectonicPlate plate = getPlateById(h.getPlateId());
            int baseElevation = (plate != null && plate.isOceanic()) ? 1 : 4;
            h.setElevation(baseElevation);
        }

        Map<Hex, Integer> faultDist = new HashMap<>();
        Map<Hex, FaultLine.FaultType> nearestFaultType = new HashMap<>();
        Map<Hex, Integer> nearestFaultIntensity = new HashMap<>();
        ArrayDeque<Hex> faultQueue = new ArrayDeque<>(boundaryHexes);
        for (Hex bh : boundaryHexes) {
            faultDist.put(bh, 0);
            nearestFaultType.put(bh, boundaryFaultTypes.get(bh));
            nearestFaultIntensity.put(bh, boundaryFaultIntensity.getOrDefault(bh, 1));
        }

        while (!faultQueue.isEmpty()) {
            Hex cur = faultQueue.poll();
            int d = faultDist.get(cur);
            if (d >= 3) continue;
            FaultLine.FaultType fType = nearestFaultType.get(cur);
            int intn = nearestFaultIntensity.getOrDefault(cur, 1);
            for (Hex n : cur.getAdjacentNeighbors()) {
                if (n != null && !faultDist.containsKey(n)) {
                    faultDist.put(n, d + 1);
                    nearestFaultType.put(n, fType);
                    nearestFaultIntensity.put(n, intn);
                    faultQueue.add(n);
                }
            }
        }

        for (Map.Entry<Hex, Integer> entry : faultDist.entrySet()) {
            Hex h = entry.getKey();
            int dist = entry.getValue();
            FaultLine.FaultType fType = nearestFaultType.get(h);
            int intensity = nearestFaultIntensity.getOrDefault(h, 1);
            TectonicPlate plate = getPlateById(h.getPlateId());
            boolean isOceanPlate = (plate != null && plate.isOceanic());
            if (fType == FaultLine.FaultType.CONVERGENT) {
                if (isOceanPlate && dist == 0) {
                    h.setElevation(-2);
                } else {
                    int uplift = switch (dist) {
                        case 0 -> (int) Math.round(((intensity == 2 ? 5 : 3) + GameRandom.nextInt(3)) * faultUpliftFactor);
                        case 1 -> (int) Math.round(((intensity == 2 ? 3 : 2) + GameRandom.nextInt(2)) * faultUpliftFactor);
                        case 2 -> intensity == 2 ? 2 : 1;
                        default -> 0;
                    };
                    h.setElevation(h.getElevation() + uplift);
                }
            } else if (fType == FaultLine.FaultType.DIVERGENT) {
                int subsidence = switch (dist) {
                    case 0 -> (intensity == 2 ? 4 : 2) + GameRandom.nextInt(3);
                    case 1 -> (intensity == 2 ? 2 : 1) + GameRandom.nextInt(2);
                    case 2 -> intensity == 2 ? 1 : 0;
                    default -> 0;
                };
                h.setElevation(h.getElevation() - subsidence);
                if (isOceanPlate && dist == 0) {
                    h.setElevation(Math.min(h.getElevation(), -2));
                }
            } else if (fType == FaultLine.FaultType.TRANSFORM) {
                if (intensity == 2) {
                    if (dist == 0) {
                        int flattened = (plate != null && plate.isOceanic()) ? 2 : 3;
                        h.setElevation(flattened);
                    } else if (dist == 1 && GameRandom.nextInt(100) < 50) {
                        int flattened = (plate != null && plate.isOceanic()) ? 2 : 3;
                        h.setElevation((h.getElevation() + flattened) / 2);
                    }
                } else {
                    if (dist == 0 && GameRandom.nextInt(100) < 40) {
                        h.setElevation(h.getElevation() + (GameRandom.nextInt(3) - 1));
                    }
                }
            }
        }

        int numFeatures = (this.peakValleyCount > 0)
                ? this.peakValleyCount
                : ((this.worldWidth > 0 && this.worldHeight > 0)
                        ? Math.max(6, (this.worldWidth * this.worldHeight) / 90)
                        : 8);

        for (int i = 0; i < numFeatures; i++) {
            Hex center = this.hexes.get(GameRandom.nextInt(this.hexes.size()));
            if (center == null) continue;
            boolean isPeak = (i % 2 == 0) || (GameRandom.nextInt(100) < 60);
            int delta = isPeak ? (2 + GameRandom.nextInt(2)) : -(2 + GameRandom.nextInt(2));
            center.setElevation(center.getElevation() + delta);
            for (Hex n : center.getAdjacentNeighbors()) {
                if (n != null) {
                    n.setElevation(n.getElevation() + (isPeak ? 1 : -1));
                }
            }
        }

        for (Hex h : this.hexes) {
            if (h == null) continue;
            if (GameRandom.nextInt(100) < 35) {
                h.setElevation(h.getElevation() + (GameRandom.nextInt(3) - 1));
            }
            h.setElevation(Math.max(0, Math.min(10, h.getElevation())));
        }

        calibrateSeaLevelContour();
    }

    private void calibrateSeaLevelContour() {
        if (this.hexes == null || this.hexes.isEmpty()) return;
        int nonIceTotal = 0;
        int polarCutoff = Math.max(2, (int) Math.round(this.worldHeight * 0.44) + 1);
        for (Hex h : this.hexes) {
            if (h.getBiome() == GameConstants.BIOME_ICE_FIELD && Math.abs(h.getR()) >= polarCutoff) {
                continue;
            }
            nonIceTotal++;
        }
        int targetLand = (int) Math.round(nonIceTotal * this.continentalLandPercentage);
        int currentLand = 0;
        for (Hex h : this.hexes) {
            if (h.getElevation() >= 4) {
                currentLand++;
            }
        }

        if (currentLand < targetLand) {
            int needed = targetLand - currentLand;
            List<Hex> elev3 = new ArrayList<>();
            for (Hex h : this.hexes) {
                if (h.getElevation() == 3) {
                    elev3.add(h);
                }
            }
            Collections.shuffle(elev3, GameRandom.getShuffleRandom());
            for (int i = 0; i < Math.min(needed, elev3.size()); i++) {
                elev3.get(i).setElevation(4);
            }
        } else if (currentLand > targetLand) {
            int excess = currentLand - targetLand;
            List<Hex> elev4 = new ArrayList<>();
            for (Hex h : this.hexes) {
                if (h.getElevation() == 4) {
                    elev4.add(h);
                }
            }
            Collections.shuffle(elev4, GameRandom.getShuffleRandom());
            for (int i = 0; i < Math.min(excess, elev4.size()); i++) {
                elev4.get(i).setElevation(3);
            }
        }

        for (Hex h : this.hexes) {
            if (h != null) {
                h.setElevation(Math.max(0, Math.min(10, h.getElevation())));
            }
        }
    }

    private void generateMultiContinents(Biome startBiome, Colony startColony, boolean seedDynasties) {
        // Step 4: Seed and grow continental landmasses
        int numContinents = (this.targetContinentCount > 0)
                ? this.targetContinentCount
                : ((this.worldHeight < 25)
                        ? (2 + GameRandom.nextInt(2))
                        : (6 + GameRandom.nextInt(2)));
        List<Hex> seeds = new ArrayList<>();

        int sectorWidth = this.worldWidth / numContinents;
        int latOffset = Math.max(1, (int) Math.round(this.worldHeight * 0.15));
        for (int i = 0; i < numContinents; i++) {
            int colCenter = -this.worldWidth / 2 + i * sectorWidth + sectorWidth / 2;
            int rCenter = (i % 2 == 0) ? -latOffset : latOffset;
            int rVar = GameRandom.nextInt(Math.max(1, latOffset / 2 + 1)) - (latOffset / 4);
            int r = rCenter + rVar;
            int colVar = GameRandom.nextInt(Math.max(1, sectorWidth / 2)) - sectorWidth / 4;
            int col = colCenter + colVar;
            int q = col - (r >> 1);
            Hex hex = getHexAt(q, r);
            if (hex != null && hex.getBiome() == GameConstants.BIOME_OCEAN && !seeds.contains(hex)) {
                seeds.add(hex);
            }
        }
        int minColDist = Math.max(3, (int) Math.round(10 * (this.worldHeight / 50.0)));
        int minRDist = Math.max(2, (int) Math.round(8 * (this.worldHeight / 50.0)));
        int attempts = 0;
        while (seeds.size() < numContinents && attempts++ < 500) {
            int col = -this.worldWidth / 2 + GameRandom.nextInt(this.worldWidth);
            int r = (seeds.size() % 2 == 0 ? -latOffset : latOffset) + GameRandom.nextInt(Math.max(1, latOffset / 2 + 1)) - (latOffset / 4);
            int q = col - (r >> 1);
            Hex hex = getHexAt(q, r);
            if (hex != null && hex.getBiome() == GameConstants.BIOME_OCEAN && !seeds.contains(hex)) {
                boolean tooClose = false;
                for (Hex s : seeds) {
                    int sR = s.getR();
                    int sCol = s.getQ() + (sR >> 1);
                    int dCol = Math.abs(col - sCol);
                    if (dCol > this.worldWidth / 2) dCol = this.worldWidth - dCol;
                    int dR = Math.abs(r - sR);
                    if (dCol < minColDist && dR < minRDist) {
                        tooClose = true;
                        break;
                    }
                }
                if (!tooClose) {
                    seeds.add(hex);
                }
            }
        }

        int nonIceHexCount = 0;
        int polarCutoff = Math.max(2, (int) Math.round(this.worldHeight * 0.44) + 1);
        for (Hex h : this.hexes) {
            if (h.getBiome() == GameConstants.BIOME_ICE_FIELD && Math.abs(h.getR()) >= polarCutoff) {
                continue;
            }
            nonIceHexCount++;
        }

        int totalContinentalTarget = (int) Math.round(nonIceHexCount * this.continentalLandPercentage);
        int basePerContinent = totalContinentalTarget / seeds.size();
        int minHexesPerContinent = (int) Math.round(basePerContinent * 0.70);

        int[] targets = new int[seeds.size()];
        int assignedSum = 0;
        for (int i = 0; i < seeds.size(); i++) {
            double variance = 0.85 + GameRandom.nextDouble() * 0.30;
            targets[i] = Math.max(minHexesPerContinent, (int) Math.round(basePerContinent * variance));
            assignedSum += targets[i];
        }
        if (assignedSum < totalContinentalTarget) {
            int diff = totalContinentalTarget - assignedSum;
            for (int i = 0; i < diff; i++) {
                targets[i % seeds.size()]++;
            }
        }

        Map<Integer, Set<Hex>> continentHexes = new HashMap<>();
        Map<Integer, List<Hex>> continentFrontiers = new HashMap<>();

        int[] shapeTypes = new int[seeds.size()];
        int[] bayCols = new int[seeds.size()];
        int[] bayRs = new int[seeds.size()];
        double[] bayRadSq = new double[seeds.size()];
        double[] axisAngles = new double[seeds.size()];
        double[] maxPerpWidths = new double[seeds.size()];
        double[] maxRadiusSq = new double[seeds.size()];
        double[] orientAngles = new double[seeds.size()];
        double[] inRadii = new double[seeds.size()];

        double scale = Math.max(0.25, this.worldHeight / 50.0);

        for (int i = 0; i < seeds.size(); i++) {
            int cid = i + 1;
            Hex seed = seeds.get(i);
            seed.setContinentId(cid);
            Set<Hex> hexSet = new HashSet<>();
            hexSet.add(seed);
            continentHexes.put(cid, hexSet);

            List<Hex> frontier = new ArrayList<>();
            frontier.add(seed);
            continentFrontiers.put(cid, frontier);

            int sR = seed.getR();
            int sCol = seed.getQ() + (sR >> 1);

            shapeTypes[i] = GameRandom.nextInt(4);

            double bayAngle = GameRandom.nextDouble() * 2.0 * Math.PI;
            double bayDist = (8.0 + GameRandom.nextDouble() * 6.0) * scale;
            bayCols[i] = sCol + (int) Math.round(bayDist * Math.cos(bayAngle));
            bayRs[i] = sR + (int) Math.round((bayDist * Math.sin(bayAngle)) / 1.5);
            double bayRad = (6.0 + GameRandom.nextDouble() * 4.0) * scale;
            bayRadSq[i] = bayRad * bayRad * 3.0;

            axisAngles[i] = GameRandom.nextDouble() * Math.PI;
            maxPerpWidths[i] = (4.0 + GameRandom.nextDouble() * 2.5) * scale;

            double cRad = (13.0 + GameRandom.nextDouble() * 4.0) * scale;
            maxRadiusSq[i] = cRad * cRad * 3.0;

            orientAngles[i] = GameRandom.nextDouble() * 2.0 * Math.PI;
            inRadii[i] = (10.0 + GameRandom.nextDouble() * 4.0) * scale;
        }

        int maxRounds = 30000;
        int round = 0;
        int consecutiveFails = 0;

        while (round++ < maxRounds && consecutiveFails < 500) {
            boolean grewThisRound = false;
            for (int i = 0; i < seeds.size(); i++) {
                int cid = i + 1;
                Set<Hex> cSet = continentHexes.get(cid);
                List<Hex> frontier = continentFrontiers.get(cid);
                if (cSet == null || frontier == null || cSet.size() >= targets[i]) {
                    continue;
                }

                if (frontier.isEmpty() && cSet.size() < targets[i]) {
                    for (Hex h : cSet) {
                        for (Hex n : h.getAdjacentNeighbors()) {
                            if (n != null && n.getContinentId() == 0) {
                                frontier.add(h);
                                break;
                            }
                        }
                    }
                    if (frontier.isEmpty()) {
                        continue;
                    }
                }

                Hex current = frontier.get(GameRandom.nextInt(frontier.size()));
                List<Hex> candidates = new ArrayList<>();
                boolean underMin = (consecutiveFails >= 100);

                for (Hex n : current.getAdjacentNeighbors()) {
                    if (n == null) continue;
                    if (n.getContinentId() != 0) continue;
                    if (this.hasIceCaps) {
                        int col = n.getQ() + (n.getR() >> 1);
                        double wave = 0.05 * Math.sin(col * 0.22) + 0.03 * Math.cos(col * 0.48);
                        double absLat = Math.abs((double) n.getR()) / (this.worldHeight / 2.0);
                        if (absLat >= (this.iceCapPolarLimit - 0.02 + wave) || n.getBiome() == GameConstants.BIOME_ICE_FIELD || touchesIceField(n)) {
                            continue;
                        }
                    }

                    boolean touchesOtherContinent = false;
                    for (Hex nn : n.getAdjacentNeighbors()) {
                        if (nn != null && nn.getContinentId() != 0 && nn.getContinentId() != cid) {
                            touchesOtherContinent = true;
                            break;
                        }
                    }
                    if (touchesOtherContinent) {
                        continue;
                    }

                    if (!underMin) {
                        Hex s = seeds.get(i);
                        int sR = s.getR();
                        int sCol = s.getQ() + (sR >> 1);
                        int nR = n.getR();
                        int nCol = n.getQ() + (nR >> 1);

                        int signCol = nCol - sCol;
                        if (signCol > this.worldWidth / 2) signCol -= this.worldWidth;
                        else if (signCol < -this.worldWidth / 2) signCol += this.worldWidth;

                        double dx = signCol * Math.sqrt(3.0);
                        double dy = (nR - sR) * 1.5;

                        int shape = shapeTypes[i];
                        if (shape == 3) {
                            int bCol = Math.abs(nCol - bayCols[i]);
                            if (bCol > this.worldWidth / 2) bCol = this.worldWidth - bCol;
                            double bdx = bCol * Math.sqrt(3.0);
                            double bdy = (nR - bayRs[i]) * 1.5;
                            if (bdx * bdx + bdy * bdy < bayRadSq[i] && GameRandom.nextInt(100) < 95) {
                                continue;
                            }
                        } else if (shape == 2) {
                            double perp = Math.abs(dx * (-Math.sin(axisAngles[i])) + dy * Math.cos(axisAngles[i])) / Math.sqrt(3.0);
                            if (perp > maxPerpWidths[i] && GameRandom.nextInt(100) < 90) {
                                continue;
                            }
                        } else if (shape == 1) {
                            double distSq = dx * dx + dy * dy;
                            if (distSq > maxRadiusSq[i] && GameRandom.nextInt(100) < 85) {
                                continue;
                            }
                        } else if (shape == 0) {
                            double angle = Math.atan2(dy, dx) - orientAngles[i];
                            double sector = (2.0 * Math.PI) / 3.0;
                            double phi = ((angle % sector) + sector) % sector;
                            double rBound = inRadii[i] / Math.cos(Math.abs(phi - sector / 2.0));
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            if (dist > rBound && GameRandom.nextInt(100) < 85) {
                                continue;
                            }
                        }
                    }

                    candidates.add(n);
                }

                if (!candidates.isEmpty()) {
                    Hex chosen = candidates.get(GameRandom.nextInt(candidates.size()));
                    chosen.setContinentId(cid);
                    cSet.add(chosen);
                    frontier.add(chosen);
                    grewThisRound = true;
                } else {
                    frontier.remove(current);
                }
            }
            if (grewThisRound) {
                consecutiveFails = 0;
            } else {
                consecutiveFails++;
            }
        }

        List<Hex> allLand = new ArrayList<>();
        for (Set<Hex> cSet : continentHexes.values()) {
            allLand.addAll(cSet);
        }
        notifyStep(6, "Grow Continents");

        generateFaultLinePeninsulas(continentHexes, allLand);

        // Step 7: Convert enclosed oceans to lakes
        List<List<Hex>> enclosedLakeComponents = convertEnclosedOceansToLakes(continentHexes, allLand);
        notifyStep(7, "Convert Enclosed Lakes");

        // Step 8: Compute coastal water distance via BFS
        ArrayDeque<Hex> bfsQueue = new ArrayDeque<>();
        for (Hex h : allLand) {
            boolean coastal = false;
            for (Hex n : h.getAdjacentNeighbors()) {
                if (n == null || n.getContinentId() == 0) {
                    coastal = true;
                    break;
                }
            }
            h.setCoastal(coastal);
            if (coastal) {
                h.setWaterDistance(1);
                bfsQueue.add(h);
            } else {
                h.setWaterDistance(Integer.MAX_VALUE);
            }
        }

        while (!bfsQueue.isEmpty()) {
            Hex cur = bfsQueue.poll();
            int nextDist = cur.getWaterDistance() + 1;
            for (Hex n : cur.getAdjacentNeighbors()) {
                if (n != null && n.getContinentId() != 0 && n.getWaterDistance() > nextDist) {
                    n.setWaterDistance(nextDist);
                    bfsQueue.add(n);
                }
            }
        }
        for (Hex h : allLand) {
            if (h == null) continue;
            int d = h.getWaterDistance();
            int baseElev = (d <= 1) ? 3 : ((d == 2) ? 4 : ((d == 3) ? 5 : 6));
            int col = h.getQ() + (h.getR() >> 1);
            double wave = 0.8 * Math.sin(col * 0.35 + h.getR() * 0.28)
                    + 0.5 * Math.cos(col * 0.70 - h.getR() * 0.50);
            int waveOffset = (int) Math.round(wave);
            int jitter = GameRandom.nextInt(3) - 1;
            int elev = baseElev + waveOffset + jitter;
            if (h.getElevation() >= 6) {
                elev = Math.max(elev, h.getElevation());
            }
            h.setElevation(Math.max(3, Math.min(10, elev)));
        }
        notifyStep(8, "Compute Coastal Distance");

        Map<Hex, Integer> oceanLandDist = new HashMap<>();
        ArrayDeque<Hex> oceanBfs = new ArrayDeque<>();
        for (Hex h : allLand) {
            if (h.isCoastal()) {
                for (Hex n : h.getAdjacentNeighbors()) {
                    if (n != null && n.getContinentId() == 0 && !oceanLandDist.containsKey(n)) {
                        oceanLandDist.put(n, 1);
                        oceanBfs.add(n);
                    }
                }
            }
        }
        while (!oceanBfs.isEmpty()) {
            Hex cur = oceanBfs.poll();
            int dist = oceanLandDist.get(cur);
            if (dist < 9) {
                for (Hex n : cur.getAdjacentNeighbors()) {
                    if (n != null && n.getContinentId() == 0 && !oceanLandDist.containsKey(n)) {
                        oceanLandDist.put(n, dist + 1);
                        oceanBfs.add(n);
                    }
                }
            }
        }
        for (Hex h : this.hexes) {
            if (h == null || allLand.contains(h) || h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                continue;
            }
            int d = oceanLandDist.getOrDefault(h, 9);
            if (d == 1) {
                h.setElevation(2);
                h.setWaterDistance(1);
            } else if (d <= 6) {
                h.setElevation(1);
                h.setWaterDistance(d);
            } else {
                h.setElevation(0);
                h.setWaterDistance(d);
            }
        }

        // Step 7: Generate inland lakes
        List<Hex> createdLakes = new ArrayList<>();
        for (Map.Entry<Integer, Set<Hex>> entry : continentHexes.entrySet()) {
            Set<Hex> cSet = entry.getValue();
            List<Hex> deepCandidates = new ArrayList<>();
            List<Hex> lakeCandidates = new ArrayList<>();
            for (Hex h : cSet) {
                if (touchesIceField(h)) continue;
                if (this.worldHeight > 0 && Math.abs(h.getR()) >= (this.worldHeight / 2 - 4)) continue;
                if (h.getWaterDistance() >= 3 && !h.isCoastal()) {
                    deepCandidates.add(h);
                } else if (h.getWaterDistance() >= 2 && !h.isCoastal()) {
                    lakeCandidates.add(h);
                }
            }
            List<Hex> pool = new ArrayList<>(deepCandidates);
            pool.addAll(lakeCandidates);
            pool.sort((a, b) -> Integer.compare(b.getWaterDistance(), a.getWaterDistance()));
            int lakeCount = Math.min(12, Math.max(7, (deepCandidates.size() + lakeCandidates.size()) / 8));
            int created = 0;
            for (Hex candidate : pool) {
                if (created >= lakeCount) break;
                boolean nearOtherLake = false;
                for (Hex n : candidate.getAdjacentNeighbors()) {
                    if (n != null && n.getBiome() == GameConstants.BIOME_LAKE) {
                        nearOtherLake = true;
                        break;
                    }
                }
                if (!nearOtherLake) {
                    candidate.setBiome(GameConstants.BIOME_LAKE);
                    candidate.setLocalWeather(getRandomWeather(GameConstants.BIOME_LAKE));
                    createdLakes.add(candidate);
                    created++;

                    if (created < lakeCount && GameRandom.nextInt(100) < 50) {
                        List<Hex> adjLand = new ArrayList<>();
                        for (Hex n : candidate.getAdjacentNeighbors()) {
                            if (n != null && !n.isCoastal() && n.getBiome() != GameConstants.BIOME_LAKE && cSet.contains(n)) {
                                adjLand.add(n);
                            }
                        }
                        if (!adjLand.isEmpty()) {
                            Hex clusterHex = adjLand.get(GameRandom.nextInt(adjLand.size()));
                            clusterHex.setBiome(GameConstants.BIOME_LAKE);
                            clusterHex.setLocalWeather(getRandomWeather(GameConstants.BIOME_LAKE));
                            createdLakes.add(clusterHex);
                            created++;
                        }
                    }
                }
            }
        }
        notifyStep(9, "Generate Inland Lakes");

        // Step 10: Assign land and water biomes by elevation, latitude, hydrology, and coastal proximity
        int latScale = this.worldHeight / 2;
        for (Hex h : allLand) {
            if (h.getBiome() == GameConstants.BIOME_LAKE) {
                continue;
            }
            Biome biome = pickBiomeForLandHex(h, latScale);
            h.setBiome(biome);
            h.setLocalWeather(getRandomWeather(biome));
        }
        for (Hex h : this.hexes) {
            if (h == null || allLand.contains(h) || h.getBiome() == GameConstants.BIOME_LAKE || h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                continue;
            }
            if (h.getElevation() <= -1) {
                h.setBiome(GameConstants.BIOME_ABYSS);
                h.setLocalWeather(getRandomWeather(GameConstants.BIOME_ABYSS));
            } else if (h.getElevation() == 0) {
                h.setBiome(GameConstants.BIOME_DEEP_OCEAN);
                h.setLocalWeather(getRandomWeather(GameConstants.BIOME_DEEP_OCEAN));
            } else if (h.getElevation() == 1) {
                h.setBiome(GameConstants.BIOME_OCEAN);
                h.setLocalWeather(getRandomWeather(GameConstants.BIOME_OCEAN));
            } else {
                h.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
                h.setLocalWeather(getRandomWeather(GameConstants.BIOME_SHALLOW_OCEAN));
            }
        }
        if (this.hasIceCaps && this.worldHeight > 0) {
            int polarMargin = Math.max(2, (int) Math.round(this.worldHeight * 0.38));
            for (Hex h : this.hexes) {
                if (h == null || allLand.contains(h) || h.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                    continue;
                }
                if (Math.abs(h.getR()) >= polarMargin) {
                    int rDist = Math.abs(h.getR()) - polarMargin;
                    int col = h.getQ() + (h.getR() >> 1);
                    double wave = 0.6 * Math.sin(col * 0.30) + 0.4 * Math.cos(col * 0.60);
                    int base = (rDist <= 1) ? 3 : (rDist <= 3 ? 4 : (rDist <= 5 ? 5 : 6));
                    int jitter = GameRandom.nextInt(3) - 1;
                    int elev = base + (int) Math.round(wave) + jitter;
                    if (h.getElevation() >= 7) {
                        elev = Math.max(elev, h.getElevation());
                    }
                    h.setElevation(Math.max(3, Math.min(10, elev)));
                    if (h.getElevation() >= 9) {
                        h.setBiome(GameConstants.BIOME_PEAK);
                        h.setLocalWeather(getRandomWeather(GameConstants.BIOME_PEAK));
                    } else if (h.getElevation() >= 7 && GameRandom.nextInt(100) < 30) {
                        h.setBiome(GameConstants.BIOME_MOUNTAIN);
                        h.setLocalWeather(getRandomWeather(GameConstants.BIOME_MOUNTAIN));
                    }
                }
            }
        }
        notifyStep(10, "Assign Biomes");

        // Step 11: Generate archipelagos and solitary islands
        generateArchipelagosAndIslands();
        int minIslands = (this.minIslandCount >= 0)
                ? this.minIslandCount
                : (this.worldHeight >= 30 ? GameNumbers.WORLD_MIN_ISLAND_COUNT : 2);
        ensureMinimumIslands(minIslands);
        notifyStep(11, "Generate Archipelagos & Islands");

        // Step 12: Classify landmasses into continents, archipelagos, and ice caps
        fuseRobustlyConnectedLandmasses();
        classifyLandmasses();
        notifyStep(12, "Classify Landmasses");

        convertEnclosedOceansToLakes(continentHexes, allLand);
        convertLakesSurroundedByIce();
        classifyLandmasses();

        // Step 13: Enforce minimum biome diversity quotas
        ensureMinimumBiomeCounts(this.worldHeight >= 30 ? 25 : Math.max(1, allLand.size() / 35));
        convertEnclosedOceansToLakes(continentHexes, allLand);
        convertLakesSurroundedByIce();
        notifyStep(13, "Enforce Biome Quotas");

        // Step 14: Generate edge river networks from lakes and glaciated mountain peaks
        generateAllDownhillRivers();
        checkEnclosedHexes();
        preventSurroundedHexes();
        clearRiversOnIce();
        cleanupOrphanRivers();
        ensureAllLakesHaveRivers();
        for (Hex h : this.hexes) {
            if (h != null && (h.getContinentId() > 0 || h.isIsland()) && h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                h.setBiome(GameConstants.BIOME_TUNDRA);
                h.setLocalWeather(getRandomWeather(GameConstants.BIOME_TUNDRA));
            }
        }
        boolean hasIceField = false;
        for (Hex h : this.hexes) {
            if (h != null && h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                hasIceField = true;
                break;
            }
        }
        if (!hasIceField) {
            Hex polar = findFurthestPolarHex();
            if (polar != null) {
                polar.setBiome(GameConstants.BIOME_ICE_FIELD);
                polar.setLocalWeather(getRandomWeather(GameConstants.BIOME_ICE_FIELD));
            }
        }
        rebuildContinentsFromHexes();
        generateGeographicFeatures();
        notifyStep(14, "Generate Downhill River Networks");

        // Step 15: Spawn player dynasty and distribute rival dynasties
        if (seedDynasties) {
            continentHexes.clear();
            for (Hex h : this.hexes) {
                if (h != null && h.getContinentId() > 0 && isLandHex(h) && !h.isIsland()) {
                    continentHexes.computeIfAbsent(h.getContinentId(), k -> new HashSet<>()).add(h);
                }
            }
            if (continentHexes.isEmpty()) {
                for (Hex h : this.hexes) {
                    if (h != null && isLandHex(h)) {
                        continentHexes.computeIfAbsent(1, k -> new HashSet<>()).add(h);
                    }
                }
            }
            if (!this.spectatorMode) {
                spawnPlayerOnRandomContinent(startBiome, startColony, continentHexes);
            }
            distributeRivalDynastiesAcrossContinents(continentHexes);
            rebuildColonizedHexIndexIfNeeded();
            notifyStep(15, "Spawn Dynasties");
        } else {
            notifyStep(15, "Generate World Complete");
        }
    }

    private void generateArchipelagosAndIslands() {
        if (this.worldWidth <= 0 || this.worldHeight <= 0) {
            return;
        }

        Map<Hex, Integer> continentDist = new HashMap<>();
        ArrayDeque<Hex> queue = new ArrayDeque<>();
        for (Hex h : this.hexes) {
            if (h.getContinentId() != 0) {
                continentDist.put(h, 0);
                queue.add(h);
            }
        }
        while (!queue.isEmpty()) {
            Hex cur = queue.poll();
            int d = continentDist.get(cur);
            for (Hex n : cur.getAdjacentNeighbors()) {
                if (n != null && !continentDist.containsKey(n)) {
                    continentDist.put(n, d + 1);
                    queue.add(n);
                }
            }
        }

        double scale = Math.max(0.25, this.worldHeight / 64.0);
        int maxLat = Math.max(2, this.worldHeight / 2 - 3);

        List<Hex> deepOceanHexes = new ArrayList<>();
        int deepMinDist = Math.max(2, (int) Math.round(5 * scale));
        for (Hex h : this.hexes) {
            if (h != null && isOceanHex(h) && h.getContinentId() == 0) {
                int d = continentDist.getOrDefault(h, 0);
                if (d >= deepMinDist && Math.abs(h.getR()) <= maxLat) {
                    deepOceanHexes.add(h);
                }
            }
        }
        if (deepOceanHexes.isEmpty()) {
            for (Hex h : this.hexes) {
                if (h != null && isOceanHex(h) && h.getContinentId() == 0) {
                    int d = continentDist.getOrDefault(h, 0);
                    if (d >= 2 && Math.abs(h.getR()) <= maxLat) {
                        deepOceanHexes.add(h);
                    }
                }
            }
        }

        for (FaultLine fault : this.faultLines) {
            if (fault == null || fault.getType() == FaultLine.FaultType.TRANSFORM) continue;
            List<Hex> oceanicBoundary = new ArrayList<>();
            for (Hex h : fault.getHexesA()) {
                if (h != null && isOceanHex(h) && h.getContinentId() == 0 && Math.abs(h.getR()) <= maxLat) {
                    if (continentDist.getOrDefault(h, 0) >= 3) {
                        oceanicBoundary.add(h);
                    }
                }
            }
            if (oceanicBoundary.size() >= 3) {
                int chainLength = Math.min(oceanicBoundary.size(), Math.max(3, (int) Math.round(6 * scale)));
                Collections.shuffle(oceanicBoundary, GameRandom.getShuffleRandom());
                for (int i = 0; i < chainLength; i++) {
                    Hex vHex = oceanicBoundary.get(i);
                    if (!isOceanHex(vHex) || vHex.getContinentId() != 0) continue;
                    if (i == 0 || GameRandom.nextInt(100) < 30) {
                        vHex.setBiome(GameConstants.BIOME_PEAK);
                        vHex.setElevation(8 + GameRandom.nextInt(2));
                    } else if (GameRandom.nextInt(100) < 25) {
                        vHex.setBiome(GameConstants.BIOME_MOUNTAIN);
                        vHex.setElevation(7);
                    } else {
                        vHex.setBiome(GameConstants.BIOME_VOLCANIC);
                        vHex.setElevation(6 + GameRandom.nextInt(3));
                    }
                    vHex.setLocalWeather(getRandomWeather(vHex.getBiome()));
                    vHex.setIsland(true);
                    vHex.setContinentId(0);
                }
            }
        }

        int numHotspots = Math.max(1, (int) Math.round(3 * scale));
        for (int hs = 0; hs < numHotspots; hs++) {
            if (deepOceanHexes.isEmpty()) break;
            Hex seed = deepOceanHexes.get(GameRandom.nextInt(deepOceanHexes.size()));
            if (!isOceanHex(seed) || seed.getContinentId() != 0) continue;

            TectonicPlate plate = getPlateById(seed.getPlateId());
            double angle = (plate != null) ? plate.getDriftAngle() : (GameRandom.nextDouble() * 2.0 * Math.PI);
            int dir = (int) Math.round(angle / (Math.PI / 3.0)) % 6;
            if (dir < 0) dir += 6;

            int hotspotChainLen = Math.max(2, (int) Math.round((3 + GameRandom.nextInt(4)) * scale));
            Hex cur = seed;
            for (int step = 0; step < hotspotChainLen && cur != null; step++) {
                if (isOceanHex(cur) && cur.getContinentId() == 0 && Math.abs(cur.getR()) <= maxLat) {
                    if (step == 0) {
                        cur.setBiome(GameConstants.BIOME_VOLCANIC);
                        cur.setElevation(8 + GameRandom.nextInt(2));
                    } else if (step == 1 && GameRandom.nextInt(100) < 40) {
                        cur.setBiome(GameConstants.BIOME_PEAK);
                        cur.setElevation(7 + GameRandom.nextInt(2));
                    } else {
                        cur.setBiome(GameConstants.BIOME_VOLCANIC);
                        cur.setElevation(6 + GameRandom.nextInt(2));
                    }
                    cur.setLocalWeather(getRandomWeather(cur.getBiome()));
                    cur.setIsland(true);
                    cur.setContinentId(0);
                }
                cur = cur.getNeighbor(dir);
            }
        }

        int numDeepArchipelagos = Math.max(2, (int) Math.round((12 + GameRandom.nextInt(6)) * scale));
        for (int a = 0; a < numDeepArchipelagos; a++) {
            if (deepOceanHexes.isEmpty()) break;
            Hex seed = deepOceanHexes.get(GameRandom.nextInt(deepOceanHexes.size()));
            if (!isOceanHex(seed) || seed.getContinentId() != 0) {
                continue;
            }
            int clusterTarget = Math.max(2, (int) Math.round((8 + GameRandom.nextInt(9)) * scale));
            List<Hex> cluster = new ArrayList<>();
            cluster.add(seed);
            assignIslandLandHex(seed, this.worldHeight / 2);

            while (cluster.size() < clusterTarget) {
                List<Hex> candidates = new ArrayList<>();
                List<Integer> touchingCounts = new ArrayList<>();
                for (Hex base : cluster) {
                    for (Hex n : base.getAdjacentNeighbors()) {
                        if (n != null && isOceanHex(n) && n.getContinentId() == 0 && !cluster.contains(n)) {
                            int touches = 0;
                            for (Hex nn : n.getAdjacentNeighbors()) {
                                if (nn != null && cluster.contains(nn)) {
                                    touches++;
                                }
                            }
                            candidates.add(n);
                            touchingCounts.add(touches);
                        }
                    }
                }
                if (candidates.isEmpty()) break;

                Hex bestCandidate = null;
                int maxScore = -1;
                for (int i = 0; i < candidates.size(); i++) {
                    Hex cand = candidates.get(i);
                    int touches = touchingCounts.get(i);
                    int score = touches * 30 + GameRandom.nextInt(40);
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
                for (int i = 0; i < candidates.size(); i++) {
                    Hex cand = candidates.get(i);
                    int touches = touchingCounts.get(i);
                    int score = touches * 30 + GameRandom.nextInt(40);
                    if (score == maxScore) {
                        bestCandidate = cand;
                        break;
                    }
                }
                if (bestCandidate != null) {
                    assignIslandLandHex(bestCandidate, this.worldHeight / 2);
                    cluster.add(bestCandidate);
                }
            }

            int satellites = Math.max(1, (int) Math.round((1 + GameRandom.nextInt(3)) * scale));
            for (int s = 0; s < satellites; s++) {
                Hex satBase = cluster.get(GameRandom.nextInt(cluster.size()));
                for (Hex n1 : satBase.getAdjacentNeighbors()) {
                    if (n1 == null) continue;
                    for (Hex n2 : n1.getAdjacentNeighbors()) {
                        if (n2 != null && isOceanHex(n2) && n2.getContinentId() == 0 && !cluster.contains(n2)) {
                            assignIslandLandHex(n2, this.worldHeight / 2);
                            cluster.add(n2);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        int numArchipelagos = Math.max(1, (int) Math.round((8 + GameRandom.nextInt(5)) * scale));
        for (int a = 0; a < numArchipelagos; a++) {
            int r = -maxLat + GameRandom.nextInt(2 * maxLat + 1);
            int col = -this.worldWidth / 2 + GameRandom.nextInt(this.worldWidth);
            int q = col - (r >> 1);
            Hex seed = getHexAt(q, r);
            if (seed == null || !isOceanHex(seed) || seed.getContinentId() != 0) {
                continue;
            }
            int clusterTarget = Math.max(2, (int) Math.round((4 + GameRandom.nextInt(5)) * scale));
            List<Hex> cluster = new ArrayList<>();
            cluster.add(seed);
            assignIslandLandHex(seed, this.worldHeight / 2);

            while (cluster.size() < clusterTarget) {
                Hex base = cluster.get(GameRandom.nextInt(cluster.size()));
                List<Hex> candidates = new ArrayList<>();
                for (Hex n : base.getAdjacentNeighbors()) {
                    if (n != null && isOceanHex(n) && n.getContinentId() == 0 && !cluster.contains(n)) {
                        candidates.add(n);
                    }
                }
                if (candidates.isEmpty()) {
                    break;
                }
                Hex added = candidates.get(GameRandom.nextInt(candidates.size()));
                assignIslandLandHex(added, this.worldHeight / 2);
                cluster.add(added);
            }
        }

        int numRandomArchipelagos = Math.max(2, (int) Math.round((6 + GameRandom.nextInt(5)) * scale));
        for (int i = 0; i < numRandomArchipelagos; i++) {
            int r = -maxLat + GameRandom.nextInt(2 * maxLat + 1);
            int col = -this.worldWidth / 2 + GameRandom.nextInt(this.worldWidth);
            int q = col - (r >> 1);
            Hex hex = getHexAt(q, r);
            if (hex != null && isOceanHex(hex) && hex.getContinentId() == 0) {
                assignIslandLandHex(hex, this.worldHeight / 2);

                int satellites = 1 + GameRandom.nextInt(3);
                for (int s = 0; s < satellites; s++) {
                    Hex satBase = hex.getNeighbor(GameRandom.nextInt(6));
                    if (satBase != null) {
                        Hex sat = satBase.getNeighbor(GameRandom.nextInt(6));
                        if (sat != null && isOceanHex(sat) && sat.getContinentId() == 0 && Math.abs(sat.getR()) <= maxLat) {
                            assignIslandLandHex(sat, this.worldHeight / 2);
                        }
                    }
                }
            }
        }

        int numSolitaryIslands = Math.max(2, (int) Math.round((4 + GameRandom.nextInt(4)) * scale));
        for (int i = 0; i < numSolitaryIslands; i++) {
            int r = -maxLat + GameRandom.nextInt(2 * maxLat + 1);
            int col = -this.worldWidth / 2 + GameRandom.nextInt(this.worldWidth);
            int q = col - (r >> 1);
            Hex hex = getHexAt(q, r);
            if (hex != null && isOceanHex(hex) && hex.getContinentId() == 0) {
                assignIslandLandHex(hex, this.worldHeight / 2);
            }
        }
        for (Hex h : this.hexes) {
            if (h != null && h.isIsland()) {
                for (Hex n : h.getAdjacentNeighbors()) {
                    if (n != null && isOceanHex(n) && !n.isIsland()) {
                        n.setElevation(Math.max(n.getElevation(), 2));
                        n.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
                        n.setLocalWeather(getRandomWeather(GameConstants.BIOME_SHALLOW_OCEAN));
                        n.setWaterDistance(1);
                    }
                }
            }
        }
        for (Hex h : this.hexes) {
            if (h != null && isOceanHex(h) && h.getElevation() == 0) {
                boolean nearShallow = false;
                for (Hex n : h.getAdjacentNeighbors()) {
                    if (n != null && n.getBiome() == GameConstants.BIOME_SHALLOW_OCEAN) {
                        nearShallow = true;
                        break;
                    }
                }
                if (nearShallow) {
                    h.setElevation(1);
                    h.setBiome(GameConstants.BIOME_OCEAN);
                    h.setLocalWeather(getRandomWeather(GameConstants.BIOME_OCEAN));
                    h.setWaterDistance(2);
                }
            }
        }
    }

    private void assignIslandLandHex(Hex hex, int worldRadius) {
        if (hex == null) return;
        hex.setIsland(true);
        hex.setContinentId(0);
        hex.setCoastal(true);
        hex.setWaterDistance(1);
        int col = hex.getQ() + (hex.getR() >> 1);
        double wave = 0.5 * Math.sin(col * 0.45) + 0.3 * Math.cos(hex.getR() * 0.40);
        int jitter = GameRandom.nextInt(3) - 1;
        int elev = 3 + (GameRandom.nextInt(100) < 50 ? 1 : 0) + (int) Math.round(wave) + jitter;
        if (hex.getElevation() >= 6) {
            elev = Math.max(elev, hex.getElevation());
        }
        hex.setElevation(Math.max(3, Math.min(8, elev)));
        Biome b = pickBiomeForLandHex(hex, worldRadius);
        hex.setBiome(b);
        hex.setLocalWeather(getRandomWeather(b));
    }

    private boolean isOceanHex(Hex h) {
        return h != null && (h.getBiome() == GameConstants.BIOME_OCEAN
                || h.getBiome() == GameConstants.BIOME_DEEP_OCEAN
                || h.getBiome() == GameConstants.BIOME_SHALLOW_OCEAN);
    }

    private static final class RiverEdgeKey {
        final Hex h1;
        final Hex h2;

        RiverEdgeKey(Hex a, Hex b) {
            if (a.getQ() < b.getQ() || (a.getQ() == b.getQ() && a.getR() < b.getR())) {
                this.h1 = a;
                this.h2 = b;
            } else {
                this.h1 = b;
                this.h2 = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RiverEdgeKey)) return false;
            RiverEdgeKey other = (RiverEdgeKey) o;
            return this.h1 == other.h1 && this.h2 == other.h2;
        }

        @Override
        public int hashCode() {
            int result = 31 * h1.getQ() + h1.getR();
            result = 31 * result + h2.getQ();
            result = 31 * result + h2.getR();
            return result;
        }
    }

    private static final class DijkstraNode implements Comparable<DijkstraNode> {
        final RiverEdgeKey edge;
        final double dist;

        DijkstraNode(RiverEdgeKey edge, double dist) {
            this.edge = edge;
            this.dist = dist;
        }

        @Override
        public int compareTo(DijkstraNode o) {
            return Double.compare(this.dist, o.dist);
        }
    }

    private List<RiverEdgeKey> getAdjacentRiverEdges(RiverEdgeKey edge) {
        List<RiverEdgeKey> result = new ArrayList<>();
        Hex u = edge.h1;
        Hex v = edge.h2;
        if (u == null || v == null) return result;
        if (u.getBiome() == GameConstants.BIOME_LAKE || v.getBiome() == GameConstants.BIOME_LAKE) return result;

        for (Hex w : u.getAdjacentNeighbors()) {
            if (w == null || w == v) continue;
            if (w.getBiome() == GameConstants.BIOME_LAKE) continue;
            if (v.directionTo(w) >= 0) {
                if (!(isOceanHex(u) && isOceanHex(w))) {
                    result.add(new RiverEdgeKey(u, w));
                }
                if (!(isOceanHex(v) && isOceanHex(w))) {
                    result.add(new RiverEdgeKey(v, w));
                }
            }
        }
        return result;
    }

    private void markRiverEdge(RiverEdgeKey edge) {
        if (edge == null || edge.h1 == null || edge.h2 == null) return;
        if (edge.h1.getBiome() == GameConstants.BIOME_LAKE || edge.h2.getBiome() == GameConstants.BIOME_LAKE) return;
        if (edge.h1.getBiome() == GameConstants.BIOME_ICE_FIELD || edge.h2.getBiome() == GameConstants.BIOME_ICE_FIELD) return;
        if (isOceanHex(edge.h1) && isOceanHex(edge.h2)) return;
        int d1 = edge.h1.directionTo(edge.h2);
        int d2 = edge.h2.directionTo(edge.h1);
        if (d1 < 0 || d2 < 0) return;
        if (wouldSurroundHex(edge.h1, d1) || wouldSurroundHex(edge.h2, d2)) {
            return;
        }
        edge.h1.setRiverEdge(d1, true);
        edge.h2.setRiverEdge(d2, true);
    }

    private void forceMarkRiverEdge(RiverEdgeKey edge) {
        if (edge == null || edge.h1 == null || edge.h2 == null) return;
        if (edge.h1.getBiome() == GameConstants.BIOME_LAKE || edge.h2.getBiome() == GameConstants.BIOME_LAKE) return;
        if (edge.h1.getBiome() == GameConstants.BIOME_ICE_FIELD || edge.h2.getBiome() == GameConstants.BIOME_ICE_FIELD) return;
        if (isOceanHex(edge.h1) && isOceanHex(edge.h2)) return;
        int d1 = edge.h1.directionTo(edge.h2);
        int d2 = edge.h2.directionTo(edge.h1);
        edge.h1.setRiverEdge(d1, true);
        edge.h2.setRiverEdge(d2, true);
    }

    private double edgeElevation(RiverEdgeKey edge) {
        if (edge == null || edge.h1 == null || edge.h2 == null) return 0;
        return Math.min(edge.h1.getElevation(), edge.h2.getElevation());
    }

    private boolean wouldSurroundHex(Hex hex, int newRiverDir) {
        if (hex == null) return false;
        int riverCount = hex.getRiverEdgeCount();
        if (!hex.hasRiverEdge(newRiverDir)) {
            riverCount++;
        }
        return riverCount > 3;
    }

    private List<RiverEdgeKey> findShortestRiverPathToOcean(List<RiverEdgeKey> startEdges, int maxDepth) {
        return findShortestRiverPathToOcean(startEdges, maxDepth, true, null);
    }

    private List<RiverEdgeKey> findShortestRiverPathToOcean(List<RiverEdgeKey> startEdges, int maxDepth, boolean avoidSurround) {
        return findShortestRiverPathToOcean(startEdges, maxDepth, avoidSurround, null);
    }

    private List<RiverEdgeKey> findShortestRiverPathToOcean(List<RiverEdgeKey> startEdges, int maxDepth, boolean avoidSurround, Set<RiverEdgeKey> existingRivers) {
        if (startEdges == null || startEdges.isEmpty()) return Collections.emptyList();

        PriorityQueue<DijkstraNode> pq = new PriorityQueue<>();
        Map<RiverEdgeKey, Double> distMap = new HashMap<>();
        Map<RiverEdgeKey, RiverEdgeKey> parentMap = new HashMap<>();
        Map<RiverEdgeKey, Integer> stepMap = new HashMap<>();

        for (RiverEdgeKey start : startEdges) {
            if (start.h1.getBiome() == GameConstants.BIOME_ICE_FIELD || start.h2.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                continue;
            }
            int d1 = start.h1.directionTo(start.h2);
            int d2 = start.h2.directionTo(start.h1);
            if (d1 < 0 || d2 < 0) continue;
            int c1 = start.h1.getRiverEdgeCount() + (start.h1.hasRiverEdge(d1) ? 0 : 1);
            int c2 = start.h2.getRiverEdgeCount() + (start.h2.hasRiverEdge(d2) ? 0 : 1);
            int edgeCap = avoidSurround ? 3 : 4;
            if (c1 > edgeCap || c2 > edgeCap) continue;
            double initialCost = 1.0 + (start.h1.getWaterDistance() + start.h2.getWaterDistance()) * 0.1;
            distMap.put(start, initialCost);
            stepMap.put(start, 1);
            pq.add(new DijkstraNode(start, initialCost));
        }

        RiverEdgeKey goalEdge = null;
        while (!pq.isEmpty()) {
            DijkstraNode curr = pq.poll();
            if (curr.dist > distMap.getOrDefault(curr.edge, Double.MAX_VALUE)) {
                continue;
            }

            int currentSteps = stepMap.getOrDefault(curr.edge, 1);
            boolean reachesWater = isOceanHex(curr.edge.h1) || isOceanHex(curr.edge.h2);
            boolean reachesLake = currentSteps > 1 && (curr.edge.h1.getBiome() == GameConstants.BIOME_LAKE || curr.edge.h2.getBiome() == GameConstants.BIOME_LAKE);
            boolean hitsExisting = false;
            if (currentSteps > 1) {
                if (existingRivers != null && existingRivers.contains(curr.edge)) {
                    hitsExisting = true;
                } else {
                    int cd = curr.edge.h1.directionTo(curr.edge.h2);
                    if (cd >= 0 && curr.edge.h1.hasRiverEdge(cd)) {
                        hitsExisting = true;
                    }
                }
            }
            if (reachesWater || reachesLake || hitsExisting) {
                goalEdge = curr.edge;
                break;
            }
            if (maxDepth > 0 && currentSteps >= maxDepth) {
                continue;
            }

            double currElev = edgeElevation(curr.edge);
            for (RiverEdgeKey next : getAdjacentRiverEdges(curr.edge)) {
                if (next.h1.getBiome() == GameConstants.BIOME_ICE_FIELD || next.h2.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                    continue;
                }
                int d1 = next.h1.directionTo(next.h2);
                int d2 = next.h2.directionTo(next.h1);
                if (d1 < 0 || d2 < 0) continue;
                double nextElev = edgeElevation(next);
                double uphillPenalty = 0.0;
                if (nextElev > currElev) {
                    uphillPenalty = (nextElev - currElev) * 1000.0;
                }
                int c1 = next.h1.getRiverEdgeCount() + (next.h1.hasRiverEdge(d1) ? 0 : 1);
                int c2 = next.h2.getRiverEdgeCount() + (next.h2.hasRiverEdge(d2) ? 0 : 1);
                RiverEdgeKey trace = curr.edge;
                while (trace != null) {
                    int td1 = trace.h1.directionTo(trace.h2);
                    if (td1 >= 0 && !trace.h1.hasRiverEdge(td1)) {
                        if (trace.h1 == next.h1 || trace.h2 == next.h1) c1++;
                        if (trace.h1 == next.h2 || trace.h2 == next.h2) c2++;
                    }
                    trace = parentMap.get(trace);
                }
                int edgeCap = avoidSurround ? 3 : 4;
                if (c1 > edgeCap || c2 > edgeCap) continue;
                double edgeWeight = 1.0 + (next.h1.getWaterDistance() + next.h2.getWaterDistance()) * 0.05
                        + (next.h1.getRiverEdgeCount() + next.h2.getRiverEdgeCount()) * 1.5
                        + uphillPenalty
                        + GameRandom.nextDouble() * 0.02;
                double newDist = curr.dist + edgeWeight;
                if (newDist < distMap.getOrDefault(next, Double.MAX_VALUE)) {
                    distMap.put(next, newDist);
                    parentMap.put(next, curr.edge);
                    stepMap.put(next, currentSteps + 1);
                    pq.add(new DijkstraNode(next, newDist));
                }
            }
        }

        if (goalEdge == null) {
            return Collections.emptyList();
        }

        List<RiverEdgeKey> path = new ArrayList<>();
        RiverEdgeKey trace = goalEdge;
        while (trace != null) {
            path.add(trace);
            trace = parentMap.get(trace);
        }
        Collections.reverse(path);
        return path;
    }

    private void generateAllDownhillRivers() {
        if (this.hexes == null) return;
        Set<Hex> visitedLakes = new HashSet<>();
        List<Set<Hex>> lakeComponents = new ArrayList<>();
        for (Hex h : this.hexes) {
            if (h != null && h.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(h)) {
                Set<Hex> comp = new HashSet<>();
                ArrayDeque<Hex> q = new ArrayDeque<>();
                q.add(h);
                comp.add(h);
                while (!q.isEmpty()) {
                    Hex cur = q.poll();
                    for (Hex n : cur.getAdjacentNeighbors()) {
                        if (n != null && n.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(n)) {
                            comp.add(n);
                            q.add(n);
                        }
                    }
                }
                lakeComponents.add(comp);
            }
        }

        List<Hex> peakSources = new ArrayList<>();
        List<Hex> highMountainSources = new ArrayList<>();
        for (Hex h : this.hexes) {
            if (h != null && h.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                if (h.getBiome() == GameConstants.BIOME_PEAK) {
                    peakSources.add(h);
                } else if (h.getElevation() >= 8) {
                    highMountainSources.add(h);
                }
            }
        }
        Collections.shuffle(peakSources, GameRandom.getShuffleRandom());
        Collections.shuffle(highMountainSources, GameRandom.getShuffleRandom());
        peakSources.addAll(highMountainSources);

        Set<RiverEdgeKey> placedRivers = new HashSet<>();
        for (Set<Hex> comp : lakeComponents) {
            generateRiversForLakeComponent(comp, placedRivers);
        }

        int targetPeakRivers = Math.min(peakSources.size(), Math.max(5, this.worldHeight / 3));
        int peakRiversPlaced = 0;
        for (Hex peak : peakSources) {
            if (peakRiversPlaced >= targetPeakRivers) {
                break;
            }
            if (peak.getRiverEdgeCount() > 0) {
                continue;
            }
            if (generateRiversForPeak(peak, placedRivers)) {
                peakRiversPlaced++;
            }
        }
    }

    private boolean generateRiversForPeak(Hex peak, Set<RiverEdgeKey> placedRivers) {
        if (peak == null || peak.getBiome() == GameConstants.BIOME_ICE_FIELD) return false;
        List<RiverEdgeKey> candidateStartEdges = new ArrayList<>();
        for (Hex n : peak.getAdjacentNeighbors()) {
            if (n != null && peak.directionTo(n) >= 0) {
                if (n.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                    if (!isOceanHex(n)) {
                        candidateStartEdges.add(new RiverEdgeKey(peak, n));
                    }
                }
            }
        }
        if (candidateStartEdges.isEmpty()) return false;
        List<RiverEdgeKey> mainPath = findShortestRiverPathToOcean(candidateStartEdges, 0, true, placedRivers);
        if (mainPath.isEmpty()) {
            mainPath = findShortestRiverPathToOcean(candidateStartEdges, 0, false, placedRivers);
        }
        if (mainPath.isEmpty()) return false;
        Set<RiverEdgeKey> markedRivers = new HashSet<>();
        double runningElev = peak.getElevation();
        for (RiverEdgeKey edge : mainPath) {
            forceMarkRiverEdge(edge);
            if (edge.h1 != peak && edge.h1.getElevation() > runningElev) {
                edge.h1.setElevation((int) Math.round(runningElev));
            }
            if (edge.h2 != peak && edge.h2.getElevation() > runningElev) {
                edge.h2.setElevation((int) Math.round(runningElev));
            }
            runningElev = Math.min(runningElev, edgeElevation(edge));
            markedRivers.add(edge);
            if (placedRivers != null) {
                placedRivers.add(edge);
            }
        }
        addForksToRiver(mainPath, markedRivers, placedRivers);
        return true;
    }

    private void checkEnclosedHexes() {
        if (this.hexes == null) return;
        boolean changed = true;
        int passes = 0;
        while (changed && passes++ < 10) {
            changed = false;
            for (Hex h : this.hexes) {
                if (h == null) continue;
                if (h.getRiverEdgeCount() == 6) {
                    h.setBiome(GameConstants.BIOME_LAKE);
                    h.setLocalWeather(getRandomWeather(h.getBiome()));
                    for (int dir = 0; dir < 6; dir++) {
                        h.setRiverEdge(dir, false);
                        Hex n = h.getNeighbor(dir);
                        if (n != null) {
                            n.setRiverEdge((dir + 3) % 6, false);
                        }
                    }
                    changed = true;
                    traceOutflowRiver(h);
                }
            }
        }
    }

    private void traceOutflowRiver(Hex lakeHex) {
        if (lakeHex == null) return;
        List<RiverEdgeKey> candidateStarts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Hex n1 = lakeHex.getNeighbor(i);
            Hex n2 = lakeHex.getNeighbor((i + 1) % 6);
            if (n1 != null && n2 != null && n1.directionTo(n2) >= 0) {
                if (n1.getBiome() != GameConstants.BIOME_ICE_FIELD && n2.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                    if (!isOceanHex(n1) || !isOceanHex(n2)) {
                        candidateStarts.add(new RiverEdgeKey(n1, n2));
                    }
                }
            }
        }
        if (!candidateStarts.isEmpty()) {
            List<RiverEdgeKey> path = findShortestRiverPathToOcean(candidateStarts, 0, true, null);
            double runningElev = !path.isEmpty() ? edgeElevation(path.get(0)) : lakeHex.getElevation();
            for (RiverEdgeKey e : path) {
                forceMarkRiverEdge(e);
                if (e.h1.getElevation() > runningElev) {
                    e.h1.setElevation((int) Math.round(runningElev));
                }
                if (e.h2.getElevation() > runningElev) {
                    e.h2.setElevation((int) Math.round(runningElev));
                }
                runningElev = Math.min(runningElev, edgeElevation(e));
            }
        }
    }

    private void ensureAllLakesHaveRivers() {
        if (this.hexes == null) return;
        Set<Hex> visitedLakes = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h != null && h.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(h)) {
                Set<Hex> comp = new HashSet<>();
                ArrayDeque<Hex> q = new ArrayDeque<>();
                q.add(h);
                comp.add(h);
                while (!q.isEmpty()) {
                    Hex cur = q.poll();
                    for (Hex n : cur.getAdjacentNeighbors()) {
                        if (n != null && n.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(n)) {
                            comp.add(n);
                            q.add(n);
                        }
                    }
                }
                boolean hasRiver = false;
                for (Hex lakeHex : comp) {
                    for (int dir = 0; dir < 6; dir++) {
                        Hex n1 = lakeHex.getNeighbor(dir);
                        Hex n2 = lakeHex.getNeighbor((dir + 1) % 6);
                        if (n1 != null && n2 != null && n1.directionTo(n2) >= 0) {
                            if (n1.hasRiverEdge(n1.directionTo(n2)) || n2.hasRiverEdge(n2.directionTo(n1))) {
                                hasRiver = true;
                                break;
                            }
                        }
                    }
                    if (hasRiver) break;
                }
                if (!hasRiver) {
                    generateRiversForLakeComponent(comp, null);
                }
            }
        }
    }

    private boolean generateRiversForLakeComponent(Set<Hex> lakeComp) {
        return generateRiversForLakeComponent(lakeComp, null);
    }

    private boolean generateRiversForLakeComponent(Set<Hex> lakeComp, Set<RiverEdgeKey> placedRivers) {
        if (lakeComp == null || lakeComp.isEmpty()) return false;

        List<RiverEdgeKey> candidateStartEdges = new ArrayList<>();
        for (Hex lakeHex : lakeComp) {
            Hex[] circle = new Hex[] {
                lakeHex.getSouthEast(),
                lakeHex.getSouth(),
                lakeHex.getSouthWest(),
                lakeHex.getNorthWest(),
                lakeHex.getNorth(),
                lakeHex.getNorthEast()
            };
            for (int i = 0; i < 6; i++) {
                Hex n1 = circle[i];
                Hex n2 = circle[(i + 1) % 6];
                if (n1 != null && n2 != null && n1.directionTo(n2) >= 0) {
                    if (n1.getBiome() != GameConstants.BIOME_LAKE && n2.getBiome() != GameConstants.BIOME_LAKE
                            && n1.getBiome() != GameConstants.BIOME_ICE_FIELD && n2.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                        if (!isOceanHex(n1) || !isOceanHex(n2)) {
                            candidateStartEdges.add(new RiverEdgeKey(n1, n2));
                        }
                    }
                }
            }
        }

        if (candidateStartEdges.isEmpty()) {
            return false;
        }

        List<RiverEdgeKey> mainPath = findShortestRiverPathToOcean(candidateStartEdges, 0, true, placedRivers);
        if (mainPath.isEmpty()) {
            mainPath = findShortestRiverPathToOcean(candidateStartEdges, 0, false, placedRivers);
        }
        if (mainPath.isEmpty()) {
            if (!candidateStartEdges.isEmpty()) {
                forceMarkRiverEdge(candidateStartEdges.get(0));
                return true;
            }
            return false;
        }

        Set<RiverEdgeKey> markedRivers = new HashSet<>();
        double runningElev = edgeElevation(mainPath.get(0));
        for (RiverEdgeKey edge : mainPath) {
            forceMarkRiverEdge(edge);
            if (edge.h1.getElevation() > runningElev) {
                edge.h1.setElevation((int) Math.round(runningElev));
            }
            if (edge.h2.getElevation() > runningElev) {
                edge.h2.setElevation((int) Math.round(runningElev));
            }
            runningElev = Math.min(runningElev, edgeElevation(edge));
            markedRivers.add(edge);
            if (placedRivers != null) {
                placedRivers.add(edge);
            }
        }

        addForksToRiver(mainPath, markedRivers, placedRivers);
        return true;
    }

    private void addForksToRiver(List<RiverEdgeKey> mainPath, Set<RiverEdgeKey> markedRivers, Set<RiverEdgeKey> placedRivers) {
        if (mainPath == null || mainPath.size() < 3 || placedRivers == null) {
            return;
        }
        int desiredForks = Math.min(3, Math.max(1, mainPath.size() / 3));
        List<Integer> branchIndices = new ArrayList<>();
        for (int i = 0; i < mainPath.size() - 1; i++) {
            branchIndices.add(i);
        }
        Collections.shuffle(branchIndices, GameRandom.getShuffleRandom());

        int forksPlaced = 0;
        for (int branchIdx : branchIndices) {
            if (forksPlaced >= desiredForks) {
                break;
            }
            RiverEdgeKey branchEdge = mainPath.get(branchIdx);
            double branchElev = edgeElevation(branchEdge);
            List<RiverEdgeKey> forkCandidates = new ArrayList<>();
            for (RiverEdgeKey adj : getAdjacentRiverEdges(branchEdge)) {
                if (!markedRivers.contains(adj) && edgeElevation(adj) <= branchElev + 0.5) {
                    if (adj.h1.getBiome() != GameConstants.BIOME_ICE_FIELD && adj.h2.getBiome() != GameConstants.BIOME_ICE_FIELD) {
                        forkCandidates.add(adj);
                    }
                }
            }
            if (forkCandidates.isEmpty()) {
                continue;
            }
            Collections.shuffle(forkCandidates, GameRandom.getShuffleRandom());
            for (RiverEdgeKey forkStart : forkCandidates) {
                List<RiverEdgeKey> forkPath = findShortestRiverPathToOcean(Collections.singletonList(forkStart), 25, true, placedRivers);
                if (forkPath.isEmpty()) {
                    forkPath = findShortestRiverPathToOcean(Collections.singletonList(forkStart), 25, false, placedRivers);
                }
                if (forkPath.isEmpty()) {
                    continue;
                }
                boolean canPlaceFork = true;
                Map<Hex, Integer> pending = new HashMap<>();
                for (RiverEdgeKey edge : forkPath) {
                    int d1 = edge.h1.directionTo(edge.h2);
                    int d2 = edge.h2.directionTo(edge.h1);
                    if (d1 < 0 || d2 < 0) {
                        canPlaceFork = false;
                        break;
                    }
                    int c1 = edge.h1.getRiverEdgeCount() + pending.getOrDefault(edge.h1, 0) + (edge.h1.hasRiverEdge(d1) ? 0 : 1);
                    int c2 = edge.h2.getRiverEdgeCount() + pending.getOrDefault(edge.h2, 0) + (edge.h2.hasRiverEdge(d2) ? 0 : 1);
                    if (c1 > 3 || c2 > 3) {
                        canPlaceFork = false;
                        break;
                    }
                    if (!edge.h1.hasRiverEdge(d1)) {
                        pending.put(edge.h1, pending.getOrDefault(edge.h1, 0) + 1);
                    }
                    if (!edge.h2.hasRiverEdge(d2)) {
                        pending.put(edge.h2, pending.getOrDefault(edge.h2, 0) + 1);
                    }
                }
                if (canPlaceFork) {
                    double forkElev = branchElev;
                    for (RiverEdgeKey edge : forkPath) {
                        forceMarkRiverEdge(edge);
                        if (edge.h1.getElevation() > forkElev) {
                            edge.h1.setElevation((int) Math.round(forkElev));
                        }
                        if (edge.h2.getElevation() > forkElev) {
                            edge.h2.setElevation((int) Math.round(forkElev));
                        }
                        forkElev = Math.min(forkElev, edgeElevation(edge));
                        markedRivers.add(edge);
                        placedRivers.add(edge);
                    }
                    forksPlaced++;
                    break;
                }
            }
        }
    }

    private boolean generateRiversForLake(Hex lakeHex) {
        if (lakeHex == null) return false;
        return generateRiversForLakeComponent(Collections.singleton(lakeHex));
    }

    private List<List<Hex>> convertEnclosedOceansToLakes(Map<Integer, Set<Hex>> continentHexes, List<Hex> allLand) {
        List<List<Hex>> enclosedComponents = new ArrayList<>();
        if (this.hexes == null) return enclosedComponents;
        Set<Hex> visited = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null || !isOceanHex(h) || visited.contains(h)) {
                continue;
            }
            List<Hex> component = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            queue.add(h);
            visited.add(h);

            while (!queue.isEmpty()) {
                Hex curr = queue.poll();
                component.add(curr);
                for (Hex n : curr.getAdjacentNeighbors()) {
                    if (n != null && isOceanHex(n) && !visited.contains(n)) {
                        visited.add(n);
                        queue.add(n);
                    }
                }
            }

            if (component.size() < 10) {
                Map<Integer, Integer> continentVotes = new HashMap<>();
                for (Hex oceanHex : component) {
                    for (Hex n : oceanHex.getAdjacentNeighbors()) {
                        if (n != null && n.getContinentId() > 0) {
                            continentVotes.put(n.getContinentId(), continentVotes.getOrDefault(n.getContinentId(), 0) + 1);
                        }
                    }
                }
                int dominantCid = 0;
                int maxVotes = 0;
                for (Map.Entry<Integer, Integer> entry : continentVotes.entrySet()) {
                    if (entry.getValue() > maxVotes) {
                        maxVotes = entry.getValue();
                        dominantCid = entry.getKey();
                    }
                }
                boolean nearIce = false;
                for (Hex oceanHex : component) {
                    if (this.worldHeight > 0 && Math.abs(oceanHex.getR()) >= (this.worldHeight / 2 - 8)) {
                        nearIce = true;
                        break;
                    }
                    for (Hex n : oceanHex.getAdjacentNeighbors()) {
                        if (n != null && n.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                            nearIce = true;
                            break;
                        }
                    }
                    if (nearIce) break;
                }
                if (nearIce) {
                    for (Hex oceanHex : component) {
                        oceanHex.setBiome(GameConstants.BIOME_TUNDRA);
                        oceanHex.setLocalWeather(getRandomWeather(GameConstants.BIOME_TUNDRA));
                        oceanHex.setElevation(Math.max(3, oceanHex.getElevation()));
                    }
                    continue;
                }
                for (Hex oceanHex : component) {
                    oceanHex.setBiome(GameConstants.BIOME_LAKE);
                    oceanHex.setLocalWeather(getRandomWeather(GameConstants.BIOME_LAKE));
                    oceanHex.setIsland(false);
                    if (dominantCid > 0) {
                        oceanHex.setContinentId(dominantCid);
                        Set<Hex> cSet = continentHexes.get(dominantCid);
                        if (cSet != null) {
                            cSet.add(oceanHex);
                        }
                    }
                    if (!allLand.contains(oceanHex)) {
                        allLand.add(oceanHex);
                    }
                }
                enclosedComponents.add(component);
            }
        }
        return enclosedComponents;
    }

    private void preventSurroundedHexes() {
        if (this.hexes == null) return;
        for (Hex h : this.hexes) {
            if (h == null || !h.hasAnyRiver() || h.getBiome() == GameConstants.BIOME_LAKE) continue;
            while (h.getRiverEdgeCount() >= 4) {
                boolean removed = false;
                for (int dir = 5; dir >= 0; dir--) {
                    if (h.hasRiverEdge(dir)) {
                        Hex n = h.getNeighbor(dir);
                        if (isLakePerimeterEdge(h, n)) {
                            continue;
                        }
                        h.setRiverEdge(dir, false);
                        if (n != null) {
                            int opp = (dir + 3) % 6;
                            n.setRiverEdge(opp, false);
                        }
                        removed = true;
                        break;
                    }
                }
                if (!removed) {
                    for (int dir = 5; dir >= 0; dir--) {
                        if (h.hasRiverEdge(dir)) {
                            h.setRiverEdge(dir, false);
                            Hex n = h.getNeighbor(dir);
                            if (n != null) {
                                int opp = (dir + 3) % 6;
                                n.setRiverEdge(opp, false);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isLakePerimeterEdge(Hex h, Hex n) {
        if (h == null || n == null) return false;
        for (Hex neighbor : h.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getBiome() == GameConstants.BIOME_LAKE && n.directionTo(neighbor) >= 0) {
                return true;
            }
        }
        return false;
    }

    private void cleanupOrphanRivers() {
        if (this.hexes == null) return;
        Set<RiverEdgeKey> activeRivers = new HashSet<>();
        for (Hex h : this.hexes) {
            if (h == null) continue;
            for (int dir = 0; dir < 6; dir++) {
                if (h.hasRiverEdge(dir)) {
                    Hex n = h.getNeighbor(dir);
                    if (n != null) {
                        activeRivers.add(new RiverEdgeKey(h, n));
                    }
                }
            }
        }
        if (activeRivers.isEmpty()) return;

        boolean changed = true;
        while (changed) {
            changed = false;
            List<RiverEdgeKey> toRemove = new ArrayList<>();
            for (RiverEdgeKey edge : activeRivers) {
                Hex u = edge.h1;
                Hex v = edge.h2;
                List<Hex> mutual = new ArrayList<>(2);
                for (Hex w : u.getAdjacentNeighbors()) {
                    if (w != null && w != v && v.directionTo(w) >= 0) {
                        mutual.add(w);
                    }
                }
                boolean hasDanglingDeadEnd = false;
                for (Hex w : mutual) {
                    boolean touchesWater = isOceanHex(u) || isOceanHex(v) || isOceanHex(w)
                            || (u.getBiome() == GameConstants.BIOME_LAKE)
                            || (v.getBiome() == GameConstants.BIOME_LAKE)
                            || (w.getBiome() == GameConstants.BIOME_LAKE);
                    boolean touchesOtherRiver = activeRivers.contains(new RiverEdgeKey(u, w))
                            || activeRivers.contains(new RiverEdgeKey(v, w));
                    if (!touchesWater && !touchesOtherRiver) {
                        hasDanglingDeadEnd = true;
                        break;
                    }
                }
                if (hasDanglingDeadEnd) {
                    toRemove.add(edge);
                }
            }
            if (!toRemove.isEmpty()) {
                for (RiverEdgeKey edge : toRemove) {
                    int d1 = edge.h1.directionTo(edge.h2);
                    int d2 = edge.h2.directionTo(edge.h1);
                    if (d1 >= 0) edge.h1.setRiverEdge(d1, false);
                    if (d2 >= 0) edge.h2.setRiverEdge(d2, false);
                    activeRivers.remove(edge);
                }
                changed = true;
            }
        }

        Set<RiverEdgeKey> visited = new HashSet<>();
        for (RiverEdgeKey root : activeRivers) {
            if (visited.contains(root)) continue;
            List<RiverEdgeKey> component = new ArrayList<>();
            ArrayDeque<RiverEdgeKey> queue = new ArrayDeque<>();
            queue.add(root);
            visited.add(root);
            boolean touchesLake = false;
            boolean touchesOcean = false;

            while (!queue.isEmpty()) {
                RiverEdgeKey cur = queue.poll();
                component.add(cur);
                Hex u = cur.h1;
                Hex v = cur.h2;
                for (Hex w : u.getAdjacentNeighbors()) {
                    if (w != null && w != v && v.directionTo(w) >= 0) {
                        if (u.getBiome() == GameConstants.BIOME_LAKE || v.getBiome() == GameConstants.BIOME_LAKE || w.getBiome() == GameConstants.BIOME_LAKE) {
                            touchesLake = true;
                        }
                        if (isOceanHex(u) || isOceanHex(v) || isOceanHex(w)) {
                            touchesOcean = true;
                        }
                        RiverEdgeKey e1 = new RiverEdgeKey(u, w);
                        if (activeRivers.contains(e1) && visited.add(e1)) {
                            queue.add(e1);
                        }
                        RiverEdgeKey e2 = new RiverEdgeKey(v, w);
                        if (activeRivers.contains(e2) && visited.add(e2)) {
                            queue.add(e2);
                        }
                    }
                }
            }

            if (!touchesLake && !touchesOcean) {
                for (RiverEdgeKey edge : component) {
                    int d1 = edge.h1.directionTo(edge.h2);
                    int d2 = edge.h2.directionTo(edge.h1);
                    if (d1 >= 0) edge.h1.setRiverEdge(d1, false);
                    if (d2 >= 0) edge.h2.setRiverEdge(d2, false);
                }
            }
        }
    }

    private Biome pickBiomeForLandHex(Hex hex, int worldRadius) {
        double absLat = Math.abs((double) hex.getR()) / Math.max(1, worldRadius);
        boolean isCoastal = hex.isCoastal();
        boolean hasRiver = hex.hasAnyRiver();
        int waterDist = hex.getWaterDistance();

        HydrologyLevel hydro;
        if (hasRiver || isCoastal) {
            hydro = HydrologyLevel.WET;
        } else if (waterDist >= 3) {
            hydro = HydrologyLevel.DRY;
        } else {
            hydro = HydrologyLevel.MODERATE;
        }

        List<Biome> candidates = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();

        for (Biome b : GameConstants.getBiomes()) {
            if (isWaterBiome(b) || (b == GameConstants.BIOME_ICE_FIELD && absLat < 0.85)) continue;
            if (!isCoastal && b.isCoastal() && !hasRiver) continue;
            if (!b.supportsElevation(hex.getElevation())) continue;

            boolean eligible = isBiomeEligibleAt(b, absLat, isCoastal, hasRiver, hydro);
            if (!eligible) continue;

            int weight = switch (b.getRarity()) {
                case COMMON -> 50;
                case UNCOMMON -> 35;
                case RARE -> 22;
                case VERY_RARE -> 15;
            };

            if (isCoastal && b.isCoastal()) {
                weight *= 3;
            }
            if (b.getHydrologyLevel() == hydro) {
                weight *= 2;
            }

            for (Hex neighbor : hex.getAdjacentNeighbors()) {
                if (neighbor != null && neighbor.getBiome() == b) {
                    weight += 20;
                    break;
                }
            }

            candidates.add(b);
            weights.add(Math.max(1, weight));
        }

        if (candidates.isEmpty()) {
            Biome best = GameConstants.BIOME_PLAINS;
            int bestDiff = Integer.MAX_VALUE;
            for (Biome b : GameConstants.getBiomes()) {
                if (isWaterBiome(b) || (b == GameConstants.BIOME_ICE_FIELD && absLat < 0.85)) continue;
                int diff = 0;
                if (hex.getElevation() < b.getMinElevation()) {
                    diff = b.getMinElevation() - hex.getElevation();
                } else if (hex.getElevation() > b.getMaxElevation()) {
                    diff = hex.getElevation() - b.getMaxElevation();
                }
                if (diff < bestDiff) {
                    bestDiff = diff;
                    best = b;
                }
            }
            return best;
        }

        int totalWeight = 0;
        for (int w : weights) totalWeight += w;
        int roll = GameRandom.nextInt(totalWeight);
        int accum = 0;
        for (int i = 0; i < candidates.size(); i++) {
            accum += weights.get(i);
            if (roll < accum) {
                return candidates.get(i);
            }
        }
        return candidates.get(0);
    }

    private boolean isBiomeEligibleAt(Biome b, double absLat, boolean isCoastal, boolean hasRiver, HydrologyLevel hydro) {
        if (b == GameConstants.BIOME_ICE_FIELD) {
            return false;
        }
        if (b == GameConstants.BIOME_TUNDRA) {
            return absLat >= 0.52;
        }
        if (b == GameConstants.BIOME_TAIGA) {
            return absLat >= 0.35 && absLat <= 0.85;
        }
        if (b == GameConstants.BIOME_FOREST) {
            return absLat >= 0.12 && absLat <= 0.68;
        }
        if (b == GameConstants.BIOME_JUNGLE) {
            return absLat <= 0.35;
        }
        if (b == GameConstants.BIOME_MANGROVE) {
            return absLat <= 0.38 && (isCoastal || hasRiver);
        }
        if (b == GameConstants.BIOME_DESERT) {
            return (absLat >= 0.12 && absLat <= 0.50) || hydro == HydrologyLevel.DRY;
        }
        if (b == GameConstants.BIOME_PLAINS) {
            return absLat <= 0.72;
        }
        if (b == GameConstants.BIOME_PRAIRIE) {
            return absLat <= 0.68;
        }
        if (b == GameConstants.BIOME_SWAMP) {
            return absLat <= 0.60 && (hydro == HydrologyLevel.WET || hasRiver || isCoastal);
        }
        if (b == GameConstants.BIOME_WETLANDS) {
            return absLat <= 0.75 && (hydro == HydrologyLevel.WET || hasRiver || isCoastal);
        }
        if (b == GameConstants.BIOME_BEACH) {
            return (isCoastal || hasRiver) && absLat <= 0.75;
        }
        if (b == GameConstants.BIOME_CLIFFS) {
            return isCoastal || hasRiver || absLat >= 0.60;
        }
        if (b == GameConstants.BIOME_BURNT_FOREST) {
            return absLat <= 0.75;
        }
        if (b == GameConstants.BIOME_MOUNTAIN || b == GameConstants.BIOME_VOLCANIC || b == GameConstants.BIOME_URBAN || b == GameConstants.BIOME_PEAK) {
            return true;
        }
        return b.getClimateZone() == ClimateZone.ANY;
    }

    public void populateDynasties(Biome startBiome, Colony startColony, String baseName, String playerTitleKey) {
        if (!this.spectatorMode) {
            if (startBiome == null) {
                startBiome = GameConstants.BIOME_PLAINS;
            }
            if (baseName == null || baseName.isEmpty()) {
                baseName = "Player";
            }
            if (playerTitleKey == null || playerTitleKey.isEmpty()) {
                playerTitleKey = GameConstants.DYNASTY_TITLE_DYNASTY.getNameKey();
            }
            if (startColony == null) {
                startColony = new Colony(1, baseName + " Prime", true);
            }
            ColonyStarterService starterService = ColonyStarterService.shared();
            if (startColony.getAntTotal() == 0) {
                starterService.initializeNewColony(startColony);
            }

            baseName = formatName(baseName);
            DynastyTitle playerTitle = GameConstants.getDynastyTitleByKey(playerTitleKey);
            String dynName = LanguageStrings.formatDynastyName(baseName, playerTitle);
            namingService.registerUsedName(dynName);

            Dynasty playerDynasty = new Dynasty(this.dynastyIdCounter++, dynName, playerTitle.getNameKey(), true, GameConstants.SPECIES_OMNI);
            playerDynasty.setThemeBase(baseName);
            playerDynasty.getStarterService().initializeDynasty(playerDynasty);

            String capName = namingService.generateCapitalName(baseName);
            startColony.setName(capName);

            playerDynasty.addColony(startColony);
            playerDynasty.setCapital(startColony);
            registerDynasty(playerDynasty);
            historyService.record(WorldHistoryEventType.DYNASTY_FORMED, LanguageStrings.HISTORY_DYNASTY_FORMED_FMT,
                    playerDynasty.getId(), startColony.getId(), -1,
                    WorldHistoryEvent.dynastyArg(playerDynasty.getId()));
            historyService.record(WorldHistoryEventType.COLONY_FOUNDED, LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                    playerDynasty.getId(), startColony.getId(), -1,
                    WorldHistoryEvent.colonyArg(startColony.getId()),
                    WorldHistoryEvent.dynastyArg(playerDynasty.getId()));

            this.colonyIdCounter = startColony.getId() + 1;
        }

        Map<Integer, Set<Hex>> continentHexes = new HashMap<>();
        for (Hex h : this.hexes) {
            if (h != null && h.getContinentId() > 0 && isLandHex(h) && !h.isIsland()) {
                continentHexes.computeIfAbsent(h.getContinentId(), k -> new HashSet<>()).add(h);
            }
        }
        if (continentHexes.isEmpty()) {
            for (Hex h : this.hexes) {
                if (h != null && isLandHex(h)) {
                    continentHexes.computeIfAbsent(1, k -> new HashSet<>()).add(h);
                }
            }
        }
        if (!this.spectatorMode) {
            spawnPlayerOnRandomContinent(startBiome, startColony, continentHexes);
        }
        distributeRivalDynastiesAcrossContinents(continentHexes);
        markColonizedHexIndexDirty();
        rebuildColonizedHexIndexIfNeeded();
        bindDynastyTradeServices();
    }

    private void spawnPlayerOnRandomContinent(Biome startBiome, Colony startColony, Map<Integer, Set<Hex>> continentHexes) {
        List<Integer> matchingContinents = new ArrayList<>();
        for (Map.Entry<Integer, Set<Hex>> entry : continentHexes.entrySet()) {
            for (Hex h : entry.getValue()) {
                if (h.getBiome() == startBiome && !h.isIsland() && h.getColony() == null) {
                    matchingContinents.add(entry.getKey());
                    break;
                }
            }
        }

        int chosenContinentId;
        if (!matchingContinents.isEmpty()) {
            chosenContinentId = matchingContinents.get(GameRandom.nextInt(matchingContinents.size()));
        } else {
            List<Integer> allCids = new ArrayList<>(continentHexes.keySet());
            chosenContinentId = allCids.get(GameRandom.nextInt(allCids.size()));
        }

        Set<Hex> cSet = continentHexes.get(chosenContinentId);
        Hex spawnHex = null;
        for (Hex h : cSet) {
            if (h.getBiome() == startBiome && !h.isIsland() && h.getColony() == null) {
                spawnHex = h;
                break;
            }
        }
        if (spawnHex == null) {
            for (Hex h : cSet) {
                if (!h.isIsland() && h.getColony() == null && h.getBiome() != null && h.getBiome().canStart()) {
                    spawnHex = h;
                    spawnHex.setBiome(startBiome);
                    spawnHex.setLocalWeather(getRandomWeather(startBiome));
                    break;
                }
            }
        }
        if (spawnHex == null) {
            spawnHex = this.hexes.get(0);
        }

        spawnHex.setColony(startColony);
        markColonizedHexIndexDirty();
        startColony.setActive(true);
        changeActiveHex(spawnHex);
    }

    private void distributeRivalDynastiesAcrossContinents(Map<Integer, Set<Hex>> continentHexes) {
        List<AntSpecies> spawnableSpecies = GameConstants.getWorldSpawnableNpcSpecies();
        int minRequired = spawnableSpecies.size() * 4;
        int multiplier = Math.max(4, (this.worldHeight >= 30) ? 4 : 2);
        int requiredNpcs = Math.max(minRequired, spawnableSpecies.size() * multiplier);
        List<AntSpecies> speciesList = assignNpcSpecies(requiredNpcs);
        List<Integer> continentIds = new ArrayList<>(continentHexes.keySet());
        Collections.shuffle(continentIds, GameRandom.getShuffleRandom());

        ColonyStarterService starterService = ColonyStarterService.shared();
        List<Hex> placedCapitals = new ArrayList<>();
        if (getActiveHex() != null && getActiveHex().getColony() != null) {
            placedCapitals.add(getActiveHex());
        }

        int dynastyLimit = (this.targetRivalDynastyCount >= 0)
                ? Math.min(this.targetRivalDynastyCount, speciesList.size())
                : speciesList.size();
        Set<AntSpecies> spawnedInFavBiome = new HashSet<>();
        for (int i = 0; i < dynastyLimit; i++) {
            AntSpecies npcSpecies = speciesList.get(i);
            Biome favBiome = (npcSpecies != null && npcSpecies.getPreferredBiome() != null && npcSpecies.getPreferredBiome().canStart())
                    ? npcSpecies.getPreferredBiome()
                    : null;
            boolean needsFavBiome = (favBiome != null && !spawnedInFavBiome.contains(npcSpecies));
            int cid = !continentIds.isEmpty() ? continentIds.get(i % continentIds.size()) : 0;
            Set<Hex> cSet = continentHexes.getOrDefault(cid, Set.of());

            Hex bestHex = null;
            int bestScore = Integer.MIN_VALUE;

            if (needsFavBiome) {
                for (Hex h : cSet) {
                    if (h.getColony() != null || h.isIsland() || h.getBiome() != favBiome) {
                        continue;
                    }
                    int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies) + 200;
                    if (score > bestScore) {
                        bestScore = score;
                        bestHex = h;
                    }
                }
                if (bestHex == null) {
                    for (Set<Hex> otherSet : continentHexes.values()) {
                        for (Hex h : otherSet) {
                            if (h.getColony() != null || h.isIsland() || h.getBiome() != favBiome) {
                                continue;
                            }
                            int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies) + 150;
                            if (score > bestScore) {
                                bestScore = score;
                                bestHex = h;
                            }
                        }
                    }
                }
                if (bestHex == null) {
                    for (Hex h : this.hexes) {
                        if (h.getColony() == null && isLandHex(h) && !h.isIsland() && h.getBiome() == favBiome) {
                            int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies) + 100;
                            if (score > bestScore) {
                                bestScore = score;
                                bestHex = h;
                            }
                        }
                    }
                }
            }

            if (bestHex == null) {
                for (Hex h : cSet) {
                    if (h.getColony() != null || h.isIsland() || h.getBiome() == null || !h.getBiome().canStart()) {
                        continue;
                    }
                    int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies);
                    if (score > bestScore) {
                        bestScore = score;
                        bestHex = h;
                    }
                }
            }

            if (favBiome != null && (bestHex == null || bestHex.getBiome() != favBiome)) {
                int favScore = bestHex != null ? bestScore : Integer.MIN_VALUE;
                for (Set<Hex> otherSet : continentHexes.values()) {
                    for (Hex h : otherSet) {
                        if (h.getColony() != null || h.isIsland() || h.getBiome() != favBiome) {
                            continue;
                        }
                        int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies);
                        if (score > favScore) {
                            favScore = score;
                            bestHex = h;
                            bestScore = score;
                        }
                    }
                }
            }

            if (bestHex == null) {
                for (Hex h : this.hexes) {
                    if (h.getColony() == null && isLandHex(h) && !h.isIsland() && h.getBiome() != null && h.getBiome().canStart()) {
                        int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies);
                        if (score > bestScore) {
                            bestScore = score;
                            bestHex = h;
                        }
                    }
                }
            }
            if (bestHex == null) {
                for (Hex h : this.hexes) {
                    if (h.getColony() == null && isLandHex(h) && h.getBiome() != null && h.getBiome().canStart()) {
                        int score = scoreCapitalCandidate(h, placedCapitals, npcSpecies);
                        if (score > bestScore) {
                            bestScore = score;
                            bestHex = h;
                        }
                    }
                }
            }

            if (bestHex == null) {
                continue;
            }

            if (favBiome != null && bestHex.getBiome() != favBiome) {
                if (needsFavBiome) {
                    bestHex.setBiome(favBiome);
                    bestHex.setLocalWeather(getRandomWeather(favBiome));
                } else if (countBiomeHexes(bestHex.getBiome()) > 22) {
                    if (bestHex.getBiome().getClimateZone() == favBiome.getClimateZone()
                            || favBiome.getClimateZone() == ClimateZone.ANY) {
                        bestHex.setBiome(favBiome);
                        bestHex.setLocalWeather(getRandomWeather(favBiome));
                    }
                }
            }
            if (favBiome != null && bestHex.getBiome() == favBiome) {
                spawnedInFavBiome.add(npcSpecies);
            }

            int dynastyId = this.dynastyIdCounter++;
            DynastyTitle npcTitle = namingService.pickRandomTitle();
            String npcThemeKey = namingService.claimThemeKey(npcSpecies);
            String npcDynName = LanguageStrings.formatDynastyName(npcThemeKey, npcTitle);
            Dynasty npcDynasty = new Dynasty(dynastyId, npcDynName, npcTitle.getNameKey(), false, npcSpecies);
            npcDynasty.setThemeBase(npcThemeKey);
            npcDynasty.getStarterService().initializeDynasty(npcDynasty);

            int colId = this.colonyIdCounter++;
            String npcCapName = namingService.generateCapitalName(npcThemeKey);
            Colony aiColony = new Colony(colId, npcCapName, false);
            npcDynasty.addColony(aiColony);
            npcDynasty.setCapital(aiColony);
            registerDynasty(npcDynasty);

            bestHex.setColony(aiColony);
            markColonizedHexIndexDirty();
            starterService.initializeNewColony(aiColony);
            placedCapitals.add(bestHex);
            historyService.record(WorldHistoryEventType.DYNASTY_FORMED, LanguageStrings.HISTORY_DYNASTY_FORMED_FMT,
                    npcDynasty.getId(), aiColony.getId(), -1,
                    WorldHistoryEvent.dynastyArg(npcDynasty.getId()));
            historyService.record(WorldHistoryEventType.COLONY_FOUNDED, LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                    npcDynasty.getId(), aiColony.getId(), -1,
                    WorldHistoryEvent.colonyArg(aiColony.getId()),
                    WorldHistoryEvent.dynastyArg(npcDynasty.getId()));
        }
    }

    private int scoreCapitalCandidate(Hex h, List<Hex> placedCapitals, AntSpecies npcSpecies) {
        int minCapDist = Integer.MAX_VALUE;
        for (Hex cap : placedCapitals) {
            int d = distanceBetweenHexes(h, cap);
            if (d < minCapDist) {
                minCapDist = d;
            }
        }

        int score = 0;
        if (minCapDist >= 3) {
            score += 50;
        } else if (minCapDist >= 2) {
            score += 20;
        }

        if (npcSpecies != null && npcSpecies.getPreferredBiome() != null && h.getBiome() != null) {
            if (h.getBiome() == npcSpecies.getPreferredBiome()) {
                score += 100;
            } else if (h.getBiome().getClimateZone() == npcSpecies.getPreferredBiome().getClimateZone()) {
                score += 30;
            }
        }

        score += GameRandom.nextInt(15);
        return score;
    }

    private int countBiomeHexes(Biome biome) {
        if (biome == null || this.hexes == null) return 0;
        int count = 0;
        for (Hex h : this.hexes) {
            if (h != null && h.getBiome() == biome) {
                count++;
            }
        }
        return count;
    }

    private void applyCoastalLandRings() {
        Set<Hex> mainland = new HashSet<>();
        for (Hex hex : this.hexes) {
            if (hexDistanceFromOrigin(hex) <= this.continentCoreRadius && isLandHex(hex)) {
                mainland.add(hex);
            }
        }

        int coastalCount = GameNumbers.WORLD_COASTAL_RING_COUNT;
        int[] chances = GameNumbers.WORLD_COASTAL_LAND_CHANCE_PERCENT;
        for (int coastalIndex = 0; coastalIndex < coastalCount; coastalIndex++) {
            int ringDist = this.continentCoreRadius + 1 + coastalIndex;
            int landChance = coastalIndex < chances.length ? chances[coastalIndex] : 0;
            boolean requireMainlandConnection = coastalIndex < coastalCount - 1;

            List<Hex> ringHexes = new ArrayList<>();
            for (Hex hex : this.hexes) {
                if (hexDistanceFromOrigin(hex) == ringDist) {
                    ringHexes.add(hex);
                }
            }
            Collections.shuffle(ringHexes, GameRandom.getShuffleRandom());

            List<Hex> newlyLand = new ArrayList<>();
            for (Hex hex : ringHexes) {
                if (GameRandom.nextInt(100) >= landChance) {
                    continue;
                }
                if (requireMainlandConnection && !touchesMainland(hex, mainland)) {
                    continue;
                }
                Biome landBiome = getBiomeForRing(Math.min(ringDist, this.continentCoreRadius));
                if (isWaterBiome(landBiome)) {
                    landBiome = GameConstants.BIOME_PLAINS;
                }
                hex.setBiome(landBiome);
                hex.setLocalWeather(getRandomWeather(landBiome));
                newlyLand.add(hex);
            }
            if (requireMainlandConnection) {
                mainland.addAll(newlyLand);
            } else {
                for (Hex hex : newlyLand) {
                    if (touchesMainland(hex, mainland)) {
                        mainland.add(hex);
                    }
                }
            }
        }
    }

    private boolean touchesMainland(Hex hex, Set<Hex> mainland) {
        if (hex == null || mainland == null || mainland.isEmpty()) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && mainland.contains(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private void ensureMinimumIslands(int minCount) {
        if (minCount <= 0) {
            return;
        }
        classifyLandmasses();
        if (this.islandCount >= minCount) {
            return;
        }

        if (this.worldWidth > 0 && this.worldHeight > 0) {
            List<Hex> candidates = new ArrayList<>();
            int polarCutoff = Math.max(2, this.worldHeight / 2 - 2);
            for (Hex hex : this.hexes) {
                if (hex == null || Math.abs(hex.getR()) >= polarCutoff) continue;
                if (isOceanHex(hex) && !touchesAnyLand(hex)) {
                    candidates.add(hex);
                }
            }
            Collections.shuffle(candidates, GameRandom.getShuffleRandom());
            int needed = minCount - this.islandCount;
            for (Hex h : candidates) {
                if (needed <= 0) break;
                if (!touchesAnyLand(h)) {
                    Biome biome = pickBiomeForLandHex(h, this.worldHeight / 2);
                    h.setBiome(biome);
                    h.setLocalWeather(getRandomWeather(biome));
                    needed--;
                }
            }
            if (needed > 0) {
                List<Hex> coastalTips = new ArrayList<>();
                for (Hex hex : this.hexes) {
                    if (hex == null || Math.abs(hex.getR()) >= polarCutoff) continue;
                    if (!isLandHex(hex) || hex.getBiome() == GameConstants.BIOME_ICE_FIELD || hex.getContinentId() <= 0) continue;
                    int landNeighborCount = 0;
                    for (Hex n : hex.getAdjacentNeighbors()) {
                        if (n != null && isLandHex(n) && n.getContinentId() == hex.getContinentId()) {
                            landNeighborCount++;
                        }
                    }
                    if (landNeighborCount == 1) {
                        coastalTips.add(hex);
                    }
                }
                Collections.shuffle(coastalTips, GameRandom.getShuffleRandom());
                for (Hex tip : coastalTips) {
                    if (needed <= 0) break;
                    Hex bridge = null;
                    for (Hex n : tip.getAdjacentNeighbors()) {
                        if (n != null && isLandHex(n) && n.getContinentId() == tip.getContinentId()) {
                            bridge = n;
                            break;
                        }
                    }
                    if (bridge != null) {
                        bridge.setBiome(GameConstants.BIOME_SHALLOW_OCEAN);
                        bridge.setLocalWeather(getRandomWeather(GameConstants.BIOME_SHALLOW_OCEAN));
                        bridge.setContinentId(0);
                        bridge.setElevation(2);
                        tip.setContinentId(0);
                        tip.setIsland(true);
                        needed--;
                    }
                }
            }
            classifyLandmasses();
            return;
        }

        int outerOceanDist = this.continentCoreRadius
                + GameNumbers.WORLD_COASTAL_RING_COUNT
                + GameNumbers.WORLD_OUTER_OCEAN_RING_COUNT;
        int islandRingDist = this.continentCoreRadius + GameNumbers.WORLD_COASTAL_RING_COUNT;

        int needed = minCount - this.islandCount;
        needed -= placeDisconnectedIslandHexes(collectIsolatedOceanCandidates(islandRingDist, outerOceanDist), islandRingDist, needed);
        if (needed > 0) {
            needed -= placeDisconnectedIslandHexes(collectIsolatedOceanCandidates(-1, outerOceanDist), islandRingDist, needed);
        }
        while (needed > 0 && carveOneIslandFromMainland(islandRingDist, outerOceanDist)) {
            needed--;
        }
    }

    private int placeDisconnectedIslandHexes(List<Hex> candidates, int biomeRingDist, int needed) {
        if (needed <= 0 || candidates.isEmpty()) {
            return 0;
        }
        Collections.shuffle(candidates, GameRandom.getShuffleRandom());
        int placed = 0;
        for (Hex hex : candidates) {
            if (placed >= needed) {
                break;
            }
            if (!isWaterBiome(hex.getBiome()) || touchesAnyLand(hex)) {
                continue;
            }
            int ring = biomeRingDist > 0 ? biomeRingDist : hexDistanceFromOrigin(hex);
            Biome landBiome = getBiomeForRing(Math.min(ring, Math.max(1, this.continentCoreRadius)));
            if (isWaterBiome(landBiome)) {
                landBiome = GameConstants.BIOME_PLAINS;
            }
            hex.setBiome(landBiome);
            hex.setLocalWeather(getRandomWeather(landBiome));
            placed++;
        }
        return placed;
    }

    private List<Hex> collectIsolatedOceanCandidates(int requiredDist, int outerOceanDist) {
        List<Hex> candidates = new ArrayList<>();
        for (Hex hex : this.hexes) {
            int dist = hexDistanceFromOrigin(hex);
            if (dist <= this.continentCoreRadius || dist >= outerOceanDist) {
                continue;
            }
            if (requiredDist >= 0 && dist != requiredDist) {
                continue;
            }
            if (!isWaterBiome(hex.getBiome()) || touchesAnyLand(hex)) {
                continue;
            }
            candidates.add(hex);
        }
        return candidates;
    }

    private boolean carveOneIslandFromMainland(int islandRingDist, int outerOceanDist) {
        List<Hex> tips = new ArrayList<>();
        for (Hex hex : this.hexes) {
            int dist = hexDistanceFromOrigin(hex);
            if (dist != islandRingDist || dist >= outerOceanDist) {
                continue;
            }
            if (!isLandHex(hex) || hex.getColony() != null) {
                continue;
            }
            tips.add(hex);
        }
        Collections.shuffle(tips, GameRandom.getShuffleRandom());
        for (Hex tip : tips) {
            List<Hex> bridges = new ArrayList<>();
            boolean blocked = false;
            for (Hex neighbor : tip.getAdjacentNeighbors()) {
                if (neighbor == null || !isLandHex(neighbor)) {
                    continue;
                }
                if (hexDistanceFromOrigin(neighbor) <= this.continentCoreRadius || neighbor.getColony() != null) {
                    blocked = true;
                    break;
                }
                bridges.add(neighbor);
            }
            if (blocked || bridges.isEmpty()) {
                continue;
            }
            for (Hex bridge : bridges) {
                bridge.setBiome(GameConstants.BIOME_OCEAN);
                bridge.setLocalWeather(getRandomWeather(GameConstants.BIOME_OCEAN));
            }
            if (!touchesAnyLand(tip)) {
                return true;
            }
            for (Hex bridge : bridges) {
                Biome restored = getBiomeForRing(Math.min(hexDistanceFromOrigin(bridge), Math.max(1, this.continentCoreRadius)));
                if (isWaterBiome(restored)) {
                    restored = GameConstants.BIOME_PLAINS;
                }
                bridge.setBiome(restored);
                bridge.setLocalWeather(getRandomWeather(restored));
            }
        }
        return false;
    }

    private boolean touchesAnyLand(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && isLandHex(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private void ensureMinimumBiomeCounts(int minPerBiome) {
        if (minPerBiome <= 0) {
            return;
        }
        Map<Biome, Integer> counts = new HashMap<>();
        Map<Biome, List<Hex>> byBiome = new HashMap<>();
        for (Biome biome : GameConstants.getBiomes()) {
            counts.put(biome, 0);
            byBiome.put(biome, new ArrayList<>());
        }
        for (Hex hex : this.hexes) {
            Biome biome = hex.getBiome();
            if (biome == null) {
                continue;
            }
            counts.merge(biome, 1, Integer::sum);
            byBiome.computeIfAbsent(biome, ignored -> new ArrayList<>()).add(hex);
        }

        boolean progress = true;
        while (progress) {
            progress = false;
            for (Biome target : GameConstants.getBiomes()) {
                if (counts.getOrDefault(target, 0) >= minPerBiome) {
                    continue;
                }
                Set<Biome> skipDonors = new HashSet<>();
                skipDonors.add(GameConstants.BIOME_OCEAN);
                skipDonors.add(GameConstants.BIOME_DEEP_OCEAN);
                skipDonors.add(GameConstants.BIOME_SHALLOW_OCEAN);
                if (target != GameConstants.BIOME_LAKE) {
                    skipDonors.add(GameConstants.BIOME_LAKE);
                }
                skipDonors.add(target);
                while (counts.getOrDefault(target, 0) < minPerBiome) {
                    Biome donor = findRichestBiomeDonor(counts, skipDonors, minPerBiome);
                    if (donor == null) {
                        break;
                    }
                    List<Hex> donorHexes = byBiome.get(donor);
                    Hex hex = pickBiomeRebalanceHex(donorHexes, target);
                    if (hex == null && target == GameConstants.BIOME_LAKE) {
                        hex = forceCreateInlandLakeHex(donorHexes, counts, byBiome, donor);
                    }
                    if (hex == null) {
                        skipDonors.add(donor);
                        continue;
                    }
                    if (hex.getBiome() != target) {
                        Biome previous = hex.getBiome();
                        if (counts.getOrDefault(previous, 0) <= minPerBiome) {
                            skipDonors.add(previous);
                            continue;
                        }
                        List<Hex> previousList = byBiome.get(previous);
                        if (previousList != null) {
                            previousList.remove(hex);
                        }
                        counts.put(previous, Math.max(0, counts.getOrDefault(previous, 0) - 1));
                        hex.setBiome(target);
                        hex.setElevation(Math.max(target.getMinElevation(), Math.min(target.getMaxElevation(), hex.getElevation())));
                        hex.setLocalWeather(getRandomWeather(target));
                        byBiome.computeIfAbsent(target, ignored -> new ArrayList<>()).add(hex);
                        counts.put(target, counts.getOrDefault(target, 0) + 1);
                        progress = true;
                    } else {
                        skipDonors.add(donor);
                    }
                }
            }
        }
        for (Biome target : GameConstants.getBiomes()) {
            if (counts.getOrDefault(target, 0) > 0) {
                continue;
            }
            if (isWaterBiome(target)) {
                if (target == GameConstants.BIOME_LAKE) {
                    Biome donor = findRichestBiomeDonor(counts, Collections.emptySet(), 1);
                    if (donor != null) {
                        forceCreateInlandLakeHex(byBiome.get(donor), counts, byBiome, donor);
                    }
                } else if (target == GameConstants.BIOME_DEEP_OCEAN) {
                    Hex hex = findDeepestOceanCandidate();
                    if (hex != null) {
                        hex.setBiome(target);
                        hex.setElevation(0);
                        counts.put(target, counts.getOrDefault(target, 0) + 1);
                    }
                } else if (target == GameConstants.BIOME_SHALLOW_OCEAN) {
                    Hex hex = findShallowOceanCandidate();
                    if (hex != null) {
                        hex.setBiome(target);
                        hex.setElevation(2);
                        counts.put(target, counts.getOrDefault(target, 0) + 1);
                    }
                } else if (target == GameConstants.BIOME_OCEAN) {
                    Hex hex = findRegularOceanCandidate();
                    if (hex != null) {
                        hex.setBiome(target);
                        hex.setElevation(1);
                        counts.put(target, counts.getOrDefault(target, 0) + 1);
                    }
                }
            } else {
                Hex hex = findLandHexForBiomeFallback(target, byBiome, counts);
                if (hex != null) {
                    Biome previous = hex.getBiome();
                    if (previous != null) {
                        counts.put(previous, Math.max(0, counts.getOrDefault(previous, 0) - 1));
                    }
                    hex.setBiome(target);
                    if (target == GameConstants.BIOME_PEAK) {
                        hex.setElevation(9);
                    } else if (target == GameConstants.BIOME_MOUNTAIN || target == GameConstants.BIOME_VOLCANIC) {
                        hex.setElevation(8);
                    } else if (target == GameConstants.BIOME_CLIFFS) {
                        hex.setElevation(5);
                    } else {
                        hex.setElevation(Math.max(target.getMinElevation(), Math.min(target.getMaxElevation(), hex.getElevation())));
                    }
                    hex.setLocalWeather(getRandomWeather(target));
                    counts.put(target, counts.getOrDefault(target, 0) + 1);
                }
            }
        }
    }

    private Hex findFurthestPolarHex() {
        Hex best = null;
        int maxDist = -1;
        for (Hex h : this.hexes) {
            if (h != null && h.getColony() == null) {
                int dist = (this.worldHeight > 0) ? Math.abs(h.getR()) : hexDistanceFromOrigin(h);
                if (dist > maxDist) {
                    maxDist = dist;
                    best = h;
                }
            }
        }
        return best;
    }

    private Hex findDeepestOceanCandidate() {
        Hex best = null;
        int maxDist = -1;
        for (Hex h : this.hexes) {
            if (h != null && isOceanHex(h) && h.getElevation() <= 1) {
                if (h.getWaterDistance() > maxDist) {
                    maxDist = h.getWaterDistance();
                    best = h;
                }
            }
        }
        return best;
    }

    private Hex findShallowOceanCandidate() {
        for (Hex h : this.hexes) {
            if (h != null && isOceanHex(h) && h.getWaterDistance() == 1) {
                return h;
            }
        }
        return null;
    }

    private Hex findRegularOceanCandidate() {
        for (Hex h : this.hexes) {
            if (h != null && isOceanHex(h) && h.getWaterDistance() >= 2 && h.getWaterDistance() <= 5) {
                return h;
            }
        }
        return null;
    }

    private Hex findLandHexForBiomeFallback(Biome target, Map<Biome, List<Hex>> byBiome, Map<Biome, Integer> counts) {
        Set<Biome> excluded = new HashSet<>();
        excluded.add(GameConstants.BIOME_OCEAN);
        excluded.add(GameConstants.BIOME_DEEP_OCEAN);
        excluded.add(GameConstants.BIOME_SHALLOW_OCEAN);
        excluded.add(GameConstants.BIOME_LAKE);
        excluded.add(target);

        Biome donor = findRichestBiomeDonor(counts, excluded, 1);
        if (donor == null) {
            return null;
        }
        List<Hex> donorHexes = byBiome.get(donor);
        if (donorHexes == null || donorHexes.isEmpty()) {
            return null;
        }
        if (target == GameConstants.BIOME_BEACH || target == GameConstants.BIOME_MANGROVE) {
            for (Hex h : donorHexes) {
                if (h != null && h.getColony() == null && touchesOcean(h)) {
                    return h;
                }
            }
        }
        if (target == GameConstants.BIOME_ICE_FIELD) {
            Hex best = null;
            int maxLat = -1;
            for (Hex h : donorHexes) {
                if (h != null && h.getColony() == null) {
                    int lat = Math.abs(h.getR());
                    if (lat > maxLat) {
                        maxLat = lat;
                        best = h;
                    }
                }
            }
            if (best != null) return best;
        }
        for (Hex h : donorHexes) {
            if (h != null && h.getColony() == null) {
                return h;
            }
        }
        return null;
    }

    private Biome findRichestBiomeDonor(Map<Biome, Integer> counts, Set<Biome> skipDonors, int minPerBiome) {
        Biome best = null;
        int bestCount = -1;
        for (Biome biome : GameConstants.getBiomes()) {
            if (skipDonors.contains(biome)) {
                continue;
            }
            int count = counts.getOrDefault(biome, 0);
            if (count <= minPerBiome) {
                continue;
            }
            if (count > bestCount) {
                best = biome;
                bestCount = count;
            }
        }
        return best;
    }

    private Hex pickBiomeRebalanceHex(List<Hex> donorHexes, Biome target) {
        if (donorHexes == null || donorHexes.isEmpty() || target == null) {
            return null;
        }
        int outerOceanDist = this.continentCoreRadius
                + GameNumbers.WORLD_COASTAL_RING_COUNT
                + GameNumbers.WORLD_OUTER_OCEAN_RING_COUNT;
        List<Hex> preferred = new ArrayList<>();
        List<Hex> fallback = new ArrayList<>();
        for (Hex hex : donorHexes) {
            if (this.worldWidth == 0 && (hex == null || hexDistanceFromOrigin(hex) >= outerOceanDist)) {
                continue;
            }
            if (this.worldWidth > 0 && (hex == null || Math.abs(hex.getR()) >= (this.worldHeight / 2 - 2))) {
                continue;
            }
            if (isOceanHex(hex)) {
                continue;
            }
            if (!canConvertHexToBiome(hex, target)) {
                continue;
            }
            if (hex.getColony() != null || hex.isIsland()) {
                fallback.add(hex);
            } else {
                preferred.add(hex);
            }
        }
        List<Hex> pool = !preferred.isEmpty() ? preferred : fallback;
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(GameRandom.nextInt(pool.size()));
    }

    private boolean canConvertHexToBiome(Hex hex, Biome target) {
        if (hex == null || target == null) {
            return false;
        }
        if (target == GameConstants.BIOME_ICE_FIELD && (hex.getContinentId() > 0 || hex.isIsland())) {
            return false;
        }
        if (isWaterBiome(target)) {
            if (hex.getColony() != null || hex.isIsland()) {
                return false;
            }
            if (target == GameConstants.BIOME_LAKE) {
                if (this.worldWidth > 0 && Math.abs(hex.getR()) >= (this.worldHeight / 2 - 4)) {
                    return false;
                }
                if (touchesOcean(hex) || touchesIceField(hex)) {
                    return false;
                }
            } else if (this.worldWidth > 0 && Math.abs(hex.getR()) >= (this.worldHeight / 2 - 4)) {
                return false;
            }
        }
        if (hex.getBiome() == GameConstants.BIOME_LAKE && !isWaterBiome(target) && wouldLinkMainlandAndIsland(hex)) {
            return false;
        }
        return true;
    }

    private boolean touchesIceField(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                return true;
            }
        }
        return false;
    }

    private boolean touchesOcean(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && isOceanHex(neighbor)) {
                return true;
            }
        }
        return false;
    }

    private boolean wouldLinkMainlandAndIsland(Hex hex) {
        if (hex == null) {
            return false;
        }
        boolean touchesMainland = false;
        boolean touchesIsland = false;
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor == null || !isLandHex(neighbor)) {
                continue;
            }
            if (neighbor.isIsland()) {
                touchesIsland = true;
            } else {
                touchesMainland = true;
            }
        }
        return touchesMainland && touchesIsland;
    }

    private Hex forceCreateInlandLakeHex(
            List<Hex> donorHexes,
            Map<Biome, Integer> counts,
            Map<Biome, List<Hex>> byBiome,
            Biome donor) {
        if (donorHexes == null || donorHexes.isEmpty() || donor == null) {
            return null;
        }
        int outerOceanDist = this.continentCoreRadius
                + GameNumbers.WORLD_COASTAL_RING_COUNT
                + GameNumbers.WORLD_OUTER_OCEAN_RING_COUNT;
        List<Hex> ranked = new ArrayList<>();
        for (Hex hex : donorHexes) {
            if (hex == null || hex.getColony() != null || hex.isIsland()) {
                continue;
            }
            if (this.worldWidth == 0 && hexDistanceFromOrigin(hex) >= outerOceanDist) {
                continue;
            }
            if (this.worldWidth > 0 && Math.abs(hex.getR()) >= (this.worldHeight / 2 - 4)) {
                continue;
            }
            if (touchesIceField(hex)) {
                continue;
            }
            ranked.add(hex);
        }
        ranked.sort((a, b) -> Integer.compare(countOceanNeighbors(a), countOceanNeighbors(b)));
        for (Hex center : ranked) {
            boolean sealed = true;
            for (Hex neighbor : center.getAdjacentNeighbors()) {
                if (neighbor == null) {
                    sealed = false;
                    break;
                }
                if (!isOceanHex(neighbor)) {
                    continue;
                }
                if (this.worldWidth == 0 && hexDistanceFromOrigin(neighbor) >= outerOceanDist) {
                    sealed = false;
                    break;
                }
                Biome fillBiome = donor;
                if (isWaterBiome(fillBiome)) {
                    fillBiome = GameConstants.BIOME_PLAINS;
                }
                Biome oldOceanBiome = neighbor.getBiome();
                List<Hex> oceanList = byBiome.get(oldOceanBiome);
                if (oceanList != null) {
                    oceanList.remove(neighbor);
                }
                counts.put(oldOceanBiome, Math.max(0, counts.getOrDefault(oldOceanBiome, 0) - 1));
                neighbor.setBiome(fillBiome);
                neighbor.setLocalWeather(getRandomWeather(fillBiome));
                byBiome.computeIfAbsent(fillBiome, ignored -> new ArrayList<>()).add(neighbor);
                counts.put(fillBiome, counts.getOrDefault(fillBiome, 0) + 1);
            }
            if (sealed && !touchesOcean(center)) {
                return center;
            }
        }
        return null;
    }

    private int countOceanNeighbors(Hex hex) {
        int count = 0;
        if (hex == null) {
            return count;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && isOceanHex(neighbor)) {
                count++;
            }
        }
        return count;
    }

    public void convertEnclosedOceansToLakes() {
        Set<Hex> openOcean = new HashSet<>();
        ArrayDeque<Hex> queue = new ArrayDeque<>();
        for (Hex hex : this.hexes) {
            if (!isOceanHex(hex)) {
                continue;
            }
            if (!isMapEdgeHex(hex)) {
                continue;
            }
            if (openOcean.add(hex)) {
                queue.add(hex);
            }
        }
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            for (Hex neighbor : current.getAdjacentNeighbors()) {
                if (neighbor == null || !isOceanHex(neighbor)) {
                    continue;
                }
                if (openOcean.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        for (Hex hex : this.hexes) {
            if (isOceanHex(hex) && !openOcean.contains(hex)) {
                hex.setBiome(GameConstants.BIOME_LAKE);
                hex.setLocalWeather(getRandomWeather(GameConstants.BIOME_LAKE));
            }
        }
    }

    public void convertLakesTouchingOceanToOcean() {
        ArrayDeque<Hex> queue = new ArrayDeque<>();
        for (Hex hex : this.hexes) {
            if (isOceanHex(hex)) {
                queue.add(hex);
            }
        }
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            for (Hex neighbor : current.getAdjacentNeighbors()) {
                if (neighbor == null || neighbor.getBiome() != GameConstants.BIOME_LAKE) {
                    continue;
                }
                neighbor.setBiome(current.getBiome());
                neighbor.setLocalWeather(getRandomWeather(current.getBiome()));
                queue.add(neighbor);
            }
        }
    }

    private static boolean isMapEdgeHex(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor == null) {
                return true;
            }
        }
        return false;
    }

    private static record PeninsulaSeed(Hex coastalLand, Hex initialWater, FaultLine fault) {}

    public List<List<Hex>> generateFaultLinePeninsulas() {
        Map<Integer, Set<Hex>> continentHexes = new HashMap<>();
        List<Hex> allLand = new ArrayList<>();
        if (this.hexes != null) {
            for (Hex h : this.hexes) {
                if (h != null && isLandHex(h) && h.getContinentId() > 0 && !h.isIsland()) {
                    continentHexes.computeIfAbsent(h.getContinentId(), unused -> new HashSet<>()).add(h);
                    allLand.add(h);
                }
            }
        }
        return generateFaultLinePeninsulas(continentHexes, allLand);
    }

    public List<List<Hex>> generateFaultLinePeninsulas(Map<Integer, Set<Hex>> continentHexes, List<Hex> allLand) {
        List<List<Hex>> createdPeninsulas = new ArrayList<>();
        if (continentHexes == null || continentHexes.isEmpty() || this.faultLines.isEmpty()) {
            return createdPeninsulas;
        }

        Map<Hex, List<FaultLine>> faultHexToFaults = new HashMap<>();
        for (FaultLine fault : this.faultLines) {
            if (fault == null) continue;
            for (Hex h : fault.getHexesA()) {
                if (h != null) faultHexToFaults.computeIfAbsent(h, unused -> new ArrayList<>()).add(fault);
            }
            for (Hex h : fault.getHexesB()) {
                if (h != null) faultHexToFaults.computeIfAbsent(h, unused -> new ArrayList<>()).add(fault);
            }
        }

        int maxLat = (this.worldHeight > 0)
                ? Math.max(2, this.worldHeight / 2 - 3)
                : Integer.MAX_VALUE;

        for (Map.Entry<Integer, Set<Hex>> entry : continentHexes.entrySet()) {
            int cid = entry.getKey();
            Set<Hex> cSet = entry.getValue();
            if (cid <= 0 || cSet == null || cSet.isEmpty()) continue;
            boolean hasIsland = false;
            for (Hex h : cSet) {
                if (h != null && h.isIsland()) {
                    hasIsland = true;
                    break;
                }
            }
            if (hasIsland) continue;
            if (isIceCapLandmass(new ArrayList<>(cSet))) continue;

            List<PeninsulaSeed> seeds = new ArrayList<>();

            for (Hex land : cSet) {
                if (land == null || Math.abs(land.getR()) > maxLat) continue;
                for (Hex w : land.getAdjacentNeighbors()) {
                    if (w == null || Math.abs(w.getR()) > maxLat) continue;
                    if (!isOceanHex(w) || w.getContinentId() != 0) continue;

                    boolean touchesOtherContinent = false;
                    for (Hex wn : w.getAdjacentNeighbors()) {
                        if (wn != null && wn.getContinentId() != 0 && wn.getContinentId() != cid) {
                            touchesOtherContinent = true;
                            break;
                        }
                        if (wn != null) {
                            for (Hex wnn : wn.getAdjacentNeighbors()) {
                                if (wnn != null && wnn.getContinentId() != 0 && wnn.getContinentId() != cid) {
                                    touchesOtherContinent = true;
                                    break;
                                }
                            }
                        }
                        if (touchesOtherContinent) break;
                    }
                    if (touchesOtherContinent) continue;

                    List<FaultLine> faults = faultHexToFaults.get(w);
                    if (faults == null) {
                        faults = faultHexToFaults.get(land);
                    }
                    if (faults != null && !faults.isEmpty()) {
                        for (FaultLine f : faults) {
                            seeds.add(new PeninsulaSeed(land, w, f));
                        }
                    }
                }
            }

            if (seeds.isEmpty()) continue;
            Collections.shuffle(seeds, GameRandom.getShuffleRandom());

            int targetCount = 1 + GameRandom.nextInt(3);
            int generatedCount = 0;
            Set<Hex> claimed = new HashSet<>();

            for (PeninsulaSeed seed : seeds) {
                if (generatedCount >= targetCount) break;
                if (claimed.contains(seed.initialWater) || !isOceanHex(seed.initialWater) || seed.initialWater.getContinentId() != 0) {
                    continue;
                }

                List<Hex> peninsula = new ArrayList<>();
                peninsula.add(seed.initialWater);
                claimed.add(seed.initialWater);

                int desiredLength = 2 + GameRandom.nextInt(3);
                Hex current = seed.initialWater;

                while (peninsula.size() < desiredLength) {
                    List<Hex> nextCandidates = new ArrayList<>();
                    for (Hex n : current.getAdjacentNeighbors()) {
                        if (n == null || Math.abs(n.getR()) > maxLat) continue;
                        if (!isOceanHex(n) || n.getContinentId() != 0 || claimed.contains(n)) continue;

                        boolean touchesOtherContinent = false;
                        for (Hex nn : n.getAdjacentNeighbors()) {
                            if (nn != null && nn.getContinentId() != 0 && nn.getContinentId() != cid) {
                                touchesOtherContinent = true;
                                break;
                            }
                            if (nn != null) {
                                for (Hex nnn : nn.getAdjacentNeighbors()) {
                                    if (nnn != null && nnn.getContinentId() != 0 && nnn.getContinentId() != cid) {
                                        touchesOtherContinent = true;
                                        break;
                                    }
                                }
                            }
                            if (touchesOtherContinent) break;
                        }
                        if (touchesOtherContinent) continue;

                        List<FaultLine> nFaults = faultHexToFaults.get(n);
                        if (nFaults != null && nFaults.contains(seed.fault)) {
                            nextCandidates.add(n);
                        }
                    }

                    if (nextCandidates.isEmpty()) break;
                    Hex nextHex = nextCandidates.get(GameRandom.nextInt(nextCandidates.size()));
                    peninsula.add(nextHex);
                    claimed.add(nextHex);
                    current = nextHex;
                }

                if (peninsula.size() >= 2) {
                    Biome fallbackBiome = GameConstants.BIOME_PLAINS;
                    for (Hex n : seed.coastalLand.getAdjacentNeighbors()) {
                        if (n != null && isLandHex(n) && n.getContinentId() == cid) {
                            fallbackBiome = n.getBiome();
                            break;
                        }
                    }
                    if (seed.coastalLand.getBiome() != null && !isWaterBiome(seed.coastalLand.getBiome())) {
                        fallbackBiome = seed.coastalLand.getBiome();
                    }

                    for (Hex penHex : peninsula) {
                        penHex.setContinentId(cid);
                        penHex.setIsland(false);
                        penHex.setCoastal(true);
                        penHex.setBiome(fallbackBiome);
                        int elev = (seed.fault.getType() == FaultLine.FaultType.CONVERGENT) ? 5 : 4;
                        penHex.setElevation(elev);
                        penHex.setLocalWeather(getRandomWeather(fallbackBiome));
                        cSet.add(penHex);
                        if (allLand != null && !allLand.contains(penHex)) {
                            allLand.add(penHex);
                        }
                    }
                    createdPeninsulas.add(peninsula);
                    generatedCount++;
                }
            }
        }

        return createdPeninsulas;
    }

    private boolean isIceCapLandmass(List<Hex> landmass) {
        if (landmass == null || landmass.isEmpty()) {
            return false;
        }
        for (Continent c : this.iceCaps) {
            if (c != null) {
                for (Hex h : landmass) {
                    if (c.containsHex(h)) {
                        return true;
                    }
                }
            }
        }
        int iceCount = 0;
        int polarMargin = (this.worldHeight > 0)
                ? Math.max(2, (int) Math.round(this.worldHeight * 0.36))
                : Integer.MAX_VALUE;
        boolean inPolarZone = true;
        for (Hex h : landmass) {
            if (h == null) continue;
            if (h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                iceCount++;
            }
            if (this.worldHeight > 0 && Math.abs(h.getR()) < polarMargin) {
                inPolarZone = false;
            }
        }
        if (iceCount >= landmass.size() * 0.60 && inPolarZone) {
            return true;
        }
        if (inPolarZone && iceCount > 0) {
            return true;
        }
        return false;
    }

    private void bridgeStraitHexToLand(Hex w, int targetContinentId) {
        if (w == null) return;
        List<Hex> landNeighbors = new ArrayList<>();
        int elevSum = 0;
        Map<Biome, Integer> biomeCounts = new HashMap<>();
        for (Hex n : w.getAdjacentNeighbors()) {
            if (n != null && isLandHex(n)) {
                landNeighbors.add(n);
                elevSum += n.getElevation();
                Biome b = n.getBiome();
                if (b != null && b != GameConstants.BIOME_PEAK && b != GameConstants.BIOME_MOUNTAIN) {
                    biomeCounts.put(b, biomeCounts.getOrDefault(b, 0) + 1);
                }
            }
        }
        Biome chosenBiome = GameConstants.BIOME_PLAINS;
        int bestCount = 0;
        for (Map.Entry<Biome, Integer> e : biomeCounts.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                chosenBiome = e.getKey();
            }
        }
        int avgElev = landNeighbors.isEmpty() ? 3 : Math.max(3, elevSum / landNeighbors.size());
        w.setBiome(chosenBiome);
        w.setElevation(avgElev);
        w.setContinentId(targetContinentId);
        w.setIsland(targetContinentId <= 0);
        w.setCoastal(true);
        w.setWaterDistance(1);
        w.setLocalWeather(getRandomWeather(chosenBiome));
        for (int d = 0; d < 6; d++) {
            w.setRiverEdge(d, false);
        }
    }

    public boolean fuseRobustlyConnectedLandmasses() {
        if (this.hexes == null || this.hexes.isEmpty()) {
            return false;
        }

        boolean anyFused = false;
        int maxPasses = 10;
        while (maxPasses-- > 0) {
            Set<Hex> visited = new HashSet<>();
            List<List<Hex>> landmasses = new ArrayList<>();
            for (Hex start : this.hexes) {
                if (start == null || !isLandHex(start) || visited.contains(start)) {
                    continue;
                }
                List<Hex> comp = new ArrayList<>();
                ArrayDeque<Hex> queue = new ArrayDeque<>();
                queue.add(start);
                visited.add(start);
                int startCid = start.getContinentId();
                boolean startIsland = start.isIsland();
                while (!queue.isEmpty()) {
                    Hex cur = queue.poll();
                    comp.add(cur);
                    for (Hex n : cur.getAdjacentNeighbors()) {
                        if (n == null || !isLandHex(n) || visited.contains(n)) {
                            continue;
                        }
                        if (n.getContinentId() != startCid || n.isIsland() != startIsland) {
                            continue;
                        }
                        visited.add(n);
                        queue.add(n);
                    }
                }
                landmasses.add(comp);
            }

            List<List<Hex>> nonIceLandmasses = new ArrayList<>();
            for (List<Hex> lm : landmasses) {
                if (!isIceCapLandmass(lm)) {
                    nonIceLandmasses.add(lm);
                }
            }

            int n = nonIceLandmasses.size();
            if (n < 2) {
                break;
            }

            Map<Hex, Integer> hexToLm = new HashMap<>();
            for (int i = 0; i < n; i++) {
                for (Hex h : nonIceLandmasses.get(i)) {
                    hexToLm.put(h, i);
                }
            }

            Map<Long, Integer> directTouches = new HashMap<>();
            Map<Long, Set<Hex>> straitHexes = new HashMap<>();

            for (int i = 0; i < n; i++) {
                for (Hex h : nonIceLandmasses.get(i)) {
                    for (Hex neighbor : h.getAdjacentNeighbors()) {
                        if (neighbor == null) continue;
                        Integer j = hexToLm.get(neighbor);
                        if (j != null && j != i) {
                            int minIdx = Math.min(i, j);
                            int maxIdx = Math.max(i, j);
                            long key = (((long) minIdx) << 32) | (maxIdx & 0xFFFFFFFFL);
                            if (i < j) {
                                directTouches.put(key, directTouches.getOrDefault(key, 0) + 1);
                            }
                        } else if (!isLandHex(neighbor)) {
                            for (Hex waterNeighbor : neighbor.getAdjacentNeighbors()) {
                                if (waterNeighbor == null) continue;
                                Integer k = hexToLm.get(waterNeighbor);
                                if (k != null && k != i) {
                                    int minIdx = Math.min(i, k);
                                    int maxIdx = Math.max(i, k);
                                    long key = (((long) minIdx) << 32) | (maxIdx & 0xFFFFFFFFL);
                                    straitHexes.computeIfAbsent(key, unused -> new HashSet<>()).add(neighbor);
                                }
                            }
                        }
                    }
                }
            }

            int[] parent = new int[n];
            for (int i = 0; i < n; i++) parent[i] = i;

            boolean passFused = false;
            Set<Long> allPairs = new HashSet<>(directTouches.keySet());
            allPairs.addAll(straitHexes.keySet());

            for (long key : allPairs) {
                int i = (int) (key >>> 32);
                int j = (int) (key & 0xFFFFFFFFL);
                int direct = directTouches.getOrDefault(key, 0);
                Set<Hex> straits = straitHexes.get(key);
                int straitCount = (straits != null) ? straits.size() : 0;
                int cidI = 0;
                for (Hex h : nonIceLandmasses.get(i)) {
                    if (h.getContinentId() > 0) {
                        cidI = h.getContinentId();
                        break;
                    }
                }
                int cidJ = 0;
                for (Hex h : nonIceLandmasses.get(j)) {
                    if (h.getContinentId() > 0) {
                        cidJ = h.getContinentId();
                        break;
                    }
                }

                boolean isRobust = false;
                if (direct > 0) {
                    isRobust = true;
                } else if (cidI > 0 && cidJ > 0 && straitCount >= 2) {
                    isRobust = true;
                } else if (cidI <= 0 && cidJ <= 0 && straitCount >= 2) {
                    isRobust = true;
                }

                if (isRobust) {
                    unionRoots(parent, i, j);
                    passFused = true;
                }
            }

            if (!passFused) {
                break;
            }
            anyFused = true;

            Map<Integer, Set<Hex>> straitsByRoot = new HashMap<>();
            for (long key : allPairs) {
                int i = (int) (key >>> 32);
                int j = (int) (key & 0xFFFFFFFFL);
                int rootI = findRoot(parent, i);
                int rootJ = findRoot(parent, j);
                if (rootI == rootJ) {
                    int direct = directTouches.getOrDefault(key, 0);
                    Set<Hex> straits = straitHexes.get(key);
                    int straitCount = (straits != null) ? straits.size() : 0;
                    int cidI = 0;
                    for (Hex h : nonIceLandmasses.get(i)) {
                        if (h.getContinentId() > 0) {
                            cidI = h.getContinentId();
                            break;
                        }
                    }
                    int cidJ = 0;
                    for (Hex h : nonIceLandmasses.get(j)) {
                        if (h.getContinentId() > 0) {
                            cidJ = h.getContinentId();
                            break;
                        }
                    }
                    boolean isRobust = (direct > 0)
                            || (cidI > 0 && cidJ > 0 && straitCount >= 2)
                            || (cidI <= 0 && cidJ <= 0 && straitCount >= 2);
                    if (isRobust && straits != null) {
                        straitsByRoot.computeIfAbsent(rootI, unused -> new HashSet<>()).addAll(straits);
                    }
                }
            }

            Map<Integer, List<Integer>> rootToLms = new HashMap<>();
            for (int i = 0; i < n; i++) {
                int root = findRoot(parent, i);
                rootToLms.computeIfAbsent(root, unused -> new ArrayList<>()).add(i);
            }

            for (Map.Entry<Integer, List<Integer>> entry : rootToLms.entrySet()) {
                int root = entry.getKey();
                List<Integer> group = entry.getValue();
                if (group.size() <= 1 && !straitsByRoot.containsKey(root)) {
                    continue;
                }

                int targetCid = 0;
                for (int idx : group) {
                    for (Hex h : nonIceLandmasses.get(idx)) {
                        if (h.getContinentId() > 0) {
                            if (targetCid == 0 || h.getContinentId() < targetCid) {
                                targetCid = h.getContinentId();
                            }
                        }
                    }
                }

                Set<Hex> straits = straitsByRoot.get(root);
                if (straits != null) {
                    for (Hex sHex : straits) {
                        if (sHex != null && !isLandHex(sHex)) {
                            bridgeStraitHexToLand(sHex, targetCid);
                        }
                    }
                }

                for (int idx : group) {
                    for (Hex h : nonIceLandmasses.get(idx)) {
                        if (targetCid > 0) {
                            h.setContinentId(targetCid);
                            h.setIsland(false);
                        } else {
                            h.setContinentId(0);
                            h.setIsland(true);
                        }
                    }
                }
            }
        }

        return anyFused;
    }

    public void classifyLandmasses() {
        this.continentCount = 0;
        this.islandCount = 0;
        for (Hex hex : this.hexes) {
            hex.setIsland(false);
        }

        Set<Hex> visited = new HashSet<>();
        for (Hex start : this.hexes) {
            if (!isLandHex(start) || visited.contains(start)) {
                continue;
            }
            List<Hex> component = new ArrayList<>();
            ArrayDeque<Hex> queue = new ArrayDeque<>();
            queue.add(start);
            visited.add(start);
            boolean touchesCore = false;
            boolean startIsIce = (start.getBiome() == GameConstants.BIOME_ICE_FIELD);
            while (!queue.isEmpty()) {
                Hex current = queue.poll();
                component.add(current);
                if (hexDistanceFromOrigin(current) <= this.continentCoreRadius) {
                    touchesCore = true;
                }
                for (Hex neighbor : current.getAdjacentNeighbors()) {
                    if (neighbor == null || !isLandHex(neighbor) || visited.contains(neighbor)) {
                        continue;
                    }
                    boolean neighborIsIce = (neighbor.getBiome() == GameConstants.BIOME_ICE_FIELD);
                    if (startIsIce != neighborIsIce) {
                        continue;
                    }
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }

            Set<Integer> existingIds = new HashSet<>();
            for (Hex hex : component) {
                if (hex.getContinentId() > 0) {
                    existingIds.add(hex.getContinentId());
                }
            }

            boolean isIceCap = false;
            int iceCount = 0;
            for (Hex hex : component) {
                if (hex.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                    iceCount++;
                }
            }
            boolean predominantlyIce = (iceCount >= component.size() * 0.60);
            if (existingIds.isEmpty()) {
                if (startIsIce || predominantlyIce) {
                    isIceCap = true;
                } else if (this.worldHeight > 0) {
                    int polarMargin = Math.max(2, (int) Math.round(this.worldHeight * 0.38));
                    boolean inPolarZone = true;
                    for (Hex h : component) {
                        if (Math.abs(h.getR()) < polarMargin) {
                            inPolarZone = false;
                            break;
                        }
                    }
                    if (inPolarZone) {
                        isIceCap = true;
                    }
                }
            } else if (predominantlyIce && this.worldHeight > 0) {
                int polarMargin = Math.max(2, (int) Math.round(this.worldHeight * 0.36));
                boolean inPolarZone = true;
                for (Hex h : component) {
                    if (Math.abs(h.getR()) < polarMargin) {
                        inPolarZone = false;
                        break;
                    }
                }
                if (inPolarZone) {
                    isIceCap = true;
                }
            }

            if (isIceCap) {
                for (Hex hex : component) {
                    hex.setIsland(false);
                    hex.setContinentId(0);
                }
                continue;
            }
            boolean isContinent = (this.worldWidth > 0 && this.worldHeight > 0)
                    ? (!existingIds.isEmpty())
                    : touchesCore;
            if (isContinent) {
                int cid = 0;
                for (int eid : existingIds) {
                    if (cid <= 0 || eid < cid) {
                        cid = eid;
                    }
                }
                if (cid <= 0) {
                    this.continentCount++;
                    cid = this.continentCount;
                }
                for (Hex hex : component) {
                    hex.setIsland(false);
                    hex.setContinentId(cid);
                }
            } else {
                this.islandCount++;
                for (Hex hex : component) {
                    hex.setIsland(true);
                    hex.setContinentId(0);
                }
            }
        }
        rebuildContinentsFromHexes();
    }

    private static int hexDistanceFromOrigin(Hex hex) {
        if (hex == null) {
            return Integer.MAX_VALUE;
        }
        int q = hex.getQ();
        int r = hex.getR();
        return (Math.abs(q) + Math.abs(q + r) + Math.abs(r)) / 2;
    }

    private int findRoot(int[] parent, int i) {
        int root = i;
        while (root != parent[root]) {
            root = parent[root];
        }
        int curr = i;
        while (curr != root) {
            int nxt = parent[curr];
            parent[curr] = root;
            curr = nxt;
        }
        return root;
    }

    private void unionRoots(int[] parent, int i, int j) {
        int rootI = findRoot(parent, i);
        int rootJ = findRoot(parent, j);
        if (rootI != rootJ) {
            parent[rootI] = rootJ;
        }
    }

    private int distanceBetweenHexes(Hex h1, Hex h2) {
        if (h1 == null || h2 == null) return Integer.MAX_VALUE;
        if (this.worldWidth <= 0) {
            return GameNumbers.axialHexDistance(h1.getQ(), h1.getR(), h2.getQ(), h2.getR());
        }
        int r1 = h1.getR();
        int r2 = h2.getR();
        int col1 = h1.getQ() + (r1 >> 1);
        int col2 = h2.getQ() + (r2 >> 1);
        int dCol = Math.abs(col1 - col2);
        if (dCol > this.worldWidth / 2) {
            dCol = this.worldWidth - dCol;
        }
        int q1 = h1.getQ();
        int q2A = col2 - (r2 >> 1);
        int q2B = (col2 + this.worldWidth) - (r2 >> 1);
        int q2C = (col2 - this.worldWidth) - (r2 >> 1);
        int distA = GameNumbers.axialHexDistance(q1, r1, q2A, r2);
        int distB = GameNumbers.axialHexDistance(q1, r1, q2B, r2);
        int distC = GameNumbers.axialHexDistance(q1, r1, q2C, r2);
        return Math.min(distA, Math.min(distB, distC));
    }

    private void convertLakesSurroundedByIce() {
        if (this.hexes == null) return;
        int polarCutoff = Math.max(2, (int) Math.round(this.worldHeight * 0.38));
        Set<Hex> visitedLakes = new HashSet<>();
        List<List<Hex>> lakeComponents = new ArrayList<>();
        for (Hex h : this.hexes) {
            if (h != null && h.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(h)) {
                List<Hex> comp = new ArrayList<>();
                ArrayDeque<Hex> q = new ArrayDeque<>();
                q.add(h);
                comp.add(h);
                while (!q.isEmpty()) {
                    Hex cur = q.poll();
                    for (Hex n : cur.getAdjacentNeighbors()) {
                        if (n != null && n.getBiome() == GameConstants.BIOME_LAKE && visitedLakes.add(n)) {
                            comp.add(n);
                            q.add(n);
                        }
                    }
                }
                lakeComponents.add(comp);
            }
        }

        for (List<Hex> comp : lakeComponents) {
            boolean hasNonIceNeighbor = false;
            boolean hasIceNeighbor = false;
            for (Hex lakeHex : comp) {
                for (Hex n : lakeHex.getAdjacentNeighbors()) {
                    if (n != null && n.getBiome() != GameConstants.BIOME_LAKE) {
                        boolean isPolarIceOrCap = (n.getBiome() == GameConstants.BIOME_ICE_FIELD)
                                || (this.worldHeight > 0 && Math.abs(n.getR()) >= polarCutoff && (n.getBiome() == GameConstants.BIOME_PEAK || n.getBiome() == GameConstants.BIOME_MOUNTAIN));
                        if (isPolarIceOrCap) {
                            hasIceNeighbor = true;
                        } else {
                            hasNonIceNeighbor = true;
                            break;
                        }
                    }
                }
                if (hasNonIceNeighbor) break;
            }
            boolean surroundedByIce = hasIceNeighbor && !hasNonIceNeighbor;
            if (surroundedByIce) {
                boolean onLandOrContinent = false;
                for (Hex lakeHex : comp) {
                    if (lakeHex.getContinentId() > 0 || lakeHex.isIsland()) {
                        onLandOrContinent = true;
                        break;
                    }
                }
                if (!onLandOrContinent) {
                    for (Hex lakeHex : comp) {
                        lakeHex.setBiome(GameConstants.BIOME_ICE_FIELD);
                        lakeHex.setLocalWeather(getRandomWeather(GameConstants.BIOME_ICE_FIELD));
                        lakeHex.setIsland(false);
                        lakeHex.setContinentId(0);
                        for (int d = 0; d < 6; d++) {
                            lakeHex.setRiverEdge(d, false);
                            Hex n = lakeHex.getNeighbor(d);
                            if (n != null) {
                                n.setRiverEdge((d + 3) % 6, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canReachOceanWithoutIce(List<Hex> comp) {
        if (comp == null || comp.isEmpty()) return false;
        ArrayDeque<Hex> q = new ArrayDeque<>();
        Set<Hex> visited = new HashSet<>(comp);
        for (Hex h : comp) {
            for (Hex n : h.getAdjacentNeighbors()) {
                if (n != null && n.getBiome() != GameConstants.BIOME_ICE_FIELD && visited.add(n)) {
                    q.add(n);
                }
            }
        }
        while (!q.isEmpty()) {
            Hex cur = q.poll();
            if (isOceanHex(cur)) {
                return true;
            }
            for (Hex n : cur.getAdjacentNeighbors()) {
                if (n != null && n.getBiome() != GameConstants.BIOME_ICE_FIELD && visited.add(n)) {
                    q.add(n);
                }
            }
        }
        return false;
    }

    private void clearRiversOnIce() {
        if (this.hexes == null) return;
        for (Hex h : this.hexes) {
            if (h == null) continue;
            if (h.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                for (int d = 0; d < 6; d++) {
                    h.setRiverEdge(d, false);
                    Hex n = h.getNeighbor(d);
                    if (n != null) {
                        n.setRiverEdge((d + 3) % 6, false);
                    }
                }
            } else {
                for (int d = 0; d < 6; d++) {
                    Hex n = h.getNeighbor(d);
                    if (n != null && n.getBiome() == GameConstants.BIOME_ICE_FIELD) {
                        h.setRiverEdge(d, false);
                        n.setRiverEdge((d + 3) % 6, false);
                    }
                }
            }
        }
    }

    private boolean isLandHex(Hex hex) {
        return hex != null && !isWaterBiome(hex.getBiome());
    }

    private List<Hex> selectNpcColonyHexes(List<Hex> eligible) {
        List<Hex> selected = new ArrayList<>();
        List<Hex> remaining = new ArrayList<>();
        for (Hex hex : eligible) {
            if (GameRandom.nextInt(100) < 30) {
                selected.add(hex);
            } else {
                remaining.add(hex);
            }
        }

        int required = GameConstants.getWorldSpawnableNpcSpecies().size();
        Collections.shuffle(remaining, GameRandom.getShuffleRandom());
        for (Hex hex : remaining) {
            if (selected.size() >= required) {
                break;
            }
            selected.add(hex);
        }
        return selected;
    }

    private List<AntSpecies> assignNpcSpecies(int colonyCount) {
        List<AntSpecies> pool = new ArrayList<>(GameConstants.getWorldSpawnableNpcSpecies());
        if (colonyCount == 0 || pool.isEmpty()) {
            return List.of();
        }
        int targetCount = Math.max(colonyCount, pool.size() * 4);
        List<AntSpecies> assignment = new ArrayList<>(targetCount);
        for (int p = 0; p < 2; p++) {
            List<AntSpecies> pass = new ArrayList<>(pool);
            Collections.shuffle(pass, GameRandom.getShuffleRandom());
            for (AntSpecies s : pass) {
                assignment.add(s);
                assignment.add(s);
            }
        }

        while (assignment.size() < targetCount) {
            Collections.shuffle(pool, GameRandom.getShuffleRandom());
            for (AntSpecies species : pool) {
                if (assignment.size() >= targetCount) {
                    break;
                }
                assignment.add(species);
                if (assignment.size() < targetCount) {
                    assignment.add(species);
                }
            }
        }
        return assignment;
    }
    
    private boolean isWaterBiome(Biome biome) {
        return biome == GameConstants.BIOME_OCEAN
                || biome == GameConstants.BIOME_DEEP_OCEAN
                || biome == GameConstants.BIOME_SHALLOW_OCEAN
                || biome == GameConstants.BIOME_ABYSS
                || biome == GameConstants.BIOME_LAKE;
    }
    
    public void linkNeighbors(Map<String, Hex> hexMap) {
        for (Hex hex : hexMap.values()) {
            int q = hex.getQ();
            int r = hex.getR();

            hex.setNorth(getNeighbor(hexMap, q, r, 0, -1));
            hex.setNorthEast(getNeighbor(hexMap, q, r, 1, -1));
            hex.setSouthEast(getNeighbor(hexMap, q, r, 1, 0));
            hex.setSouth(getNeighbor(hexMap, q, r, 0, 1));
            hex.setSouthWest(getNeighbor(hexMap, q, r, -1, 1));
            hex.setNorthWest(getNeighbor(hexMap, q, r, -1, 0));
        }
    }

    private Hex getNeighbor(Map<String, Hex> hexMap, int q, int r, int dq, int dr) {
        int nr = r + dr;
        if (this.worldWidth > 0 && this.worldHeight > 0) {
            int minR = -this.worldHeight / 2;
            int maxR = this.worldHeight / 2 - 1;
            if (nr < minR || nr > maxR) {
                return null;
            }
            int col = q + (r >> 1);
            int ncol = (q + dq) + (nr >> 1);
            int minCol = -this.worldWidth / 2;
            int maxCol = this.worldWidth / 2 - 1;
            while (ncol < minCol) ncol += this.worldWidth;
            while (ncol > maxCol) ncol -= this.worldWidth;
            int nq = ncol - (nr >> 1);
            return hexMap.get(nq + "," + nr);
        }
        return hexMap.get((q + dq) + "," + nr);
    }
    
    private Biome getBiomeForRing(int ring) {
        List<Biome> options = new ArrayList<>();
        
        switch (ring) {
            case 1:
                options.add(GameConstants.BIOME_PLAINS);
                options.add(GameConstants.BIOME_FOREST);
                options.add(GameConstants.BIOME_JUNGLE);
                break;
            case 2:
                options.add(GameConstants.BIOME_FOREST);
                options.add(GameConstants.BIOME_JUNGLE);
                options.add(GameConstants.BIOME_SWAMP);
                break;
            case 3:
                options.add(GameConstants.BIOME_JUNGLE);
                options.add(GameConstants.BIOME_SWAMP);
                options.add(GameConstants.BIOME_DESERT);
                break;
            case 4:
                options.add(GameConstants.BIOME_SWAMP);
                options.add(GameConstants.BIOME_DESERT);
                options.add(GameConstants.BIOME_TAIGA);
                options.add(GameConstants.BIOME_URBAN);
                break;
            case 5:
                options.add(GameConstants.BIOME_TAIGA);
                options.add(GameConstants.BIOME_TUNDRA);
                options.add(GameConstants.BIOME_DESERT);
                options.add(GameConstants.BIOME_URBAN);
                options.add(GameConstants.BIOME_LAKE);
                break;
            case 6:
                options.add(GameConstants.BIOME_TUNDRA);
                options.add(GameConstants.BIOME_DESERT);
                options.add(GameConstants.BIOME_MOUNTAIN);
                options.add(GameConstants.BIOME_URBAN);
                options.add(GameConstants.BIOME_LAKE);
                break;
            case 7:
                options.add(GameConstants.BIOME_TUNDRA);
                options.add(GameConstants.BIOME_DESERT);
                options.add(GameConstants.BIOME_MOUNTAIN);
                options.add(GameConstants.BIOME_URBAN);
                options.add(GameConstants.BIOME_VOLCANIC);
                break;
            default:
                options.add(GameConstants.BIOME_OCEAN);
                break;
        }
        
        return pickBiomeForRing(ring, options);
    }

    private Biome pickBiomeForRing(int ring, List<Biome> options) {
        if (options.isEmpty()) {
            return GameConstants.BIOME_PLAINS;
        }
        int targetDifficulty = Math.min(5, Math.max(1, ring / 2 + 1));
        List<Biome> weighted = new ArrayList<>();
        for (Biome biome : options) {
            int distance = Math.abs(biome.getDifficulty() - targetDifficulty);
            int weight = Math.max(1, 4 - distance);
            for (int i = 0; i < weight; i++) {
                weighted.add(biome);
            }
        }
        return weighted.get(GameRandom.nextInt(weighted.size()));
    }
    
    public Biome getBiomeById(int id) {
        for(Biome b : GameConstants.getBiomes()) {
            if (b.getId() == id) return b;
        }
        return GameConstants.BIOME_PLAINS;
    }
    
    public Weather getWeatherById(int id) {
        for (Weather w : GameConstants.getWeathers()) {
            if (w.getId() == id) return w;
        }
        return GameConstants.WEATHER_CLEAR;
    }
    
    private Weather getRandomWeather(Biome biome) {
        if (biome != null) {
            if (biome == GameConstants.BIOME_DESERT && GameRandom.nextInt(100) < 20) {
                return GameConstants.WEATHER_SAND_STORM;
            }
            if (biome == GameConstants.BIOME_VOLCANIC && GameRandom.nextInt(100) < 30) {
                return GameConstants.WEATHER_PYROCLASTIC_FOG;
            }
            if (biome == GameConstants.BIOME_URBAN && GameRandom.nextInt(100) < 15) {
                return GameConstants.WEATHER_ACID_RAIN;
            }
        }

        return STANDARD_RANDOM_WEATHERS.get(GameRandom.nextInt(STANDARD_RANDOM_WEATHERS.size()));
    }

    public void startWorld(Biome biome, Colony colony, String baseName) {
        startWorld(biome, colony, baseName, LanguageStrings.DYNASTY_TITLE_DYNASTY);
    }

    public void startWorld(Biome biome, Colony colony, String baseName, String playerTitleKey) {
        System.out.println("[World] startWorld called. Colony ants before init: " + colony.getAntTotal());

        if (colony.getAntTotal() == 0) {
            ColonyStarterService starterService = ColonyStarterService.shared();
            starterService.initializeNewColony(colony);
        }
        
        System.out.println("[World] Colony ants after init: " + colony.getAntTotal());

        baseName = formatName(baseName);
        generateRectangularWorld(biome, colony, baseName, playerTitleKey);
        changeActiveHex(getSpawnHex()); 
        updateEnvironmentalConditions();
    }

    public void relocalizeDynastyNames() {
        if (dynastys == null) {
            return;
        }
        for (Dynasty dynasty : dynastys) {
            dynasty.applyLocalizedName();
        }
    }

    public void loadWorld(Savefile savefile) {
        this.minute = savefile.getMinute();
        this.hour = savefile.getHour();
        this.day = savefile.getDay();
        this.month = savefile.getMonth();
        this.year = savefile.getYear();
        this.spectatorMode = savefile.isSpectatorMode();
        if (!savefile.allowsAchievements()) {
            disableAchievements();
        }
        boolean isRectangularSave = savefile.getWorldWidth() > 0
                || (savefile.getWorldHexes() == null || savefile.getWorldHexes().isEmpty())
                && !(savefile.getContinentCoreRadius() > 0 && savefile.getWorldWidth() == 0);
        this.worldRadius = (savefile.getWorldRadius() > 0)
                ? savefile.getWorldRadius()
                : (isRectangularSave ? 32 : GameNumbers.worldRadiusForContinentCore(GameNumbers.WORLD_DEFAULT_CONTINENT_CORE_RADIUS));
        this.worldWidth = isRectangularSave
                ? (savefile.getWorldWidth() > 0 ? savefile.getWorldWidth() : GameNumbers.WORLD_RECT_WIDTH)
                : 0;
        this.worldHeight = isRectangularSave
                ? (savefile.getWorldHeight() > 0 ? savefile.getWorldHeight() : GameNumbers.WORLD_RECT_HEIGHT)
                : 0;
        this.continentCoreRadius = isRectangularSave
                ? 0
                : (savefile.getContinentCoreRadius() > 0 ? savefile.getContinentCoreRadius() : this.worldRadius);
        
        String playerTitleKey = GameConstants.getDynastyTitleById(savefile.resolvePlayerDynastyTitleId()).getNameKey();

        String baseName = "Player";
        if (savefile.getName() != null && !savefile.getName().trim().isEmpty()) {
            String sName = savefile.getName().trim();
            if (!LanguageStrings.isGenericSaveName(sName, savefile.getId())) {
                baseName = LanguageStrings.resolvePlayerThemeName(sName, savefile.resolvePlayerDynastyTitleId());
            }
        }
        baseName = formatName(baseName);

        this.hexes.clear();
        this.hexCoordCache.clear();
        markColonizedHexIndexDirty();
        this.continents.clear();
        this.dynastys.clear();
        this.historyService.clear();
        this.historyService.setRecordingEnabled(false);
        Map<String, Hex> hexMap = new HashMap<>();
        Map<String, Colony> loadedColonies = new HashMap<>();
        Map<Integer, Dynasty> loadedDynastys = new HashMap<>();
        
        int maxColId = 0;
        int maxDynastyId = 0;

        if (savefile.getDynastys() != null) {
            for (Savefile.SavedDynasty sc : savefile.getDynastys()) {
                Dynasty dynasty = new Dynasty(sc);
                loadedDynastys.put(dynasty.getId(), dynasty);
                registerDynasty(dynasty);
                namingService.registerUsedName(dynasty.getName());
                if (dynasty.getThemeBase() != null && !dynasty.getThemeBase().isEmpty()) {
                    namingService.registerUsedThemeKey(dynasty.getThemeBase());
                }
                if (dynasty.getId() > maxDynastyId) maxDynastyId = dynasty.getId();
            }
        }
        
        if (savefile.getColonies() != null) {
            for (Savefile.SavedColony sc : savefile.getColonies()) {
                Colony c = new Colony(sc);
                String key = sc.q + "," + sc.r;
                loadedColonies.put(key, c);
                if (c.getId() > maxColId) maxColId = c.getId();
                
                if (loadedDynastys.containsKey(sc.dynastyId)) {
                    loadedDynastys.get(sc.dynastyId).addColony(c);
                } else {
                    int newDynastyId = ++maxDynastyId;
                    DynastyTitle adHocTitle = namingService.pickRandomTitle();
                    String dynName = c.isPlayer()
                            ? LanguageStrings.formatDynastyName(baseName, playerTitleKey)
                            : LanguageStrings.formatWildDynastyName(adHocTitle);
                    namingService.registerUsedName(dynName);
                    Dynasty adHocDynasty = new Dynasty(
                            newDynastyId,
                            dynName,
                            c.isPlayer() ? playerTitleKey : adHocTitle.getNameKey(),
                            c.isPlayer(),
                            GameConstants.SPECIES_OMNI);
                    adHocDynasty.getStarterService().initializeDynasty(adHocDynasty);
                    if (!c.isPlayer()) {
                        adHocDynasty.setWildDynasty(true);
                    }
                    adHocDynasty.addColony(c);
                    registerDynasty(adHocDynasty);
                    loadedDynastys.put(newDynastyId, adHocDynasty);
                    System.out.println("[World] Created ad-hoc Dynasty ID " + newDynastyId + " for orphan colony " + c.getName());
                }
                
                c.refreshAntStats(); 
            }
        }

        if (savefile.getWorldHexes() != null && !savefile.getWorldHexes().isEmpty()) {
            for (Savefile.SavedHex sh : savefile.getWorldHexes()) {
                Hex hex = new Hex();
                hex.setQ(sh.q);
                hex.setR(sh.r);
                hex.setBiome(getBiomeById(sh.biomeId));
                hex.setTimeOffset(GameNumbers.calculateTimeZoneOffset(sh.q + (sh.r >> 1), this.worldWidth));
                hex.setLocalWeather(getWeatherById(sh.weatherId));
                hex.setActive(false); 
                
                String key = sh.q + "," + sh.r;
                if (loadedColonies.containsKey(key)) {
                    hex.setColony(loadedColonies.get(key));
                } else if (sh.hasColony) {
                    hex.setColony(null);
                } else {
                    hex.setColony(null);
                }

                hex.setNonWaterResourceSourcesGenerated(sh.nonWaterResourceSourcesGenerated);
                if (sh.riverEdges != null) {
                    for (int i = 0; i < Math.min(6, sh.riverEdges.length); i++) {
                        hex.setRiverEdge(i, sh.riverEdges[i]);
                    }
                }
                hex.setContinentId(sh.continentId);
                hex.setWaterDistance(sh.waterDistance);
                hex.setCoastal(sh.coastal);
                hex.setElevation(sh.elevation);
                hex.setHydrology(sh.hydrology);
                hex.setIsland(sh.island);
                hex.setPlateId(sh.plateId);
                
                hexMap.put(sh.q + "," + sh.r, hex);
                this.hexes.add(hex);
            }
            linkNeighbors(hexMap);
            classifyLandmasses();
            ensureFaultLinesReconstructed();
            System.out.println("[World] Loaded world grid from savefile (" + this.hexes.size() + " hexes, " + loadedColonies.size() + " colonies).");
            System.out.println("[World] Landmasses: " + this.continentCount + " continent(s), "
                    + this.islandCount + " island(s).");
            if (loadedColonies.isEmpty()) {
                this.historyService.setRecordingEnabled(true);
                Colony colony = null;
                if (!this.spectatorMode) {
                    String playerCapName = namingService.generateCapitalName(baseName);
                    colony = new Colony(1, playerCapName, true);
                    ColonyStarterService starter = ColonyStarterService.shared();
                    starter.initializeNewColony(colony);
                }
                int starterBiomeId = savefile.getStarterBiomeId() > 0 ? savefile.getStarterBiomeId() : GameConstants.BIOME_PLAINS.getId();
                Biome chosenBiome = getBiomeById(starterBiomeId);
                if (chosenBiome == null) chosenBiome = GameConstants.BIOME_PLAINS;
                populateDynasties(chosenBiome, colony, baseName, playerTitleKey);
            }
        } else {
            System.out.println("[World] No map data in save (or old save version). Generating fresh world map for existing colony.");
            this.historyService.setRecordingEnabled(true);
            
            Colony colony = null;
            if (!this.spectatorMode) {
                for (Colony c : loadedColonies.values()) {
                    if (c.isPlayer()) {
                        colony = c;
                        break;
                    }
                }
                 
                if (colony == null) {
                    String playerCapName = namingService.generateCapitalName(baseName);
                    colony = new Colony(1, playerCapName, true);
                }
                 
                if (colony.getAntTotal() == 0) {
                    ColonyStarterService starter = ColonyStarterService.shared();
                    starter.initializeNewColony(colony);
                }
            }

            int starterBiomeId = savefile.getStarterBiomeId() > 0 ? savefile.getStarterBiomeId() : GameConstants.BIOME_PLAINS.getId();
            Biome chosenBiome = getBiomeById(starterBiomeId);
            if (chosenBiome == null) {
                chosenBiome = GameConstants.BIOME_PLAINS;
            }
            if (isRectangularSave) {
                generateRectangularWorld(chosenBiome, colony, baseName, playerTitleKey);
            } else {
                int coreRadius = this.continentCoreRadius > 0
                        ? this.continentCoreRadius
                        : GameNumbers.WORLD_DEFAULT_CONTINENT_CORE_RADIUS;
                generateWorld(chosenBiome, coreRadius, colony, baseName, playerTitleKey);
            }
        }

        if (savefile.getDynastys() != null) {
            for (Savefile.SavedDynasty sd : savefile.getDynastys()) {
                Dynasty d = loadedDynastys.get(sd.id);
                if (d != null && sd.tunnels != null) {
                    for (Savefile.SavedTunnel st : sd.tunnels) {
                        Hex hA = getHexAt(st.qA, st.rA);
                        Hex hB = getHexAt(st.qB, st.rB);
                        if (hA != null && hB != null) {
                            Tunnel tunnel = new Tunnel(hA, hB, st.totalCost);
                            tunnel.restoreState(st.progress, st.isComplete);
                            d.addTunnel(tunnel);
                            
                            if (!st.isComplete) {
                                Colony sponsor = null;
                                if (hA.getColony() != null && hA.getColony().getDynasty() == d) {
                                    sponsor = hA.getColony();
                                } else if (hB.getColony() != null && hB.getColony().getDynasty() == d) {
                                    sponsor = hB.getColony();
                                }
                                if (sponsor != null && sponsor.getCurrentTunnelProject() == null) {
                                    sponsor.setCurrentTunnelProject(tunnel);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (engine != null && engine.getTradeManager() != null) {
            if (savefile.getTrades() != null) {
            for (Savefile.SavedTrade st : savefile.getTrades()) {
                Hex hO = getHexAt(st.qOrigin, st.rOrigin);
                Hex hD = getHexAt(st.qDest, st.rDest);
                if (hO != null && hD != null) {
                    Map<ResourceType, Double> load = new HashMap<>();
                    putResourceLoad(st.load, load);
                    Map<ResourceType, Double> returnLoad = new HashMap<>();
                    putResourceLoad(st.returnLoad, returnLoad);
                    Map<AntClass, Integer> trans = new HashMap<>();
                    putAntTransport(st.transport, trans);
                    TradeMethod method = GameConstants.getTradeMethodById(st.methodId);
                    if (method == null) {
                        method = GameConstants.METHOD_LAND;
                    }

                    Trade trade = new Trade(hO, hD, load, returnLoad, trans, st.isRecurrent, st.isBilateral, method);
                    trade.setActive(st.isActive);

                    if (st.hasPendingUpdate) {
                        Map<ResourceType, Double> pLoad = new HashMap<>();
                        putResourceLoad(st.pendingLoad, pLoad);
                        Map<ResourceType, Double> pReturnLoad = new HashMap<>();
                        putResourceLoad(st.pendingReturnLoad, pReturnLoad);
                        Map<AntClass, Integer> pTrans = new HashMap<>();
                        putAntTransport(st.pendingTransport, pTrans);
                        TradeMethod pMethod = GameConstants.getTradeMethodById(st.pendingMethodId);
                        if (pMethod == null) {
                            pMethod = method;
                        }
                        trade.setPendingUpdate(pLoad, pReturnLoad, pTrans, st.pendingRecurrent, st.pendingIsBilateral, pMethod);
                    }

                    trade.restoreTripState(st.totalHours, st.remainingHours, st.isReturning);
                    engine.getTradeManager().addTrade(trade);
                }
            }
            }
        }

        this.colonyIdCounter = Math.max(this.colonyIdCounter, maxColId + 1);
        this.dynastyIdCounter = Math.max(this.dynastyIdCounter, maxDynastyId + 1);
        
        if (savefile.getDynastys() != null) {
            for (Savefile.SavedDynasty sd : savefile.getDynastys()) {
                Dynasty d = loadedDynastys.get(sd.id);
                if (d != null && sd.capitalColonyId != -1) {
                    for (Colony c : d.getColonies()) {
                        if (c.getId() == sd.capitalColonyId) {
                            d.setCapital(c);
                            break;
                        }
                    }
                }
                if (d != null) {
                    d.resolveCapitalFromColonies();
                }
            }
        }

        DynastySynergyService.refreshAll(this.dynastys);

        reapplyRoleAssignmentsAfterLoad();
        markColonizedHexIndexDirty();
        bindDynastyTradeServices();
        ColonyMilitaryService.refreshAllMilitaryPower(this.dynastys);
        if (savefile.getWars() != null) {
            warService.loadFromSave(savefile.getWars());
        } else {
            warService.syncFromDynasties();
        }
        warService.pruneInvalidWars();
        if (savefile.getWorldHistory() != null && savefile.getDynastys() != null && !savefile.getDynastys().isEmpty()) {
            historyService.loadFromSave(savefile.getWorldHistory());
        }
        historyService.setRecordingEnabled(true);

        ensurePlayerDynastyHasQueenAfterLoad();
        relocalizeDynastyNames();
        changeActiveHex(getSpawnHex());
        updateEnvironmentalConditions();
    }

    private void ensurePlayerDynastyHasQueenAfterLoad() {
        for (Dynasty dynasty : getDynastys()) {
            if (dynasty == null || !dynasty.isPlayer() || dynasty.isDefeated()) {
                continue;
            }
            boolean hasQueen = false;
            for (Colony c : dynasty.getColonies()) {
                if (c != null && !c.getQueens().isEmpty()) {
                    hasQueen = true;
                    break;
                }
            }
            if (!hasQueen) {
                Colony target = dynasty.getCapital() != null ? dynasty.getCapital() : (dynasty.getColonies().isEmpty() ? null : dynasty.getColonies().get(0));
                if (target != null) {
                    Ant freeQueen = new Ant(target, GameConstants.CLASS_QUEEN);
                    freeQueen.setDimension(WorldSpaces.UNDERWORLD);
                    freeQueen.setRole(GameConstants.ROLE_LAYER);
                    target.getQueens().add(freeQueen);
                    target.setDaysWithoutQueen(0);
                }
            }
        }
    }

    private void reapplyRoleAssignmentsAfterLoad() {
        if (this.engine == null) {
            return;
        }
        for (Hex hex : colonizedHexesForTicks()) {
            if (hex.getColony() != null) {
                hex.getColony().runRoleAssignment(this.engine);
            }
        }
    }

    private static long hexCoordKey(int q, int r) {
        return (((long) q) << 32) | (r & 0xFFFFFFFFL);
    }

    public Hex getHexAt(int q, int r) {
        if (hexes == null || hexes.isEmpty()) return null;
        long key = hexCoordKey(q, r);
        Hex cached = hexCoordCache.get(key);
        if (cached != null) {
            return cached;
        }
        for (Hex h : hexes) {
            if (h.getQ() == q && h.getR() == r) {
                hexCoordCache.put(key, h);
                return h;
            }
        }
        return null;
    }
    
    private void updateEnvironmentalConditions() {
        if (this.activeHex == null || this.activeHex.getBiome() == null) {
            return;
        }

        Biome baseBiome = this.activeHex.getBiome();
        float calcTemp = baseBiome.getTemperature();
        float calcHumid = baseBiome.isIsHumid();

        if (this.season != null) {
            calcTemp *= this.season.getTempMult();
            calcHumid *= this.season.getHumidityMult();
        }

        if (this.timeOfDay != null) {
            calcTemp *= this.timeOfDay.getTempMult();
        }

        Weather localW = this.activeHex.getLocalWeather();
        if (localW == null) localW = this.weather;
        
        if (localW != null) {
            calcTemp *= localW.getTempMult();
            calcHumid += localW.getHumidMult(); 
        }

        this.temperature = Math.round(calcTemp);

        int finalHumid = Math.round(calcHumid);
        if (finalHumid < 0) finalHumid = 0;
        if (finalHumid > 5) finalHumid = 5;
        this.humidity = finalHumid;
    }

    private void randomizeWeather() {
        if (GameRandom.nextInt(1000) == 0) {
            if (GameRandom.nextBoolean()) {
                this.setWeather(GameConstants.WEATHER_FROG);
            } else {
                this.setWeather(GameConstants.WEATHER_BLOOD);
            }
        } else {
            List<Weather> possibleWeathers = new ArrayList<>();
            possibleWeathers.add(GameConstants.WEATHER_CLEAR);
            possibleWeathers.add(GameConstants.WEATHER_CLEAR);
            if (this.season == GameConstants.SEASON_WINTER) {
                possibleWeathers.add(GameConstants.WEATHER_SNOW);
                possibleWeathers.add(GameConstants.WEATHER_HEAVY_SNOW);
                possibleWeathers.add(GameConstants.WEATHER_WIND);
            } else if (this.season == GameConstants.SEASON_SUMMER) {
                possibleWeathers.add(GameConstants.WEATHER_RAIN);
                possibleWeathers.add(GameConstants.WEATHER_THUNDER);
                possibleWeathers.add(GameConstants.WEATHER_HEAT);
            } else {
                possibleWeathers.add(GameConstants.WEATHER_RAIN);
                possibleWeathers.add(GameConstants.WEATHER_HEAVY_RAIN);
                possibleWeathers.add(GameConstants.WEATHER_WIND);
            }
            Weather newWeather = possibleWeathers.get(GameRandom.nextInt(possibleWeathers.size()));
            if (this.weather != newWeather) {
                this.setWeather(newWeather);
            }
        }
        
        for (Hex h : colonizedHexesForTicks()) {
            maybeRandomizeHexWeather(h);
            maybeRandomizeHexWeather(h.getNorth());
            maybeRandomizeHexWeather(h.getNorthEast());
            maybeRandomizeHexWeather(h.getSouthEast());
            maybeRandomizeHexWeather(h.getSouth());
            maybeRandomizeHexWeather(h.getSouthWest());
            maybeRandomizeHexWeather(h.getNorthWest());
        }
    }

    private void maybeRandomizeHexWeather(Hex hex) {
        if (hex != null && GameRandom.nextInt(100) < 5) {
            hex.setLocalWeather(getRandomWeather(hex.getBiome()));
        }
    }

    public void runMinute() {
        try (SimulationDiagnostics.TimedSection section = SimulationDiagnostics.start(Scope.WORLD_MINUTE)) {
            this.minute++;

            if (!this.spectatorMode) {
                int hexCount = colonizedHexesForTicks().size();
                SimulationDiagnostics.addCounter(Counter.HEXES_SCANNED_MINUTE, hexCount);

                long colonyNanos = 0L;
                int colonies = 0;
                boolean timingColonies = SimulationDiagnostics.isEnabled();
                for (Hex hex : colonizedHexesForTicks()) {
                    if (hex == null || hex.getColony() == null) {
                        continue;
                    }
                    colonies++;
                    long colonyStart = timingColonies ? System.nanoTime() : 0L;
                    hex.getColony().runMinutelyJobs();
                    if (timingColonies) {
                        colonyNanos += System.nanoTime() - colonyStart;
                    }
                }
                SimulationDiagnostics.addCounter(Counter.COLONIES_MINUTE, colonies);
                SimulationDiagnostics.record(Scope.COLONY_MINUTELY_TOTAL, colonyNanos);
            }

            if (this.minute > 59) {
                this.minute = 0;
                this.runHour();
            }
        }
    }

    public void runHour() {
        try (SimulationDiagnostics.TimedSection section = SimulationDiagnostics.start(Scope.WORLD_HOUR)) {
            this.hour++;

            List<Hex> activeColonized = colonizedHexesForTicks();
            int hexCount = activeColonized.size();
            SimulationDiagnostics.addCounter(Counter.HEXES_SCANNED_HOUR, hexCount);

            runContinentHourlyJobsParallel(activeColonized);

            boolean isEclipse = (this.timeOfDay == GameConstants.TIME_SOLAR_ECLIPSE ||
                                 this.timeOfDay == GameConstants.TIME_LUNAR_ECLIPSE);

            if (!isEclipse) {
                this.setTimeOfDay(GameConstants.getTimeOfDayForHour(this.hour));
            }

            updateEnvironmentalConditions();

            if (engine != null) {
                engine.notifyHourListeners();
            }

            warService.tickWarProgressHourly();

            if (this.hour > 23) {
                this.hour = 0;
                this.runDay();
            }
        }
    }

    private void runContinentHourlyJobsParallel(List<Hex> hexList) {
        List<Continent> activeLandmasses = new ArrayList<>();
        for (Continent c : getAllLandmasses()) {
            if (c != null && c.hasPopulatedHexes()) {
                activeLandmasses.add(c);
            }
        }
        if (activeLandmasses.isEmpty()) {
            if (hexList != null && !hexList.isEmpty()) {
                boolean timingColonies = SimulationDiagnostics.isEnabled();
                long colonyNanos = 0L;
                int colonies = 0;
                for (Hex hex : hexList) {
                    if (hex != null && hex.getColony() != null) {
                        colonies++;
                        long colonyStart = timingColonies ? System.nanoTime() : 0L;
                        hex.getColony().runHourlyJobs(hex.getBiome(), this.engine);
                        if (timingColonies) {
                            colonyNanos += System.nanoTime() - colonyStart;
                        }
                    }
                }
                SimulationDiagnostics.addCounter(Counter.COLONIES_HOUR, colonies);
                SimulationDiagnostics.record(Scope.COLONY_HOURLY_TOTAL, colonyNanos);
            }
            return;
        }

        int groupCount = activeLandmasses.size();
        Semaphore semaphore = new Semaphore(0);
        AtomicReference<Throwable> firstError = new AtomicReference<>(null);

        for (Continent landmass : activeLandmasses) {
            List<Hex> populatedHexes = landmass.getPopulatedHexes();
            CONTINENT_EXECUTOR.execute(() -> {
                try {
                    boolean timingColonies = SimulationDiagnostics.isEnabled();
                    long colonyNanos = 0L;
                    int colonies = 0;
                    for (Hex hex : populatedHexes) {
                        if (hex == null || hex.getColony() == null) {
                            continue;
                        }
                        colonies++;
                        long colonyStart = timingColonies ? System.nanoTime() : 0L;
                        hex.getColony().runHourlyJobs(hex.getBiome(), this.engine);
                        if (timingColonies) {
                            colonyNanos += System.nanoTime() - colonyStart;
                        }
                    }
                    SimulationDiagnostics.addCounter(Counter.COLONIES_HOUR, colonies);
                    SimulationDiagnostics.record(Scope.COLONY_HOURLY_TOTAL, colonyNanos);
                } catch (Throwable t) {
                    firstError.compareAndSet(null, t);
                } finally {
                    semaphore.release();
                }
            });
        }

        semaphore.acquireUninterruptibly(groupCount);

        Throwable error = firstError.get();
        if (error != null) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else if (error instanceof Error) {
                throw (Error) error;
            } else {
                throw new RuntimeException("Error during continent hourly jobs", error);
            }
        }
    }

    public void runDay() {
        try (SimulationDiagnostics.TimedSection section = SimulationDiagnostics.start(Scope.WORLD_DAY)) {
            this.day++;

            int dynastyCount = this.dynastys != null ? this.dynastys.size() : 0;
            SimulationDiagnostics.addCounter(Counter.DYNASTIES_DAY, dynastyCount);

            long dynastyNanos = 0L;
            boolean timingDynasties = SimulationDiagnostics.isEnabled();
            for (Dynasty dynasty : this.dynastys) {
                if (dynasty.isPlayer() || GameNumbers.runsNpcDailyWorkToday(this.day, dynasty.getId())) {
                    long dynastyStart = timingDynasties ? System.nanoTime() : 0L;
                    dynasty.runDailyJobs(this, engine != null ? engine.getTradeManager() : null);
                    if (timingDynasties) {
                        dynastyNanos += System.nanoTime() - dynastyStart;
                    }
                }
            }
            SimulationDiagnostics.record(Scope.DYNASTY_DAILY_TOTAL, dynastyNanos);

            int hexCount = colonizedHexesForTicks().size();
            SimulationDiagnostics.addCounter(Counter.HEXES_SCANNED_DAY, hexCount);

            long colonyNanos = 0L;
            int colonies = 0;
            boolean timingColonies = SimulationDiagnostics.isEnabled();
            for (Hex hex : colonizedHexesForTicks()) {
                if (hex == null || hex.getColony() == null) {
                    continue;
                }
                colonies++;
                Colony colony = hex.getColony();
                long colonyStart = timingColonies ? System.nanoTime() : 0L;
                colony.runDailyJobs(this.getTemperatureIcon(), hex.getBiome(), hex);
                tryQueenRecoveryNuptial(this, colony, hex);
                if (timingColonies) {
                    colonyNanos += System.nanoTime() - colonyStart;
                }
            }
            SimulationDiagnostics.addCounter(Counter.COLONIES_DAY, colonies);
            SimulationDiagnostics.record(Scope.COLONY_DAILY_TOTAL, colonyNanos);

            for (Dynasty dynasty : this.dynastys) {
                ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
            }

            warService.tickWarProgressDaily();
            DynastyIntegrationService.tickIntegrationsDaily(this, engine != null ? engine.getTradeManager() : null);

            DYNASTY_DEATH_SERVICE.processDynastyDeaths(this);

            randomizeWeather();

            if (GameRandom.nextInt(1000) == 0) {
                if (GameRandom.nextBoolean()) {
                    this.setTimeOfDay(GameConstants.TIME_SOLAR_ECLIPSE);
                } else {
                    this.setTimeOfDay(GameConstants.TIME_LUNAR_ECLIPSE);
                }

                for (Hex hex : colonizedHexesForTicks()) {
                    Colony colony = hex.getColony();
                    if (colonyHasActiveDynasty(colony)) {
                        colony.getLabourService().runNuptial(colony, this, hex);
                        colony.logEvent(ColonyLogPrefixes.NUPTIAL + " "
                            + LanguageStrings.get(LanguageStrings.EVENT_ECLIPSE_NUPTIAL));
                    }
                }

            } else {
                this.setTimeOfDay(GameConstants.TIME_NIGHT);
            }

            if (this.day >= 1 && this.day < 2) {
                this.moonPhase = GameConstants.PHASE_NEW_MOON;
            } else if (this.day >= 2 && this.day < 8) {
                this.moonPhase = GameConstants.PHASE_WAXING_CRESCENT;
            } else if (this.day >= 8 && this.day < 9) {
                this.moonPhase = GameConstants.PHASE_FIRST_QUARTER;
            } else if (this.day >= 9 && this.day < 15) {
                this.moonPhase = GameConstants.PHASE_WAXING_GIBBOUS;
            } else if (this.day >= 15 && this.day < 16) {
                this.moonPhase = GameConstants.PHASE_FULL_MOON;
            } else if (this.day >= 16 && this.day < 22) {
                this.moonPhase = GameConstants.PHASE_WANING_GIBBOUS;
            } else if (this.day >= 22 && this.day < 23) {
                this.moonPhase = GameConstants.PHASE_LAST_QUARTER;
            } else if (this.day >= 23 && this.day < 30) {
                this.moonPhase = GameConstants.PHASE_WANING_CRESCENT;
            } else {
                this.moonPhase = GameConstants.PHASE_NEW_MOON;
            }

            if (engine != null) {
                engine.notifyDayListeners();
            }

            if (this.day > 30) {
                this.day = 1;
                this.runMonth();
            }
        }
    }

    public void runMonth() {
        this.month++;
        Season monthSeason = GameConstants.seasonForMonth(this.month);

        for (Hex hex : colonizedHexesForTicks()) {
            if (hex == null) {
                continue;
            }
            Colony colony = hex.getColony();
            if (colonyHasActiveDynasty(colony)) {
                colony.runMonthlyJobs(monthSeason, hex.getBiome());
            }
        }

        TradeManager tradeManager = engine != null ? engine.getTradeManager() : null;
        DynastyRebellionService.runMonthlyChecks(this, tradeManager);

        if (this.dynastys != null) {
            for (Dynasty dynasty : this.dynastys) {
                if (dynasty == null || dynasty.isDefeated()) {
                    continue;
                }
                DynastyIntelligenceService intel = dynasty.getIntelligenceService();
                if (intel != null) {
                    intel.runMonthlyCounterIntelligence(this);
                }
            }
        }

        this.setSeason(monthSeason);

        try {
            if (this.engine != null) {
                int freq = this.engine.getAutosaveFrequency();
                if (freq > 0 && (this.month % freq == 0)) {
                    this.engine.getSaveManager().saveAutosave(this, this.engine);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (engine != null) {
            engine.notifyMonthListeners();
        }

        if (this.month > 12) {
            this.month = 1;
            this.runYear();
        }
    }

    public void runYear() {
        this.year++;
        
        for (Hex hex : colonizedHexesForTicks()) {
            Colony colony = hex.getColony();
            if (colonyHasActiveDynasty(colony)) {
                colony.runYearlyJobs(this, hex);
            }
        }
    }

    private void tryQueenRecoveryNuptial(World world, Colony colony, Hex hex) {
        if (!colonyHasActiveDynasty(colony) || hex == null || world == null) {
            return;
        }
        if (!colony.getQueens().isEmpty() || colony.getDaysWithoutQueen() <= 0) {
            return;
        }
        if (!colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            return;
        }
        if (!ColonyLabourService.meetsNuptialRequirements(colony)) {
            return;
        }
        colony.getLabourService().runNuptial(colony, world, hex);
    }

    private static boolean colonyHasActiveDynasty(Colony colony) {
        return colony != null && colony.getDynasty() != null && !colony.getDynasty().isDefeated();
    }

    private static void putResourceLoad(Map<Integer, Double> src, Map<ResourceType, Double> dest) {
        if (src == null) {
            return;
        }
        for (Map.Entry<Integer, Double> entry : src.entrySet()) {
            ResourceType resource = GameConstants.getResourceById(entry.getKey());
            if (resource != null) {
                dest.put(resource, entry.getValue());
            }
        }
    }

    private static void putAntTransport(Map<Integer, Integer> src, Map<AntClass, Integer> dest) {
        if (src == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> entry : src.entrySet()) {
            AntClass antClass = GameConstants.getAntClassById(entry.getKey());
            if (antClass != null) {
                dest.put(antClass, entry.getValue());
            }
        }
    }
}
