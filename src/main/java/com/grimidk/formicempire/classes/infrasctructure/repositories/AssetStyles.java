package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;

public class AssetStyles {
    // --- Palette ---
    public static final Color COLOR_ABSOLUTE_BLACK = new Color(0, 0, 0);
    public static final Color COLOR_ABSOLUTE_WHITE = new Color(255, 255, 255);
    public static final Color COLOR_DARK_GRAY = new Color(67, 67, 67);
    public static final Color COLOR_MEDIUM_GRAY = new Color(103, 103, 103);
    public static final Color COLOR_LIGHT_GRAY = new Color(157, 157, 157);
    public static final Color COLOR_LIGHTER_GRAY = new Color(200, 200, 200);
    public static final Color COLOR_VERY_LIGHT_GRAY = new Color(230, 230, 230);
    public static final Color COLOR_LIGHTEST_GRAY = new Color(245, 245, 245);
    public static final Color COLOR_DARK_RED = new Color(117, 23, 23);
    public static final Color COLOR_MEDIUM_RED = new Color(190, 38, 51);
    public static final Color COLOR_LIGHT_RED = new Color(224, 111, 139);
    public static final Color COLOR_DARK_ORANGE = new Color(73, 60, 43);
    public static final Color COLOR_MEDIUM_ORANGE = new Color(164, 100, 34);
    public static final Color COLOR_LIGHT_ORANGE = new Color(235, 137, 49);
    public static final Color COLOR_LIGHT_YELLOW = new Color(247, 226, 107);
    public static final Color COLOR_MEDIUM_GREEN = new Color(68, 137, 26);
    public static final Color COLOR_LIGHT_GREEN = new Color(163, 206, 39);
    public static final Color COLOR_DARK_TEAL = new Color(47, 72, 78);
    public static final Color COLOR_DARK_BLUE = new Color(27, 38, 50);
    public static final Color COLOR_MEDIUM_BLUE = new Color(0, 87, 132);
    public static final Color COLOR_LIGHT_BLUE = new Color(49, 162, 242);
    public static final Color COLOR_LIGHTEST_BLUE = new Color(178, 220, 239);
    public static final Color COLOR_MEDIUM_PURPLE = new Color(148, 39, 196);
    public static final Color COLOR_LIGHT_PURPLE = new Color(174, 126, 229);

    // --- Overlay Colors ---
    private static final int OVERLAY_ALPHA = 40;
    public static final Color OVERLAY_DAWN = new Color(235, 137, 49, OVERLAY_ALPHA);
    public static final Color OVERLAY_DAY = new Color(255, 255, 255, 0);
    public static final Color OVERLAY_DUSK = new Color(148, 39, 196, OVERLAY_ALPHA);
    public static final Color OVERLAY_NIGHT = new Color(27, 38, 50, OVERLAY_ALPHA + 40);
    public static final Color OVERLAY_SOLAR_ECLIPSE = new Color(0, 0, 0, OVERLAY_ALPHA + 80);
    public static final Color OVERLAY_LUNAR_ECLIPSE = new Color(117, 23, 23, OVERLAY_ALPHA + 40);
    
    public static final Color OVERLAY_CLEAR = new Color(255, 255, 255, 0);
    public static final Color OVERLAY_RAIN = new Color(49, 162, 242, OVERLAY_ALPHA);
    public static final Color OVERLAY_HEAVY_RAIN = new Color(0, 87, 132, OVERLAY_ALPHA + 20);
    public static final Color OVERLAY_SNOW = new Color(255, 255, 255, OVERLAY_ALPHA);
    public static final Color OVERLAY_HEAVY_SNOW = new Color(178, 220, 239, OVERLAY_ALPHA + 20); 
    public static final Color OVERLAY_THUNDER = new Color(67, 67, 67, OVERLAY_ALPHA + 30);
    public static final Color OVERLAY_WIND = new Color(157, 157, 157, OVERLAY_ALPHA);
    public static final Color OVERLAY_HEAT = new Color(247, 226, 107, OVERLAY_ALPHA); 
    public static final Color OVERLAY_FOG = new Color(157, 157, 157, OVERLAY_ALPHA + 40);
    public static final Color OVERLAY_FROG = new Color(68, 137, 26, OVERLAY_ALPHA);
    public static final Color OVERLAY_BLOOD = new Color(190, 38, 51, OVERLAY_ALPHA + 20);
    public static final Color OVERLAY_SANDSTORM = new Color(164, 100, 34, OVERLAY_ALPHA + 30); 
    public static final Color OVERLAY_PYROCLASTIC = new Color(67, 67, 67, OVERLAY_ALPHA + 50);
    public static final Color OVERLAY_ACID_RAIN = new Color(163, 206, 39, OVERLAY_ALPHA + 20);

