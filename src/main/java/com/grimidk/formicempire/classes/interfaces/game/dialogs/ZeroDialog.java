package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.EdgeTriggeredKeyBindings;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public abstract class ZeroDialog extends JDialog {

    protected final JPanel southPanel;
    
    private final String titleKey;
    private final JButton closeButton;
    private boolean hideOnClose;

    public ZeroDialog(JFrame owner, String titleKey, Dimension preferredSize) {
        super(owner, LanguageStrings.get(titleKey), true);
        this.titleKey = titleKey;

        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }
        
        getContentPane().setBackground(AssetStyles.UI_BG_PRIMARY);
        setLayout(new BorderLayout());
        
        Dimension size = (preferredSize != null) ? preferredSize : AssetStyles.DEFAULT_DIALOG_SIZE;
        setPreferredSize(size);

        southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        
        closeButton = new JButton(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        closeButton.setFocusable(false);
        AssetStyles.styleButton(closeButton);
        closeButton.addActionListener(e -> requestClose());
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);

        EdgeTriggeredKeyBindings.bind(
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                getRootPane().getActionMap(),
                KeyEvent.VK_ESCAPE,
                "escapeClose",
                this::handleEscapeKey);
        
        setFocusable(true);
    }

    protected void setHideOnClose(boolean hideOnClose) {
        this.hideOnClose = hideOnClose;
    }

    public void requestClose() {
        if (hideOnClose) {
            setVisible(false);
        } else {
            dispose();
        }
    }
    
    public void refreshTranslations() {
        setTitle(LanguageStrings.get(titleKey));
        closeButton.setText(LanguageStrings.get(LanguageStrings.UI_CLOSE));
        refreshDialog();
    }

    public void refreshTheme() {
        getContentPane().setBackground(AssetStyles.UI_BG_PRIMARY);
        southPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        AssetStyles.styleButton(closeButton);
        AssetStyles.applyThemeToContainer(getContentPane());
        refreshDialog();
    }

    protected void disableFocusTraversal(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof AbstractButton || comp instanceof JComboBox || comp instanceof JSpinner || comp instanceof JList || comp instanceof JTable || comp instanceof JTabbedPane) {
                comp.setFocusable(false);
            }
            if (comp instanceof Container) {
                disableFocusTraversal((Container) comp);
            }
        }
    }

    private void handleEscapeKey() {
        if (consumeEscape()) {
            return;
        }
        if (getOwner() instanceof MainFrame frame && frame.getGamePanel() != null
                && frame.getGamePanel().handleEscapeKey()) {
            return;
        }
        requestClose();
    }

    protected boolean consumeEscape() {
        return false;
    }

    protected void registerCloseKey(int keyEvent) {
        EdgeTriggeredKeyBindings.bind(
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                getRootPane().getActionMap(),
                keyEvent,
                "closeKey",
                this::requestClose);
    }

    protected void addToSouthPanel(JComponent component) {
        southPanel.add(component, 0); 
    }

    public void showDialog() {
        refreshDialog();

        disableFocusTraversal(this);

        Dimension size = getPreferredSize();
        if (size == null || size.width < AssetStyles.DEFAULT_DIALOG_SIZE.width) {
            size = AssetStyles.DEFAULT_DIALOG_SIZE;
        }
        setSize(size);

        if (getOwner() != null && getOwner().isVisible()) {
            Point ownerLoc = getOwner().getLocationOnScreen();
            int x = ownerLoc.x + (getOwner().getWidth() - getWidth()) / 2;
            int y = ownerLoc.y + 100;
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
            Point loc = getLocation();
            setLocation(loc.x, loc.y + 60);
        }

        if (getOwner() instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(this);
        }

        setVisible(true);
    }

    protected abstract void refreshDialog();

    public void liveUpdate() {}
}
