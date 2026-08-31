package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.BiConsumer;

public final class CritterManagementDialog extends ZeroDialog {

    public static final int TAB_HUNTS = 0;
    public static final int TAB_INVASIONS = 1;
    public static final int TAB_PETS = 2;
    public static final int TAB_PARASITES = 3;

    private final Colony colony;
    private final Engine engine;
    private final BiConsumer<Colony, Integer> openBattleCallback;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final HuntsPanel huntsPanel;
    private final InvasionsPanel invasionsPanel;
    private final PetsPanel petsPanel;
    private final ParasitesPanel parasitesPanel;

    public CritterManagementDialog(JFrame owner, Colony colony, Engine engine,
            BiConsumer<Colony, Integer> openHuntBattleCallback,
            BiConsumer<Colony, Integer> openInvasionBattleCallback) {
        super(owner, LanguageStrings.DIALOG_CRITTER_MANAGEMENT_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;
        this.engine = engine;
        this.openBattleCallback = openHuntBattleCallback;

        huntsPanel = new HuntsPanel(owner, colony, engine, openHuntBattleCallback);
        invasionsPanel = new InvasionsPanel(owner, colony, engine, openInvasionBattleCallback, this::refreshAllTabs);
        petsPanel = new PetsPanel(colony, engine);
        parasitesPanel = new ParasitesPanel(colony, engine);

        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.CRITTER_TAB_HUNTS), huntsPanel);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.CRITTER_TAB_INVASIONS), invasionsPanel);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.CRITTER_TAB_PETS), petsPanel);
        tabbedPane.addTab(LanguageStrings.get(LanguageStrings.CRITTER_TAB_PARASITES), parasitesPanel);
        AssetStyles.styleTabbedPane(tabbedPane);

        add(tabbedPane, BorderLayout.CENTER);
        registerCloseKey(KeyEvent.VK_H);

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
    }

    @Override
    protected void refreshDialog() {
        refreshAllTabs();
    }

    private void refreshAllTabs() {
        huntsPanel.refresh();
        invasionsPanel.refresh();
        petsPanel.refresh();
        parasitesPanel.refresh();
    }

    public void liveUpdate() {
        if (!isVisible()) {
            return;
        }
        refreshAllTabs();
    }
}
