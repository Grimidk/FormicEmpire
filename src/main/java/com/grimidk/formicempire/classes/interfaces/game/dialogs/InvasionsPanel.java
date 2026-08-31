package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.invasion.InvasionAlert;
import com.grimidk.formicempire.classes.entities.invasion.InvasionDefense;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyHuntService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyInvasionService;
import com.grimidk.formicempire.classes.entities.services.colony.HuntBattleState;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class InvasionsPanel extends JPanel {

    private static final List<AntType> INVASION_ANT_TYPES = List.of(
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS);

    private final JFrame dialogOwner;
    private final Colony colony;
    private final Engine engine;
    private final BiConsumer<Colony, Integer> openBattleCallback;
    private final Runnable onChanged;
    private final JPanel contentPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    public InvasionsPanel(JFrame dialogOwner, Colony colony, Engine engine,
            BiConsumer<Colony, Integer> openBattleCallback, Runnable onChanged) {
        this.dialogOwner = dialogOwner;
        this.colony = colony;
        this.engine = engine;
        this.openBattleCallback = openBattleCallback;
        this.onChanged = onChanged;

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
            statusLabel.setText(LanguageStrings.get(LanguageStrings.INVASION_STATUS_UNAVAILABLE));
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }
        if (colony.getDynasty() != null && colony.getDynasty().isAtWar()) {
            statusLabel.setText(LanguageStrings.get(LanguageStrings.INVASION_STATUS_AT_WAR));
        } else {
            int activeBattles = colony.getActiveInvasionDefenses().size();
            statusLabel.setText(LanguageStrings.get(LanguageStrings.INVASION_STATUS_PEACE)
                    + (activeBattles > 0
                            ? " " + LanguageStrings.format(LanguageStrings.INVASION_ACTIVE_BATTLES_FMT, activeBattles)
                            : ""));
        }

        World world = engine != null ? engine.getWorld() : null;
        int worldDay = world != null ? world.getDay() : 0;
        int worldHour = world != null ? world.getHour() : 0;

        boolean anyRows = false;
        for (InvasionDefense defense : colony.getActiveInvasionDefenses()) {
            InvasionAlert alert = findAlert(defense.getAlertId());
            if (alert == null) {
                continue;
            }
            contentPanel.add(buildActiveDefenseRow(alert, defense));
            contentPanel.add(Box.createVerticalStrut(8));
            anyRows = true;
        }

        List<InvasionAlert> alerts = ColonyInvasionService.getVisibleAlerts(colony);
        if (alerts.isEmpty() && !anyRows) {
            JLabel empty = new JLabel(LanguageStrings.get(LanguageStrings.INVASION_NO_ALERTS));
            empty.setFont(AssetStyles.FONT_NORMAL);
            empty.setForeground(AssetStyles.FONT_COLOR);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(empty);
        } else {
            for (InvasionAlert alert : alerts) {
                if (colony.getInvasionDefenseForAlert(alert.getId()) != null) {
                    continue;
                }
                contentPanel.add(buildAlertRow(alert, worldDay, worldHour));
                contentPanel.add(Box.createVerticalStrut(8));
            }
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildActiveDefenseRow(InvasionAlert alert, InvasionDefense defense) {
        Species species = alert.getSpecies() != null ? alert.getSpecies() : GameConstants.TYPE_ANT_LION;
        String scopeLabel = alert.getScope() == InvasionAlert.Scope.DYNASTY
                ? LanguageStrings.get(LanguageStrings.INVASION_SCOPE_DYNASTY)
                : LanguageStrings.get(LanguageStrings.INVASION_SCOPE_COLONY);

        JPanel detailColumn = new JPanel();
        detailColumn.setLayout(new BoxLayout(detailColumn, BoxLayout.Y_AXIS));
        detailColumn.setOpaque(false);
        detailColumn.add(CritterPanelUtils.buildDetailLine(scopeLabel));
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildBoldDetailLine(
                LanguageStrings.get(LanguageStrings.INVASION_EXPEDITION_PHASE_FIGHTING)));

        Map<AntType, Integer> sentCounts = ColonyHuntService.countPartyByType(defense.getParty());
        detailColumn.add(Box.createVerticalStrut(6));
        detailColumn.add(CritterPanelUtils.buildLabeledDetailRow(
                LanguageStrings.INVASION_PERSONNEL,
                CritterPanelUtils.buildAntTypeCountsRow(sentCounts, INVASION_ANT_TYPES)));

        int totalSent = defense.getParty().size();
        int living = totalSent;
        int bugPct = 100;
        HuntBattleState battle = HuntCreatureCombatService.getInvasionState(colony, alert.getId());
        if (battle != null) {
            bugPct = Math.round(battle.getBugHealthRatio() * 100f);
            living = battle.livingHunters().size();
        }
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildDetailLine(LanguageStrings.format(
                LanguageStrings.INVASION_EXPEDITION_COMBAT_DETAIL_FMT,
                bugPct,
                AssetStyles.formatNumber(living),
                AssetStyles.formatNumber(totalSent))));

        List<Ant> previewParty = livingPartyForPreview(defense);
        ColonyInvasionService.InvasionDispatchPreview preview =
                ColonyInvasionService.previewDefense(colony, alert,
                        ColonyHuntService.countPartyByType(previewParty), 0, 0);
        detailColumn.add(Box.createVerticalStrut(4));
        detailColumn.add(CritterPanelUtils.buildDetailLine(LanguageStrings.format(
                LanguageStrings.HUNT_WIN_CHANCE_FMT, Math.round(preview.winChance * 100f))));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setBackground(AssetStyles.UI_BG_SECONDARY);
        if (openBattleCallback != null) {
            JButton battleBtn = new JButton(LanguageStrings.get(LanguageStrings.INVASION_OPEN_BATTLE));
            AssetStyles.styleCompactButton(battleBtn);
            battleBtn.addActionListener(e -> openBattleCallback.accept(colony, alert.getId()));
            actions.add(battleBtn);
        }

        return CritterPanelUtils.buildSpeciesDetailColumnRow(species, detailColumn, actions, 260);
    }

    private JPanel buildAlertRow(InvasionAlert alert, int worldDay, int worldHour) {
        Species species = alert.getSpecies() != null ? alert.getSpecies() : GameConstants.TYPE_ANT_LION;
        String scopeLabel = alert.getScope() == InvasionAlert.Scope.DYNASTY
                ? LanguageStrings.get(LanguageStrings.INVASION_SCOPE_DYNASTY)
                : LanguageStrings.get(LanguageStrings.INVASION_SCOPE_COLONY);
        int hoursLeft = alert.hoursRemaining(worldDay, worldHour);

        JPanel detailColumn = new JPanel();
        detailColumn.setLayout(new BoxLayout(detailColumn, BoxLayout.Y_AXIS));
        detailColumn.setOpaque(false);
        detailColumn.add(CritterPanelUtils.buildDetailLine(LanguageStrings.format(
                LanguageStrings.INVASION_ALERT_DETAIL_FMT, scopeLabel, hoursLeft)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        actions.setBackground(AssetStyles.UI_BG_SECONDARY);

        JButton setupBtn = new JButton(LanguageStrings.get(LanguageStrings.INVASION_SETUP_PARTY));
        AssetStyles.styleCompactButton(setupBtn);
        setupBtn.setEnabled(ColonyInvasionService.canDispatchDefense(colony, alert)
                && ColonyInvasionService.countAvailableDefenders(colony) > 0);
        if (!ColonyInvasionService.canDispatchDefense(colony, alert)
                && alert.getTargetColonyId() != colony.getId()) {
            setupBtn.setToolTipText(LanguageStrings.get(LanguageStrings.INVASION_DEFEND_AT_TARGET));
        }
        setupBtn.addActionListener(e -> openDispatchDialog(alert, worldDay, worldHour));

        actions.add(setupBtn);
        return CritterPanelUtils.buildSpeciesDetailColumnRow(species, detailColumn, actions, 120);
    }

    private List<Ant> livingPartyForPreview(InvasionDefense defense) {
        List<Ant> living = new ArrayList<>();
        for (Ant ant : defense.getParty()) {
            if (ant != null && ant.isAlive()) {
                living.add(ant);
            }
        }
        return living;
    }

    private InvasionAlert findAlert(int alertId) {
        for (InvasionAlert alert : colony.getInvasionAlerts()) {
            if (alert != null && alert.getId() == alertId) {
                return alert;
            }
        }
        if (colony.getDynasty() != null) {
            Colony capital = colony.getDynasty().getCapital();
            if (capital != null && capital != colony) {
                for (InvasionAlert alert : capital.getInvasionAlerts()) {
                    if (alert != null && alert.getId() == alertId) {
                        return alert;
                    }
                }
            }
        }
        return null;
    }

    private void openDispatchDialog(InvasionAlert alert, int worldDay, int worldHour) {
        InvasionDispatchDialog dialog = new InvasionDispatchDialog(
                dialogOwner, colony, alert, worldDay, worldHour, alertId -> {
                    refresh();
                    if (onChanged != null) {
                        onChanged.run();
                    }
                    if (openBattleCallback != null) {
                        openBattleCallback.accept(colony, alertId);
                    }
                });
        dialog.showDialog();
    }
}
