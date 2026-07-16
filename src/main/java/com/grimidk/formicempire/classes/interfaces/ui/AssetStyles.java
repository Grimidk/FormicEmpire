package com.grimidk.formicempire.classes.interfaces.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiColorUtils;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiFonts;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiLookAndFeel;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiPalette;
import com.grimidk.formicempire.classes.interfaces.ui.theme.UiTheme;
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
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableStyles;
import com.grimidk.formicempire.classes.interfaces.ui.styles.UiTableBooleanStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiNumberFormat;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiResourceLoader;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


public class AssetStyles {
    public static final String META_CURSOR_NORMAL = "/meta/ui/CursorNormal.png";
    public static final String META_CURSOR_CLICK = "/meta/ui/CursorClick.png";
    public static final String META_APP_ICON = "/meta/icon.ico";
    public static final String META_DIALOG_ICON = "/meta/icon.iconset/icon_32x32@2x.png";
    public static final String META_FONT = "/meta/fonts/font.ttf";
    public static final String META_ROADMAP = "/meta/roadmap.txt";
    public static final String META_CREDITS = "/meta/credits.txt";
    public static final String META_AUDIT = "/meta/audit.txt";

    public static final String ICON_BUTTON_CLIENT_KEY = "formicempire.iconButton";

    // --- Palette ---
    public static final Color COLOR_ABSOLUTE_BLACK = UiPalette.COLOR_ABSOLUTE_BLACK;
    public static final Color COLOR_ABSOLUTE_WHITE = UiPalette.COLOR_ABSOLUTE_WHITE;
    public static final String COLOR_ABSOLUTE_WHITE_HTML = UiPalette.COLOR_ABSOLUTE_WHITE_HTML;
    public static final Color COLOR_DARK_GRAY = UiPalette.COLOR_DARK_GRAY;
    public static final Color COLOR_MEDIUM_GRAY = UiPalette.COLOR_MEDIUM_GRAY;
    public static final Color COLOR_LIGHT_GRAY = UiPalette.COLOR_LIGHT_GRAY;
    public static final Color COLOR_LIGHTER_GRAY = UiPalette.COLOR_LIGHTER_GRAY;
    public static final Color COLOR_VERY_LIGHT_GRAY = UiPalette.COLOR_VERY_LIGHT_GRAY;
    public static final Color COLOR_LIGHTEST_GRAY = UiPalette.COLOR_LIGHTEST_GRAY;
    public static final Color COLOR_DARK_RED = UiPalette.COLOR_DARK_RED;
    public static final Color COLOR_MEDIUM_RED = UiPalette.COLOR_MEDIUM_RED;
    public static final Color COLOR_LIGHT_RED = UiPalette.COLOR_LIGHT_RED;
    public static final Color COLOR_DARK_ORANGE = UiPalette.COLOR_DARK_ORANGE;
    public static final Color COLOR_MEDIUM_ORANGE = UiPalette.COLOR_MEDIUM_ORANGE;
    public static final Color COLOR_LIGHT_ORANGE = UiPalette.COLOR_LIGHT_ORANGE;
    public static final Color COLOR_LIGHT_YELLOW = UiPalette.COLOR_LIGHT_YELLOW;
    public static final Color COLOR_MEDIUM_GREEN = UiPalette.COLOR_MEDIUM_GREEN;
    public static final Color COLOR_LIGHT_GREEN = UiPalette.COLOR_LIGHT_GREEN;
    public static final Color COLOR_DARK_TEAL = UiPalette.COLOR_DARK_TEAL;
    public static final Color COLOR_DARK_BLUE = UiPalette.COLOR_DARK_BLUE;
    public static final Color COLOR_NEAR_BLACK = UiPalette.COLOR_NEAR_BLACK;
    public static final Color COLOR_MEDIUM_BLUE = UiPalette.COLOR_MEDIUM_BLUE;
    public static final Color COLOR_LIGHT_BLUE = UiPalette.COLOR_LIGHT_BLUE;
    public static final Color COLOR_LIGHTEST_BLUE = UiPalette.COLOR_LIGHTEST_BLUE;
    public static final Color COLOR_MEDIUM_PURPLE = UiPalette.COLOR_MEDIUM_PURPLE;
    public static final Color COLOR_LIGHT_PURPLE = UiPalette.COLOR_LIGHT_PURPLE;

