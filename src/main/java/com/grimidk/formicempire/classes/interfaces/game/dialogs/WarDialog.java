package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;

public class WarDialog extends ZeroDialog {

    private final Engine engine;
    private final WarManagementPanel warPanel;
    private final Runnable refreshTask = this::liveUpdate;

    public WarDialog(JFrame owner, Dynasty dynasty, Engine engine, WarManagementPanel.Callbacks callbacks) {
        super(owner, LanguageStrings.DIALOG_WAR_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.engine = engine;

        warPanel = new WarManagementPanel(dynasty, engine, callbacks);
        add(warPanel, BorderLayout.CENTER);

        registerCloseKey(KeyEvent.VK_F);

        if (engine != null) {
            engine.addHourTickListener(refreshTask);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                detachTickListener();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                detachTickListener();
            }
        });

        setLocationRelativeTo(owner);
    }

    public void liveUpdate() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::liveUpdate);
            return;
        }
        if (!isShowing()) {
            return;
        }
        warPanel.updateData();
    }

    @Override
    public void dispose() {
        detachTickListener();
        super.dispose();
    }

    @Override
    protected void refreshDialog() {
        warPanel.updateData();
    }

    private void detachTickListener() {
        if (engine != null) {
            engine.removeHourTickListener(refreshTask);
        }
    }
}
