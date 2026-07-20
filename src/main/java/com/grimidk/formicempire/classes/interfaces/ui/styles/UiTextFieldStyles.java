package com.grimidk.formicempire.classes.interfaces.ui.styles;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

public final class UiTextFieldStyles {
    public static final int MIN_WIDTH = 148;
    public static final int MIN_HEIGHT = 28;
    public static final Insets FIELD_MARGIN = new Insets(4, 8, 4, 8);

    private UiTextFieldStyles() {
    }

    public static void style(JTextComponent field) {
        if (field == null) {
            return;
        }
        field.setFont(AssetStyles.FONT_NORMAL);
        field.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        field.setForeground(AssetStyles.FONT_COLOR);
        field.setCaretColor(AssetStyles.FONT_COLOR);
        field.setBorder(AssetStyles.INTERNAL_BORDER);
        field.setCursor(field.isEditable() ? AssetStyles.cursorWriteable() : AssetStyles.cursorNormal());
        applyMinimumSize(field);
    }

    public static void applyMinimumSize(JComponent field) {
        if (field == null) {
            return;
        }
        FontMetrics metrics = field.getFontMetrics(field.getFont());
        int height = Math.max(MIN_HEIGHT, FIELD_MARGIN.top + FIELD_MARGIN.bottom + metrics.getHeight());
        Dimension size = new Dimension(MIN_WIDTH, height);
        field.setMinimumSize(size);
        if (field.getPreferredSize().width < MIN_WIDTH || field.getPreferredSize().height < height) {
            field.setPreferredSize(new Dimension(
                    Math.max(MIN_WIDTH, field.getPreferredSize().width),
                    Math.max(height, field.getPreferredSize().height)));
        }
    }
}
