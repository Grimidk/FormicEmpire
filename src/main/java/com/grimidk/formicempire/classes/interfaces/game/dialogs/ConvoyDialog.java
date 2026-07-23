package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.entities.dynasty.Trade;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ConvoyDialog extends ZeroDialog {

    private final ConvoyViewPanel convoyPanel;

    public ConvoyDialog(JFrame owner, Trade trade, Engine engine) {
        super(owner, LanguageStrings.DIALOG_CONVOY_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);

        convoyPanel = new ConvoyViewPanel(trade, engine);
        add(convoyPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                convoyPanel.stopAnimation();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                convoyPanel.stopAnimation();
            }
        });

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
    }

    public void showDialog() {
        convoyPanel.startAnimation();
        convoyPanel.refreshScene();
        pack();
        setLocationRelativeTo(null);
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
        convoyPanel.refreshScene();
    }

    @Override
    public void dispose() {
        convoyPanel.stopAnimation();
        super.dispose();
    }

    @Override
    protected void refreshDialog() {
        convoyPanel.refreshScene();
    }
}
