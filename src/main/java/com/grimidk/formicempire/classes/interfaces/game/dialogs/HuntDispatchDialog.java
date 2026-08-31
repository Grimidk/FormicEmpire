package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyHuntService;
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

public final class HuntDispatchDialog extends ZeroDialog {

    private static final List<AntType> HUNT_ANT_TYPES = List.of(
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS);

    private final Colony colony;
    private final KnownHuntTarget target;
    private final Runnable onDispatched;
    private final Map<AntType, JSpinner> antSpinners = new HashMap<>();
    private final Map<AntType, Integer> availableByType = new HashMap<>();
    private final int maxParty;
    private boolean updating;

    private final JLabel travelLabel = new JLabel(" ");
    private final JLabel winChanceLabel = new JLabel(" ");
    private final JPanel rewardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JLabel partyLimitLabel = new JLabel(" ");
    private final JButton dispatchBtn;

    public HuntDispatchDialog(JFrame owner, Colony colony, KnownHuntTarget target, Runnable onDispatched) {
        super(owner, LanguageStrings.DIALOG_HUNT_DISPATCH_TITLE, new Dimension(760, 560));
        this.colony = colony;
        this.target = target;
        this.onDispatched = onDispatched;
        this.maxParty = ColonyHuntService.maxPartyCapacity(colony);
        availableByType.putAll(ColonyHuntService.getAvailableHunterCountsByType(colony));

        JPanel headerPanel = new JPanel(new GridLayout(1, 4, 16, 8));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        headerPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        styleHeaderLabel(travelLabel);
        styleHeaderLabel(winChanceLabel);
        styleHeaderLabel(partyLimitLabel);
        rewardsPanel.setOpaque(false);
        headerPanel.add(travelLabel);
        headerPanel.add(winChanceLabel);
        headerPanel.add(rewardsPanel);
        headerPanel.add(partyLimitLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        mainPanel.setBackground(AssetStyles.UI_BG_PRIMARY);

        Species species = target.getSpecies() != null ? target.getSpecies() : GameConstants.TYPE_COCKROACH;
        mainPanel.add(CritterPanelUtils.buildTargetHeading(species, LanguageStrings.HUNT_LABEL_TARGET));
        mainPanel.add(Box.createVerticalStrut(12));

        JPanel antGrid = new JPanel(new GridLayout(0, 1, 8, 8));
        antGrid.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR),
                LanguageStrings.get(LanguageStrings.HUNT_PERSONNEL)));
        antGrid.setBackground(AssetStyles.UI_BG_PRIMARY);
        for (AntType type : HUNT_ANT_TYPES) {
            antGrid.add(buildAntRow(type));
        }
        mainPanel.add(antGrid);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        dispatchBtn = new JButton(LanguageStrings.get(LanguageStrings.HUNT_DISPATCH));
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

        int initial = 0;
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initial, 0, available, 1));
        spinner.setFocusable(false);
        AssetStyles.styleSpinner(spinner);
        spinner.setPreferredSize(AssetStyles.preferredSpinnerSize(100));
        spinner.setEnabled(available > 0);
        spinner.addChangeListener(e -> {
            if (updating) {
                return;
            }
            enforcePartyLimit(type);
            updatePreview();
        });
        antSpinners.put(type, spinner);
        row.add(spinner);

        JButton maxBtn = new JButton(LanguageStrings.get(LanguageStrings.UI_MAX));
        maxBtn.setFocusable(false);
        AssetStyles.styleCompactButton(maxBtn);
        maxBtn.setEnabled(available > 0);
        maxBtn.addActionListener(e -> setMaxForType(type));
        row.add(maxBtn);
        return row;
    }

    private void setMaxForType(AntType type) {
        JSpinner spinner = antSpinners.get(type);
        if (spinner == null) {
            return;
        }
        int available = availableByType.getOrDefault(type, 0);
        int others = getTotalSelected() - (Integer) spinner.getValue();
        int room = Math.max(0, maxParty - others);
        spinner.setValue(Math.min(available, room));
        updatePreview();
    }

    private void enforcePartyLimit(AntType changedType) {
        int total = getTotalSelected();
        if (total <= maxParty) {
            return;
        }
        JSpinner spinner = antSpinners.get(changedType);
        if (spinner == null) {
            return;
        }
        updating = true;
        try {
            int val = (Integer) spinner.getValue();
            spinner.setValue(Math.max(0, val - (total - maxParty)));
        } finally {
            updating = false;
        }
    }

    private int getTotalSelected() {
        int total = 0;
        for (JSpinner spinner : antSpinners.values()) {
            total += (Integer) spinner.getValue();
        }
        return total;
    }

    private Map<AntType, Integer> getSelectedCounts() {
        Map<AntType, Integer> counts = new HashMap<>();
        for (Map.Entry<AntType, JSpinner> entry : antSpinners.entrySet()) {
            counts.put(entry.getKey(), (Integer) entry.getValue().getValue());
        }
        return counts;
    }

    private void updatePreview() {
        ColonyHuntService.HuntDispatchPreview preview =
                ColonyHuntService.previewDispatch(colony, target, getSelectedCounts());
        travelLabel.setText(LanguageStrings.format(
                LanguageStrings.HUNT_TRAVEL_ESTIMATE_FMT, AssetStyles.formatNumber(Math.round(preview.travelHours))));
        winChanceLabel.setText(LanguageStrings.format(
                LanguageStrings.HUNT_WIN_CHANCE_FMT, Math.round(preview.winChance * 100f)));
        rewardsPanel.removeAll();
        JLabel rewardsHeading = new JLabel(LanguageStrings.get(LanguageStrings.HUNT_LABEL_REWARDS) + ":");
        styleHeaderLabel(rewardsHeading);
        rewardsPanel.add(rewardsHeading);
        rewardsPanel.add(CritterPanelUtils.buildRewardIconsPanel(
                preview.rewardProtein, preview.rewardMushrooms, preview.rewardResearch));
        partyLimitLabel.setText(LanguageStrings.format(
                LanguageStrings.HUNT_PARTY_LIMIT_FMT,
                AssetStyles.formatNumber(preview.partySize),
                AssetStyles.formatNumber(maxParty)));
        dispatchBtn.setEnabled(preview.partySize > 0 && ColonyHuntService.canDispatchHunt(colony, target.getId()));
    }

    private void applyDefaultSelection() {
        updating = true;
        try {
            for (AntType type : HUNT_ANT_TYPES) {
                if (availableByType.getOrDefault(type, 0) > 0) {
                    JSpinner spinner = antSpinners.get(type);
                    if (spinner != null) {
                        spinner.setValue(1);
                    }
                    break;
                }
            }
        } finally {
            updating = false;
        }
    }

    private void attemptDispatch() {
        if (!ColonyHuntService.dispatchHunt(colony, target.getId(), getSelectedCounts())) {
            UiOptionPane.showMessageDialog(this,
                    LanguageStrings.get(LanguageStrings.HUNT_DISPATCH_FAILED),
                    LanguageStrings.get(LanguageStrings.DIALOG_HUNT_DISPATCH_TITLE),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (onDispatched != null) {
            onDispatched.run();
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
