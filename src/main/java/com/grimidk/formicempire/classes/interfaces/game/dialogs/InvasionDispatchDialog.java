package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.invasion.InvasionAlert;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyInvasionService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InvasionDispatchDialog extends ZeroDialog {

    private static final List<AntType> INVASION_ANT_TYPES = List.of(
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS);

    private final Colony colony;
    private final InvasionAlert alert;
    private final int worldDay;
    private final int worldHour;
    private final java.util.function.IntConsumer onDispatched;
    private final Map<AntType, JSpinner> antSpinners = new HashMap<>();
    private final Map<AntType, Integer> availableByType = new HashMap<>();
    private boolean updating;

    private final JLabel winChanceLabel = new JLabel(" ");
    private final JLabel deadlineLabel = new JLabel(" ");
    private final JLabel partySizeLabel = new JLabel(" ");
    private final JButton dispatchBtn;

    public InvasionDispatchDialog(JFrame owner, Colony colony, InvasionAlert alert, int worldDay, int worldHour,
            java.util.function.IntConsumer onDispatched) {
        super(owner, LanguageStrings.DIALOG_INVASION_DISPATCH_TITLE, new Dimension(760, 560));
        this.colony = colony;
        this.alert = alert;
        this.worldDay = worldDay;
        this.worldHour = worldHour;
        this.onDispatched = onDispatched;
        availableByType.putAll(ColonyInvasionService.getAvailableDefenderCountsByType(colony));

        JPanel headerPanel = new JPanel(new GridLayout(1, 3, 16, 8));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        styleHeaderLabel(winChanceLabel);
        styleHeaderLabel(deadlineLabel);
        styleHeaderLabel(partySizeLabel);
        headerPanel.add(winChanceLabel);
        headerPanel.add(deadlineLabel);
        headerPanel.add(partySizeLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        mainPanel.setBackground(AssetStyles.UI_BG_PRIMARY);

        Species species = alert.getSpecies() != null ? alert.getSpecies() : GameConstants.TYPE_ANT_LION;
        mainPanel.add(CritterPanelUtils.buildTargetHeading(species, LanguageStrings.INVASION_LABEL_THREAT));
        mainPanel.add(Box.createVerticalStrut(12));

        JPanel antGrid = new JPanel(new GridLayout(0, 1, 8, 8));
        antGrid.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR),
                LanguageStrings.get(LanguageStrings.INVASION_PERSONNEL)));
        antGrid.setBackground(AssetStyles.UI_BG_PRIMARY);
        for (AntType type : INVASION_ANT_TYPES) {
            antGrid.add(buildAntRow(type));
        }
        mainPanel.add(antGrid);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        dispatchBtn = new JButton(LanguageStrings.get(LanguageStrings.INVASION_DISPATCH));
        dispatchBtn.setFocusable(false);
        dispatchBtn.addActionListener(e -> attemptDispatch());
        addToSouthPanel(dispatchBtn);

        applyDefaultSelection();
        updatePreview();
    }

    private JPanel buildAntRow(AntType type) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        row.setBackground(AssetStyles.UI_BG_PRIMARY);
        int available = availableByType.getOrDefault(type, 0);
        row.add(CritterPanelUtils.buildAntTypeAvailabilityLabel(type, available));

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, available, 1));
        spinner.setFocusable(false);
        AssetStyles.styleSpinner(spinner);
        spinner.setPreferredSize(AssetStyles.preferredSpinnerSize(100));
        spinner.setEnabled(available > 0);
        spinner.addChangeListener(e -> {
            if (updating) {
                return;
            }
            updatePreview();
        });
        antSpinners.put(type, spinner);
        row.add(spinner);

        JButton maxBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_MAX));
        maxBtn.setFocusable(false);
        AssetStyles.styleCompactButton(maxBtn);
        maxBtn.setEnabled(available > 0);
        maxBtn.addActionListener(e -> spinner.setValue(available));
        row.add(maxBtn);
        return row;
    }

    private Map<AntType, Integer> getSelectedCounts() {
        Map<AntType, Integer> counts = new HashMap<>();
        for (Map.Entry<AntType, JSpinner> entry : antSpinners.entrySet()) {
            counts.put(entry.getKey(), (Integer) entry.getValue().getValue());
        }
        return counts;
    }

    private void updatePreview() {
        ColonyInvasionService.InvasionDispatchPreview preview =
                ColonyInvasionService.previewDefense(colony, alert, getSelectedCounts(), worldDay, worldHour);
        winChanceLabel.setText(LanguageStrings.format(
                LanguageStrings.HUNT_WIN_CHANCE_FMT, Math.round(preview.winChance * 100f)));
        deadlineLabel.setText(LanguageStrings.format(
                LanguageStrings.INVASION_DEADLINE_FMT, AssetStyles.formatNumber(preview.hoursRemaining)));
        partySizeLabel.setText(LanguageStrings.format(
                LanguageStrings.INVASION_PARTY_SIZE_FMT, AssetStyles.formatNumber(preview.partySize)));
        dispatchBtn.setEnabled(preview.partySize > 0 && ColonyInvasionService.canDispatchDefense(colony, alert));
    }

    private void applyDefaultSelection() {
        updating = true;
        try {
            for (AntType type : INVASION_ANT_TYPES) {
                int available = availableByType.getOrDefault(type, 0);
                if (available > 0) {
                    JSpinner spinner = antSpinners.get(type);
                    if (spinner != null) {
                        spinner.setValue(Math.min(available, Math.max(1, available / 4)));
                    }
                }
            }
        } finally {
            updating = false;
        }
    }

    private void attemptDispatch() {
        if (!ColonyInvasionService.dispatchDefense(colony, alert.getId(), getSelectedCounts(), worldDay, worldHour)) {
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.get(LanguageStrings.INVASION_DISPATCH_FAILED),
                    LanguageStrings.get(LanguageStrings.DIALOG_INVASION_DISPATCH_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onDispatched != null) {
            onDispatched.accept(alert.getId());
        }
        requestClose();
    }

    @Override
    protected boolean shouldDelegateEscapeToGamePanel() {
        return false;
    }

    @Override
    protected void refreshDialog() {
        updatePreview();
    }

    private static void styleHeaderLabel(JLabel label) {
        label.setFont(AssetStyles.FONT_BOLD.deriveFont(13f));
        label.setForeground(AssetStyles.FONT_COLOR);
    }
}
