package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.world.MoonPhase;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.TimeOfDay;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import java.awt.*;

public class WorldPanel extends ZeroGamePanel {
    
    // --- Time Components ---
    private final JLabel dateTimeLabel = new JLabel("00:00 01/01/0000");
    private final JLabel timeOfDayLabel = new JLabel(); 
    private final JLabel moonPhaseLabel = new JLabel(); 
    private final JLabel seasonLabel = new JLabel();    
    private final JLabel weatherLabel = new JLabel();  

    // --- World Components ---
    private final JLabel biomeLabel = new JLabel(); 
    private final JLabel temperatureLabel = new JLabel(); 
    private final JLabel humidityLabel = new JLabel(); 

    // --- Cached Values ---
    private String lastDateTime = "";
    private TimeOfDay lastTimeOfDay = null;
    private MoonPhase lastMoonPhase = null;
    private Season lastSeason = null;
    private Weather lastWeather = null;
    private int lastTemperature = -999;
    private int lastHumidity = -1;
    
    private World lastWorldRef;

    public WorldPanel() {
        super(new GridBagLayout());
        initComponents();
        initLayout();
    }
    
    @Override
    protected void initComponents() {
        biomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        temperatureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        humidityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeOfDayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        moonPhaseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        seasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        weatherLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    @Override
    protected void initLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        
        JPanel timePanel = createTimePanel();
        JPanel infoPanel = createWorldInfoPanel();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        add(timePanel, gbc);
        
        gbc.gridy = 1;
        gbc.weighty = 0.0; 
        add(infoPanel, gbc);
        
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(Box.createGlue(), gbc);
    }
    
    public void reset() {
        lastDateTime = "";
        lastTimeOfDay = null;
        lastMoonPhase = null;
        lastSeason = null;
        lastWeather = null;
        lastTemperature = -999;
        lastHumidity = -1;
        lastWorldRef = null;
        
        dateTimeLabel.setText("00:00 01/01/0000");
        biomeLabel.setText("");
        biomeLabel.setIcon(null);
        temperatureLabel.setText("");
        temperatureLabel.setIcon(null);
        humidityLabel.setText("");
        humidityLabel.setIcon(null);
        timeOfDayLabel.setIcon(null);
        moonPhaseLabel.setIcon(null);
        seasonLabel.setIcon(null);
        weatherLabel.setIcon(null);
    }
    
    @Override
    public void refreshTranslations() {
        super.refreshTranslations();
        if (lastWorldRef != null) {
            // Force refresh of localized tooltips
            updateStaticData(lastWorldRef);
            
            // Re-apply tooltips for time/weather/etc
            if (lastTimeOfDay != null) timeOfDayLabel.setToolTipText(lastTimeOfDay.getName());
            if (lastWeather != null) weatherLabel.setToolTipText(lastWeather.getName());
            if (lastMoonPhase != null) moonPhaseLabel.setToolTipText(lastMoonPhase.getName());
            if (lastSeason != null) seasonLabel.setToolTipText(lastSeason.getName());
            
            temperatureLabel.setToolTipText(LanguageStrings.get(LanguageStrings.WORLD_TEMP_PREFIX) + lastTemperature + "°C" + " (" + lastWorldRef.getTemperatureIcon().getName() + ")");
            humidityLabel.setToolTipText(LanguageStrings.get(LanguageStrings.WORLD_HUMIDITY_PREFIX) + lastWorldRef.getHumidityIcon().getName());
        }
    }
    
    private JPanel createTimePanel() {
        JPanel panel = createTitledPanel(LanguageStrings.PANEL_TIME, null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(dateTimeLabel);
        panel.add(timeOfDayLabel);
        panel.add(moonPhaseLabel);
        panel.add(weatherLabel);
        panel.add(seasonLabel);
        return panel;
    }

    private JPanel createWorldInfoPanel() {
        JPanel panel = createTitledPanel(LanguageStrings.PANEL_WORLD, null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(biomeLabel);
        panel.add(temperatureLabel);
        panel.add(humidityLabel);
        return panel;
    }
    
    public void updateStaticData(World world) {
        if (world == null || world.getActiveHex() == null) return;
        this.lastWorldRef = world;
        
        String name = (world.getActiveHex().getBiome() != null) ? world.getActiveHex().getBiome().getName() : LanguageStrings.get(LanguageStrings.WORLD_NA);
        String fullTooltip = LanguageStrings.get(LanguageStrings.WORLD_BIOME_PREFIX) + name;
        
        biomeLabel.setText(name); 
        biomeLabel.setToolTipText(fullTooltip);
        if (world.getActiveHex().getBiome() != null) {
            biomeLabel.setIcon(world.getActiveHex().getBiome().getIcon());
        }
    }
    
    public void updateMinuteData(World world) {
        String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
            world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
        if (!dateTime.equals(lastDateTime)) {
            dateTimeLabel.setText(dateTime);
            lastDateTime = dateTime;
        }
    }
    
    public void updateHourData(World world) {
        this.lastWorldRef = world;
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
        
        int temp = world.getTemperature();
        if (temp != lastTemperature) {
            temperatureLabel.setText("");
            temperatureLabel.setToolTipText(LanguageStrings.get(LanguageStrings.WORLD_TEMP_PREFIX) + temp + "°C" + " (" + world.getTemperatureIcon().getName() + ")");
            temperatureLabel.setIcon(world.getTemperatureIcon().getIcon());
            lastTemperature = temp;
        }
        int humidity = world.getHumidity();
        if (humidity != lastHumidity) {
            humidityLabel.setText("");
            humidityLabel.setToolTipText(LanguageStrings.get(LanguageStrings.WORLD_HUMIDITY_PREFIX) + world.getHumidityIcon().getName());
            humidityLabel.setIcon(world.getHumidityIcon().getIcon());
            lastHumidity = humidity;
        }
    }

    public void updateDayData(World world) {
        this.lastWorldRef = world;
        MoonPhase currentMoonPhase = world.getMoonPhase();
        if (currentMoonPhase != lastMoonPhase) {
            moonPhaseLabel.setIcon(currentMoonPhase.getIcon());
            moonPhaseLabel.setToolTipText(currentMoonPhase.getName());
            lastMoonPhase = currentMoonPhase;
        }
    }

    public void updateMonthData(World world) {
        this.lastWorldRef = world;
        Season currentSeason = world.getSeason();
        if (currentSeason != lastSeason) {
            seasonLabel.setIcon(currentSeason.getIcon());
            seasonLabel.setToolTipText(currentSeason.getName());
            lastSeason = currentSeason;
        }
    }
}
