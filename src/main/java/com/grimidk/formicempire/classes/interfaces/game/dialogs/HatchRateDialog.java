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
    
    private boolean isAdjusting = false;

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
    public void showDialog() {
        super.showDialog(); 
        
        finalizeHatchRates();
    }

    @Override
    protected void refreshDialog() {
        centerPanel.removeAll();
        spinnerMap.clear();
        isAdjusting = false; 
        
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

            spinner.addChangeListener(e -> handleSpinnerChange(type, spinner));
            
            disableSpinnerLetterInput(spinner); 
            
            spinnerMap.put(type, spinner);
            row.add(spinner);
            centerPanel.add(row);
        }
        
        updateHatchRateTotals();
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void handleSpinnerChange(AntType type, JSpinner spinner) {
        if (isAdjusting) return;
        isAdjusting = true;

        try {
            double newValue = (Double) spinner.getValue();
            double otherTotal = 0.0;

            for (Map.Entry<AntType, JSpinner> entry : spinnerMap.entrySet()) {
                if (entry.getKey() != type) {
                    otherTotal += (Double) entry.getValue().getValue();
                }
            }

            if (newValue + otherTotal > 100.0) {
                AntType workerType = GameConstants.TYPE_WORKER;
                JSpinner workerSpinner = spinnerMap.get(workerType);

                if (type != workerType && workerSpinner != null) {
                    double workerValue = (Double) workerSpinner.getValue();
                    double excess = (newValue + otherTotal) - 100.0;

                    if (workerValue >= excess) {
                        double newWorkerValue = workerValue - excess;
                        workerSpinner.setValue(newWorkerValue);
                        colony.setHatchRate(workerType, (float) newWorkerValue);
                    } else {
                        newValue = 100.0 - otherTotal;
                        spinner.setValue(newValue);
                    }
                } else {
                    newValue = 100.0 - otherTotal;
                    spinner.setValue(newValue);
                }
            }

            colony.setHatchRate(type, (float) newValue);
            updateHatchRateTotals();

        } finally {
            isAdjusting = false;
        }
    }

    private void finalizeHatchRates() {
        double currentTotal = 0.0;
        for (JSpinner s : spinnerMap.values()) {
            currentTotal += (Double) s.getValue();
        }

        if (currentTotal < 99.99) {
            double remainder = 100.0 - currentTotal;
            float currentWorkerRate = colony.getHatchRate(GameConstants.TYPE_WORKER);
            
            colony.setHatchRate(GameConstants.TYPE_WORKER, currentWorkerRate + (float) remainder);
        }
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
        
        totalLabel.setText(String.format("Total: %.1f%%", totalAssigned));
        
        if (Math.abs(100.0 - totalAssigned) > 0.1) { 
            totalLabel.setForeground(Color.RED);
            totalLabel.setToolTipText(String.format("Warning: Total is not 100%%."));
        } else {
            totalLabel.setForeground(Color.BLACK);
            totalLabel.setToolTipText("Total is 100%");
        }
    }
}