    public static final Color OVERLAY_DAWN = UiPalette.OVERLAY_DAWN;
    public static final Color OVERLAY_DAY = UiPalette.OVERLAY_DAY;
    public static final Color OVERLAY_DUSK = UiPalette.OVERLAY_DUSK;
    public static final Color OVERLAY_NIGHT = UiPalette.OVERLAY_NIGHT;
    public static final Color OVERLAY_SOLAR_ECLIPSE = UiPalette.OVERLAY_SOLAR_ECLIPSE;
    public static final Color OVERLAY_LUNAR_ECLIPSE = UiPalette.OVERLAY_LUNAR_ECLIPSE;
    public static final Color OVERLAY_CLEAR = UiPalette.OVERLAY_CLEAR;
    public static final Color OVERLAY_RAIN = UiPalette.OVERLAY_RAIN;
    public static final Color OVERLAY_HEAVY_RAIN = UiPalette.OVERLAY_HEAVY_RAIN;
    public static final Color OVERLAY_SNOW = UiPalette.OVERLAY_SNOW;
    public static final Color OVERLAY_HEAVY_SNOW = UiPalette.OVERLAY_HEAVY_SNOW;
    public static final Color OVERLAY_THUNDER = UiPalette.OVERLAY_THUNDER;
    public static final Color OVERLAY_WIND = UiPalette.OVERLAY_WIND;
    public static final Color OVERLAY_HEAT = UiPalette.OVERLAY_HEAT;
    public static final Color OVERLAY_FOG = UiPalette.OVERLAY_FOG;
    public static final Color OVERLAY_FROG = UiPalette.OVERLAY_FROG;
    public static final Color OVERLAY_BLOOD = UiPalette.OVERLAY_BLOOD;
    public static final Color OVERLAY_SANDSTORM = UiPalette.OVERLAY_SANDSTORM;
    public static final Color OVERLAY_PYROCLASTIC = UiPalette.OVERLAY_PYROCLASTIC;
    public static final Color OVERLAY_ACID_RAIN = UiPalette.OVERLAY_ACID_RAIN;

    public static final Color PLAYER_COLOR = UiPalette.PLAYER_COLOR;
    public static final Color PLAYER_FACTION = UiPalette.PLAYER_FACTION;

    // --- Theme  ---
    public static Color BACKGROUND_COLOR = COLOR_LIGHTEST_GRAY;
    public static Color BACKGROUND_SECONDARY = COLOR_VERY_LIGHT_GRAY;
    public static Color BACKGROUND_DARK = COLOR_LIGHTER_GRAY;
    public static Color BACKGROUND_LIGHT = COLOR_ABSOLUTE_WHITE;
    public static float MAP_HEX_INACTIVE_FADE = 0.4f;

    public static Color FONT_COLOR = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_BRIGHT = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_HEADER = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_SUCCESS = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_ERROR = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_WARNING = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_HIGHLIGHT = COLOR_ABSOLUTE_BLACK;
    public static Color FONT_COLOR_VALUE = COLOR_ABSOLUTE_BLACK;

    public static Color BORDER_COLOR = COLOR_ABSOLUTE_BLACK;
    public static Color UI_BG_PRIMARY = BACKGROUND_COLOR;
    public static Color UI_BG_SECONDARY = BACKGROUND_SECONDARY;
    public static Color UI_BG_HEADER = BACKGROUND_SECONDARY;
    public static Color UI_BORDER_COLOR = BORDER_COLOR;

    public static Color TAB_SELECTED_BG = BACKGROUND_LIGHT;
    public static Color TAB_UNSELECTED_BG = BACKGROUND_DARK;

    public static Color TEXT_NORMAL = COLOR_ABSOLUTE_BLACK;
    public static Color TEXT_HEADER = COLOR_ABSOLUTE_BLACK;
    public static Color TEXT_SUCCESS = COLOR_ABSOLUTE_BLACK;
    public static Color TEXT_ERROR = COLOR_ABSOLUTE_BLACK;
    public static Color TEXT_WARNING = COLOR_ABSOLUTE_BLACK;

    public static Color SELECTION_BACKGROUND = COLOR_LIGHTER_GRAY;

    public static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(1150, 720);
    public static final Dimension DYNASTY_DIALOG_SIZE = new Dimension(1400, 860);
    public static final Dimension MAP_DIALOG_SIZE = new Dimension(1280, 820);

