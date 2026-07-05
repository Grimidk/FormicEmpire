package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WarBattleDialog extends ZeroDialog {

    private final Engine engine;
    private final WarBattleViewPanel battlePanel;
    private final Runnable refreshTask = this::liveUpdate;

    public WarBattleDialog(JFrame owner, War war, Engine engine) {
        super(owner, LanguageStrings.DIALOG_BATTLE_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.engine = engine;

        battlePanel = new WarBattleViewPanel(war, engine);
        add(battlePanel, BorderLayout.CENTER);

        if (engine != null) {
            engine.addHourTickListener(refreshTask);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                detachTickListener();
                battlePanel.stopAnimation();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                detachTickListener();
                battlePanel.stopAnimation();
            }
        });

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
        setLocationRelativeTo(owner);
    }

    public void showDialog() {
        battlePanel.refreshScene();
        pack();
        setVisible(true);
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::liveUpdate);
            return;
        }
        if (!isShowing()) {
            return;
        }
        battlePanel.refreshScene();
    }

    @Override
    public void dispose() {
        detachTickListener();
        battlePanel.stopAnimation();
        super.dispose();
    }

    @Override
    protected void refreshDialog() {
        battlePanel.refreshScene();
    }

    private void detachTickListener() {
        if (engine != null) {
            engine.removeHourTickListener(refreshTask);
        }
    }
}
