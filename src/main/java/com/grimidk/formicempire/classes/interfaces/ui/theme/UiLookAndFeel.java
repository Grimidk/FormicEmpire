package com.grimidk.formicempire.classes.interfaces.ui.theme;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.icons.SquareCheckIcons;
import com.grimidk.formicempire.classes.interfaces.ui.icons.SquareRadioIcons;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatComboBoxUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatProgressBarUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatScrollBarUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatSliderUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatSpinnerUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.FlatTabbedPaneUI;
import com.grimidk.formicempire.classes.interfaces.ui.plaf.PanelBorderButtonUI;
import java.awt.Insets;
import javax.swing.UIManager;

import static com.grimidk.formicempire.classes.interfaces.ui.theme.UiFonts.*;
import static com.grimidk.formicempire.classes.interfaces.ui.theme.UiPalette.COLOR_LIGHT_GRAY;

public final class UiLookAndFeel {
    private static boolean lookAndFeelInstalled = false;

    private UiLookAndFeel() {
    }

    public static void applyGlobalStyles() {
        ensureLookAndFeel();
        installFlatComponentUis();

        UIManager.put("Panel.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Panel.foreground", AssetStyles.FONT_COLOR);

        UIManager.put("Label.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Label.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("Label.font", FONT_NORMAL);

        UIManager.put("Button.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Button.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Button.select", AssetStyles.SELECTION_BACKGROUND);
        UIManager.put("Button.disabledText", COLOR_LIGHT_GRAY);
        UIManager.put("Button.focus", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Button.border", AssetStyles.buttonPaddingBorder());
        UIManager.put("Button.margin", new Insets(0, 0, 0, 0));
        UIManager.put("ToggleButton.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("ToggleButton.foreground", AssetStyles.FONT_COLOR);

        UIManager.put("MenuBar.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("MenuBar.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("Menu.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Menu.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("MenuItem.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("MenuItem.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("MenuItem.selectionBackground", AssetStyles.SELECTION_BACKGROUND);
        UIManager.put("MenuItem.selectionForeground", AssetStyles.FONT_COLOR);
        UIManager.put("PopupMenu.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("PopupMenu.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("RadioButton.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("RadioButton.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("RadioButton.font", FONT_NORMAL);
        UIManager.put("RadioButton.icon", SquareRadioIcons.UNSELECTED);
        UIManager.put("RadioButton.selectedIcon", SquareRadioIcons.SELECTED);
        UIManager.put("RadioButton.disabledIcon", SquareRadioIcons.UNSELECTED_DISABLED);
        UIManager.put("RadioButton.disabledSelectedIcon", SquareRadioIcons.SELECTED_DISABLED);
        UIManager.put("Spinner.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("Spinner.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("TextField.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("TextField.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("TextField.caretForeground", AssetStyles.FONT_COLOR);
        UIManager.put("FormattedTextField.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("FormattedTextField.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("PasswordField.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("PasswordField.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("ToolTip.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("ToolTip.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("OptionPane.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("OptionPane.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("OptionPane.messageForeground", AssetStyles.FONT_COLOR);
        UIManager.put("ScrollBar.width", FlatScrollBarUI.VERTICAL_BAR_WIDTH);
        UIManager.put("ScrollBar.background", AssetStyles.BACKGROUND_DARK);
        UIManager.put("ScrollBar.thumb", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("ScrollBar.track", AssetStyles.BACKGROUND_DARK);
        UIManager.put("Slider.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Slider.foreground", AssetStyles.FONT_COLOR_HEADER);
        UIManager.put("Slider.verticalSize", FlatSliderUI.VERTICAL_SIZE);
        UIManager.put("Slider.minimumVerticalSize", FlatSliderUI.VERTICAL_SIZE);
        UIManager.put("Tree.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Tree.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("Tree.selectionBackground", AssetStyles.SELECTION_BACKGROUND);
        UIManager.put("Tree.selectionForeground", AssetStyles.FONT_COLOR_HEADER);
        UIManager.put("InternalFrame.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Desktop.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("control", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("text", AssetStyles.FONT_COLOR);

        UIManager.put("TabbedPane.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("TabbedPane.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("TabbedPane.selected", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("TabbedPane.unselectedBackground", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("TabbedPane.selectedForeground", AssetStyles.FONT_COLOR);
        UIManager.put("TabbedPane.font", FONT_BOLD);

        UIManager.put("Table.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Table.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("Table.gridColor", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("Table.selectionBackground", AssetStyles.SELECTION_BACKGROUND);
        UIManager.put("Table.selectionForeground", AssetStyles.FONT_COLOR_HEADER);
        UIManager.put("Table.font", FONT_NORMAL);

        UIManager.put("TableHeader.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("TableHeader.foreground", AssetStyles.FONT_COLOR_HEADER);
        UIManager.put("TableHeader.font", FONT_BOLD);

        UIManager.put("ProgressBar.background", AssetStyles.BACKGROUND_DARK);
        UIManager.put("ProgressBar.foreground", AssetStyles.FONT_COLOR_SUCCESS);
        UIManager.put("ProgressBar.selectionBackground", AssetStyles.FONT_COLOR);
        UIManager.put("ProgressBar.selectionForeground", AssetStyles.FONT_COLOR);
        UIManager.put("ProgressBar.font", FONT_SMALL);
        UIManager.put("ProgressBar.border", AssetStyles.INTERNAL_BORDER);

        UIManager.put("CheckBox.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("CheckBox.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("CheckBox.font", FONT_NORMAL);
        UIManager.put("CheckBox.icon", SquareCheckIcons.UNCHECKED);
        UIManager.put("CheckBox.selectedIcon", SquareCheckIcons.CHECKED);
        UIManager.put("CheckBox.disabledIcon", SquareCheckIcons.UNCHECKED_DISABLED);
        UIManager.put("CheckBox.disabledSelectedIcon", SquareCheckIcons.CHECKED_DISABLED);

        UIManager.put("ComboBox.background", AssetStyles.BACKGROUND_SECONDARY);
        UIManager.put("ComboBox.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("ComboBox.font", FONT_NORMAL);
        UIManager.put("ComboBox.selectionBackground", AssetStyles.SELECTION_BACKGROUND);
        UIManager.put("ComboBox.selectionForeground", AssetStyles.FONT_COLOR);
        UIManager.put("ComboBox.listBackground", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("ComboBox.listForeground", AssetStyles.FONT_COLOR);

        UIManager.put("TextArea.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("TextArea.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("TextArea.font", FONT_NORMAL);

        UIManager.put("List.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("List.foreground", AssetStyles.FONT_COLOR);
        UIManager.put("List.selectionBackground", AssetStyles.SELECTION_BACKGROUND);
        UIManager.put("List.selectionForeground", AssetStyles.FONT_COLOR_HEADER);
        UIManager.put("List.font", FONT_NORMAL);

        UIManager.put("SplitPane.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("SplitPane.dividerSize", 5);

        UIManager.put("ScrollPane.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("Viewport.background", AssetStyles.BACKGROUND_COLOR);

        UIManager.put("Separator.foreground", AssetStyles.BORDER_COLOR);
        UIManager.put("Separator.background", AssetStyles.BACKGROUND_COLOR);
        UIManager.put("JSeparator.foreground", AssetStyles.BORDER_COLOR);
        UIManager.put("JSeparator.background", AssetStyles.BACKGROUND_COLOR);

        UIManager.put("TitledBorder.titleColor", AssetStyles.FONT_COLOR_HEADER);
        UIManager.put("TitledBorder.font", FONT_BOLD);
        UIManager.put("TitledBorder.border", AssetStyles.PANEL_BORDER);
    }

    private static void installFlatComponentUis() {
        UIManager.put("ButtonUI", PanelBorderButtonUI.class.getName());
        UIManager.put("ToggleButtonUI", PanelBorderButtonUI.class.getName());
        UIManager.put("ScrollBarUI", FlatScrollBarUI.class.getName());
        UIManager.put("ProgressBarUI", FlatProgressBarUI.class.getName());
        UIManager.put("SliderUI", FlatSliderUI.class.getName());
        UIManager.put("ComboBoxUI", FlatComboBoxUI.class.getName());
        UIManager.put("TabbedPaneUI", FlatTabbedPaneUI.class.getName());
        UIManager.put("SpinnerUI", FlatSpinnerUI.class.getName());
    }

    private static void ensureLookAndFeel() {
        if (lookAndFeelInstalled) {
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            lookAndFeelInstalled = true;
        } catch (Exception ignored) {
        }
    }
}