    // --- Global Style References ---
    public static final Color BACKGROUND_COLOR = COLOR_LIGHTEST_GRAY;
    public static final Color BACKGROUND_SECONDARY = COLOR_VERY_LIGHT_GRAY;
    public static final Color BACKGROUND_DARK = COLOR_LIGHTER_GRAY;
    public static final Color BACKGROUND_LIGHT = COLOR_ABSOLUTE_WHITE;
    
    public static final Color FONT_COLOR = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_BRIGHT = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_HEADER = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_SUCCESS = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_ERROR = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_WARNING = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_HIGHLIGHT = COLOR_ABSOLUTE_BLACK;
    public static final Color FONT_COLOR_VALUE = COLOR_ABSOLUTE_BLACK;
    
    public static final Color BORDER_COLOR = COLOR_ABSOLUTE_BLACK;
    public static final Color PLAYER_COLOR = COLOR_MEDIUM_BLUE;

    public static final Color UI_BG_PRIMARY = BACKGROUND_COLOR;
    public static final Color UI_BG_SECONDARY = BACKGROUND_SECONDARY;
    public static final Color UI_BG_HEADER = BACKGROUND_SECONDARY;
    public static final Color UI_BORDER_COLOR = BORDER_COLOR;
    
    public static final Color TEXT_NORMAL = COLOR_ABSOLUTE_BLACK;
    public static final Color TEXT_HEADER = COLOR_ABSOLUTE_BLACK;
    public static final Color TEXT_SUCCESS = COLOR_ABSOLUTE_BLACK;
    public static final Color TEXT_ERROR = COLOR_ABSOLUTE_BLACK;
    public static final Color TEXT_WARNING = COLOR_ABSOLUTE_BLACK;
    
    public static final Color PLAYER_FACTION = PLAYER_COLOR;

    public static final java.awt.Dimension DEFAULT_DIALOG_SIZE = new java.awt.Dimension(1000, 650);

    public static final int BORDER_THICKNESS_EXTERNAL = 2;
    public static final int BORDER_THICKNESS_INTERNAL = 1;

    public static final Border PANEL_BORDER = BorderFactory.createLineBorder(UI_BORDER_COLOR, BORDER_THICKNESS_EXTERNAL);
    public static final Border INTERNAL_BORDER = BorderFactory.createLineBorder(UI_BORDER_COLOR, BORDER_THICKNESS_INTERNAL);
    
    public static JSeparator createInternalSeparator() {
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setForeground(COLOR_ABSOLUTE_BLACK);
        sep.setBackground(COLOR_ABSOLUTE_BLACK);
        return sep;
    }

    private static Font customFont;

