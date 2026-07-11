package com.grimidk.formicempire.classes.interfaces.ui.theme;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatChevronButton;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiButtonStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiCheckBoxStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiComboBoxStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiMenuStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiProgressBarStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiRadioButtonStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiScrollBarStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiSliderStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiSpinnerStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTabbedPaneStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTextFieldStyles;
import java.awt.Component;
import java.awt.Container;
import javax.swing.BorderFactory;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import static com.grimidk.formicempire.classes.interfaces.ui.theme.UiPalette.*;

/** Theme application and component walk — mutates {@link AssetStyles} palette fields. */
public final class UiTheme {
    private UiTheme() {
    }

    public static void applyTheme(boolean dark) {
        AssetStyles.setDarkMode(dark);
        if (dark) {
            AssetStyles.BACKGROUND_COLOR = COLOR_NEAR_BLACK;
            AssetStyles.BACKGROUND_SECONDARY = COLOR_NEAR_BLACK;
            AssetStyles.BACKGROUND_DARK = COLOR_NEAR_BLACK;
            AssetStyles.BACKGROUND_LIGHT = COLOR_DARK_GRAY;
            AssetStyles.FONT_COLOR = COLOR_ABSOLUTE_WHITE;
            AssetStyles.FONT_COLOR_BRIGHT = COLOR_ABSOLUTE_WHITE;
            AssetStyles.FONT_COLOR_HEADER = COLOR_ABSOLUTE_WHITE;
            AssetStyles.FONT_COLOR_SUCCESS = COLOR_LIGHT_GREEN;
            AssetStyles.FONT_COLOR_ERROR = COLOR_LIGHT_RED;
            AssetStyles.FONT_COLOR_WARNING = COLOR_LIGHT_YELLOW;
            AssetStyles.FONT_COLOR_HIGHLIGHT = COLOR_LIGHT_BLUE;
            AssetStyles.FONT_COLOR_VALUE = COLOR_ABSOLUTE_WHITE;
            AssetStyles.BORDER_COLOR = COLOR_MEDIUM_GRAY;
            AssetStyles.TEXT_NORMAL = COLOR_ABSOLUTE_WHITE;
            AssetStyles.TEXT_HEADER = COLOR_ABSOLUTE_WHITE;
            AssetStyles.TEXT_SUCCESS = COLOR_LIGHT_GREEN;
            AssetStyles.TEXT_ERROR = COLOR_LIGHT_RED;
            AssetStyles.TEXT_WARNING = COLOR_LIGHT_YELLOW;
            AssetStyles.SELECTION_BACKGROUND = COLOR_DARK_GRAY;
        } else {
            AssetStyles.BACKGROUND_COLOR = COLOR_LIGHTEST_GRAY;
            AssetStyles.BACKGROUND_SECONDARY = COLOR_VERY_LIGHT_GRAY;
            AssetStyles.BACKGROUND_DARK = COLOR_LIGHTER_GRAY;
            AssetStyles.BACKGROUND_LIGHT = COLOR_ABSOLUTE_WHITE;
            AssetStyles.FONT_COLOR = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_BRIGHT = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_HEADER = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_SUCCESS = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_ERROR = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_WARNING = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_HIGHLIGHT = COLOR_ABSOLUTE_BLACK;
            AssetStyles.FONT_COLOR_VALUE = COLOR_ABSOLUTE_BLACK;
            AssetStyles.BORDER_COLOR = COLOR_ABSOLUTE_BLACK;
            AssetStyles.TEXT_NORMAL = COLOR_ABSOLUTE_BLACK;
            AssetStyles.TEXT_HEADER = COLOR_ABSOLUTE_BLACK;
            AssetStyles.TEXT_SUCCESS = COLOR_ABSOLUTE_BLACK;
            AssetStyles.TEXT_ERROR = COLOR_ABSOLUTE_BLACK;
            AssetStyles.TEXT_WARNING = COLOR_ABSOLUTE_BLACK;
            AssetStyles.SELECTION_BACKGROUND = COLOR_LIGHTER_GRAY;
        }
        AssetStyles.UI_BG_PRIMARY = AssetStyles.BACKGROUND_COLOR;
        AssetStyles.UI_BG_SECONDARY = AssetStyles.BACKGROUND_SECONDARY;
        AssetStyles.UI_BG_HEADER = AssetStyles.BACKGROUND_SECONDARY;
        AssetStyles.UI_BORDER_COLOR = AssetStyles.BORDER_COLOR;
        AssetStyles.PANEL_BORDER = BorderFactory.createLineBorder(
                AssetStyles.UI_BORDER_COLOR, AssetStyles.BORDER_THICKNESS_EXTERNAL);
        AssetStyles.INTERNAL_BORDER = BorderFactory.createLineBorder(
                AssetStyles.UI_BORDER_COLOR, AssetStyles.BORDER_THICKNESS_INTERNAL);
        AssetStyles.BUTTON_BORDER = BorderFactory.createLineBorder(
                AssetStyles.UI_BORDER_COLOR, AssetStyles.BORDER_THICKNESS_BUTTON);
        AssetStyles.TAB_SELECTED_BG = dark ? COLOR_DARK_GRAY : COLOR_ABSOLUTE_WHITE;
        AssetStyles.TAB_UNSELECTED_BG = dark ? COLOR_NEAR_BLACK : COLOR_VERY_LIGHT_GRAY;
        AssetStyles.MAP_HEX_INACTIVE_FADE = dark ? 0.25f : 0.4f;
    }

