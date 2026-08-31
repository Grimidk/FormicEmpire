package com.grimidk.formicempire.classes.interfaces;

import javax.swing.*;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;
import com.grimidk.formicempire.classes.infrasctructure.diagnostics.FrameRateTracker;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.FpsOverlayPanel;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiCursors;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.interfaces.menu.MenuChaoticPanel;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.util.MacOsNativeFullscreen;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
    private final MenuChaoticPanel menuChaoticPanel;
    private String menuReturnCard = CARD_INIT;

    private Cursor cursorNormal;
    private Cursor cursorClick;
    private AWTEventListener cursorEventListener;
    private boolean cursorPressed;
    private Component pressedCursorComponent;
    private Cursor pressedCursorPrevious;
    private boolean pausedForFocusLoss;
    private final Runnable translationRefresh = this::refreshTranslations;
    private boolean macNativeFullscreenActive;
    private boolean macFullscreenTransition;
    private boolean macFullscreenSupportInstalled;
    private boolean macFullscreenEnsurePending;
    private int macFullscreenRetryCount;
    private Timer macFullscreenVerifyTimer;
    private final FrameRateTracker frameRateTracker = new FrameRateTracker();
    private FpsOverlayPanel fpsOverlayPanel;

    private static final int MAC_FULLSCREEN_MAX_RETRIES = 6;
    private static final int MAC_FULLSCREEN_VERIFY_MS = 1600;

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
            if (UiCursors.isHoverCursor(childCursor)) {
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
            pressedCursorComponent = source;
            pressedCursorPrevious = source.getCursor();
            source.setCursor(cursorClick);
            window.setCursor(cursorClick);
        } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
            cursorPressed = false;
            if (pressedCursorComponent != null) {
                pressedCursorComponent.setCursor(pressedCursorPrevious);
                pressedCursorComponent = null;
                pressedCursorPrevious = null;
            }
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

    public Cursor getGameCursorClickable() {
        return AssetStyles.cursorClickable();
    }

    public Cursor getGameCursorWriteable() {
        return AssetStyles.cursorWriteable();
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
        cards.setOpaque(false);

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

        menuChaoticPanel = new MenuChaoticPanel();
        fpsOverlayPanel = new FpsOverlayPanel(frameRateTracker);
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.add(menuChaoticPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(cards, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(fpsOverlayPanel, JLayeredPane.DRAG_LAYER);
        Runnable visualFrameListener = () -> {
            if (engine.isShowFpsCounter()) {
                fpsOverlayPanel.onFramePainted();
            }
        };
        menuChaoticPanel.setFramePaintListener(visualFrameListener);
        gamePanel.setFramePaintListener(visualFrameListener);
        Runnable syncLayerBounds = () -> {
            Dimension size = layeredPane.getSize();
            int w = Math.max(0, size.width);
            int h = Math.max(0, size.height);
            menuChaoticPanel.setBounds(0, 0, w, h);
            cards.setBounds(0, 0, w, h);
            fpsOverlayPanel.setBounds(0, 0, w, h);
        };
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                syncLayerBounds.run();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                syncLayerBounds.run();
            }
        });
        SwingUtilities.invokeLater(syncLayerBounds);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(layeredPane, BorderLayout.CENTER);

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
                maybeRetryMacFullscreenAfterFocus();
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
        menuChaoticPanel.setActive(false);
        LanguageStrings.removeListener(translationRefresh);
        if (engine.getMusicService() != null) {
            engine.getMusicService().shutdown();
        }
        if (engine.getSfxService() != null) {
            engine.getSfxService().shutdown();
        }
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
        UiOptionPane.applyLocalizedButtonTexts();
        saveSelectPanel.refreshTranslations();
        introPanel.refreshTranslations();
        initPanel.refreshTranslations();
        settingsPanel.refreshTranslations();
        gamePanel.refreshTranslations();
        helpPanel.refreshTranslations();
    }

    public String getMenuReturnCard() {
        return menuReturnCard;
    }

    public void showSettingsMenu(String returnTo) {
        menuReturnCard = returnTo;
        showCard(CARD_SETTINGS);
    }

    private void initCursors() {
        AssetStyles.installCursors();
        cursorNormal = AssetStyles.cursorNormal();
        cursorClick = AssetStyles.cursorClick();

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
        if (fpsOverlayPanel != null) {
            fpsOverlayPanel.applyEnabled(engine.isShowFpsCounter());
        }
        if (gamePanel != null) {
            gamePanel.refreshAuditMenuOption();
            gamePanel.applyVisualFrameRateSetting();
        }
        if (menuChaoticPanel != null) {
            menuChaoticPanel.applyVisualFrameInterval(engine.getVisualFrameIntervalMs());
        }
        if (engine.getMusicService() != null) {
            engine.getMusicService().refreshVolume();
        }
        if (engine.getSfxService() != null) {
            engine.getSfxService().refreshVolume();
        }
        applyTheme();
    }

    public void applyEngineSettings() {
        reapplyWindowChrome();
        applyRuntimeSettings();
    }

    public void syncDisplayModeAfterShown() {
        if (!MacOsNativeFullscreen.isMac()) {
            return;
        }
        macFullscreenEnsurePending = engine.isFullScreen();
        macFullscreenRetryCount = 0;
        prepareMacWindowForFullscreen();
        SwingUtilities.invokeLater(() -> {
            prepareMacWindowForFullscreen();
            syncMacNativeFullscreenToEngine();
        });
    }

    private void prepareMacWindowForFullscreen() {
        if (!MacOsNativeFullscreen.isMac()) {
            return;
        }
        MacOsNativeFullscreen.requestForeground();
        if (isVisible()) {
            toFront();
            requestFocus();
        }
    }

    private void maybeRetryMacFullscreenAfterFocus() {
        if (!macFullscreenEnsurePending || !MacOsNativeFullscreen.isMac()) {
            return;
        }
        if (!engine.isFullScreen() || macNativeFullscreenActive || macFullscreenTransition) {
            return;
        }
        SwingUtilities.invokeLater(this::syncMacNativeFullscreenToEngine);
    }

    private void cancelMacFullscreenVerifyTimer() {
        if (macFullscreenVerifyTimer != null) {
            macFullscreenVerifyTimer.stop();
            macFullscreenVerifyTimer = null;
        }
    }

    private void scheduleMacFullscreenVerification() {
        cancelMacFullscreenVerifyTimer();
        int delay = MAC_FULLSCREEN_VERIFY_MS + macFullscreenRetryCount * 350;
        macFullscreenVerifyTimer = new Timer(delay, e -> {
            cancelMacFullscreenVerifyTimer();
            verifyMacFullscreenTransition();
        });
        macFullscreenVerifyTimer.setRepeats(false);
        macFullscreenVerifyTimer.start();
    }

    private void verifyMacFullscreenTransition() {
        if (!macFullscreenTransition) {
            if (engine.isFullScreen() == macNativeFullscreenActive) {
                macFullscreenEnsurePending = false;
                macFullscreenRetryCount = 0;
            }
            return;
        }
        macFullscreenTransition = false;
        boolean want = engine.isFullScreen();
        if (want == macNativeFullscreenActive) {
            macFullscreenEnsurePending = false;
            macFullscreenRetryCount = 0;
            return;
        }
        if (!macFullscreenEnsurePending || macFullscreenRetryCount >= MAC_FULLSCREEN_MAX_RETRIES) {
            macFullscreenEnsurePending = false;
            macFullscreenRetryCount = 0;
            if (want && !macNativeFullscreenActive) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                if (gc != null) {
                    setBounds(gc.getBounds());
                }
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                notifyGeometryChanged();
            }
            return;
        }
        macFullscreenRetryCount++;
        prepareMacWindowForFullscreen();
        SwingUtilities.invokeLater(this::syncMacNativeFullscreenToEngine);
    }

    private void reapplyWindowChrome() {
        Runnable apply = () -> {
            if (MacOsNativeFullscreen.isMac()) {
                reapplyMacOsFullscreen();
            } else {
                reapplyBorderlessFullscreen();
            }
            validate();
            repaint();
            applyGameCursors(this);
            requestFocus();
            notifyGeometryChanged();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            apply.run();
        } else {
            SwingUtilities.invokeLater(apply);
        }
    }

    private void notifyGeometryChanged() {
        if (gamePanel != null) {
            gamePanel.onWindowGeometryChanged();
        }
    }

    private void installMacFullscreenSupport() {
        if (macFullscreenSupportInstalled || !MacOsNativeFullscreen.isMac()) {
            return;
        }
        MacOsNativeFullscreen.markFullscreenable(this);
        MacOsNativeFullscreen.addFullscreenListener(this,
                () -> {
                    cancelMacFullscreenVerifyTimer();
                    macFullscreenTransition = false;
                    macNativeFullscreenActive = true;
                    macFullscreenEnsurePending = false;
                    macFullscreenRetryCount = 0;
                    if (!engine.isFullScreen()) {
                        engine.setFullScreen(true);
                    }
                    notifyGeometryChanged();
                },
                () -> {
                    cancelMacFullscreenVerifyTimer();
                    macFullscreenTransition = false;
                    macNativeFullscreenActive = false;
                    macFullscreenEnsurePending = false;
                    macFullscreenRetryCount = 0;
                    if (engine.isFullScreen()) {
                        engine.setFullScreen(false);
                    }
                    if (!engine.isFullScreen()) {
                        applyWindowedSizeFromEngine();
                    }
                    notifyGeometryChanged();
                });
        macFullscreenSupportInstalled = true;
    }

    private void reapplyMacOsFullscreen() {
        installMacFullscreenSupport();

        if (macFullscreenTransition) {
            return;
        }

        if (macNativeFullscreenActive) {
            if (!engine.isFullScreen()) {
                syncMacNativeFullscreenToEngine();
            }
            return;
        }

        if (isUndecorated()) {
            boolean wasVisible = isVisible();
            if (wasVisible) {
                setVisible(false);
            }
            if (isDisplayable()) {
                dispose();
            }
            setUndecorated(false);
            if (wasVisible) {
                setVisible(true);
            }
        }

        setResizable(true);
        if (!engine.isFullScreen()) {
            macFullscreenEnsurePending = false;
            macFullscreenRetryCount = 0;
            cancelMacFullscreenVerifyTimer();
            applyWindowedSizeFromEngine();
        } else if (!macNativeFullscreenActive) {
            macFullscreenEnsurePending = true;
            macFullscreenRetryCount = 0;
        }
        prepareMacWindowForFullscreen();
        syncMacNativeFullscreenToEngine();
    }

    private void syncMacNativeFullscreenToEngine() {
        boolean want = engine.isFullScreen();
        if (!MacOsNativeFullscreen.isEawtAvailable()) {
            macFullscreenEnsurePending = false;
            macFullscreenRetryCount = 0;
            if (want) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                if (gc != null) {
                    setBounds(gc.getBounds());
                }
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            } else {
                setExtendedState(JFrame.NORMAL);
                applyWindowedSizeFromEngine();
                if (isVisible()) {
                    setLocationRelativeTo(null);
                }
            }
            return;
        }

        if (macFullscreenTransition || want == macNativeFullscreenActive) {
            if (want == macNativeFullscreenActive) {
                macFullscreenEnsurePending = false;
                macFullscreenRetryCount = 0;
            }
            return;
        }

        if (!isVisible() || !isDisplayable()) {
            SwingUtilities.invokeLater(this::syncMacNativeFullscreenToEngine);
            return;
        }

        prepareMacWindowForFullscreen();
        macFullscreenTransition = true;
        if (!MacOsNativeFullscreen.requestToggle(this)) {
            macFullscreenTransition = false;
            if (want) {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            macFullscreenEnsurePending = false;
            macFullscreenRetryCount = 0;
            return;
        }
        scheduleMacFullscreenVerification();
    }

    private void reapplyBorderlessFullscreen() {
        boolean wantFullscreen = engine.isFullScreen();
        boolean needUndecorated = wantFullscreen;

        if (isUndecorated() != needUndecorated) {
            boolean wasVisible = isVisible();
            if (wasVisible) {
                setVisible(false);
            }
            if (isDisplayable()) {
                dispose();
            }
            setUndecorated(needUndecorated);
            if (wasVisible) {
                setVisible(true);
            }
        }

        if (wantFullscreen) {
            setResizable(false);
            setExtendedState(JFrame.NORMAL);
            GraphicsConfiguration gc = getGraphicsConfiguration();
            if (gc != null) {
                setBounds(gc.getBounds());
            } else {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setSize(screenSize);
            }
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setResizable(true);
            setExtendedState(JFrame.NORMAL);
            applyWindowedSizeFromEngine();
        }
    }

    private void applyWindowedSizeFromEngine() {
        if (macNativeFullscreenActive || macFullscreenTransition) {
            return;
        }
        Dimension requested = parseScreenSize(engine.getScreenSize());
        Rectangle usable = resolveUsableWindowBounds();
        int width = Math.min(Math.max(requested.width, 640), Math.max(usable.width, 640));
        int height = Math.min(Math.max(requested.height, 480), Math.max(usable.height, 480));
        setExtendedState(JFrame.NORMAL);
        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);
        int x = usable.x + Math.max(0, (usable.width - width) / 2);
        int y = usable.y + Math.max(0, (usable.height - height) / 2);
        setLocation(x, y);
    }

    private static Dimension parseScreenSize(String screenSize) {
        if (screenSize == null || !screenSize.contains("x")) {
            return new Dimension(1000, 700);
        }
        String[] parts = screenSize.split("x");
        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());
            return new Dimension(Math.max(width, 640), Math.max(height, 480));
        } catch (NumberFormatException e) {
            return new Dimension(1000, 700);
        }
    }

    static Rectangle resolveUsableWindowBounds() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle max = ge.getMaximumWindowBounds();
        if (max != null && max.width > 0 && max.height > 0) {
            return new Rectangle(max);
        }
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        return new Rectangle(0, 0, screen.width, screen.height);
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
        if (CARD_HELP.equals(card)) {
            try {
                helpPanel.ensureTabsContentCurrent();
            } catch (Exception ignore) {}
        }
        cardLayout.show(cards, card);
        boolean showcaseActive = CARD_INIT.equals(card) || CARD_SAVE.equals(card);
        menuChaoticPanel.setActive(showcaseActive);
        syncMusicForCard(card);
        applyGameCursors(this);
        SwingUtilities.invokeLater(() -> {
            Container parent = cards.getParent();
            if (parent != null) {
                Dimension size = parent.getSize();
                int w = Math.max(0, size.width);
                int h = Math.max(0, size.height);
                menuChaoticPanel.setBounds(0, 0, w, h);
                cards.setBounds(0, 0, w, h);
            }
        });
    }

    private void syncMusicForCard(String card) {
        if (engine == null || engine.getMusicService() == null) {
            return;
        }
        if (CARD_INTRO.equals(card)) {
            engine.getMusicService().stopAll();
        } else if (!CARD_GAME.equals(card)) {
            engine.getMusicService().enterMenu();
        }
    }

    public void openGameWithSave(Savefile savefile) {
        showCard(CARD_GAME);
        gamePanel.enterWithSavefile(savefile);
        applyRuntimeSettings();
    }
    
    @Override
    public void onUpgradeTriggered(Upgrade unlockedUpgrade, String title, String message) {
        if (engine.isDisablePopups()) {
            if (gamePanel != null) {
                gamePanel.forceControlPanelMenuRefresh();
                gamePanel.refreshAllGUIData();
            }
            return;
        }

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
            gamePanel.forceControlPanelMenuRefresh();
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
