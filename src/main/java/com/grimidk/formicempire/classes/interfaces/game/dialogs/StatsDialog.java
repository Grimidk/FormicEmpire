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
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

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
        super(owner, LanguageStrings.DIALOG_STATS_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;
        this.engine = engine;
        this.dynastyStatsService = new DynastyStatService(); 
        
        setLayout(new BorderLayout());
        
        // --- Top Toggle Panel ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        dynastyModeToggle = new JCheckBox(LanguageStrings.STATS_DYNASTY_MODE);
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
        tabbedPane.setTitleAt(0, LanguageStrings.STATS_TAB_GENERAL);
        tabbedPane.setTitleAt(1, LanguageStrings.STATS_TAB_DYNASTY);
        tabbedPane.setTitleAt(2, LanguageStrings.STATS_TAB_ECONOMY);
        tabbedPane.setTitleAt(3, LanguageStrings.STATS_TAB_POPULATION);
        tabbedPane.setTitleAt(4, LanguageStrings.STATS_TAB_LOCAL_HEX);
        tabbedPane.setTitleAt(5, LanguageStrings.STATS_TAB_RATES);
        tabbedPane.setTitleAt(6, LanguageStrings.STATS_TAB_UNIT_STATS);
        tabbedPane.setTitleAt(7, LanguageStrings.STATS_TAB_MORTALITY);
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
        String[] columns = {LanguageStrings.COL_CATEGORY, LanguageStrings.COL_PROPERTY, LanguageStrings.COL_VALUE};
        generalTable = new JTable(createIconModel(columns));
        tabbedPane.addTab(LanguageStrings.STATS_TAB_GENERAL, createTablePane(generalTable));
    }
    
    private void initDynastyTab() {
        String[] columns = {"", LanguageStrings.COL_SCOPE, LanguageStrings.COL_METRIC, LanguageStrings.COL_VALUE};
        dynastyTable = new JTable(createIconModel(columns));
        
        dynastyTable.getColumnModel().getColumn(0).setMaxWidth(40);
        dynastyTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        tabbedPane.addTab(LanguageStrings.STATS_TAB_DYNASTY, createTablePane(dynastyTable));
    }

    private void initResourceTab() {
        String[] columns = {"", LanguageStrings.COL_RESOURCE, LanguageStrings.COL_CURRENT, LanguageStrings.COL_CAPACITY, LanguageStrings.COL_SOURCES, LanguageStrings.COL_PROD_DAY, LanguageStrings.COL_CONS_DAY, LanguageStrings.COL_NET};
        resourcesTable = new JTable(createIconModel(columns));
        
        resourcesTable.getColumnModel().getColumn(0).setMaxWidth(40);
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        
        tabbedPane.addTab(LanguageStrings.STATS_TAB_ECONOMY, createTablePane(resourcesTable));
    }

    private void initPopulationTab() {
        String[] columns = {"", LanguageStrings.COL_TYPE, LanguageStrings.COL_ROLE, LanguageStrings.COL_COUNT};
        populationTable = new JTable(createIconModel(columns));
        
        populationTable.getColumnModel().getColumn(0).setMaxWidth(40);
        populationTable.getColumnModel().getColumn(0).setPreferredWidth(40);

        tabbedPane.addTab(LanguageStrings.STATS_TAB_POPULATION, createTablePane(populationTable));
    }

    private void initLocalHexTab() {
        String[] columns = {LanguageStrings.COL_CATEGORY, LanguageStrings.COL_PROPERTY, LanguageStrings.COL_VALUE};
        localHexTable = new JTable(createIconModel(columns));
        tabbedPane.addTab(LanguageStrings.STATS_TAB_LOCAL_HEX, createTablePane(localHexTable));
    }

    private void initRatesTab() {
        String[] columns = {LanguageStrings.COL_ACTIVITY, LanguageStrings.COL_ASSIGNED, LanguageStrings.COL_RATE_CAP, LanguageStrings.COL_COV_OUT};
        ratesTable = new JTable(createIconModel(columns));
        tabbedPane.addTab(LanguageStrings.STATS_TAB_RATES, createTablePane(ratesTable));
    }

    private void initUnitStatsTab() {
        String[] columns = {LanguageStrings.COL_STAT, LanguageStrings.COL_BASE_VAL, LanguageStrings.COL_DESCRIPTION};
        unitStatsTable = new JTable(createIconModel(columns));
        tabbedPane.addTab(LanguageStrings.STATS_TAB_UNIT_STATS, createTablePane(unitStatsTable));
    }

    private void initDeathTab() {
        String[] columns = {LanguageStrings.COL_CAUSE, LanguageStrings.COL_TOTAL};
        deathTable = new JTable(createIconModel(columns));
        tabbedPane.addTab(LanguageStrings.STATS_TAB_MORTALITY, createTablePane(deathTable));
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
            model.addRow(new Object[]{LanguageStrings.PANEL_WORLD, LanguageStrings.COL_VALUE, LanguageStrings.STAT_STATUS_NO_HEX});
            return;
        }

        Hex hex = world.getActiveHex();
        Biome biome = hex.getBiome();

        // Hex Coordinates & Basic Info
        model.addRow(new Object[]{LanguageStrings.PANEL_WORLD, LanguageStrings.STAT_COORDS, hex.getQ() + ", " + hex.getR()});
        
        Weather localWeather = hex.getLocalWeather();
        model.addRow(new Object[]{LanguageStrings.PANEL_WORLD, LanguageStrings.STAT_LOCAL_WEATHER, localWeather != null ? localWeather.getName() : LanguageStrings.STAT_USING_GLOBAL});

        if (biome != null) {
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.COL_VALUE, biome.getName()});
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.STAT_BASE_TEMP, biome.getTemperature() + "°C"});
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.STAT_HUMIDITY_LEVEL, biome.isIsHumid() + " / 5"});
            model.addRow(new Object[]{LanguageStrings.STAT_ABUNDANCES, LanguageStrings.RESOURCE_PLANT, String.format("%.2f", biome.getPlantAbundance())});
            model.addRow(new Object[]{LanguageStrings.STAT_ABUNDANCES, LanguageStrings.RESOURCE_MEAT, String.format("%.2f", biome.getAnimalAbundance())});
            model.addRow(new Object[]{LanguageStrings.STAT_ABUNDANCES, LanguageStrings.RESOURCE_ROCK, String.format("%.2f", biome.getMineralAbundance())});
        }

        // Neighbors
        model.addRow(new Object[]{null, null, "------", "------"});
        model.addRow(new Object[]{LanguageStrings.STAT_NEIGHBORS, "North", getHexSummary(hex.getNorth())});
        model.addRow(new Object[]{LanguageStrings.STAT_NEIGHBORS, "North-West", getHexSummary(hex.getNorthWest())});
        model.addRow(new Object[]{LanguageStrings.STAT_NEIGHBORS, "North-East", getHexSummary(hex.getNorthEast())});
        model.addRow(new Object[]{LanguageStrings.STAT_NEIGHBORS, "South", getHexSummary(hex.getSouth())});
        model.addRow(new Object[]{LanguageStrings.STAT_NEIGHBORS, "South-West", getHexSummary(hex.getSouthWest())});
        model.addRow(new Object[]{LanguageStrings.STAT_NEIGHBORS, "South-East", getHexSummary(hex.getSouthEast())});
    }

    private String getHexSummary(Hex neighbor) {
        if (neighbor == null) return LanguageStrings.STAT_EDGE_WORLD;
        String summary = (neighbor.getBiome() != null ? neighbor.getBiome().getName() : LanguageStrings.STAT_UNKNOWN);
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
                model.addRow(new Object[]{LanguageStrings.STAT_DYNASTY, LanguageStrings.COL_VALUE, dynasty.getName()});
                model.addRow(new Object[]{LanguageStrings.STAT_DYNASTY, LanguageStrings.COL_VALUE, dynasty.getRank().getName()});
                model.addRow(new Object[]{LanguageStrings.STAT_DYNASTY, LanguageStrings.COL_VALUE, dynasty.getSpecies() != null ? dynasty.getSpecies().getName() : LanguageStrings.STAT_UNKNOWN});
                model.addRow(new Object[]{LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_TOTAL_COLONIES, dynasty.getColonies().size()});
                model.addRow(new Object[]{LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_GLOBAL_POP, dynastyStatsService.getTotalPopulation(dynasty)});
                
                int totalQueens = dynasty.getColonies().stream().mapToInt(c -> c.getQueens().size()).sum();
                model.addRow(new Object[]{LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_GLOBAL_QUEENS, totalQueens});
            }
        } else {
            // Colony Info
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.COL_VALUE, colony.getName()});
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.COL_VALUE, colony.getRank().getName()});
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.COL_VALUE, colony.getSpecies() != null ? colony.getSpecies().getName() : LanguageStrings.STAT_UNKNOWN});
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.STAT_SPECIES_SCIENTIFIC, colony.getSpecies() != null ? colony.getSpecies().getScientific() : LanguageStrings.STAT_UNKNOWN});
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, "ID", colony.getId()});
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.STAT_AGE, colony.getAge() + LanguageStrings.STAT_DAYS_SUFFIX});
            
            if (colony.getQueens().isEmpty()) {
                model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.STAT_QUEEN_STATUS, LanguageStrings.UI_MISSING + " (" + colony.getDaysWithoutQueen() + LanguageStrings.STAT_DAYS_SUFFIX + ")"});
            } else {
                model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.STAT_QUEEN_STATUS, LanguageStrings.UI_HEALTHY + " (" + colony.getQueens().size() + " " + LanguageStrings.UI_TOTAL + ")"});
            }

            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.STAT_AUTOMATION, colony.isAutomationEnabled() ? LanguageStrings.UI_ENABLED : LanguageStrings.UI_DISABLED});
            model.addRow(new Object[]{LanguageStrings.PANEL_COLONY, LanguageStrings.STAT_AUTO_BUILD, colony.isAutoBuildEnabled() ? LanguageStrings.UI_ENABLED : LanguageStrings.UI_DISABLED});
        }

        // World Info
        World world = engine != null ? engine.getWorld() : null;
        if (world != null) {
            String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
                world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
            
            model.addRow(new Object[]{LanguageStrings.PANEL_WORLD, LanguageStrings.STAT_DATE_TIME, dateTime});
            model.addRow(new Object[]{LanguageStrings.PANEL_WORLD, LanguageStrings.STAT_TIME_DAY, world.getTimeOfDay().getName()});
            model.addRow(new Object[]{LanguageStrings.PANEL_WORLD, LanguageStrings.STAT_MOON_PHASE, world.getMoonPhase().getName()});
            
            Season season = world.getSeason();
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.SEASON_SPRING, season.getName()}); // Season spring is just "Season" generic in key naming I used
            
            Weather weather = world.getWeather();
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.PANEL_WORLD, weather.getName()});
            
            if (world.getActiveHex() != null && world.getActiveHex().getBiome() != null) {
                model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.WORLD_BIOME_PREFIX, world.getActiveHex().getBiome().getName()});
            }
            
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.WORLD_TEMP_PREFIX, world.getTemperature() + "°C"});
            model.addRow(new Object[]{LanguageStrings.STAT_ENVIRONMENT, LanguageStrings.WORLD_HUMIDITY_PREFIX, world.getHumidity()});
        }
    }

    private void updateDynastyData() {
        DefaultTableModel model = (DefaultTableModel) dynastyTable.getModel();
        model.setRowCount(0);
        
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null) {
            model.addRow(new Object[]{null, "Error", LanguageStrings.COL_VALUE, LanguageStrings.STAT_NO_DYNASTY});
            return;
        }

        // Basic Info
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.COL_VALUE, dynasty.getName()});
        model.addRow(new Object[]{dynasty.getRank().getIcon(), LanguageStrings.STAT_DYNASTY, LanguageStrings.COLONY_RANK, dynasty.getRank().getName()}); 
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.COL_VALUE, dynasty.getSpecies() != null ? dynasty.getSpecies().getName() : LanguageStrings.SPECIES_OMNI});
        
        Colony capital = dynasty.getCapital();
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_CAPITAL, (capital != null ? capital.getName() : LanguageStrings.ASSIMILATION_NONE)});
        
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_TOTAL_COLONIES, dynastyStatsService.getTotalColonies(dynasty)});
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_GLOBAL_POP, dynastyStatsService.getTotalPopulation(dynasty)});
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_NUPTIAL_FLIGHTS, dynasty.getTotalNuptialFlights()});

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
        model.addRow(new Object[]{null, LanguageStrings.STAT_DYNASTY, LanguageStrings.STAT_DEFEATED_SPECIES, defeated.isEmpty() ? LanguageStrings.ASSIMILATION_NONE : defeated});
        
        // Unlocks
        model.addRow(new Object[]{null, null, "------", "------"});
        model.addRow(new Object[]{null, "Progress", LanguageStrings.STAT_UPGRADES_RES, dynasty.getUnlockedUpgrades().size()});
        model.addRow(new Object[]{null, "Progress", LanguageStrings.STAT_ASSIM_COMP, dynasty.getCompletedAssimilations().size()});
        
        int totalBuildings = 0;
        for (Colony c : dynasty.getColonies()) {
            totalBuildings += c.getUnlockedBuildings().size();
        }
        model.addRow(new Object[]{null, "Progress", LanguageStrings.STAT_BUILDINGS_BUILT, totalBuildings});

        // Research
        model.addRow(new Object[]{null, null, "------", "------"});
        model.addRow(new Object[]{GameConstants.ICON_RESEARCH, LanguageStrings.TAB_RESEARCH, LanguageStrings.STAT_STORED_POINTS, dynasty.getResearchPoints()});
        model.addRow(new Object[]{null, LanguageStrings.TAB_RESEARCH, LanguageStrings.STAT_GLOBAL_RATE, String.format(LanguageStrings.STAT_PTS_DAY_FORMAT, dynastyStatsService.getGlobalResearchRateDaily(dynasty))});

        // Global Resources
        model.addRow(new Object[]{null, null, "------", "------"});
        Map<ResourceType, Integer> resources = dynastyStatsService.getGlobalResources(dynasty);
        
        model.addRow(new Object[]{GameConstants.RESOURCE_PLANT.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_PLANT, resources.get(GameConstants.RESOURCE_PLANT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_FUNGI.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_FUNGI, resources.get(GameConstants.RESOURCE_FUNGI)});
        model.addRow(new Object[]{GameConstants.RESOURCE_MEAT.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_MEAT, resources.get(GameConstants.RESOURCE_MEAT)});
        model.addRow(new Object[]{GameConstants.RESOURCE_WATER.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_WATER, resources.get(GameConstants.RESOURCE_WATER)});
        model.addRow(new Object[]{GameConstants.RESOURCE_SYRUP.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_SYRUP, resources.getOrDefault(GameConstants.RESOURCE_SYRUP, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_RESIN.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_RESIN, resources.getOrDefault(GameConstants.RESOURCE_RESIN, 0)});
        model.addRow(new Object[]{GameConstants.RESOURCE_ROCK.getIcon(), LanguageStrings.PANEL_RESOURCES, "Global " + LanguageStrings.RESOURCE_ROCK, resources.getOrDefault(GameConstants.RESOURCE_ROCK, 0)});
        
        // Global Deaths
        model.addRow(new Object[]{null, null, "------", "------"});
        int totalDeaths = 0;
        if (dynasty.getGlobalDeathStatistics() != null) {
            totalDeaths = dynasty.getGlobalDeathStatistics().values().stream().mapToInt(Integer::intValue).sum();
        }
        model.addRow(new Object[]{GameConstants.STATUS_DEAD.getIcon(), LanguageStrings.STAT_MORTALITY, LanguageStrings.STAT_GLOBAL_DEATHS, totalDeaths});
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

        addResourceRow(model, GameConstants.RESOURCE_PLANT.getIcon(), LanguageStrings.RESOURCE_PLANT, (int)totalPlants, capPlants, sourcesPlants, maxSources, prodPlants, consPlants);
        addResourceRow(model, GameConstants.RESOURCE_FUNGI.getIcon(), LanguageStrings.RESOURCE_FUNGI, (int)totalMushrooms, capMushrooms, 0, 0, prodMushrooms, consMushrooms);
        addResourceRow(model, GameConstants.RESOURCE_MEAT.getIcon(), LanguageStrings.RESOURCE_MEAT, (int)totalProtein, capProtein, sourcesProtein, maxSources, prodProtein, consProtein);
        addResourceRow(model, GameConstants.RESOURCE_WATER.getIcon(), LanguageStrings.RESOURCE_WATER, (int)totalWater, capWater, sourcesWater, maxSources, prodWater, consWater);
        addResourceRow(model, GameConstants.RESOURCE_ROCK.getIcon(), LanguageStrings.RESOURCE_ROCK, (int)totalMinerals, capMinerals, sourcesMinerals, maxSources, prodMinerals, consMinerals);
        addResourceRow(model, GameConstants.RESOURCE_SYRUP.getIcon(), LanguageStrings.RESOURCE_SYRUP, (int)totalSyrups, capSyrups, 0, 0, prodSyrups, 0);
        addResourceRow(model, GameConstants.RESOURCE_RESIN.getIcon(), LanguageStrings.RESOURCE_RESIN, (int)totalResins, capResins, 0, 0, prodResins, 0);

        model.addRow(new Object[]{null, "------", "---", "---", "---", "---", "---", "---"});
        int netFood = prodMushrooms - consMushrooms;
        model.addRow(new Object[]{null, LanguageStrings.STAT_TOTAL_FOOD, "---", "---", "---", prodMushrooms, consMushrooms, (netFood >= 0 ? "+" : "") + netFood});
    }

    private void addResourceRow(DefaultTableModel model, ImageIcon icon, String name, int current, int cap, int sources, int maxSources, int production, int consumption) {
        String sourceStr = (maxSources > 0) ? sources + " / " + maxSources : LanguageStrings.WORLD_NA;
        String prodStr = String.valueOf(production);
        String consStr = String.valueOf(consumption);
        
        int net = production - consumption;
        String netStr = (net >= 0 ? "+" : "") + net;
        
        if (name.equals(LanguageStrings.RESOURCE_SYRUP) || name.equals(LanguageStrings.RESOURCE_RESIN)) {
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

            model.addRow(new Object[]{type.getIcon(), type.getName(), LanguageStrings.UI_TOTAL, count});

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
        model.addRow(new Object[]{null, LanguageStrings.UI_SUMMARY, LanguageStrings.STAT_ADULTS, totalAdult});
        model.addRow(new Object[]{null, LanguageStrings.UI_SUMMARY, LanguageStrings.STAT_JUVENILES, totalJuvenile});
        model.addRow(new Object[]{null, LanguageStrings.UI_SUMMARY, dynastyModeToggle.isSelected() ? LanguageStrings.STAT_DYNASTY_TOTAL : LanguageStrings.STAT_COLONY_TOTAL, grandTotal});
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

        if (foragers > 0) model.addRow(new Object[]{LanguageStrings.ROLE_FORAGER, foragers + " " + LanguageStrings.ROLE_FORAGER + "s", LanguageStrings.UI_COMBINED, "~" + dailyForage + " res/day"});
        if (farmers > 0) model.addRow(new Object[]{"Farming", farmers + " Eff. Farmers", LanguageStrings.UI_COMBINED, "~" + dailyFarm + " convert/day"});
        if (hunters > 0) model.addRow(new Object[]{LanguageStrings.ROLE_HUNTER, hunters + " " + LanguageStrings.ROLE_HUNTER + "s", LanguageStrings.UI_COMBINED, "~" + dailyHunt + " pwr/day"});
        if (miners > 0) model.addRow(new Object[]{LanguageStrings.ROLE_MINER, miners + " " + LanguageStrings.ROLE_MINER + "s", LanguageStrings.UI_COMBINED, "~" + dailyMine + " pwr/day"});
        if (scouts > 0) model.addRow(new Object[]{LanguageStrings.ROLE_SCOUT, scouts + " " + LanguageStrings.ROLE_SCOUT + "s", "---", "Finding resources"});
        if (researchers + assistants > 0) model.addRow(new Object[]{LanguageStrings.TAB_RESEARCH, researchers + " Res / " + assistants + " Asst", LanguageStrings.UI_COMBINED, "+" + dailyRP + " pts/day"});
        if (layers > 0) model.addRow(new Object[]{"Egg Laying", layers + " Layers", LanguageStrings.UI_COMBINED, "+" + dailyEggs + " eggs/day"});
        if (nurses > 0) model.addRow(new Object[]{LanguageStrings.ROLE_NURSE, nurses + " Nurses", nurseCap + " Cap", babies + " / " + nurseCap + " Load"});
        if (gravers > 0) model.addRow(new Object[]{LanguageStrings.ROLE_GRAVER, gravers + " Gravers", graveCap + " Cap", deadAnts + " / " + graveCap + " Load"});
        if (ranchers > 0) model.addRow(new Object[]{LanguageStrings.ROLE_RANCHER, ranchers + " Ranchers", aphidCap + " Cap", aphids + " / " + aphidCap + " Aphids"});
        if (police > 0) model.addRow(new Object[]{LanguageStrings.ROLE_POLICE, police + " Police", parasites + " Parasites", "~" + dailyDetect + " det./day"});
    }

    private void updateUnitStatsData() {
        DefaultTableModel model = (DefaultTableModel) unitStatsTable.getModel();
        model.setRowCount(0);
        ColonyStatsService stats = colony.getStatsService();

        int hp = stats.getBaseHealth(colony);
        if (hp > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_HEALTH, hp, LanguageStrings.UNIT_STAT_HEALTH_DESC});

        int def = stats.getBaseDefense(colony);
        if (def > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_DEFENSE, def, LanguageStrings.UNIT_STAT_DEFENSE_DESC});

        int atk = stats.getBaseAttack(colony);
        if (atk > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_ATTACK, atk, LanguageStrings.UNIT_STAT_ATTACK_DESC});

        int spd = stats.getBaseSpeed(colony);
        if (spd > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_SPEED, spd, LanguageStrings.UNIT_STAT_SPEED_DESC});

        int regen = stats.getBaseRegen(colony);
        if (regen > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_REGEN, regen, LanguageStrings.UNIT_STAT_REGEN_DESC});

        int tempRes = stats.getBaseTempRes(colony);
        if (tempRes > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_TEMP_RES, tempRes, LanguageStrings.UNIT_STAT_TEMP_RES_DESC});

        int cons = stats.getBaseConsumption(colony);
        if (cons > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_CONSUMPTION, cons, LanguageStrings.UNIT_STAT_CONSUMPTION_DESC});

        float detect = stats.getParasiteDetection(colony);
        if (detect > 0) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_DETECTION, String.format("%.2f", detect), LanguageStrings.UNIT_STAT_DETECTION_DESC});
        
        float contam = stats.getContaminationMitigation(colony);
        if (contam < 1.0f) model.addRow(new Object[]{LanguageStrings.UNIT_STAT_IMMUNITY, String.format("%.0f%%", (1.0f - contam) * 100), LanguageStrings.UNIT_STAT_IMMUNITY_DESC});
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
            model.addRow(new Object[]{LanguageStrings.STAT_NO_DEATHS, 0});
        } else {
            for (Map.Entry<String, Integer> entry : aggregateDeaths.entrySet()) {
                model.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        }
        
        model.addRow(new Object[]{"------", "------"});
        model.addRow(new Object[]{dynastyModeToggle.isSelected() ? LanguageStrings.STAT_DYNASTY_TOTAL : LanguageStrings.STAT_COLONY_TOTAL, totalDeaths});
    }
}