package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.ResourceType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class ColonyPanel extends JPanel {

    // --- Resources Components ---
    private final JLabel totalResourcesLabel = new JLabel("Total resources: 0");
    private final JLabel mushroomsLabel = new JLabel("0");
    private final JLabel planLabel = new JLabel("0");
    private final JLabel proteinLabel = new JLabel("0");
    private final JLabel waterLabel = new JLabel("0");
    private final JLabel syrupLabel = new JLabel("0");
    private final JLabel resinLabel = new JLabel("0");
    private final JLabel mineralLabel = new JLabel("0");
    // --- Ant Components ---
    private final JLabel totalAntLabel = new JLabel("Total ants: 0");
    private final JLabel queensLabel = new JLabel("0");
    private final JLabel princessLabel = new JLabel("0");
    private final JLabel droneLabel = new JLabel("0");
    private final JLabel majorLabel = new JLabel("0");
    private final JLabel soldiersLabel = new JLabel("0");
    private final JLabel workersLabel = new JLabel("0");
    private final JLabel pupaLabel = new JLabel("0");
    private final JLabel larvaLabel = new JLabel("0");
    private final JLabel eggsLabel = new JLabel("0");
    private final JLabel deadAntsLabel = new JLabel("0");
    // --- Stats Components ---
    private final JLabel totalConsumptionLabel = new JLabel("Consumption: 0/day");
    private final JLabel totalProductionLabel = new JLabel("Max Food Prod: 0/day");
    private final JLabel netMushroomsLabel = new JLabel("Net Food: 0/day");
    private final JLabel layingRateLabel = new JLabel("Laying Rate: 0/day");
    private final JLabel nurseCoverageLabel = new JLabel("Nurse Coverage: 0/0");
    private final JLabel graveKeepingLabel = new JLabel("Grave Capacity: 0");
    private final JLabel ranchingRateLabel = new JLabel("Ranching: 0");
    private final JLabel babyAntsLabel = new JLabel("Baby Ants: 0");
    private final JLabel adultAntsLabel = new JLabel("Adult Ants: 0");
    
    // --- Cached Values ---
    private int lastMushrooms = -1;
    private int lastPlants = -1;
    private int lastProtein = -1;
    private int lastWater = -1; 
    private int lastSyrups = -1; 
    private int lastResins = -1; 
    private int lastMinerals = -1; 
    private int lastTotalConsumption = -1;
    private int lastBabyTotal = -1;
    private int lastAdultTotal = -1;
    private int lastEggs = -1;


    public ColonyPanel() {
        initComponents();
        initLayout();
    }
    
    // Helper to configure labels 
    private void setupConstantLabel(JLabel label, AntType type) {
        label.setIcon(type.getIcon());
        label.setToolTipText(type.getName());
    }
    private void setupConstantLabel(JLabel label, ResourceType resource) { 
        label.setIcon(resource.getIcon());
        label.setToolTipText(resource.getName());
    }

    private void initComponents() {
        // Resources Setup
        setupConstantLabel(mushroomsLabel, GameConstants.FUNGI_RESOURCE);
        setupConstantLabel(planLabel, GameConstants.PLANT_RESOURCE);
        setupConstantLabel(proteinLabel, GameConstants.MEAT_RESOURCE);
        setupConstantLabel(waterLabel, GameConstants.WATER_RESOURCE);
        setupConstantLabel(syrupLabel, GameConstants.SYRUP_RESOURCE);
        setupConstantLabel(resinLabel, GameConstants.RESIN_RESOURCE);
        setupConstantLabel(mineralLabel, GameConstants.ROCK_RESOURCE);
        
        // Ants Setup
        setupConstantLabel(queensLabel, GameConstants.TYPE_QUEEN);
        setupConstantLabel(princessLabel, GameConstants.TYPE_PRINCESS);
        setupConstantLabel(droneLabel, GameConstants.TYPE_DRONE);
        setupConstantLabel(majorLabel, GameConstants.TYPE_MAJOR);
        setupConstantLabel(soldiersLabel, GameConstants.TYPE_SOLDIER);
        setupConstantLabel(workersLabel, GameConstants.TYPE_WORKER);
        setupConstantLabel(pupaLabel, GameConstants.TYPE_PUPA);
        setupConstantLabel(larvaLabel, GameConstants.TYPE_LARVA);
        setupConstantLabel(eggsLabel, GameConstants.TYPE_EGG);
        setupConstantLabel(deadAntsLabel, GameConstants.TYPE_DEAD);
    }

    private void initLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel resourcesPanel = createResourcesDetailPanel();
        JPanel antsPanel = createAntsDetailPanel();
        JPanel statsPanel = createColonyStatsPanel();

        add(resourcesPanel);
        add(antsPanel);
        add(statsPanel);
    }
    
    private JPanel createResourcesDetailPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Resources"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(totalResourcesLabel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        panel.add(mushroomsLabel);
        panel.add(planLabel);
        panel.add(proteinLabel);
        panel.add(waterLabel);
        panel.add(syrupLabel);
        panel.add(resinLabel);
        panel.add(mineralLabel);
        return panel;
    }
    
    private JPanel createAntsDetailPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Ants"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(totalAntLabel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        panel.add(queensLabel);
        panel.add(princessLabel);
        panel.add(droneLabel);
        panel.add(majorLabel);
        panel.add(soldiersLabel);
        panel.add(workersLabel);
        panel.add(pupaLabel);
        panel.add(larvaLabel);
        panel.add(eggsLabel);
        panel.add(deadAntsLabel);
        return panel;
    }

    private JPanel createColonyStatsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Colony Stats"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(totalConsumptionLabel);
        panel.add(totalProductionLabel);
        panel.add(netMushroomsLabel);
        panel.add(layingRateLabel);
        panel.add(nurseCoverageLabel);
        panel.add(graveKeepingLabel);
        panel.add(ranchingRateLabel);
        panel.add(babyAntsLabel);
        panel.add(adultAntsLabel);
        return panel;
    }

    // --- Update Methods ---

    public void updateMinuteData(Colony colony) {
        // Resources 
        int mushrooms = colony.getMushrooms(); 
        int plants = colony.getPlants();     
        int protein = colony.getProtein();  
        int water = colony.getWater(); 
        int syrups = colony.getSyrups();
        int resins = colony.getResins();
        int minerals = colony.getMinerals();
        int totalResources = mushrooms + plants + protein + water + syrups + resins + minerals;
        
        if (totalResources != -1) totalResourcesLabel.setText("Total resources: " + totalResources);
        if (mushrooms != lastMushrooms) mushroomsLabel.setText(String.valueOf(mushrooms));
        if (plants != lastPlants) planLabel.setText(String.valueOf(plants));
        if (protein != lastProtein) proteinLabel.setText(String.valueOf(protein));
        if (water != lastWater) waterLabel.setText(String.valueOf(water));
        if (syrups != lastSyrups) syrupLabel.setText(String.valueOf(syrups));
        if (resins != lastResins) resinLabel.setText(String.valueOf(resins));
        if (minerals != lastMinerals) mineralLabel.setText(String.valueOf(minerals));

        // Stats 
        int totalConsumption = colony.getTotalConsumption();
        int netMushrooms = colony.getTotalProduction() - totalConsumption;
        if (netMushrooms != -1) netMushroomsLabel.setText(String.format("Net Food: %d/day", netMushrooms));
        
        // Update cached values
        lastMushrooms = mushrooms; 
        lastPlants = plants; 
        lastProtein = protein; 
        lastWater = water; 
        lastSyrups = syrups; 
        lastResins = resins; 
        lastMinerals = minerals; 
    }

    public void updateHourData(Colony colony) {
        // Ants
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        if (eggs != lastEggs) eggsLabel.setText(String.valueOf(eggs));
        
        // Resources
        int plants = colony.getPlants();     
        int protein = colony.getProtein();  
        if (plants != lastPlants) planLabel.setText(String.valueOf(plants));
        if (protein != lastProtein) proteinLabel.setText(String.valueOf(protein));
        
        // Stats
        int totalConsumption = colony.getTotalConsumption();
        if (totalConsumption != lastTotalConsumption) totalConsumptionLabel.setText(String.format("Consumption: %d/day", totalConsumption));
        
        if (colony.getTotalProduction() != -1) totalProductionLabel.setText(String.format("Max Food Prod: %d/day", colony.getTotalProduction()));

        int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
        int hourlyLayingRate = layerCount * colony.getLayingRate();
        int layingRate = hourlyLayingRate * 24;
        if (layingRate != -1) layingRateLabel.setText(String.format("Laying Rate: %d/day", layingRate));

        int nurseCount = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
        int babyAntTotal = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        int nurseCapacity = (int) (nurseCount * colony.getNursingRate());
        nurseCoverageLabel.setText(String.format("Nurse Coverage: %d/%d", babyAntTotal, nurseCapacity));

        int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
        int graveCapacity = graverCount * (int) colony.getGravingRate();
        graveKeepingLabel.setText("Grave Capacity: " + graveCapacity);

        ranchingRateLabel.setText("Ranching: 0");

        lastEggs = eggs; 
        lastPlants = plants; 
        lastProtein = protein; 
        lastTotalConsumption = totalConsumption;
    }

    public void updateDayData(Colony colony) {
        int totalAnts = colony.getAntTotal();
        totalAntLabel.setText("Total ants: " + totalAnts);
        queensLabel.setText(String.valueOf(colony.getQueens() != null ? colony.getQueens().size() : 0));
        princessLabel.setText(String.valueOf(colony.getPrincesses() != null ? colony.getPrincesses().size() : 0));
        droneLabel.setText(String.valueOf(colony.getDrones() != null ? colony.getDrones().size() : 0));
        majorLabel.setText(String.valueOf(colony.getMajors() != null ? colony.getMajors().size() : 0));
        soldiersLabel.setText(String.valueOf(colony.getSoldiers() != null ? colony.getSoldiers().size() : 0));
        workersLabel.setText(String.valueOf(colony.getWorkers() != null ? colony.getWorkers().size() : 0));
        pupaLabel.setText(String.valueOf(colony.getPupae() != null ? colony.getPupae().size() : 0));
        larvaLabel.setText(String.valueOf(colony.getLarvae() != null ? colony.getLarvae().size() : 0));
        deadAntsLabel.setText(String.valueOf(colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0));

        if (colony.getMushrooms() != lastMushrooms) mushroomsLabel.setText(String.valueOf(colony.getMushrooms()));
        
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        int babyTotal = eggs + larva + pupa;
        if (babyTotal != lastBabyTotal) babyAntsLabel.setText("Baby Ants: " + babyTotal);

        int adultTotal = (colony.getQueens().size() + colony.getPrincesses().size() + colony.getDrones().size() + 
                          colony.getMajors().size() + colony.getSoldiers().size() + colony.getWorkers().size());
        if (adultTotal != lastAdultTotal) adultAntsLabel.setText("Adult Ants: " + adultTotal);

        lastMushrooms = colony.getMushrooms(); 
        lastBabyTotal = babyTotal; 
        lastAdultTotal = adultTotal; 
    }
}