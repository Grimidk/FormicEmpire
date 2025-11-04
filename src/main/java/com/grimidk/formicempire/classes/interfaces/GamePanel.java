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
import java.util.HashMap;
import java.util.Map;

public class GamePanel extends JPanel {
    private final MainFrame frame;
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

    private JPanel timePanel;
    private JPanel antsDetailPanel;
    private JPanel resourcesDetailPanel;

    private Runnable tickListener;
    private JButton playPauseButton;

    private JLabel dateTimeLabel;
    private JLabel timeOfDayLabel;
    private JLabel moonPhaseLabel;
    private JLabel seasonLabel;
    private JLabel weatherLabel;

    private JLabel statusIndicator;
    private JButton speedUpButton;
    private JButton speedDownButton;
    private JLabel tickLabel;

    private JButton menuButton;
    private JPopupMenu gameMenu;

    private volatile boolean engineStarted = false;
    private int speedLevel = 1;

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
        this.tickListener = null;

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

        dateTimeLabel = new JLabel("00:00 01/01/0000");
        timeOfDayLabel = new JLabel(); 
        moonPhaseLabel = new JLabel(); 
        seasonLabel = new JLabel();    
        weatherLabel = new JLabel();  

        speedDownButton = new JButton("Speed- (-)");
        speedUpButton = new JButton("Speed+ (+)");
        tickLabel = new JLabel("Tick: 250ms");
        playPauseButton = new JButton("Pause (Space)");
        menuButton = new JButton("Menu (ESC)");

        antsDetailPanel = new JPanel();
        antsDetailPanel.setBorder(new TitledBorder("Ants"));
        antsDetailPanel.setLayout(new BoxLayout(antsDetailPanel, BoxLayout.Y_AXIS));
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

        resourcesDetailPanel = new JPanel();
        resourcesDetailPanel.setBorder(new TitledBorder("Resources"));
        resourcesDetailPanel.setLayout(new BoxLayout(resourcesDetailPanel, BoxLayout.Y_AXIS));
        resourcesDetailPanel.add(mushroomsLabel);
        resourcesDetailPanel.add(planLabel);
        resourcesDetailPanel.add(proteinLabel);
        resourcesDetailPanel.add(waterLabel);
        resourcesDetailPanel.add(syrupLabel);
        resourcesDetailPanel.add(resinLabel);
        resourcesDetailPanel.add(mineralLabel);

