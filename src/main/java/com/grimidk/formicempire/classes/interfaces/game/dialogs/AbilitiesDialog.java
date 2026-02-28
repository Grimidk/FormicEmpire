package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class AbilitiesDialog extends ZeroDialog {

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel researchPointsLabel;
    private final JScrollPane scrollPane;

    private final Map<JButton, Upgrade> abilityButtons = new HashMap<>();

    public AbilitiesDialog(JFrame owner, Colony colony) {
        super(owner, "Colony Operations", new Dimension(500, 400));
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
        
        registerCloseKey(KeyEvent.VK_C);
    }

    @Override
    protected void refreshDialog() {
        listPanel.removeAll();
        abilityButtons.clear();
        
        updateResearchPointsLabel();
        
        boolean hasAnyAbility = false;

        if (colony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) {
            int currentCost = colony.getNuptialFlightCost();
            String failureReason = getNuptialFailureReason(currentCost);
            boolean enabled = (failureReason == null);
            
            JPanel p = createAbilityPanel("Forced Nuptial Flight", 
                "Spend " + currentCost + " RP to immediately trigger a nuptial flight.\nRequires Drones and Breeder Princesses.",
                currentCost, 
                e -> {
                    if (getOwner() instanceof MainFrame) {
                        MainFrame main = (MainFrame) getOwner();
                        Engine engine = main.getEngine();
                        if (engine != null) {
                            World world = engine.getWorld();
                            if (world != null) {
                                Hex targetHex = null;
                                for(Hex h : world.getHexes()) {
                                    if (h.getColony() == colony) {
                                        targetHex = h;
                                        break;
                                    }
                                }
                                
                                if (targetHex != null) {
                                    colony.forceNuptialFlight(world, targetHex);
                                    refreshDialog(); 
                                } else {
                                    JOptionPane.showMessageDialog(this, "Error: Could not locate colony on the world map.");
                                }
                            }
                        }
                    }
                },
                enabled,
                failureReason
            );
            listPanel.add(p);
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT) && colony.getDynasty() != null) {
            int massCost = colony.getDynasty().getMassNuptialFlightCost();
            boolean hasRP = colony.getResearchPoints() >= massCost;
            String fail = hasRP ? null : "Not enough Research Points (" + massCost + " needed).";

            JPanel mp = createAbilityPanel("Mass Nuptial Flights",
                "Spend " + massCost + " RP to trigger nuptial flights in ALL capable colonies across your dynasty.",
                massCost,
                e -> {
                    if (getOwner() instanceof MainFrame) {
                        MainFrame main = (MainFrame) getOwner();
                        if (main.getEngine() != null && main.getEngine().getWorld() != null) {
                            colony.getDynasty().runMassNuptialFlight(main.getEngine().getWorld());
                            refreshDialog();
                        }
                    }
                },
                hasRP,
                fail
            );
            listPanel.add(mp);
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
    
    private String getNuptialFailureReason(int cost) {
        if (colony.getResearchPoints() < cost) return "Not enough Research Points (" + cost + " needed).";
        if (colony.getDrones().isEmpty()) return "No Drones available in the colony.";
        if (colony.getPrincesses().stream().noneMatch(p -> p.getRole() == GameConstants.ROLE_BREEDER)) return "No Breeder Princesses available.";
        return null;
    }

    private void updateResearchPointsLabel() {
        researchPointsLabel.setText("Research Points: " + colony.getResearchPoints());
    }

    private JPanel createAbilityPanel(String title, String desc, int rpCost, java.awt.event.ActionListener action, boolean enabled, String tooltip) {
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
        
        if (!enabled && tooltip != null) {
            btn.setToolTipText(tooltip);
        } else {
            btn.setToolTipText(null);
        }
        
        JLabel costLabel = new JLabel(rpCost + " RP");
        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        actionPanel.add(costLabel);
        actionPanel.add(Box.createVerticalStrut(5));
        actionPanel.add(btn);
        
        panel.add(actionPanel, BorderLayout.EAST);
        return panel;
    }
    
    @Override
    public void liveUpdate() {
        if (!isShowing()) return;
        updateResearchPointsLabel();
        refreshDialog(); 
    }
}