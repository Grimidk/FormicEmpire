package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.grimidk.formicempire.classes.constants.misc.Species;
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
import com.grimidk.formicempire.classes.entities.services.ColonyStarterService;
import com.grimidk.formicempire.classes.entities.services.DynastyDeathService;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

public class World {

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
    private Random random;    
    private int worldRadius = 8; 
    private int colonyIdCounter = 1;
    private int dynastyIdCounter = 1;

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
        this.random = new Random();
    }
    
    public synchronized int getNextColonyId() {
        return colonyIdCounter++;
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
    
    public List<Dynasty> getDynastys() { return dynastys; }
    
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
    
    public void generateWorld(Biome startBiome, int size, Colony startColony, String baseName) {
        System.out.println("Generating World... Size: " + size + " rings.");
        this.worldRadius = size;
        this.hexes.clear();
        this.dynastys.clear();
        Map<String, Hex> hexMap = new HashMap<>();
        ColonyStarterService starterService = new ColonyStarterService();
        
        baseName = formatName(baseName);
        
        Dynasty playerDynasty = new Dynasty(this.dynastyIdCounter++, baseName + " Dynasty", true, GameConstants.SPECIES_OMNI);
        playerDynasty.getStarterService().initializeDynasty(playerDynasty);
        playerDynasty.addColony(startColony);
        this.dynastys.add(playerDynasty);
        
        this.colonyIdCounter = startColony.getId() + 1;

        for (int q = -size; q <= size; q++) {
            int r1 = Math.max(-size, -q - size);
            int r2 = Math.min(size, -q + size);
            for (int r = r1; r <= r2; r++) {
                int dist = (Math.abs(q) + Math.abs(q + r) + Math.abs(r)) / 2;

                Hex hex = new Hex();
                hex.setQ(q);
                hex.setR(r);
                hex.setTimeOffset(q); 
                hex.setLocalWeather(getRandomWeather());
                hex.setActive(false); 

                if (dist == 0) {
                    hex.setBiome(startBiome);
                    hex.setColony(startColony); 
                    startColony.setActive(true);
                } else {
                    Biome ringBiome = getBiomeForRing(dist);
                    hex.setBiome(ringBiome);
                    
                    if (dist > 1 && !isWaterBiome(ringBiome) && random.nextInt(100) < 30) {
                        int dynastyId = this.dynastyIdCounter++;
                        
                        // Random non-omni species
                        List<Species> allSpecies = GameConstants.getSpecies();
                        List<Species> nonOmni = new ArrayList<>();
                        for (Species s : allSpecies) {
                            if (s.getId() != 1) nonOmni.add(s);
                        }
                        Species randomSpecies = nonOmni.isEmpty() ? GameConstants.SPECIES_OMNI : nonOmni.get(random.nextInt(nonOmni.size()));
                        
                        Dynasty npcDynasty = new Dynasty(dynastyId, randomSpecies.getName() + " Hive " + dynastyId, false, randomSpecies);
                        npcDynasty.getStarterService().initializeDynasty(npcDynasty);
                        this.dynastys.add(npcDynasty);
                        
                        int colId = this.colonyIdCounter++;
                        Colony aiColony = new Colony(colId, "Wild Colony " + colId, false);
                        npcDynasty.addColony(aiColony);
                        
                        starterService.initializeNewColony(aiColony);
                        hex.setColony(aiColony);
                    } else {
                        hex.setColony(null);
                    }
                }
                
                hexMap.put(q + "," + r, hex);
                this.hexes.add(hex);
            }
        }

        linkNeighbors(hexMap);        
        printWorldToConsole(size, hexMap);
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
        if (this.random == null) this.random = new Random();
        
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
        
        return options.get(random.nextInt(options.size()));
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
    
    private Weather getRandomWeather() {
        if (random == null) random = new Random();
        List<Weather> weathers = GameConstants.getWeathers();
        return weathers.get(random.nextInt(weathers.size()));
    }

    private void printWorldToConsole(int size, Map<String, Hex> hexMap) {
        System.out.println("\n--- Generated World Map ---\n");
        for (int r = -size; r <= size; r++) {
            StringBuilder line = new StringBuilder();
            
            int indent = Math.abs(r);
            for (int s = 0; s < indent; s++) line.append(" ");
            
            int q1 = Math.max(-size, -r - size);
            int q2 = Math.min(size, -r + size);

            for (int q = q1; q <= q2; q++) {
                Hex hex = hexMap.get(q + "," + r);
                if (hex != null) {
                    char c = (hex.getBiome().getName().length() > 0) ? hex.getBiome().getName().charAt(0) : '?';
                    
                    if (hex.getColony() != null) {
                        if (hex.getColony().isPlayer()) {
                            line.append("P ");
                        } else {
                            line.append("E ");
                        }
                    } else {
                        line.append(c).append(" ");
                    }
                } else {
                    line.append("  ");
                }
            }
            System.out.println(line.toString());
        }
    }

    public void startWorld(Biome biome, Colony colony, String baseName) {
        System.out.println("[World] startWorld called. Colony ants before init: " + colony.getAntTotal());

        if (colony.getAntTotal() == 0) {
            ColonyStarterService starterService = new ColonyStarterService();
            starterService.initializeNewColony(colony);
        }
        
        System.out.println("[World] Colony ants after init: " + colony.getAntTotal());

        baseName = formatName(baseName);
        generateWorld(biome, 8, colony, baseName);
        changeActiveHex(getSpawnHex()); 
        updateEnvironmentalConditions();
    }

    public void loadWorld(Savefile savefile) {
        this.minute = savefile.getMinute();
        this.hour = savefile.getHour();
        this.day = savefile.getDay();
        this.month = savefile.getMonth();
        this.year = savefile.getYear();  
        this.worldRadius = (savefile.getWorldRadius() > 0) ? savefile.getWorldRadius() : 8;
        
        String baseName = "Player";
        if (savefile.getName() != null && !savefile.getName().trim().isEmpty()) {
            String sName = savefile.getName().trim();
            if (!sName.startsWith("Save ") && !sName.equals("Autosave")) {
                baseName = sName;
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
                    String dynName = c.isPlayer() ? (baseName + " Dynasty") : "Wild Dynasty";
                    Dynasty adHocDynasty = new Dynasty(newDynastyId, dynName, c.isPlayer(), GameConstants.SPECIES_OMNI);
                    adHocDynasty.getStarterService().initializeDynasty(adHocDynasty);
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
                colony = new Colony(1, baseName + " Prime", true);
            }
             
            if (colony.getAntTotal() == 0) {
                ColonyStarterService starter = new ColonyStarterService();
                starter.initializeNewColony(colony);
            }

            generateWorld(GameConstants.BIOME_PLAINS, this.worldRadius, colony, baseName);
        }

        this.colonyIdCounter = maxColId + 1;
        this.dynastyIdCounter = maxDynastyId + 1;
        
        changeActiveHex(getSpawnHex()); 
        updateEnvironmentalConditions();
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
        if (random.nextInt(1000) == 0) {
            if (random.nextBoolean()) {
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
            Weather newWeather = possibleWeathers.get(random.nextInt(possibleWeathers.size()));
            if (this.weather != newWeather) {
                this.setWeather(newWeather);
            }
        }
        
        for (Hex h : this.hexes) {
             if (random.nextInt(100) < 5) { 
                 h.setLocalWeather(getRandomWeather());
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
                hex.getColony().runHourlyJobs(hex.getBiome());
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

        if (this.hour > 23) {
            this.hour = 0;
            this.runDay();
        }
    }

    public void runDay() {
        this.day++;
        
        for (Dynasty dynasty : this.dynastys) {
            dynasty.runDailyJobs();
        }
        
        for (Hex hex : this.hexes) {
            if (hex.getColony() != null) {
                hex.getColony().runDailyJobs(this.getTemperatureIcon(), hex.getBiome());
            }
        }
        
        // --- Process Dynasty/Colony Deaths ---
        DynastyDeathService deathService = new DynastyDeathService();
        deathService.processDynastyDeaths(this);

        randomizeWeather();

        if (random.nextInt(1000) == 0) {
            if (random.nextBoolean()) {
                this.setTimeOfDay(GameConstants.TIME_SOLAR_ECLIPSE);
            } else {
                this.setTimeOfDay(GameConstants.TIME_LUNAR_ECLIPSE);
            }
            
            for (Hex hex : this.hexes) {
                if (hex.getColony() != null && !hex.getColony().getDynasty().isDefeated()) {
                    hex.getColony().getLabourService().runNuptial(hex.getColony(), this, hex);
                    hex.getColony().logEvent("The Eclipse has triggered a spontaneous Nuptial Flight!");
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
        
        for (Hex hex : this.hexes) {
            if (hex.getColony() != null && !hex.getColony().getDynasty().isDefeated()) {
                hex.getColony().runMonthlyJobs();
            }
        }

        if (this.month >= 1 && this.month < 4) {
            this.setSeason(GameConstants.SEASON_SPRING);
        } else if (this.month >= 4 && this.month < 7) {
            this.setSeason(GameConstants.SEASON_SUMMER);
        } else if (this.month >= 7 && this.month < 10) {
            this.setSeason(GameConstants.SEASON_AUTUMN);
        } else if (this.month >= 10 && this.month < 12) {
            this.setSeason(GameConstants.SEASON_WINTER);
        } else {
            this.setSeason(GameConstants.SEASON_SPRING);
        }

        try {
            if (this.engine != null) {
                int freq = this.engine.getAutosaveFrequency();
                if (freq > 0 && (this.month % freq == 0)) {
                    SaveManager sm = new SaveManager();
                    sm.saveAutosave(this, this.engine);
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
            if (hex.getColony() != null && !hex.getColony().getDynasty().isDefeated()) {
                hex.getColony().runYearlyJobs(this, hex);
            }
        }
    }
}