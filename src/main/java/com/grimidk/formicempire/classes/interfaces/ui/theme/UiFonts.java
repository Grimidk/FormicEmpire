package com.grimidk.formicempire.classes.interfaces.ui.theme;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/** Loads and exposes UI fonts from classpath `/fonts/font.ttf`. */
public final class UiFonts {
    private static final Font customFont;

    static {
        Font loaded = null;
        try (InputStream is = UiFonts.class.getResourceAsStream("/fonts/font.ttf")) {
            if (is != null) {
                loaded = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (FontFormatException | IOException ignored) {
        }
        if (loaded == null) {
            loaded = new Font("Monospaced", Font.PLAIN, 12);
        }
        customFont = loaded;
    }

    public static final Font FONT_NORMAL = customFont.deriveFont(12f);
    public static final Font FONT_BOLD = customFont.deriveFont(Font.BOLD, 12f);
    public static final Font FONT_TITLE = customFont.deriveFont(Font.BOLD, 18f);
    public static final Font FONT_SMALL = customFont.deriveFont(10f);
    public static final Font FONT_MONOSPACED = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private UiFonts() {
    }
}
