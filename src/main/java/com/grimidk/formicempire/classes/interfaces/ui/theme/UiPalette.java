package com.grimidk.formicempire.classes.interfaces.ui.theme;

import java.awt.Color;

public final class UiPalette {
    public static final Color COLOR_ABSOLUTE_BLACK = new Color(0, 0, 0);
    public static final Color COLOR_ABSOLUTE_WHITE = new Color(255, 255, 255);
    public static final String COLOR_ABSOLUTE_WHITE_HTML = "#FFFFFF";
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
    public static final Color COLOR_NEAR_BLACK = new Color(18, 18, 18);
    public static final Color COLOR_MEDIUM_BLUE = new Color(0, 87, 132);
    public static final Color COLOR_LIGHT_BLUE = new Color(49, 162, 242);
    public static final Color COLOR_LIGHTEST_BLUE = new Color(178, 220, 239);
    public static final Color COLOR_MEDIUM_PURPLE = new Color(148, 39, 196);
    public static final Color COLOR_LIGHT_PURPLE = new Color(174, 126, 229);

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

    public static final Color PLAYER_COLOR = COLOR_MEDIUM_BLUE;
    public static final Color PLAYER_FACTION = PLAYER_COLOR;

    private UiPalette() {
    }
}
