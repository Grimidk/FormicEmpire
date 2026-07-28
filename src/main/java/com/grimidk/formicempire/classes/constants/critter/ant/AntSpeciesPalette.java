package com.grimidk.formicempire.classes.constants.critter.ant;

public final class AntSpeciesPalette {
    private final int headColor;
    private final int torsoColor;
    private final int abdomenColor;
    private final int wingPrimaryColor;
    private final int wingSecondaryColor;
    private final int droneColor;
    private final int droneWingPrimaryColor;
    private final int droneWingSecondaryColor;
    private final int honeypotColor;

    private final String headColorHex;
    private final String torsoColorHex;
    private final String abdomenColorHex;
    private final String wingPrimaryColorHex;
    private final String wingSecondaryColorHex;
    private final String droneColorHex;
    private final String droneWingPrimaryColorHex;
    private final String droneWingSecondaryColorHex;
    private final String honeypotColorHex;

    public AntSpeciesPalette(
            String headColor,
            String torsoColor,
            String abdomenColor,
            String wingPrimaryColor,
            String wingSecondaryColor,
            String droneColor,
            String droneWingPrimaryColor,
            String droneWingSecondaryColor,
            String honeypotColor) {
        this.headColorHex = normalizeHex(headColor);
        this.torsoColorHex = normalizeHex(torsoColor);
        this.abdomenColorHex = normalizeHex(abdomenColor);
        this.wingPrimaryColorHex = normalizeHex(wingPrimaryColor);
        this.wingSecondaryColorHex = normalizeHex(wingSecondaryColor);
        this.droneColorHex = normalizeHex(droneColor);
        this.droneWingPrimaryColorHex = normalizeHex(droneWingPrimaryColor);
        this.droneWingSecondaryColorHex = normalizeHex(droneWingSecondaryColor);
        this.honeypotColorHex = normalizeHex(honeypotColor);

        this.headColor = parseHex(this.headColorHex);
        this.torsoColor = parseHex(this.torsoColorHex);
        this.abdomenColor = parseHex(this.abdomenColorHex);
        this.wingPrimaryColor = parseHex(this.wingPrimaryColorHex);
        this.wingSecondaryColor = parseHex(this.wingSecondaryColorHex);
        this.droneColor = parseHex(this.droneColorHex);
        this.droneWingPrimaryColor = parseHex(this.droneWingPrimaryColorHex);
        this.droneWingSecondaryColor = parseHex(this.droneWingSecondaryColorHex);
        this.honeypotColor = parseHex(this.honeypotColorHex);
    }

    public static String normalizeHex(String hex) {
        if (hex == null || hex.isBlank()) {
            return "000000";
        }
        String h = hex.trim();
        if (h.startsWith("#") || h.startsWith("0x") || h.startsWith("0X")) {
            h = h.startsWith("#") ? h.substring(1) : h.substring(2);
        }
        if (h.length() != 6) {
            throw new IllegalArgumentException("Expected RRGGBB hex color, got: " + hex);
        }
        return h.toLowerCase();
    }

    public static int parseHex(String hex) {
        return Integer.parseInt(normalizeHex(hex), 16) & 0xFFFFFF;
    }

    public int getHeadColor() {
        return headColor;
    }

    public int getTorsoColor() {
        return torsoColor;
    }

    public int getAbdomenColor() {
        return abdomenColor;
    }

    public int getWingPrimaryColor() {
        return wingPrimaryColor;
    }

    public int getWingSecondaryColor() {
        return wingSecondaryColor;
    }

    public int getDroneColor() {
        return droneColor;
    }

    public int getDroneWingPrimaryColor() {
        return droneWingPrimaryColor;
    }

    public int getDroneWingSecondaryColor() {
        return droneWingSecondaryColor;
    }

    public int getHoneypotColor() {
        return honeypotColor;
    }

    public String getHeadColorHex() {
        return headColorHex;
    }

    public String getTorsoColorHex() {
        return torsoColorHex;
    }

    public String getAbdomenColorHex() {
        return abdomenColorHex;
    }

    public String getWingPrimaryColorHex() {
        return wingPrimaryColorHex;
    }

    public String getWingSecondaryColorHex() {
        return wingSecondaryColorHex;
    }

    public String getDroneColorHex() {
        return droneColorHex;
    }

    public String getDroneWingPrimaryColorHex() {
        return droneWingPrimaryColorHex;
    }

    public String getDroneWingSecondaryColorHex() {
        return droneWingSecondaryColorHex;
    }

    public String getHoneypotColorHex() {
        return honeypotColorHex;
    }
}
