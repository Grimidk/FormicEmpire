package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
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
    private final Map<AntSubtypeSlot, Map<AntType, Map<Integer, JSpinner>>> subtypeSpinnerMap =
            new EnumMap<>(AntSubtypeSlot.class);
    private final Map<AntSubtypeSlot, Map<AntType, JLabel>> subtypeRowTotalLabels = new EnumMap<>(AntSubtypeSlot.class);
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
        subtypeRowTotalLabels.clear();
        isAdjusting = false;

        totalLabel.setFont(AssetStyles.FONT_BOLD);
        totalLabel.setForeground(AssetStyles.FONT_COLOR);

        JLabel descLabel = new JLabel(LanguageStrings.get(LanguageStrings.HATCH_DESC));
        descLabel.setForeground(AssetStyles.FONT_COLOR);

        centerPanel.add(descLabel);
        centerPanel.add(totalLabel);
        centerPanel.add(AssetStyles.createInternalSeparator());

        List<AntType> typesToRate = buildUnlockedHatchTypes();
        List<AntType> subtypeTypes = buildUnlockedSubtypeRateTypes();

        JPanel ratesRow = new JPanel();
        ratesRow.setLayout(new BoxLayout(ratesRow, BoxLayout.X_AXIS));
        ratesRow.setOpaque(false);
        ratesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel typePanel = new JPanel();
        typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
        typePanel.setOpaque(false);
        typePanel.setAlignmentY(Component.TOP_ALIGNMENT);

        for (AntType type : typesToRate) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
            row.setOpaque(false);

            String typeTip = typeTooltip(type);
            JLabel typeLabel = new JLabel(type.getIcon());
            typeLabel.setToolTipText(typeTip);
            row.add(typeLabel);

            float currentRate = colony.getHatchRate(type);
            SpinnerModel model = new SpinnerNumberModel((double) currentRate, 0.0, 100.0, 0.1);
            JSpinner spinner = new JSpinner(model);
            AssetStyles.styleSpinner(spinner);
            spinner.setPreferredSize(AssetStyles.preferredSpinnerSize(80));
            spinner.setToolTipText(typeTip);

            spinner.addChangeListener(e -> handleSpinnerChange(type, spinner));

            disableSpinnerLetterInput(spinner);

            spinnerMap.put(type, spinner);
            row.add(spinner);
            typePanel.add(row);
        }

        ratesRow.add(typePanel);
        ratesRow.add(Box.createHorizontalStrut(24));

        JPanel subtypePanel = new JPanel();
        subtypePanel.setLayout(new BoxLayout(subtypePanel, BoxLayout.Y_AXIS));
        subtypePanel.setOpaque(false);
        subtypePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        buildSubtypeSections(subtypeTypes, subtypePanel);

        ratesRow.add(subtypePanel);

        centerPanel.add(ratesRow);

        updateHatchRateTotals();
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    private List<AntType> buildUnlockedHatchTypes() {
        List<AntType> typesToRate = new ArrayList<>();
        if (colony.hasUpgrade(GameUnlocks.TYPE_WORKER)) {
            typesToRate.add(GameConstants.TYPE_WORKER);
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
            typesToRate.add(GameConstants.TYPE_SOLDIER);
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
            typesToRate.add(GameConstants.TYPE_MAJOR);
        }
        if (colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
            typesToRate.add(GameConstants.TYPE_DRONE);
            typesToRate.add(GameConstants.TYPE_PRINCESS);
        }
        return typesToRate;
    }

    private List<AntType> buildUnlockedSubtypeRateTypes() {
        List<AntType> types = new ArrayList<>();
        for (AntType type : AntSubtypeService.getSubtypeRateTypes()) {
            if (type == GameConstants.TYPE_WORKER && colony.hasUpgrade(GameUnlocks.TYPE_WORKER)) {
                types.add(type);
            } else if (type == GameConstants.TYPE_SOLDIER && colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER)) {
                types.add(type);
            } else if (type == GameConstants.TYPE_MAJOR && colony.hasUpgrade(GameUnlocks.TYPE_MAJOR)) {
                types.add(type);
            } else if (type == GameConstants.TYPE_PRINCESS && colony.hasUpgrade(GameUnlocks.TYPE_PRINCESS)) {
                types.add(type);
            }
        }
        return types;
    }

    private void buildSubtypeSections(List<AntType> subtypeTypes, JPanel host) {
        if (subtypeTypes.isEmpty() || !colony.hasUpgrade(GameUnlocks.ABILITY_SUBTYPE_HATCH)) {
            return;
        }

        boolean firstSection = true;
        for (AntSubtypeSlot slot : GameConstants.getConfigurableSubtypeSlots()) {
            List<AntSubtype> subtypes = AntSubtypeService.getAvailableSubtypes(colony, slot);
            if (subtypes.size() <= 1) {
                continue;
            }

            if (!firstSection) {
                host.add(AssetStyles.createInternalSeparator());
            }
            firstSection = false;

            JLabel sectionLabel = new JLabel(LanguageStrings.get(sectionLabelKey(slot)));
            sectionLabel.setFont(AssetStyles.FONT_BOLD);
            sectionLabel.setForeground(AssetStyles.FONT_COLOR);
            sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            host.add(sectionLabel);

            JPanel matrixPanel = new JPanel(new GridBagLayout());
            matrixPanel.setOpaque(false);
            matrixPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            GridBagConstraints header = new GridBagConstraints();
            header.gridy = 0;
            header.insets = new Insets(4, 4, 4, 4);
            header.anchor = GridBagConstraints.CENTER;

            int column = 1;
            for (AntSubtype subtype : subtypes) {
                header.gridx = column++;
                String subtypeTip = subtypeTooltip(subtype);
                JLabel subtypeHeader = new JLabel(subtype.getIcon());
                subtypeHeader.setToolTipText(subtypeTip);
                matrixPanel.add(subtypeHeader, header);
            }

            header.gridx = column;
            JLabel totalHeader = new JLabel(LanguageStrings.get(LanguageStrings.HATCH_SUBTYPE_ROW_TOTAL));
            totalHeader.setFont(AssetStyles.FONT_BOLD);
            totalHeader.setForeground(AssetStyles.FONT_COLOR);
            matrixPanel.add(totalHeader, header);

            Map<AntType, Map<Integer, JSpinner>> typeSpinners = new HashMap<>();
            subtypeSpinnerMap.put(slot, typeSpinners);
            Map<AntType, JLabel> rowTotals = new HashMap<>();
            subtypeRowTotalLabels.put(slot, rowTotals);

            int rowIndex = 1;
            GridBagConstraints cell = new GridBagConstraints();
            cell.insets = new Insets(4, 4, 4, 4);
            cell.anchor = GridBagConstraints.CENTER;

            for (AntType type : subtypeTypes) {
                cell.gridy = rowIndex;
                cell.gridx = 0;
                String typeTip = typeTooltip(type);
                JLabel typeLabel = new JLabel(type.getIcon());
                typeLabel.setToolTipText(typeTip);
                matrixPanel.add(typeLabel, cell);

                Map<Integer, JSpinner> digitSpinners = new HashMap<>();
                typeSpinners.put(type, digitSpinners);

                column = 1;
                for (AntSubtype subtype : subtypes) {
                    cell.gridx = column++;
                    float currentRate = colony.getSubtypeHatchRate(type, slot, subtype.getDigit());
                    SpinnerModel model = new SpinnerNumberModel((double) currentRate, 0.0, 100.0, 0.1);
                    JSpinner spinner = new JSpinner(model);
                    AssetStyles.styleSpinner(spinner);
                    spinner.setPreferredSize(AssetStyles.preferredSpinnerSize(70));
                    spinner.setToolTipText(matrixCellTooltip(type, subtype));
                    spinner.addChangeListener(e -> handleSubtypeSpinnerChange(slot, type, subtype.getDigit(), spinner));
                    disableSpinnerLetterInput(spinner);
                    digitSpinners.put(subtype.getDigit(), spinner);
                    matrixPanel.add(spinner, cell);
                }

                cell.gridx = column;
                JLabel rowTotal = new JLabel(LanguageStrings.format(LanguageStrings.HATCH_TOTAL, 100.0f));
                rowTotal.setFont(AssetStyles.FONT_BOLD);
                rowTotal.setForeground(AssetStyles.FONT_COLOR);
                rowTotals.put(type, rowTotal);
                matrixPanel.add(rowTotal, cell);

                updateSubtypeRowTotals(slot, type);
                rowIndex++;
            }

            host.add(matrixPanel);
        }
    }

    private static String sectionLabelKey(AntSubtypeSlot slot) {
        return switch (slot) {
            case HEAD -> LanguageStrings.HATCH_SUBTYPE_HEAD_SECTION;
            case ABDOMEN -> LanguageStrings.HATCH_SUBTYPE_ABDOMEN_SECTION;
            default -> LanguageStrings.HATCH_DESC;
        };
    }

    private static String typeTooltip(AntType type) {
        return formatStackedTooltip(type.getName(), helpDescForType(type));
    }

    private static String subtypeTooltip(AntSubtype subtype) {
        String effect = subtype.getDesc();
        if (subtype.isNone()) {
            return formatStackedTooltip(subtype.getName(), effect);
        }
        return formatStackedTooltip(
                subtype.getName(),
                effect,
                LanguageStrings.get(LanguageStrings.SUBTYPE_FOOD_COST_PER_TRAIT));
    }

    private static String matrixCellTooltip(AntType type, AntSubtype subtype) {
        String effect = subtype.getDesc();
        if (subtype.isNone()) {
            return formatStackedTooltip(type.getName() + " — " + subtype.getName(), effect);
        }
        return formatStackedTooltip(
                type.getName() + " — " + subtype.getName(),
                effect,
                LanguageStrings.get(LanguageStrings.SUBTYPE_FOOD_COST_PER_TRAIT));
    }

    private static String helpDescForType(AntType type) {
        if (type == GameConstants.TYPE_WORKER) {
            return LanguageStrings.get(LanguageStrings.HELP_TYPE_WORKER_DESC);
        }
        if (type == GameConstants.TYPE_SOLDIER) {
            return LanguageStrings.get(LanguageStrings.HELP_TYPE_SOLDIER_DESC);
        }
        if (type == GameConstants.TYPE_MAJOR) {
            return LanguageStrings.get(LanguageStrings.HELP_TYPE_MAJOR_DESC);
        }
        if (type == GameConstants.TYPE_PRINCESS) {
            return LanguageStrings.get(LanguageStrings.HELP_TYPE_PRINCESS_DESC);
        }
        if (type == GameConstants.TYPE_DRONE) {
            return LanguageStrings.get(LanguageStrings.HELP_TYPE_DRONE_DESC);
        }
        if (type == GameConstants.TYPE_QUEEN) {
            return LanguageStrings.get(LanguageStrings.HELP_TYPE_QUEEN_DESC);
        }
        return "";
    }

    private static String formatStackedTooltip(String title, String... details) {
        StringBuilder sb = new StringBuilder("<html><body style='width:240px'><b>");
        sb.append(escapeHtml(title == null ? "" : title));
        sb.append("</b>");
        if (details != null) {
            for (String detail : details) {
                if (detail == null || detail.isEmpty()) {
                    continue;
                }
                sb.append("<br>").append(escapeHtml(detail));
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private void handleSpinnerChange(AntType type, JSpinner spinner) {
        if (isAdjusting) {
            return;
        }
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

    private void handleSubtypeSpinnerChange(AntSubtypeSlot slot, AntType type, int digit, JSpinner spinner) {
        if (isAdjusting) {
            return;
        }
        isAdjusting = true;
        try {
            double newValue = (Double) spinner.getValue();
            Map<AntType, Map<Integer, JSpinner>> slotSpinners = subtypeSpinnerMap.get(slot);
            if (slotSpinners == null) {
                return;
            }
            Map<Integer, JSpinner> typeSpinners = slotSpinners.get(type);
            if (typeSpinners == null) {
                return;
            }

            double otherTotal = 0.0;
            for (Map.Entry<Integer, JSpinner> entry : typeSpinners.entrySet()) {
                if (entry.getKey() != digit) {
                    otherTotal += (Double) entry.getValue().getValue();
                }
            }

            if (newValue + otherTotal > 100.0) {
                JSpinner noneSpinner = typeSpinners.get(AntSubtype.DIGIT_NONE);
                if (digit != AntSubtype.DIGIT_NONE && noneSpinner != null) {
                    double noneValue = (Double) noneSpinner.getValue();
                    double excess = (newValue + otherTotal) - 100.0;
                    if (noneValue >= excess) {
                        double newNoneValue = noneValue - excess;
                        noneSpinner.setValue(newNoneValue);
                        colony.setSubtypeHatchRate(type, slot, AntSubtype.DIGIT_NONE, (float) newNoneValue);
                    } else {
                        newValue = 100.0 - otherTotal;
                        spinner.setValue(newValue);
                    }
                } else {
                    newValue = 100.0 - otherTotal;
                    spinner.setValue(newValue);
                }
            }

            colony.setSubtypeHatchRate(type, slot, digit, (float) newValue);
            updateSubtypeRowTotals(slot, type);
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
        Map<AntType, Map<Integer, JSpinner>> slotSpinners = subtypeSpinnerMap.get(slot);
        if (slotSpinners == null) {
            return;
        }
        for (Map.Entry<AntType, Map<Integer, JSpinner>> typeEntry : slotSpinners.entrySet()) {
            AntType type = typeEntry.getKey();
            Map<Integer, JSpinner> digitSpinners = typeEntry.getValue();
            double currentTotal = 0.0;
            for (JSpinner spinner : digitSpinners.values()) {
                currentTotal += (Double) spinner.getValue();
            }
            if (currentTotal < 99.99) {
                double remainder = 100.0 - currentTotal;
                float currentNoneRate = colony.getSubtypeHatchRate(type, slot, AntSubtype.DIGIT_NONE);
                colony.setSubtypeHatchRate(type, slot, AntSubtype.DIGIT_NONE,
                        currentNoneRate + (float) remainder);
            }
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

    private void updateSubtypeRowTotals(AntSubtypeSlot slot, AntType type) {
        Map<AntType, JLabel> rowTotals = subtypeRowTotalLabels.get(slot);
        Map<AntType, Map<Integer, JSpinner>> slotSpinners = subtypeSpinnerMap.get(slot);
        if (rowTotals == null || slotSpinners == null) {
            return;
        }
        JLabel rowTotal = rowTotals.get(type);
        Map<Integer, JSpinner> typeSpinners = slotSpinners.get(type);
        if (rowTotal == null || typeSpinners == null) {
            return;
        }
        double totalAssigned = 0.0;
        for (JSpinner spinner : typeSpinners.values()) {
            totalAssigned += (Double) spinner.getValue();
        }
        rowTotal.setText(LanguageStrings.format(LanguageStrings.HATCH_TOTAL, totalAssigned));
        if (Math.abs(100.0 - totalAssigned) > 0.1) {
            rowTotal.setForeground(AssetStyles.FONT_COLOR_ERROR);
            rowTotal.setToolTipText(LanguageStrings.get(LanguageStrings.HATCH_WARNING_TOTAL));
        } else {
            rowTotal.setForeground(AssetStyles.FONT_COLOR);
            rowTotal.setToolTipText(LanguageStrings.get(LanguageStrings.HATCH_TOTAL_OK));
        }
    }
}
