package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class ZeroGamePanel extends JPanel {

    private String mainTitleKey;
    private final Map<JPanel, String> titledPanels = new HashMap<>();

    public ZeroGamePanel(LayoutManager layout) {
        super(layout != null ? layout : new FlowLayout());
        setBackground(AssetStyles.UI_BG_PRIMARY);
        setForeground(AssetStyles.TEXT_NORMAL);
    }

    protected abstract void initComponents();

    protected abstract void initLayout();

    public void refreshTranslations() {
        if (mainTitleKey != null) {
            updateTitledBorder(this, mainTitleKey);
        }
        for (Map.Entry<JPanel, String> entry : titledPanels.entrySet()) {
            updateTitledBorder(entry.getKey(), entry.getValue());
        }
    }

    public void refreshTheme() {
        setBackground(AssetStyles.UI_BG_PRIMARY);
        setForeground(AssetStyles.TEXT_NORMAL);
        refreshTranslations();
        AssetStyles.applyThemeToContainer(this);
    }

    protected void setTitledBorder(String key) {
        this.mainTitleKey = key;
        updateTitledBorder(this, key);
    }
    
    private void updateTitledBorder(JPanel panel, String key) {
        TitledBorder border = BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, LanguageStrings.get(key));
        border.setTitleColor(AssetStyles.TEXT_HEADER);
        border.setTitleFont(AssetStyles.FONT_BOLD);
        panel.setBorder(border);
    }
    
    protected JPanel createTitledPanel(String key, LayoutManager layout) {
        JPanel panel = new JPanel(layout != null ? layout : new FlowLayout());
        panel.setBackground(AssetStyles.UI_BG_PRIMARY);
        titledPanels.put(panel, key);
        updateTitledBorder(panel, key);
        return panel;
    }
}