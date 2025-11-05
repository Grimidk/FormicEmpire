package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.MoonPhase;
import com.grimidk.formicempire.classes.constants.Season;
import com.grimidk.formicempire.classes.constants.TimeOfDay;
import com.grimidk.formicempire.classes.constants.Weather;
import com.grimidk.formicempire.classes.entities.World;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class WorldPanel extends JPanel {
    
    // --- Time Components ---
    private final JLabel dateTimeLabel = new JLabel("00:00 01/01/0000");
    private final JLabel timeOfDayLabel = new JLabel(); 
    private final JLabel moonPhaseLabel = new JLabel(); 
    private final JLabel seasonLabel = new JLabel();    
    private final JLabel weatherLabel = new JLabel();  

    // --- World Components ---
    private final JLabel biomeLabel = new JLabel("Biome: N/A");
    private final JLabel temperatureLabel = new JLabel("Temp: 0°C");
    private final JLabel humidityLabel = new JLabel("Humidity: 0");

    // --- Cached Values ---
    private String lastDateTime = "";
    private TimeOfDay lastTimeOfDay = null;
    private MoonPhase lastMoonPhase = null;
    private Season lastSeason = null;
    private Weather lastWeather = null;
    private String lastBiome = "";
    private int lastTemperature = -999;
    private int lastHumidity = -1;

    public WorldPanel() {
        initLayout();
    }
    
    private void initLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel timePanel = createTimePanel();
        JPanel infoPanel = createWorldInfoPanel();
        
        add(timePanel);
        add(infoPanel);
    }
    
    private JPanel createTimePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Time"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(dateTimeLabel);
        panel.add(timeOfDayLabel);
        panel.add(moonPhaseLabel);
        panel.add(weatherLabel);
        panel.add(seasonLabel);
        return panel;
    }

    private JPanel createWorldInfoPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("World"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(biomeLabel);
        panel.add(temperatureLabel);
        panel.add(humidityLabel);
        return panel;
    }
    
    // --- Update Methods ---

    public void updateStaticData(World world) {
        if (world == null || world.getSpawnHex() == null) return;
        
        // Biome
        String biomeName = (world.getSpawnHex().getBiome() != null) ? "Biome: " + world.getSpawnHex().getBiome().getName() : "Biome: N/A";
        if (!biomeName.equals(lastBiome)) {
            biomeLabel.setText(biomeName);
            lastBiome = biomeName;
        }
    }
    
    public void updateMinuteData(World world) {
        // Date/Time
        String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
            world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
        if (!dateTime.equals(lastDateTime)) {
            dateTimeLabel.setText(dateTime);
            lastDateTime = dateTime;
        }
    }
    
    public void updateHourData(World world) {
        // Time of Day and Weather
        TimeOfDay currentTimeOfDay = world.getTimeOfDay();
        if (currentTimeOfDay != lastTimeOfDay) {
            timeOfDayLabel.setIcon(currentTimeOfDay.getIcon());
            timeOfDayLabel.setToolTipText(currentTimeOfDay.getName());
            lastTimeOfDay = currentTimeOfDay;
        }
        Weather currentWeather = world.getWeather();
        if (currentWeather != lastWeather) {
            weatherLabel.setIcon(currentWeather.getIcon());
            weatherLabel.setToolTipText(currentWeather.getName());
            lastWeather = currentWeather;
        }
        
        // Temperature & Humidity
        int temp = world.getTemperature();
        if (temp != lastTemperature) {
            temperatureLabel.setText("Temp: " + temp + "°C");
            lastTemperature = temp;
        }
        int humidity = world.getHumidity();
        if (humidity != lastHumidity) {
            humidityLabel.setText("Humidity: " + humidity);
            lastHumidity = humidity;
        }
    }

    public void updateDayData(World world) {
        // Moon Phase
        MoonPhase currentMoonPhase = world.getMoonPhase();
        if (currentMoonPhase != lastMoonPhase) {
            moonPhaseLabel.setIcon(currentMoonPhase.getIcon());
            moonPhaseLabel.setToolTipText(currentMoonPhase.getName());
            lastMoonPhase = currentMoonPhase;
        }
    }

    public void updateMonthData(World world) {
        // Season
        Season currentSeason = world.getSeason();
        if (currentSeason != lastSeason) {
            seasonLabel.setIcon(currentSeason.getIcon());
            seasonLabel.setToolTipText(currentSeason.getName());
            lastSeason = currentSeason;
        }
    }
}