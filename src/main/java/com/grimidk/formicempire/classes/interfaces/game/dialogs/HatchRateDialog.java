package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HatchRateDialog extends ZeroDialog {

    private final Colony colony;
    private final Map<AntType, JSpinner> spinnerMap = new HashMap<>();
    private final JLabel totalLabel = new JLabel("Total: 100.0%");
    private final JPanel centerPanel;

    public HatchRateDialog(JFrame owner, Colony colony) {
        super(owner, "Manage Pupa Hatch Rates", new Dimension(400, 350));
        this.colony = colony;
        
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        add(centerPanel, BorderLayout.CENTER);
        
        registerCloseKey(KeyEvent.VK_P);
    }
    
    @Override
    protected void refreshDialog() {
        centerPanel.removeAll();
        spinnerMap.clear();
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));
        centerPanel.add(new JLabel("Set hatch chance for new ants:"));
        centerPanel.add(totalLabel);
        centerPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        List<AntType> typesToRate = new ArrayList<>();
        if (colony.hasUpgrade(GameUnlocks.TYPE_WORKER)) typesToRate.add(GameConstants.TYPE_WORKER);
        if (colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) typesToRate.add(GameConstants.TYPE_SOLDIER);
        if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) typesToRate.add(GameConstants.TYPE_MAJOR);
        if (colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            typesToRate.add(GameConstants.TYPE_DRONE);
            typesToRate.add(GameConstants.TYPE_PRINCESS);
        }

        for (AntType type : typesToRate) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            JLabel typeLabel = new JLabel(type.getIcon());
            typeLabel.setToolTipText(type.getName());
            row.add(typeLabel);
            
            float currentRate = colony.getHatchRate(type);
            SpinnerModel model = new SpinnerNumberModel((double)currentRate, 0.0, 100.0, 0.1);
            JSpinner spinner = new JSpinner(model);
            spinner.setPreferredSize(new Dimension(80, 25));

            spinner.addChangeListener(e -> {
                double newValue = (Double) spinner.getValue();
                colony.setHatchRate(type, (float)newValue);
                updateHatchRateTotals();
            });
            
            disableSpinnerLetterInput(spinner); 
            
            spinnerMap.put(type, spinner);
            row.add(spinner);
            centerPanel.add(row);
        }
        
        updateHatchRateTotals();
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void disableSpinnerLetterInput(JSpinner spinner) {
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
            textField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent e) {
                    if (Character.isLetter(e.getKeyChar())) {
                        e.consume();
                    }
                }
            });
        }
    }

    private void updateHatchRateTotals() {
        double totalAssigned = 0.0;
        for (JSpinner s : spinnerMap.values()) {
            totalAssigned += (Double) s.getValue();
        }
        
        double unassigned = 100.0 - totalAssigned;
        
        totalLabel.setText(String.format("Total: %.1f%%", totalAssigned));
        
        if (Math.abs(unassigned) > 0.1) { 
            totalLabel.setForeground(Color.RED);
            totalLabel.setToolTipText(String.format("Warning: Total is not 100%%. You are %.1f%% over/under.", -unassigned));
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
}