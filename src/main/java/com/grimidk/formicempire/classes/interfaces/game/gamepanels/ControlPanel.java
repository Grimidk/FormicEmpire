package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.constants.misc.GameSpeed;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.HelpPanel;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import java.util.function.BooleanSupplier;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class ControlPanel extends ZeroGamePanel {

    private final MainFrame frame;
    private final Runnable handleBackButtonCallback;
    private final Runnable showHatchRateDialogCallback;
    private final Runnable showResearchDialogCallback;
    private final Runnable showBuildDialogCallback;
    private final Runnable showAssimilationDialogCallback;
    private final RoleManagementCallback showRoleManagementDialogCallback;
    private final Runnable showAbilitiesDialogCallback;
    private final Runnable showStatsDialogCallback; 
    private final Runnable toggleViewCallback;
    private final Runnable showMapDialogCallback;
    private final Runnable showDynastyDialogCallback;
    private final Runnable showTradeDialogCallback;
    private final Runnable showDiplomacyDialogCallback;
    private final Runnable showWarDialogCallback;
    private final Runnable showSettingsDialogCallback;
    private final BooleanSupplier dynastyDialogOpenCheck;

    // --- UI Components ---
    private final JButton speedUpButton = new JButton(LanguageStrings.get(LanguageStrings.UI_SPEED_UP));
    private final JButton speedDownButton = new JButton(LanguageStrings.get(LanguageStrings.UI_SPEED_DOWN));
    private final JLabel tickLabel = new JLabel();
    private final JButton playPauseButton = new JButton();
    private final JButton menuButton = new JButton(LanguageStrings.get(LanguageStrings.UI_MENU));
    private final JPopupMenu gameMenu = new JPopupMenu();
    private JMenu colonyMenu;
    private JMenu dynastyMenu;
    
    private JMenuItem backToGame;
    private JMenuItem toggleView;
    private JMenuItem showMap;
    private JMenuItem showStats;
    private JMenuItem manageRoles;
    private JMenuItem manageHatchRates;
    private JMenuItem manageResearch;
    private JMenuItem manageBuilding;
    private JMenuItem manageAssimilation;
    private JMenuItem manageAbilities;
    private JMenuItem manageDynasty;
    private JMenuItem manageTrade;
    private JMenuItem manageDiplomacy;
    private JMenuItem manageWars;
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
                        RoleManagementCallback showRoleManagementDialogCallback,
                        Runnable showAbilitiesDialogCallback,
                        Runnable showStatsDialogCallback,
                        Runnable toggleViewCallback,
                        Runnable showMapDialogCallback,
                        Runnable showDynastyDialogCallback,
                        Runnable showTradeDialogCallback,
                        Runnable showDiplomacyDialogCallback,
                        Runnable showWarDialogCallback,
                        Runnable showSettingsDialogCallback,
                        BooleanSupplier dynastyDialogOpenCheck) {
        super(new FlowLayout(FlowLayout.RIGHT));
        
        this.frame = frame;
        this.handleBackButtonCallback = handleBackButtonCallback;
        this.showHatchRateDialogCallback = showHatchRateDialogCallback;
        this.showResearchDialogCallback = showResearchDialogCallback;
        this.showBuildDialogCallback = showBuildDialogCallback;
        this.showAssimilationDialogCallback = showAssimilationDialogCallback;
        this.showRoleManagementDialogCallback = showRoleManagementDialogCallback;
        this.showAbilitiesDialogCallback = showAbilitiesDialogCallback;
        this.showStatsDialogCallback = showStatsDialogCallback;
        this.toggleViewCallback = toggleViewCallback;
        this.showMapDialogCallback = showMapDialogCallback;
        this.showDynastyDialogCallback = showDynastyDialogCallback;
        this.showTradeDialogCallback = showTradeDialogCallback;
        this.showDiplomacyDialogCallback = showDiplomacyDialogCallback;
        this.showWarDialogCallback = showWarDialogCallback;
        this.showSettingsDialogCallback = showSettingsDialogCallback;
        this.dynastyDialogOpenCheck = dynastyDialogOpenCheck;

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

        tickLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tickLabel.setVerticalAlignment(SwingConstants.CENTER);
        tickLabel.setIconTextGap(0);
        tickLabel.setText("");
        
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
        manageAbilities = new JMenuItem();
        manageDynasty = new JMenuItem();
        manageTrade = new JMenuItem();
        manageDiplomacy = new JMenuItem();
        manageWars = new JMenuItem();
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
        manageAbilities.setText(LanguageStrings.get(LanguageStrings.MENU_ABILITIES));
        manageDynasty.setText(LanguageStrings.get(LanguageStrings.MENU_DYNASTY));
        manageTrade.setText(LanguageStrings.get(LanguageStrings.MENU_TRADE));
        manageDiplomacy.setText(LanguageStrings.get(LanguageStrings.MENU_DIPLOMACY));
        manageWars.setText(LanguageStrings.get(LanguageStrings.MENU_WARS));
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
            styleMenuComponent(component);
        }
    }

    private void styleMenuComponent(Component component) {
        if (component instanceof JMenu menu) {
            menu.setBackground(AssetStyles.BACKGROUND_COLOR);
            menu.setForeground(AssetStyles.FONT_COLOR);
            for (Component child : menu.getMenuComponents()) {
                styleMenuComponent(child);
            }
        } else if (component instanceof JMenuItem menuItem) {
            AssetStyles.styleMenuItem(menuItem);
        }
    }

    private JMenu createSubmenu(String titleKey) {
        JMenu menu = new JMenu(LanguageStrings.get(titleKey));
        menu.setBackground(AssetStyles.BACKGROUND_COLOR);
        menu.setForeground(AssetStyles.FONT_COLOR);
        return menu;
    }

    private void refreshSubmenuVisibility(JMenu submenu) {
        if (submenu == null) {
            return;
        }
        boolean anyVisible = false;
        for (Component child : submenu.getMenuComponents()) {
            if (child.isVisible()) {
                anyVisible = true;
                break;
            }
        }
        submenu.setVisible(anyVisible);
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

        setPlayPauseButtonText(eng.isPaused());
        updateTickLabel(eng);
        if (frame.getGamePanel() != null) frame.getGamePanel().updateStatusIndicator(eng.isPaused());
    }

    private void initListeners() {
        speedDownButton.addActionListener(e -> {
            Engine eng = frame.getEngine();
            if (eng == null) return;
            eng.stepSpeedDown();
            applySpeedLevel();
        });

        speedUpButton.addActionListener(e -> {
            Engine eng = frame.getEngine();
            if (eng == null) return;
            eng.stepSpeedUp();
            applySpeedLevel();
        });

        playPauseButton.addActionListener(e -> {
            Engine engine = frame.getEngine();
            if (engine == null || !frame.getGamePanel().isEngineStarted()) return;
            engine.togglePause();
            setPlayPauseButtonText(engine.isPaused());
            updateTickLabel(engine);
            if (frame.getGamePanel() != null) {
                frame.getGamePanel().updateStatusIndicator(engine.isPaused());
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

        manageAbilities.addActionListener(e -> showAbilitiesDialogCallback.run());
        manageAbilities.setVisible(false);

        manageDynasty.addActionListener(e -> showDynastyDialogCallback.run());
        manageDynasty.setVisible(false);

        manageTrade.addActionListener(e -> showTradeDialogCallback.run());
        manageTrade.setVisible(false);

        manageDiplomacy.addActionListener(e -> showDiplomacyDialogCallback.run());
        manageDiplomacy.setVisible(false);

        manageWars.addActionListener(e -> showWarDialogCallback.run());
        manageWars.setVisible(false);
        
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

        colonyMenu = createSubmenu(LanguageStrings.MENU_GROUP_COLONY);
        colonyMenu.add(manageRoles);
        colonyMenu.add(manageHatchRates);
        colonyMenu.add(manageResearch);
        colonyMenu.add(manageBuilding);
        colonyMenu.add(manageAssimilation);
        colonyMenu.add(manageAbilities);
        gameMenu.add(colonyMenu);

        dynastyMenu = createSubmenu(LanguageStrings.MENU_GROUP_DYNASTY);
        dynastyMenu.add(manageDynasty);
        dynastyMenu.add(manageTrade);
        dynastyMenu.add(manageDiplomacy);
        dynastyMenu.add(manageWars);
        gameMenu.add(dynastyMenu);

        gameMenu.add(openSettings);
        gameMenu.add(showTutorial);
        gameMenu.add(showRoadmap);
        gameMenu.add(showCredits);
        gameMenu.add(AssetStyles.createInternalSeparator());
        gameMenu.add(quitToMenu);
        gameMenu.setBackground(AssetStyles.BACKGROUND_COLOR);
        gameMenu.setForeground(AssetStyles.FONT_COLOR);
        for (Component component : gameMenu.getComponents()) {
            styleMenuComponent(component);
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
                if (frame.getGamePanel() != null && frame.getGamePanel().handleEscapeKey()) {
                    return;
                }
                Engine engine = frame.getEngine();
                if (engine != null && engine.isEscapeKeyGameActions()) {
                    toggleGameMenu();
                }
            }
        });

        addRoleKeyBinding(inputMap, actionMap, "openRoles1", KeyEvent.VK_Q, 0);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "handleWKey");
        actionMap.put("handleWKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRoleManagementDialogCallback.showDialog(1);
            }
        });
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

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "openDiplomacy");
        actionMap.put("openDiplomacy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (manageDynasty.isVisible()) {
                    showDiplomacyDialogCallback.run();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "openWars");
        actionMap.put("openWars", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((dynastyDialogOpenCheck != null && dynastyDialogOpenCheck.getAsBoolean())
                        || manageWars.isVisible()) {
                    showWarDialogCallback.run();
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

    public void toggleGameMenu() {
        if (gameMenu.isVisible()) {
            gameMenu.setVisible(false);
        } else {
            menuButton.doClick();
        }
    }

    public void updateTickLabel(Engine eng) {
        if (eng == null) {
            tickLabel.setIcon(null);
            tickLabel.setToolTipText(null);
            return;
        }
        if (eng.isPaused()) {
            tickLabel.setIcon(GameConstants.ICON_SPEED_PAUSE);
            tickLabel.setToolTipText(LanguageStrings.get(LanguageStrings.UI_PAUSED_TICK));
            return;
        }
        GameSpeed speed = eng.getSpeed();
        if (speed != null && speed.getIcon() != null) {
            tickLabel.setIcon(speed.getIcon());
            tickLabel.setToolTipText(speed.getName());
        } else {
            tickLabel.setIcon(null);
            tickLabel.setToolTipText(eng.getSpeedLabel());
        }
    }
    
    public void setPlayPauseButtonText(boolean isPaused) {
        playPauseButton.setText(isPaused ? LanguageStrings.get(LanguageStrings.UI_PLAY) : LanguageStrings.get(LanguageStrings.UI_PAUSE));
    }

    public void updateResearchMenu(boolean visible) {
        if (manageResearch != null) {
            manageResearch.setVisible(visible);
        }
        refreshSubmenuVisibility(colonyMenu);
    }
    
    public void updateBuildMenu(boolean visible) {
        if (manageBuilding != null) {
            manageBuilding.setVisible(visible);
        }
        refreshSubmenuVisibility(colonyMenu);
    }

    public void updateAssimilationMenu(boolean visible) {
        if (manageAssimilation != null) {
            manageAssimilation.setVisible(visible);
        }
        refreshSubmenuVisibility(colonyMenu);
    }
    
    public void updateAbilitiesMenu(boolean visible) {
        if (manageAbilities != null) {
            manageAbilities.setVisible(visible);
        }
        refreshSubmenuVisibility(colonyMenu);
    }

    public void updateDynastyMenu(boolean visible) {
        if (manageDynasty != null) {
            manageDynasty.setVisible(visible);
        }
        if (manageDiplomacy != null) {
            manageDiplomacy.setVisible(visible);
        }
        refreshSubmenuVisibility(dynastyMenu);
    }

    public void updateTradeMenu(boolean visible) {
        if (manageTrade != null) {
            manageTrade.setVisible(visible);
        }
        refreshSubmenuVisibility(dynastyMenu);
    }

    public void updateWarsMenu(boolean visible) {
        if (manageWars != null) {
            manageWars.setVisible(visible);
        }
        refreshSubmenuVisibility(dynastyMenu);
    }
}
