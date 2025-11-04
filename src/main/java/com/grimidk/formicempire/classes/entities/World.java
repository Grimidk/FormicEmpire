package com.grimidk.formicempire.classes.entities;

import java.util.ArrayList;

import com.grimidk.formicempire.classes.constants.Biome;
import com.grimidk.formicempire.classes.constants.MoonPhase;
import com.grimidk.formicempire.classes.constants.Season;
import com.grimidk.formicempire.classes.constants.TimeOfDay;
import com.grimidk.formicempire.classes.constants.Weather;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

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
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
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
        return this.hexes.get(0);
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
    }

    public void runMinute() {
        this.minute++;

        this.getSpawnHex().getColony().runConverting();

        if (this.minute > 59) {
            this.minute = 0;
            this.runHour();
        }
    }

    public void runHour() {
        this.hour++;

        this.getSpawnHex().getColony().runRoleAssignment();
        this.getSpawnHex().getColony().runCollecting();;
        this.getSpawnHex().getColony().runLaying();
        this.getSpawnHex().getColony().runRanching();

        if (this.hour >= 0 && this.hour < 5) {
            this.timeOfDay = GameConstants.NIGHT_TIME;
        } else if (this.hour >= 5 && this.hour < 7) {
            this.timeOfDay = GameConstants.DAWN_TIME;
        } else if (this.hour >= 7 && this.hour < 18) {
            this.timeOfDay = GameConstants.DAY_TIME;
        } else if (this.hour >= 18 && this.hour < 20) {
            this.timeOfDay = GameConstants.DUSK_TIME;
        } else if (this.hour >= 20 && this.hour <= 23) {
            this.timeOfDay = GameConstants.NIGHT_TIME;
        }

        // --- Notify Hour Listeners ---
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

        this.getSpawnHex().getColony().runEating();
        this.getSpawnHex().getColony().runHatching();
        this.getSpawnHex().getColony().runAging();
        this.getSpawnHex().getColony().runNursing();
        this.getSpawnHex().getColony().runGraveKeeping();

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

        // --- Notify Day Listeners ---
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
            this.season = GameConstants.SPRING_SEASON;
        } else if (this.month >= 4 && this.month < 7) {
            this.season = GameConstants.SUMMER_SEASON;
        } else if (this.month >= 7 && this.month < 10) {
            this.season = GameConstants.AUTUMN_SEASON;
        } else if (this.month >= 10 && this.month < 12) {
            this.season = GameConstants.WINTER_SEASON;
        } else {
            this.season = GameConstants.SPRING_SEASON;
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

        // --- Notify Month Listeners ---
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

        this.getSpawnHex().getColony().runNuptial();
    }

}