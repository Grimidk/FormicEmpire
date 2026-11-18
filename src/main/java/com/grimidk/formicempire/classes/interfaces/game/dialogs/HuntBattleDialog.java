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

public class HuntBattleDialog extends ZeroDialog {

    private final HuntBattleViewPanel battlePanel;
    private final BattleDialogSupport dialogSupport = new BattleDialogSupport();
    private final Engine engine;

    public HuntBattleDialog(JFrame owner, Colony colony, Engine engine, int targetId) {
        super(owner, LanguageStrings.DIALOG_HUNT_BATTLE_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE, ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.engine = engine;
        battlePanel = new HuntBattleViewPanel(colony, engine, targetId);
        add(battlePanel, BorderLayout.CENTER);
        dialogSupport.populateSpeedControlBar(southPanel, engine, owner, this::requestClose);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                battlePanel.stopAnimation();
                dialogSupport.closeAndRestore();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                battlePanel.stopAnimation();
                dialogSupport.closeAndRestore();
            }
        });

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
    }

    public int getTargetId() {
        return battlePanel.getTargetId();
    }

    public void showDialog() {
        battlePanel.startAnimation();
        battlePanel.refreshScene();
        dialogSupport.updateControls(engine);
        dialogSupport.openFullScreen(this, getOwner(), engine);
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
                || HuntCreatureCombatService.getState(battlePanel.getColony(), battlePanel.getTargetId()) == null) {
            dispose();
            return;
        }
        battlePanel.refreshScene();
        dialogSupport.updateControls(engine);
        battlePanel.repaint();
    }

    @Override
    public void dispose() {
        battlePanel.stopAnimation();
        dialogSupport.closeAndRestore();
        super.dispose();
    }

    @Override
    protected void refreshDialog() {
        battlePanel.refreshScene();
    }
}
