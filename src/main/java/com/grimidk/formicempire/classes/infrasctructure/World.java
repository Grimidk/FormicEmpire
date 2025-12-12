package com.grimidk.formicempire.classes.infrasctructure;

import java.util.ArrayList;
import java.util.List;
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

    public Hex getSpawnHex() {
        if (this.hexes.isEmpty()) return null;
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
    
    public void generateWorld() {

    }

    public void startWorld(Biome biome, Colony colony) {
        Hex startHex = new Hex();
        startHex.setBiome(biome);
        startHex.setColony(colony);
        if (colony.getAntTotal() == 0) {
            colony.startColony();
        }
        this.hexes.add(startHex);
        updateEnvironmentalConditions();
    }

    public void loadWorld(Savefile savefile) {
        this.minute = savefile.getMinute();
        this.hour = savefile.getHour();
        this.day = savefile.getDay();
        this.month = savefile.getMonth();
        this.year = savefile.getYear();  
        Colony colony = new Colony(savefile);  
        Hex startHex = new Hex();
        startHex.setColony(colony);
        this.hexes.add(startHex);
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

        if (this.weather != null) {
            calcTemp *= this.weather.getTempMult();
            calcHumid += this.weather.getHumidMult(); 
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
            return;
        }

        List<Weather> possibleWeathers = new ArrayList<>();
        
        possibleWeathers.add(GameConstants.CLEAR_WEATHER);
        possibleWeathers.add(GameConstants.CLEAR_WEATHER); 
        
        if (this.season == GameConstants.WINTER_SEASON) {
            // Winter Events
            possibleWeathers.add(GameConstants.SNOW_WEATHER);
            possibleWeathers.add(GameConstants.HEAVY_SNOW_WEATHER);
            possibleWeathers.add(GameConstants.WIND_WEATHER);
        } else if (this.season == GameConstants.SUMMER_SEASON) {
            // Summer Events
            possibleWeathers.add(GameConstants.RAIN_WEATHER);
            possibleWeathers.add(GameConstants.THUNDER_WEATHER);
            possibleWeathers.add(GameConstants.HEAT_WEATHER);
        } else {
            // Spring/Autumn Events
            possibleWeathers.add(GameConstants.RAIN_WEATHER);
            possibleWeathers.add(GameConstants.HEAVY_RAIN_WEATHER);
            possibleWeathers.add(GameConstants.WIND_WEATHER);
        }

        Weather newWeather = possibleWeathers.get(random.nextInt(possibleWeathers.size()));
        
        if (this.weather != newWeather) {
            this.setWeather(newWeather);
        }
    }

    public void runMinute() {
        this.minute++;

        if (this.getSpawnHex() != null) {
             this.getSpawnHex().getColony().runMinutelyJobs();
        }

        if (this.minute > 59) {
            this.minute = 0;
            this.runHour();
        }
    }

    public void runHour() {
        this.hour++;

        if (this.getSpawnHex() != null) {
            this.getSpawnHex().getColony().runHourlyJobs(getSpawnHex().getBiome());
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

        if (this.getSpawnHex() != null) {
            this.getSpawnHex().getColony().runDailyJobs(this.getTemperatureIcon(), this.getSpawnHex().getBiome());
        }

        randomizeWeather();

        if (random.nextInt(1000) == 0) {
            if (random.nextBoolean()) {
                this.setTimeOfDay(GameConstants.SOLAR_ECLIPSE_TIME);
            } else {
                this.setTimeOfDay(GameConstants.LUNAR_ECLIPSE_TIME);
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
        if (this.getSpawnHex() != null) {
            this.getSpawnHex().getColony().runYearlyJobs();
        }
    }
}