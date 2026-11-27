package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Colony;
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
        this((Window) owner, colony, engine, targetId, HuntBattleViewPanel.BattleKind.HUNT, ModalityType.MODELESS);
    }

    public HuntBattleDialog(
            Window owner,
            Colony colony,
            Engine engine,
            int battleId,
            HuntBattleViewPanel.BattleKind battleKind,
            ModalityType modalityType) {
        super(owner,
                battleKind == HuntBattleViewPanel.BattleKind.INVASION
                        ? LanguageStrings.DIALOG_INVASION_BATTLE_TITLE
                        : LanguageStrings.DIALOG_HUNT_BATTLE_TITLE,
                AssetStyles.DEFAULT_DIALOG_SIZE,
                modalityType);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.engine = engine;
        battlePanel = new HuntBattleViewPanel(colony, engine, battleId, battleKind);
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
        } else if (owner != null) {
            Window root = SwingUtilities.getWindowAncestor(owner);
            if (root instanceof MainFrame mainFrame) {
                mainFrame.applyGameCursors(this);
            }
        }
    }

    public int getTargetId() {
        return battlePanel.getTargetId();
    }

    public void showDialog() {
        battlePanel.startAnimation();
        battlePanel.refreshScene();
        if (!battlePanel.hasActiveState()) {
            dispose();
            return;
        }
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
        if (battlePanel.getColony() == null || !battlePanel.hasActiveState()) {
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
