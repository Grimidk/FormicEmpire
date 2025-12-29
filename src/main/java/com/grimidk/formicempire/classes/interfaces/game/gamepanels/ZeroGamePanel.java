package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public abstract class ZeroGamePanel extends JPanel {

    public ZeroGamePanel(LayoutManager layout) {
        super(layout != null ? layout : new FlowLayout());
    }

    protected abstract void initComponents();

    protected abstract void initLayout();

    protected void setTitledBorder(String title) {
        setBorder(BorderFactory.createTitledBorder(title));
    }
    
    protected JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout != null ? layout : new FlowLayout());
        panel.setBorder(new TitledBorder(title));
        return panel;
    }
}