package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Humidity;
import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
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
    private int saveSlotId = 0; // 0 = no slot (ad-hoc)
    private Engine engine;
    private Random random;    
    private int worldRadius = 7; 

    public World() {
        this.minute = 0;
        this.hour = 0;
        this.day = 1;
        this.month = 1;
        this.year = 0;
        this.temperature = 25;
        this.humidity = 2;
        this.hexes = new ArrayList<>();
        this.timeOfDay = GameConstants.DAWN_TIME;
        this.moonPhase = GameConstants.NEW_MOON_PHASE;
        this.season = GameConstants.SPRING_SEASON;
        this.weather = GameConstants.CLEAR_WEATHER;
        this.random = new Random();
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
    
    public void generateWorld(Biome startBiome, int size, Colony startColony) {
        System.out.println("Generating World... Size: " + size + " rings.");
        this.worldRadius = size;
        this.hexes.clear();
        Map<String, Hex> hexMap = new HashMap<>();

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

                if (dist == 0) {
                    hex.setBiome(startBiome);
                    hex.setColony(startColony); 
                } else {
                    hex.setBiome(getBiomeForRing(dist));
                    hex.setColony(null);
                }
                
                hexMap.put(q + "," + r, hex);
                this.hexes.add(hex);
            }
        }

        linkNeighbors(hexMap);        
        printWorldToConsole(size, hexMap);
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
                options.add(GameConstants.PLAINS_BIOME);
                options.add(GameConstants.FOREST_BIOME);
                options.add(GameConstants.JUNGLE_BIOME);
                break;
            case 2:
                options.add(GameConstants.FOREST_BIOME);
                options.add(GameConstants.JUNGLE_BIOME);
                options.add(GameConstants.SWAMP_BIOME);
                break;
            case 3:
                options.add(GameConstants.JUNGLE_BIOME);
                options.add(GameConstants.SWAMP_BIOME);
                options.add(GameConstants.DESSERT_BIOME);
                break;
            case 4:
                options.add(GameConstants.SWAMP_BIOME);
                options.add(GameConstants.DESSERT_BIOME);
                options.add(GameConstants.TAIGA_BIOME);
                options.add(GameConstants.URBAN_BIOME);
                break;
            case 5:
                options.add(GameConstants.TAIGA_BIOME);
                options.add(GameConstants.TUNDRA_BIOME);
                options.add(GameConstants.DESSERT_BIOME);
                options.add(GameConstants.URBAN_BIOME);
                options.add(GameConstants.LAKE_BIOME);
                break;
            case 6:
                options.add(GameConstants.TUNDRA_BIOME);
                options.add(GameConstants.DESSERT_BIOME);
                options.add(GameConstants.MOUNTAIN_BIOME);
                options.add(GameConstants.URBAN_BIOME);
                options.add(GameConstants.LAKE_BIOME);
                break;
            case 7:
                options.add(GameConstants.TUNDRA_BIOME);
                options.add(GameConstants.DESSERT_BIOME);
                options.add(GameConstants.MOUNTAIN_BIOME);
                options.add(GameConstants.URBAN_BIOME);
                options.add(GameConstants.VOLCANIC_BIOME);
                break;
            default:
                options.add(GameConstants.OCEAN_BIOME);
                break;
        }
        
        return options.get(random.nextInt(options.size()));
    }
    
    private Biome getBiomeById(int id) {
        for(Biome b : GameConstants.getBiomes()) {
            if (b.getId() == id) return b;
        }
        return GameConstants.PLAINS_BIOME;
    }
    
    private Weather getWeatherById(int id) {
        for (Weather w : GameConstants.getWeathers()) {
            if (w.getId() == id) return w;
        }
        return GameConstants.CLEAR_WEATHER;
    }
    
    private Weather getRandomWeather() {
        if (random == null) random = new Random();
        List<Weather> weathers = GameConstants.getWeathers();
        return weathers.get(random.nextInt(weathers.size()));
    }

    private void printWorldToConsole(int size, Map<String, Hex> hexMap) {
        System.out.println("\n--- Generated World Map (First Letter of Biome) ---\n");
        for (int r = -size; r <= size; r++) {
            StringBuilder line = new StringBuilder();
            
            for (int s = 0; s < Math.abs(r); s++) line.append(" "); 
            if (r < 0) {
                 for(int s=0; s < (size + r); s++) line.append(" ");
            }
            
            int q1 = Math.max(-size, -r - size);
            int q2 = Math.min(size, -r + size);

            for (int q = q1; q <= q2; q++) {
                Hex hex = hexMap.get(q + "," + r);
                if (hex != null) {
                    char c = hex.getBiome().getName().charAt(0);
                    line.append(c).append(" ");
                } else {
                    line.append("  ");
                }
            }
            System.out.println(line.toString());
        }
        System.out.println("\n--------------------------------------------------\n");
    }

    public void startWorld(Biome biome, Colony colony) {
        if (colony.getAntTotal() == 0) {
            colony.startColony();
        }
        generateWorld(biome, 8, colony);
        updateEnvironmentalConditions();
    }

    public void loadWorld(Savefile savefile) {
        this.minute = savefile.getMinute();
        this.hour = savefile.getHour();
        this.day = savefile.getDay();
        this.month = savefile.getMonth();
        this.year = savefile.getYear();  
        this.worldRadius = (savefile.getWorldRadius() > 0) ? savefile.getWorldRadius() : 8;
        
        Colony colony = new Colony(savefile);  
        
        this.hexes.clear();
        Map<String, Hex> hexMap = new HashMap<>();
        
        if (savefile.getWorldHexes() != null && !savefile.getWorldHexes().isEmpty()) {
            for (Savefile.SavedHex sh : savefile.getWorldHexes()) {
                Hex hex = new Hex();
                hex.setQ(sh.q);
                hex.setR(sh.r);
                hex.setBiome(getBiomeById(sh.biomeId));
                hex.setTimeOffset(sh.timeOffset);
                hex.setLocalWeather(getWeatherById(sh.weatherId));
                
                if (sh.hasColony) {
                    hex.setColony(colony);
                } else {
                    hex.setColony(null);
                }
                
                hexMap.put(sh.q + "," + sh.r, hex);
                this.hexes.add(hex);
            }
            linkNeighbors(hexMap);
            System.out.println("Loaded world grid from savefile (" + this.hexes.size() + " hexes).");
        } else {
            System.out.println("No map data in save (or old save version). Generating fresh world map for existing colony.");
            generateWorld(GameConstants.PLAINS_BIOME, this.worldRadius, colony);
        }

        updateEnvironmentalConditions();
    }
    
    private void updateEnvironmentalConditions() {
        if (this.getSpawnHex() == null || this.getSpawnHex().getBiome() == null) {
            return;
        }

        Biome baseBiome = this.getSpawnHex().getBiome();
        float calcTemp = baseBiome.getTemperature();
        float calcHumid = baseBiome.isIsHumid();

        if (this.season != null) {
            calcTemp *= this.season.getTempMult();
            calcHumid *= this.season.getHumidityMult();
        }

        if (this.timeOfDay != null) {
            calcTemp *= this.timeOfDay.getTempMult();
        }

        Weather localW = this.getSpawnHex().getLocalWeather();
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
                this.setWeather(GameConstants.FROG_WEATHER);
            } else {
                this.setWeather(GameConstants.BLOOD_WEATHER);
            }
        } else {
            List<Weather> possibleWeathers = new ArrayList<>();
            possibleWeathers.add(GameConstants.CLEAR_WEATHER);
            possibleWeathers.add(GameConstants.CLEAR_WEATHER);
            if (this.season == GameConstants.WINTER_SEASON) {
                possibleWeathers.add(GameConstants.SNOW_WEATHER);
                possibleWeathers.add(GameConstants.HEAVY_SNOW_WEATHER);
                possibleWeathers.add(GameConstants.WIND_WEATHER);
            } else if (this.season == GameConstants.SUMMER_SEASON) {
                possibleWeathers.add(GameConstants.RAIN_WEATHER);
                possibleWeathers.add(GameConstants.THUNDER_WEATHER);
                possibleWeathers.add(GameConstants.HEAT_WEATHER);
            } else {
                possibleWeathers.add(GameConstants.RAIN_WEATHER);
                possibleWeathers.add(GameConstants.HEAVY_RAIN_WEATHER);
                possibleWeathers.add(GameConstants.WIND_WEATHER);
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

        if (this.getSpawnHex() != null && this.getSpawnHex().getColony() != null) {
             this.getSpawnHex().getColony().runMinutelyJobs();
        }

        if (this.minute > 59) {
            this.minute = 0;
            this.runHour();
        }
    }

    public void runHour() {
        this.hour++;

        if (this.getSpawnHex() != null && this.getSpawnHex().getColony() != null) {
            this.getSpawnHex().getColony().runHourlyJobs();
        }

        boolean isEclipse = (this.timeOfDay == GameConstants.SOLAR_ECLIPSE_TIME || 
                             this.timeOfDay == GameConstants.LUNAR_ECLIPSE_TIME);

        if (!isEclipse) {
            if (this.hour >= 0 && this.hour < 5) {
                this.setTimeOfDay(GameConstants.NIGHT_TIME);
            } else if (this.hour >= 5 && this.hour < 7) {
                this.setTimeOfDay(GameConstants.DAWN_TIME);
            } else if (this.hour >= 7 && this.hour < 18) {
                this.setTimeOfDay(GameConstants.DAY_TIME);
            } else if (this.hour >= 18 && this.hour < 20) {
                this.setTimeOfDay(GameConstants.DUSK_TIME);
            } else if (this.hour >= 20 && this.hour <= 23) {
                this.setTimeOfDay(GameConstants.NIGHT_TIME);
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

        if (this.getSpawnHex() != null && this.getSpawnHex().getColony() != null) {
            this.getSpawnHex().getColony().runDailyJobs(this.getTemperatureIcon(), this.getSpawnHex().getBiome());
        }

        randomizeWeather();

        if (random.nextInt(1000) == 0) {
            if (random.nextBoolean()) {
                this.setTimeOfDay(GameConstants.SOLAR_ECLIPSE_TIME);
            } else {
                this.setTimeOfDay(GameConstants.LUNAR_ECLIPSE_TIME);
            }
            
            if (this.getSpawnHex() != null && this.getSpawnHex().getColony() != null) {
                this.getSpawnHex().getColony().getLabourService().runNuptial(this.getSpawnHex().getColony());
                this.getSpawnHex().getColony().logEvent("The Eclipse has triggered a spontaneous Nuptial Flight!");
            }
            
        } else {
            this.setTimeOfDay(GameConstants.NIGHT_TIME);
        }

        if (this.day >= 1 && this.day < 2) {
            this.moonPhase = GameConstants.NEW_MOON_PHASE;
        } else if (this.day >= 2 && this.day < 8) {
            this.moonPhase = GameConstants.WAXING_CRESCENT_PHASE;
        } else if (this.day >= 8 && this.day < 9) {
            this.moonPhase = GameConstants.FIRST_QUARTER_PHASE;
        } else if (this.day >= 9 && this.day < 15) {
            this.moonPhase = GameConstants.WAXING_GIBBOUS_PHASE;
        } else if (this.day >= 15 && this.day < 16) {
            this.moonPhase = GameConstants.FULL_MOON_PHASE;
        } else if (this.day >= 16 && this.day < 22) {
            this.moonPhase = GameConstants.WANING_GIBBOUS_PHASE;
        } else if (this.day >= 22 && this.day < 23) {
            this.moonPhase = GameConstants.LAST_QUARTER_PHASE;
        } else if (this.day >= 23 && this.day < 30) {
            this.moonPhase = GameConstants.WANING_CRESCENT_PHASE;
        } else {
            this.moonPhase = GameConstants.NEW_MOON_PHASE;
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

        if (this.getSpawnHex() != null && this.getSpawnHex().getColony() != null) {
            this.getSpawnHex().getColony().runMonthlyJobs();
        }

        if (this.month >= 1 && this.month < 4) {
            this.setSeason(GameConstants.SPRING_SEASON);
        } else if (this.month >= 4 && this.month < 7) {
            this.setSeason(GameConstants.SUMMER_SEASON);
        } else if (this.month >= 7 && this.month < 10) {
            this.setSeason(GameConstants.AUTUMN_SEASON);
        } else if (this.month >= 10 && this.month < 12) {
            this.setSeason(GameConstants.WINTER_SEASON);
        } else {
            this.setSeason(GameConstants.SPRING_SEASON);
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

        if (this.getSpawnHex() != null && this.getSpawnHex().getColony() != null) {
            this.getSpawnHex().getColony().runYearlyJobs();
        }
    }
}