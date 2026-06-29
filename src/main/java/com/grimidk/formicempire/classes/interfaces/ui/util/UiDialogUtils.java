package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public final class UiDialogUtils {
    private UiDialogUtils() {
    }

    public static void prepareDialog(JDialog dialog, Component parent) {
        if (dialog == null) {
            return;
        }
        Container content = dialog.getContentPane();
        content.setBackground(AssetStyles.UI_BG_PRIMARY);
        applyGameCursors(dialog, parent);
        AssetStyles.applyThemeToContainer(content);
    }

    public static void show(JDialog dialog, Component parent) {
        prepareDialog(dialog, parent);
        if (dialog.getSize().width <= 0 || dialog.getSize().height <= 0) {
            dialog.pack();
        }
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static void applyGameCursors(JDialog dialog, Component parent) {
        Window owner = dialog.getOwner();
        if (owner instanceof MainFrame mainFrame) {
            mainFrame.applyGameCursors(dialog);
            return;
        }
        if (parent != null) {
            Window ancestor = SwingUtilities.getWindowAncestor(parent);
            if (ancestor instanceof MainFrame mainFrame) {
                mainFrame.applyGameCursors(dialog);
            }
        }
    }
}
