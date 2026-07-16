package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.DynastyTitle;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.misc.TradeMethod;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Trade;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDeathService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyNamingService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntegrationService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyRebellionService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.entities.services.world.WarService;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class World {

    private static final DynastyDeathService DYNASTY_DEATH_SERVICE = new DynastyDeathService();
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
    private List<Dynasty> dynastys;
    private Hex activeHex; 
    private int saveSlotId = 0; // 0 = no slot (ad-hoc)
    private Engine engine;
    private int worldRadius = 8; 
    private int colonyIdCounter = 1;
    private int dynastyIdCounter = 1;
    private final DynastyNamingService namingService;
    private final WarService warService;

    public World() {
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0;
        this.temperature = 25;
        this.humidity = 2;
        this.hexes = new ArrayList<>();
        this.dynastys = new ArrayList<>();
        this.timeOfDay = GameConstants.TIME_DAWN;
        this.moonPhase = GameConstants.PHASE_NEW_MOON;
        this.season = GameConstants.SEASON_SPRING;
        this.weather = GameConstants.WEATHER_CLEAR;
        this.namingService = new DynastyNamingService();
        this.warService = new WarService(this);
    }
    
    public WarService getWarService() {
        return warService;
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

    public void setHexes(ArrayList<Hex> hexes) {
        this.hexes = hexes;
    }

    public Hex getHexOfColony(Colony colony) {
        if (colony == null || hexes == null) return null;
        for (Hex h : hexes) {
            if (h.getColony() == colony) return h;
        }
        return null;
    }

    public int colonyHexDistance(Colony from, Colony to) {
        Hex fromHex = getHexOfColony(from);
        Hex toHex = getHexOfColony(to);
        if (fromHex == null || toHex == null) {
            return 0;
        }
        return GameConstants.axialHexDistance(fromHex.getQ(), fromHex.getR(), toHex.getQ(), toHex.getR());
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

    public Hex getSpawnHex() {
        if (this.hexes == null || this.hexes.isEmpty()) return null;
        
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
        if (newHex == null || !hexes.contains(newHex)) return;

        if (this.activeHex != null) {
            this.activeHex.setActive(false);
            if (this.activeHex.getColony() != null) {
                this.activeHex.getColony().setActive(false);
            }
        }

        this.activeHex = newHex;
        this.activeHex.setActive(true);
        if (this.activeHex.getColony() != null) {
            this.activeHex.getColony().setActive(true);
        }
        
        updateEnvironmentalConditions();
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

    public void generateWorld(Biome startBiome, int size, Colony startColony, String baseName, String playerTitleKey) {
        System.out.println("Generating World... Size: " + size + " rings.");
        this.worldRadius = size;
        this.hexes.clear();
        this.dynastys.clear();
        Map<String, Hex> hexMap = new HashMap<>();
        ColonyStarterService starterService = ColonyStarterService.shared();
        
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
        this.dynastys.add(playerDynasty);
        
        this.colonyIdCounter = startColony.getId() + 1;

        List<Hex> eligibleNpcHexes = new ArrayList<>();

        for (int q = -size; q <= size; q++) {
            int r1 = Math.max(-size, -q - size);
            int r2 = Math.min(size, -q + size);
            for (int r = r1; r <= r2; r++) {
                int dist = (Math.abs(q) + Math.abs(q + r) + Math.abs(r)) / 2;

                Hex hex = new Hex();
                hex.setQ(q);
                hex.setR(r);
                hex.setTimeOffset(q); 
                
                Biome ringBiome = (dist == 0) ? startBiome : getBiomeForRing(dist);
                hex.setBiome(ringBiome);
                hex.setLocalWeather(getRandomWeather(ringBiome));
                hex.setActive(false); 

                if (dist == 0) {
                    hex.setColony(startColony); 
                    startColony.setActive(true);
                } else if (dist > 1 && !isWaterBiome(ringBiome)) {
                    eligibleNpcHexes.add(hex);
                } else {
                    hex.setColony(null);
                }
                
                hexMap.put(q + "," + r, hex);
                this.hexes.add(hex);
            }
        }

        List<Hex> npcColonyHexes = selectNpcColonyHexes(eligibleNpcHexes);
        List<Species> npcSpeciesAssignments = assignNpcSpecies(npcColonyHexes.size());
        for (int i = 0; i < npcColonyHexes.size(); i++) {
            Hex hex = npcColonyHexes.get(i);
            Species npcSpecies = npcSpeciesAssignments.get(i);

            int dynastyId = this.dynastyIdCounter++;
            DynastyTitle npcTitle = namingService.pickRandomTitle();
            String npcThemeKey = namingService.claimThemeKey(npcSpecies);
            String npcDynName = LanguageStrings.formatDynastyName(npcThemeKey, npcTitle);
            Dynasty npcDynasty = new Dynasty(dynastyId, npcDynName, npcTitle.getNameKey(), false, npcSpecies);
            npcDynasty.setThemeBase(npcThemeKey);
            npcDynasty.getStarterService().initializeDynasty(npcDynasty);
            this.dynastys.add(npcDynasty);

            int colId = this.colonyIdCounter++;
            String npcCapName = namingService.generateCapitalName(npcThemeKey);
            Colony aiColony = new Colony(colId, npcCapName, false);
            npcDynasty.addColony(aiColony);

            starterService.initializeNewColony(aiColony);
            hex.setColony(aiColony);
        }

        linkNeighbors(hexMap);
        bindDynastyTradeServices();
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

    private List<Species> assignNpcSpecies(int colonyCount) {
        List<Species> nonOmni = new ArrayList<>(GameConstants.getWorldSpawnableNpcSpecies());
        if (colonyCount == 0 || nonOmni.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(nonOmni, GameRandom.getShuffleRandom());
        List<Species> assignment = new ArrayList<>(colonyCount);
        for (int i = 0; i < colonyCount; i++) {
            if (i < nonOmni.size()) {
                assignment.add(nonOmni.get(i));
            } else {
                assignment.add(nonOmni.get(GameRandom.nextInt(nonOmni.size())));
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

            hex.setNorth(hexMap.get(q + "," + (r - 1)));
            hex.setNorthEast(hexMap.get((q + 1) + "," + (r - 1)));
            hex.setSouthEast(hexMap.get((q + 1) + "," + r));
            hex.setSouth(hexMap.get(q + "," + (r + 1)));
            hex.setSouthWest(hexMap.get((q - 1) + "," + (r + 1)));
            hex.setNorthWest(hexMap.get((q - 1) + "," + r));
        }
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
        generateWorld(biome, 8, colony, baseName, playerTitleKey);
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
        this.worldRadius = (savefile.getWorldRadius() > 0) ? savefile.getWorldRadius() : 8;
        
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
        this.dynastys.clear();
        Map<String, Hex> hexMap = new HashMap<>();
        Map<String, Colony> loadedColonies = new HashMap<>();
        Map<Integer, Dynasty> loadedDynastys = new HashMap<>();
        
        int maxColId = 0;
        int maxDynastyId = 0;

        if (savefile.getDynastys() != null) {
            for (Savefile.SavedDynasty sc : savefile.getDynastys()) {
                Dynasty dynasty = new Dynasty(sc);
                loadedDynastys.put(dynasty.getId(), dynasty);
                this.dynastys.add(dynasty);
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
                    this.dynastys.add(adHocDynasty);
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
                
                hexMap.put(sh.q + "," + sh.r, hex);
                this.hexes.add(hex);
            }
            linkNeighbors(hexMap);
            System.out.println("[World] Loaded world grid from savefile (" + this.hexes.size() + " hexes, " + loadedColonies.size() + " colonies).");
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

            generateWorld(GameConstants.BIOME_PLAINS, this.worldRadius, colony, baseName, playerTitleKey);
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
                                if (hA.getColony() != null && hA.getColony().getDynasty() == d) {
                                    hA.getColony().setCurrentTunnelProject(tunnel);
                                }
                                if (hB.getColony() != null && hB.getColony().getDynasty() == d) {
                                    hB.getColony().setCurrentTunnelProject(tunnel);
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
                    Map<AntType, Integer> trans = new HashMap<>();
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
                        Map<AntType, Integer> pTrans = new HashMap<>();
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
        bindDynastyTradeServices();
        ColonyMilitaryService.refreshAllMilitaryPower(this.dynastys);
        if (savefile.getWars() != null) {
            warService.loadFromSave(savefile.getWars());
        } else {
            warService.syncFromDynasties();
        }
        warService.pruneInvalidWars();

        relocalizeDynastyNames();
        changeActiveHex(getSpawnHex());
        updateEnvironmentalConditions();
    }

    private void reapplyRoleAssignmentsAfterLoad() {
        if (this.engine == null) {
            return;
        }
        for (Hex hex : this.hexes) {
            if (hex.getColony() != null) {
                hex.getColony().runRoleAssignment(this.engine);
            }
        }
    }

    public Hex getHexAt(int q, int r) {
        if (hexes == null) return null;
        for (Hex h : hexes) {
            if (h.getQ() == q && h.getR() == r) return h;
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
        
        for (Hex h : this.hexes) {
             if (GameRandom.nextInt(100) < 5) { 
                 h.setLocalWeather(getRandomWeather(h.getBiome()));
             }
        }
    }

    public void runMinute() {
        this.minute++;
        
        for (Hex hex : this.hexes) {
            if (hex.getColony() != null) {
                hex.getColony().runMinutelyJobs();
            }
        }
        
        if (this.minute > 59) {
            this.minute = 0;
            this.runHour();
        }
    }

    public void runHour() {
        this.hour++;
        
        for (Hex hex : this.hexes) {
            if (hex.getColony() != null) {
                hex.getColony().runHourlyJobs(hex.getBiome(), this.engine);
            }
        }

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

    public void runDay() {
        this.day++;
        
        for (Dynasty dynasty : this.dynastys) {
            dynasty.runDailyJobs(this, engine != null ? engine.getTradeManager() : null);
        }
        
        for (Hex hex : this.hexes) {
            if (hex.getColony() != null) {
                Colony colony = hex.getColony();
                colony.runDailyJobs(this.getTemperatureIcon(), hex.getBiome(), hex);
                tryQueenRecoveryNuptial(this, colony, hex);
            }
        }

        for (Dynasty dynasty : this.dynastys) {
            ColonyMilitaryService.refreshDynastyMilitaryPower(dynasty);
        }

        warService.tickWarProgressDaily();
        DynastyIntegrationService.tickIntegrationsDaily(this, engine != null ? engine.getTradeManager() : null);
        
        // --- Process Dynasty/Colony Deaths ---
        DYNASTY_DEATH_SERVICE.processDynastyDeaths(this);

        randomizeWeather();

        if (GameRandom.nextInt(1000) == 0) {
            if (GameRandom.nextBoolean()) {
                this.setTimeOfDay(GameConstants.TIME_SOLAR_ECLIPSE);
            } else {
                this.setTimeOfDay(GameConstants.TIME_LUNAR_ECLIPSE);
            }
            
            for (Hex hex : this.hexes) {
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

    public void runMonth() {
        this.month++;
        Season monthSeason = GameConstants.seasonForMonth(this.month);

        for (Hex hex : this.hexes) {
            Colony colony = hex.getColony();
            if (colonyHasActiveDynasty(colony)) {
                colony.runMonthlyJobs(monthSeason, hex.getBiome());
            }
        }

        TradeManager tradeManager = engine != null ? engine.getTradeManager() : null;
        DynastyRebellionService.runMonthlyChecks(this, tradeManager);

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
        
        for (Hex hex : this.hexes) {
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

    private static void putAntTransport(Map<Integer, Integer> src, Map<AntType, Integer> dest) {
        if (src == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> entry : src.entrySet()) {
            AntType antType = GameConstants.getAntTypeById(entry.getKey());
            if (antType != null) {
                dest.put(antType, entry.getValue());
            }
        }
    }
}
