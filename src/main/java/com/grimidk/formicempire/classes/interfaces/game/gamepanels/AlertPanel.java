package com.grimidk.formicempire.classes.interfaces.game.gamepanels;

import com.grimidk.formicempire.classes.infrasctructure.repositories.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AlertPanel extends ZeroGamePanel {

    private JPanel listPanel;
    private JScrollPane scrollPane;

    public AlertPanel() {
        super(new BorderLayout());        
        initComponents();
        initLayout();
    }

    @Override
    protected void initComponents() {
        setPreferredSize(new Dimension(250, 150));
        setTitledBorder("Alerts");
        
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(AssetStyles.BACKGROUND_LIGHT); 

        scrollPane = new JScrollPane(listPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(AssetStyles.BACKGROUND_LIGHT);
    }

    @Override
    protected void initLayout() {
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateAlerts(List<Alert> alerts) {
        listPanel.removeAll();
        
        if (alerts.isEmpty()) {
            JLabel emptyLabel = new JLabel("No alerts");
            emptyLabel.setForeground(AssetStyles.BACKGROUND_SECONDARY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(emptyLabel);
            listPanel.add(Box.createVerticalGlue());
        } else {
            for (Alert alert : alerts) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBackground(AssetStyles.BACKGROUND_LIGHT);
                itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AssetStyles.BACKGROUND_SECONDARY),
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