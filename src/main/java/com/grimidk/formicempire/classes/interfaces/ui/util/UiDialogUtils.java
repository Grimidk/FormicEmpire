package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.interfaces.MainFrame;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import java.awt.KeyboardFocusManager;

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

    public static Window resolveForegroundOwner(Component fallback) {
        Window root = fallback instanceof Window window ? window : SwingUtilities.getWindowAncestor(fallback);
        Window active = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (active != null && isUnderGameRoot(active, root)) {
            return active;
        }
        Window top = root;
        if (root != null) {
            for (Window window : Window.getWindows()) {
                if (!window.isVisible() || !isUnderGameRoot(window, root)) {
                    continue;
                }
                if (top == null || window instanceof java.awt.Dialog) {
                    top = window;
                }
            }
        }
        return top != null ? top : root;
    }

    private static boolean isUnderGameRoot(Window window, Window root) {
        if (window == null || root == null) {
            return false;
        }
        if (window == root) {
            return true;
        }
        for (Window owner = window.getOwner(); owner != null; owner = owner.getOwner()) {
            if (owner == root) {
                return true;
            }
        }
        return false;
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
