package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.entities.services.world.WarCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WarBattleDialog extends ZeroDialog {

    private final War war;
    private final WarBattleViewPanel battlePanel;
    private final BattleDialogSupport dialogSupport = new BattleDialogSupport();
    private final Engine engine;

    public WarBattleDialog(JFrame owner, War war, Engine engine) {
        this((Window) owner, war, engine);
    }

    public WarBattleDialog(Window owner, War war, Engine engine) {
        super(owner, LanguageStrings.DIALOG_BATTLE_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE,
                owner instanceof JDialog ? ModalityType.DOCUMENT_MODAL : ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.war = war;
        this.engine = engine;

        battlePanel = new WarBattleViewPanel(war, engine, this::dispose);
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
        if (!isShowing()) {
            return;
        }
        if (war == null || !war.isActive() || WarCreatureCombatService.getState(war) == null) {
            dispose();
            return;
        }
        battlePanel.refreshScene();
        dialogSupport.updateControls(engine);
    }

    public void applyVisualFrameRate() {
        battlePanel.applyVisualFrameRate();
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