    public static final int BORDER_THICKNESS_EXTERNAL = 2;
    public static final int BORDER_THICKNESS_INTERNAL = 1;
    public static final int BORDER_THICKNESS_BUTTON = BORDER_THICKNESS_INTERNAL;

    public static Border PANEL_BORDER = BorderFactory.createLineBorder(UI_BORDER_COLOR, BORDER_THICKNESS_EXTERNAL);
    public static Border INTERNAL_BORDER = BorderFactory.createLineBorder(UI_BORDER_COLOR, BORDER_THICKNESS_INTERNAL);
    public static Border BUTTON_BORDER = BorderFactory.createLineBorder(UI_BORDER_COLOR, BORDER_THICKNESS_BUTTON);

    public static final Insets BUTTON_MARGIN_INSETS = new Insets(12, 28, 12, 28);
    public static final Insets BUTTON_COMPACT_MARGIN_INSETS = new Insets(5, 12, 5, 12);

    public static final Insets TAB_MARGIN_INSETS = new Insets(4, 10, 4, 10);

    public static final int TAB_STRIP_WIDTH = 110;

    public static final int TAB_STRIP_HEIGHT;

    static {
        FontMetrics tabMetrics = Toolkit.getDefaultToolkit().getFontMetrics(UiFonts.FONT_BOLD);
        TAB_STRIP_HEIGHT = TAB_MARGIN_INSETS.top + tabMetrics.getHeight() + TAB_MARGIN_INSETS.bottom;
    }

    public static Dimension tabStripSize() {
        return new Dimension(TAB_STRIP_WIDTH, TAB_STRIP_HEIGHT);
    }

    public static String themeTextColorHtml() {
        return colorToHtml(FONT_COLOR);
    }

    public static String themeFontFamilyCss() {
        return FONT_NORMAL.getFamily();
    }