    static {
        try (InputStream is = AssetStyles.class.getResourceAsStream("/fonts/font.ttf")) {
            if (is != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                customFont = new Font("Monospaced", Font.PLAIN, 12);
            }
        } catch (FontFormatException | IOException e) {
            customFont = new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    public static final Font FONT_NORMAL = customFont.deriveFont(12f);
    public static final Font FONT_BOLD = customFont.deriveFont(Font.BOLD, 12f);
    public static final Font FONT_TITLE = customFont.deriveFont(Font.BOLD, 18f);
    public static final Font FONT_SMALL = customFont.deriveFont(10f);

    public static void applyGlobalStyles() {
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("Panel.foreground", FONT_COLOR);
        
        UIManager.put("Label.background", BACKGROUND_COLOR);
        UIManager.put("Label.foreground", FONT_COLOR);
        UIManager.put("Label.font", FONT_NORMAL);
        
        UIManager.put("Button.background", BACKGROUND_SECONDARY);
        UIManager.put("Button.foreground", COLOR_ABSOLUTE_BLACK);
        UIManager.put("Button.font", FONT_BOLD);
        
        UIManager.put("TabbedPane.background", BACKGROUND_SECONDARY);
        UIManager.put("TabbedPane.foreground", FONT_COLOR);
        UIManager.put("TabbedPane.selected", BACKGROUND_COLOR);
        UIManager.put("TabbedPane.font", FONT_BOLD);
        
        UIManager.put("Table.background", BACKGROUND_COLOR);
        UIManager.put("Table.foreground", FONT_COLOR);
        UIManager.put("Table.gridColor", BACKGROUND_SECONDARY);
        UIManager.put("Table.selectionBackground", BACKGROUND_SECONDARY);
        UIManager.put("Table.selectionForeground", FONT_COLOR_HEADER);
        UIManager.put("Table.font", FONT_NORMAL);
        
        UIManager.put("TableHeader.background", BACKGROUND_SECONDARY);
        UIManager.put("TableHeader.foreground", FONT_COLOR_HEADER);
        UIManager.put("TableHeader.font", FONT_BOLD);
        
        UIManager.put("ProgressBar.background", BACKGROUND_DARK);
        UIManager.put("ProgressBar.foreground", FONT_COLOR_SUCCESS);
        UIManager.put("ProgressBar.selectionBackground", FONT_COLOR);
        UIManager.put("ProgressBar.selectionForeground", FONT_COLOR);
        UIManager.put("ProgressBar.font", FONT_SMALL);
        
        UIManager.put("CheckBox.background", BACKGROUND_COLOR);
        UIManager.put("CheckBox.foreground", FONT_COLOR);
        UIManager.put("CheckBox.font", FONT_NORMAL);
        
        UIManager.put("ComboBox.background", BACKGROUND_SECONDARY);
        UIManager.put("ComboBox.foreground", FONT_COLOR);
        UIManager.put("ComboBox.font", FONT_NORMAL);
        
        UIManager.put("TextArea.background", BACKGROUND_COLOR);
        UIManager.put("TextArea.foreground", FONT_COLOR);
        UIManager.put("TextArea.font", FONT_NORMAL);
        
        UIManager.put("List.background", BACKGROUND_COLOR);
        UIManager.put("List.foreground", FONT_COLOR);
        UIManager.put("List.selectionBackground", BACKGROUND_SECONDARY);
        UIManager.put("List.selectionForeground", FONT_COLOR_HEADER);
        UIManager.put("List.font", FONT_NORMAL);

        UIManager.put("SplitPane.background", BACKGROUND_COLOR);
        UIManager.put("SplitPane.dividerSize", 5);

        UIManager.put("ScrollPane.background", BACKGROUND_COLOR);
        UIManager.put("Viewport.background", BACKGROUND_COLOR);
        
        UIManager.put("Separator.foreground", FONT_COLOR);
        UIManager.put("Separator.background", FONT_COLOR);
        UIManager.put("JSeparator.foreground", FONT_COLOR);
        UIManager.put("JSeparator.background", FONT_COLOR);
        
        UIManager.put("TitledBorder.titleColor", FONT_COLOR_HEADER);
        UIManager.put("TitledBorder.font", FONT_BOLD);
        UIManager.put("TitledBorder.border", PANEL_BORDER);
    }

    public static Cursor loadCustomCursor(String path, String name) {
        try {
            URL url = AssetStyles.class.getResource(path);
            if (url != null) {
                Image img = new ImageIcon(url).getImage();
                return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }

    public static Image loadImage(String path) {
        try {
            URL url = AssetStyles.class.getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            } else {
                System.err.println("Resource not found: " + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Color colorFromRgb(int r, int g, int b) {
        return new Color(clampByte(r), clampByte(g), clampByte(b));
    }

    public static Color colorFromRgba(int r, int g, int b, int a) {
        return new Color(clampByte(r), clampByte(g), clampByte(b), clampByte(a));
    }

    public static Color colorFromAveragedRgb(long sumR, long sumG, long sumB, long count) {
        if (count <= 0) {
            return BACKGROUND_COLOR;
        }
        return colorFromRgb((int) (sumR / count), (int) (sumG / count), (int) (sumB / count));
    }

    public static Color lightenTowardBackground(Color c, float amount) {
        Color bg = BACKGROUND_COLOR;
        int r = Math.min(255, (int) (c.getRed() + (bg.getRed() - c.getRed()) * amount));
        int g = Math.min(255, (int) (c.getGreen() + (bg.getGreen() - c.getGreen()) * amount));
        int b = Math.min(255, (int) (c.getBlue() + (bg.getBlue() - c.getBlue()) * amount));
        return colorFromRgba(r, g, b, c.getAlpha());
    }

    public static Color fadeTowardBackground(Color c, float factor) {
        Color bg = BACKGROUND_COLOR;
        int r = (int) (c.getRed() * (1 - factor) + bg.getRed() * factor);
        int g = (int) (c.getGreen() * (1 - factor) + bg.getGreen() * factor);
        int b = (int) (c.getBlue() * (1 - factor) + bg.getBlue() * factor);
        return colorFromRgb(r, g, b);
    }

    private static int clampByte(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
