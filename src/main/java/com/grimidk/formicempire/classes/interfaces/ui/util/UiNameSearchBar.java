package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.awt.FlowLayout;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class UiNameSearchBar extends JPanel {
    public static final int DEFAULT_COLUMNS = 18;

    private final JLabel label;
    private final JTextField field;
    private Runnable onQueryChanged;

    public UiNameSearchBar(String labelText, String tooltipText, Runnable onQueryChanged) {
        this(labelText, tooltipText, DEFAULT_COLUMNS, onQueryChanged);
    }

    public UiNameSearchBar(String labelText, String tooltipText, int columns, Runnable onQueryChanged) {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setOpaque(false);
        this.onQueryChanged = onQueryChanged;

        label = new JLabel(labelText != null ? labelText : "");
        label.setForeground(AssetStyles.FONT_COLOR);

        field = new JTextField(Math.max(1, columns));
        AssetStyles.styleTextField(field);
        if (tooltipText != null && !tooltipText.isEmpty()) {
            field.setToolTipText(tooltipText);
            label.setToolTipText(tooltipText);
        }
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                fireQueryChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                fireQueryChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                fireQueryChanged();
            }
        });

        add(label);
        add(field);
    }

    public void setOnQueryChanged(Runnable onQueryChanged) {
        this.onQueryChanged = onQueryChanged;
    }

    public JTextField getField() {
        return field;
    }

    public String getRawQuery() {
        return field.getText();
    }

    public String getNormalizedQuery() {
        return normalizeQuery(field.getText());
    }

    public boolean isBlank() {
        return getNormalizedQuery().isEmpty();
    }

    public boolean matchesAny(String... candidates) {
        return matches(getNormalizedQuery(), candidates);
    }

    public boolean matchesAny(Iterable<String> candidates) {
        return matches(getNormalizedQuery(), candidates);
    }

    public void refreshTheme() {
        label.setForeground(AssetStyles.FONT_COLOR);
        AssetStyles.styleTextField(field);
    }

    public static String normalizeQuery(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean matches(String normalizedQuery, String... candidates) {
        if (normalizedQuery == null || normalizedQuery.isEmpty()) {
            return true;
        }
        if (candidates == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(String normalizedQuery, Iterable<String> candidates) {
        if (normalizedQuery == null || normalizedQuery.isEmpty()) {
            return true;
        }
        if (candidates == null) {
            return false;
        }
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    private void fireQueryChanged() {
        if (onQueryChanged != null) {
            onQueryChanged.run();
        }
    }
}
