package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public class AlertPanel extends ZeroGamePanel {

    private JPanel listPanel;
    private JScrollPane scrollPane;
    private List<Alert> lastAlerts = new ArrayList<>();

    public AlertPanel() {
        super(new BorderLayout());        
        initComponents();
        initLayout();
    }

    @Override
    protected void initComponents() {
        setPreferredSize(new Dimension(250, 150));
        setTitledBorder(LanguageStrings.PANEL_ALERTS);
        
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(AssetStyles.BACKGROUND_COLOR); 

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
    }

    @Override
    protected void initLayout() {
        add(scrollPane, BorderLayout.CENTER);
    }
    
    @Override
    public void refreshTranslations() {
        super.refreshTranslations();
        updateAlerts(lastAlerts);
    }

    public void updateAlerts(List<Alert> alerts) {
        this.lastAlerts = alerts;
        listPanel.removeAll();
        
        if (alerts.isEmpty()) {
            JLabel emptyLabel = new JLabel(LanguageStrings.get(LanguageStrings.PANEL_NO_ALERTS));
            emptyLabel.setForeground(AssetStyles.BACKGROUND_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(emptyLabel);
            listPanel.add(Box.createVerticalGlue());
        } else {
            for (Alert alert : alerts) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBackground(AssetStyles.BACKGROUND_COLOR);
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, AssetStyles.BORDER_THICKNESS_INTERNAL, 0, AssetStyles.BORDER_COLOR),
                    new EmptyBorder(2, 5, 2, 5)
                ));

                JLabel msgLabel = new JLabel(alert.message);
                msgLabel.setFont(msgLabel.getFont().deriveFont(Font.BOLD, 11f));
                msgLabel.setForeground(alert.color);
                
                itemPanel.add(msgLabel, BorderLayout.CENTER);
                listPanel.add(itemPanel);
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public static class Alert {
        public String key;
        public String message;
        public Color color;
        public long expiresAt; 

        public Alert(String key, String message, Color color, long durationMs) {
            this.key = key;
            this.message = message;
            this.color = color;
            this.expiresAt = System.currentTimeMillis() + durationMs;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
