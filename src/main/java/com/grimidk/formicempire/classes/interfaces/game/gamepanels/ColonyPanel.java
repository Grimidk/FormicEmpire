package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.services.ColonyLocationService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import javax.swing.*;
import java.awt.*;

public class ColonyPanel extends ZeroGamePanel {

    // --- Container Components ---
    private JPanel contentPanel;
    private JPanel emptyPanel;
    private boolean isShowingContent = true;

    // --- Rank Components ---
    private final JLabel rankLabel = new JLabel(LanguageStrings.get(LanguageStrings.COLONY_RANK));
    
    // --- Resources Components ---
    private final JLabel totalResourcesLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_TOTAL_RESOURCES), 0));
    private final JLabel mushroomsLabel = new JLabel("0");
    private final JLabel plantLabel = new JLabel("0");
    private final JLabel proteinLabel = new JLabel("0");
    private final JLabel waterLabel = new JLabel("0");
    private final JLabel syrupLabel = new JLabel("0");
    private final JLabel resinLabel = new JLabel("0");
    private final JLabel mineralLabel = new JLabel("0");
    
    // --- Ant Components ---
    private final JLabel totalAntLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_TOTAL_ANTS), 0));
    private final JLabel queensLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_QUEENS), 0, 0));
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
    private final JLabel totalConsumptionLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_CONSUMPTION), 0));
    private final JLabel totalProductionLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_MAX_FOOD_PROD), 0));
    private final JLabel netMushroomsLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_NET_FOOD), 0));
    private final JLabel layingRateLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_LAYING_RATE), 0));
    private final JLabel nurseCoverageLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_NURSE_COVERAGE), 0, 0));
    private final JLabel graveKeepingLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_GRAVE_CLEANING), 0, 0));
    private final JLabel aphidCountLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_APHIDS), 0, 0));
    private final JLabel parasiteCountLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_PARASITES), LanguageStrings.get(LanguageStrings.WORLD_NA))); 
    private final JLabel policeStatsLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_POLICING), 0)); 
    private final JLabel researchPointsLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_RESEARCH), 0));
    private final JLabel researchRateLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_RESEARCH_RATE), 0));
    private final JLabel babyAntsLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_JUVENILE_ANTS), 0));
    private final JLabel adultAntsLabel = new JLabel(String.format(LanguageStrings.get(LanguageStrings.COLONY_ADULT_ANTS), 0));
    
    // --- Cached Values ---
    private Colony lastColonyRef;
    private Engine engine;
    
    private int lastMushrooms = -1;
    private int lastPlants = -1;
    private int lastProtein = -1;
    private int lastWater = -1; 
    private int lastSyrups = -1; 
    private int lastResins = -1; 
    private int lastMinerals = -1; 
    
    private int lastPlantsAvailable = -1;
    private int lastProteinAvailable = -1;
    private int lastWaterAvailable = -1;
    private int lastMineralsAvailable = -1;
    
    private int lastTotalConsumption = -1;
    private int lastEggs = -1;
    private ColonyRank lastRank = null;

    public ColonyPanel() {
        super(new CardLayout()); 
        initComponents();
        initLayout();
    }
    
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @Override
    protected void initComponents() {
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
        parasiteCountLabel.setVisible(false);
        policeStatsLabel.setVisible(false);
        researchPointsLabel.setVisible(false);
        researchRateLabel.setVisible(false);

        // Resources Setup
        setupConstantLabel(mushroomsLabel, GameConstants.RESOURCE_FUNGI);
        setupConstantLabel(plantLabel, GameConstants.RESOURCE_PLANT);
        setupConstantLabel(proteinLabel, GameConstants.RESOURCE_MEAT);
        setupConstantLabel(waterLabel, GameConstants.RESOURCE_WATER);
        setupConstantLabel(syrupLabel, GameConstants.RESOURCE_SYRUP);
        setupConstantLabel(resinLabel, GameConstants.RESOURCE_RESIN);
        setupConstantLabel(mineralLabel, GameConstants.RESOURCE_ROCK);
        
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

        // Special Icons & Tooltips
        aphidCountLabel.setIcon(GameConstants.ICON_APHID);
        parasiteCountLabel.setIcon(GameConstants.TYPE_PARASITE.getIcon());
        policeStatsLabel.setIcon(GameConstants.TYPE_SOLDIER.getIcon());
        researchPointsLabel.setIcon(GameConstants.ICON_RESEARCH);
        
        updateTooltips();
        
        // Panels
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        
        emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
        JLabel noColonyLabel = new JLabel(LanguageStrings.get(LanguageStrings.PANEL_NO_COLONY), SwingConstants.CENTER);
        noColonyLabel.setForeground(AssetStyles.BACKGROUND_SECONDARY);
        emptyPanel.add(noColonyLabel, BorderLayout.CENTER);
    }
    
    private void updateTooltips() {
        mushroomsLabel.setToolTipText(GameConstants.RESOURCE_FUNGI.getName());
        plantLabel.setToolTipText(GameConstants.RESOURCE_PLANT.getName());
        proteinLabel.setToolTipText(GameConstants.RESOURCE_MEAT.getName());
        waterLabel.setToolTipText(GameConstants.RESOURCE_WATER.getName());
        syrupLabel.setToolTipText(GameConstants.RESOURCE_SYRUP.getName());
        resinLabel.setToolTipText(GameConstants.RESOURCE_RESIN.getName());
        mineralLabel.setToolTipText(GameConstants.RESOURCE_ROCK.getName());
        
        queensLabel.setToolTipText(GameConstants.TYPE_QUEEN.getName());
        princessLabel.setToolTipText(GameConstants.TYPE_PRINCESS.getName());
        droneLabel.setToolTipText(GameConstants.TYPE_DRONE.getName());
        majorLabel.setToolTipText(GameConstants.TYPE_MAJOR.getName());
        soldiersLabel.setToolTipText(GameConstants.TYPE_SOLDIER.getName());
        workersLabel.setToolTipText(GameConstants.TYPE_WORKER.getName());
        pupaLabel.setToolTipText(GameConstants.TYPE_PUPA.getName());
        larvaLabel.setToolTipText(GameConstants.TYPE_LARVA.getName());
        eggsLabel.setToolTipText(GameConstants.TYPE_EGG.getName());
        deadAntsLabel.setToolTipText(GameConstants.TYPE_DEAD.getName());
        
        aphidCountLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_APHIDS));
        parasiteCountLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_PARASITES));
        policeStatsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_POLICING));
        researchPointsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_RESEARCH_POINTS));
    }

    private void setupConstantLabel(JLabel label, AntType type) {
        label.setIcon(type.getIcon());
        label.setForeground(AssetStyles.FONT_COLOR);
    }

    private void setupConstantLabel(JLabel label, ResourceType resource) { 
        label.setIcon(resource.getIcon());
        label.setForeground(AssetStyles.FONT_COLOR);
    }

    @Override
    protected void initLayout() {
        contentPanel.add(createResourcesDetailPanel());
        contentPanel.add(createAntsDetailPanel());
        contentPanel.add(createColonyStatsPanel());
        add(contentPanel, "CONTENT");
        add(emptyPanel, "EMPTY");
    }
    
    private void setView(boolean showContent) {
        if (this.isShowingContent == showContent) return;
        CardLayout cl = (CardLayout) getLayout();
        if (showContent) {
            cl.show(this, "CONTENT");
        } else {
            cl.show(this, "EMPTY");
        }
        this.isShowingContent = showContent;
    }
    
    // --- Reset Method ---
    public void reset() {
        lastColonyRef = null;
        lastMushrooms = -1;
        lastPlants = -1;
        lastProtein = -1;
        lastWater = -1;
        lastSyrups = -1;
        lastResins = -1;
        lastMinerals = -1;
        lastPlantsAvailable = -1;
        lastProteinAvailable = -1;
        lastWaterAvailable = -1;
        lastMineralsAvailable = -1;
        lastTotalConsumption = -1;
        lastEggs = -1;
        lastRank = null;
        
        setView(false);
    }
    
    @Override
    public void refreshTranslations() {
        super.refreshTranslations();
        updateTooltips();
        if (lastColonyRef != null) {
            // Force refresh text fields
            lastMushrooms = -1;
            lastTotalConsumption = -1;
            lastRank = null;
            updateMinuteData(lastColonyRef);
            updateHourData(lastColonyRef);
            updateDayData(lastColonyRef);
        }
    }
    
    private JPanel createResourcesDetailPanel() {
        JPanel panel = createTitledPanel(LanguageStrings.PANEL_RESOURCES, new BoxLayout(null, BoxLayout.Y_AXIS));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); 
        totalResourcesLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(totalResourcesLabel);
        panel.add(AssetStyles.createInternalSeparator());
        panel.add(mushroomsLabel);
        panel.add(plantLabel);
        panel.add(proteinLabel);
        panel.add(waterLabel);
        panel.add(syrupLabel);
        panel.add(resinLabel);
        panel.add(mineralLabel);
        return panel;
    }
    
    private JPanel createAntsDetailPanel() {
        JPanel panel = createTitledPanel(LanguageStrings.PANEL_ANTS, new BoxLayout(null, BoxLayout.Y_AXIS));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        totalAntLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(totalAntLabel);
        panel.add(AssetStyles.createInternalSeparator());
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
        JPanel panel = createTitledPanel(LanguageStrings.PANEL_COLONY_STATS, new BoxLayout(null, BoxLayout.Y_AXIS));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rankLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(rankLabel);
        panel.add(AssetStyles.createInternalSeparator());
        
        totalConsumptionLabel.setForeground(AssetStyles.FONT_COLOR);
        totalProductionLabel.setForeground(AssetStyles.FONT_COLOR);
        netMushroomsLabel.setForeground(AssetStyles.FONT_COLOR);
        layingRateLabel.setForeground(AssetStyles.FONT_COLOR);
        nurseCoverageLabel.setForeground(AssetStyles.FONT_COLOR);
        graveKeepingLabel.setForeground(AssetStyles.FONT_COLOR);
        aphidCountLabel.setForeground(AssetStyles.FONT_COLOR);
        parasiteCountLabel.setForeground(AssetStyles.FONT_COLOR);
        policeStatsLabel.setForeground(AssetStyles.FONT_COLOR);
        researchPointsLabel.setForeground(AssetStyles.FONT_COLOR);
        researchRateLabel.setForeground(AssetStyles.FONT_COLOR);
        babyAntsLabel.setForeground(AssetStyles.FONT_COLOR);
        adultAntsLabel.setForeground(AssetStyles.FONT_COLOR);

        panel.add(totalConsumptionLabel);
        panel.add(totalProductionLabel);
        panel.add(netMushroomsLabel);
        panel.add(layingRateLabel);
        panel.add(nurseCoverageLabel);
        panel.add(graveKeepingLabel);
        panel.add(aphidCountLabel);
        panel.add(parasiteCountLabel);
        panel.add(policeStatsLabel);
        panel.add(researchPointsLabel);
        panel.add(researchRateLabel);
        panel.add(babyAntsLabel);
        panel.add(adultAntsLabel);
        return panel;
    }

    public void updateMinuteData(Colony colony) {
        if (colony == null) {
            setView(false);
            return;
        }
        this.lastColonyRef = colony;
        setView(true);

        int mushrooms = colony.getMushrooms(); 
        int plants = colony.getPlants();     
        int protein = colony.getProtein();  
        int water = colony.getWater(); 
        int syrups = colony.getSyrups();
        int resins = colony.getResins();
        int minerals = colony.getMinerals();
        int totalResources = mushrooms + plants + protein + water + syrups + resins + minerals;
        
        ColonyLocationService locService = colony.getLocationService();
        int plantsAvail = locService != null ? locService.getTotalQuantityAvailable(GameConstants.RESOURCE_PLANT) : 0;
        int proteinAvail = locService != null ? locService.getTotalQuantityAvailable(GameConstants.RESOURCE_MEAT) : 0;
        int waterAvail = locService != null ? locService.getTotalQuantityAvailable(GameConstants.RESOURCE_WATER) : 0;
        int mineralsAvail = locService != null ? locService.getTotalQuantityAvailable(GameConstants.RESOURCE_ROCK) : 0;

        totalResourcesLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_TOTAL_RESOURCES), totalResources));
        if (mushrooms != lastMushrooms) mushroomsLabel.setText(String.valueOf(mushrooms));
        
        if (plants != lastPlants || plantsAvail != lastPlantsAvailable) {
            plantLabel.setText(plants + " / (" + plantsAvail + ")");
            lastPlantsAvailable = plantsAvail;
        }
        
        if (protein != lastProtein || proteinAvail != lastProteinAvailable) {
            proteinLabel.setText(protein + " / (" + proteinAvail + ")");
            lastProteinAvailable = proteinAvail;
        }
        
        if (water != lastWater || waterAvail != lastWaterAvailable) {
            waterLabel.setText(water + " / (" + waterAvail + ")");
            lastWaterAvailable = waterAvail;
        }

        boolean hasRanching = colony.hasUpgrade(GameUnlocks.ROLE_RANCHER);
        syrupLabel.setVisible(hasRanching);
        if (hasRanching && syrups != lastSyrups) syrupLabel.setText(String.valueOf(syrups));

        boolean hasResinResonation = colony.hasUpgrade(GameUnlocks.ABILITY_RESIN);
        resinLabel.setVisible(hasResinResonation);
        if (hasResinResonation && resins != lastResins) resinLabel.setText(String.valueOf(resins));

        boolean hasMining = colony.hasUpgrade(GameUnlocks.ROLE_MINER);
        mineralLabel.setVisible(hasMining);
        if (hasMining && (minerals != lastMinerals || mineralsAvail != lastMineralsAvailable)) {
             mineralLabel.setText(minerals + " / (" + mineralsAvail + ")");
             lastMineralsAvailable = mineralsAvail;
        }

        int totalConsumption = colony.getTotalConsumption();
        int netMushrooms = colony.getTotalProduction() - totalConsumption;
        netMushroomsLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_NET_FOOD), netMushrooms));
        
        lastMushrooms = mushrooms; 
        lastPlants = plants; 
        lastProtein = protein; 
        lastWater = water; 
        lastSyrups = syrups; 
        lastResins = resins; 
        lastMinerals = minerals; 
    }

    public void updateHourData(Colony colony) {
        if (colony == null) {
            setView(false);
            return;
        }
        this.lastColonyRef = colony;

        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        if (eggs != lastEggs) eggsLabel.setText(String.valueOf(eggs));
        
        boolean hasHunter = colony.hasUpgrade(GameUnlocks.ROLE_HUNTER);
        proteinLabel.setVisible(hasHunter);
        
        int totalConsumption = colony.getTotalConsumption();
        if (totalConsumption != lastTotalConsumption) totalConsumptionLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_CONSUMPTION), totalConsumption));
        totalProductionLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_MAX_FOOD_PROD), colony.getTotalProduction()));

        boolean hasLayer = colony.hasUpgrade(GameUnlocks.ROLE_LAYER);
        layingRateLabel.setVisible(hasLayer);
        if (hasLayer) {
            int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            int hourlyLayingRate = layerCount * (int) colony.getLayingRate();
            layingRateLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_LAYING_RATE), hourlyLayingRate * 24));
        }
        
        boolean hasNurse = colony.hasUpgrade(GameUnlocks.ROLE_NURSE);
        nurseCoverageLabel.setVisible(hasNurse);
        if (hasNurse) {
            int nurseCount = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                nurseCount += 2;
            } else {
                nurseCount += 1;
            }
            int babyAntTotal = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
            int nurseCapacity = (int) (nurseCount * colony.getNursingRate());
            nurseCoverageLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_NURSE_COVERAGE), babyAntTotal, nurseCapacity));
        }

        boolean hasResearcher = colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER);
        boolean hasAssistant = colony.hasUpgrade(GameUnlocks.ROLE_ASSISTANT);
        researchPointsLabel.setVisible(hasResearcher || hasAssistant);
        researchRateLabel.setVisible(hasResearcher || hasAssistant);
        
        if (hasResearcher || hasAssistant) {
            researchPointsLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_RESEARCH), colony.getResearchPoints()));
            int researcherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
            if (colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
                if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                    researcherCount += 2;
                } else {
                    researcherCount += 1;
                }
            }
            int assistantCount = colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT);
            int speed = colony.getResearchSpeed();
            int hourlyQueen = researcherCount * speed;
            int hourlyAssistant = (int) (assistantCount * (speed / 5.0)); 
            researchRateLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_RESEARCH_RATE), (hourlyQueen + hourlyAssistant) * 24));
        }

        lastEggs = eggs; 
        lastTotalConsumption = totalConsumption;
    }

    public void updateDayData(Colony colony) {
        if (colony == null) {
            setView(false);
            return;
        }
        this.lastColonyRef = colony;
        
        int totalAnts = colony.getAntTotal();
        totalAntLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_TOTAL_ANTS), totalAnts));
        
        ColonyRank currentRank = colony.getRank();
        if (currentRank != lastRank) {
            rankLabel.setText(currentRank.getName());
            rankLabel.setIcon(currentRank.getIcon());
            lastRank = currentRank;
        }

        queensLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_QUEEN));
        int queenCount = colony.getQueens() != null ? colony.getQueens().size() : 0;
        int queenCapacity = colony.getQueensCapacity();
        queensLabel.setText(queenCount + " / " + queenCapacity);
        
        boolean hasPrincess = colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS);
        princessLabel.setVisible(hasPrincess);
        princessLabel.setText(String.valueOf(colony.getPrincesses() != null ? colony.getPrincesses().size() : 0));
        droneLabel.setVisible(hasPrincess);
        droneLabel.setText(String.valueOf(colony.getDrones() != null ? colony.getDrones().size() : 0));
        
        majorLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_MAJOR));
        majorLabel.setText(String.valueOf(colony.getMajors() != null ? colony.getMajors().size() : 0));
        
        soldiersLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER));
        soldiersLabel.setText(String.valueOf(colony.getSoldiers() != null ? colony.getSoldiers().size() : 0));
        
        workersLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_WORKER));
        workersLabel.setText(String.valueOf(colony.getWorkers() != null ? colony.getWorkers().size() : 0));
        
        boolean hasBabies = colony.hasUpgrade(GameUnlocks.TYPE_EGG);
        pupaLabel.setVisible(hasBabies);
        pupaLabel.setText(String.valueOf(colony.getPupae() != null ? colony.getPupae().size() : 0));
        larvaLabel.setVisible(hasBabies);
        larvaLabel.setText(String.valueOf(colony.getLarvae() != null ? colony.getLarvae().size() : 0));
        eggsLabel.setVisible(hasBabies);
        
        deadAntsLabel.setText(String.valueOf(colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0));

        if (colony.getMushrooms() != lastMushrooms) mushroomsLabel.setText(String.valueOf(colony.getMushrooms()));
        
        boolean hasGraver = colony.hasUpgrade(GameUnlocks.ROLE_GRAVER);
        graveKeepingLabel.setVisible(hasGraver);
        if(hasGraver) {
            int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
            if (colony.hasBuilding(GameUnlocks.PASSIVE_GRAVE)) {
                if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                    graverCount += 2;
                } else {
                    graverCount += 1;
                }
            }
            int graveCapacity = graverCount * (int) colony.getGravingRate();
            int currentDead = colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0;
            graveKeepingLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_GRAVE_CLEANING), currentDead, graveCapacity));
        }

        boolean hasRancher = colony.hasUpgrade(GameUnlocks.ROLE_RANCHER);
        aphidCountLabel.setVisible(hasRancher);
        if (hasRancher) {
            int rancherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
            if (colony.hasBuilding(GameUnlocks.PASSIVE_APHID)) {
                if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) {
                    rancherCount += 2;
                } else {
                    rancherCount += 1;
                }
            }
            int maxSustainableAphids = colony.getAphidCapacity() * rancherCount;
            aphidCountLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_APHIDS), colony.getAphids(), maxSustainableAphids));
        }
        
        boolean hasPolice = colony.hasUpgrade(GameUnlocks.ROLE_POLICE);
        parasiteCountLabel.setVisible(hasPolice);
        policeStatsLabel.setVisible(hasPolice);
        if (hasPolice) {
            boolean fuzz = engine != null && engine.isFuzzParasites();
            parasiteCountLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_PARASITES), colony.getParasiteCountDisplay(fuzz)));
            float detection = colony.getStatsService().getParasiteDetection(colony);
            int policeCount = colony.getAssignedRoleCount(GameConstants.ROLE_POLICE);
            policeStatsLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_DETECTION_RATE), Math.round(policeCount * detection)));
        }
        
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        int babyTotal = eggs + larva + pupa;
        babyAntsLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_JUVENILE_ANTS), babyTotal));

        int adultTotal = (colony.getQueens().size() + colony.getPrincesses().size() + colony.getDrones().size() + colony.getMajors().size() + colony.getSoldiers().size() + colony.getWorkers().size());
        adultAntsLabel.setText(String.format(LanguageStrings.get(LanguageStrings.COLONY_ADULT_ANTS), adultTotal));

        lastMushrooms = colony.getMushrooms(); 
    }
}
