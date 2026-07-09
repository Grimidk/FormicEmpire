package com.grimidk.formicempire.classes.interfaces;

import javax.swing.*;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
public class MainFrame extends JFrame implements TriggerManager.TriggerListener {
    public static final String CARD_INTRO = "INTRO";
    public static final String CARD_INIT = "INIT";
    public static final String CARD_SAVE = "SAVE";
    public static final String CARD_HELP = "HELP";
    public static final String CARD_SETTINGS = "SETTINGS";
    public static final String CARD_GAME = "GAME";

    private final CardLayout cardLayout;
    private final JPanel cards;
    private final Engine engine;
    private final GamePanel gamePanel;
    private final SaveSelectPanel saveSelectPanel;
    private final SettingsPanel settingsPanel;
    private final IntroPanel introPanel;
    private final InitPanel initPanel;
    private final HelpPanel helpPanel;
    private String menuReturnCard = CARD_INIT;

    private Cursor cursorNormal;
    private Cursor cursorClick;
    private AWTEventListener cursorEventListener;
    private boolean cursorPressed;
    private boolean pausedForFocusLoss;
    private final Runnable translationRefresh = this::refreshTranslations;

    private boolean isGameWindow(Window window) {
        if (window == null) {
            return false;
        }
        if (window == this) {
            return true;
        }
        return window instanceof Dialog dialog && dialog.getOwner() == this;
    }

    public void applyGameCursors(Window window) {
        if (window == null || cursorNormal == null) {
            return;
        }
        cursorPressed = false;
        window.setCursor(cursorNormal);
        clearInheritedCursors(window);
    }

    private void clearInheritedCursors(Container container) {
        for (Component child : container.getComponents()) {
            Cursor childCursor = child.getCursor();
            if (childCursor != null && childCursor.getType() == Cursor.HAND_CURSOR) {
                continue;
            }
            child.setCursor(null);
            if (child instanceof Container nested) {
                clearInheritedCursors(nested);
            }
        }
    }

    private void applyGameCursorForMouseEvent(MouseEvent me) {
        if (cursorNormal == null || cursorClick == null) {
            return;
        }
        Component source = me.getComponent();
        if (source == null) {
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(source);
        if (!isGameWindow(window)) {
            return;
        }
        if (me.getID() == MouseEvent.MOUSE_PRESSED) {
            cursorPressed = true;
            window.setCursor(cursorClick);
        } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
            cursorPressed = false;
            window.setCursor(cursorNormal);
        } else if (me.getID() == MouseEvent.MOUSE_DRAGGED && !cursorPressed) {
            window.setCursor(cursorNormal);
        }
    }

    public void clearFocusPauseState() {
        pausedForFocusLoss = false;
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public JPanel getCards() {
        return cards;
    }

    public GamePanel getGamePanel() {
        return gamePanel;
    }

    public Cursor getGameCursorNormal() {
        return cursorNormal;
    }

    public Cursor getGameCursorClick() {
        return cursorClick;
    }

    public SaveSelectPanel getSaveSelectPanel() {
        return saveSelectPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    public MainFrame(Engine engine) {
        super(LanguageStrings.APP_DISPLAY_NAME);
        this.engine = engine;
        
        Image icon = AssetStyles.loadImage(AssetStyles.META_APP_ICON);
        if (icon != null) {
            setIconImage(icon);
        }
        
        this.cardLayout = new CardLayout();
        this.cards = new JPanel(cardLayout);

        initCursors();

        this.introPanel = new IntroPanel(this);
        this.initPanel = new InitPanel(this);
        this.saveSelectPanel = new SaveSelectPanel(this);
        this.helpPanel = new HelpPanel(this);
        this.settingsPanel = new SettingsPanel(this);
        this.gamePanel = new GamePanel(this);

        cards.add(introPanel, CARD_INTRO);
        cards.add(initPanel, CARD_INIT);
        cards.add(saveSelectPanel, CARD_SAVE);
        cards.add(helpPanel, CARD_HELP);
        cards.add(settingsPanel, CARD_SETTINGS);
        cards.add(gamePanel, CARD_GAME);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cards, BorderLayout.CENTER);

        // --- Window Listeners ---
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });
        
        addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (!engine.isPauseOnFocusLoss() || !gamePanel.isEngineStarted() || !pausedForFocusLoss) {
                    return;
                }
                pausedForFocusLoss = false;
                if (engine.isPaused()) {
                    engine.resumeEngine();
                    gamePanel.updateStatusIndicator(false);
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (!engine.isPauseOnFocusLoss() || !gamePanel.isEngineStarted() || engine.isPaused()) {
                    return;
                }
                engine.pauseEngine();
                pausedForFocusLoss = true;
                gamePanel.updateStatusIndicator(true);
            }
        });

        applyEngineSettings();
        
        LanguageStrings.addListener(translationRefresh);
    }

    public void applyTheme() {
        AssetStyles.applyTheme(engine.isDarkMode());
        AssetStyles.applyGlobalStyles();
        SwingUtilities.updateComponentTreeUI(this);
        getContentPane().setBackground(AssetStyles.BACKGROUND_COLOR);
        cards.setBackground(AssetStyles.BACKGROUND_COLOR);
        AssetStyles.applyThemeToContainer(cards);
        applyGameCursors(this);
        settingsPanel.refreshTheme();
        helpPanel.refreshTheme();
        saveSelectPanel.refreshTheme();
        initPanel.refreshTheme();
        gamePanel.refreshTheme();
    }

    public void requestExit() {
        handleExit();
    }
    
    private void handleExit() {
        if (gamePanel.isEngineStarted() && engine.isConfirmOnQuit()) {
            int res = UiOptionPane.showConfirmDialog(this, 
                LanguageStrings.get(LanguageStrings.UI_CONFIRM_EXIT_MSG), 
                LanguageStrings.get(LanguageStrings.UI_CONFIRM_EXIT_TITLE), 
                JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                shutdownAndExit();
            }
        } else {
            shutdownAndExit();
        }
    }

    private void shutdownAndExit() {
        gamePanel.endSession();
        LanguageStrings.removeListener(translationRefresh);
        engine.pauseEngine();
        SaveManager.shutdownSharedExecutor();
        if (cursorEventListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(cursorEventListener);
            cursorEventListener = null;
        }
        System.exit(0);
    }
    
    private void refreshTranslations() {
        setTitle(LanguageStrings.APP_DISPLAY_NAME);
        saveSelectPanel.refreshTranslations();
        introPanel.refreshTranslations();
        initPanel.refreshTranslations();
        helpPanel.refreshTranslations();
        settingsPanel.refreshTranslations();
        gamePanel.refreshTranslations();
    }

    public String getMenuReturnCard() {
        return menuReturnCard;
    }

    public void showSettingsMenu(String returnTo) {
        menuReturnCard = returnTo;
        showCard(CARD_SETTINGS);
    }

    private void initCursors() {
        cursorNormal = AssetStyles.loadCustomCursor(AssetStyles.META_CURSOR_NORMAL, "AntCursorNormal");
        cursorClick = AssetStyles.loadCustomCursor(AssetStyles.META_CURSOR_CLICK, "AntCursorClick");
        
        setCursor(cursorNormal);

        Toolkit.getDefaultToolkit().addAWTEventListener(cursorEventListener = event -> {
            if (event instanceof MouseEvent me) {
                int id = me.getID();
                if (id == MouseEvent.MOUSE_PRESSED || id == MouseEvent.MOUSE_RELEASED || id == MouseEvent.MOUSE_DRAGGED) {
                    applyGameCursorForMouseEvent(me);
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    public Engine getEngine() {
        return engine;
    }

    public void applyRuntimeSettings() {
        ToolTipManager.sharedInstance().setEnabled(engine.isShowTooltips());
        initPanel.refreshMenuOptions();
        if (gamePanel != null) {
            gamePanel.refreshAuditMenuOption();
        }
        applyTheme();
    }

    public void applyEngineSettings() {
        reapplyWindowChrome();
        applyRuntimeSettings();
    }

    private void reapplyWindowChrome() {
        Runnable apply = () -> {
            boolean wantFullscreen = engine.isFullScreen();

            if (wantFullscreen) {
                setVisible(false);
                dispose();
                setUndecorated(true);
                setExtendedState(JFrame.NORMAL);
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setSize(screenSize);
                setVisible(true);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                if (isUndecorated()) {
                    setVisible(false);
                    dispose();
                    setUndecorated(false);
                }
                setExtendedState(JFrame.NORMAL);
                applyWindowedSizeFromEngine();
                setLocationRelativeTo(null);
                setVisible(true);
            }

            validate();
            repaint();
            applyGameCursors(this);
            requestFocus();
            if (gamePanel != null) {
                gamePanel.onWindowGeometryChanged();
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            apply.run();
        } else {
            SwingUtilities.invokeLater(apply);
        }
    }

    private void applyWindowedSizeFromEngine() {
        String screenSize = engine.getScreenSize();
        if (screenSize == null || !screenSize.contains("x")) {
            setSize(1000, 700);
            return;
        }
        String[] parts = screenSize.split("x");
        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            setSize(Math.max(width, 640), Math.max(height, 480));
        } catch (NumberFormatException e) {
            setSize(1000, 700);
        }
    }

    public void showCard(String card) {
        if (CARD_SAVE.equals(card)) {
            try {
                saveSelectPanel.refreshSlots();
            } catch (Exception ignore) {}
        }
        if (CARD_SETTINGS.equals(card)) {
            try {
                settingsPanel.loadSettings();
            } catch (Exception ignore) {}
        }
        cardLayout.show(cards, card);
        applyGameCursors(this);
    }

    public void openGameWithSave(Savefile savefile) {
        showCard(CARD_GAME);
        gamePanel.enterWithSavefile(savefile);
        applyRuntimeSettings();
    }
    
    @Override
    public void onUpgradeTriggered(Upgrade unlockedUpgrade, String title, String message) {
        boolean wasPaused = engine.isPaused();
        if (!wasPaused) {
            engine.pauseEngine();
            if (gamePanel != null) {
                gamePanel.updateStatusIndicator(true);
            }
        }

        UiOptionPane.showForegroundMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);

        if (!wasPaused) {
            engine.resumeEngine();
            if (gamePanel != null) {
                gamePanel.updateStatusIndicator(false);
            }
        }

        if (gamePanel != null) {
            gamePanel.refreshAllGUIData();
        }
    }
    
    @Override
    public void onColonyDeath() {
        engine.pauseEngine();
        if (gamePanel != null) gamePanel.updateStatusIndicator(true);

        String[] options = {LanguageStrings.get(LanguageStrings.DEATH_OPTIONS_RELOAD), LanguageStrings.get(LanguageStrings.DEATH_OPTIONS_MENU)};
        String title = LanguageStrings.get(LanguageStrings.DEATH_TITLE);
        String message = LanguageStrings.get(LanguageStrings.DEATH_MESSAGE);

        int choice = UiOptionPane.showForegroundOptionDialog(
                this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) { 
            int slotId = (engine.getWorld() != null) ? engine.getWorld().getSaveSlotId() : 0;
            
            if (slotId == 0) {
                UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_NEW_GAME), LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_TITLE), JOptionPane.ERROR_MESSAGE);
                handleQuitToMenu();
            } else {
                SaveManager sm = engine.getSaveManager();
                Savefile saveToLoad = sm.loadAutosaveForSlot(slotId);
                
                if (saveToLoad != null) {
                    openGameWithSave(saveToLoad);
                } else {
                    UiOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_NO_AUTOSAVE), LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_TITLE), JOptionPane.ERROR_MESSAGE);
                    handleQuitToMenu();
                }
            }
        } else {
            handleQuitToMenu();
        }
    }
    
    private void handleQuitToMenu() {
        if (gamePanel != null) {
            gamePanel.quitToMenuWithoutSaving();
        } else {
            showCard(CARD_SAVE);
        }
    }
}
