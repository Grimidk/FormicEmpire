package com.grimidk.formicempire.classes.interfaces;

import javax.swing.*;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.managers.SaveManager;
import com.grimidk.formicempire.classes.infrasctructure.managers.TriggerManager;
import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class MainFrame extends JFrame implements TriggerManager.TriggerListener {
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
    private final InitPanel initPanel;
    private final HelpPanel helpPanel;

    private Cursor cursorNormal;
    private Cursor cursorClick;

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

    public void applyGameCursors(Window window) {
        if (window != null && cursorNormal != null) {
            window.setCursor(cursorNormal);
        }
    }

    private void applyGameCursorForMouseEvent(MouseEvent me) {
        if (cursorNormal == null || cursorClick == null) {
            return;
        }
        Component component = me.getComponent();
        if (component == null) {
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(component);
        if (window == null) {
            return;
        }
        if (me.getID() == MouseEvent.MOUSE_PRESSED) {
            window.setCursor(cursorClick);
        } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
            window.setCursor(cursorNormal);
        }
    }

    public SaveSelectPanel getSaveSelectPanel() {
        return saveSelectPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    public MainFrame(Engine engine) {
        super(LanguageStrings.get(LanguageStrings.UI_APP_TITLE));
        this.engine = engine;
        
        Image icon = AssetStyles.loadImage("/icon.ico");
        if (icon != null) {
            setIconImage(icon);
        }
        
        this.cardLayout = new CardLayout();
        this.cards = new JPanel(cardLayout);

        initCursors();

        this.initPanel = new InitPanel(this);
        this.saveSelectPanel = new SaveSelectPanel(this);
        this.helpPanel = new HelpPanel(this);
        this.settingsPanel = new SettingsPanel(this);
        this.gamePanel = new GamePanel(this);

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
                if (engine.isPauseOnFocusLoss() && gamePanel.isEngineStarted() && engine.isPaused()) {
                    engine.resumeEngine();
                    gamePanel.updateStatusIndicator(false);
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (engine.isPauseOnFocusLoss() && gamePanel.isEngineStarted() && !engine.isPaused()) {
                    engine.pauseEngine();
                    gamePanel.updateStatusIndicator(true);
                }
            }
        });

        applyEngineSettings();
        
        LanguageStrings.addListener(this::refreshTranslations);
    }
    
    private void handleExit() {
        if (engine.isConfirmOnQuit()) {
            int res = JOptionPane.showConfirmDialog(this, 
                LanguageStrings.get("UI_CONFIRM_EXIT_MSG"), 
                LanguageStrings.get("UI_CONFIRM_EXIT_TITLE"), 
                JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    
    private void refreshTranslations() {
        setTitle(LanguageStrings.get(LanguageStrings.UI_APP_TITLE));
        saveSelectPanel.refreshTranslations();
        initPanel.refreshTranslations();
        helpPanel.refreshTranslations();
    }

    private void initCursors() {
        cursorNormal = AssetStyles.loadCustomCursor("/icons/ui/cursor_normal.png", "AntCursorNormal");
        cursorClick = AssetStyles.loadCustomCursor("/icons/ui/cursor_click.png", "AntCursorClick");
        
        setCursor(cursorNormal);

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent me) {
                if (me.getID() == MouseEvent.MOUSE_PRESSED || me.getID() == MouseEvent.MOUSE_RELEASED) {
                    applyGameCursorForMouseEvent(me);
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    public Engine getEngine() {
        return engine;
    }

    public void applyEngineSettings() {
        if (engine.isFullScreen()) {
            dispose();
            setUndecorated(true);
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(screenSize);
            setVisible(true);
            
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            dispose();
            setUndecorated(false);
            setExtendedState(JFrame.NORMAL);
            String[] size = engine.getScreenSize().split("x");
            try {
                int width = Integer.parseInt(size[0]);
                int height = Integer.parseInt(size[1]);
                setSize(width, height);
            } catch (Exception e) {
                setSize(1000, 700);
            }
            setLocationRelativeTo(null);
            setVisible(true);
        }
        
        ToolTipManager.sharedInstance().setEnabled(engine.isShowTooltips());
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
    }

    public void openGameWithSave(Savefile savefile) {
        showCard(CARD_GAME);
        gamePanel.enterWithSavefile(savefile);
        applyEngineSettings();
    }
    
    @Override
    public void onUpgradeTriggered(Upgrade unlockedUpgrade, String title, String message) {
        boolean wasPaused = engine.isPaused();
        if (!wasPaused) {
            engine.pauseEngine();
            if (gamePanel != null) gamePanel.updateStatusIndicator(true);
        }
        
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        
        if (!wasPaused) {
            engine.resumeEngine();
            if (gamePanel != null) gamePanel.updateStatusIndicator(false);
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

        int choice = JOptionPane.showOptionDialog(
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
                JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_NEW_GAME), LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_TITLE), JOptionPane.ERROR_MESSAGE);
                handleQuitToMenu();
            } else {
                SaveManager sm = new SaveManager();
                Savefile saveToLoad = sm.loadAutosaveForSlot(slotId);
                
                if (saveToLoad != null) {
                    openGameWithSave(saveToLoad);
                } else {
                    JOptionPane.showMessageDialog(this, LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_NO_AUTOSAVE), LanguageStrings.get(LanguageStrings.DEATH_LOAD_FAILED_TITLE), JOptionPane.ERROR_MESSAGE);
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