    public static String colorToHtml(Color color) {
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    public static Border buttonPaddingBorder() {
        return BorderFactory.createCompoundBorder(
                BUTTON_BORDER,
                new EmptyBorder(BUTTON_MARGIN_INSETS));
    }

    public static Border buttonCompactPaddingBorder() {
        return BorderFactory.createCompoundBorder(
                BUTTON_BORDER,
                new EmptyBorder(BUTTON_COMPACT_MARGIN_INSETS));
    }

    public static final Font FONT_NORMAL = UiFonts.FONT_NORMAL;
    public static final Font FONT_BOLD = UiFonts.FONT_BOLD;
    public static final Font FONT_TITLE = UiFonts.FONT_TITLE;
    public static final Font FONT_SMALL = UiFonts.FONT_SMALL;
    public static final Font FONT_MONOSPACED = UiFonts.FONT_MONOSPACED;

    private static boolean darkMode = false;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static float mapInactiveHexFade() {
        return MAP_HEX_INACTIVE_FADE;
    }

    public static void applyTheme(boolean dark) {
        UiTheme.applyTheme(dark);
    }

    public static JSeparator createInternalSeparator() {
        return UiTheme.createInternalSeparator();
    }

    public static void applyThemeToContainer(Container root) {
        UiTheme.applyThemeToContainer(root);
    }

    public static void styleMenuItem(JMenuItem menuItem) {
        UiMenuStyles.style(menuItem);
    }

    public static void styleButton(AbstractButton button) {
        UiButtonStyles.style(button);
    }

    public static void styleCompactButton(AbstractButton button) {
        UiButtonStyles.styleCompact(button);
    }

    public static void styleIconButton(AbstractButton button) {
        UiButtonStyles.styleIcon(button);
    }

    public static void styleSectionTabButton(AbstractButton button) {
        UiButtonStyles.styleSectionTab(button);
    }

    public static void styleCheckBox(JCheckBox box) {
        UiCheckBoxStyles.style(box);
    }

    public static void styleRadioButton(JRadioButton button) {
        UiRadioButtonStyles.style(button);
    }

    public static void styleComboBox(JComboBox<?> box) {
        UiComboBoxStyles.style(box);
    }

    public static void styleSpinner(JSpinner spinner) {
        UiSpinnerStyles.style(spinner);
    }

    public static void styleTextField(javax.swing.text.JTextComponent field) {
        UiTextFieldStyles.style(field);
    }

    public static void styleProgressBar(JProgressBar bar) {
        UiProgressBarStyles.style(bar);
    }

    public static void styleSlider(JSlider slider) {
        UiSliderStyles.style(slider);
    }

    public static void styleScrollBar(javax.swing.JScrollBar scrollBar) {
        UiScrollBarStyles.style(scrollBar);
    }

    public static void styleScrollPane(javax.swing.JScrollPane scrollPane) {
        UiScrollBarStyles.style(scrollPane);
    }

    public static void styleTabbedPane(JTabbedPane tabbedPane) {
        UiTabbedPaneStyles.style(tabbedPane);
        if (tabbedPane == null) {
            return;
        }
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public static void applyTabSelection(AbstractButton button, boolean selected) {
        UiButtonStyles.applyTabSelection(button, selected);
    }

    public static void styleDialogTable(JTable table) {
        UiTableStyles.applyDialogTable(table);
    }

    public static String formatNumber(int value) {
        return UiNumberFormat.format(value);
    }

    public static String formatNumber(long value) {
        return UiNumberFormat.format(value);
    }

    public static String formatNumber(float value) {
        return UiNumberFormat.format(value);
    }

    public static String formatNumber(double value) {
        return UiNumberFormat.format(value);
    }

    public static String formatNumber(Number value) {
        return UiNumberFormat.format(value);
    }

    public static String formatSignedNumber(int value) {
        return UiNumberFormat.formatSigned(value);
    }

    public static String formatRatio(long current, long max) {
        return UiNumberFormat.formatRatio(current, max);
    }

    public static String formatRatio(int current, int max) {
        return UiNumberFormat.formatRatio(current, max);
    }

    public static void fitTableColumns(JTable table) {
        UiTableStyles.fitColumns(table);
    }

    public static void fitTableColumn(JTable table, int columnIndex, int minWidth, int maxWidth) {
        UiTableStyles.fitColumn(table, columnIndex, minWidth, maxWidth, 16);
    }

    public static void applyScrollableDialogTable(JTable table) {
        UiTableStyles.applyScrollableDialogTable(table);
    }

    public static void layoutTableColumnsForViewport(JTable table, int viewportWidth, boolean[] growableColumns) {
        UiTableStyles.layoutColumnsForViewport(table, viewportWidth, growableColumns);
    }

    public static void applyTableColumnAlignment(JTable table, int columnIndex, int alignment) {
        UiTableStyles.applyColumnAlignment(table, columnIndex, alignment);
    }

    public static void applyTableHeaderAlignment(JTable table, int columnIndex, int alignment) {
        UiTableStyles.applyHeaderAlignment(table, columnIndex, alignment);
    }

    public static JScrollPane wrapScrollableTable(JTable table) {
        return UiTableStyles.wrapScrollableTable(table);
    }

    public static void relayoutTableInScrollPane(JScrollPane scrollPane, boolean[] growableColumns) {
        UiTableStyles.relayoutTableInScrollPane(scrollPane, growableColumns);
    }

    public static void styleTableBooleanColumn(JTable table, int columnIndex) {
        UiTableBooleanStyles.applyBooleanColumn(table, columnIndex);
    }

    public static void applyGlobalStyles() {
        UiLookAndFeel.applyGlobalStyles();
    }

    public static java.awt.Cursor loadCustomCursor(String path, String name) {
        return UiResourceLoader.loadCustomCursor(AssetStyles.class, path, name);
    }

    public static java.awt.Image loadImage(String path) {
        return UiResourceLoader.loadImage(AssetStyles.class, path);
    }

    public static Color colorFromRgb(int r, int g, int b) {
        return UiColorUtils.colorFromRgb(r, g, b);
    }

    public static Color colorFromRgba(int r, int g, int b, int a) {
        return UiColorUtils.colorFromRgba(r, g, b, a);
    }

    public static Color colorFromAveragedRgb(long sumR, long sumG, long sumB, long count) {
        return UiColorUtils.colorFromAveragedRgb(sumR, sumG, sumB, count, BACKGROUND_COLOR);
    }

    public static Color lightenTowardBackground(Color c, float amount) {
        return UiColorUtils.lightenTowardBackground(c, amount, BACKGROUND_COLOR);
    }

    public static Color fadeTowardBackground(Color c, float factor) {
        return UiColorUtils.fadeTowardBackground(c, factor, BACKGROUND_COLOR);
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    static boolean isThemeExempt(Component component) {
        return UiTheme.isThemeExempt(component);
    }
}
