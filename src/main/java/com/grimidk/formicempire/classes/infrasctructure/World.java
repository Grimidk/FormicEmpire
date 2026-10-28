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
import com.grimidk.formicempire.classes.entities.world.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.entities.world.Tunnel;
import com.grimidk.formicempire.classes.entities.world.Continent;
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
        if (dynastys != null) {
            for (Dynasty dynasty : dynastys) {
                if (dynasty != null && dynasty.isPlayer()) {
                    return dynasty;
                }
            }
        }
        Hex spawn = getSpawnHex();
        if (spawn != null && spawn.getColony() != null) {
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
        if (!this.continents.isEmpty()) {
            this.continentCount = this.continents.size();
        }

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

        int archIndex = 0;
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

            archIndex++;
            int archId = 1000 + archIndex;
            String archKey = null;
            String archName = null;
            for (String k : LanguageStrings.ARCHIPELAGO_NAMES) {
                String candidateName = LanguageStrings.get(k);
                if (!usedKeysInWorld.contains(k) && !usedNamesInWorld.contains(candidateName)) {
                    archKey = k;
                    archName = candidateName;
                    break;
                }
            }
            if (archKey == null) {
                int idx = (archIndex - 1) % LanguageStrings.ARCHIPELAGO_NAMES.length;
                if (idx < 0) idx = 0;
                archKey = LanguageStrings.ARCHIPELAGO_NAMES[idx];
                String base = LanguageStrings.get(archKey);
                int num = 2;
                String candidate = base + " " + toRomanNumeral(num);
                while (usedNamesInWorld.contains(candidate)) {
                    num++;
                    candidate = base + " " + toRomanNumeral(num);
                }
                archName = candidate;
            }
            usedKeysInWorld.add(archKey);
            usedNamesInWorld.add(archName);

            Continent arch = new Continent(archId, archName, archKey);
            arch.setArchipelago(true);
            for (Hex h : comp) {
                h.setIsland(true);
                h.setContinentId(0);
                arch.addHex(h);
            }
            this.archipelagos.add(arch);
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
        if (this.activeHex.getColony() != null) {
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
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, GameNumbers.WORLD_RECT_HEIGHT);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, int height) {
        generateRectangularWorld(startBiome, startColony, baseName, playerTitleKey, 2 * height, height);
    }

    public void generateRectangularWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey, int width, int height) {
        int h = (height > 0) ? height : GameNumbers.WORLD_RECT_HEIGHT;
        int w = (width > 0) ? width : 2 * h;
        this.continentCoreRadius = 0;
        this.worldHeight = h;
        this.worldWidth = w;
        this.worldRadius = h;
        initAndGenerateWorld(startBiome, startColony, baseName, playerTitleKey);
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

    private void initAndGenerateWorld(Biome startBiome, Colony startColony, String baseName, String playerTitleKey) {
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
        if (startColony == null) {
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
                hex.setTimeOffset(col);
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
        generateMultiContinents(startBiome, startColony);

        // Step 16: Finalize indexing and bind dynasty trade services
        markColonizedHexIndexDirty();
        rebuildColonizedHexIndexIfNeeded();
        bindDynastyTradeServices();
        notifyStep(16, "Finalize World");
        System.out.println("[World] Landmasses: " + this.continentCount + " continent(s), "
                + this.islandCount + " island(s).");
    }

    private void generateMultiContinents(Biome startBiome, Colony startColony) {
        // Step 4: Seed and grow continental landmasses
        int numContinents = (this.targetContinentCount > 0)
                ? this.targetContinentCount
                : ((this.worldHeight < 25)
                        ? (2 + GameRandom.nextInt(2))
                        : (4 + GameRandom.nextInt(4)));
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

        while (round++ < maxRounds && consecutiveFails < 35) {
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
                boolean underMin = (consecutiveFails >= 15);

                for (Hex n : current.getAdjacentNeighbors()) {
                    if (n == null) continue;
                    if (n.getContinentId() != 0) continue;

                    boolean touchesOtherContinent = false;
                    for (Hex nn : n.getAdjacentNeighbors()) {
                        if (nn != null && nn.getContinentId() != 0 && nn.getContinentId() != cid) {
                            touchesOtherContinent = true;
                            break;
                        }
                    }
                    if (touchesOtherContinent && !underMin) {
                        if (GameRandom.nextInt(100) < 85) {
                            continue;
                        }
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
        notifyStep(4, "Grow Continents");

        // Step 5: Convert enclosed oceans to lakes
        List<List<Hex>> enclosedLakeComponents = convertEnclosedOceansToLakes(continentHexes, allLand);
        notifyStep(5, "Convert Enclosed Lakes");

        // Step 6: Compute coastal water distance via BFS
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
        notifyStep(6, "Compute Coastal Distance");

        // Step 7: Generate inland lakes
        List<Hex> createdLakes = new ArrayList<>();
        for (Map.Entry<Integer, Set<Hex>> entry : continentHexes.entrySet()) {
            Set<Hex> cSet = entry.getValue();
            List<Hex> deepCandidates = new ArrayList<>();
            List<Hex> lakeCandidates = new ArrayList<>();
            for (Hex h : cSet) {
                if (h.getWaterDistance() >= 3 && !h.isCoastal()) {
                    deepCandidates.add(h);
                } else if (h.getWaterDistance() >= 2 && !h.isCoastal()) {
                    lakeCandidates.add(h);
                }
            }
            List<Hex> pool = new ArrayList<>(deepCandidates);
            pool.addAll(lakeCandidates);
            pool.sort((a, b) -> Integer.compare(b.getWaterDistance(), a.getWaterDistance()));
            int lakeCount = Math.min(6, Math.max(2, (deepCandidates.size() + lakeCandidates.size()) / 15));
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
        notifyStep(7, "Generate Inland Lakes");

        // Step 8: Assign land biomes by latitude, hydrology, and coastal proximity
        int latScale = this.worldHeight / 2;
        for (Hex h : allLand) {
            if (h.getBiome() == GameConstants.BIOME_LAKE) {
                continue;
            }
            Biome biome = pickBiomeForLandHex(h, latScale);
            h.setBiome(biome);
            h.setLocalWeather(getRandomWeather(biome));
        }
        notifyStep(8, "Assign Biomes");

        // Step 9: Generate archipelagos and solitary islands
        generateArchipelagosAndIslands();
        notifyStep(9, "Generate Archipelagos & Islands");

        // Step 10: Ensure minimum island counts
        int minIslands = (this.minIslandCount >= 0)
                ? this.minIslandCount
                : (this.worldHeight >= 30 ? GameNumbers.WORLD_MIN_ISLAND_COUNT : 2);
        ensureMinimumIslands(minIslands);
        notifyStep(10, "Ensure Island Counts");

        // Step 11: Classify landmasses into continents, archipelagos, and ice caps
        classifyLandmasses();
        notifyStep(11, "Classify Landmasses");

        // Step 12: Enforce minimum biome diversity quotas
        ensureMinimumBiomeCounts(this.worldHeight >= 30 ? 25 : Math.max(1, allLand.size() / 35));
        classifyLandmasses();

        convertEnclosedOceansToLakes(continentHexes, allLand);
        classifyLandmasses();
        notifyStep(12, "Enforce Biome Quotas");

        // Step 13: Generate edge river networks from lakes to oceans
        generateAllLakeRivers();
        preventSurroundedHexes();
        ensureAllLakesHaveRivers();
        cleanupOrphanRivers();
        notifyStep(13, "Generate River Networks");

        // Step 14: Spawn player dynasty on matching biome continent
        spawnPlayerOnRandomContinent(startBiome, startColony, continentHexes);
        notifyStep(14, "Spawn Player Dynasty");

        // Step 15: Distribute rival dynasties across continents
        distributeRivalDynastiesAcrossContinents(continentHexes);
        rebuildColonizedHexIndexIfNeeded();
        notifyStep(15, "Distribute Rival Dynasties");
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
            if (h != null && h.getBiome() == GameConstants.BIOME_OCEAN && h.getContinentId() == 0) {
                int d = continentDist.getOrDefault(h, 0);
                if (d >= deepMinDist && Math.abs(h.getR()) <= maxLat) {
                    deepOceanHexes.add(h);
                }
            }
        }

        int numDeepArchipelagos = Math.max(2, (int) Math.round((12 + GameRandom.nextInt(6)) * scale));
        for (int a = 0; a < numDeepArchipelagos; a++) {
            if (deepOceanHexes.isEmpty()) break;
            Hex seed = deepOceanHexes.get(GameRandom.nextInt(deepOceanHexes.size()));
            if (seed.getBiome() != GameConstants.BIOME_OCEAN || seed.getContinentId() != 0) {
                continue;
            }
            int clusterTarget = Math.max(2, (int) Math.round((8 + GameRandom.nextInt(9)) * scale));
            List<Hex> cluster = new ArrayList<>();
            cluster.add(seed);
            seed.setBiome(pickBiomeForLandHex(seed, this.worldHeight / 2));
            seed.setLocalWeather(getRandomWeather(seed.getBiome()));
            seed.setIsland(true);
            seed.setContinentId(0);

            while (cluster.size() < clusterTarget) {
                List<Hex> candidates = new ArrayList<>();
                List<Integer> touchingCounts = new ArrayList<>();
                for (Hex base : cluster) {
                    for (Hex n : base.getAdjacentNeighbors()) {
                        if (n != null && n.getBiome() == GameConstants.BIOME_OCEAN && n.getContinentId() == 0 && !cluster.contains(n)) {
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
                    bestCandidate.setBiome(pickBiomeForLandHex(bestCandidate, this.worldHeight / 2));
                    bestCandidate.setLocalWeather(getRandomWeather(bestCandidate.getBiome()));
                    bestCandidate.setIsland(true);
                    bestCandidate.setContinentId(0);
                    cluster.add(bestCandidate);
                }
            }

            int satellites = Math.max(1, (int) Math.round((1 + GameRandom.nextInt(3)) * scale));
            for (int s = 0; s < satellites; s++) {
                Hex satBase = cluster.get(GameRandom.nextInt(cluster.size()));
                for (Hex n1 : satBase.getAdjacentNeighbors()) {
                    if (n1 == null) continue;
                    for (Hex n2 : n1.getAdjacentNeighbors()) {
                        if (n2 != null && n2.getBiome() == GameConstants.BIOME_OCEAN && n2.getContinentId() == 0 && !cluster.contains(n2)) {
                            n2.setBiome(pickBiomeForLandHex(n2, this.worldHeight / 2));
                            n2.setLocalWeather(getRandomWeather(n2.getBiome()));
                            n2.setIsland(true);
                            n2.setContinentId(0);
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
            if (seed == null || seed.getBiome() != GameConstants.BIOME_OCEAN || seed.getContinentId() != 0) {
                continue;
            }
            int clusterTarget = Math.max(2, (int) Math.round((4 + GameRandom.nextInt(5)) * scale));
            List<Hex> cluster = new ArrayList<>();
            cluster.add(seed);
            seed.setBiome(pickBiomeForLandHex(seed, this.worldHeight / 2));
            seed.setLocalWeather(getRandomWeather(seed.getBiome()));
            seed.setIsland(true);
            seed.setContinentId(0);

            while (cluster.size() < clusterTarget) {
                Hex base = cluster.get(GameRandom.nextInt(cluster.size()));
                List<Hex> candidates = new ArrayList<>();
                for (Hex n : base.getAdjacentNeighbors()) {
                    if (n != null && n.getBiome() == GameConstants.BIOME_OCEAN && n.getContinentId() == 0 && !cluster.contains(n)) {
                        candidates.add(n);
                    }
                }
                if (candidates.isEmpty()) {
                    break;
                }
                Hex added = candidates.get(GameRandom.nextInt(candidates.size()));
                added.setBiome(pickBiomeForLandHex(added, this.worldHeight / 2));
                added.setLocalWeather(getRandomWeather(added.getBiome()));
                added.setIsland(true);
                added.setContinentId(0);
                cluster.add(added);
            }
        }

        int numRandomIslands = Math.max(4, (int) Math.round((30 + GameRandom.nextInt(25)) * scale));
        for (int i = 0; i < numRandomIslands; i++) {
            int r = -maxLat + GameRandom.nextInt(2 * maxLat + 1);
            int col = -this.worldWidth / 2 + GameRandom.nextInt(this.worldWidth);
            int q = col - (r >> 1);
            Hex hex = getHexAt(q, r);
            if (hex != null && hex.getBiome() == GameConstants.BIOME_OCEAN && hex.getContinentId() == 0) {
                Biome b = pickBiomeForLandHex(hex, this.worldHeight / 2);
                hex.setBiome(b);
                hex.setLocalWeather(getRandomWeather(b));
                hex.setIsland(true);
                hex.setContinentId(0);
            }
        }
    }

    private boolean isOceanHex(Hex h) {
        return h != null && h.getBiome() == GameConstants.BIOME_OCEAN;
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
        if (isOceanHex(edge.h1) && isOceanHex(edge.h2)) return;
        int d1 = edge.h1.directionTo(edge.h2);
        int d2 = edge.h2.directionTo(edge.h1);
        if (d1 < 0 || d2 < 0) return;
        edge.h1.setRiverEdge(d1, true);
        edge.h2.setRiverEdge(d2, true);
    }

    private boolean wouldSurroundHex(Hex hex, int newRiverDir) {
        if (hex == null) return false;
        int riverCount = hex.getRiverEdgeCount();
        if (!hex.hasRiverEdge(newRiverDir)) {
            riverCount++;
        }
        return riverCount >= 4;
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

            if (isOceanHex(curr.edge.h1) || isOceanHex(curr.edge.h2) || (existingRivers != null && existingRivers.contains(curr.edge))) {
                goalEdge = curr.edge;
                break;
            }

            int currentSteps = stepMap.getOrDefault(curr.edge, 1);
            if (maxDepth > 0 && currentSteps >= maxDepth) {
                continue;
            }

            for (RiverEdgeKey next : getAdjacentRiverEdges(curr.edge)) {
                int d1 = next.h1.directionTo(next.h2);
                int d2 = next.h2.directionTo(next.h1);
                if (d1 < 0 || d2 < 0) continue;
                if (avoidSurround) {
                    if (!isOceanHex(next.h1) && next.h1.getRiverEdgeCount() >= 2) continue;
                    if (!isOceanHex(next.h2) && next.h2.getRiverEdgeCount() >= 2) continue;
                    if (wouldSurroundHex(next.h1, d1) || wouldSurroundHex(next.h2, d2)) {
                        continue;
                    }
                }
                double edgeWeight = 1.0 + (next.h1.getWaterDistance() + next.h2.getWaterDistance()) * 0.05
                        + (next.h1.getRiverEdgeCount() + next.h2.getRiverEdgeCount()) * 2.0
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
            if (avoidSurround) {
                return findShortestRiverPathToOcean(startEdges, maxDepth, false, existingRivers);
            }
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

    private void generateAllLakeRivers() {
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

        Set<RiverEdgeKey> placedRivers = new HashSet<>();
        for (Set<Hex> comp : lakeComponents) {
            generateRiversForLakeComponent(comp, placedRivers);
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
                    if (n1.getBiome() != GameConstants.BIOME_LAKE && n2.getBiome() != GameConstants.BIOME_LAKE) {
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
            return false;
        }

        Set<RiverEdgeKey> markedRivers = new HashSet<>();
        for (RiverEdgeKey edge : mainPath) {
            forceMarkRiverEdge(edge);
            markedRivers.add(edge);
            if (placedRivers != null) {
                placedRivers.add(edge);
            }
        }

        int desiredForks = Math.min(3, Math.max(1, mainPath.size() / 3));
        for (int f = 0; f < desiredForks; f++) {
            int branchIdx = GameRandom.nextInt(mainPath.size());
            RiverEdgeKey branchEdge = mainPath.get(branchIdx);
            List<RiverEdgeKey> forkCandidates = new ArrayList<>();
            for (RiverEdgeKey adj : getAdjacentRiverEdges(branchEdge)) {
                if (!markedRivers.contains(adj)) {
                    forkCandidates.add(adj);
                }
            }
            if (!forkCandidates.isEmpty()) {
                RiverEdgeKey forkStart = forkCandidates.get(GameRandom.nextInt(forkCandidates.size()));
                List<RiverEdgeKey> forkPath = findShortestRiverPathToOcean(Collections.singletonList(forkStart), 15, true, placedRivers);
                if (!forkPath.isEmpty()) {
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
                        if (c1 >= 4 || c2 >= 4) {
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
                        for (RiverEdgeKey edge : forkPath) {
                            forceMarkRiverEdge(edge);
                            markedRivers.add(edge);
                            if (placedRivers != null) {
                                placedRivers.add(edge);
                            }
                        }
                    }
                }
            }
        }
        return true;
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
            if (h == null || h.getBiome() != GameConstants.BIOME_OCEAN || visited.contains(h)) {
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
                    if (n != null && n.getBiome() == GameConstants.BIOME_OCEAN && !visited.contains(n)) {
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
                if (dominantCid == 0) {
                    boolean nearIce = false;
                    for (Hex oceanHex : component) {
                        if (this.worldHeight > 0 && Math.abs(oceanHex.getR()) >= (this.worldHeight / 2 - 5)) {
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
                            oceanHex.setBiome(GameConstants.BIOME_ICE_FIELD);
                            oceanHex.setLocalWeather(getRandomWeather(GameConstants.BIOME_ICE_FIELD));
                        }
                        continue;
                    }
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
            if (h == null || !h.hasAnyRiver()) continue;
            while (h.getRiverEdgeCount() >= 4) {
                boolean removed = false;
                for (int dir = 5; dir >= 0; dir--) {
                    if (h.hasRiverEdge(dir)) {
                        h.setRiverEdge(dir, false);
                        Hex n = h.getNeighbor(dir);
                        if (n != null) {
                            int opp = (dir + 3) % 6;
                            n.setRiverEdge(opp, false);
                        }
                        removed = true;
                        break;
                    }
                }
                if (!removed) break;
            }
        }
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

            if (!touchesLake || !touchesOcean) {
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
            if (isWaterBiome(b)) continue;
            if (!isCoastal && b.isCoastal() && !hasRiver) continue;

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
            return GameConstants.BIOME_PLAINS;
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
            return absLat >= 0.70;
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
        if (b == GameConstants.BIOME_MOUNTAIN || b == GameConstants.BIOME_VOLCANIC || b == GameConstants.BIOME_URBAN) {
            return true;
        }
        return b.getClimateZone() == ClimateZone.ANY;
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
                if (!h.isIsland() && h.getColony() == null && h.getBiome() != GameConstants.BIOME_LAKE) {
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
        for (int i = 0; i < dynastyLimit; i++) {
            AntSpecies npcSpecies = speciesList.get(i);
            int cid = continentIds.get(i % continentIds.size());
            Set<Hex> cSet = continentHexes.get(cid);

            Hex bestHex = null;
            int bestScore = Integer.MIN_VALUE;

            for (Hex h : cSet) {
                if (h.getColony() != null || h.isIsland() || h.getBiome() == GameConstants.BIOME_LAKE) {
                    continue;
                }

                int minCapDist = Integer.MAX_VALUE;
                for (Hex cap : placedCapitals) {
                    int d = GameNumbers.axialHexDistance(h.getQ(), h.getR(), cap.getQ(), cap.getR());
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

                if (npcSpecies.getPreferredBiome() != null) {
                    if (h.getBiome() == npcSpecies.getPreferredBiome()) {
                        score += 100;
                    } else if (h.getBiome().getClimateZone() == npcSpecies.getPreferredBiome().getClimateZone()) {
                        score += 30;
                    }
                }

                score += GameRandom.nextInt(15);

                if (score > bestScore) {
                    bestScore = score;
                    bestHex = h;
                }
            }

            if (bestHex == null) {
                for (Hex h : this.hexes) {
                    if (h.getColony() == null && isLandHex(h) && !h.isIsland()) {
                        bestHex = h;
                        break;
                    }
                }
            }
            if (bestHex == null) {
                for (Hex h : this.hexes) {
                    if (h.getColony() == null && isLandHex(h)) {
                        bestHex = h;
                        break;
                    }
                }
            }

            if (bestHex == null) {
                continue;
            }

            if (npcSpecies.getPreferredBiome() != null && bestHex.getBiome() != npcSpecies.getPreferredBiome()) {
                if (bestHex.getBiome().getClimateZone() == npcSpecies.getPreferredBiome().getClimateZone()
                        || npcSpecies.getPreferredBiome().getClimateZone() == ClimateZone.ANY) {
                    bestHex.setBiome(npcSpecies.getPreferredBiome());
                    bestHex.setLocalWeather(getRandomWeather(npcSpecies.getPreferredBiome()));
                }
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

            starterService.initializeNewColony(aiColony);
            bestHex.setColony(aiColony);
            placedCapitals.add(bestHex);

            registerDynasty(npcDynasty);
            historyService.record(WorldHistoryEventType.DYNASTY_FORMED, LanguageStrings.HISTORY_DYNASTY_FORMED_FMT,
                    npcDynasty.getId(), aiColony.getId(), -1,
                    WorldHistoryEvent.dynastyArg(npcDynasty.getId()));
            historyService.record(WorldHistoryEventType.COLONY_FOUNDED, LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                    npcDynasty.getId(), aiColony.getId(), -1,
                    WorldHistoryEvent.colonyArg(aiColony.getId()),
                    WorldHistoryEvent.dynastyArg(npcDynasty.getId()));
        }
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
                if (hex.getBiome() == GameConstants.BIOME_OCEAN && !touchesAnyLand(hex)) {
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
                        List<Hex> previousList = byBiome.get(previous);
                        if (previousList != null) {
                            previousList.remove(hex);
                        }
                        counts.put(previous, Math.max(0, counts.getOrDefault(previous, 0) - 1));
                        hex.setBiome(target);
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
            if (hex.getBiome() == GameConstants.BIOME_OCEAN) {
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
        if (isWaterBiome(target)) {
            if (hex.getColony() != null || hex.isIsland()) {
                return false;
            }
            if (this.worldWidth > 0 && Math.abs(hex.getR()) >= (this.worldHeight / 2 - 4)) {
                return false;
            }
            if (target == GameConstants.BIOME_LAKE && touchesOcean(hex)) {
                return false;
            }
        }
        if (hex.getBiome() == GameConstants.BIOME_LAKE && !isWaterBiome(target) && wouldLinkMainlandAndIsland(hex)) {
            return false;
        }
        return true;
    }

    private boolean touchesOcean(Hex hex) {
        if (hex == null) {
            return false;
        }
        for (Hex neighbor : hex.getAdjacentNeighbors()) {
            if (neighbor != null && neighbor.getBiome() == GameConstants.BIOME_OCEAN) {
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
                if (neighbor.getBiome() != GameConstants.BIOME_OCEAN) {
                    continue;
                }
                if (hexDistanceFromOrigin(neighbor) >= outerOceanDist) {
                    sealed = false;
                    break;
                }
                Biome fillBiome = donor;
                if (isWaterBiome(fillBiome)) {
                    fillBiome = GameConstants.BIOME_PLAINS;
                }
                List<Hex> oceanList = byBiome.get(GameConstants.BIOME_OCEAN);
                if (oceanList != null) {
                    oceanList.remove(neighbor);
                }
                counts.put(GameConstants.BIOME_OCEAN, Math.max(0, counts.getOrDefault(GameConstants.BIOME_OCEAN, 0) - 1));
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
            if (neighbor != null && neighbor.getBiome() == GameConstants.BIOME_OCEAN) {
                count++;
            }
        }
        return count;
    }

    public void convertEnclosedOceansToLakes() {
        Set<Hex> openOcean = new HashSet<>();
        ArrayDeque<Hex> queue = new ArrayDeque<>();
        for (Hex hex : this.hexes) {
            if (hex.getBiome() != GameConstants.BIOME_OCEAN) {
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
                if (neighbor == null || neighbor.getBiome() != GameConstants.BIOME_OCEAN) {
                    continue;
                }
                if (openOcean.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        for (Hex hex : this.hexes) {
            if (hex.getBiome() == GameConstants.BIOME_OCEAN && !openOcean.contains(hex)) {
                hex.setBiome(GameConstants.BIOME_LAKE);
                hex.setLocalWeather(getRandomWeather(GameConstants.BIOME_LAKE));
            }
        }
    }

    public void convertLakesTouchingOceanToOcean() {
        ArrayDeque<Hex> queue = new ArrayDeque<>();
        for (Hex hex : this.hexes) {
            if (hex.getBiome() == GameConstants.BIOME_OCEAN) {
                queue.add(hex);
            }
        }
        while (!queue.isEmpty()) {
            Hex current = queue.poll();
            for (Hex neighbor : current.getAdjacentNeighbors()) {
                if (neighbor == null || neighbor.getBiome() != GameConstants.BIOME_LAKE) {
                    continue;
                }
                neighbor.setBiome(GameConstants.BIOME_OCEAN);
                neighbor.setLocalWeather(getRandomWeather(GameConstants.BIOME_OCEAN));
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
                    if (this.worldWidth > 0 && this.worldHeight > 0) {
                        if (current.getContinentId() != neighbor.getContinentId()) {
                            continue;
                        }
                    } else {
                        boolean neighborIsIce = (neighbor.getBiome() == GameConstants.BIOME_ICE_FIELD);
                        if (startIsIce != neighborIsIce) {
                            continue;
                        }
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
            if (existingIds.isEmpty()) {
                if (startIsIce) {
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
                if (this.worldWidth > 0 && existingIds.size() > 1) {
                    for (Hex hex : component) {
                        hex.setIsland(false);
                    }
                } else {
                    int cid = 0;
                    for (int eid : existingIds) {
                        cid = eid;
                        break;
                    }
                    if (cid <= 0) {
                        this.continentCount++;
                        cid = this.continentCount;
                    }
                    for (Hex hex : component) {
                        hex.setIsland(false);
                        hex.setContinentId(cid);
                    }
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
        for (int p = 0; p < 4; p++) {
            List<AntSpecies> pass = new ArrayList<>(pool);
            Collections.shuffle(pass, GameRandom.getShuffleRandom());
            assignment.addAll(pass);
        }

        while (assignment.size() < targetCount) {
            Collections.shuffle(pool, GameRandom.getShuffleRandom());
            for (AntSpecies species : pool) {
                if (assignment.size() >= targetCount) {
                    break;
                }
                assignment.add(species);
            }
        }
        return assignment;
    }
    
    private boolean isWaterBiome(Biome biome) {
        return biome == GameConstants.BIOME_OCEAN || biome == GameConstants.BIOME_LAKE;
    }
    
    private void linkNeighbors(Map<String, Hex> hexMap) {
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
    
    private Biome getBiomeById(int id) {
        for(Biome b : GameConstants.getBiomes()) {
            if (b.getId() == id) return b;
        }
        return GameConstants.BIOME_PLAINS;
    }
    
    private Weather getWeatherById(int id) {
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
                hex.setTimeOffset(sh.timeOffset);
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
                
                hexMap.put(sh.q + "," + sh.r, hex);
                this.hexes.add(hex);
            }
            linkNeighbors(hexMap);
            classifyLandmasses();
            System.out.println("[World] Loaded world grid from savefile (" + this.hexes.size() + " hexes, " + loadedColonies.size() + " colonies).");
            System.out.println("[World] Landmasses: " + this.continentCount + " continent(s), "
                    + this.islandCount + " island(s).");
        } else {
            System.out.println("[World] No map data in save (or old save version). Generating fresh world map for existing colony.");
            
            Colony colony = null;
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

        this.colonyIdCounter = maxColId + 1;
        this.dynastyIdCounter = maxDynastyId + 1;
        
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
        if (savefile.getWorldHistory() != null) {
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
                if (this.hour >= 0 && this.hour < 5) {
                    this.setTimeOfDay(GameConstants.TIME_NIGHT);
                } else if (this.hour >= 5 && this.hour < 7) {
                    this.setTimeOfDay(GameConstants.TIME_DAWN);
                } else if (this.hour >= 7 && this.hour < 18) {
                    this.setTimeOfDay(GameConstants.TIME_DAY);
                } else if (this.hour >= 18 && this.hour < 20) {
                    this.setTimeOfDay(GameConstants.TIME_DUSK);
                } else if (this.hour >= 20 && this.hour <= 23) {
                    this.setTimeOfDay(GameConstants.TIME_NIGHT);
                }
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
