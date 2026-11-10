package com.grimidk.formicempire.classes.interfaces.ui.theme;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class UiFonts {
    private static final String FONT_RESOURCE = "/meta/fonts/font.ttf";
    private static final Font customFont;

    static {
        Font loaded = null;
        try (InputStream is = UiFonts.class.getResourceAsStream(FONT_RESOURCE)) {
            if (is != null) {
                loaded = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loaded);
            }
        } catch (FontFormatException | IOException ignored) {
        }
        if (loaded == null) {
            loaded = resolveDefaultFont();
        }
        customFont = loaded;
    }

    public static final Font FONT_NORMAL = customFont.deriveFont(12f);
    public static final Font FONT_BOLD = customFont.deriveFont(Font.BOLD, 12f);
    public static final Font FONT_TITLE = customFont.deriveFont(Font.BOLD, 18f);
    public static final Font FONT_SMALL = customFont.deriveFont(10f);
    public static final Font FONT_MONOSPACED = customFont.deriveFont(12f);

    private UiFonts() {
    }

    private static Font resolveDefaultFont() {
        String[] preferredFamilies = {
            "Menlo",
            "Consolas",
            "Cascadia Mono",
            "Cascadia Code",
            "DejaVu Sans Mono",
            "Liberation Mono",
            "Monaco"
        };
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFamilies = ge.getAvailableFontFamilyNames();
            Set<String> familySet = new HashSet<>(availableFamilies.length);
            for (String family : availableFamilies) {
                familySet.add(family.toLowerCase(Locale.ROOT));
            }
            for (String candidate : preferredFamilies) {
                if (familySet.contains(candidate.toLowerCase(Locale.ROOT))) {
                    return new Font(candidate, Font.PLAIN, 12);
                }
            }
        } catch (Exception ignored) {
        }
        return new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }
}
