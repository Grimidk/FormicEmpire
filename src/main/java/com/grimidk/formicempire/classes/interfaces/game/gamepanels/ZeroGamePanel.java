package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public abstract class ZeroGamePanel extends JPanel {

    public ZeroGamePanel(LayoutManager layout) {
        super(layout != null ? layout : new FlowLayout());
        setBackground(AssetStyles.UI_BG_PRIMARY);
        setForeground(AssetStyles.TEXT_NORMAL);
    }

    protected abstract void initComponents();

    protected abstract void initLayout();

    protected void setTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, title);
        border.setTitleColor(AssetStyles.TEXT_HEADER);
        border.setTitleFont(AssetStyles.FONT_BOLD);
        setBorder(border);
    }
    
    protected JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout != null ? layout : new FlowLayout());
        panel.setBackground(AssetStyles.UI_BG_PRIMARY);
        
        TitledBorder border = BorderFactory.createTitledBorder(AssetStyles.PANEL_BORDER, title);
        border.setTitleColor(AssetStyles.TEXT_HEADER);
        border.setTitleFont(AssetStyles.FONT_BOLD);
        
        panel.setBorder(border);
        return panel;
    }
}