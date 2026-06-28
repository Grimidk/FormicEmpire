package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.HelpPanel;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.constants.misc.GameSpeed;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class ControlPanel extends ZeroGamePanel {

    private final MainFrame frame;
    private final Runnable handleBackButtonCallback;
    private final Runnable showHatchRateDialogCallback;
    private final Runnable showResearchDialogCallback;
    private final Runnable showBuildDialogCallback;
    private final Runnable showAssimilationDialogCallback;
    private final Runnable showSynergyDialogCallback;
    private final RoleManagementCallback showRoleManagementDialogCallback;
    private final Runnable showAbilitiesDialogCallback;
    private final Runnable showStatsDialogCallback; 
    private final Runnable toggleViewCallback;
    private final Runnable showMapDialogCallback;
    private final Runnable showDynastyDialogCallback;
    private final Runnable showTradeDialogCallback;
    private final Runnable showSettingsDialogCallback;

    // --- UI Components ---
    private final JButton speedUpButton = new JButton(LanguageStrings.get(LanguageStrings.UI_SPEED_UP));
    private final JButton speedDownButton = new JButton(LanguageStrings.get(LanguageStrings.UI_SPEED_DOWN));
    private final JLabel tickLabel = new JLabel();
    private final JButton playPauseButton = new JButton();
    private final JButton menuButton = new JButton(LanguageStrings.get(LanguageStrings.UI_MENU));
    private final JPopupMenu gameMenu = new JPopupMenu();
    
    private JMenuItem backToGame;
    private JMenuItem toggleView;
    private JMenuItem showMap;
    private JMenuItem showStats;
    private JMenuItem manageRoles;
    private JMenuItem manageHatchRates;
    private JMenuItem manageResearch;
    private JMenuItem manageBuilding;
    private JMenuItem manageAssimilation;
    private JMenuItem manageSynergy;
    private JMenuItem manageAbilities;
    private JMenuItem manageDynasty;
    private JMenuItem manageTrade;
    private JMenuItem openSettings;
    private JMenuItem showTutorial;
    private JMenuItem showRoadmap;
    private JMenuItem showCredits;
    private JMenuItem quitToMenu;
    
    public interface RoleManagementCallback {
        void showDialog(int tabIndex);
    }

    public ControlPanel(MainFrame frame, 
                        Runnable handleBackButtonCallback, 
                        Runnable showHatchRateDialogCallback, 
                        Runnable showResearchDialogCallback, 
                        Runnable showBuildDialogCallback,
                        Runnable showAssimilationDialogCallback,
                        Runnable showSynergyDialogCallback,
                        RoleManagementCallback showRoleManagementDialogCallback,
                        Runnable showAbilitiesDialogCallback,
                        Runnable showStatsDialogCallback,
                        Runnable toggleViewCallback,
                        Runnable showMapDialogCallback,
                        Runnable showDynastyDialogCallback,
                        Runnable showTradeDialogCallback,
                        Runnable showSettingsDialogCallback) {
        super(new FlowLayout(FlowLayout.RIGHT));
        
        this.frame = frame;
        this.handleBackButtonCallback = handleBackButtonCallback;
        this.showHatchRateDialogCallback = showHatchRateDialogCallback;
        this.showResearchDialogCallback = showResearchDialogCallback;
        this.showBuildDialogCallback = showBuildDialogCallback;
        this.showAssimilationDialogCallback = showAssimilationDialogCallback;
        this.showSynergyDialogCallback = showSynergyDialogCallback;
        this.showRoleManagementDialogCallback = showRoleManagementDialogCallback;
        this.showAbilitiesDialogCallback = showAbilitiesDialogCallback;
        this.showStatsDialogCallback = showStatsDialogCallback;
        this.toggleViewCallback = toggleViewCallback;
        this.showMapDialogCallback = showMapDialogCallback;
        this.showDynastyDialogCallback = showDynastyDialogCallback;
        this.showTradeDialogCallback = showTradeDialogCallback;
        this.showSettingsDialogCallback = showSettingsDialogCallback;

        initComponents();
        initLayout();        
        initListeners();
        initKeyBindings();
        updateTickLabel(frame.getEngine());
        updatePlayPauseButton();
    }

    @Override
    protected void initComponents() {
        speedDownButton.setFocusable(false);
        speedUpButton.setFocusable(false);
        playPauseButton.setFocusable(false);
        menuButton.setFocusable(false);
        AssetStyles.styleButton(speedUpButton);
        AssetStyles.styleButton(speedDownButton);
        AssetStyles.styleButton(playPauseButton);
        AssetStyles.styleButton(menuButton);
        
        // Init menu items
        backToGame = new JMenuItem();
        toggleView = new JMenuItem();
        showMap = new JMenuItem(); 
        showStats = new JMenuItem();
        manageRoles = new JMenuItem();
        manageHatchRates = new JMenuItem();
        manageResearch = new JMenuItem();
        manageBuilding = new JMenuItem();
        manageAssimilation = new JMenuItem();
        manageSynergy = new JMenuItem();
        manageAbilities = new JMenuItem();
        manageDynasty = new JMenuItem();
        manageTrade = new JMenuItem();
        openSettings = new JMenuItem();
        showTutorial = new JMenuItem();
        showRoadmap = new JMenuItem();
        showCredits = new JMenuItem();
        quitToMenu = new JMenuItem();
        
        refreshTranslations();
    }

    @Override
    protected void initLayout() {
        add(speedDownButton);
        add(speedUpButton);
        add(tickLabel);
        add(playPauseButton);
        add(menuButton);
    }
    
    @Override
    public void refreshTranslations() {
        speedUpButton.setText(LanguageStrings.get(LanguageStrings.UI_SPEED_UP));
        speedDownButton.setText(LanguageStrings.get(LanguageStrings.UI_SPEED_DOWN));
        menuButton.setText(LanguageStrings.get(LanguageStrings.UI_MENU));
        updateTickLabel(frame.getEngine());
        updatePlayPauseButton();
        
        backToGame.setText(LanguageStrings.get(LanguageStrings.UI_BACK_TO_GAME));
        toggleView.setText(LanguageStrings.get(LanguageStrings.MENU_TOGGLE_VIEW));
        showMap.setText(LanguageStrings.get(LanguageStrings.MENU_WORLD_MAP));
        showStats.setText(LanguageStrings.get(LanguageStrings.MENU_STATS));
        manageRoles.setText(LanguageStrings.get(LanguageStrings.MENU_ROLES));
        manageHatchRates.setText(LanguageStrings.get(LanguageStrings.MENU_HATCH_RATES));
        manageResearch.setText(LanguageStrings.get(LanguageStrings.MENU_RESEARCH));
        manageBuilding.setText(LanguageStrings.get(LanguageStrings.MENU_BUILD));
        manageAssimilation.setText(LanguageStrings.get(LanguageStrings.MENU_ASSIMILATION));
        manageSynergy.setText(LanguageStrings.get(LanguageStrings.MENU_SYNERGY));
        manageAbilities.setText(LanguageStrings.get(LanguageStrings.MENU_ABILITIES));
        manageDynasty.setText(LanguageStrings.get(LanguageStrings.MENU_DYNASTY));
        manageTrade.setText(LanguageStrings.get(LanguageStrings.MENU_TRADE));
        openSettings.setText(LanguageStrings.get(LanguageStrings.UI_SETTINGS));
        showTutorial.setText(LanguageStrings.get(LanguageStrings.UI_TUTORIAL));
        showRoadmap.setText(LanguageStrings.get(LanguageStrings.UI_ROADMAP));
        showCredits.setText(LanguageStrings.get(LanguageStrings.UI_CREDITS));
        quitToMenu.setText(LanguageStrings.get(LanguageStrings.UI_BACK_TO_MENU));
    }

    @Override
    public void refreshTheme() {
        super.refreshTheme();
        gameMenu.setBackground(AssetStyles.BACKGROUND_COLOR);
        gameMenu.setForeground(AssetStyles.FONT_COLOR);
        for (Component component : gameMenu.getComponents()) {
            if (component instanceof JMenuItem menuItem) {
                AssetStyles.styleMenuItem(menuItem);
            }
        }
    }
    
    private void updatePlayPauseButton() {
        Engine engine = frame.getEngine();
        if (engine != null) {
            setPlayPauseButtonText(engine.isPaused());
        }
    }

    private void applySpeedLevel() {
        Engine eng = frame.getEngine();
        if (eng == null) return;

        if (eng.isPaused()) {
            eng.resumeEngine();
        }
        setPlayPauseButtonText(false);
        updateTickLabel(eng);
        if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(false);
    }

    private void initListeners() {
        speedDownButton.addActionListener(e -> {
            Engine eng = frame.getEngine();
            if (eng == null) return;
            
            GameSpeed current = eng.getSpeed();
            if (current != GameSpeed.VERY_SLOW) {
                eng.setSpeed(GameSpeed.getPrevious(current));
                applySpeedLevel();
            }
        });

        speedUpButton.addActionListener(e -> {
            Engine eng = frame.getEngine();
            if (eng == null) return;
            
            GameSpeed current = eng.getSpeed();
            GameSpeed next = GameSpeed.getNext(current);
            
            boolean canGoTurbo = eng.isAllowTurboMode();
            if (current == GameSpeed.VERY_FAST && !canGoTurbo) {
            } else if (current == GameSpeed.TURBO) {
            } else {
                eng.setSpeed(next);
                applySpeedLevel();
            }
        });

        playPauseButton.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine == null || !frame.getGamePanel().isEngineStarted()) return;
            if (engine.isPaused()) {
                engine.resumeEngine();
                setPlayPauseButtonText(false);
                if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(false);
            } else {
                engine.pauseEngine();
                setPlayPauseButtonText(true);
                if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(true);
            }
        });

        backToGame.addActionListener(e -> gameMenu.setVisible(false));
        toggleView.addActionListener(e -> toggleViewCallback.run());
        showMap.addActionListener(e -> showMapDialogCallback.run()); 
        showStats.addActionListener(e -> showStatsDialogCallback.run());
        manageRoles.addActionListener(e -> showRoleManagementDialogCallback.showDialog(0));
        manageHatchRates.addActionListener(e -> showHatchRateDialogCallback.run());
        
        manageResearch.addActionListener(e -> showResearchDialogCallback.run());
        manageResearch.setVisible(false); 
        
        manageBuilding.addActionListener(e -> showBuildDialogCallback.run());
        manageBuilding.setVisible(false);

        manageAssimilation.addActionListener(e -> showAssimilationDialogCallback.run());
        manageAssimilation.setVisible(false);

        manageSynergy.addActionListener(e -> showSynergyDialogCallback.run());
        manageSynergy.setVisible(false);
        
        manageAbilities.addActionListener(e -> showAbilitiesDialogCallback.run());
        manageAbilities.setVisible(false);

        manageDynasty.addActionListener(e -> showDynastyDialogCallback.run());
        manageDynasty.setVisible(false);

        manageTrade.addActionListener(e -> showTradeDialogCallback.run());
        manageTrade.setVisible(false);
        
        openSettings.addActionListener(e -> {
            showSettingsDialogCallback.run();
        });
        
        showTutorial.addActionListener(e -> HelpPanel.showTutorialDialog(frame));
        showRoadmap.addActionListener(e -> HelpPanel.showRoadmapDialog(frame));
        showCredits.addActionListener(e -> HelpPanel.showCreditsDialog(frame));
        
        quitToMenu.addActionListener(e -> handleBackButtonCallback.run());
        
        gameMenu.add(backToGame);
        gameMenu.add(toggleView);
        gameMenu.add(showMap);
        gameMenu.add(showStats);
        gameMenu.add(manageRoles);
        gameMenu.add(manageHatchRates);
        gameMenu.add(manageResearch);
        gameMenu.add(manageBuilding);
        gameMenu.add(manageAssimilation);
        gameMenu.add(manageSynergy);
        gameMenu.add(manageAbilities);
        gameMenu.add(manageDynasty);
        gameMenu.add(manageTrade);
        gameMenu.add(openSettings);
        gameMenu.add(showTutorial);
        gameMenu.add(showRoadmap);
        gameMenu.add(showCredits);
        gameMenu.add(AssetStyles.createInternalSeparator());
        gameMenu.add(quitToMenu);
        gameMenu.setBackground(AssetStyles.BACKGROUND_COLOR);
        gameMenu.setForeground(AssetStyles.FONT_COLOR);
        for (Component component : gameMenu.getComponents()) {
            if (component instanceof JMenuItem menuItem) {
                AssetStyles.styleMenuItem(menuItem);
            }
        }

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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "toggleView");
        actionMap.put("toggleView", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleViewCallback.run();
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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "openAssimilation");
        actionMap.put("openAssimilation", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageAssimilation.isVisible()) {
                    showAssimilationDialogCallback.run();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "openSynergy");
        actionMap.put("openSynergy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageSynergy.isVisible()) {
                    showSynergyDialogCallback.run();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "openMap");
        actionMap.put("openMap", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMapDialogCallback.run();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "openStats");
        actionMap.put("openStats", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStatsDialogCallback.run();
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "openAbilities");
        actionMap.put("openAbilities", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageAbilities.isVisible()) {
                    showAbilitiesDialogCallback.run();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "openDynasty");
        actionMap.put("openDynasty", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageDynasty.isVisible()) {
                    showDynastyDialogCallback.run();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "openTrade");
        actionMap.put("openTrade", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageTrade.isVisible()) {
                    showTradeDialogCallback.run();
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
            tickLabel.setText("-");
            return;
        }
        tickLabel.setText(eng.getSpeed().getLabel());
    }
    
    public void setPlayPauseButtonText(boolean isPaused) {
        playPauseButton.setText(isPaused ? LanguageStrings.get(LanguageStrings.UI_PLAY) : LanguageStrings.get(LanguageStrings.UI_PAUSE));
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

    public void updateAssimilationMenu(boolean visible) {
        if (manageAssimilation != null) {
            manageAssimilation.setVisible(visible);
        }
    }

    public void updateSynergyMenu(boolean visible) {
        if (manageSynergy != null) {
            manageSynergy.setVisible(visible);
        }
    }
    
    public void updateAbilitiesMenu(boolean visible) {
        if (manageAbilities != null) {
            manageAbilities.setVisible(visible);
        }
    }

    public void updateDynastyMenu(boolean visible) {
        if (manageDynasty != null) {
            manageDynasty.setVisible(visible);
        }
    }

    public void updateTradeMenu(boolean visible) {
        if (manageTrade != null) {
            manageTrade.setVisible(visible);
        }
    }
}
