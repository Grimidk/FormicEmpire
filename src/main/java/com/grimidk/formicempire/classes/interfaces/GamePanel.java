package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.constants.AntRole;
import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.constants.MoonPhase;
import com.grimidk.formicempire.classes.constants.Season;
import com.grimidk.formicempire.classes.constants.TimeOfDay;
import com.grimidk.formicempire.classes.constants.Weather;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.World;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePanel extends JPanel {
    private final MainFrame frame;
    
    // --- UI Components ---
    private JLabel statusLabel;
    private JLabel totalAntLabel;
    private JLabel queensLabel;
    private JLabel princessLabel;
    private JLabel droneLabel;
    private JLabel majorLabel;
    private JLabel soldiersLabel;
    private JLabel workersLabel;
    private JLabel larvaLabel;
    private JLabel pupaLabel;
    private JLabel eggsLabel;
    private JLabel deadAntsLabel;

    private JLabel mushroomsLabel;
    private JLabel planLabel;
    private JLabel proteinLabel;
    private JLabel waterLabel;
    private JLabel syrupLabel;
    private JLabel resinLabel;
    private JLabel mineralLabel;
    private JLabel totalResourcesLabel;

    private JLabel totalConsumptionLabel;
    private JLabel totalProductionLabel;
    private JLabel netMushroomsLabel;
    private JLabel layingRateLabel;
    private JLabel nurseCoverageLabel;
    private JLabel graveKeepingLabel;
    private JLabel ranchingRateLabel;
    private JLabel babyAntsLabel;
    private JLabel adultAntsLabel;
    
    private JLabel biomeLabel;
    private JLabel temperatureLabel;
    private JLabel humidityLabel;
    
    private JLabel dateTimeLabel;
    private JLabel timeOfDayLabel;
    private JLabel moonPhaseLabel;
    private JLabel seasonLabel;
    private JLabel weatherLabel;

    private JLabel statusIndicator;
    private JButton speedUpButton;
    private JButton speedDownButton;
    private JLabel tickLabel;
    private JButton playPauseButton;
    private JButton menuButton;
    private JPopupMenu gameMenu;
    
    private JDialog hatchDialog;
    private JDialog roleDialog;

    // --- State & Engine ---
    private Runnable minuteTickListener;
    private Runnable hourTickListener;
    private Runnable dayTickListener;
    private Runnable monthTickListener;
    
    private volatile boolean engineStarted = false;
    private int speedLevel = 1;

    // --- Cached Values for UI Optimization ---
    private int lastTotalAnts = -1;
    private int lastQueens = -1;
    private int lastPrincesses = -1;
    private int lastDrones = -1;
    private int lastMajors = -1;
    private int lastSoldiers = -1;
    private int lastWorkers = -1;
    private int lastPupa = -1;
    private int lastLarva = -1;
    private int lastEggs = -1;
    private int lastDeadAnts = -1;
    private int lastTotalResources = -1;
    private int lastMushrooms = -1;
    private int lastPlants = -1;
    private int lastProtein = -1;
    private int lastWater = -1;
    private int lastSyrups = -1;
    private int lastResins = -1;
    private int lastMinerals = -1;
    private int lastTotalConsumption = -1;
    private int lastTotalProduction = -1;
    private int lastNetMushrooms = -1;
    private int lastLayingRate = -1;
    private int lastBabyAntTotal = -1;
    private int lastNurseCapacity = -1;
    private int lastGraveCapacity = -1;
    private int lastRanchingRate = -1;
    private int lastBabyTotal = -1;
    private int lastAdultTotal = -1;
    private String lastDateTime = "";
    private String lastBiome = "";
    private int lastTemperature = -999;
    private int lastHumidity = -1;
    private TimeOfDay lastTimeOfDay = null;
    private MoonPhase lastMoonPhase = null;
    private Season lastSeason = null;
    private Weather lastWeather = null;


    private static final float[] SPEED_DELAYS = {
        -1f,    // Level 0 (Paused)
        250f,   // Level 1
        125f,   // Level 2
        50f,    // Level 3
        25f,    // Level 4
        10f,    // Level 5
        5f,     // Level 6
        1f      // Level 7 (Turbo)
    };

    public GamePanel(MainFrame frame) {
        this.frame = frame;
        this.minuteTickListener = null;
        this.hourTickListener = null;
        this.dayTickListener = null;
        this.monthTickListener = null;

        initComponents();
        initLayout();
        initListeners();
        initKeyBindings();

        updateTickLabel(frame.getEngine());
        updateStatusIndicator(false);
    }

    private void initComponents() {
        statusLabel = new JLabel("Game not started");
        statusIndicator = new JLabel();

        totalAntLabel = new JLabel("Total ants: 0");
        queensLabel = new JLabel("0");
        princessLabel = new JLabel("0");
        droneLabel = new JLabel("0");
        majorLabel = new JLabel("0");
        soldiersLabel = new JLabel("0");
        workersLabel = new JLabel("0");
        pupaLabel = new JLabel("0");
        larvaLabel = new JLabel("0");
        eggsLabel = new JLabel("0");
        deadAntsLabel = new JLabel("0"); 

        totalResourcesLabel = new JLabel("Total resources: 0");
        mushroomsLabel = new JLabel("0");
        planLabel = new JLabel("0");
        proteinLabel = new JLabel("0");
        waterLabel = new JLabel("0");
        syrupLabel = new JLabel("0");
        resinLabel = new JLabel("0");
        mineralLabel = new JLabel("0");

        totalConsumptionLabel = new JLabel("Consumption: 0/day");
        totalProductionLabel = new JLabel("Max Food Prod: 0/day");
        netMushroomsLabel = new JLabel("Net Food: 0/day");
        layingRateLabel = new JLabel("Laying Rate: 0/day");
        nurseCoverageLabel = new JLabel("Nurse Coverage: 0/0");
        graveKeepingLabel = new JLabel("Grave Capacity: 0");
        ranchingRateLabel = new JLabel("Ranching: 0");
        babyAntsLabel = new JLabel("Baby Ants: 0");
        adultAntsLabel = new JLabel("Adult Ants: 0");
        
        biomeLabel = new JLabel("Biome: N/A");
        temperatureLabel = new JLabel("Temp: 0°C");
        humidityLabel = new JLabel("Humidity: 0");

        dateTimeLabel = new JLabel("00:00 01/01/0000");
        timeOfDayLabel = new JLabel(); 
        moonPhaseLabel = new JLabel(); 
        seasonLabel = new JLabel();    
        weatherLabel = new JLabel();  

        speedDownButton = new JButton("Speed-");
        speedUpButton = new JButton("Speed+");
        tickLabel = new JLabel("Tick: 250ms");
        playPauseButton = new JButton("Pause");
        menuButton = new JButton("Menu");

        // --- Set Static Icons & Tooltips ---
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

        setupConstantLabel(mushroomsLabel, GameConstants.FUNGI_RESOURCE);
        setupConstantLabel(planLabel, GameConstants.PLANT_RESOURCE);
        setupConstantLabel(proteinLabel, GameConstants.MEAT_RESOURCE);
        setupConstantLabel(waterLabel, GameConstants.WATER_RESOURCE);
        setupConstantLabel(syrupLabel, GameConstants.SYRUP_RESOURCE);
        setupConstantLabel(resinLabel, GameConstants.RESIN_RESOURCE);
        setupConstantLabel(mineralLabel, GameConstants.ROCK_RESOURCE);
    }
    
    private void setupConstantLabel(JLabel label, Object constant) {
        if (constant instanceof AntType) {
            AntType type = (AntType) constant;
            label.setIcon(type.getIcon());
            label.setToolTipText(type.getName());
        } 
        else if (constant == GameConstants.FUNGI_RESOURCE) {
             label.setIcon(GameConstants.FUNGI_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.FUNGI_RESOURCE.getName());
        } else if (constant == GameConstants.PLANT_RESOURCE) {
             label.setIcon(GameConstants.PLANT_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.PLANT_RESOURCE.getName());
        } else if (constant == GameConstants.MEAT_RESOURCE) {
             label.setIcon(GameConstants.MEAT_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.MEAT_RESOURCE.getName());
        } else if (constant == GameConstants.WATER_RESOURCE) {
             label.setIcon(GameConstants.WATER_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.WATER_RESOURCE.getName());
        } else if (constant == GameConstants.SYRUP_RESOURCE) {
             label.setIcon(GameConstants.SYRUP_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.SYRUP_RESOURCE.getName());
        } else if (constant == GameConstants.RESIN_RESOURCE) {
             label.setIcon(GameConstants.RESIN_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.RESIN_RESOURCE.getName());
        } else if (constant == GameConstants.ROCK_RESOURCE) {
             label.setIcon(GameConstants.ROCK_RESOURCE.getIcon());
             label.setToolTipText(GameConstants.ROCK_RESOURCE.getName());
        }
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.WEST); 
        add(createEastPanel(), BorderLayout.EAST);
        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel createNorthPanel() {
        statusIndicator.setOpaque(true);
        statusIndicator.setBackground(Color.GRAY);
        statusIndicator.setPreferredSize(new Dimension(12, 12));
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(statusIndicator);
        north.add(statusLabel);
        return north;
    }

    private JPanel createCenterPanel() {
        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));

        JPanel antsWrapper = new JPanel(new BorderLayout());
        antsWrapper.add(createAntsDetailPanel(), BorderLayout.NORTH);
        stats.add(antsWrapper);

        JPanel resourcesWrapper = new JPanel(new BorderLayout());
        resourcesWrapper.add(createResourcesDetailPanel(), BorderLayout.NORTH);
        stats.add(resourcesWrapper);

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.add(createColonyStatsPanel(), BorderLayout.NORTH);
        stats.add(statsWrapper);

        return stats;
    }
    
    private JPanel createAntsDetailPanel() {
        JPanel antsDetailPanel = new JPanel();
        antsDetailPanel.setBorder(new TitledBorder("Ants"));
        antsDetailPanel.setLayout(new BoxLayout(antsDetailPanel, BoxLayout.Y_AXIS));
        antsDetailPanel.add(totalAntLabel);
        antsDetailPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        antsDetailPanel.add(queensLabel);
        antsDetailPanel.add(princessLabel);
        antsDetailPanel.add(droneLabel);
        antsDetailPanel.add(majorLabel);
        antsDetailPanel.add(soldiersLabel);
        antsDetailPanel.add(workersLabel);
        antsDetailPanel.add(pupaLabel);
        antsDetailPanel.add(larvaLabel);
        antsDetailPanel.add(eggsLabel);
        antsDetailPanel.add(deadAntsLabel);
        return antsDetailPanel;
    }
    
    private JPanel createResourcesDetailPanel() {
        JPanel resourcesDetailPanel = new JPanel();
        resourcesDetailPanel.setBorder(new TitledBorder("Resources"));
        resourcesDetailPanel.setLayout(new BoxLayout(resourcesDetailPanel, BoxLayout.Y_AXIS));
        resourcesDetailPanel.add(totalResourcesLabel);
        resourcesDetailPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        resourcesDetailPanel.add(mushroomsLabel);
        resourcesDetailPanel.add(planLabel);
        resourcesDetailPanel.add(proteinLabel);
        resourcesDetailPanel.add(waterLabel);
        resourcesDetailPanel.add(syrupLabel);
        resourcesDetailPanel.add(resinLabel);
        resourcesDetailPanel.add(mineralLabel);
        return resourcesDetailPanel;
    }
    
    private JPanel createColonyStatsPanel() {
        JPanel colonyStatsPanel = new JPanel();
        colonyStatsPanel.setBorder(new TitledBorder("Colony Stats"));
        colonyStatsPanel.setLayout(new BoxLayout(colonyStatsPanel, BoxLayout.Y_AXIS));
        colonyStatsPanel.add(totalConsumptionLabel);
        colonyStatsPanel.add(totalProductionLabel);
        colonyStatsPanel.add(netMushroomsLabel);
        colonyStatsPanel.add(layingRateLabel);
        colonyStatsPanel.add(nurseCoverageLabel);
        colonyStatsPanel.add(graveKeepingLabel);
        colonyStatsPanel.add(ranchingRateLabel);
        colonyStatsPanel.add(babyAntsLabel);
        colonyStatsPanel.add(adultAntsLabel);
        return colonyStatsPanel;
    }

    private JPanel createEastPanel() {
        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.add(createTimePanel());
        east.add(createWorldInfoPanel());
        return east;
    }
    
    private JPanel createTimePanel() {
        JPanel timePanel = new JPanel();
        timePanel.setBorder(new TitledBorder("Time"));
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.add(dateTimeLabel);
        timePanel.add(timeOfDayLabel);
        timePanel.add(moonPhaseLabel);
        timePanel.add(weatherLabel);
        timePanel.add(seasonLabel);
        return timePanel;
    }
    
    private JPanel createWorldInfoPanel() {
        JPanel worldInfoPanel = new JPanel();
        worldInfoPanel.setBorder(new TitledBorder("World"));
        worldInfoPanel.setLayout(new BoxLayout(worldInfoPanel, BoxLayout.Y_AXIS));
        worldInfoPanel.add(biomeLabel);
        worldInfoPanel.add(temperatureLabel);
        worldInfoPanel.add(humidityLabel);
        return worldInfoPanel;
    }

    private JPanel createSouthPanel() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(speedDownButton);
        south.add(speedUpButton);
        south.add(tickLabel);
        south.add(playPauseButton);
        south.add(menuButton);
        return south;
    }

    private int getMaxSpeedLevel() {
        Engine engine = frame.getEngine();
        if (engine == null) return 1;
        
        int maxLevel = SPEED_DELAYS.length - 1; 
        if (!engine.isAllowTurboMode() && maxLevel > 6) {
            maxLevel = 6;
        }
        return maxLevel;
    }

    private void initListeners() {
        speedDownButton.addActionListener(e -> {
            if (speedLevel > 0) speedLevel--;
            applySpeedLevel();
        });

        speedUpButton.addActionListener(e -> {
            int maxLevel = getMaxSpeedLevel();
            if (speedLevel < maxLevel) {
                speedLevel++;
            }
            applySpeedLevel();
        });

        playPauseButton.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine == null || !engineStarted) return;
            if (engine.isPaused()) {
                engine.resumeEngine();
                playPauseButton.setText("Pause");
                updateStatusIndicator(false);
            } else {
                engine.pauseEngine();
                playPauseButton.setText("Play");
                updateStatusIndicator(true);
            }
        });

        gameMenu = new JPopupMenu();
        JMenuItem backToGame = new JMenuItem("Back to Game");
        JMenuItem manageRoles = new JMenuItem("Manage Roles");
        JMenuItem manageHatchRates = new JMenuItem("Manage Hatch Rates");
        JMenuItem openSettings = new JMenuItem("Settings");
        JMenuItem showTutorial = new JMenuItem("Show Tutorial");
        JMenuItem quitToMenu = new JMenuItem("Quit to Main Menu");

        backToGame.addActionListener(e -> gameMenu.setVisible(false));
        manageRoles.addActionListener(e -> showRoleManagementDialog(0));
        manageHatchRates.addActionListener(e -> showHatchRateDialog());
        
        openSettings.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine != null) {
                engine.pauseEngine();
            }
            playPauseButton.setText("Play");
            updateStatusIndicator(true);

            try {
                SaveManager sm = new SaveManager();
                if (engine != null && engine.getWorld() != null) {
                    sm.saveAutosaveAsync(engine.getWorld(), engine, () -> {
                        frame.showCard(MainFrame.CARD_SETTINGS);
                    });
                } else {
                    frame.showCard(MainFrame.CARD_SETTINGS);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                frame.showCard(MainFrame.CARD_SETTINGS);
            }
        });
        
        showTutorial.addActionListener(e -> HelpPanel.showTutorialDialog(frame));
        
        quitToMenu.addActionListener(e -> handleBackButton());
        
        gameMenu.add(backToGame);
        gameMenu.add(manageRoles);
        gameMenu.add(manageHatchRates);
        gameMenu.add(openSettings);
        gameMenu.add(showTutorial);
        gameMenu.add(new JSeparator());
        gameMenu.add(quitToMenu);

        menuButton.addActionListener(e -> {
            gameMenu.show(menuButton, 0, -gameMenu.getPreferredSize().height);
        });
    }

    private void showHatchRateDialog() {
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null || engine.getWorld().getSpawnHex() == null) {
            return;
        }
        Colony colony = engine.getWorld().getSpawnHex().getColony();
        if (colony == null) return;

        if (hatchDialog == null) {
            hatchDialog = new JDialog(frame, "Manage Pupa Hatch Rates", true);
            hatchDialog.setLayout(new BorderLayout());
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> hatchDialog.dispose());
            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPanel.add(closeButton);
            hatchDialog.add(southPanel, BorderLayout.SOUTH);
            hatchDialog.setPreferredSize(new Dimension(400, 350));
        }
        
        Component oldCenter = ((BorderLayout)hatchDialog.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (oldCenter != null) {
            hatchDialog.remove(oldCenter);
        }

        JPanel panel = createHatchRatePanel(colony);
        hatchDialog.add(panel, BorderLayout.CENTER);

        hatchDialog.pack();
        hatchDialog.setLocationRelativeTo(frame);
        hatchDialog.setVisible(true);
    }

    private JPanel createHatchRatePanel(Colony colony) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel totalLabel = new JLabel("Total: 100.00%");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        
        panel.add(new JLabel("Set hatch chance for new ants:"));
        panel.add(totalLabel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        Map<AntType, JSpinner> spinnerMap = new HashMap<>();
        List<AntType> typesToRate = Arrays.asList(
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_DRONE,
            GameConstants.TYPE_PRINCESS
        );

        for (AntType type : typesToRate) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            JLabel typeLabel = new JLabel(type.getIcon());
            typeLabel.setToolTipText(type.getName());
            row.add(typeLabel);
            
            float currentRate = colony.getHatchRate(type);
            SpinnerModel model = new SpinnerNumberModel((double)currentRate, 0.0, 100.0, 0.01);
            JSpinner spinner = new JSpinner(model);
            spinner.setPreferredSize(new Dimension(80, 25));

            spinner.addChangeListener(e -> {
                double newValue = (Double) spinner.getValue();
                colony.setHatchRate(type, (float)newValue);
                updateHatchRateTotals(colony, totalLabel, spinnerMap);
            });
            
            spinnerMap.put(type, spinner);
            row.add(spinner);
            panel.add(row);
        }
        
        updateHatchRateTotals(colony, totalLabel, spinnerMap);
        return panel;
    }

    private void updateHatchRateTotals(Colony colony, JLabel totalLabel, Map<AntType, JSpinner> spinnerMap) {
        double totalAssigned = 0.0;
        for (JSpinner s : spinnerMap.values()) {
            totalAssigned += (Double) s.getValue();
        }
        
        double unassigned = 100.0 - totalAssigned;
        
        totalLabel.setText(String.format("Total: %.2f%%", totalAssigned));
        
        if (Math.abs(unassigned) > 0.01) { 
            totalLabel.setForeground(Color.RED);
            totalLabel.setToolTipText(String.format("Warning: Total is not 100%%. You are %.2f%% over/under.", -unassigned));
        } else {
            totalLabel.setForeground(Color.BLACK);
            totalLabel.setToolTipText("Total is 100%");
        }

        for (JSpinner s : spinnerMap.values()) {
            SpinnerNumberModel model = (SpinnerNumberModel) s.getModel();
            double currentValue = (Double) s.getValue();
            double newMax = currentValue + Math.max(0.0, unassigned);
            model.setMaximum(Math.max(currentValue, newMax));
        }
    }

    private void showRoleManagementDialog(int tabIndex) {
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null || engine.getWorld().getSpawnHex() == null) {
            return;
        }
        Colony colony = engine.getWorld().getSpawnHex().getColony();
        if (colony == null) return;

        if (roleDialog == null) {
            roleDialog = new JDialog(frame, "Manage Ant Roles", true);
            roleDialog.setLayout(new BorderLayout());
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> roleDialog.dispose());
            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            southPanel.add(closeButton);
            roleDialog.add(southPanel, BorderLayout.SOUTH);
            
            roleDialog.setPreferredSize(new Dimension(550, 500));
            
            roleDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    Component centerComp = ((BorderLayout)roleDialog.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
                    if (centerComp instanceof JTabbedPane) {
                        centerComp.requestFocusInWindow();
                    }
                }
            });
        }
        
        Component oldCenter = ((BorderLayout)roleDialog.getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (oldCenter != null) {
            roleDialog.remove(oldCenter);
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(GameConstants.TYPE_WORKER.getName(), GameConstants.TYPE_WORKER.getIcon(), createRolePanel(colony, GameConstants.TYPE_WORKER));
        tabbedPane.addTab(GameConstants.TYPE_SOLDIER.getName(), GameConstants.TYPE_SOLDIER.getIcon(), createRolePanel(colony, GameConstants.TYPE_SOLDIER));
        tabbedPane.addTab(GameConstants.TYPE_MAJOR.getName(), GameConstants.TYPE_MAJOR.getIcon(), createRolePanel(colony, GameConstants.TYPE_MAJOR));
        tabbedPane.addTab(GameConstants.TYPE_PRINCESS.getName(), GameConstants.TYPE_PRINCESS.getIcon(), createRolePanel(colony, GameConstants.TYPE_PRINCESS));
        tabbedPane.addTab(GameConstants.TYPE_QUEEN.getName(), GameConstants.TYPE_QUEEN.getIcon(), createRolePanel(colony, GameConstants.TYPE_QUEEN));
        
        InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = tabbedPane.getActionMap();
        
        addTabSwitchAction(inputMap, actionMap, "selectTab1", KeyEvent.VK_Q, tabbedPane, 0);
        addTabSwitchAction(inputMap, actionMap, "selectTab2", KeyEvent.VK_W, tabbedPane, 1);
        addTabSwitchAction(inputMap, actionMap, "selectTab3", KeyEvent.VK_E, tabbedPane, 2);
        addTabSwitchAction(inputMap, actionMap, "selectTab4", KeyEvent.VK_R, tabbedPane, 3);
        addTabSwitchAction(inputMap, actionMap, "selectTab5", KeyEvent.VK_T, tabbedPane, 4);

        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
        
        roleDialog.add(tabbedPane, BorderLayout.CENTER);
        roleDialog.pack();
        roleDialog.setLocationRelativeTo(frame);
        roleDialog.setVisible(true);
    }
    
    private void addTabSwitchAction(InputMap im, ActionMap am, String name, int key, JTabbedPane pane, int index) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pane.getTabCount() > index) pane.setSelectedIndex(index);
            }
        });
    }


    private JPanel createRolePanel(Colony colony, AntType antType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int totalAnts = colony.getAntsByType(antType).size();
        
        JLabel totalLabel = new JLabel("Total " + antType.getName() + "s: " + totalAnts);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        JLabel assignedLabel = new JLabel("Total Assigned: 0");
        JLabel unassignedLabel = new JLabel("Unassigned: " + totalAnts);
        
        panel.add(totalLabel);
        panel.add(assignedLabel);
        panel.add(unassignedLabel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        Map<AntRole, JSpinner> spinnerMap = new HashMap<>();

        for (AntRole role : GameConstants.getAntRoles()) {
            if (role.getAntType() == antType) {
                JPanel roleRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
                roleRow.add(new JLabel(role.getName() + ":"));
                
                int currentAssigned = colony.getAssignedRoleCount(role);
                SpinnerModel model = new SpinnerNumberModel(currentAssigned, 0, totalAnts, 1);
                JSpinner spinner = new JSpinner(model);
                spinner.setPreferredSize(new Dimension(80, 25));

                spinner.addChangeListener(e -> {
                    int newValue = (Integer) spinner.getValue();
                    int otherSpinnersTotal = 0;
                    for (Map.Entry<AntRole, JSpinner> entry : spinnerMap.entrySet()) {
                        if (entry.getValue() != spinner) {
                            otherSpinnersTotal += (Integer) entry.getValue().getValue();
                        }
                    }

                    int newTotalAssigned = newValue + otherSpinnersTotal;
                    if (newTotalAssigned > totalAnts) {
                        int allowedValue = Math.max(0, totalAnts - otherSpinnersTotal);
                        SwingUtilities.invokeLater(() -> spinner.setValue(allowedValue));
                        newValue = allowedValue;
                    }
                    
                    colony.setAssignedRoleCount(role, newValue);
                    updateRolePanelTotals(totalAnts, assignedLabel, unassignedLabel, spinnerMap);
                });
                
                roleRow.add(spinner);
                spinnerMap.put(role, spinner);
                panel.add(roleRow);
            }
        }
        
        updateRolePanelTotals(totalAnts, assignedLabel, unassignedLabel, spinnerMap);
        return panel;
    }
    
    private void updateRolePanelTotals(int totalAnts, JLabel assignedLabel, JLabel unassignedLabel, Map<AntRole, JSpinner> spinnerMap) {
        int totalAssigned = 0;
        for (JSpinner s : spinnerMap.values()) {
            totalAssigned += (Integer) s.getValue();
        }
        
        int unassigned = totalAnts - totalAssigned;
        
        assignedLabel.setText("Total Assigned: " + totalAssigned);
        unassignedLabel.setText("Unassigned: " + unassigned);

        if (totalAssigned > totalAnts) {
            assignedLabel.setForeground(Color.RED);
            assignedLabel.setToolTipText("You have assigned more roles than you have ants.");
            unassignedLabel.setForeground(Color.RED);
            unassignedLabel.setToolTipText("You have assigned more roles than you have ants.");
        } else {
            assignedLabel.setForeground(Color.BLACK);
            assignedLabel.setToolTipText(null);
            unassignedLabel.setForeground(Color.BLACK);
            unassignedLabel.setToolTipText(null);
        }
    }


    private void initKeyBindings() {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "togglePause");
        actionMap.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playPauseButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "speedUp");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.SHIFT_DOWN_MASK), "speedUp");
        actionMap.put("speedUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speedUpButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "speedDown");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "speedDown");
        actionMap.put("speedDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                speedDownButton.doClick();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "openMenu");
        actionMap.put("openMenu", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameMenu.isVisible()) {
                    gameMenu.setVisible(false);
                } else {
                    menuButton.doClick();
                }
            }
        });

        // Key bindings for role management tabs
        addRoleKeyBinding(inputMap, actionMap, "openRoles1", KeyEvent.VK_Q, 0);
        addRoleKeyBinding(inputMap, actionMap, "openRoles2", KeyEvent.VK_W, 1);
        addRoleKeyBinding(inputMap, actionMap, "openRoles3", KeyEvent.VK_E, 2);
        addRoleKeyBinding(inputMap, actionMap, "openRoles4", KeyEvent.VK_R, 3);
        addRoleKeyBinding(inputMap, actionMap, "openRoles5", KeyEvent.VK_T, 4);
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "openHatchRates");
        actionMap.put("openHatchRates", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHatchRateDialog();
            }
        });
    }

    private void addRoleKeyBinding(InputMap im, ActionMap am, String name, int key, int tab) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialog(tab);
            }
        });
    }

    private void handleBackButton() {
        Engine eng = frame.getEngine();
        if (eng != null) {
            eng.pauseEngine();
        }
        updateStatusIndicator(true);
        unregisterTickListeners(); 

        try {
            SaveManager sm = new SaveManager();
            Engine engine = frame.getEngine();
            if (engine != null && engine.getWorld() != null) {
                World w = engine.getWorld();
                int slotIdLocal = 0;
                try {
                    slotIdLocal = w.getSaveSlotId();
                } catch (Exception ignore) {
                    slotIdLocal = 0;
                }

                final int capturedSlot = slotIdLocal;
                if (capturedSlot > 0) {
                    Savefile existing = sm.loadSlot(capturedSlot);
                    String nameToUse = (existing != null && existing.getName() != null && !existing.getName().trim().isEmpty())
                                       ? existing.getName() : ("Save " + capturedSlot);

                    sm.saveWorldToSlotUserAsync(engine.getWorld(), engine, capturedSlot, nameToUse, () -> {
                        frame.showCard(MainFrame.CARD_SAVE);
                    });
                    return;
                } else {
                    sm.saveWorldToSlot(engine.getWorld(), 0); 
                    frame.showCard(MainFrame.CARD_SAVE);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        frame.showCard(MainFrame.CARD_SAVE);
    }

    public void enterWithSavefile(Savefile savefile) {
        statusLabel.setText("Starting game...");
        Engine engine = frame.getEngine();
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                engine.startUp(savefile);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    statusLabel.setText("Game started");
                    resetCachedValues(); 
                    registerTickListeners(); 
                    updateStaticWorldInfo();
                    updateMinuteGUI();
                    updateHourGUI();
                    updateDayGUI();
                    updateMonthGUI();
                    
                    if (!engineStarted) {
                        engineStarted = true;
                        engine.start();
                    }
                    if(engine.isPaused()) {
                        playPauseButton.setText("Play");
                    } else {
                        playPauseButton.setText("Pause");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading game!");
                }
            }
        };
        
        worker.execute();
    }

    private void registerTickListeners() {
        unregisterTickListeners();
        Engine engine = frame.getEngine();
        if (engine == null) return;
        
        minuteTickListener = () -> SwingUtilities.invokeLater(this::updateMinuteGUI);
        hourTickListener = () -> SwingUtilities.invokeLater(this::updateHourGUI);
        dayTickListener = () -> SwingUtilities.invokeLater(this::updateDayGUI);
        monthTickListener = () -> SwingUtilities.invokeLater(this::updateMonthGUI);
        
        engine.addTickListener(minuteTickListener);
        engine.addHourTickListener(hourTickListener);
        engine.addDayTickListener(dayTickListener);
        engine.addMonthTickListener(monthTickListener);
        
        updateTickLabel(engine);
        updateStatusIndicator(engine.isPaused());
    }

    private void unregisterTickListeners() {
        Engine engine = frame.getEngine();
        if (engine != null) {
            if (minuteTickListener != null) engine.removeTickListener(minuteTickListener);
            if (hourTickListener != null) engine.removeHourTickListener(hourTickListener);
            if (dayTickListener != null) engine.removeDayTickListener(dayTickListener);
            if (monthTickListener != null) engine.removeMonthTickListener(monthTickListener);
        }
        minuteTickListener = null;
        hourTickListener = null;
        dayTickListener = null;
        monthTickListener = null;
    }

    private void updateTickLabel(Engine eng) {
        if (eng == null) {
            tickLabel.setText("Tick: -");
            return;
        }
        tickLabel.setText("Tick: " + (long) eng.getDelay() + "ms");
    }

    private void updateStatusIndicator(boolean paused) {
        if (!engineStarted) {
            statusIndicator.setBackground(Color.GRAY);
            statusLabel.setText("Game not started");
            return;
        }
        if (paused) {
            statusIndicator.setBackground(Color.RED);
            statusLabel.setText("Paused");
        } else {
            statusIndicator.setBackground(Color.GREEN);
            statusLabel.setText("Running");
        }
    }

    private void applySpeedLevel() {
        Engine eng = frame.getEngine();
        if (eng == null) return;

        int maxLevel = getMaxSpeedLevel();
        
        if (speedLevel > maxLevel) speedLevel = maxLevel;
        if (speedLevel < 0) speedLevel = 0;

        float delay = SPEED_DELAYS[speedLevel];

        if (delay == -1f) {
            eng.pauseEngine();
            updateStatusIndicator(true);
            playPauseButton.setText("Play");
            tickLabel.setText("Tick: PAUSED");
        } else {
            eng.setDelay(delay);
            if (!engineStarted || eng.isPaused()) {
                eng.resumeEngine();
            }
            updateStatusIndicator(false);
            playPauseButton.setText("Pause");
            updateTickLabel(eng);
        }
    }

    private void resetCachedValues() {
        lastTotalAnts = -1;
        lastQueens = -1;
        lastPrincesses = -1;
        lastDrones = -1;
        lastMajors = -1;
        lastSoldiers = -1;
        lastWorkers = -1;
        lastPupa = -1;
        lastLarva = -1;
        lastEggs = -1;
        lastDeadAnts = -1;
        lastTotalResources = -1;
        lastMushrooms = -1;
        lastPlants = -1;
        lastProtein = -1;
        lastWater = -1;
        lastSyrups = -1;
        lastResins = -1;
        lastMinerals = -1;
        lastTotalConsumption = -1;
        lastTotalProduction = -1;
        lastNetMushrooms = -1;
        lastLayingRate = -1;
        lastBabyAntTotal = -1;
        lastNurseCapacity = -1;
        lastGraveCapacity = -1;
        lastRanchingRate = -1;
        lastBabyTotal = -1;
        lastAdultTotal = -1;
        lastDateTime = "";
        lastBiome = "";
        lastTemperature = -999;
        lastHumidity = -1;
        lastTimeOfDay = null;
        lastMoonPhase = null;
        lastSeason = null;
        lastWeather = null;
    }

    // --- Event-Driven Update Methods ---
    private void updateStaticWorldInfo() {
        World world = frame.getEngine().getWorld();
        if (world == null) return;
        
        String biomeName = (world.getSpawnHex().getBiome() != null) ? "Biome: " + world.getSpawnHex().getBiome().getName() : "Biome: N/A";
        if (!biomeName.equals(lastBiome)) {
            biomeLabel.setText(biomeName);
            lastBiome = biomeName;
        }
        
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

        Weather currentWeather = world.getWeather();
        if (currentWeather != lastWeather) {
            weatherLabel.setIcon(currentWeather.getIcon());
            weatherLabel.setToolTipText(currentWeather.getName());
            lastWeather = currentWeather;
        }
    }

    private void updateMinuteGUI() {
        Engine engine = frame.getEngine();
        if (engine == null) return;
        World world = engine.getWorld();
        if (world == null) return;
        Colony colony = world.getSpawnHex().getColony();
        if (colony == null) return;

        // --- Time ---
        String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
            world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
        if (!dateTime.equals(lastDateTime)) {
            dateTimeLabel.setText(dateTime);
            lastDateTime = dateTime;
        }

        // --- Resources ---
        int mushrooms = colony.getMushrooms(); 
        int plants = colony.getPlants();     
        int protein = colony.getProtein();  
        int water = colony.getWater(); 
        int syrups = colony.getSyrups();
        int resins = colony.getResins();
        int minerals = colony.getMinerals();
        int totalResources = mushrooms + plants + protein + water + syrups + resins + minerals;
        
        if (totalResources != lastTotalResources) {
            totalResourcesLabel.setText("Total resources: " + totalResources);
            lastTotalResources = totalResources;
        }
        if (mushrooms != lastMushrooms) {
            mushroomsLabel.setText(String.valueOf(mushrooms));
            lastMushrooms = mushrooms;
        }
        if (plants != lastPlants) {
            planLabel.setText(String.valueOf(plants));
            lastPlants = plants;
        }
        if (protein != lastProtein) {
            proteinLabel.setText(String.valueOf(protein));
            lastProtein = protein;
        }
        if (water != lastWater) {
            waterLabel.setText(String.valueOf(water));
            lastWater = water;
        }
        if (syrups != lastSyrups) {
            syrupLabel.setText(String.valueOf(syrups));
            lastSyrups = syrups;
        }
        if (resins != lastResins) {
            resinLabel.setText(String.valueOf(resins));
            lastResins = resins;
        }
        if (minerals != lastMinerals) {
            mineralLabel.setText(String.valueOf(minerals));
            lastMinerals = minerals;
        }
        
        // --- Net Food ---
        int totalConsumption = colony.getTotalConsumption(); 
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        int conversionPerMinute = ((int) colony.getConversionRate()) * farmerCount;
        int totalProductionRate = conversionPerMinute * 4 * 60 * 24; 
        int totalProduction = Math.min(totalProductionRate, colony.getMushroomsCapacity());
        int netMushrooms = totalProduction - totalConsumption;

        if (netMushrooms != lastNetMushrooms) {
            netMushroomsLabel.setText(String.format("Net Food: %d/day", netMushrooms));
            lastNetMushrooms = netMushrooms;
        }
    }

    private void updateHourGUI() {
        Engine engine = frame.getEngine();
        if (engine == null) return;
        World world = engine.getWorld();
        if (world == null) return;
        Colony colony = world.getSpawnHex().getColony();
        if (colony == null) return;

        // --- Time of Day ---
        TimeOfDay currentTimeOfDay = world.getTimeOfDay();
        if (currentTimeOfDay != lastTimeOfDay) {
            timeOfDayLabel.setIcon(currentTimeOfDay.getIcon());
            timeOfDayLabel.setToolTipText(currentTimeOfDay.getName());
            lastTimeOfDay = currentTimeOfDay;
        }

        // --- Eggs ---
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        if (eggs != lastEggs) {
            eggsLabel.setText(String.valueOf(eggs));
            lastEggs = eggs;
        }
        
        // --- Resources ---
        int plants = colony.getPlants();     
        int protein = colony.getProtein();  
        if (plants != lastPlants) {
            planLabel.setText(String.valueOf(plants));
            lastPlants = plants;
        }
        if (protein != lastProtein) {
            proteinLabel.setText(String.valueOf(protein));
            lastProtein = protein;
        }

        // --- Colony Stats ---
        int totalConsumption = colony.getTotalConsumption();
        if (totalConsumption != lastTotalConsumption) {
            totalConsumptionLabel.setText(String.format("Consumption: %d/day", totalConsumption));
            lastTotalConsumption = totalConsumption;
        }

        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        int conversionPerMinute = ((int) colony.getConversionRate()) * farmerCount;
        int totalProductionRate = conversionPerMinute * 4 * 60 * 24; 
        int totalProduction = Math.min(totalProductionRate, colony.getMushroomsCapacity());
        if (totalProduction != lastTotalProduction) {
            totalProductionLabel.setText(String.format("Max Food Prod: %d/day", totalProduction));
            lastTotalProduction = totalProduction;
        }

        int layerCount = colony.getAssignedRoleCount(GameConstants.ROLE_LAYER);
        int hourlyLayingRate = layerCount * colony.getLayingRate();
        int layingRate = hourlyLayingRate * 24;
        if (layingRate != lastLayingRate) {
            layingRateLabel.setText(String.format("Laying Rate: %d/day", layingRate));
            lastLayingRate = layingRate;
        }

        int nurseCount = colony.getAssignedRoleCount(GameConstants.ROLE_NURSE);
        int babyAntTotal = colony.getEggs().size() + colony.getLarvae().size() + colony.getPupae().size();
        int nurseCapacity = (int) (nurseCount * colony.getNursingRate());
        if (babyAntTotal != lastBabyAntTotal || nurseCapacity != lastNurseCapacity) {
            nurseCoverageLabel.setText(String.format("Nurse Coverage: %d/%d", babyAntTotal, nurseCapacity));
            lastBabyAntTotal = babyAntTotal;
            lastNurseCapacity = nurseCapacity;
        }

        int graverCount = colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER);
        int graveCapacity = graverCount * (int) colony.getGravingRate();
        if (graveCapacity != lastGraveCapacity) {
            graveKeepingLabel.setText("Grave Capacity: " + graveCapacity);
            lastGraveCapacity = graveCapacity;
        }

        int rancherCount = colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER);
        int ranchingRate = 0; // Placeholder
        if (ranchingRate != lastRanchingRate) {
            ranchingRateLabel.setText("Ranching: " + ranchingRate);
            lastRanchingRate = ranchingRate;
        }
    }

    private void updateDayGUI() {
        Engine engine = frame.getEngine();
        if (engine == null) return;
        World world = engine.getWorld();
        if (world == null) return;
        Colony colony = world.getSpawnHex().getColony();
        if (colony == null) return;

        // --- Moon Phase ---
        MoonPhase currentMoonPhase = world.getMoonPhase();
        if (currentMoonPhase != lastMoonPhase) {
            moonPhaseLabel.setIcon(currentMoonPhase.getIcon());
            moonPhaseLabel.setToolTipText(currentMoonPhase.getName());
            lastMoonPhase = currentMoonPhase;
        }

        // --- Ant Counts ---
        int totalAnts = colony.getAntTotal();
        if (totalAnts != lastTotalAnts) {
            totalAntLabel.setText("Total ants: " + totalAnts);
            lastTotalAnts = totalAnts;
        }

        int queens = colony.getQueens() != null ? colony.getQueens().size() : 0;
        if (queens != lastQueens) {
            queensLabel.setText(String.valueOf(queens));
            lastQueens = queens;
        }
        
        int princesses = colony.getPrincesses() != null ? colony.getPrincesses().size() : 0;
        if (princesses != lastPrincesses) {
            princessLabel.setText(String.valueOf(princesses));
            lastPrincesses = princesses;
        }

        int drones = colony.getDrones() != null ? colony.getDrones().size() : 0;
        if (drones != lastDrones) {
            droneLabel.setText(String.valueOf(drones));
            lastDrones = drones;
        }

        int majors = colony.getMajors() != null ? colony.getMajors().size() : 0;
        if (majors != lastMajors) {
            majorLabel.setText(String.valueOf(majors));
            lastMajors = majors;
        }

        int soldiers = colony.getSoldiers() != null ? colony.getSoldiers().size() : 0;
        if (soldiers != lastSoldiers) {
            soldiersLabel.setText(String.valueOf(soldiers));
            lastSoldiers = soldiers;
        }

        int workers = colony.getWorkers() != null ? colony.getWorkers().size() : 0;
        if (workers != lastWorkers) {
            workersLabel.setText(String.valueOf(workers));
            lastWorkers = workers;
        }

        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        if (pupa != lastPupa) {
            pupaLabel.setText(String.valueOf(pupa));
            lastPupa = pupa;
        }

        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
        if (larva != lastLarva) {
            larvaLabel.setText(String.valueOf(larva));
            lastLarva = larva;
        }

        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        if (eggs != lastEggs) {
            eggsLabel.setText(String.valueOf(eggs));
            lastEggs = eggs;
        }

        int deadAnts = colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0;
        if (deadAnts != lastDeadAnts) {
            deadAntsLabel.setText(String.valueOf(deadAnts));
            lastDeadAnts = deadAnts;
        }
        
        // --- Ant Totals ---
        int babyTotal = eggs + larva + pupa;
        if (babyTotal != lastBabyTotal) {
            babyAntsLabel.setText("Baby Ants: " + babyTotal);
            lastBabyTotal = babyTotal;
        }
        
        int adultTotal = queens + princesses + drones + majors + soldiers + workers;
        if (adultTotal != lastAdultTotal) {
            adultAntsLabel.setText("Adult Ants: " + adultTotal);
            lastAdultTotal = adultTotal;
        }

        int mushrooms = colony.getMushrooms(); 
        if (mushrooms != lastMushrooms) {
            mushroomsLabel.setText(String.valueOf(mushrooms));
            lastMushrooms = mushrooms;
        }
    }

    private void updateMonthGUI() {
        Engine engine = frame.getEngine();
        if (engine == null) return;
        World world = engine.getWorld();
        if (world == null) return;

        Season currentSeason = world.getSeason();
        if (currentSeason != lastSeason) {
            seasonLabel.setIcon(currentSeason.getIcon());
            seasonLabel.setToolTipText(currentSeason.getName());
            lastSeason = currentSeason;
        }
    }
}