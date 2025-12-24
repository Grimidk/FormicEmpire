package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class AbilitiesDialog extends JDialog {

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel researchPointsLabel;
    private final JScrollPane scrollPane;

    // Track active ability buttons
    private final Map<JButton, Upgrade> abilityButtons = new HashMap<>();

    public AbilitiesDialog(JFrame owner, Colony colony) {
        super(owner, "Colony Operations", true);
        this.colony = colony;
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 400));

        // Header (Research Points Status)
        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        researchPointsLabel = new JLabel();
        researchPointsLabel.setFont(researchPointsLabel.getFont().deriveFont(Font.BOLD));
        northPanel.add(researchPointsLabel);
        add(northPanel, BorderLayout.NORTH);

        // Center (List of Abilities)
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(listPanel); 
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Footer (Close)
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        southPanel.add(closeButton);
        add(southPanel, BorderLayout.SOUTH);
        
        // Key bindings
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        pack();
        setLocationRelativeTo(owner);
    }

    public void showDialog() {
        refreshDialog();
        setVisible(true);
    }

    private void refreshDialog() {
        listPanel.removeAll();
        abilityButtons.clear();
        
        updateResearchPointsLabel();
        
        boolean hasAnyAbility = false;

        // --- Forced Nuptial Flight ---
        if (colony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) {
            JPanel p = createAbilityPanel("Forced Nuptial Flight", 
                "Spend 1000 RP to immediately trigger a nuptial flight.\nRequires Drones and Breeder Princesses.",
                1000, 
                e -> {
                    colony.forceNuptialFlight();
                    refreshDialog(); 
                },
                canTriggerNuptial()
            );
            listPanel.add(p);
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (!hasAnyAbility) {
            JLabel empty = new JLabel("No active abilities unlocked yet.");
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(Box.createVerticalGlue());
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
    
    private boolean canTriggerNuptial() {
        if (colony.getResearchPoints() < 1000) return false;
        boolean hasDrones = !colony.getDrones().isEmpty();
        boolean hasBreeders = colony.getPrincesses().stream().anyMatch(p -> p.getRole() == GameConstants.ROLE_BREEDER);
        return hasDrones && hasBreeders;
    }

    private void updateResearchPointsLabel() {
        researchPointsLabel.setText("Research Points: " + colony.getResearchPoints());
    }

    private JPanel createAbilityPanel(String title, String desc, int rpCost, java.awt.event.ActionListener action, boolean enabled) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder(title));

        JTextArea descriptionArea = new JTextArea(desc);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setFont(new JLabel().getFont());
        
        panel.add(descriptionArea, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        
        JButton btn = new JButton("Trigger");
        btn.setEnabled(enabled);
        btn.addActionListener(action);
        
        JLabel costLabel = new JLabel(rpCost + " RP");
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        actionPanel.add(costLabel);
        actionPanel.add(Box.createVerticalStrut(5));
        actionPanel.add(btn);
        
        panel.add(actionPanel, BorderLayout.EAST);
        return panel;
    }
    
    public void liveUpdate() {
        if (!isShowing()) return;
        updateResearchPointsLabel();
        refreshDialog(); 
    }
}