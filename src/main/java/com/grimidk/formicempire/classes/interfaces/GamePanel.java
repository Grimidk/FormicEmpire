package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.AlertManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.repositories.WorldSpaces;
import com.grimidk.formicempire.classes.interfaces.game.dialogs.*;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.*;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class GamePanel extends ZeroGamePanel {
    private final MainFrame frame;
    
    private JLabel statusLabel;
    private JLabel statusIndicator;
    
    private ColonyPanel colonyPanel; 
    private WorldPanel worldPanel;  
    private AlertPanel alertPanel; 
    private ControlPanel controlPanel;
    private GameAreaPanel gameAreaPanel;
    private JScrollPane gameScrollPane;
    
    private HatchRateDialog hatchDialog;
    private RoleManagementDialog roleDialog;    
    private UpgradeDialog upgradeDialog; 
    private AbilitiesDialog abilitiesDialog;
    private MapDialog mapDialog;
    private StatsDialog statsDialog; 
    private DynastyManagementDialog dynastyDialog;

    private AlertManager alertManager;
    private TriggerManager triggerManager; 

    private Runnable minuteTickListener;
    private Runnable hourTickListener;
    private Runnable dayTickListener;
    private Runnable monthTickListener;
    
    private volatile boolean engineStarted = false;
    
    public GamePanel(MainFrame frame) {
        super(new BorderLayout()); 
        this.frame = frame;
        
        initComponents();
        initControlPanelCallbacks();
        initLayout();        
        updateStatusIndicator(false);
    }

    @Override
    protected void initComponents() {
        statusLabel = new JLabel("Game not started");
        statusIndicator = new JLabel();
        
        colonyPanel = new ColonyPanel();
        worldPanel = new WorldPanel();
        alertPanel = new AlertPanel();
        gameAreaPanel = new GameAreaPanel();
        
        gameScrollPane = new JScrollPane(gameAreaPanel);
        gameScrollPane.setBorder(null);
        gameScrollPane.getViewport().setOpaque(false);
        gameScrollPane.setOpaque(false);
        gameScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gameScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        gameScrollPane.getVerticalScrollBar().setUnitIncrement(16);        
        gameScrollPane.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateGameAreaSize();
            }
        });
    }
    
    private void initControlPanelCallbacks() {
        Runnable handleBackButtonCallback = this::handleBackButton;
        Runnable showHatchRateDialogCallback = this::showHatchRateDialog;        
        Runnable showResearchDialogCallback = this::showResearchDialog;
        Runnable showBuildDialogCallback = this::showBuildDialog;
        Runnable showAssimilationDialogCallback = this::showAssimilationDialog;
        Runnable showSynergyDialogCallback = this::showSynergyDialog;
        Runnable showAbilitiesDialogCallback = this::showAbilitiesDialog;
        Runnable showMapDialogCallback = this::showMapDialog; 
        Runnable showStatsDialogCallback = this::showStatsDialog;
        Runnable showDynastyDialogCallback = this::showDynastyDialog;
        Runnable showTradeDialogCallback = this::showTradeDialog;
        ControlPanel.RoleManagementCallback showRoleManagementDialogCallback = this::showRoleManagementDialog;
        
        Runnable toggleViewCallback = () -> {
            if (gameAreaPanel != null) {
                gameAreaPanel.toggleDimension();
                
                updateGameAreaSize();
                
                if (gameAreaPanel.getCurrentDimension() == WorldSpaces.OVERWORLD) {
                    gameScrollPane.getVerticalScrollBar().setValue(0);
                }
            }
        };
        
        controlPanel = new ControlPanel(frame, 
            handleBackButtonCallback, 
            showHatchRateDialogCallback, 
            showResearchDialogCallback,
            showBuildDialogCallback,
            showAssimilationDialogCallback,
            showSynergyDialogCallback,
            showRoleManagementDialogCallback,
            showAbilitiesDialogCallback,
            showStatsDialogCallback,
            toggleViewCallback,
            showMapDialogCallback,
            showDynastyDialogCallback,
            showTradeDialogCallback); 
    }
    
    private void updateGameAreaSize() {
        if (gameAreaPanel != null && gameScrollPane != null) {
            java.awt.Dimension viewportSize = gameScrollPane.getViewport().getSize();
            gameAreaPanel.refreshSize(viewportSize.width, viewportSize.height);
        }
    }

    @Override
    protected void initLayout() {
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER); 
        if (controlPanel != null) {
            add(controlPanel, BorderLayout.SOUTH);
        }
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
        JPanel center = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;
        gbc.weighty = 1.0; 
        gbc.fill = GridBagConstraints.BOTH;

        // --- Colony Panel (Left) ---
        colonyPanel.setPreferredSize(new Dimension(200, 0));
        colonyPanel.setMinimumSize(new Dimension(200, 0));
        gbc.gridx = 0;
        gbc.weightx = 0.0; 
        center.add(colonyPanel, gbc);

        // --- Game Area (Middle) ---
        gameScrollPane.setPreferredSize(new Dimension(100, 0));
        gbc.gridx = 1;
        gbc.weightx = 0.6; 
        center.add(gameScrollPane, gbc); 

        // --- Right Panel Container (World + Alert) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(230, 0));
        rightPanel.setMinimumSize(new Dimension(230, 0));
        rightPanel.add(worldPanel, BorderLayout.NORTH);
        rightPanel.add(alertPanel, BorderLayout.CENTER); 

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        center.add(rightPanel, gbc);
        
        return center;
    }

    // --- Dialog Methods ---
    private void showHatchRateDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (hatchDialog != null) {
            hatchDialog.dispose();
        }
        hatchDialog = new HatchRateDialog(frame, colony);
        hatchDialog.showDialog();
    }

    private void showRoleManagementDialog(int tabIndex) {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (roleDialog != null) {
            roleDialog.dispose();
        }
        roleDialog = new RoleManagementDialog(frame, colony);
        roleDialog.showDialog(tabIndex);
    }

    private void showResearchDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony);
        upgradeDialog.showDialog(UpgradeDialog.TAB_RESEARCH);
    }
    
    private void showBuildDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony);
        upgradeDialog.showDialog(UpgradeDialog.TAB_BUILD);
    }

    private void showAssimilationDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony);
        upgradeDialog.showDialog(UpgradeDialog.TAB_ASSIMILATION);
    }

    private void showSynergyDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (upgradeDialog != null) {
            upgradeDialog.dispose();
        }
        upgradeDialog = new UpgradeDialog(frame, colony);
        upgradeDialog.showDialog(UpgradeDialog.TAB_SYNERGY);
    }

    private void showAbilitiesDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || !colony.isPlayer()) return;
        
        if (abilitiesDialog != null) {
            abilitiesDialog.dispose();
        }
        abilitiesDialog = new AbilitiesDialog(frame, colony);
        abilitiesDialog.showDialog();
    }

    private void showMapDialog() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        if (world == null) return;
        
        if (mapDialog == null || mapDialog.getOwner() != frame) {
            if (mapDialog != null) mapDialog.dispose();
            mapDialog = new MapDialog(frame, world, this::refreshAllGUIData);
        }
        mapDialog.showDialog();
    }

    private void showStatsDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null) return;

        if (statsDialog != null) {
            statsDialog.dispose();
        }
        
        statsDialog = new StatsDialog(frame, colony, engine);
        statsDialog.showDialog();
    }

    private void showDynastyDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) return;

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(DynastyManagementDialog.TAB_OVERVIEW);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony);
        dynastyDialog.showDialog(DynastyManagementDialog.TAB_OVERVIEW);
    }

    private void showTradeDialog() {
        Engine engine = frame.getEngine();
        Colony colony = getColonyFromEngine(engine);
        if (colony == null || colony.getDynasty() == null) return;

        if (dynastyDialog != null && dynastyDialog.isShowing()) {
            dynastyDialog.setTab(DynastyManagementDialog.TAB_TRADE);
            dynastyDialog.requestFocus();
            return;
        }

        if (dynastyDialog != null) {
            dynastyDialog.dispose();
        }

        dynastyDialog = new DynastyManagementDialog(frame, colony.getDynasty(), engine, this::handleGoToColony);
        dynastyDialog.showDialog(DynastyManagementDialog.TAB_TRADE);
    }

    private void handleGoToColony(Colony target) {
        Engine engine = frame.getEngine();
        if (engine == null || engine.getWorld() == null) return;
        
        World world = engine.getWorld();
        if (world.getHexes() != null) {
            for (Hex hex : world.getHexes()) {
                if (hex.getColony() == target) {
                    world.changeActiveHex(hex);
                    
                    if (gameAreaPanel != null) {
                        gameAreaPanel.setColony(target);
                        gameAreaPanel.resetView(); 
                    }
                    
                    refreshAllGUIData();
                    updateGameAreaSize();
                    
                    if (triggerManager != null) {
                    }
                    break;
                }
            }
        }
    }
    
    private Colony getColonyFromEngine(Engine engine) {
        return engine != null && engine.getWorld() != null && engine.getWorld().getActiveHex() != null ? engine.getWorld().getActiveHex().getColony() : null;
    }
    
    public boolean isEngineStarted() { return engineStarted; }

    // --- Cleanup Methods ---
    private void disposeAllDialogs() {
        if (hatchDialog != null) { hatchDialog.dispose(); hatchDialog = null; }
        if (roleDialog != null) { roleDialog.dispose(); roleDialog = null; }
        if (upgradeDialog != null) { upgradeDialog.dispose(); upgradeDialog = null; }
        if (abilitiesDialog != null) { abilitiesDialog.dispose(); abilitiesDialog = null; }
        if (mapDialog != null) { mapDialog.dispose(); mapDialog = null; }
        if (statsDialog != null) { statsDialog.dispose(); statsDialog = null; }
        if (dynastyDialog != null) { dynastyDialog.dispose(); dynastyDialog = null; }
    }

    private void cleanupSession() {
        disposeAllDialogs();
        unregisterTickListeners();
        
        if (gameAreaPanel != null) gameAreaPanel.resetView();
        if (colonyPanel != null) colonyPanel.reset();
        if (worldPanel != null) worldPanel.reset();
        if (alertPanel != null) alertPanel.updateAlerts(new ArrayList<>());
        
        Engine eng = frame.getEngine();
        if (eng != null) {
            eng.pauseEngine();
        }
        updateStatusIndicator(true);
        if (controlPanel != null) {
            controlPanel.setPlayPauseButtonText(true);
        }
        
        this.triggerManager = null; 
        this.engineStarted = false;
        statusLabel.setText("Game not started");
    }

    private void handleBackButton() {
        Engine eng = frame.getEngine();
        if (eng != null) eng.pauseEngine();
        
        try {
            SaveManager sm = new SaveManager();
            Engine engine = frame.getEngine();
            if (engine != null && engine.getWorld() != null) {
                World w = engine.getWorld();
                int slotIdLocal = w.getSaveSlotId();
                if (slotIdLocal > 0) {
                    Savefile existing = sm.loadSlot(slotIdLocal);
                    String nameToUse = (existing != null && existing.getName() != null && !existing.getName().trim().isEmpty()) ? existing.getName() : ("Save " + slotIdLocal);
                    sm.saveWorldToSlotUserAsync(engine.getWorld(), engine, slotIdLocal, nameToUse, () -> {
                        cleanupSession();
                        frame.showCard(MainFrame.CARD_SAVE);
                    });
                } else {
                    sm.saveWorldToSlot(engine.getWorld(), 0);
                    cleanupSession();
                    frame.showCard(MainFrame.CARD_SAVE);
                }
            } else {
                cleanupSession();
                frame.showCard(MainFrame.CARD_SAVE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            cleanupSession();
            frame.showCard(MainFrame.CARD_SAVE);
        }
    }
    
    public void quitToMenuWithoutSaving() {
        cleanupSession();
        frame.showCard(MainFrame.CARD_SAVE);
    }

    public void enterWithSavefile(Savefile savefile) {
        statusLabel.setText("Starting game...");
        Engine engine = frame.getEngine();
        
        JDialog loadingDialog = new JDialog(frame, "Loading", true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        panel.setBackground(Color.DARK_GRAY);

        JLabel label = new JLabel("Loading game, please wait...", SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        panel.add(label, BorderLayout.CENTER);
        loadingDialog.add(panel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(frame);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                engine.startUp(savefile);
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    get();
                    statusLabel.setText("Game started");
                    registerTickListeners(); 
                    
                    World world = engine.getWorld();
                    Colony colony = null;
                    if (world != null && world.getActiveHex() != null && world.getActiveHex().getColony() != null) {
                        colony = world.getActiveHex().getColony();
                        gameAreaPanel.setColony(colony);
                        
                        alertManager = new AlertManager(colony, alertPanel);

                        triggerManager = new TriggerManager(world, colony, engine);
                        if (frame instanceof TriggerManager.TriggerListener) {
                            triggerManager.addListener((TriggerManager.TriggerListener) frame);
                        }
                        triggerManager.registerListeners();
                    }
                    
                    updateStaticWorldInfo();
                    refreshAllGUIData();
                    updateGameAreaSize();
                    
                    SwingUtilities.invokeLater(() -> {
                        if (!engineStarted) {
                            engineStarted = true;
                            if (!engine.isAlive()) {
                                engine.start();
                            }
                        }
                        controlPanel.setPlayPauseButtonText(engine.isPaused());
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    statusLabel.setText("Error loading game!");
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
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
        
        controlPanel.updateTickLabel(engine);
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

    public void updateStatusIndicator(boolean paused) {
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

    public void refreshAllGUIData() {
        updateMinuteGUI();
        updateHourGUI();
        updateDayGUI();
        updateMonthGUI();
    }

    private void updateStaticWorldInfo() {
        World world = frame.getEngine().getWorld();
        if (world == null) return;
        worldPanel.updateStaticData(world);
        if (world.getActiveHex() != null && world.getActiveHex().getBiome() != null) {
            String biomeName = world.getActiveHex().getBiome().getName();
            gameAreaPanel.setBackgroundByBiome(biomeName);
        }
    }

    private void updateMinuteGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getActiveHex() != null ? world.getActiveHex().getColony() : null;
        
        gameAreaPanel.setColony(colony);        
        updateStaticWorldInfo(); 
        
        if (world == null) return;

        if (colony != null) {
            int w = gameAreaPanel.getWidth();
            int h = gameAreaPanel.getHeight();
            if (w > 1 && h > 1) {
                colony.setGameAreaDimensions(w, h);
            }
            colony.runPhysics(gameAreaPanel.getCurrentDimension()); 
        }

        worldPanel.updateMinuteData(world);
        colonyPanel.updateMinuteData(colony);
        gameAreaPanel.repaint(); 
    }

    private void updateHourGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getActiveHex() != null ? world.getActiveHex().getColony() : null;
        if (world == null) return;

        worldPanel.updateHourData(world);
        colonyPanel.updateHourData(colony);
        
        if (colony != null && colony.isPlayer()) {
            updateGameAreaSize();
        }

        if (colony != null && colony.isPlayer()) {
            if (upgradeDialog != null && upgradeDialog.isShowing()) {
                upgradeDialog.liveUpdate();
            }
            if (abilitiesDialog != null && abilitiesDialog.isShowing()) {
                abilitiesDialog.liveUpdate();
            }
            if (statsDialog != null && statsDialog.isShowing()) {
                statsDialog.liveUpdate();
            }
            if (dynastyDialog != null && dynastyDialog.isShowing()) {
                dynastyDialog.liveUpdate();
            }
            if (controlPanel != null) {
                controlPanel.updateResearchMenu(colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH));
                controlPanel.updateBuildMenu(colony.hasUpgrade(GameUnlocks.ABILITY_BUILD));
                controlPanel.updateAssimilationMenu(colony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION));
                controlPanel.updateSynergyMenu(colony.hasUpgrade(GameUnlocks.ABILITY_SYNERGY));
                controlPanel.updateAbilitiesMenu(colony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT));
                controlPanel.updateDynastyMenu(colony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY));
                controlPanel.updateTradeMenu(colony.hasUpgrade(GameUnlocks.ABILITY_TRADE));
            }
        } else {
             if (controlPanel != null) {
                controlPanel.updateResearchMenu(false);
                controlPanel.updateBuildMenu(false);
                controlPanel.updateAssimilationMenu(false);
                controlPanel.updateSynergyMenu(false);
                controlPanel.updateAbilitiesMenu(false);
                controlPanel.updateDynastyMenu(false);
                controlPanel.updateTradeMenu(false);
            }
        }
    }

    private void updateDayGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getActiveHex() != null ? world.getActiveHex().getColony() : null;
        if (world == null) return;

        worldPanel.updateDayData(world);
        colonyPanel.updateDayData(colony);
        
        if (alertManager != null) {
            alertManager.checkStatus();
        }

        if (roleDialog != null && roleDialog.isShowing()) {
            roleDialog.liveUpdate();
        }
    }

    private void updateMonthGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        if (world == null) return;
        worldPanel.updateMonthData(world);
    }
}
