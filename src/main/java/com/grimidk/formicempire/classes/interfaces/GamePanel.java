package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.World;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades;
import com.grimidk.formicempire.classes.infrasctructure.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.TriggerManager;
import com.grimidk.formicempire.classes.interfaces.game.ColonyPanel;
import com.grimidk.formicempire.classes.interfaces.game.ControlPanel;
import com.grimidk.formicempire.classes.interfaces.game.GameAreaPanel;
import com.grimidk.formicempire.classes.interfaces.game.HatchRateDialog;
import com.grimidk.formicempire.classes.interfaces.game.ResearchDialog;
import com.grimidk.formicempire.classes.interfaces.game.RoleManagementDialog;
import com.grimidk.formicempire.classes.interfaces.game.WorldPanel;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {
    private final MainFrame frame;
    
    // --- Core UI Components ---
    private JLabel statusLabel;
    private JLabel statusIndicator;
    
    // --- Refactored Sub-Panels ---
    private ColonyPanel colonyPanel; 
    private WorldPanel worldPanel;  
    private ControlPanel controlPanel;
    private GameAreaPanel gameAreaPanel;
    
    // --- Refactored Dialogs ---
    private HatchRateDialog hatchDialog;
    private RoleManagementDialog roleDialog;
    private ResearchDialog researchDialog;

    // --- State & Engine ---
    private Runnable minuteTickListener;
    private Runnable hourTickListener;
    private Runnable dayTickListener;
    private Runnable monthTickListener;
    
    private volatile boolean engineStarted = false;
    
    public GamePanel(MainFrame frame) {
        this.frame = frame;
        
        initComponents();
        initControlPanelCallbacks();
        initLayout();
        
        updateStatusIndicator(false);
    }

    private void initComponents() {
        statusLabel = new JLabel("Game not started");
        statusIndicator = new JLabel();
        
        colonyPanel = new ColonyPanel();
        worldPanel = new WorldPanel();
        gameAreaPanel = new GameAreaPanel();
    }
    
    private void initControlPanelCallbacks() {
        Runnable handleBackButtonCallback = this::handleBackButton;
        
        Runnable showHatchRateDialogCallback = this::showHatchRateDialog;
        Runnable showResearchDialogCallback = this::showResearchDialog;
        
        ControlPanel.RoleManagementCallback showRoleManagementDialogCallback = this::showRoleManagementDialog;
        
        controlPanel = new ControlPanel(frame, 
                                        handleBackButtonCallback, 
                                        showHatchRateDialogCallback, 
                                        showResearchDialogCallback,
                                        showRoleManagementDialogCallback);
    }

    private void initLayout() {
        setLayout(new BorderLayout());
        add(createNorthPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER); 
        add(controlPanel, BorderLayout.SOUTH);
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

        // 1. Colony Panel (Left)
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.0; 
        gbc.fill = GridBagConstraints.VERTICAL; 
        center.add(colonyPanel, gbc);

        // 2. Game Area (Middle)
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH; 
        center.add(gameAreaPanel, gbc); 

        // 3. World Panel (Right)
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.NORTHEAST; 
        gbc.weightx = 0.0; 
        gbc.fill = GridBagConstraints.VERTICAL; 
        center.add(worldPanel, gbc);
        
        return center;
    }
    
    // --- Dialog Methods ---
    private void showHatchRateDialog() {
        Engine engine = frame.getEngine();
        Colony colony = engine != null && engine.getWorld() != null && engine.getWorld().getSpawnHex() != null ? engine.getWorld().getSpawnHex().getColony() : null;
        if (colony == null) return;

        if (hatchDialog == null || hatchDialog.getOwner() != frame) {
            if (hatchDialog != null) hatchDialog.dispose();
            hatchDialog = new HatchRateDialog(frame, colony);
        } else {
             hatchDialog.dispose();
             hatchDialog = new HatchRateDialog(frame, colony);
        }
        
        hatchDialog.showDialog();
    }

    private void showRoleManagementDialog(int tabIndex) {
        Engine engine = frame.getEngine();
        Colony colony = engine != null && engine.getWorld() != null && engine.getWorld().getSpawnHex() != null ? engine.getWorld().getSpawnHex().getColony() : null;
        if (colony == null) return;

        if (roleDialog == null || roleDialog.getOwner() != frame) {
            if (roleDialog != null) roleDialog.dispose();
            roleDialog = new RoleManagementDialog(frame, colony);
        } else {
             roleDialog.dispose();
             roleDialog = new RoleManagementDialog(frame, colony);
        }
        
        roleDialog.showDialog(tabIndex);
    }

    private void showResearchDialog() {
        Engine engine = frame.getEngine();
        Colony colony = engine != null && engine.getWorld() != null && engine.getWorld().getSpawnHex() != null ? engine.getWorld().getSpawnHex().getColony() : null;
        if (colony == null) return;

        if (researchDialog == null || researchDialog.getOwner() != frame) {
            if (researchDialog != null) researchDialog.dispose();
            researchDialog = new ResearchDialog(frame, colony);
        }
        
        researchDialog.showDialog();
    }
    
    // --- Engine Control & Setup ---
    public boolean isEngineStarted() {
        return engineStarted;
    }

    private void handleBackButton() {
        Engine eng = frame.getEngine();
        if (eng != null) {
            eng.pauseEngine();
        }
        updateStatusIndicator(true);
        controlPanel.setPlayPauseButtonText(true);
        unregisterTickListeners(); 

        try {
            SaveManager sm = new SaveManager();
            Engine engine = frame.getEngine();
            if (engine != null && engine.getWorld() != null) {
                World w = engine.getWorld();
                int slotIdLocal = w.getSaveSlotId();
                
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
                    
                    registerTickListeners(); 
                    
                    World world = engine.getWorld();
                    Colony colony = null;
                    if (world != null && world.getSpawnHex() != null && world.getSpawnHex().getColony() != null) {
                        colony = world.getSpawnHex().getColony();
                        
                        gameAreaPanel.setColony(colony);
                        
                        TriggerManager triggerManager = new TriggerManager(world, colony, engine);
                        if (frame instanceof TriggerManager.TriggerListener) {
                            triggerManager.addListener((TriggerManager.TriggerListener) frame);
                        }
                        triggerManager.registerListeners();
                    }
                    
                    updateStaticWorldInfo();
                    
                    updateMinuteGUI();
                    updateHourGUI();
                    updateDayGUI();
                    updateMonthGUI();
                    
                    if (!engineStarted) {
                        engineStarted = true;
                        engine.start();
                    }
                    controlPanel.setPlayPauseButtonText(engine.isPaused());
                    
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

    // --- Event-Driven Update Methods ---
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
        
        if (world.getSpawnHex() != null && world.getSpawnHex().getBiome() != null) {
            String biomeName = world.getSpawnHex().getBiome().getName();
            gameAreaPanel.setBackgroundByBiome(biomeName);
        }
    }

    private void updateMinuteGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getSpawnHex() != null ? world.getSpawnHex().getColony() : null;
        if (world == null || colony == null) return;

        int w = gameAreaPanel.getWidth();
        int h = gameAreaPanel.getHeight();
        if (w > 1 && h > 1) {
            colony.setGameAreaDimensions(w, h);
        }

        worldPanel.updateMinuteData(world);
        colonyPanel.updateMinuteData(colony);
        
        gameAreaPanel.repaint(); 
    }

    private void updateHourGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getSpawnHex() != null ? world.getSpawnHex().getColony() : null;
        if (world == null || colony == null) return;

        worldPanel.updateHourData(world);
        colonyPanel.updateHourData(colony);
        
        if (controlPanel != null) {
            controlPanel.updateResearchMenu(colony.hasUpgrade(GameUpgrades.ABILITY_RESEARCH));
        }
    }

    private void updateDayGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        Colony colony = world != null && world.getSpawnHex() != null ? world.getSpawnHex().getColony() : null;
        if (world == null || colony == null) return;

        worldPanel.updateDayData(world);
        colonyPanel.updateDayData(colony);
    }

    private void updateMonthGUI() {
        Engine engine = frame.getEngine();
        World world = engine != null ? engine.getWorld() : null;
        if (world == null) return;

        worldPanel.updateMonthData(world);
    }
}