        timePanel = new JPanel();
        timePanel.setBorder(new TitledBorder("Time"));
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.Y_AXIS));
        timePanel.add(dateTimeLabel);
        timePanel.add(timeOfDayLabel);
        timePanel.add(moonPhaseLabel);
        timePanel.add(weatherLabel);
        timePanel.add(seasonLabel);

        // --- Set Static Icons ---
        queensLabel.setIcon(GameConstants.TYPE_QUEEN.getIcon());
        princessLabel.setIcon(GameConstants.TYPE_PRINCESS.getIcon());
        droneLabel.setIcon(GameConstants.TYPE_DRONE.getIcon());
        majorLabel.setIcon(GameConstants.TYPE_MAJOR.getIcon());
        soldiersLabel.setIcon(GameConstants.TYPE_SOLDIER.getIcon());
        workersLabel.setIcon(GameConstants.TYPE_WORKER.getIcon());
        pupaLabel.setIcon(GameConstants.TYPE_PUPA.getIcon());
        larvaLabel.setIcon(GameConstants.TYPE_LARVA.getIcon());
        eggsLabel.setIcon(GameConstants.TYPE_EGG.getIcon());
        deadAntsLabel.setIcon(GameConstants.TYPE_DEAD.getIcon());

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

        mushroomsLabel.setIcon(GameConstants.FUNGI_RESOURCE.getIcon()); 
        planLabel.setIcon(GameConstants.PLANT_RESOURCE.getIcon());
        proteinLabel.setIcon(GameConstants.MEAT_RESOURCE.getIcon());  
        waterLabel.setIcon(GameConstants.WATER_RESOURCE.getIcon());  
        syrupLabel.setIcon(GameConstants.SYRUP_RESOURCE.getIcon());    
        resinLabel.setIcon(GameConstants.RESIN_RESOURCE.getIcon());
        mineralLabel.setIcon(GameConstants.ROCK_RESOURCE.getIcon());

        mushroomsLabel.setToolTipText(GameConstants.FUNGI_RESOURCE.getName());
        planLabel.setToolTipText(GameConstants.PLANT_RESOURCE.getName());
        proteinLabel.setToolTipText(GameConstants.MEAT_RESOURCE.getName());
        waterLabel.setToolTipText(GameConstants.WATER_RESOURCE.getName());
        syrupLabel.setToolTipText(GameConstants.SYRUP_RESOURCE.getName());
        resinLabel.setToolTipText(GameConstants.RESIN_RESOURCE.getName());
        mineralLabel.setToolTipText(GameConstants.ROCK_RESOURCE.getName());
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
        stats.add(totalAntLabel);
        stats.add(totalResourcesLabel);

        JPanel antsWrapper = new JPanel(new BorderLayout());
        antsWrapper.add(antsDetailPanel, BorderLayout.NORTH);
        stats.add(antsWrapper);

        JPanel resourcesWrapper = new JPanel(new BorderLayout());
        resourcesWrapper.add(resourcesDetailPanel, BorderLayout.NORTH);
        stats.add(resourcesWrapper);

        return stats;
    }

    private JPanel createEastPanel() {
        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.add(timePanel);
        return east;
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

    private void initListeners() {
        speedDownButton.addActionListener(e -> {
            if (speedLevel > 0) speedLevel--;
            applySpeedLevel();
        });

        speedUpButton.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine == null) return;

            int maxLevel = SPEED_DELAYS.length - 1; 
            
            if (!engine.isAllowTurboMode()) {
                maxLevel = 6;
            }

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
                playPauseButton.setText("Pause (Space)");
                updateStatusIndicator(false);
            } else {
                engine.pauseEngine();
                playPauseButton.setText("Play (Space)");
                updateStatusIndicator(true);
            }
        });

        gameMenu = new JPopupMenu();
        JMenuItem backToGame = new JMenuItem("Back to Game");
        JMenuItem manageRoles = new JMenuItem("Manage Roles");
        JMenuItem openSettings = new JMenuItem("Settings");
        JMenuItem showTutorial = new JMenuItem("Show Tutorial");
        JMenuItem quitToMenu = new JMenuItem("Quit to Main Menu");

        backToGame.addActionListener(e -> gameMenu.setVisible(false));
        manageRoles.addActionListener(e -> showRoleManagementDialog(0));
        
        openSettings.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine != null) {
                engine.pauseEngine();
            }
            playPauseButton.setText("Play (Space)");
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
        gameMenu.add(openSettings);
        gameMenu.add(showTutorial);
        gameMenu.add(new JSeparator());
        gameMenu.add(quitToMenu);

        menuButton.addActionListener(e -> {
            gameMenu.show(menuButton, 0, -gameMenu.getPreferredSize().height);
        });
    }

    private void showRoleManagementDialog(int tabIndex) {
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null || engine.getWorld().getSpawnHex() == null) {
            return;
        }
        Colony colony = engine.getWorld().getSpawnHex().getColony();
        if (colony == null) return;

        JDialog roleDialog = new JDialog(frame, "Manage Ant Roles", true);
        roleDialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(GameConstants.TYPE_WORKER.getName(), GameConstants.TYPE_WORKER.getIcon(), createRolePanel(colony, GameConstants.TYPE_WORKER));
        tabbedPane.addTab(GameConstants.TYPE_SOLDIER.getName(), GameConstants.TYPE_SOLDIER.getIcon(), createRolePanel(colony, GameConstants.TYPE_SOLDIER));
        tabbedPane.addTab(GameConstants.TYPE_MAJOR.getName(), GameConstants.TYPE_MAJOR.getIcon(), createRolePanel(colony, GameConstants.TYPE_MAJOR));
        tabbedPane.addTab(GameConstants.TYPE_PRINCESS.getName(), GameConstants.TYPE_PRINCESS.getIcon(), createRolePanel(colony, GameConstants.TYPE_PRINCESS));
        tabbedPane.addTab(GameConstants.TYPE_QUEEN.getName(), GameConstants.TYPE_QUEEN.getIcon(), createRolePanel(colony, GameConstants.TYPE_QUEEN));
        
        InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = tabbedPane.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "selectTab1");
        actionMap.put("selectTab1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > 0) tabbedPane.setSelectedIndex(0);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "selectTab2");
        actionMap.put("selectTab2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > 1) tabbedPane.setSelectedIndex(1);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "selectTab3");
        actionMap.put("selectTab3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > 2) tabbedPane.setSelectedIndex(2);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "selectTab4");
        actionMap.put("selectTab4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > 3) tabbedPane.setSelectedIndex(3);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "selectTab5");
        actionMap.put("selectTab5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tabbedPane.getTabCount() > 4) tabbedPane.setSelectedIndex(4);
            }
        });

        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }

        roleDialog.add(tabbedPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> roleDialog.dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(closeButton);
        roleDialog.add(southPanel, BorderLayout.SOUTH);

        roleDialog.setPreferredSize(new Dimension(550, 500));
        roleDialog.pack();
        roleDialog.setLocationRelativeTo(frame);

        roleDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                tabbedPane.requestFocusInWindow();
            }
        });

        roleDialog.setVisible(true);
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
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "openRoles1");
        actionMap.put("openRoles1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialog(0); // Workers
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "openRoles2");
        actionMap.put("openRoles2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialog(1); // Soldiers
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "openRoles3");
        actionMap.put("openRoles3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialog(2); // Majors
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "openRoles4");
        actionMap.put("openRoles4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialog(3); // Princesses
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "openRoles5");
        actionMap.put("openRoles5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialog(4); // Queens
            }
        });
    }

    private void handleBackButton() {
        Engine eng = frame.getEngine();
        if (eng != null) {
            eng.pauseEngine();
        }
        updateStatusIndicator(true);
        unregisterTickListener();

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
        new Thread(() -> {
            engine.startUp(savefile);
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Game started");
                registerTickListener();
                updateCounts();
                if (engine != null && !engineStarted) {
                    engineStarted = true;
                    engine.start();
                }
                if(engine.isPaused()) {
                    playPauseButton.setText("Play (Space)");
                } else {
                    playPauseButton.setText("Pause (Space)");
                }
            });
        }).start();
    }

    private void registerTickListener() {
        unregisterTickListener();
        Engine engine = frame.getEngine();
        if (engine == null) return;
        tickListener = () -> SwingUtilities.invokeLater(this::updateCounts);
        engine.addTickListener(tickListener);
        updateTickLabel(engine);
        updateStatusIndicator(engine.isPaused());
    }

    private void unregisterTickListener() {
        if (tickListener != null) {
            Engine engine = frame.getEngine();
            if (engine != null) engine.removeTickListener(tickListener);
        }
        tickListener = null;
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

        int maxLevel = SPEED_DELAYS.length - 1;
        if (!eng.isAllowTurboMode() && maxLevel == 7) {
             maxLevel = 6; 
        }
        
        if (speedLevel > maxLevel) {
            speedLevel = maxLevel;
        }
        if (speedLevel < 0) {
            speedLevel = 0;
        }

        float delay = SPEED_DELAYS[speedLevel];

        if (delay == -1f) {
            eng.pauseEngine();
            updateStatusIndicator(true);
            playPauseButton.setText("Play (Space)");
            tickLabel.setText("Tick: PAUSED");
        } else {
            eng.setDelay(delay);
            eng.resumeEngine(); 
            updateStatusIndicator(false);
            playPauseButton.setText("Pause (Space)");
            updateTickLabel(eng);
        }
    }

    private void updateCounts() {
        Engine engine = frame.getEngine();
        if (engine == null) return;
        World world = engine.getWorld();
        if (world == null) return;
        if (world.getHexes() == null || world.getHexes().isEmpty()) return;
        Colony colony = world.getSpawnHex().getColony();
        if (colony == null) return;

        int totalAnts = colony.getAntTotal();
        int queens = colony.getQueens() != null ? colony.getQueens().size() : 0;
        int princesses = colony.getPrincesses() != null ? colony.getPrincesses().size() : 0;
        int drones = colony.getDrones() != null ? colony.getDrones().size() : 0;
        int majors = colony.getMajors() != null ? colony.getMajors().size() : 0;
        int soldiers = colony.getSoldiers() != null ? colony.getSoldiers().size() : 0;
        int workers = colony.getWorkers() != null ? colony.getWorkers().size() : 0;
        int pupa = colony.getPupae() != null ? colony.getPupae().size() : 0;
        int larva = colony.getLarvae() != null ? colony.getLarvae().size() : 0;
        int eggs = colony.getEggs() != null ? colony.getEggs().size() : 0;
        int deadAnts = colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0;

        int mushrooms = colony.getMushrooms(); 
        int plants = colony.getPlants();     
        int protein = colony.getProtein();  
        int water = colony.getWater();
        int syrups = colony.getSyrups();
        int resins = colony.getResins();
        int minerals = colony.getMinerals();
        int totalResources = mushrooms + plants + protein + water + syrups + resins + minerals;

        totalAntLabel.setText("Total ants: " + totalAnts);
        queensLabel.setText(String.valueOf(queens));
        princessLabel.setText(String.valueOf(princesses));
        droneLabel.setText(String.valueOf(drones));
        majorLabel.setText(String.valueOf(majors));
        soldiersLabel.setText(String.valueOf(soldiers));
        workersLabel.setText(String.valueOf(workers));
        pupaLabel.setText(String.valueOf(pupa));
        larvaLabel.setText(String.valueOf(larva));
        eggsLabel.setText(String.valueOf(eggs));
        deadAntsLabel.setText(String.valueOf(deadAnts));

        totalResourcesLabel.setText("Total resources: " + totalResources);
        mushroomsLabel.setText(String.valueOf(mushrooms));
        planLabel.setText(String.valueOf(plants));
        proteinLabel.setText(String.valueOf(protein));
        waterLabel.setText(String.valueOf(water));
        syrupLabel.setText(String.valueOf(syrups));
        resinLabel.setText(String.valueOf(resins));
        mineralLabel.setText(String.valueOf(minerals));

        String dateTime = String.format("%02d:%02d %02d/%02d/%04d",
        world.getHour(), world.getMinute(), world.getDay(), world.getMonth(), world.getYear());
        dateTimeLabel.setText(dateTime);

        TimeOfDay currentTimeOfDay = world.getTimeOfDay();
        timeOfDayLabel.setIcon(currentTimeOfDay.getIcon());
        timeOfDayLabel.setToolTipText(currentTimeOfDay.getName());

        MoonPhase currentMoonPhase = world.getMoonPhase();
        moonPhaseLabel.setIcon(currentMoonPhase.getIcon());
        moonPhaseLabel.setToolTipText(currentMoonPhase.getName());

        Season currentSeason = world.getSeason();
        seasonLabel.setIcon(currentSeason.getIcon());
        seasonLabel.setToolTipText(currentSeason.getName());

        Weather currentWeather = world.getWeather();
        weatherLabel.setIcon(currentWeather.getIcon());
        weatherLabel.setToolTipText(currentWeather.getName());
    }
}