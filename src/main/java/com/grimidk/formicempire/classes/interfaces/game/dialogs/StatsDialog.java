package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.constants.world.Weather;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.DynastyStatService;
import com.grimidk.formicempire.classes.entities.services.ColonyLocationService;
import com.grimidk.formicempire.classes.entities.services.ColonyStatsService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsDialog extends ZeroDialog {

    private final Colony colony;
    private final Engine engine;
    private final DynastyStatService dynastyStatsService; 
    
    private final JTabbedPane tabbedPane;
    private JCheckBox dynastyModeToggle;
    
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
        super(owner, "Statistics", new Dimension(1000, 650));
        this.colony = colony;
        this.engine = engine;
        this.dynastyStatsService = new DynastyStatService(); 
        
        setLayout(new BorderLayout());
        
        // --- Top Toggle Panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        dynastyModeToggle = new JCheckBox("Dynasty Mode");
        dynastyModeToggle.setOpaque(false);
        dynastyModeToggle.setFont(AssetStyles.FONT_BOLD);
        dynastyModeToggle.setForeground(AssetStyles.TEXT_HEADER);
        dynastyModeToggle.setFocusable(false);
        dynastyModeToggle.addActionListener(e -> {
            updateTabTitles();
            refreshDialog();
        });
        topPanel.add(dynastyModeToggle);
        add(topPanel, BorderLayout.NORTH);

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
        
        updateTabTitles();
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

    private void updateTabTitles() {
        boolean dynastyMode = dynastyModeToggle.isSelected();

        tabbedPane.setTitleAt(0, "General & World");
        tabbedPane.setTitleAt(1, "Dynasty Overview");
        tabbedPane.setTitleAt(2, "Economy ");
        tabbedPane.setTitleAt(3, "Population ");
        tabbedPane.setTitleAt(4, "Local Hex");
        tabbedPane.setTitleAt(5, "Rates & Jobs ");
        tabbedPane.setTitleAt(6, "Unit Stats ");
        tabbedPane.setTitleAt(7, "Mortality ");
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
        table.setBackground(AssetStyles.UI_BG_PRIMARY);
        table.setForeground(AssetStyles.TEXT_NORMAL);
        table.setSelectionBackground(AssetStyles.UI_BG_SECONDARY);
        table.setSelectionForeground(AssetStyles.TEXT_HEADER);
        table.setFont(AssetStyles.FONT_NORMAL);
        
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setBackground(AssetStyles.UI_BG_SECONDARY);
        headerRenderer.setForeground(AssetStyles.TEXT_HEADER);
        headerRenderer.setFont(AssetStyles.FONT_BOLD);
        table.getTableHeader().setDefaultRenderer(headerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        scrollPane.setBorder(null);
        return scrollPane;
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
        
        tabbedPane.addTab("Dynasty Overview", createTablePane(dynastyTable));
    }

    private void initResourceTab() {
        String[] columns = {"", "Resource", "Current", "Capacity", "Sources", "Prod/Day", "Cons/Day", "Net"};
        resourcesTable = new JTable(createIconModel(columns));
        
        resourcesTable.getColumnModel().getColumn(0).setMaxWidth(40);
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        tabbedPane.addTab("Economy", createTablePane(resourcesTable));
    }

    private void initPopulationTab() {
        String[] columns = {"", "Type", "Role", "Count"};
        populationTable = new JTable(createIconModel(columns));
        
        populationTable.getColumnModel().getColumn(0).setMaxWidth(40);
        populationTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        tabbedPane.addTab("Population", createTablePane(populationTable));
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

        if (dynastyModeToggle.isSelected()) {
            Dynasty dynasty = colony.getDynasty();
            if (dynasty != null) {
                model.addRow(new Object[]{"Dynasty", "Name", dynasty.getName()});
                model.addRow(new Object[]{"Dynasty", "Rank", dynasty.getRank().getName()});
                model.addRow(new Object[]{"Dynasty", "Species", dynasty.getSpecies() != null ? dynasty.getSpecies().getName() : "Unknown"});
                model.addRow(new Object[]{"Dynasty", "Total Colonies", dynasty.getColonies().size()});
                model.addRow(new Object[]{"Dynasty", "Global Population", dynastyStatsService.getTotalPopulation(dynasty)});
                
                int totalQueens = dynasty.getColonies().stream().mapToInt(c -> c.getQueens().size()).sum();
                model.addRow(new Object[]{"Dynasty", "Global Queens", totalQueens});
            }
        } else {
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
        }

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
        
        Colony capital = dynasty.getCapital();
        model.addRow(new Object[]{null, "Dynasty", "Capital", (capital != null ? capital.getName() : "None")});
        
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
        
        model.addRow(new Object[]{GameConstants.RESOURCE_PLANT.getIcon(), "Resources", "Global Plants", resources.get(GameConstants.RESOURCE_PLANT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_FUNGI.getIcon(), "Resources", "Global Mushrooms", resources.get(GameConstants.RESOURCE_FUNGI)});
        model.addRow(new Object[]{GameConstants.RESOURCE_MEAT.getIcon(), "Resources", "Global Protein", resources.get(GameConstants.RESOURCE_MEAT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_WATER.getIcon(), "Resources", "Global Water", resources.get(GameConstants.RESOURCE_WATER)});
        model.addRow(new Object[]{GameConstants.RESOURCE_SYRUP.getIcon(), "Resources", "Global Syrups", resources.getOrDefault(GameConstants.RESOURCE_SYRUP, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_RESIN.getIcon(), "Resources", "Global Resins", resources.getOrDefault(GameConstants.RESOURCE_RESIN, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_ROCK.getIcon(), "Resources", "Global Minerals", resources.getOrDefault(GameConstants.RESOURCE_ROCK, 0)});
        
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

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        long totalPlants = 0, totalMushrooms = 0, totalProtein = 0, totalWater = 0, totalSyrups = 0, totalResins = 0, totalMinerals = 0;
        int capPlants = 0, capMushrooms = 0, capProtein = 0, capWater = 0, capSyrups = 0, capResins = 0, capMinerals = 0;
        int sourcesPlants = 0, sourcesProtein = 0, sourcesWater = 0, sourcesMinerals = 0;
        int maxSources = 0;
        int prodPlants = 0, prodMushrooms = 0, prodProtein = 0, prodWater = 0, prodSyrups = 0, prodResins = 0, prodMinerals = 0;
        int consPlants = 0, consMushrooms = 0, consProtein = 0, consWater = 0, consMinerals = 0;

        for (Colony c : coloniesToCount) {
            ColonyStatsService cs = c.getStatsService();
            ColonyLocationService ls = c.getLocationService();

            totalPlants += c.getPlants();
            totalMushrooms += c.getMushrooms();
            totalProtein += c.getProtein();
            totalWater += c.getWater();
            totalSyrups += c.getSyrups();
            totalResins += c.getResins();
            totalMinerals += c.getMinerals();

            capPlants += cs.getPlantsCapacity(c);
            capMushrooms += cs.getMushroomsCapacity(c);
            capProtein += cs.getProteinCapacity(c);
            capWater += cs.getWaterCapacity(c);
            capSyrups += cs.getSyrupsCapacity(c);
            capResins += cs.getResinsCapacity(c);
            capMinerals += cs.getMineralsCapacity(c);

            if (ls != null) {
                sourcesPlants += ls.getSourcesByType(GameConstants.RESOURCE_PLANT).size();
                sourcesProtein += ls.getSourcesByType(GameConstants.RESOURCE_MEAT).size();
                sourcesWater += ls.getSourcesByType(GameConstants.RESOURCE_WATER).size();
                sourcesMinerals += ls.getSourcesByType(GameConstants.RESOURCE_ROCK).size();
            }
            maxSources += cs.getSourceCapacity(c);

            prodPlants += cs.getPlantProduction(c);
            prodMushrooms += cs.getTotalProduction(c);
            prodProtein += cs.getProteinProduction(c);
            prodWater += cs.getWaterProduction(c);
            prodMinerals += cs.getMineralProduction(c);
            prodSyrups += c.getAphids();
            prodResins += (int)(cs.getPlantProduction(c) * 0.01);

            consPlants += cs.getPlantConsumption(c);
            consMushrooms += cs.getTotalConsumption(c);
            consProtein += cs.getProteinConsumption(c);
            consWater += cs.getWaterConsumption(c);
        }

        addResourceRow(model, GameConstants.RESOURCE_PLANT.getIcon(), "Plants", (int)totalPlants, capPlants, sourcesPlants, maxSources, prodPlants, consPlants);
        addResourceRow(model, GameConstants.RESOURCE_FUNGI.getIcon(), "Mushrooms", (int)totalMushrooms, capMushrooms, 0, 0, prodMushrooms, consMushrooms);
        addResourceRow(model, GameConstants.RESOURCE_MEAT.getIcon(), "Protein", (int)totalProtein, capProtein, sourcesProtein, maxSources, prodProtein, consProtein);
        addResourceRow(model, GameConstants.RESOURCE_WATER.getIcon(), "Water", (int)totalWater, capWater, sourcesWater, maxSources, prodWater, consWater);
        addResourceRow(model, GameConstants.RESOURCE_ROCK.getIcon(), "Minerals", (int)totalMinerals, capMinerals, sourcesMinerals, maxSources, prodMinerals, consMinerals);
        addResourceRow(model, GameConstants.RESOURCE_SYRUP.getIcon(), "Syrups", (int)totalSyrups, capSyrups, 0, 0, prodSyrups, 0);
        addResourceRow(model, GameConstants.RESOURCE_RESIN.getIcon(), "Resins", (int)totalResins, capResins, 0, 0, prodResins, 0);

        model.addRow(new Object[]{null, "------", "---", "---", "---", "---", "---", "---"});
        int netFood = prodMushrooms - consMushrooms;
        model.addRow(new Object[]{null, "Total Food", "---", "---", "---", prodMushrooms, consMushrooms, (netFood >= 0 ? "+" : "") + netFood});
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

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        Map<AntType, Integer> typeTotals = new HashMap<>();
        Map<AntRole, Integer> roleTotals = new HashMap<>();
        int totalJuvenile = 0;
        int totalAdult = 0;
        int grandTotal = 0;

        for (Colony c : coloniesToCount) {
            grandTotal += c.getAntTotal();
            for (AntType type : GameConstants.getAntTypes()) {
                int count = c.getAntsByType(type).size();
                if (count > 0) {
                    typeTotals.merge(type, count, Integer::sum);
                    if (type == GameConstants.TYPE_EGG || type == GameConstants.TYPE_LARVA || type == GameConstants.TYPE_PUPA) {
                        totalJuvenile += count;
                    } else if (type != GameConstants.TYPE_DEAD) {
                        totalAdult += count;
                    }
                }
            }
            for (AntRole role : GameConstants.getAntRoles()) {
                int count = c.getAssignedRoleCount(role);
                if (count > 0) {
                    roleTotals.merge(role, count, Integer::sum);
                }
            }
        }

        for (AntType type : GameConstants.getAntTypes()) {
            Integer count = typeTotals.get(type);
            if (count == null || count == 0) continue;

            model.addRow(new Object[]{type.getIcon(), type.getName(), "Total", count});

            if (type != GameConstants.TYPE_EGG && type != GameConstants.TYPE_LARVA && type != GameConstants.TYPE_PUPA && type != GameConstants.TYPE_DEAD) {
                for (AntRole role : GameConstants.getAntRoles()) {
                    if (role.getAntType() == type) {
                        Integer rCount = roleTotals.get(role);
                        if (rCount != null && rCount > 0) {
                            model.addRow(new Object[]{null, "", role.getName(), rCount});
                        }
                    }
                }
            }
        }
        
        model.addRow(new Object[]{null, "------", "------", "------"});
        model.addRow(new Object[]{null, "Summary", "Total Adults", totalAdult});
        model.addRow(new Object[]{null, "Summary", "Total Juveniles", totalJuvenile});
        model.addRow(new Object[]{null, "Summary", dynastyModeToggle.isSelected() ? "Dynasty Total" : "Colony Total", grandTotal});
    }

    private void updateRatesData() {
        DefaultTableModel model = (DefaultTableModel) ratesTable.getModel();
        model.setRowCount(0);

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        // We'll aggregate counts and daily totals
        int foragers = 0, dailyForage = 0;
        int farmers = 0, dailyFarm = 0;
        int hunters = 0, dailyHunt = 0;
        int miners = 0, dailyMine = 0;
        int scouts = 0;
        int researchers = 0, assistants = 0, dailyRP = 0;
        int layers = 0, dailyEggs = 0;
        int nurses = 0, babies = 0, nurseCap = 0;
        int gravers = 0, deadAnts = 0, graveCap = 0;
        int ranchers = 0, aphidCap = 0, aphids = 0;
        int police = 0, parasites = 0, dailyDetect = 0;

        for (Colony c : coloniesToCount) {
            ColonyStatsService cs = c.getStatsService();
            
            foragers += c.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
            dailyForage += cs.getPlantProduction(c) + cs.getWaterProduction(c);

            farmers += cs.getEffectiveFarmerCount(c);
            dailyFarm += (int)(cs.getEffectiveFarmerCount(c) * cs.getConversionRate(c) * 1440);

            hunters += c.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
            dailyHunt += cs.getProteinProduction(c);

            miners += c.getAssignedRoleCount(GameConstants.ROLE_MINER);
            dailyMine += cs.getMineralProduction(c);

            scouts += c.getAssignedRoleCount(GameConstants.ROLE_SCOUT);

            researchers += c.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER);
            assistants += c.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT);
            int baseSpeed = cs.getResearchSpeed(c);
            dailyRP += (researchers * baseSpeed + (int)(assistants * (baseSpeed / 5.0))) * 24;

            layers += c.getAssignedRoleCount(GameConstants.ROLE_LAYER);
            dailyEggs += (int)(c.getAssignedRoleCount(GameConstants.ROLE_LAYER) * cs.getLayingRate(c) * 24);

            nurses += c.getAssignedRoleCount(GameConstants.ROLE_NURSE);
            babies += c.getEggs().size() + c.getLarvae().size() + c.getPupae().size();
            nurseCap += (int)(c.getAssignedRoleCount(GameConstants.ROLE_NURSE) * cs.getNursingRate(c));

            gravers += c.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
            deadAnts += c.getDeadAnts().size();
            graveCap += (int)(c.getAssignedRoleCount(GameConstants.ROLE_GRAVER) * cs.getGravingRate(c));

            ranchers += c.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
            aphids += c.getAphids();
            aphidCap += c.getAssignedRoleCount(GameConstants.ROLE_RANCHER) * cs.getAphidCapacity(c);

            police += c.getAssignedRoleCount(GameConstants.ROLE_POLICE);
            parasites += c.getParasites();
            dailyDetect += Math.round(c.getAssignedRoleCount(GameConstants.ROLE_POLICE) * cs.getParasiteDetection(c));
        }

        if (foragers > 0) model.addRow(new Object[]{"Foraging", foragers + " Foragers", "Combined", "~" + dailyForage + " res/day"});
        if (farmers > 0) model.addRow(new Object[]{"Farming", farmers + " Eff. Farmers", "Combined", "~" + dailyFarm + " convert/day"});
        if (hunters > 0) model.addRow(new Object[]{"Hunting", hunters + " Hunters", "Combined", "~" + dailyHunt + " pwr/day"});
        if (miners > 0) model.addRow(new Object[]{"Mining", miners + " Miners", "Combined", "~" + dailyMine + " pwr/day"});
        if (scouts > 0) model.addRow(new Object[]{"Scouting", scouts + " Scouts", "---", "Finding resources"});
        if (researchers + assistants > 0) model.addRow(new Object[]{"Research", researchers + " Res / " + assistants + " Asst", "Combined", "+" + dailyRP + " pts/day"});
        if (layers > 0) model.addRow(new Object[]{"Egg Laying", layers + " Layers", "Combined", "+" + dailyEggs + " eggs/day"});
        if (nurses > 0) model.addRow(new Object[]{"Nursing", nurses + " Nurses", nurseCap + " Cap", babies + " / " + nurseCap + " Load"});
        if (gravers > 0) model.addRow(new Object[]{"Grave Keeping", gravers + " Gravers", graveCap + " Cap", deadAnts + " / " + graveCap + " Load"});
        if (ranchers > 0) model.addRow(new Object[]{"Ranching", ranchers + " Ranchers", aphidCap + " Cap", aphids + " / " + aphidCap + " Aphids"});
        if (police > 0) model.addRow(new Object[]{"Policing", police + " Police", parasites + " Parasites", "~" + dailyDetect + " det./day"});
    }

    private void updateUnitStatsData() {
        DefaultTableModel model = (DefaultTableModel) unitStatsTable.getModel();
        model.setRowCount(0);
        ColonyStatsService stats = colony.getStatsService();

        // Unit stats are generally the same across the species/dynasty due to global upgrades,
        // but we'll show them based on the current colony's perspective.
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

        List<Colony> coloniesToCount = new ArrayList<>();
        if (dynastyModeToggle.isSelected() && colony.getDynasty() != null) {
            coloniesToCount.addAll(colony.getDynasty().getColonies());
        } else {
            coloniesToCount.add(colony);
        }

        Map<String, Integer> aggregateDeaths = new HashMap<>();
        int totalDeaths = 0;

        for (Colony c : coloniesToCount) {
            if (c.getPopulationService() != null) {
                Map<String, Integer> stats = c.getPopulationService().getDeathStatistics();
                for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                    aggregateDeaths.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
                totalDeaths += c.getTotalDeaths();
            }
        }
        
        if (aggregateDeaths.isEmpty()) {
            model.addRow(new Object[]{"No deaths recorded", 0});
        } else {
            for (Map.Entry<String, Integer> entry : aggregateDeaths.entrySet()) {
                model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        
        model.addRow(new Object[]{"------", "------"});
        model.addRow(new Object[]{dynastyModeToggle.isSelected() ? "Dynasty Total" : "Colony Total", totalDeaths});
    }
}
