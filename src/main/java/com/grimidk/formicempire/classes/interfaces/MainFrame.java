package com.grimidk.formicempire.classes.interfaces;

import com.grimidk.formicempire.classes.Engine;
import com.grimidk.formicempire.classes.Savefile;

import javax.swing.*;
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

    public MainFrame(Engine engine) {
        super("Formic Empire");
        this.engine = engine;
        this.cardLayout = new CardLayout();
        this.cards = new JPanel(cardLayout);

        InitPanel initPanel = new InitPanel(this);
        this.saveSelectPanel = new SaveSelectPanel(this);
        HelpPanel helpPanel = new HelpPanel(this);
        SettingsPanel settingsPanel = new SettingsPanel(this);
        this.gamePanel = new GamePanel(this);

        cards.add(initPanel, CARD_INIT);
        cards.add(saveSelectPanel, CARD_SAVE);
        cards.add(helpPanel, CARD_HELP);
        cards.add(settingsPanel, CARD_SETTINGS);
        cards.add(gamePanel, CARD_GAME);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cards, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    public Engine getEngine() {
        return engine;
    }

    public void showCard(String card) {
        if (CARD_SAVE.equals(card)) {
            try {
                saveSelectPanel.refreshSlots();
            } catch (Exception ignore) {}
        }
        cardLayout.show(cards, card);
    }

    public void openGameWithSave(Savefile savefile) {
        showCard(CARD_GAME);
        gamePanel.enterWithSavefile(savefile);
    }
}
