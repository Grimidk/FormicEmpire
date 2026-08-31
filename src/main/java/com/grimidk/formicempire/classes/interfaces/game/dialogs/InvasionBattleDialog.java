package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public final class InvasionBattleDialog extends ZeroDialog {

    private final HuntBattleViewPanel battlePanel;

    public InvasionBattleDialog(JFrame owner, Colony colony, Engine engine, int alertId) {
        super(owner, LanguageStrings.get(LanguageStrings.DIALOG_INVASION_BATTLE_TITLE), AssetStyles.DEFAULT_DIALOG_SIZE);
        battlePanel = new HuntBattleViewPanel(colony, engine, alertId, HuntBattleViewPanel.BattleKind.INVASION);
        add(battlePanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                battlePanel.stopAnimation();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                battlePanel.stopAnimation();
            }
        });

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
    }

    public int getAlertId() {
        return battlePanel.getBattleId();
    }

    public void showDialog() {
        battlePanel.startAnimation();
        battlePanel.refreshScene();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::liveUpdate);
            return;
        }
        if (!isVisible()) {
            return;
        }
        if (battlePanel.getColony() == null
                || HuntCreatureCombatService.getInvasionState(battlePanel.getColony(), battlePanel.getBattleId()) == null) {
            dispose();
            return;
        }
        battlePanel.refreshScene();
        battlePanel.repaint();
    }

    @Override
    protected void refreshDialog() {
        battlePanel.refreshScene();
    }
}
