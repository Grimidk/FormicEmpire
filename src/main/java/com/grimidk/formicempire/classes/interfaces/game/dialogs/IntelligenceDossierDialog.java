package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.dynasty.IntelFact;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyIntelligenceService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyStatService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;
import com.grimidk.formicempire.classes.interfaces.ui.util.UiDialogUtils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class IntelligenceDossierDialog extends JDialog {

    private final Dynasty viewer;
    private final Dynasty target;
    private final DynastyIntelligenceService intel;

    public IntelligenceDossierDialog(Component parent, Dynasty viewer, Dynasty target) {
        super(resolveOwner(parent),
                LanguageStrings.get(LanguageStrings.INTEL_DOSSIER_TITLE),
                ModalityType.MODELESS);
        this.viewer = viewer;
        this.target = target;
        this.intel = viewer != null ? viewer.getIntelligenceService() : null;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        buildUi();
        UiDialogUtils.prepareDialog(this, parent);
        pack();
        setSize(Math.max(getWidth(), 520), Math.max(getHeight(), 480));
        setLocationRelativeTo(parent);
    }

    private static Window resolveOwner(Component parent) {
        if (parent instanceof Window window) {
            return window;
        }
        return SwingUtilities.getWindowAncestor(parent);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        root.setBackground(AssetStyles.BACKGROUND_COLOR);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        double level = viewer.getIntelligenceToward(target.getId());
        int spies = intel != null ? intel.countSpiesToward(target) : 0;
        header.add(label(LanguageStrings.format(
                LanguageStrings.INTEL_LEVEL_FMT, String.format("%.1f", level)),
                true, GameConstants.ICON_STAT_INTELLIGENCE));
        header.add(Box.createVerticalStrut(4));
        header.add(label(LanguageStrings.format(
                LanguageStrings.INTEL_CI_FMT,
                LanguageStrings.formatNumber((int) Math.round(target.getCounterIntelligence()))),
                false, GameConstants.ICON_STAT_COUNTER_INTELLIGENCE));
        header.add(Box.createVerticalStrut(4));
        header.add(label(LanguageStrings.format(
                LanguageStrings.INTEL_SPIES_ASSIGNED_FMT, LanguageStrings.formatNumber(spies)),
                false, GameConstants.ROLE_SPY.getIcon()));
        header.add(Box.createVerticalStrut(4));
        header.add(label(LanguageStrings.format(
                LanguageStrings.INTEL_CI_FMT,
                LanguageStrings.formatNumber((int) Math.round(viewer.getCounterIntelligence()))),
                false, GameConstants.ICON_STAT_COUNTER_INTELLIGENCE));

        root.add(header, BorderLayout.NORTH);

        JPanel tiers = new JPanel();
        tiers.setLayout(new BoxLayout(tiers, BoxLayout.Y_AXIS));
        tiers.setOpaque(false);
        tiers.add(tierRow(IntelFact.RANK_COLONIES, LanguageStrings.INTEL_TIER_1, describeRankColonies()));
        tiers.add(tierRow(IntelFact.SPECIES_ASSIMILATIONS, LanguageStrings.INTEL_TIER_2, describeSpecies()));
        tiers.add(tierRow(IntelFact.POPULATION, LanguageStrings.INTEL_TIER_3, describePopulation()));
        tiers.add(tierRow(IntelFact.MILITARY, LanguageStrings.INTEL_TIER_4, describeMilitary()));
        tiers.add(tierRow(IntelFact.RESOURCES, LanguageStrings.INTEL_TIER_5, describeResources()));
        tiers.add(tierRow(IntelFact.BUILDINGS, LanguageStrings.INTEL_TIER_6, describeBuildings()));
        tiers.add(tierRow(IntelFact.UPGRADES, LanguageStrings.INTEL_TIER_7, describeUpgrades()));
        tiers.add(tierRow(IntelFact.ECONOMY_DIPLOMACY, LanguageStrings.INTEL_TIER_8, describeEconomy()));
        tiers.add(tierRow(IntelFact.COLONY_BREAKDOWN, LanguageStrings.INTEL_TIER_9, describeColonies()));
        tiers.add(tierRow(IntelFact.THEFT, LanguageStrings.INTEL_TIER_10,
                LanguageStrings.get(LanguageStrings.INTEL_TIER_10)));

        JScrollPane scroll = new JScrollPane(tiers);
        scroll.setBorder(AssetStyles.INTERNAL_BORDER);
        scroll.getViewport().setBackground(AssetStyles.BACKGROUND_COLOR);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel tierRow(IntelFact fact, String titleKey, String detail) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(true);
        row.setBackground(AssetStyles.BACKGROUND_COLOR);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AssetStyles.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        boolean unlocked = intel != null && intel.canSee(target, fact);
        JLabel title = new JLabel(LanguageStrings.get(titleKey)
                + " (" + fact.getMinIntelligence() + "%)");
        title.setForeground(AssetStyles.FONT_COLOR_HEADER);
        title.setFont(title.getFont().deriveFont(Font.BOLD));

        JLabel body = new JLabel();
        body.setForeground(AssetStyles.FONT_COLOR);
        if (unlocked) {
            body.setText("<html>" + detail.replace("\n", "<br>") + "</html>");
        } else {
            body.setText(LanguageStrings.get(LanguageStrings.INTEL_REDACTED));
            body.setOpaque(true);
            body.setBackground(redactFill());
            body.setForeground(redactFill());
            body.setHorizontalAlignment(SwingConstants.LEFT);
            body.setPreferredSize(new Dimension(200, AssetStyles.MIN_CONTROL_HIT_SIZE));
        }

        row.add(title, BorderLayout.NORTH);
        row.add(body, BorderLayout.CENTER);
        return row;
    }

    private static Color redactFill() {
        return AssetStyles.FONT_COLOR;
    }

    private JLabel label(String text, boolean bold, Icon icon) {
        JLabel label = new JLabel(text, icon, SwingConstants.LEFT);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setIconTextGap(8);
        if (bold) {
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private String describeRankColonies() {
        String rank = target.getRank() != null ? target.getRank().getName() : "?";
        return rank + " — " + LanguageStrings.formatNumber(target.getColonies().size());
    }

    private String describeSpecies() {
        String species = target.getSpecies() != null ? target.getSpecies().getName() : "?";
        return species + " — " + LanguageStrings.formatNumber(target.getCompletedAssimilations().size());
    }

    private String describePopulation() {
        DynastyStatService stats = target.getStatService();
        if (stats == null) {
            return LanguageStrings.formatNumber(0);
        }
        return LanguageStrings.formatNumber(stats.getTotalPopulation(target));
    }

    private String describeMilitary() {
        return LanguageStrings.formatNumber(target.getMilitaryPower());
    }

    private String describeResources() {
        DynastyStatService stats = target.getStatService();
        if (stats == null) {
            return "";
        }
        Map<ResourceType, Integer> resources = stats.getGlobalResources(target);
        StringBuilder sb = new StringBuilder();
        sb.append(LanguageStrings.get(LanguageStrings.TOOLTIP_RESEARCH_POINTS)).append(": ")
                .append(LanguageStrings.formatNumber(target.getResearchPoints()));
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            sb.append("\n").append(entry.getKey().getName()).append(": ")
                    .append(LanguageStrings.formatNumber(entry.getValue()));
        }
        return sb.toString();
    }

    private String describeBuildings() {
        StringBuilder sb = new StringBuilder();
        Colony capital = target.getCapital();
        if (capital == null) {
            return "-";
        }
        for (Building building : capital.getUnlockedBuildings()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(building.getName());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private String describeUpgrades() {
        StringBuilder sb = new StringBuilder();
        for (Upgrade upgrade : target.getUnlockedUpgrades()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(upgrade.getName());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private String describeEconomy() {
        return LanguageStrings.formatNumber(target.getMilitaryPower())
                + " / " + LanguageStrings.formatNumber(target.getResearchPoints());
    }

    private String describeColonies() {
        StringBuilder sb = new StringBuilder();
        for (Colony colony : target.getColonies()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(colony.getName()).append(": ")
                    .append(LanguageStrings.formatNumber(colony.getAntTotal()));
        }
        return sb.toString();
    }
}
