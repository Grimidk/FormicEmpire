package com.grimidk.formicempire.classes.interfaces;

import javax.swing.*;

import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;

import java.awt.*;

public class MainFrame extends JFrame {
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

    public MainFrame(Engine engine) {
        super("Formic Empire");
        this.engine = engine;
        this.cardLayout = new CardLayout();
        this.cards = new JPanel(cardLayout);

        InitPanel initPanel = new InitPanel(this);
        this.saveSelectPanel = new SaveSelectPanel(this);
        HelpPanel helpPanel = new HelpPanel(this);
        this.settingsPanel = new SettingsPanel(this);
        this.gamePanel = new GamePanel(this);

        cards.add(initPanel, CARD_INIT);
        cards.add(saveSelectPanel, CARD_SAVE);
        cards.add(helpPanel, CARD_HELP);
        cards.add(settingsPanel, CARD_SETTINGS);
        cards.add(gamePanel, CARD_GAME);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cards, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        applyEngineSettings();
    }

    public Engine getEngine() {
        return engine;
    }

    public void applyEngineSettings() {
        if (engine.isFullScreen()) {
            dispose();
            setUndecorated(true);
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
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
}