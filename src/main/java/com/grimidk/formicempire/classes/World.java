package com.grimidk.formicempire.classes;

import java.util.ArrayList;

public class World {

    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;
    private TimeOfDay timeOfDay;
    private Weather weather;
    private int temperature;
    private int humidity;
    private ArrayList<Hex> hexes;
    private int saveSlotId = 0; // 0 = no slot (ad-hoc)

    public World() {
        this.minute = 0;
        this.hour = 0;
        this.day = 0;
        this.month = 0;
        this.year = 0;
        this.temperature = 25;
        this.humidity = 2;
        this.hexes = new ArrayList<>();
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
    }

    public void runMinute() {
        this.minute++;
        if (this.minute > 59) {
            this.minute = 0;
            this.runHour();
        }
    }

    public void runHour() {
        this.hour++;
        if (this.hour > 23) {
            this.hour = 0;
            this.runDay();
        }

        try {
            if (this.hexes != null && !this.hexes.isEmpty()) {
                Hex spawn = this.getSpawnHex();
                if (spawn != null && spawn.getColony() != null) {
                    spawn.getColony().runHatching();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runDay() {
        this.day++;
        if (this.day > 29) {
            this.day = 0;
            this.runMonth();
        }
        try {
            SaveManager sm = new SaveManager();
            sm.saveAutosave(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runMonth() {
        this.month++;
        if (this.month > 11) {
            this.month = 0;
            this.runYear();
        }
    }

    public void runYear() {
        this.year++;
    }

}
