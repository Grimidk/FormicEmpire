package com.grimidk.formicempire.classes.interfaces.ui.util;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/** Themed wrappers around {@link JOptionPane} — flat buttons and palette via global LaF + theme walk. */
public final class UiOptionPane {
    private UiOptionPane() {
    }

    public static void showMessageDialog(Component parent, Object message) {
        showMessageDialog(parent, message, null, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType);
        JDialog dialog = pane.createDialog(parent, title);
        UiDialogUtils.prepareDialog(dialog, parent);
        dialog.setVisible(true);
        dialog.dispose();
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, JOptionPane.PLAIN_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType, optionType);
        JDialog dialog = pane.createDialog(parent, title);
        UiDialogUtils.prepareDialog(dialog, parent);
        dialog.setVisible(true);
        dialog.dispose();
        Object value = pane.getValue();
        if (value instanceof Integer integer) {
            return integer;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public static int showOptionDialog(
            Component parent,
            Object message,
            String title,
            int optionType,
            int messageType,
            Object[] options,
            Object initialValue) {
        return showOptionDialog(parent, message, title, optionType, messageType, null, options, initialValue);
    }

    public static int showOptionDialog(
            Component parent,
            Object message,
            String title,
            int optionType,
            int messageType,
            Icon icon,
            Object[] options,
            Object initialValue) {
        JOptionPane pane = new JOptionPane(message, messageType, optionType, icon, options, initialValue);
        JDialog dialog = pane.createDialog(parent, title);
        UiDialogUtils.prepareDialog(dialog, parent);
        dialog.setVisible(true);
        dialog.dispose();
        Object value = pane.getValue();
        if (value instanceof Integer integer) {
            return integer;
        }
        if (value != null && options != null) {
            for (int i = 0; i < options.length; i++) {
                if (value.equals(options[i])) {
                    return i;
                }
            }
        }
        return JOptionPane.CLOSED_OPTION;
    }

    public static String showInputDialog(Component parent, Object message, Object initialSelectionValue) {
        return showInputDialog(parent, message, null, JOptionPane.PLAIN_MESSAGE, initialSelectionValue);
    }

    public static String showInputDialog(Component parent, Object message, String title, int messageType) {
        return showInputDialog(parent, message, title, messageType, null);
    }

    public static String showInputDialog(
            Component parent,
            Object message,
            String title,
            int messageType,
            Object initialSelection) {
        JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, null, null, initialSelection);
        pane.setWantsInput(true);
        JDialog dialog = pane.createDialog(parent, title);
        UiDialogUtils.prepareDialog(dialog, parent);
        dialog.setVisible(true);
        dialog.dispose();
        Object value = pane.getInputValue();
        return value != null ? value.toString() : null;
    }
}
