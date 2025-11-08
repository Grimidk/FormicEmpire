package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.ResourceType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades; 

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
    private final JLabel graveKeepingLabel = new JLabel("Grave Cleaning: 0/0");
    private final JLabel aphidCountLabel = new JLabel("Aphids: 0/0");
    private final JLabel researchPointsLabel = new JLabel("Research: 0");
    private final JLabel researchRateLabel = new JLabel("Research Rate: 0/day");
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
        
        syrupLabel.setVisible(false);
        resinLabel.setVisible(false);
        mineralLabel.setVisible(false);
        queensLabel.setVisible(false);
        princessLabel.setVisible(false);
        droneLabel.setVisible(false);
        majorLabel.setVisible(false);
        soldiersLabel.setVisible(false);
        layingRateLabel.setVisible(false);
        nurseCoverageLabel.setVisible(false);
        graveKeepingLabel.setVisible(false);
        aphidCountLabel.setVisible(false);
        researchPointsLabel.setVisible(false);
        researchRateLabel.setVisible(false);
    }
    
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
        panel.add(aphidCountLabel);
        panel.add(researchPointsLabel);
        panel.add(researchRateLabel);
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

        // Show/Hide resource labels based on upgrades
        boolean hasRanching = colony.hasUpgrade(GameUpgrades.ROLE_RANCHER);
        syrupLabel.setVisible(hasRanching);
        if (hasRanching && syrups != lastSyrups) syrupLabel.setText(String.valueOf(syrups));

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
        
        boolean hasHunter = colony.hasUpgrade(GameUpgrades.ROLE_HUNTER);
        proteinLabel.setVisible(hasHunter);
        if (hasHunter && protein != lastProtein) proteinLabel.setText(String.valueOf(protein));
        
        // Stats
        int totalConsumption = colony.getTotalConsumption();
        if (totalConsumption != lastTotalConsumption) totalConsumptionLabel.setText(String.format("Consumption: %d/day", totalConsumption));
        
        if (colony.getTotalProduction() != -1) totalProductionLabel.setText(String.format("Max Food Prod: %d/day", colony.getTotalProduction()));

        boolean hasLayer = colony.hasUpgrade(GameUpgrades.ROLE_LAYER);
        layingRateLabel.setVisible(hasLayer);
        if (hasLayer) {
            int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            int hourlyLayingRate = layerCount * colony.getLayingRate();
            int layingRate = hourlyLayingRate * 24;
            layingRateLabel.setText(String.format("Laying Rate: %d/day", layingRate));
        }
        
        boolean hasNurse = colony.hasUpgrade(GameUpgrades.ROLE_NURSE);
        nurseCoverageLabel.setVisible(hasNurse);
        if (hasNurse) {
            int nurseCount = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
            int babyAntTotal = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
            int nurseCapacity = (int) (nurseCount * colony.getNursingRate());
            nurseCoverageLabel.setText(String.format("Nurse Coverage: %d/%d", babyAntTotal, nurseCapacity));
        }

        boolean hasResearcher = colony.hasUpgrade(GameUpgrades.ROLE_RESEARCHER);
        researchPointsLabel.setVisible(hasResearcher);
        researchRateLabel.setVisible(hasResearcher);
        if (hasResearcher) {
            researchPointsLabel.setText("Research: " + colony.getResearchPoints());
            int researcherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
            int researchPerDay = researcherCount * colony.getResearchSpeed() * 24;
            researchRateLabel.setText("Research Rate: " + researchPerDay + "/day");
        }

        lastEggs = eggs; 
        lastPlants = plants; 
        lastProtein = protein; 
        lastTotalConsumption = totalConsumption;
    }

    public void updateDayData(Colony colony) {
        int totalAnts = colony.getAntTotal();
        totalAntLabel.setText("Total ants: " + totalAnts);

        queensLabel.setVisible(colony.hasUpgrade(GameUpgrades.TYPE_QUEEN));
        queensLabel.setText(String.valueOf(colony.getQueens() != null ? colony.getQueens().size() : 0));
        
        boolean hasPrincess = colony.hasUpgrade(GameUpgrades.TYPE_PRINCESS);
        princessLabel.setVisible(hasPrincess);
        princessLabel.setText(String.valueOf(colony.getPrincesses() != null ? colony.getPrincesses().size() : 0));
        droneLabel.setVisible(hasPrincess);
        droneLabel.setText(String.valueOf(colony.getDrones() != null ? colony.getDrones().size() : 0));
        
        majorLabel.setVisible(colony.hasUpgrade(GameUpgrades.TYPE_MAJOR));
        majorLabel.setText(String.valueOf(colony.getMajors() != null ? colony.getMajors().size() : 0));
        
        soldiersLabel.setVisible(colony.hasUpgrade(GameUpgrades.TYPE_SOLDIER));
        soldiersLabel.setText(String.valueOf(colony.getSoldiers() != null ? colony.getSoldiers().size() : 0));
        
        workersLabel.setVisible(colony.hasUpgrade(GameUpgrades.TYPE_WORKER));
        workersLabel.setText(String.valueOf(colony.getWorkers() != null ? colony.getWorkers().size() : 0));
        
        boolean hasBabies = colony.hasUpgrade(GameUpgrades.TYPE_EGG);
        pupaLabel.setVisible(hasBabies);
        pupaLabel.setText(String.valueOf(colony.getPupae() != null ? colony.getPupae().size() : 0));
        larvaLabel.setVisible(hasBabies);
        larvaLabel.setText(String.valueOf(colony.getLarvae() != null ? colony.getLarvae().size() : 0));
        eggsLabel.setVisible(hasBabies);
        
        deadAntsLabel.setText(String.valueOf(colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0));

        if (colony.getMushrooms() != lastMushrooms) mushroomsLabel.setText(String.valueOf(colony.getMushrooms()));
        
        boolean hasGraver = colony.hasUpgrade(GameUpgrades.ROLE_GRAVER);
        graveKeepingLabel.setVisible(hasGraver);
        if(hasGraver) {
            int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
            int graveCapacity = graverCount * (int) colony.getGravingRate();
            int currentDead = colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0;
            graveKeepingLabel.setText(String.format("Grave Cleaning: %d/%d", currentDead, graveCapacity));
        }

        boolean hasRancher = colony.hasUpgrade(GameUpgrades.ROLE_RANCHER);
        aphidCountLabel.setVisible(hasRancher);
        if (hasRancher) {
            int rancherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
            int maxSustainableAphids = colony.getAphidCapacity() * rancherCount;
            aphidCountLabel.setText(String.format("Aphids: %d/%d", colony.getAphids(), maxSustainableAphids));
        }
        
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