package com.grimidk.formicempire.classes.interfaces.game.panels;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.interfaces.HelpPanel;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ControlPanel extends JPanel {

    private final MainFrame frame;
    private final Runnable handleBackButtonCallback;
    private final Runnable showHatchRateDialogCallback;
    private final Runnable showResearchDialogCallback;
    private final Runnable showBuildDialogCallback;
    private final RoleManagementCallback showRoleManagementDialogCallback;

    // --- UI Components ---
    private final JButton speedUpButton = new JButton("Speed+");
    private final JButton speedDownButton = new JButton("Speed-");
    private final JLabel tickLabel = new JLabel("Tick: 250ms");
    private final JButton playPauseButton = new JButton("Pause");
    private final JButton menuButton = new JButton("Menu");
    private final JPopupMenu gameMenu = new JPopupMenu();
    private JMenuItem manageResearch;
    private JMenuItem manageBuilding;
    
    // --- State ---
    private int speedLevel = 1;
    
    private static final float[] SPEED_DELAYS = { 250f, 125f, 60f, 30f, 15f, 5f, 1f}; 
    
    public interface RoleManagementCallback {
        void showDialog(int tabIndex);
    }

    public ControlPanel(MainFrame frame, 
                        Runnable handleBackButtonCallback, 
                        Runnable showHatchRateDialogCallback, 
                        Runnable showResearchDialogCallback, 
                        Runnable showBuildDialogCallback,
                        RoleManagementCallback showRoleManagementDialogCallback) {
        this.frame = frame;
        this.handleBackButtonCallback = handleBackButtonCallback;
        this.showHatchRateDialogCallback = showHatchRateDialogCallback;
        this.showResearchDialogCallback = showResearchDialogCallback;
        this.showBuildDialogCallback = showBuildDialogCallback;
        this.showRoleManagementDialogCallback = showRoleManagementDialogCallback;

        initLayout();
        initListeners();
        initKeyBindings();
        updateTickLabel(frame.getEngine());
    }

    private void initLayout() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        speedDownButton.setFocusable(false);
        speedUpButton.setFocusable(false);
        playPauseButton.setFocusable(false);
        menuButton.setFocusable(false);
        
        add(speedDownButton);
        add(speedUpButton);
        add(tickLabel);
        add(playPauseButton);
        add(menuButton);
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

    private void applySpeedLevel() {
        Engine eng = frame.getEngine();
        if (eng == null) return;

        int maxLevel = getMaxSpeedLevel();
        
        if (speedLevel > maxLevel) speedLevel = maxLevel;
        if (speedLevel < 0) speedLevel = 0;

        float delay = SPEED_DELAYS[speedLevel];

        if (delay == -1f) {
            eng.pauseEngine();
            playPauseButton.setText("Play");
            tickLabel.setText("Tick: PAUSED");
            if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(true);
        } else {
            eng.setDelay(delay);
            if (eng.isPaused()) {
                eng.resumeEngine();
            }
            playPauseButton.setText("Pause");
            updateTickLabel(eng);
            if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(false);
        }
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
            if (engine == null || !frame.getGamePanel().isEngineStarted()) return;
            if (engine.isPaused()) {
                engine.resumeEngine();
                playPauseButton.setText("Pause");
                if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(false);
            } else {
                engine.pauseEngine();
                playPauseButton.setText("Play");
                if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(true);
            }
        });

        // --- Game Menu Setup ---
        JMenuItem backToGame = new JMenuItem("Back to Game");
        JMenuItem manageRoles = new JMenuItem("Manage Roles (Q/W/E/R/T)");
        JMenuItem manageHatchRates = new JMenuItem("Manage Hatch Rates (P)");
        manageResearch = new JMenuItem("Research (Y)");
        manageBuilding = new JMenuItem("Build (U)");
        JMenuItem openSettings = new JMenuItem("Settings");
        JMenuItem showTutorial = new JMenuItem("Show Tutorial");
        JMenuItem quitToMenu = new JMenuItem("Quit to Main Menu");

        backToGame.addActionListener(e -> gameMenu.setVisible(false));
        manageRoles.addActionListener(e -> showRoleManagementDialogCallback.showDialog(0));
        manageHatchRates.addActionListener(e -> showHatchRateDialogCallback.run());
        
        manageResearch.addActionListener(e -> showResearchDialogCallback.run());
        manageResearch.setVisible(false); 
        
        manageBuilding.addActionListener(e -> showBuildDialogCallback.run());
        manageBuilding.setVisible(false);
        
        openSettings.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine != null) {
                engine.pauseEngine();
            }
            if (frame.getGamePanel() != null) {
                frame.getGamePanel().updateStatusIndicator(true);
                playPauseButton.setText("Play");
            }

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
        
        quitToMenu.addActionListener(e -> handleBackButtonCallback.run());
        
        gameMenu.add(backToGame);
        gameMenu.add(manageRoles);
        gameMenu.add(manageHatchRates);
        gameMenu.add(manageResearch);
        gameMenu.add(manageBuilding);
        gameMenu.add(openSettings);
        gameMenu.add(showTutorial);
        gameMenu.add(new JSeparator());
        gameMenu.add(quitToMenu);

        menuButton.addActionListener(e -> {
            gameMenu.show(menuButton, 0, -gameMenu.getPreferredSize().height);
        });
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
                showHatchRateDialogCallback.run();
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0), "openResearch");
        actionMap.put("openResearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageResearch.isVisible()) {
                    showResearchDialogCallback.run();
                }
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "openBuilding");
        actionMap.put("openBuilding", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageBuilding.isVisible()) {
                    showBuildDialogCallback.run();
                }
            }
        });
    }
    
    private void addRoleKeyBinding(InputMap im, ActionMap am, String name, int key, int tab) {
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialogCallback.showDialog(tab);
            }
        });
    }

    public void updateTickLabel(Engine eng) {
        if (eng == null) {
            tickLabel.setText("Tick: -");
            return;
        }
        tickLabel.setText("Tick: " + (long) eng.getDelay() + "ms");
    }
    
    public void setPlayPauseButtonText(boolean isPaused) {
        playPauseButton.setText(isPaused ? "Play" : "Pause");
    }

    public void updateResearchMenu(boolean visible) {
        if (manageResearch != null) {
            manageResearch.setVisible(visible);
        }
    }
    
    public void updateBuildMenu(boolean visible) {
        if (manageBuilding != null) {
            manageBuilding.setVisible(visible);
        }
    }
}