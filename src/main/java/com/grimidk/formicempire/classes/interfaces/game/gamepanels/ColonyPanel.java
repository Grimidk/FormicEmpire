package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ColonyLoyalty;
import com.grimidk.formicempire.classes.constants.misc.ColonyRank;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLocationService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import java.awt.*;

public class ColonyPanel extends ZeroGamePanel {

    // --- Container Components ---
    private JPanel contentPanel;
    private JPanel emptyPanel;
    private boolean isShowingContent = true;

    // --- Rank Components ---
    private final JLabel colonyNameLabel = new JLabel("");
    private final JLabel warStatusLabel = new JLabel();
    private final JLabel rankLabel = new JLabel(LanguageStrings.get(LanguageStrings.COLONY_RANK));
    
    // --- Resources Components ---
    private final JLabel totalResourcesLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_TOTAL_RESOURCES, 0));
    private final JLabel mushroomsLabel = new JLabel("0");
    private final JLabel plantLabel = new JLabel("0");
    private final JLabel proteinLabel = new JLabel("0");
    private final JLabel waterLabel = new JLabel("0");
    private final JLabel syrupLabel = new JLabel("0");
    private final JLabel resinLabel = new JLabel("0");
    private final JLabel mineralLabel = new JLabel("0");
    
    // --- Ant Components ---
    private final JLabel totalAntLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_TOTAL_ANTS, 0));
    private final JLabel queensLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_QUEENS, 0, 0));
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
    private final JLabel totalConsumptionLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_CONSUMPTION, 0));
    private final JLabel totalProductionLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_MAX_FOOD_PROD, 0));
    private final JLabel netMushroomsLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_NET_FOOD, 0));
    private final JLabel layingRateLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_LAYING_RATE, 0));
    private final JLabel nurseCoverageLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_NURSE_COVERAGE, 0, 0));
    private final JLabel graveKeepingLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_GRAVE_CLEANING, 0, 0));
    private final JLabel petInsectsLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_PET_INSECTS, 0, 0));
    private final JLabel parasiticMiteCountLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_PARASITIC_MITES, 0, 0));
    private final JLabel parasiteAntCountLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_PARASITE_ANTS, LanguageStrings.get(LanguageStrings.WORLD_NA))); 
    private final JLabel policeStatsLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_POLICING, 0)); 
    private final JLabel researchPointsLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_RESEARCH, 0));
    private final JLabel researchRateLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_RESEARCH_RATE, 0));
    private final JLabel babyAntsLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_JUVENILE_ANTS, 0));
    private final JLabel adultAntsLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_ADULT_ANTS, 0));
    private final JLabel militaryPowerLabel = new JLabel(LanguageStrings.format(LanguageStrings.COLONY_MILITARY_POWER, 0));
    private final JLabel loyaltyLabel = new JLabel();
    
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
    private int lastMilitaryPower = -1;
    private int lastEffectiveLoyalty = -1;
    private ColonyRank lastRank = null;
    private String lastColonyName = "";
    private boolean lastAtWar = false;

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
        petInsectsLabel.setVisible(false);
        parasiticMiteCountLabel.setVisible(false);
        parasiteAntCountLabel.setVisible(false);
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
        setupStatLabel(totalAntLabel, GameConstants.ICON_STAT_POPULATION);
        setupStatLabel(totalConsumptionLabel, GameConstants.ICON_STAT_FOOD_CONSUMPTION);
        setupStatLabel(totalProductionLabel, GameConstants.ICON_STAT_FOOD_PRODUCTION);
        setupStatLabel(netMushroomsLabel, GameConstants.ICON_STAT_NET_FOOD);
        setupConstantLabel(layingRateLabel, GameConstants.ROLE_LAYER);
        setupConstantLabel(nurseCoverageLabel, GameConstants.ROLE_NURSE);
        setupConstantLabel(graveKeepingLabel, GameConstants.ROLE_GRAVER);
        setupConstantLabel(babyAntsLabel, GameConstants.TYPE_EGG);
        setupConstantLabel(adultAntsLabel, GameConstants.TYPE_WORKER);
        petInsectsLabel.setIcon(GameConstants.ICON_APHID);
        parasiticMiteCountLabel.setIcon(GameConstants.ICON_PARASITIC_MITE);
        parasiteAntCountLabel.setIcon(GameConstants.TYPE_PARASITE_ANT.getIcon());
        policeStatsLabel.setIcon(GameConstants.ROLE_POLICE.getIcon());
        researchPointsLabel.setIcon(GameConstants.ICON_RESEARCH);
        researchRateLabel.setIcon(GameConstants.ROLE_RESEARCHER.getIcon());
        militaryPowerLabel.setIcon(GameConstants.ICON_STAT_MILITARY_POWER);
        militaryPowerLabel.setToolTipText(LanguageStrings.get(LanguageStrings.STAT_MILITARY_POWER_DESC));
        loyaltyLabel.setIcon(GameConstants.ICON_STAT_LOYALTY);
        loyaltyLabel.setIconTextGap(6);
        
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
        
        petInsectsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_PET_INSECTS));
        parasiticMiteCountLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_PARASITIC_MITES));
        parasiteAntCountLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_PARASITE_ANTS));
        policeStatsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_POLICING));
        researchPointsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_RESEARCH_POINTS));
    }

    private void setupConstantLabel(JLabel label, AntType type) {
        label.setIcon(type.getIcon());
        label.setForeground(AssetStyles.FONT_COLOR);
    }

    private void setupConstantLabel(JLabel label, AntRole role) {
        label.setIcon(role.getIcon());
        label.setForeground(AssetStyles.FONT_COLOR);
    }

    private void setupConstantLabel(JLabel label, ResourceType resource) { 
        label.setIcon(resource.getIcon());
        label.setForeground(AssetStyles.FONT_COLOR);
    }

    private void setupStatLabel(JLabel label, ImageIcon icon) {
        label.setIcon(icon);
        label.setIconTextGap(6);
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
        lastMilitaryPower = -1;
        lastEffectiveLoyalty = -1;
        lastRank = null;
        lastColonyName = "";
        lastAtWar = false;
        
        setView(false);
    }
    
    @Override
    public void refreshTranslations() {
        super.refreshTranslations();
        updateTooltips();
        if (lastColonyRef != null) {
            lastMushrooms = -1;
            lastTotalConsumption = -1;
            lastMilitaryPower = -1;
            lastEffectiveLoyalty = -1;
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
        colonyNameLabel.setForeground(AssetStyles.FONT_COLOR);
        colonyNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        warStatusLabel.setForeground(AssetStyles.FONT_COLOR);
        warStatusLabel.setIconTextGap(6);
        warStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        warStatusLabel.setText(LanguageStrings.get(LanguageStrings.COLONY_AT_PEACE));
        setupStatLabel(warStatusLabel, GameConstants.ROLE_DIPLOMAT.getIcon());
        rankLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rankLabel.setForeground(AssetStyles.FONT_COLOR);
        panel.add(colonyNameLabel);
        panel.add(warStatusLabel);
        panel.add(rankLabel);
        panel.add(AssetStyles.createInternalSeparator());
        
        totalConsumptionLabel.setForeground(AssetStyles.FONT_COLOR);
        totalProductionLabel.setForeground(AssetStyles.FONT_COLOR);
        netMushroomsLabel.setForeground(AssetStyles.FONT_COLOR);
        layingRateLabel.setForeground(AssetStyles.FONT_COLOR);
        nurseCoverageLabel.setForeground(AssetStyles.FONT_COLOR);
        graveKeepingLabel.setForeground(AssetStyles.FONT_COLOR);
        petInsectsLabel.setForeground(AssetStyles.FONT_COLOR);
        parasiticMiteCountLabel.setForeground(AssetStyles.FONT_COLOR);
        parasiteAntCountLabel.setForeground(AssetStyles.FONT_COLOR);
        policeStatsLabel.setForeground(AssetStyles.FONT_COLOR);
        researchPointsLabel.setForeground(AssetStyles.FONT_COLOR);
        researchRateLabel.setForeground(AssetStyles.FONT_COLOR);
        babyAntsLabel.setForeground(AssetStyles.FONT_COLOR);
        adultAntsLabel.setForeground(AssetStyles.FONT_COLOR);
        militaryPowerLabel.setForeground(AssetStyles.FONT_COLOR);
        loyaltyLabel.setForeground(AssetStyles.FONT_COLOR);

        panel.add(totalConsumptionLabel);
        panel.add(totalProductionLabel);
        panel.add(netMushroomsLabel);
        panel.add(layingRateLabel);
        panel.add(nurseCoverageLabel);
        panel.add(graveKeepingLabel);
        panel.add(petInsectsLabel);
        panel.add(parasiticMiteCountLabel);
        panel.add(parasiteAntCountLabel);
        panel.add(policeStatsLabel);
        panel.add(researchPointsLabel);
        panel.add(researchRateLabel);
        panel.add(babyAntsLabel);
        panel.add(adultAntsLabel);
        panel.add(militaryPowerLabel);
        panel.add(loyaltyLabel);
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

        totalResourcesLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_TOTAL_RESOURCES, totalResources));
        if (mushrooms != lastMushrooms) mushroomsLabel.setText(AssetStyles.formatNumber(mushrooms));
        
        if (plants != lastPlants || plantsAvail != lastPlantsAvailable) {
            plantLabel.setText(AssetStyles.formatNumber(plants) + " / (" + AssetStyles.formatNumber(plantsAvail) + ")");
            lastPlantsAvailable = plantsAvail;
        }
        
        if (protein != lastProtein || proteinAvail != lastProteinAvailable) {
            proteinLabel.setText(AssetStyles.formatNumber(protein) + " / (" + AssetStyles.formatNumber(proteinAvail) + ")");
            lastProteinAvailable = proteinAvail;
        }
        
        if (water != lastWater || waterAvail != lastWaterAvailable) {
            waterLabel.setText(AssetStyles.formatNumber(water) + " / (" + AssetStyles.formatNumber(waterAvail) + ")");
            lastWaterAvailable = waterAvail;
        }

        boolean hasRanching = colony.hasUpgrade(GameUnlocks.ROLE_RANCHER);
        syrupLabel.setVisible(hasRanching);
        if (hasRanching && syrups != lastSyrups) syrupLabel.setText(AssetStyles.formatNumber(syrups));

        boolean hasResinResonation = colony.hasUpgrade(GameUnlocks.ABILITY_RESIN);
        resinLabel.setVisible(hasResinResonation);
        if (hasResinResonation && resins != lastResins) resinLabel.setText(AssetStyles.formatNumber(resins));

        boolean hasMining = colony.hasUpgrade(GameUnlocks.ROLE_MINER);
        mineralLabel.setVisible(hasMining);
        if (hasMining && (minerals != lastMinerals || mineralsAvail != lastMineralsAvailable)) {
             mineralLabel.setText(AssetStyles.formatNumber(minerals) + " / (" + AssetStyles.formatNumber(mineralsAvail) + ")");
             lastMineralsAvailable = mineralsAvail;
        }

        int totalConsumption = colony.getTotalConsumption();
        int netMushrooms = colony.getTotalProduction() - totalConsumption;
        netMushroomsLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_NET_FOOD, netMushrooms));
        
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
        if (eggs != lastEggs) eggsLabel.setText(AssetStyles.formatNumber(eggs));
        
        boolean hasHunter = colony.hasUpgrade(GameUnlocks.ROLE_HUNTER);
        proteinLabel.setVisible(hasHunter);
        
        int totalConsumption = colony.getTotalConsumption();
        if (totalConsumption != lastTotalConsumption) totalConsumptionLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_CONSUMPTION, totalConsumption));
        totalProductionLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_MAX_FOOD_PROD, colony.getTotalProduction()));

        boolean hasLayer = colony.hasUpgrade(GameUnlocks.ROLE_LAYER);
        layingRateLabel.setVisible(hasLayer);
        if (hasLayer) {
            int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            int hourlyLayingRate = layerCount * (int) colony.getLayingRate();
            layingRateLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_LAYING_RATE, hourlyLayingRate * 24));
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
            nurseCoverageLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_NURSE_COVERAGE, babyAntTotal, nurseCapacity));
        }

        boolean hasResearcher = colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER);
        boolean hasAssistant = colony.hasUpgrade(GameUnlocks.ROLE_ASSISTANT);
        researchPointsLabel.setVisible(hasResearcher || hasAssistant);
        researchRateLabel.setVisible(hasResearcher || hasAssistant);
        
        if (hasResearcher || hasAssistant) {
            researchPointsLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_RESEARCH, colony.getResearchPoints()));
            researchRateLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_RESEARCH_RATE,
                    colony.getStatsService().getDailyResearchPoints(colony)));
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
        totalAntLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_TOTAL_ANTS, totalAnts));

        String colonyName = colony.getName();
        if (!colonyName.equals(lastColonyName)) {
            colonyNameLabel.setText(colonyName);
            lastColonyName = colonyName;
        }

        boolean atWar = colony.getDynasty() != null && colony.getDynasty().isAtWar();
        if (atWar != lastAtWar) {
            if (atWar) {
                warStatusLabel.setText(LanguageStrings.get(LanguageStrings.COLONY_AT_WAR));
                setupStatLabel(warStatusLabel, GameConstants.ICON_STAT_MILITARY_POWER);
            } else {
                warStatusLabel.setText(LanguageStrings.get(LanguageStrings.COLONY_AT_PEACE));
                setupStatLabel(warStatusLabel, GameConstants.ROLE_DIPLOMAT.getIcon());
            }
            lastAtWar = atWar;
        }
        
        ColonyRank currentRank = colony.getRank();
        if (currentRank != lastRank) {
            rankLabel.setText(currentRank.getName());
            rankLabel.setIcon(currentRank.getIcon());
            lastRank = currentRank;
        }

        queensLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_QUEEN));
        int queenCount = colony.getQueens() != null ? colony.getQueens().size() : 0;
        int queenCapacity = colony.getQueensCapacity();
        queensLabel.setText(AssetStyles.formatRatio(queenCount, queenCapacity));
        
        boolean hasPrincess = colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS);
        princessLabel.setVisible(hasPrincess);
        princessLabel.setText(AssetStyles.formatNumber(colony.getPrincesses() != null ? colony.getPrincesses().size() : 0));
        droneLabel.setVisible(hasPrincess);
        droneLabel.setText(AssetStyles.formatNumber(colony.getDrones() != null ? colony.getDrones().size() : 0));
        
        majorLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_MAJOR));
        majorLabel.setText(AssetStyles.formatNumber(colony.getMajors() != null ? colony.getMajors().size() : 0));
        
        soldiersLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER));
        soldiersLabel.setText(AssetStyles.formatNumber(colony.getSoldiers() != null ? colony.getSoldiers().size() : 0));
        
        workersLabel.setVisible(colony.hasUpgrade(GameUnlocks.TYPE_WORKER));
        workersLabel.setText(AssetStyles.formatNumber(colony.getWorkers() != null ? colony.getWorkers().size() : 0));
        
        boolean hasBabies = colony.hasUpgrade(GameUnlocks.TYPE_EGG);
        pupaLabel.setVisible(hasBabies);
        pupaLabel.setText(AssetStyles.formatNumber(colony.getPupae() != null ? colony.getPupae().size() : 0));
        larvaLabel.setVisible(hasBabies);
        larvaLabel.setText(AssetStyles.formatNumber(colony.getLarvae() != null ? colony.getLarvae().size() : 0));
        eggsLabel.setVisible(hasBabies);
        
        deadAntsLabel.setText(AssetStyles.formatNumber(colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0));

        if (colony.getMushrooms() != lastMushrooms) mushroomsLabel.setText(AssetStyles.formatNumber(colony.getMushrooms()));
        
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
            graveKeepingLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_GRAVE_CLEANING, currentDead, graveCapacity));
        }

        boolean hasRancher = colony.hasUpgrade(GameUnlocks.ROLE_RANCHER);
        boolean hasCatcher = colony.hasUpgrade(GameUnlocks.ROLE_CATCHER);
        boolean hasSymbioticMiteCatch = colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE);
        boolean hasDermestidCatch = colony.hasUpgrade(GameUnlocks.ABILITY_CATCH_DERMESTID);
        boolean showPetInsects = hasRancher || hasCatcher || hasSymbioticMiteCatch || hasDermestidCatch
                || colony.getAphids() > 0 || colony.getSymbioticMites() > 0 || colony.getDermestids() > 0;
        petInsectsLabel.setVisible(showPetInsects);
        if (showPetInsects) {
            var bugHandling = colony.getBugHandlingService();
            int poolUsed = bugHandling.getUnlockedPetCount(colony);
            int poolMax = bugHandling.getUnlockedPetCapacityMax(colony);
            petInsectsLabel.setText(LanguageStrings.format(
                    LanguageStrings.COLONY_PET_INSECTS, poolUsed, poolMax));
        }

        int parasiticMites = colony.getParasiticMites();
        parasiticMiteCountLabel.setVisible(parasiticMites > 0);
        if (parasiticMites > 0) {
            int slowed = colony.getParasiticMiteSlowedAntCount();
            parasiticMiteCountLabel.setText(LanguageStrings.format(
                    LanguageStrings.COLONY_PARASITIC_MITES, parasiticMites, slowed));
        }
        
        boolean hasPolice = colony.hasUpgrade(GameUnlocks.ROLE_POLICE);
        parasiteAntCountLabel.setVisible(hasPolice);
        policeStatsLabel.setVisible(hasPolice);
        if (hasPolice) {
            boolean fuzz = engine != null && engine.isFuzzParasiteAnts();
            parasiteAntCountLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_PARASITE_ANTS, colony.getParasiteAntCountDisplay(fuzz)));
            float detection = colony.getStatsService().getParasiteDetection(colony);
            int policeCount = colony.getAssignedRoleCount(GameConstants.ROLE_POLICE);
            policeStatsLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_DETECTION_RATE, Math.round(policeCount * detection)));

            World world = engine != null ? engine.getWorld() : null;
            if (world != null) {
                Hex hex = world.getHexOfColony(colony);
                Biome biome = hex != null ? hex.getBiome() : null;
                Season season = world.getSeason();
                if (biome != null && season != null) {
                    int projected = colony.getPopulationService().projectParasiteAntMonthlySpawn(colony, biome, season);
                    if (projected > 0) {
                        int required = colony.getPopulationService().requiredPoliceToPreventParasiteAntOutbreak(colony, biome, season);
                        String prevention = LanguageStrings.format(
                                LanguageStrings.STAT_OUTBREAK_PREV_FMT,
                                policeCount, required, projected);
                        if (policeCount >= required) {
                            prevention = LanguageStrings.get(LanguageStrings.STAT_OUTBREAK_PREV_BLOCKED) + " — " + prevention;
                        }
                        policeStatsLabel.setToolTipText(
                                LanguageStrings.get(LanguageStrings.TOOLTIP_POLICING) + "<br>"
                                + LanguageStrings.get(LanguageStrings.TOOLTIP_OUTBREAK_PREV_POLICE) + "<br>"
                                + prevention);
                    } else {
                        policeStatsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_POLICING));
                    }
                }
            }
        }
        
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        int babyTotal = eggs + larva + pupa;
        babyAntsLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_JUVENILE_ANTS, babyTotal));

        int adultTotal = (colony.getQueens().size() + colony.getPrincesses().size() + colony.getDrones().size() + colony.getMajors().size() + colony.getSoldiers().size() + colony.getWorkers().size());
        adultAntsLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_ADULT_ANTS, adultTotal));

        int militaryPower = colony.getMilitaryPower();
        if (militaryPower != lastMilitaryPower) {
            militaryPowerLabel.setText(LanguageStrings.format(
                    LanguageStrings.COLONY_MILITARY_POWER, militaryPower));
            lastMilitaryPower = militaryPower;
        }

        int effectiveLoyalty = colony.getEffectiveLoyalty(
                engine != null ? engine.getTradeManager() : null,
                engine != null ? engine.getWorld() : null);
        if (effectiveLoyalty != lastEffectiveLoyalty) {
            ColonyLoyalty tier = GameConstants.getColonyLoyaltyLevel(effectiveLoyalty);
            loyaltyLabel.setText(LanguageStrings.format(
                    LanguageStrings.SCORE_TIER_FORMAT,
                    effectiveLoyalty,
                    tier.getName()));
            loyaltyLabel.setIcon(tier.getIcon());
            loyaltyLabel.setToolTipText(colony.buildLoyaltyModifierTooltip(
                    engine != null ? engine.getTradeManager() : null,
                    engine != null ? engine.getWorld() : null));
            lastEffectiveLoyalty = effectiveLoyalty;
        }

        lastMushrooms = colony.getMushrooms(); 
    }
}