    public static JSeparator createInternalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(AssetStyles.BORDER_COLOR);
        sep.setBackground(AssetStyles.BORDER_COLOR);
        return sep;
    }

    public static boolean isThemeExempt(Component component) {
        if (component == null) {
            return false;
        }
        String name = component.getClass().getName();
        return name.endsWith("IntroPanel") || name.endsWith("GameAreaPanel");
    }

    public static void applyThemeToContainer(Container root) {
        if (root == null || isThemeExempt(root)) {
            return;
        }
        for (Component component : root.getComponents()) {
            if (isThemeExempt(component)) {
                continue;
            }
            applyThemeToComponent(component);
            if (component instanceof Container container) {
                applyThemeToContainer(container);
            }
        }
    }

    private static void applyThemeToComponent(Component component) {
        if (component instanceof javax.swing.JPanel panel) {
            panel.setBackground(AssetStyles.UI_BG_PRIMARY);
            panel.setForeground(AssetStyles.TEXT_NORMAL);
        } else if (component instanceof javax.swing.JLabel label) {
            label.setBackground(AssetStyles.UI_BG_PRIMARY);
            label.setForeground(AssetStyles.TEXT_NORMAL);
        } else if (component instanceof FlatChevronButton chevronButton) {
            chevronButton.setBackground(AssetStyles.BACKGROUND_COLOR);
            chevronButton.repaint();
        } else if (component instanceof javax.swing.JButton button) {
            if (Boolean.TRUE.equals(button.getClientProperty(AssetStyles.ICON_BUTTON_CLIENT_KEY))) {
                UiButtonStyles.styleIcon(button);
            } else {
                UiButtonStyles.style(button);
            }
            button.repaint();
        } else if (component instanceof javax.swing.JToggleButton toggleButton) {
            UiButtonStyles.style(toggleButton);
            toggleButton.repaint();
        } else if (component instanceof javax.swing.JMenuItem menuItem) {
            UiMenuStyles.style(menuItem);
        } else if (component instanceof javax.swing.JCheckBox checkBox) {
            UiCheckBoxStyles.style(checkBox);
        } else if (component instanceof javax.swing.JRadioButton radioButton) {
            UiRadioButtonStyles.style(radioButton);
        } else if (component instanceof javax.swing.JComboBox<?> comboBox) {
            UiComboBoxStyles.applyComboColors(comboBox);
            UiComboBoxStyles.style(comboBox);
            comboBox.updateUI();
        } else if (component instanceof javax.swing.JSpinner spinner) {
            UiSpinnerStyles.style(spinner);
        } else if (component instanceof javax.swing.JProgressBar progressBar) {
            UiProgressBarStyles.style(progressBar);
            progressBar.updateUI();
        } else if (component instanceof javax.swing.JFormattedTextField formattedTextField) {
            UiTextFieldStyles.style(formattedTextField);
        } else if (component instanceof javax.swing.JTextField textField) {
            UiTextFieldStyles.style(textField);
        } else if (component instanceof javax.swing.JTextArea textArea) {
            textArea.setBackground(AssetStyles.BACKGROUND_COLOR);
            textArea.setForeground(AssetStyles.FONT_COLOR);
        } else if (component instanceof javax.swing.JTextPane textPane) {
            textPane.setBackground(AssetStyles.BACKGROUND_COLOR);
            textPane.setForeground(AssetStyles.FONT_COLOR);
        } else if (component instanceof javax.swing.JEditorPane editorPane) {
            editorPane.setBackground(AssetStyles.BACKGROUND_COLOR);
            editorPane.setForeground(AssetStyles.FONT_COLOR);
        } else if (component instanceof javax.swing.JList<?> list) {
            list.setBackground(AssetStyles.BACKGROUND_COLOR);
            list.setForeground(AssetStyles.FONT_COLOR);
        } else if (component instanceof javax.swing.JTable table) {
            table.setBackground(AssetStyles.BACKGROUND_COLOR);
            table.setForeground(AssetStyles.FONT_COLOR);
            table.setGridColor(AssetStyles.BACKGROUND_SECONDARY);
            table.setSelectionBackground(AssetStyles.SELECTION_BACKGROUND);
            table.setSelectionForeground(AssetStyles.FONT_COLOR_HEADER);
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(AssetStyles.BACKGROUND_SECONDARY);
                table.getTableHeader().setForeground(AssetStyles.FONT_COLOR);
            }
        } else if (component instanceof javax.swing.JTabbedPane tabbedPane) {
            UiTabbedPaneStyles.style(tabbedPane);
            tabbedPane.updateUI();
        } else if (component instanceof javax.swing.JScrollPane scrollPane) {
            scrollPane.setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
            scrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            if (UiScrollBarStyles.isHidden(scrollPane)) {
                scrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            } else {
                UiScrollBarStyles.style(scrollPane.getVerticalScrollBar());
                scrollPane.getVerticalScrollBar().updateUI();
            }
        } else if (component instanceof javax.swing.JScrollBar scrollBar) {
            if (!UiScrollBarStyles.isHidden(scrollBar)) {
                UiScrollBarStyles.style(scrollBar);
                scrollBar.updateUI();
            }
        } else if (component instanceof javax.swing.JSlider slider) {
            UiSliderStyles.style(slider);
        } else if (component instanceof javax.swing.JSeparator separator) {
            separator.setForeground(AssetStyles.BORDER_COLOR);
            separator.setBackground(AssetStyles.BACKGROUND_COLOR);
        } else if (component instanceof javax.swing.JSplitPane splitPane) {
            splitPane.setBackground(AssetStyles.BACKGROUND_COLOR);
        } else if (component instanceof javax.swing.JPopupMenu popupMenu) {
            popupMenu.setBackground(AssetStyles.BACKGROUND_COLOR);
            popupMenu.setForeground(AssetStyles.FONT_COLOR);
            for (Component child : popupMenu.getComponents()) {
                applyThemeToComponent(child);
            }
        } else if (component instanceof javax.swing.JToolBar toolBar) {
            toolBar.setBackground(AssetStyles.BACKGROUND_SECONDARY);
            toolBar.setForeground(AssetStyles.FONT_COLOR);
        }
    }
}
