package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class ZeroDialog extends JDialog {

    protected final JPanel southPanel;

    public ZeroDialog(JFrame owner, String title, Dimension preferredSize) {
        super(owner, title, true);
        
        getContentPane().setBackground(AssetStyles.UI_BG_PRIMARY);
        setLayout(new BorderLayout());
        if (preferredSize != null) {
            setPreferredSize(preferredSize);
        }

        southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(AssetStyles.FONT_NORMAL);
        closeButton.addActionListener(e -> dispose());
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }


    protected void registerCloseKey(int keyEvent) {
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(keyEvent, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    protected void addToSouthPanel(JComponent component) {
        southPanel.add(component, 0); 
    }

    public void showDialog() {
        refreshDialog();
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    protected abstract void refreshDialog();

    public void liveUpdate() {}
}