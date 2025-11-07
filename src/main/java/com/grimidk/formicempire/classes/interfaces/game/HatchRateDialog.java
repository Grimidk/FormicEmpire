package com.grimidk.formicempire.classes.interfaces.game;

import com.grimidk.formicempire.classes.constants.AntType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.GameUpgrades;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HatchRateDialog extends JDialog {

    private final Colony colony;
    private final Map<AntType, JSpinner> spinnerMap = new HashMap<>();
    private final JLabel totalLabel = new JLabel("Total: 100.00%");

    public HatchRateDialog(JFrame owner, Colony colony) {
        super(owner, "Manage Pupa Hatch Rates", true);
        this.colony = colony;
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 350));
        
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createSouthPanel(), BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(owner);
    }
    
    private JPanel createSouthPanel() {
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(closeButton);
        return southPanel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        
        panel.add(new JLabel("Set hatch chance for new ants:"));
        panel.add(totalLabel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        // Build the list of types dynamically based on unlocked upgrades
        List<AntType> typesToRate = new ArrayList<>();
        if (colony.hasUpgrade(GameUpgrades.TYPE_WORKER)) {
            typesToRate.add(GameConstants.TYPE_WORKER);
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_SOLDIER)) {
            typesToRate.add(GameConstants.TYPE_SOLDIER);
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_MAJOR)) {
            typesToRate.add(GameConstants.TYPE_MAJOR);
        }
        if (colony.hasUpgrade(GameUpgrades.TYPE_PRINCESS)) {
            typesToRate.add(GameConstants.TYPE_DRONE);
            typesToRate.add(GameConstants.TYPE_PRINCESS);
        }

        for (AntType type : typesToRate) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            JLabel typeLabel = new JLabel(type.getIcon());
            typeLabel.setToolTipText(type.getName());
            row.add(typeLabel);
            
            float currentRate = colony.getHatchRate(type);
            SpinnerModel model = new SpinnerNumberModel((double)currentRate, 0.0, 100.0, 0.01);
            JSpinner spinner = new JSpinner(model);
            spinner.setPreferredSize(new Dimension(80, 25));

            spinner.addChangeListener(e -> {
                double newValue = (Double) spinner.getValue();
                colony.setHatchRate(type, (float)newValue);
                updateHatchRateTotals();
            });
            
            spinnerMap.put(type, spinner);
            row.add(spinner);
            panel.add(row);
        }
        
        updateHatchRateTotals();
        return panel;
    }

    private void updateHatchRateTotals() {
        double totalAssigned = 0.0;
        for (JSpinner s : spinnerMap.values()) {
            totalAssigned += (Double) s.getValue();
        }
        
        double unassigned = 100.0 - totalAssigned;
        
        totalLabel.setText(String.format("Total: %.2f%%", totalAssigned));
        
        if (Math.abs(unassigned) > 0.01) { 
            totalLabel.setForeground(Color.RED);
            totalLabel.setToolTipText(String.format("Warning: Total is not 100%%. You are %.2f%% over/under.", -unassigned));
        } else {
            totalLabel.setForeground(Color.BLACK);
            totalLabel.setToolTipText("Total is 100%");
        }

        for (JSpinner s : spinnerMap.values()) {
            SpinnerNumberModel model = (SpinnerNumberModel) s.getModel();
            double currentValue = (Double) s.getValue();
            double newMax = currentValue + Math.max(0.0, unassigned);
            model.setMaximum(Math.max(currentValue, newMax));
        }
    }
    
    public void showDialog() {
        setVisible(true);
    }
}