package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.hunt.HuntExpedition;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyHuntService;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleState;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class HuntsPanel extends JPanel {

    private static final List<AntType> HUNT_ANT_TYPES = List.of(
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS);

    private final Colony colony;
    private final Engine engine;
    private final BiConsumer<Colony, Integer> openBattleCallback;
    private final JFrame dialogOwner;
    private final JPanel contentPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    public HuntsPanel(JFrame dialogOwner, Colony colony, Engine engine, BiConsumer<Colony, Integer> openBattleCallback) {
        this.dialogOwner = dialogOwner;
        this.colony = colony;
        this.engine = engine;
        this.openBattleCallback = openBattleCallback;

        setLayout(new BorderLayout());
        setBackground(AssetStyles.UI_BG_PRIMARY);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AssetStyles.UI_BG_PRIMARY);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        statusLabel.setFont(AssetStyles.FONT_NORMAL);
        statusLabel.setForeground(AssetStyles.FONT_COLOR);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getViewport().setBackground(AssetStyles.UI_BG_PRIMARY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.setBackground(AssetStyles.UI_BG_SECONDARY);
        south.add(statusLabel);

        add(scrollPane, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        refresh();
    }

    public void refresh() {
        contentPanel.removeAll();
        if (colony == null) {
            statusLabel.setText(LanguageStrings.get(LanguageStrings.HUNT_STATUS_UNAVAILABLE));
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }
        if (!colony.hasUpgrade(GameUnlocks.ABILITY_HUNTS)) {
            statusLabel.setText(LanguageStrings.get(LanguageStrings.HUNT_STATUS_NEED_SCOUTS));
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        World world = engine != null ? engine.getWorld() : null;
        int worldDay = world != null ? world.getDay() : 0;
        int activeExpeditions = colony.getActiveHuntExpeditions().size();

        if (colony.getDynasty() != null && colony.getDynasty().isAtWar()) {
            statusLabel.setText(LanguageStrings.get(LanguageStrings.HUNT_STATUS_AT_WAR));
        } else {
            statusLabel.setText(LanguageStrings.format(
                    LanguageStrings.HUNT_TARGETS_CAPACITY_FMT,
                    colony.getKnownHuntTargets().size(),
                    ColonyHuntService.getKnownTargetCapacity(colony))
                    + (activeExpeditions > 0
                            ? " " + LanguageStrings.format(LanguageStrings.HUNT_ACTIVE_EXPEDITIONS_FMT, activeExpeditions)
                            : ""));
        }

        if (colony.getKnownHuntTargets().isEmpty()) {
            JLabel empty = new JLabel(LanguageStrings.get(LanguageStrings.HUNT_NO_TARGETS));
            empty.setFont(AssetStyles.FONT_NORMAL);
            empty.setForeground(AssetStyles.FONT_COLOR);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(empty);
        } else {
            for (KnownHuntTarget target : colony.getKnownHuntTargets()) {
                HuntExpedition expedition = colony.getHuntExpeditionForTarget(target.getId());
                contentPanel.add(expedition != null
                        ? buildActiveTargetRow(target, expedition, worldDay)
                        : buildAvailableTargetRow(target, worldDay));
                contentPanel.add(Box.createVerticalStrut(8));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildActiveTargetRow(KnownHuntTarget target, HuntExpedition expedition, int worldDay) {
        Species species = target.getSpecies() != null ? target.getSpecies() : GameConstants.TYPE_COCKROACH;

        JPanel detailColumn = new JPanel();
        detailColumn.setLayout(new BoxLayout(detailColumn, BoxLayout.Y_AXIS));
        detailColumn.setOpaque(false);

        int daysLeft = Math.max(0, target.getEscapeWorldDay() - worldDay);
        detailColumn.add(CritterPanelUtils.buildDetailLine(
                LanguageStrings.format(LanguageStrings.HUNT_TARGET_ESCAPES_FMT, daysLeft)));

        String phaseKey = expedition.getPhase() == HuntExpedition.Phase.FIGHTING
                ? LanguageStrings.HUNT_EXPEDITION_PHASE_FIGHTING
                : LanguageStrings.HUNT_EXPEDITION_PHASE_TRAVELING;
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildBoldDetailLine(LanguageStrings.get(phaseKey)));

        Map<AntType, Integer> sentCounts = ColonyHuntService.countPartyByType(expedition.getParty());
        detailColumn.add(Box.createVerticalStrut(6));
        detailColumn.add(CritterPanelUtils.buildLabeledDetailRow(
                LanguageStrings.HUNT_PERSONNEL,
                CritterPanelUtils.buildAntTypeCountsRow(sentCounts, HUNT_ANT_TYPES)));

        if (expedition.getPhase() == HuntExpedition.Phase.TRAVELING) {
            int pct = Math.round(expedition.getTravelProgressRatio() * 100f);
            int hoursLeft = Math.max(0, Math.round(expedition.getTravelHoursRemaining()));
            detailColumn.add(Box.createVerticalStrut(4));
            detailColumn.add(CritterPanelUtils.buildDetailLine(LanguageStrings.format(
                    LanguageStrings.HUNT_EXPEDITION_TRAVEL_REMAINING_FMT,
                    AssetStyles.formatNumber(hoursLeft),
                    pct)));
        } else if (expedition.getPhase() == HuntExpedition.Phase.FIGHTING) {
            int totalSent = expedition.getParty().size();
            int living = totalSent;
            int bugPct = 100;
            HuntBattleState battle = HuntCreatureCombatService.getState(colony, target.getId());
            if (battle != null) {
                bugPct = Math.round(battle.getBugHealthRatio() * 100f);
                living = battle.livingHunters().size();
            }
            detailColumn.add(Box.createVerticalStrut(4));
            detailColumn.add(CritterPanelUtils.buildDetailLine(LanguageStrings.format(
                    LanguageStrings.HUNT_EXPEDITION_COMBAT_DETAIL_FMT,
                    bugPct,
                    AssetStyles.formatNumber(living),
                    AssetStyles.formatNumber(totalSent))));
        }

        List<Ant> previewParty = livingPartyForPreview(expedition);
        ColonyHuntService.HuntDispatchPreview preview = ColonyHuntService.previewDispatch(
                colony, target, ColonyHuntService.countPartyByType(previewParty));
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildDetailLine(LanguageStrings.format(
                LanguageStrings.HUNT_WIN_CHANCE_FMT, Math.round(preview.winChance * 100f))));
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildLabeledDetailRow(
                LanguageStrings.HUNT_LABEL_REWARDS,
                CritterPanelUtils.buildRewardIconsPanel(
                        preview.rewardProtein, preview.rewardMushrooms, preview.rewardResearch)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setBackground(AssetStyles.UI_BG_SECONDARY);
        if (expedition.getPhase() == HuntExpedition.Phase.FIGHTING && openBattleCallback != null) {
            JButton battleBtn = new JButton(LanguageStrings.get(LanguageStrings.HUNT_OPEN_BATTLE));
            AssetStyles.styleCompactButton(battleBtn);
            battleBtn.addActionListener(e -> openBattleCallback.accept(colony, target.getId()));
            actions.add(battleBtn);
        }

        return CritterPanelUtils.buildSpeciesDetailColumnRow(species, detailColumn, actions, 260);
    }

    private JPanel buildAvailableTargetRow(KnownHuntTarget target, int worldDay) {
        Species species = target.getSpecies() != null ? target.getSpecies() : GameConstants.TYPE_COCKROACH;
        int daysLeft = Math.max(0, target.getEscapeWorldDay() - worldDay);

        JPanel detailColumn = new JPanel();
        detailColumn.setLayout(new BoxLayout(detailColumn, BoxLayout.Y_AXIS));
        detailColumn.setOpaque(false);
        detailColumn.add(CritterPanelUtils.buildDetailLine(
                LanguageStrings.format(LanguageStrings.HUNT_TARGET_ESCAPES_FMT, daysLeft)));

        ColonyHuntService.HuntDispatchPreview preview =
                ColonyHuntService.previewDispatch(colony, target, Map.of());
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildLabeledDetailRow(
                LanguageStrings.HUNT_LABEL_REWARDS,
                CritterPanelUtils.buildRewardIconsPanel(
                        preview.rewardProtein, preview.rewardMushrooms, preview.rewardResearch)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setBackground(AssetStyles.UI_BG_SECONDARY);

        JButton setupBtn = new JButton(LanguageStrings.get(LanguageStrings.HUNT_SETUP_PARTY));
        AssetStyles.styleCompactButton(setupBtn);
        setupBtn.setEnabled(ColonyHuntService.canDispatchHunt(colony, target.getId())
                && ColonyHuntService.countAvailableHunters(colony) > 0);
        setupBtn.addActionListener(e -> openDispatchDialog(target));

        actions.add(setupBtn);
        return CritterPanelUtils.buildSpeciesDetailColumnRow(species, detailColumn, actions, 120);
    }

    private List<Ant> livingPartyForPreview(HuntExpedition expedition) {
        List<Ant> living = new ArrayList<>();
        for (Ant ant : expedition.getParty()) {
            if (ant != null && ant.isAlive()) {
                living.add(ant);
            }
        }
        return living;
    }

    private void openDispatchDialog(KnownHuntTarget target) {
        HuntDispatchDialog dialog = new HuntDispatchDialog(dialogOwner, colony, target, this::refresh);
        dialog.showDialog();
    }
}
