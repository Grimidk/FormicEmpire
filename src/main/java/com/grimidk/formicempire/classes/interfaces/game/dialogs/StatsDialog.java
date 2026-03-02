package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.DynastyStatService;
import com.grimidk.formicempire.classes.entities.services.ColonyLocationService;
import com.grimidk.formicempire.classes.entities.services.ColonyStatsService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsDialog extends ZeroDialog {

    private final Colony colony;
    private final Engine engine;
    private final DynastyStatService dynastyStatsService; 
    
    private final JTabbedPane tabbedPane;
    
    private JTable generalTable;
    private JTable dynastyTable;
    private JTable resourcesTable;
    private JTable populationTable;
    private JTable localHexTable;
    private JTable ratesTable;
    private JTable unitStatsTable;
    private JTable deathTable;
    
    private final Runnable refreshTask = this::liveUpdate;

    public StatsDialog(JFrame owner, Colony colony, Engine engine) {
        super(owner, "Statistics", new Dimension(1000, 600));
        this.colony = colony;
        this.engine = engine;
        this.dynastyStatsService = new DynastyStatService(); 
        
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
        
        initGeneralTab();
        initDynastyTab();
        initResourceTab();
        initPopulationTab();
        initLocalHexTab();
        initRatesTab();
        initUnitStatsTab();
        initDeathTab();
        
        refreshDialog();

        if (this.engine != null) {
            this.engine.addHourTickListener(refreshTask);
        }
        
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (engine != null) {
                    engine.removeHourTickListener(refreshTask);
                }
            }
            @Override
            public void windowClosing(WindowEvent e) {
                if (engine != null) {
                    engine.removeHourTickListener(refreshTask);
                }
            }
        });

        registerCloseKey(KeyEvent.VK_X);
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::refreshDialog);
        } else {
            refreshDialog();
        }
    }

    private DefaultTableModel createIconModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object val = getValueAt(row, columnIndex);
                    if (val != null) {
                        return val.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    private JScrollPane createTablePane(JTable table) {
        table.setRowHeight(24);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        return new JScrollPane(table);
    }

    // --- Tab Initialization ---

    private void initGeneralTab() {
        String[] columns = {"Category", "Property", "Value"};
        generalTable = new JTable(createIconModel(columns));
        tabbedPane.addTab("General & World", createTablePane(generalTable));
    }
    
    private void initDynastyTab() {
        String[] columns = {"", "Scope", "Metric", "Value"};
        dynastyTable = new JTable(createIconModel(columns));
        
        dynastyTable.getColumnModel().getColumn(0).setMaxWidth(40);
        dynastyTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        tabbedPane.addTab("Dynasty", createTablePane(dynastyTable));
    }

    private void initResourceTab() {
        String[] columns = {"", "Resource", "Current", "Capacity", "Sources", "Prod/Day", "Cons/Day", "Net"};
        resourcesTable = new JTable(createIconModel(columns));
        
        resourcesTable.getColumnModel().getColumn(0).setMaxWidth(40);
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        tabbedPane.addTab("Economy (Local)", createTablePane(resourcesTable));
    }

    private void initPopulationTab() {
        String[] columns = {"", "Type", "Role", "Count"};
        populationTable = new JTable(createIconModel(columns));
        
        populationTable.getColumnModel().getColumn(0).setMaxWidth(40);
        populationTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        tabbedPane.addTab("Population (Local)", createTablePane(populationTable));
    }

    private void initLocalHexTab() {
        String[] columns = {"Category", "Property", "Value"};
        localHexTable = new JTable(createIconModel(columns));
        tabbedPane.addTab("Local Hex", createTablePane(localHexTable));
    }

    private void initRatesTab() {
        String[] columns = {"Activity", "Assigned", "Rate/Capacity", "Coverage/Output"};
        ratesTable = new JTable(createIconModel(columns));
        tabbedPane.addTab("Rates & Jobs", createTablePane(ratesTable));
    }

    private void initUnitStatsTab() {
        String[] columns = {"Stat", "Base Value", "Description"};
        unitStatsTable = new JTable(createIconModel(columns));
        tabbedPane.addTab("Unit Stats", createTablePane(unitStatsTable));
    }

    private void initDeathTab() {
        String[] columns = {"Cause", "Total"};
        deathTable = new JTable(createIconModel(columns));
        tabbedPane.addTab("Mortality", createTablePane(deathTable));
    }

    // --- Data Updates ---

    @Override
    protected void refreshDialog() {
        updateGeneralData();
        updateDynastyData(); 
        updateResourceData();
        updatePopulationData();
        updateLocalHexData();
        updateRatesData();
        updateUnitStatsData();
        updateDeathData();
    }

    private void updateLocalHexData() {
        DefaultTableModel model = (DefaultTableModel) localHexTable.getModel();
        model.setRowCount(0);

        World world = engine != null ? engine.getWorld() : null;
        if (world == null || world.getActiveHex() == null) {
            model.addRow(new Object[]{"Hex", "Status", "No Active Hex"});
            return;
        }

        Hex hex = world.getActiveHex();
        Biome biome = hex.getBiome();

        // Hex Coordinates & Basic Info
        model.addRow(new Object[]{"Hex", "Coordinates (Q, R)", hex.getQ() + ", " + hex.getR()});
        
        Weather localWeather = hex.getLocalWeather();
        model.addRow(new Object[]{"Hex", "Local Weather", localWeather != null ? localWeather.getName() : "Using Global"});

        if (biome != null) {
            model.addRow(new Object[]{"Biome", "Name", biome.getName()});
            model.addRow(new Object[]{"Biome", "Base Temp", biome.getTemperature() + "°C"});
            model.addRow(new Object[]{"Biome", "Humidity Level", biome.isIsHumid() + " / 5"});
            model.addRow(new Object[]{"Abundances", "Plants", String.format("%.2f", biome.getPlantAbundance())});
            model.addRow(new Object[]{"Abundances", "Animals", String.format("%.2f", biome.getAnimalAbundance())});
            model.addRow(new Object[]{"Abundances", "Minerals", String.format("%.2f", biome.getMineralAbundance())});
        }

        // Neighbors
        model.addRow(new Object[]{null, null, "------", "------"});
        model.addRow(new Object[]{"Neighbors", "North", getHexSummary(hex.getNorth())});
        model.addRow(new Object[]{"Neighbors", "North-West", getHexSummary(hex.getNorthWest())});
        model.addRow(new Object[]{"Neighbors", "North-East", getHexSummary(hex.getNorthEast())});
        model.addRow(new Object[]{"Neighbors", "South", getHexSummary(hex.getSouth())});
        model.addRow(new Object[]{"Neighbors", "South-West", getHexSummary(hex.getSouthWest())});
        model.addRow(new Object[]{"Neighbors", "South-East", getHexSummary(hex.getSouthEast())});
    }

    private String getHexSummary(Hex neighbor) {
        if (neighbor == null) return "Edge of World";
        String summary = (neighbor.getBiome() != null ? neighbor.getBiome().getName() : "Unknown");
        if (neighbor.getColony() != null) {
            summary += " (Colony: " + neighbor.getColony().getName() + ")";
        }
        return summary;
    }

    private void updateGeneralData() {
        DefaultTableModel model = (DefaultTableModel) generalTable.getModel();
        model.setRowCount(0);

        // Colony Info
        model.addRow(new Object[]{"Colony", "Name", colony.getName()});
        model.addRow(new Object[]{"Colony", "Rank", colony.getRank().getName()});
        model.addRow(new Object[]{"Colony", "Species", colony.getSpecies() != null ? colony.getSpecies().getName() : "Unknown"});
        model.addRow(new Object[]{"Colony", "Species (Scientific)", colony.getSpecies() != null ? colony.getSpecies().getScientific() : "Unknown"});
        model.addRow(new Object[]{"Colony", "ID", colony.getId()});
        model.addRow(new Object[]{"Colony", "Age", colony.getAge() + " days"});
        
        if (colony.getQueens().isEmpty()) {
            model.addRow(new Object[]{"Colony", "Queen Status", "MISSING (" + colony.getDaysWithoutQueen() + " days)"});
        } else {
            model.addRow(new Object[]{"Colony", "Queen Status", "Healthy (" + colony.getQueens().size() + " total)"});
        }

        model.addRow(new Object[]{"Colony", "Automation", colony.isAutomationEnabled() ? "ENABLED" : "Disabled"});
        model.addRow(new Object[]{"Colony", "Auto-Build", colony.isAutoBuildEnabled() ? "ENABLED" : "Disabled"});

        // World Info
        World world = engine != null ? engine.getWorld() : null;
        if (world != null) {
            String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
                world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
            
            model.addRow(new Object[]{"World", "Date & Time", dateTime});
            model.addRow(new Object[]{"World", "Time of Day", world.getTimeOfDay().getName()});
            model.addRow(new Object[]{"World", "Moon Phase", world.getMoonPhase().getName()});
            
            Season season = world.getSeason();
            model.addRow(new Object[]{"Environment", "Season", season.getName()});
            
            Weather weather = world.getWeather();
            model.addRow(new Object[]{"Environment", "Weather", weather.getName()});
            
            if (world.getActiveHex() != null && world.getActiveHex().getBiome() != null) {
                model.addRow(new Object[]{"Environment", "Biome", world.getActiveHex().getBiome().getName()});
            }
            
            model.addRow(new Object[]{"Environment", "Temperature", world.getTemperature() + "°C"});
            model.addRow(new Object[]{"Environment", "Humidity", world.getHumidity()});
        }
    }

    private void updateDynastyData() {
        DefaultTableModel model = (DefaultTableModel) dynastyTable.getModel();
        model.setRowCount(0);
        
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) {
            model.addRow(new Object[]{null, "Error", "Status", "No Dynasty Linked"});
            return;
        }

        // Basic Info
        model.addRow(new Object[]{null, "Dynasty", "Name", dynasty.getName()});
        model.addRow(new Object[]{dynasty.getRank().getIcon(), "Dynasty", "Rank", dynasty.getRank().getName()}); 
        model.addRow(new Object[]{null, "Dynasty", "Species", dynasty.getSpecies() != null ? dynasty.getSpecies().getName() : "Omni"});
        model.addRow(new Object[]{null, "Dynasty", "Total Colonies", dynastyStatsService.getTotalColonies(dynasty)});
        model.addRow(new Object[]{null, "Dynasty", "Global Population", dynastyStatsService.getTotalPopulation(dynasty)});
        model.addRow(new Object[]{null, "Dynasty", "Total Nuptial Flights", dynasty.getTotalNuptialFlights()});

        // Conquest & Expansion
        model.addRow(new Object[]{null, null, "------", "------"});
        String defeated = dynasty.getDefeatedSpeciesIds().stream()
            .map(id -> {
                for (Species s : GameConstants.getSpecies()) {
                    if (s.getId() == id) return s.getName();
                }
                return "ID:" + id;
            })
            .collect(Collectors.joining(", "));
        model.addRow(new Object[]{null, "Dynasty", "Defeated Species", defeated.isEmpty() ? "None" : defeated});
        
        // Unlocks
        model.addRow(new Object[]{null, null, "------", "------"});
        model.addRow(new Object[]{null, "Progress", "Upgrades Researched", dynasty.getUnlockedUpgrades().size()});
        model.addRow(new Object[]{null, "Progress", "Completed Assimilations", dynasty.getCompletedAssimilations().size()});
        
        int totalBuildings = 0;
        for (Colony c : dynasty.getColonies()) {
            totalBuildings += c.getUnlockedBuildings().size();
        }
        model.addRow(new Object[]{null, "Progress", "Total Buildings Built", totalBuildings});

        // Research
        model.addRow(new Object[]{null, null, "------", "------"});
        model.addRow(new Object[]{GameConstants.ICON_RESEARCH, "Research", "Stored Points", dynasty.getResearchPoints()});
        model.addRow(new Object[]{null, "Research", "Global Rate", "+" + dynastyStatsService.getGlobalResearchRateDaily(dynasty) + " pts/day"});

        // Global Resources
        model.addRow(new Object[]{null, null, "------", "------"});
        Map<ResourceType, Integer> resources = dynastyStatsService.getGlobalResources(dynasty);
        
        model.addRow(new Object[]{GameConstants.RESOURCE_PLANT.getIcon(), "Resources", "Total Plants", resources.get(GameConstants.RESOURCE_PLANT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_FUNGI.getIcon(), "Resources", "Total Mushrooms", resources.get(GameConstants.RESOURCE_FUNGI)});
        model.addRow(new Object[]{GameConstants.RESOURCE_MEAT.getIcon(), "Resources", "Total Protein", resources.get(GameConstants.RESOURCE_MEAT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_WATER.getIcon(), "Resources", "Total Water", resources.get(GameConstants.RESOURCE_WATER)});
        model.addRow(new Object[]{GameConstants.RESOURCE_SYRUP.getIcon(), "Resources", "Total Syrups", resources.getOrDefault(GameConstants.RESOURCE_SYRUP, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_RESIN.getIcon(), "Resources", "Total Resins", resources.getOrDefault(GameConstants.RESOURCE_RESIN, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_ROCK.getIcon(), "Resources", "Total Minerals", resources.get(GameConstants.RESOURCE_ROCK)});
        
        // Global Deaths
        model.addRow(new Object[]{null, null, "------", "------"});
        int totalDeaths = 0;
        if (dynasty.getGlobalDeathStatistics() != null) {
            totalDeaths = dynasty.getGlobalDeathStatistics().values().stream().mapToInt(Integer::intValue).sum();
        }
        model.addRow(new Object[]{GameConstants.STATUS_DEAD.getIcon(), "Mortality", "Global Deaths", totalDeaths});
    }

    private void updateResourceData() {
        DefaultTableModel model = (DefaultTableModel) resourcesTable.getModel();
        model.setRowCount(0);

        ColonyStatsService stats = colony.getStatsService();
        ColonyLocationService loc = colony.getLocationService();
        
        // --- Plants ---
        int plantProd = stats.getPlantProduction(colony);
        int plantCons = stats.getPlantConsumption(colony);
        addResourceRow(model, GameConstants.RESOURCE_PLANT.getIcon(), "Plants", (int) colony.getPlants(), stats.getPlantsCapacity(colony), 
            loc != null ? loc.getSourcesByType(GameConstants.RESOURCE_PLANT).size() : 0, stats.getSourceCapacity(colony),
            plantProd, plantCons);
            
        // --- Mushrooms ---
        int mushProd = stats.getTotalProduction(colony);
        int mushCons = stats.getTotalConsumption(colony);
        addResourceRow(model, GameConstants.RESOURCE_FUNGI.getIcon(), "Mushrooms", (int) colony.getMushrooms(), stats.getMushroomsCapacity(colony), 
            0, 0, mushProd, mushCons);
            
        // --- Protein ---
        int protProd = stats.getProteinProduction(colony);
        int protCons = stats.getProteinConsumption(colony);
        addResourceRow(model, GameConstants.RESOURCE_MEAT.getIcon(), "Protein", (int) colony.getProtein(), stats.getProteinCapacity(colony), 
            loc != null ? loc.getSourcesByType(GameConstants.RESOURCE_MEAT).size() : 0, stats.getSourceCapacity(colony),
            protProd, protCons);
            
        // --- Water ---
        int waterProd = stats.getWaterProduction(colony);
        int waterCons = stats.getWaterConsumption(colony);
        addResourceRow(model, GameConstants.RESOURCE_WATER.getIcon(), "Water", (int) colony.getWater(), stats.getWaterCapacity(colony), 
            loc != null ? loc.getSourcesByType(GameConstants.RESOURCE_WATER).size() : 0, stats.getSourceCapacity(colony),
            waterProd, waterCons);

        // --- Minerals ---
        int minProd = stats.getMineralProduction(colony);
        int minCons = stats.getMineralConsumption(colony);
        addResourceRow(model, GameConstants.RESOURCE_ROCK.getIcon(), "Minerals", (int) colony.getMinerals(), stats.getMineralsCapacity(colony), 
            loc != null ? loc.getSourcesByType(GameConstants.RESOURCE_ROCK).size() : 0, stats.getSourceCapacity(colony),
            minProd, minCons);
            
        // --- Syrups/Resins  ---
        addResourceRow(model, GameConstants.RESOURCE_SYRUP.getIcon(), "Syrups", (int) colony.getSyrups(), stats.getSyrupsCapacity(colony), 
            0, 0, colony.getAphids(), 0);
            
        addResourceRow(model, GameConstants.RESOURCE_RESIN.getIcon(), "Resins", (int) colony.getResins(), stats.getResinsCapacity(colony), 
            0, 0, (int) (plantProd * 0.01), 0);

        model.addRow(new Object[]{null, "------", "---", "---", "---", "---", "---", "---"});
        int netFood = mushProd - mushCons;
        model.addRow(new Object[]{null, "Total Food", "---", "---", "---", mushProd, mushCons, (netFood >= 0 ? "+" : "") + netFood});
    }

    private void addResourceRow(DefaultTableModel model, ImageIcon icon, String name, int current, int cap, int sources, int maxSources, int production, int consumption) {
        String sourceStr = (maxSources > 0) ? sources + " / " + maxSources : "N/A";
        String prodStr = String.valueOf(production);
        String consStr = String.valueOf(consumption);
        
        int net = production - consumption;
        String netStr = (net >= 0 ? "+" : "") + net;
        
        if (name.equals("Syrups") || name.equals("Resins")) {
            if (production == 0 && consumption == 0) {
                prodStr = "---"; consStr = "---"; netStr = "---";
            }
        }
        model.addRow(new Object[]{icon, name, current, cap, sourceStr, prodStr, consStr, netStr});
    }

    private void updatePopulationData() {
        DefaultTableModel model = (DefaultTableModel) populationTable.getModel();
        model.setRowCount(0);

        int totalJuvenile = 0;
        int totalAdult = 0;

        for (AntType type : GameConstants.getAntTypes()) {
            List<Ant> ants = colony.getAntsByType(type);
            int count = ants.size();
            
            if (count == 0) continue;

            model.addRow(new Object[]{type.getIcon(), type.getName(), "Total", count});

            if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA || type == GameConstants.TYPE_PUPA) {
                totalJuvenile += count;
            } else if (type != GameConstants.TYPE_DEAD) {
                totalAdult += count;
                
                for (AntRole role : GameConstants.getAntRoles()) {
                    if (role.getAntType() == type) {
                        int roleCount = colony.getAssignedRoleCount(role);
                        if (roleCount > 0) {
                            model.addRow(new Object[]{null, "", role.getName(), roleCount});
                        }
                    }
                }
            }
        }
        
        model.addRow(new Object[]{null, "------", "------", "------"});
        model.addRow(new Object[]{null, "Summary", "Total Adults", totalAdult});
        model.addRow(new Object[]{null, "Summary", "Total Juveniles", totalJuvenile});
        model.addRow(new Object[]{null, "Summary", "Colony Total", colony.getAntTotal()});
    }

    private void updateRatesData() {
        DefaultTableModel model = (DefaultTableModel) ratesTable.getModel();
        model.setRowCount(0);
        ColonyStatsService stats = colony.getStatsService();
        
        // Foragers
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {
            int count = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
            float rate = stats.getCollectingRate(colony);
            int daily = (int)(count * rate * 24); 
            model.addRow(new Object[]{"Foraging", count + " Foragers", rate + " /hr", "~" + daily + " pwr/day"});
        }

        // Farmers
        if (colony.hasUpgrade(GameUnlocks.ROLE_FARMER)) {
            int count = stats.getEffectiveFarmerCount(colony);
            float rate = stats.getConversionRate(colony);
            int daily = (int)(count * rate * 1440);
            model.addRow(new Object[]{"Farming", count + " Eff. Farmers", rate + " /min", "~" + daily + " convert/day"});
        }

        // Hunters
        if (colony.hasUpgrade(GameUnlocks.ROLE_HUNTER)) {
            int count = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
            float rate = stats.getCollectingRate(colony);
            int daily = (int)(count * rate * 24); 
            model.addRow(new Object[]{"Hunting", count + " Hunters", rate + " /hr", "~" + daily + " pwr/day"});
        }

        // Miners
        if (colony.hasUpgrade(GameUnlocks.ROLE_MINER)) {
            int count = colony.getAssignedRoleCount(GameConstants.ROLE_MINER);
            float rate = stats.getCollectingRate(colony);
            int daily = (int)(count * rate * 24); 
            model.addRow(new Object[]{"Mining", count + " Miners", rate + " /hr", "~" + daily + " pwr/day"});
        }
        
        // Scouts
        if (colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {
            int count = colony.getAssignedRoleCount(GameConstants.ROLE_SCOUT);
            float rate = stats.getScoutingRate(colony);
            model.addRow(new Object[]{"Scouting", count + " Scouts", String.format("%.0f%%", rate * 100) + " /hr", "Finds resources"});
        }

        // Builders
        if (colony.getCurrentBuildingProject() != null) {
            int builders = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
            int cranes = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
            double efficiency = stats.getConstructionEfficiency(colony);
            
            String label = builders + " Builders";
            if (colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
                label += " / " + cranes + " Cranes";
            }
            
            model.addRow(new Object[]{"Construction", label, String.format("%.0f%% efficiency", efficiency * 100), "Project: " + colony.getCurrentBuildingProject().getName()});
        }
        
        // Research
        boolean hasResearcher = colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER) || colony.hasUpgrade(GameUnlocks.ROLE_ASSISTANT);
        if (hasResearcher) {
            int researchers = colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
            int assistants = colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT);
            int baseSpeed = stats.getResearchSpeed(colony);
            
            int hourlyQueen = researchers * baseSpeed;
            int hourlyAssistant = (int) (assistants * (baseSpeed / 5.0));
            int totalDaily = (hourlyQueen + hourlyAssistant) * 24;
            
            model.addRow(new Object[]{"Research", 
                researchers + " Res / " + assistants + " Asst", 
                "Contribution", 
                "+" + totalDaily + " pts/day"});
        }

        // Egg Laying
        if (colony.hasUpgrade(GameUnlocks.ROLE_LAYER)) {
            int layers = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            float rate = stats.getLayingRate(colony);
            int dailyProduction = (int)(layers * rate * 24);
            model.addRow(new Object[]{"Egg Laying", layers + " Layers", rate + "/hr", "+" + dailyProduction + " eggs/day"});
        }

        // Nursing
        if (colony.hasUpgrade(GameUnlocks.ROLE_NURSE)) {
            int nurses = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
            int babies = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
            float capacityPerNurse = stats.getNursingRate(colony);
            int totalCapacity = (int)(nurses * capacityPerNurse);
            
            model.addRow(new Object[]{"Nursing", nurses + " Nurses", totalCapacity + " Cap", babies + " / " + totalCapacity + " Load"});
        }

        // Grave Keeping
        if (colony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) {
            int gravers = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
            int dead = colony.getDeadAnts().size();
            float capacityPerGraver = stats.getGravingRate(colony);
            int totalCapacity = (int)(gravers * capacityPerGraver);
            
            model.addRow(new Object[]{"Grave Keeping", gravers + " Gravers", totalCapacity + " Cap", dead + " / " + totalCapacity + " Load"});
        }

        // Ranching
        if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {
            int ranchers = colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
            int capacityPerRancher = stats.getAphidCapacity(colony);
            int maxAphids = ranchers * capacityPerRancher;
            model.addRow(new Object[]{"Ranching", ranchers + " Ranchers", maxAphids + " Cap", colony.getAphids() + " / " + maxAphids + " Aphids"});
        }

        // Policing
        if (colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {
            int police = colony.getAssignedRoleCount(GameConstants.ROLE_POLICE);
            float detection = stats.getParasiteDetection(colony);
            int dailyDetection = Math.round(police * detection);
            model.addRow(new Object[]{"Policing", police + " Police", colony.getParasiteCountDisplay() + " Parasites", "~" + dailyDetection + " det./day"});
        }
    }

    private void updateUnitStatsData() {
        DefaultTableModel model = (DefaultTableModel) unitStatsTable.getModel();
        model.setRowCount(0);
        ColonyStatsService stats = colony.getStatsService();

        int hp = stats.getBaseHealth(colony);
        if (hp > 0) model.addRow(new Object[]{"Base Health", hp, "Hitpoints per worker"});

        int def = stats.getBaseDefense(colony);
        if (def > 0) model.addRow(new Object[]{"Base Defense", def, "Percent damage reduction"});

        int atk = stats.getBaseAttack(colony);
        if (atk > 0) model.addRow(new Object[]{"Base Attack", atk, "Damage per hit"});

        int spd = stats.getBaseSpeed(colony);
        if (spd > 0) model.addRow(new Object[]{"Movement Speed", spd, "Pixels per tick"});

        int regen = stats.getBaseRegen(colony);
        if (regen > 0) model.addRow(new Object[]{"Regeneration", regen, "Health recovered per tick"});

        int tempRes = stats.getBaseTempRes(colony);
        if (tempRes > 0) model.addRow(new Object[]{"Temp. Resist", tempRes, "Degrees tolerance"});

        int cons = stats.getBaseConsumption(colony);
        if (cons > 0) model.addRow(new Object[]{"Base Consumption", cons, "Food units per day per ant"});

        float detect = stats.getParasiteDetection(colony);
        if (detect > 0) model.addRow(new Object[]{"Parasite Detection", String.format("%.2f", detect), "Chance to find parasites"});
        
        float contam = stats.getContaminationMitigation(colony);
        if (contam < 1.0f) model.addRow(new Object[]{"Immunity", String.format("%.0f%%", (1.0f - contam) * 100), "Contamination mitigation"});
    }

    private void updateDeathData() {
        DefaultTableModel model = (DefaultTableModel) deathTable.getModel();
        model.setRowCount(0);

        if (colony.getPopulationService() == null) {
            model.addRow(new Object[]{"Population Service Not Initialized", 0});
            return;
        }

        Map<String, Integer> stats = colony.getPopulationService().getDeathStatistics();
        
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        model.addRow(new Object[]{"------", "------"});
        model.addRow(new Object[]{"Total Deaths", colony.getTotalDeaths()});
    }
}
