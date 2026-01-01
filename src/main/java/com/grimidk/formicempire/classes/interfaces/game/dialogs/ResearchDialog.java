package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchDialog extends ZeroDialog {

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel researchPointsLabel;
    private final JScrollPane scrollPane;

    private final Map<JButton, Upgrade> buttonUpgradeMap = new HashMap<>();

    public ResearchDialog(JFrame owner, Colony colony) {
        super(owner, "Research & Development", new Dimension(600, 500));
        this.colony = colony;

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        researchPointsLabel = new JLabel();
        researchPointsLabel.setFont(researchPointsLabel.getFont().deriveFont(Font.BOLD));
        northPanel.add(researchPointsLabel);
        add(northPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(listPanel); 
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        registerCloseKey(KeyEvent.VK_Y);
    }

    @Override
    protected void refreshDialog() {
        listPanel.removeAll();
        buttonUpgradeMap.clear();
        
        updateResearchPointsLabel();

        int currentRP = colony.getResearchPoints();
        List<Upgrade> allUpgrades = GameUnlocks.getUpgrades();
        List<Upgrade> availableUpgrades = new ArrayList<>();

        for (Upgrade upgrade : allUpgrades) {
            boolean owned = colony.hasUpgrade(upgrade);
            boolean reqMet = (upgrade.getRequirement() == null || colony.hasUpgrade(upgrade.getRequirement()));

            if (!owned && reqMet && upgrade.getCost() > 0) {
                availableUpgrades.add(upgrade);
            }
        }

        availableUpgrades.sort((u1, u2) -> Integer.compare(u1.getCost(), u2.getCost()));

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
        
        SwingUtilities.invokeLater(() -> scrollPane.getViewport().setViewPosition(new Point(0, 0)));
    }
    
    private void updateResearchPointsLabel() {
        researchPointsLabel.setText("Available Research Points: " + colony.getResearchPoints());
    }

    private JPanel createUpgradePanel(Upgrade upgrade, int currentRP) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder(upgrade.getFlavorName()));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JTextArea descriptionArea = new JTextArea(upgrade.getDescription());
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(infoPanel.getFont());
        descriptionArea.setBorder(null);
        infoPanel.add(descriptionArea);
        panel.add(infoPanel, BorderLayout.CENTER);

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

        buttonUpgradeMap.put(purchaseButton, upgrade);

        return panel;
    }

    @Override
    public void liveUpdate() {
        if (!isShowing()) {
            return; 
        }

        updateResearchPointsLabel();

        int currentRP = colony.getResearchPoints();
        for (Map.Entry<JButton, Upgrade> entry : buttonUpgradeMap.entrySet()) {
            JButton button = entry.getKey();
            Upgrade upgrade = entry.getValue();

            if (currentRP < upgrade.getCost()) {
                button.setEnabled(false);
                button.setToolTipText("Not enough Research Points");
            } else {
                button.setEnabled(true);
                button.setToolTipText(null);
            }
        }
    }
}