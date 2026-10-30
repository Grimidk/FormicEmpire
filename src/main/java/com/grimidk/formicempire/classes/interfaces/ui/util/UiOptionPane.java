package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.infrasctructure.audio.SfxService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.SoundEffects;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Component;
import java.awt.Window;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class UiOptionPane {
    private static final int DIALOG_ICON_SIZE_PX = 32;
    private static Icon dialogIcon;

    private UiOptionPane() {
    }

    public static void applyLocalizedButtonTexts() {
        UIManager.put("OptionPane.yesButtonText", LanguageStrings.get(LanguageStrings.UI_YES));
        UIManager.put("OptionPane.noButtonText", LanguageStrings.get(LanguageStrings.UI_NO));
        UIManager.put("OptionPane.okButtonText", LanguageStrings.get(LanguageStrings.UI_OK));
        UIManager.put("OptionPane.cancelButtonText", LanguageStrings.get(LanguageStrings.UI_CANCEL));
    }

    public static void showMessageDialog(Component parent, Object message) {
        showMessageDialog(parent, message, null, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showMessageDialog(Component parent, Object message, String title, int messageType) {
        JDialog dialog = createDialog(parent, title, createPane(message, messageType, JOptionPane.DEFAULT_OPTION, null, null, null));
        showBlockingDialog(dialog, parent);
    }

    public static void showForegroundMessageDialog(Component parent, Object message, String title, int messageType) {
        Window owner = UiDialogUtils.resolveForegroundOwner(parent);
        JDialog dialog = createDialog(owner, title, createPane(message, messageType, JOptionPane.DEFAULT_OPTION, null, null, null));
        showForegroundBlockingDialog(dialog, owner);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showConfirmDialog(parent, message, title, optionType, JOptionPane.PLAIN_MESSAGE);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType, int messageType) {
        JOptionPane pane = createPane(message, messageType, optionType, null, null, null);
        JDialog dialog = createDialog(parent, title, pane);
        showBlockingDialog(dialog, parent);
        return readIntegerValue(pane.getValue());
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
        JOptionPane pane = createPane(message, messageType, optionType, icon, options, initialValue);
        JDialog dialog = createDialog(parent, title, pane);
        showBlockingDialog(dialog, parent);
        return readOptionValue(pane.getValue(), options);
    }

    public static int showForegroundOptionDialog(
            Component parent,
            Object message,
            String title,
            int optionType,
            int messageType,
            Icon icon,
            Object[] options,
            Object initialValue) {
        Window owner = UiDialogUtils.resolveForegroundOwner(parent);
        JOptionPane pane = createPane(message, messageType, optionType, icon, options, initialValue);
        JDialog dialog = createDialog(owner, title, pane);
        showForegroundBlockingDialog(dialog, owner);
        return readOptionValue(pane.getValue(), options);
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
        JOptionPane pane = createPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, null, null, initialSelection);
        pane.setWantsInput(true);
        JDialog dialog = createDialog(parent, title, pane);
        showBlockingDialog(dialog, parent);
        Object value = pane.getInputValue();
        if (value == null || value == JOptionPane.UNINITIALIZED_VALUE) {
            return null;
        }
        return value.toString();
    }

    private static JOptionPane createPane(
            Object message,
            int messageType,
            int optionType,
            Icon icon,
            Object[] options,
            Object initialValue) {
        applyLocalizedButtonTexts();
        Icon resolvedIcon = resolveIcon(messageType, icon);
        if (options == null && initialValue == null) {
            return new JOptionPane(message, messageType, optionType, resolvedIcon);
        }
        return new JOptionPane(message, messageType, optionType, resolvedIcon, options, initialValue);
    }

    private static Icon resolveIcon(int messageType, Icon explicitIcon) {
        if (explicitIcon != null) {
            return explicitIcon;
        }
        if (messageType == JOptionPane.PLAIN_MESSAGE) {
            return null;
        }
        return dialogIcon();
    }

    private static Icon dialogIcon() {
        if (dialogIcon == null) {
            dialogIcon = UiResourceLoader.loadDialogIcon(
                    AssetStyles.class,
                    AssetStyles.META_DIALOG_ICON,
                    DIALOG_ICON_SIZE_PX);
        }
        return dialogIcon;
    }

    private static JDialog createDialog(Component parent, String title, JOptionPane pane) {
        return pane.createDialog(parent, title);
    }

    private static void showBlockingDialog(JDialog dialog, Component parent) {
        UiDialogUtils.prepareDialog(dialog, parent);
        SfxService.play(SoundEffects.POPUP);
        dialog.setVisible(true);
        dialog.dispose();
    }

    private static void showForegroundBlockingDialog(JDialog dialog, Component parent) {
        UiDialogUtils.prepareDialog(dialog, parent);
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        SfxService.play(SoundEffects.POPUP);
        dialog.setVisible(true);
        dialog.dispose();
    }

    private static int readIntegerValue(Object value) {
        if (value instanceof Integer integer) {
            return integer;
        }
        return JOptionPane.CLOSED_OPTION;
    }

    private static int readOptionValue(Object value, Object[] options) {
        int integerValue = readIntegerValue(value);
        if (integerValue != JOptionPane.CLOSED_OPTION) {
            return integerValue;
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
}
