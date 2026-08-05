package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiOptionPane;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.interfaces.MainFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class AbilitiesDialog extends ZeroDialog {

    private static final int ACTION_PANEL_WIDTH = 130;

    private final Colony colony;
    private final JPanel listPanel;
    private final JLabel researchPointsLabel;

    @FunctionalInterface
    private interface TriggerSync {
        void sync();
    }

    private record AbilityRow(JPanel panel, JButton button, JLabel costLabel) {}

    private final List<TriggerSync> triggerSyncs = new ArrayList<>();

    public AbilitiesDialog(JFrame owner, Colony colony) {
        super(owner, LanguageStrings.DIALOG_ABILITIES_TITLE, AssetStyles.DEFAULT_DIALOG_SIZE);
        this.colony = colony;

        JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        northPanel.setBackground(AssetStyles.UI_BG_SECONDARY);
        northPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        researchPointsLabel = new JLabel();
        researchPointsLabel.setFont(AssetStyles.FONT_BOLD);
        researchPointsLabel.setForeground(AssetStyles.TEXT_HEADER);
        researchPointsLabel.setIcon(GameConstants.ICON_RESEARCH);
        researchPointsLabel.setIconTextGap(6);
        researchPointsLabel.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_RESEARCH_POINTS));
        northPanel.add(researchPointsLabel);
        add(northPanel, BorderLayout.NORTH);

        listPanel = new JPanel();
        listPanel.setBackground(AssetStyles.UI_BG_PRIMARY);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        registerCloseKey(KeyEvent.VK_C);
    }

    @Override
    protected void refreshDialog() {
        listPanel.removeAll();
        triggerSyncs.clear();

        updateResearchPointsLabel();

        boolean hasAnyAbility = false;

        if (colony.hasUpgrade(GameUnlocks.ABILITY_FORCED_FLIGHT)) {
            int currentCost = colony.getNuptialFlightCost();
            AbilityRow row = createAbilityPanel(
                    LanguageStrings.get(LanguageStrings.ABILITY_FORCED_FLIGHT),
                    GameUnlocks.ABILITY_FORCED_FLIGHT.getIcon(),
                    LanguageStrings.format(LanguageStrings.ABILITY_FORCED_FLIGHT_DESC, currentCost),
                    createCostLabel(GameConstants.ICON_RESEARCH, currentCost),
                    e -> {
                        if (getOwner() instanceof MainFrame main) {
                            Engine engine = main.getEngine();
                            if (engine != null) {
                                World world = engine.getWorld();
                                if (world != null) {
                                    Hex targetHex = null;
                                    for (Hex h : world.getHexes()) {
                                        if (h.getColony() == colony) {
                                            targetHex = h;
                                            break;
                                        }
                                    }

                                    if (targetHex != null) {
                                        colony.forceNuptialFlight(world, targetHex);
                                        refreshDialog();
                                    } else {
                                        UiOptionPane.showMessageDialog(this,
                                                LanguageStrings.get(LanguageStrings.ABILITY_ERROR_LOCATE_COLONY));
                                    }
                                }
                            }
                        }
                    });
            triggerSyncs.add(() -> syncForcedFlight(row));
            listPanel.add(row.panel());
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT) && colony.getDynasty() != null) {
            int massCost = colony.getDynasty().getMassNuptialFlightCost();
            AbilityRow row = createAbilityPanel(
                    LanguageStrings.get(LanguageStrings.ABILITY_MASS_FLIGHT),
                    GameUnlocks.ABILITY_MASS_FLIGHT.getIcon(),
                    LanguageStrings.format(LanguageStrings.ABILITY_MASS_FLIGHT_DESC, massCost),
                    createCostLabel(GameConstants.ICON_RESEARCH, massCost),
                    e -> {
                        if (getOwner() instanceof MainFrame main) {
                            if (main.getEngine() != null && main.getEngine().getWorld() != null) {
                                colony.getDynasty().runMassNuptialFlight(main.getEngine().getWorld());
                                refreshDialog();
                            }
                        }
                    });
            triggerSyncs.add(() -> syncMassFlight(row, massCost));
            listPanel.add(row.panel());
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_PHEROMONE_STORM)) {
            int syrupCost = GameNumbers.PHEROMONE_STORM_SYRUP_COST;
            AbilityRow row = createAbilityPanel(
                    GameUnlocks.ABILITY_PHEROMONE_STORM.getName(),
                    GameUnlocks.ABILITY_PHEROMONE_STORM.getIcon(),
                    GameUnlocks.ABILITY_PHEROMONE_STORM.getDescription(),
                    createCostLabel(GameConstants.RESOURCE_SYRUP.getIcon(), syrupCost),
                    e -> {
                        if (colony.activatePheromoneStorm()) {
                            refreshDialog();
                        }
                    });
            triggerSyncs.add(() -> syncResourceAbility(
                    row,
                    syrupCost,
                    GameConstants.RESOURCE_SYRUP,
                    colony::isPheromoneStormActive,
                    colony.getSyrups()));
            listPanel.add(row.panel());
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (colony.hasUpgrade(GameUnlocks.ABILITY_CREATINE_DIET)) {
            int proteinCost = GameNumbers.CREATINE_DIET_PROTEIN_COST;
            AbilityRow row = createAbilityPanel(
                    GameUnlocks.ABILITY_CREATINE_DIET.getName(),
                    GameUnlocks.ABILITY_CREATINE_DIET.getIcon(),
                    GameUnlocks.ABILITY_CREATINE_DIET.getDescription(),
                    createCostLabel(GameConstants.RESOURCE_MEAT.getIcon(), proteinCost),
                    e -> {
                        if (colony.activateCreatineDiet()) {
                            refreshDialog();
                        }
                    });
            triggerSyncs.add(() -> syncResourceAbility(
                    row,
                    proteinCost,
                    GameConstants.RESOURCE_MEAT,
                    colony::isCreatineDietActive,
                    colony.getProtein()));
            listPanel.add(row.panel());
            listPanel.add(Box.createVerticalStrut(10));
            hasAnyAbility = true;
        }

        if (!hasAnyAbility) {
            JLabel empty = new JLabel(LanguageStrings.get(LanguageStrings.ABILITY_NO_ABILITIES));
            empty.setForeground(AssetStyles.TEXT_NORMAL);
            empty.setFont(AssetStyles.FONT_NORMAL);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(empty);
            listPanel.add(Box.createVerticalGlue());
        }

        syncAllTriggers();
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void syncForcedFlight(AbilityRow row) {
        int cost = colony.getNuptialFlightCost();
        row.costLabel().setText(AssetStyles.formatNumber(cost));
        applyTriggerState(row.button(), getNuptialFailureReason(cost));
    }

    private void syncMassFlight(AbilityRow row, int cost) {
        row.costLabel().setText(AssetStyles.formatNumber(cost));
        String fail = colony.getResearchPoints() >= cost
                ? null
                : LanguageStrings.format(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RP, cost);
        applyTriggerState(row.button(), fail);
    }

    private void syncResourceAbility(AbilityRow row, int cost, ResourceType resourceType,
            java.util.function.BooleanSupplier activeCheck, int available) {
        row.costLabel().setText(AssetStyles.formatNumber(cost));
        String fail;
        if (activeCheck.getAsBoolean()) {
            fail = LanguageStrings.get(LanguageStrings.ABILITY_ERROR_ALREADY_ACTIVE);
        } else if (available >= cost) {
            fail = null;
        } else {
            fail = String.format(
                    LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RESOURCE),
                    cost,
                    resourceType.getName());
        }
        applyTriggerState(row.button(), fail);
    }

    private static void applyTriggerState(JButton button, String failureReason) {
        boolean enabled = failureReason == null;
        button.setEnabled(enabled);
        button.setToolTipText(enabled ? null : failureReason);
    }

    private void syncAllTriggers() {
        for (TriggerSync sync : triggerSyncs) {
            sync.sync();
        }
    }

    private String getNuptialFailureReason(int cost) {
        if (colony.getResearchPoints() < cost) {
            return LanguageStrings.format(LanguageStrings.ABILITY_ERROR_NOT_ENOUGH_RP, cost);
        }
        if (!ColonyLabourService.meetsNuptialRequirements(colony)) {
            if (colony.getDrones().isEmpty()) {
                return LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NO_DRONES);
            }
            return LanguageStrings.get(LanguageStrings.ABILITY_ERROR_NO_BREEDERS);
        }
        return null;
    }

    private void updateResearchPointsLabel() {
        researchPointsLabel.setText(AssetStyles.formatNumber(colony.getResearchPoints()));
    }

    private JLabel createCostLabel(Icon icon, int amount) {
        JLabel label = new JLabel(AssetStyles.formatNumber(amount), icon, SwingConstants.CENTER);
        label.setForeground(AssetStyles.TEXT_NORMAL);
        label.setFont(AssetStyles.FONT_BOLD);
        label.setIconTextGap(6);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private AbilityRow createAbilityPanel(String title, Icon titleIcon, String desc, JLabel costLabel, ActionListener action) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(AssetStyles.UI_BG_SECONDARY);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        TitledBorder border = new TitledBorder(AssetStyles.PANEL_BORDER, title);
        border.setTitleColor(AssetStyles.TEXT_HEADER);
        border.setTitleFont(AssetStyles.FONT_BOLD);
        panel.setBorder(border);

        if (titleIcon != null) {
            JLabel iconLabel = new JLabel(titleIcon);
            iconLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
            panel.add(iconLabel, BorderLayout.WEST);
        }

        JTextArea descriptionArea = new JTextArea(desc);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setEnabled(false);
        descriptionArea.setDisabledTextColor(AssetStyles.TEXT_NORMAL);
        descriptionArea.setBackground(panel.getBackground());
        descriptionArea.setForeground(AssetStyles.TEXT_NORMAL);
        descriptionArea.setFont(AssetStyles.FONT_NORMAL);
        panel.add(descriptionArea, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new EmptyBorder(0, 0, 0, 5));

        costLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = new JButton(LanguageStrings.get(LanguageStrings.UI_TRIGGER));
        AssetStyles.styleButton(btn);
        btn.setFocusable(false);
        btn.addActionListener(action);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension btnSize = new Dimension(ACTION_PANEL_WIDTH, btn.getPreferredSize().height);
        btn.setPreferredSize(btnSize);
        btn.setMinimumSize(btnSize);
        btn.setMaximumSize(btnSize);

        actionPanel.add(costLabel);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        actionPanel.add(btn);
        panel.add(actionPanel, BorderLayout.EAST);

        return new AbilityRow(panel, btn, costLabel);
    }

    @Override
    public void liveUpdate() {
        if (!isShowing()) {
            return;
        }
        updateResearchPointsLabel();
        syncAllTriggers();
    }
}
