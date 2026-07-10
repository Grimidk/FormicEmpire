package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HatchRateDialog extends ZeroDialog {

    private final Colony colony;
    private final Map<AntType, JSpinner> spinnerMap = new HashMap<>();
    private final Map<AntSubtypeSlot, Map<Integer, JSpinner>> subtypeSpinnerMap = new EnumMap<>(AntSubtypeSlot.class);
    private final Map<AntSubtypeSlot, JLabel> subtypeTotalLabels = new EnumMap<>(AntSubtypeSlot.class);
    private final JLabel totalLabel = new JLabel(LanguageStrings.format(LanguageStrings.HATCH_TOTAL, 100.0f));
    private final JPanel centerPanel;
    
    private boolean isAdjusting = false;

    public HatchRateDialog(JFrame owner, Colony colony) {
        super(owner, LanguageStrings.DIALOG_HATCH_RATES_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;
        
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(AssetStyles.BACKGROUND_COLOR);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            AssetStyles.PANEL_BORDER,
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        centerPanel.setBackground(AssetStyles.BACKGROUND_SECONDARY);
        
        outer.add(centerPanel);
        add(outer, BorderLayout.CENTER);
        
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
        subtypeSpinnerMap.clear();
        subtypeTotalLabels.clear();
        isAdjusting = false; 
        
        totalLabel.setFont(AssetStyles.FONT_BOLD);
        totalLabel.setForeground(AssetStyles.FONT_COLOR);
        
        JLabel descLabel = new JLabel(LanguageStrings.get(LanguageStrings.HATCH_DESC));
        descLabel.setForeground(AssetStyles.FONT_COLOR);
        
        centerPanel.add(descLabel);
        centerPanel.add(totalLabel);
        centerPanel.add(AssetStyles.createInternalSeparator());
        
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
            row.setOpaque(false);
            
            JLabel typeLabel = new JLabel(type.getIcon());
            typeLabel.setToolTipText(type.getName());
            row.add(typeLabel);
            
            float currentRate = colony.getHatchRate(type);
            SpinnerModel model = new SpinnerNumberModel((double)currentRate, 0.0, 100.0, 0.1);
            JSpinner spinner = new JSpinner(model);
            AssetStyles.styleSpinner(spinner);
            spinner.setPreferredSize(new Dimension(80, 25));

            spinner.addChangeListener(e -> handleSpinnerChange(type, spinner));
            
            disableSpinnerLetterInput(spinner); 
            
            spinnerMap.put(type, spinner);
            row.add(spinner);
            centerPanel.add(row);
        }

        buildSubtypeSections();
        
        updateHatchRateTotals();
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private void buildSubtypeSections() {
        for (AntSubtypeSlot slot : GameConstants.getConfigurableSubtypeSlots()) {
            List<AntSubtype> subtypes = AntSubtypeService.getAvailableSubtypes(colony, slot);
            if (subtypes.size() <= 1) {
                continue;
            }

            centerPanel.add(AssetStyles.createInternalSeparator());

            JLabel sectionLabel = new JLabel(LanguageStrings.get(sectionLabelKey(slot)));
            sectionLabel.setFont(AssetStyles.FONT_BOLD);
            sectionLabel.setForeground(AssetStyles.FONT_COLOR);
            centerPanel.add(sectionLabel);

            JLabel slotTotal = new JLabel(LanguageStrings.format(LanguageStrings.HATCH_TOTAL, 100.0f));
            slotTotal.setFont(AssetStyles.FONT_BOLD);
            slotTotal.setForeground(AssetStyles.FONT_COLOR);
            subtypeTotalLabels.put(slot, slotTotal);
            centerPanel.add(slotTotal);

            Map<Integer, JSpinner> slotSpinners = new HashMap<>();
            subtypeSpinnerMap.put(slot, slotSpinners);

            for (AntSubtype subtype : subtypes) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
                row.setOpaque(false);

                JLabel subtypeLabel = new JLabel(subtype.getName());
                if (!subtype.getDesc().isEmpty()) {
                    subtypeLabel.setToolTipText(subtype.getDesc());
                }
                row.add(subtypeLabel);

                float currentRate = colony.getSubtypeHatchRate(slot, subtype.getDigit());
                SpinnerModel model = new SpinnerNumberModel((double) currentRate, 0.0, 100.0, 0.1);
                JSpinner spinner = new JSpinner(model);
                AssetStyles.styleSpinner(spinner);
                spinner.setPreferredSize(new Dimension(80, 25));
                spinner.addChangeListener(e -> handleSubtypeSpinnerChange(slot, subtype.getDigit(), spinner));
                disableSpinnerLetterInput(spinner);

                slotSpinners.put(subtype.getDigit(), spinner);
                row.add(spinner);
                centerPanel.add(row);
            }

            updateSubtypeTotals(slot);
        }
    }

    private static String sectionLabelKey(AntSubtypeSlot slot) {
        return switch (slot) {
            case HEAD -> LanguageStrings.HATCH_SUBTYPE_HEAD_SECTION;
            case ABDOMEN -> LanguageStrings.HATCH_SUBTYPE_ABDOMEN_SECTION;
            default -> LanguageStrings.HATCH_DESC;
        };
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

    private void handleSubtypeSpinnerChange(AntSubtypeSlot slot, int digit, JSpinner spinner) {
        if (isAdjusting) {
            return;
        }
        isAdjusting = true;
        try {
            double newValue = (Double) spinner.getValue();
            Map<Integer, JSpinner> slotSpinners = subtypeSpinnerMap.get(slot);
            if (slotSpinners == null) {
                return;
            }

            double otherTotal = 0.0;
            for (Map.Entry<Integer, JSpinner> entry : slotSpinners.entrySet()) {
                if (entry.getKey() != digit) {
                    otherTotal += (Double) entry.getValue().getValue();
                }
            }

            if (newValue + otherTotal > 100.0) {
                JSpinner noneSpinner = slotSpinners.get(GameConstants.SUBTYPE_DIGIT_NONE);
                if (digit != GameConstants.SUBTYPE_DIGIT_NONE && noneSpinner != null) {
                    double noneValue = (Double) noneSpinner.getValue();
                    double excess = (newValue + otherTotal) - 100.0;
                    if (noneValue >= excess) {
                        double newNoneValue = noneValue - excess;
                        noneSpinner.setValue(newNoneValue);
                        colony.setSubtypeHatchRate(slot, GameConstants.SUBTYPE_DIGIT_NONE, (float) newNoneValue);
                    } else {
                        newValue = 100.0 - otherTotal;
                        spinner.setValue(newValue);
                    }
                } else {
                    newValue = 100.0 - otherTotal;
                    spinner.setValue(newValue);
                }
            }

            colony.setSubtypeHatchRate(slot, digit, (float) newValue);
            updateSubtypeTotals(slot);
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

        for (AntSubtypeSlot slot : subtypeSpinnerMap.keySet()) {
            finalizeSubtypeRates(slot);
        }
    }

    private void finalizeSubtypeRates(AntSubtypeSlot slot) {
        Map<Integer, JSpinner> slotSpinners = subtypeSpinnerMap.get(slot);
        if (slotSpinners == null) {
            return;
        }
        double currentTotal = 0.0;
        for (JSpinner spinner : slotSpinners.values()) {
            currentTotal += (Double) spinner.getValue();
        }
        if (currentTotal < 99.99) {
            double remainder = 100.0 - currentTotal;
            float currentNoneRate = colony.getSubtypeHatchRate(slot, GameConstants.SUBTYPE_DIGIT_NONE);
            colony.setSubtypeHatchRate(slot, GameConstants.SUBTYPE_DIGIT_NONE, currentNoneRate + (float) remainder);
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
        
        totalLabel.setText(LanguageStrings.format(LanguageStrings.HATCH_TOTAL, totalAssigned));
        
        if (Math.abs(100.0 - totalAssigned) > 0.1) { 
            totalLabel.setForeground(AssetStyles.FONT_COLOR_ERROR);
            totalLabel.setToolTipText(LanguageStrings.get(LanguageStrings.HATCH_WARNING_TOTAL));
        } else {
            totalLabel.setForeground(AssetStyles.FONT_COLOR);
            totalLabel.setToolTipText(LanguageStrings.get(LanguageStrings.HATCH_TOTAL_OK));
        }
    }

    private void updateSubtypeTotals(AntSubtypeSlot slot) {
        JLabel slotTotal = subtypeTotalLabels.get(slot);
        Map<Integer, JSpinner> slotSpinners = subtypeSpinnerMap.get(slot);
        if (slotTotal == null || slotSpinners == null) {
            return;
        }
        double totalAssigned = 0.0;
        for (JSpinner spinner : slotSpinners.values()) {
            totalAssigned += (Double) spinner.getValue();
        }
        slotTotal.setText(LanguageStrings.format(LanguageStrings.HATCH_TOTAL, totalAssigned));
        if (Math.abs(100.0 - totalAssigned) > 0.1) {
            slotTotal.setForeground(AssetStyles.FONT_COLOR_ERROR);
            slotTotal.setToolTipText(LanguageStrings.get(LanguageStrings.HATCH_WARNING_TOTAL));
        } else {
            slotTotal.setForeground(AssetStyles.FONT_COLOR);
            slotTotal.setToolTipText(LanguageStrings.get(LanguageStrings.HATCH_TOTAL_OK));
        }
    }
}
