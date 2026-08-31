package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

final class CritterPanelUtils {

    private CritterPanelUtils() {
    }

    static Biome resolveColonyBiome(Colony colony, Engine engine) {
        if (colony == null || engine == null) {
            return null;
        }
        World world = engine.getWorld();
        if (world == null) {
            return null;
        }
        Hex hex = world.getHexOfColony(colony);
        return hex != null ? hex.getBiome() : null;
    }

    static JPanel buildSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AssetStyles.UI_BG_SECONDARY);
        if (title != null && !title.isEmpty()) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    AssetStyles.INTERNAL_BORDER,
                    BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(AssetStyles.BORDER_COLOR),
                            title)));
        } else {
            panel.setBorder(BorderFactory.createCompoundBorder(
                    AssetStyles.INTERNAL_BORDER,
                    new EmptyBorder(8, 8, 8, 8)));
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        return panel;
    }

    static JPanel buildSpeciesSection(Species species) {
        JPanel panel = buildSectionPanel("");
        panel.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.INTERNAL_BORDER,
                new EmptyBorder(8, 8, 8, 8)));
        return panel;
    }

    static JLabel buildBodyLabel(String text) {
        JLabel label = new JLabel(wrapHtml(text));
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel buildMutedLabel(String text) {
        return buildBodyLabel(text);
    }

    static JLabel buildIconOnlyLabel(Species species) {
        ImageIcon icon = species != null ? species.getIcon() : null;
        JLabel label = new JLabel(icon);
        label.setToolTipText(species != null ? species.getName() : null);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    static JLabel buildAntTypeAvailabilityLabel(AntType type, int available) {
        JLabel label = new JLabel(AssetStyles.formatNumber(available), type.getIcon(), SwingConstants.LEFT);
        label.setToolTipText(type.getName());
        label.setIconTextGap(6);
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setPreferredSize(new Dimension(AssetStyles.MIN_CONTROL_HIT_SIZE + 48, AssetStyles.MIN_CONTROL_HIT_SIZE));
        return label;
    }

    static JPanel buildAntTypeCountsRow(Map<AntType, Integer> counts, List<AntType> order) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        boolean any = false;
        if (order != null) {
            for (AntType type : order) {
                if (type == null) {
                    continue;
                }
                int count = counts != null ? counts.getOrDefault(type, 0) : 0;
                if (count <= 0) {
                    continue;
                }
                any = true;
                row.add(buildAntTypeAvailabilityLabel(type, count));
            }
        }
        if (!any) {
            JLabel none = new JLabel(LanguageStrings.get(LanguageStrings.HUNT_REWARDS_NONE));
            none.setFont(AssetStyles.FONT_NORMAL);
            none.setForeground(AssetStyles.FONT_COLOR);
            row.add(none);
        }
        return row;
    }

    static JPanel buildLabeledDetailRow(String labelKey, JComponent content) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel(LanguageStrings.get(labelKey));
        heading.setFont(AssetStyles.FONT_NORMAL);
        heading.setForeground(AssetStyles.FONT_COLOR);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(heading);
        block.add(Box.createVerticalStrut(4));
        if (content != null) {
            content.setAlignmentX(Component.LEFT_ALIGNMENT);
            block.add(content);
        }
        return block;
    }

    static JLabel buildDetailLine(String text) {
        JLabel label = new JLabel(text);
        label.setFont(AssetStyles.FONT_NORMAL);
        label.setForeground(AssetStyles.FONT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel buildBoldDetailLine(String text) {
        JLabel label = buildDetailLine(text);
        label.setFont(AssetStyles.FONT_BOLD);
        return label;
    }

    static JLabel buildResourceAmountLabel(ResourceType resource, int amount) {
        Icon icon = resource != null ? resource.getIcon() : null;
        String tip = resource != null ? resource.getName() : null;
        JLabel label = new JLabel(AssetStyles.formatNumber(amount), icon, SwingConstants.LEFT);
        label.setToolTipText(tip);
        label.setIconTextGap(4);
        label.setFont(AssetStyles.FONT_BOLD);
        label.setForeground(AssetStyles.FONT_COLOR);
        return label;
    }

    static JLabel buildResearchAmountLabel(int amount) {
        JLabel label = new JLabel(AssetStyles.formatNumber(amount), GameConstants.ICON_RESEARCH, SwingConstants.LEFT);
        label.setToolTipText(LanguageStrings.get(LanguageStrings.TOOLTIP_RESEARCH_POINTS));
        label.setIconTextGap(4);
        label.setFont(AssetStyles.FONT_BOLD);
        label.setForeground(AssetStyles.FONT_COLOR);
        return label;
    }

    static JPanel buildRewardIconsPanel(int protein, int mushrooms, int research) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (protein <= 0 && mushrooms <= 0 && research <= 0) {
            JLabel none = new JLabel(LanguageStrings.get(LanguageStrings.HUNT_REWARDS_NONE));
            none.setFont(AssetStyles.FONT_NORMAL);
            none.setForeground(AssetStyles.FONT_COLOR);
            row.add(none);
            return row;
        }
        if (protein > 0) {
            row.add(buildResourceAmountLabel(GameConstants.RESOURCE_MEAT, protein));
        }
        if (mushrooms > 0) {
            row.add(buildResourceAmountLabel(GameConstants.RESOURCE_FUNGI, mushrooms));
        }
        if (research > 0) {
            row.add(buildResearchAmountLabel(research));
        }
        return row;
    }

    static JPanel buildSpeciesTargetRow(Species species, JComponent detail, JComponent actions) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(AssetStyles.UI_BG_SECONDARY);
        row.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.INTERNAL_BORDER,
                new EmptyBorder(8, 8, 8, 8)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel west = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        west.setOpaque(false);
        west.add(buildIconOnlyLabel(species));
        if (detail != null) {
            west.add(detail);
        }
        row.add(west, BorderLayout.CENTER);
        if (actions != null) {
            row.add(actions, BorderLayout.EAST);
        }
        return row;
    }

    static JPanel buildSpeciesDetailColumnRow(Species species, JComponent detail, JComponent actions, int maxHeight) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(AssetStyles.UI_BG_SECONDARY);
        row.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.INTERNAL_BORDER,
                new EmptyBorder(8, 8, 8, 8)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));

        JPanel west = new JPanel(new BorderLayout(8, 0));
        west.setOpaque(false);
        west.add(buildIconOnlyLabel(species), BorderLayout.WEST);
        if (detail != null) {
            west.add(detail, BorderLayout.CENTER);
        }
        row.add(west, BorderLayout.CENTER);
        if (actions != null) {
            row.add(actions, BorderLayout.EAST);
        }
        return row;
    }

    static JPanel buildTargetHeading(Species species, String labelKey) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(AssetStyles.UI_BG_PRIMARY);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, AssetStyles.MIN_CONTROL_HIT_SIZE + 8));

        row.add(buildIconOnlyLabel(species));
        JLabel label = new JLabel(LanguageStrings.get(labelKey));
        label.setFont(AssetStyles.FONT_BOLD);
        label.setForeground(AssetStyles.FONT_COLOR);
        row.add(label);
        return row;
    }

    static JPanel buildNativeSpeciesIcons(List<Species> species) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (species == null || species.isEmpty()) {
            JLabel none = new JLabel(LanguageStrings.get(LanguageStrings.CRITTER_NATIVE_NONE));
            none.setFont(AssetStyles.FONT_NORMAL);
            none.setForeground(AssetStyles.FONT_COLOR);
            row.add(none);
            return row;
        }
        for (Species entry : species) {
            row.add(buildIconOnlyLabel(entry));
        }
        return row;
    }

    static JPanel buildNativeSpeciesLine(String labelKey, List<Species> species) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heading = new JLabel(LanguageStrings.get(labelKey));
        heading.setFont(AssetStyles.FONT_NORMAL);
        heading.setForeground(AssetStyles.FONT_COLOR);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.add(heading);
        block.add(Box.createVerticalStrut(4));
        block.add(buildNativeSpeciesIcons(species));
        return block;
    }

    static JPanel buildSpeciesHeader(Species species, String countLine) {
        JPanel row = new JPanel(new BorderLayout(8, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel title = buildIconOnlyLabel(species);
        row.add(title, BorderLayout.WEST);

        if (countLine != null && !countLine.isEmpty()) {
            JLabel count = new JLabel(countLine);
            count.setFont(AssetStyles.FONT_NORMAL);
            count.setForeground(AssetStyles.FONT_COLOR);
            row.add(count, BorderLayout.EAST);
        }
        return row;
    }

    static JPanel buildUnlockRequirementRow(Upgrade upgrade) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel requires = new JLabel(LanguageStrings.get(LanguageStrings.CRITTER_REQUIRES_LABEL));
        requires.setFont(AssetStyles.FONT_NORMAL);
        requires.setForeground(AssetStyles.FONT_COLOR);
        row.add(requires);

        if (upgrade != null && upgrade.getIcon() != null) {
            JLabel icon = new JLabel(upgrade.getIcon());
            icon.setToolTipText(upgrade.getName());
            row.add(icon);
        }
        return row;
    }

    static void addPadded(JPanel host, JComponent component, int bottomGap) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        host.add(component);
        if (bottomGap > 0) {
            host.add(Box.createVerticalStrut(bottomGap));
        }
    }

    private static String wrapHtml(String text) {
        if (text == null || text.isEmpty()) {
            return " ";
        }
        if (text.startsWith("<html>")) {
            return text;
        }
        return "<html>" + text.replace("\n", "<br>") + "</html>";
    }
}
