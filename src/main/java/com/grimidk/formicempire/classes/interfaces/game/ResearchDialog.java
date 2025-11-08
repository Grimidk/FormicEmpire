package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ResearchDialog extends JDialog {

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel researchPointsLabel;

    public ResearchDialog(JFrame owner, Colony colony) {
        super(owner, "Research & Development", true);
        this.colony = colony;
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 500));

        // Header Panel (shows current RP)
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        researchPointsLabel = new JLabel();
        researchPointsLabel.setFont(researchPointsLabel.getFont().deriveFont(Font.BOLD));
        northPanel.add(researchPointsLabel);
        add(northPanel, BorderLayout.NORTH);

        // Center Panel (list of upgrades)
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // South Panel (Close button)
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(owner);
    }

    private void refreshDialog() {
        listPanel.removeAll();
        updateResearchPointsLabel();

        int currentRP = colony.getResearchPoints();
        List<Upgrade> allUpgrades = GameUpgrades.getUpgrades();
        List<Upgrade> availableUpgrades = new ArrayList<>();

        for (Upgrade upgrade : allUpgrades) {
            boolean owned = colony.hasUpgrade(upgrade);
            boolean reqMet = (upgrade.getRequirement() == null || colony.hasUpgrade(upgrade.getRequirement()));
            boolean isResearchable = (upgrade.getCost() > 0);

            if (!owned && reqMet && isResearchable) {
                availableUpgrades.add(upgrade);
            }
        }

        if (availableUpgrades.isEmpty()) {
            listPanel.add(new JLabel("  No new research available at this time."));
        } else {
            for (Upgrade upgrade : availableUpgrades) {
                listPanel.add(createUpgradePanel(upgrade, currentRP));
                listPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
    
    private void updateResearchPointsLabel() {
        researchPointsLabel.setText("Available Research Points: " + colony.getResearchPoints());
    }

    private JPanel createUpgradePanel(Upgrade upgrade, int currentRP) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder(upgrade.getFlavorName()));

        // Info Panel (Name, Description)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(new JLabel("<html><p>" + upgrade.getDescription() + "</p></html>"));
        panel.add(infoPanel, BorderLayout.CENTER);

        // Action Panel (Button, Cost)
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        
        JButton purchaseButton = new JButton("Buy");
        purchaseButton.setFocusable(false);
        if (currentRP < upgrade.getCost()) {
            purchaseButton.setEnabled(false);
            purchaseButton.setToolTipText("Not enough Research Points");
        }

        purchaseButton.addActionListener(e -> {
            if (colony.getResearchPoints() >= upgrade.getCost()) {
                colony.setResearchPoints(colony.getResearchPoints() - upgrade.getCost());
                colony.unlockUpgrade(upgrade);
                refreshDialog();
            }
        });

        JLabel costLabel = new JLabel(upgrade.getCost() + " RP");
        costLabel.setFont(costLabel.getFont().deriveFont(Font.BOLD));
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        purchaseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        actionPanel.add(costLabel);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        actionPanel.add(purchaseButton);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    public void showDialog() {
        refreshDialog();
        setVisible(true);
    }
}