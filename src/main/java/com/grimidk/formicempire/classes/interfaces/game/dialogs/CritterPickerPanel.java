package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.BugRole;
import com.grimidk.formicempire.classes.constants.critter.Skill;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.entities.services.colony.HuntCreatureCombatService;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

final class CritterPickerPanel extends JPanel {
    private static final int PREVIEW_MAX_SIZE = 128;

    private final JLabel titleLabel;
    private final JLabel speciesLabel;
    private final JComboBox<Species> critterCombo;
    private final JLabel spriteLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel nameLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel scientificNameLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel encounterTypeLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel statsHeadingLabel = new JLabel();
    private final JLabel hpTitleLabel = new JLabel();
    private final JLabel hpValueLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel attackTitleLabel = new JLabel();
    private final JLabel attackValueLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel defenseTitleLabel = new JLabel();
    private final JLabel defenseValueLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel speedTitleLabel = new JLabel();
    private final JLabel speedValueLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel skillsHeadingLabel = new JLabel();
    private final JPanel skillsListPanel = new JPanel();
    private Runnable onSelectionChanged;

    CritterPickerPanel(String title) {
        super(new BorderLayout(8, 8));
        setBackground(AssetStyles.UI_BG_SECONDARY);
        setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.INTERNAL_BORDER,
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JPanel topPanel = new JPanel(new BorderLayout(8, 6));
        topPanel.setOpaque(false);

        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(16f));
        titleLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel speciesRow = new JPanel(new BorderLayout(8, 0));
        speciesRow.setOpaque(false);
        speciesLabel = new JLabel(LanguageStrings.get(LanguageStrings.BATTLE_SIM_SPECIES) + ":");
        speciesLabel.setFont(AssetStyles.FONT_BOLD);
        speciesLabel.setForeground(AssetStyles.FONT_COLOR);

        List<Species> critters = GameConstants.getCombatCritterSpecies();
        critterCombo = new JComboBox<>(critters.toArray(new Species[0]));
        AssetStyles.styleComboBox(critterCombo);
        critterCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                if (value instanceof Species sp) {
                    label.setText(sp.getName() + " (" + encounterLabelFor(sp) + ")");
                    label.setIcon(sp.getIcon());
                    label.setIconTextGap(6);
                }
                return label;
            }
        });
        critterCombo.addActionListener(e -> updateCritterPreview());

        speciesRow.add(speciesLabel, BorderLayout.WEST);
        speciesRow.add(critterCombo, BorderLayout.CENTER);
        topPanel.add(speciesRow, BorderLayout.CENTER);

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

        centerContent.add(buildPreviewSection());
        centerContent.add(Box.createVerticalStrut(10));
        centerContent.add(buildStatsSection());
        centerContent.add(Box.createVerticalStrut(10));
        centerContent.add(buildSkillsSection());

        JScrollPane scrollPane = new JScrollPane(centerContent);
        AssetStyles.styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        resetToDefaults();
    }

    private JComponent buildPreviewSection() {
        JPanel section = new JPanel(new BorderLayout(4, 6));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
                AssetStyles.INTERNAL_BORDER,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        spriteLabel.setPreferredSize(new Dimension(PREVIEW_MAX_SIZE, PREVIEW_MAX_SIZE));
        spriteLabel.setMinimumSize(new Dimension(PREVIEW_MAX_SIZE, PREVIEW_MAX_SIZE));

        nameLabel.setFont(AssetStyles.FONT_BOLD.deriveFont(15f));
        nameLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);

        scientificNameLabel.setFont(AssetStyles.FONT_NORMAL.deriveFont(Font.ITALIC));
        scientificNameLabel.setForeground(AssetStyles.FONT_COLOR);

        encounterTypeLabel.setFont(AssetStyles.FONT_BOLD);
        encounterTypeLabel.setForeground(AssetStyles.FONT_COLOR_HIGHLIGHT);

        JPanel infoStack = new JPanel();
        infoStack.setOpaque(false);
        infoStack.setLayout(new BoxLayout(infoStack, BoxLayout.Y_AXIS));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        scientificNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        encounterTypeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoStack.add(nameLabel);
        infoStack.add(Box.createVerticalStrut(2));
        infoStack.add(scientificNameLabel);
        infoStack.add(Box.createVerticalStrut(4));
        infoStack.add(encounterTypeLabel);

        section.add(spriteLabel, BorderLayout.CENTER);
        section.add(infoStack, BorderLayout.SOUTH);
        return section;
    }

    private JComponent buildStatsSection() {
        JPanel section = new JPanel(new BorderLayout(4, 4));
        section.setOpaque(false);
        section.setBorder(AssetStyles.INTERNAL_BORDER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        statsHeadingLabel.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_CRITTER));
        statsHeadingLabel.setFont(AssetStyles.FONT_BOLD);
        statsHeadingLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        header.add(statsHeadingLabel, BorderLayout.WEST);

        JPanel rowsPanel = new JPanel(new GridBagLayout());
        rowsPanel.setOpaque(false);
        rowsPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addStatRow(rowsPanel, gbc, 0, hpTitleLabel, hpValueLabel,
                LanguageStrings.UNIT_STAT_HEALTH, GameConstants.ICON_HEALTH);
        addStatRow(rowsPanel, gbc, 1, attackTitleLabel, attackValueLabel,
                LanguageStrings.UNIT_STAT_ATTACK, GameConstants.ICON_ATTACK);
        addStatRow(rowsPanel, gbc, 2, defenseTitleLabel, defenseValueLabel,
                LanguageStrings.UNIT_STAT_DEFENSE, GameConstants.ICON_DEFENSE);
        addStatRow(rowsPanel, gbc, 3, speedTitleLabel, speedValueLabel,
                LanguageStrings.UNIT_STAT_SPEED, null);

        section.add(header, BorderLayout.NORTH);
        section.add(rowsPanel, BorderLayout.CENTER);
        return section;
    }

    private static void addStatRow(
            JPanel rowsPanel,
            GridBagConstraints gbc,
            int row,
            JLabel titleLabel,
            JLabel valueLabel,
            String titleKey,
            ImageIcon icon) {
        titleLabel.setText(LanguageStrings.get(titleKey));
        titleLabel.setIcon(icon);
        titleLabel.setIconTextGap(6);
        titleLabel.setFont(AssetStyles.FONT_NORMAL);
        titleLabel.setForeground(AssetStyles.FONT_COLOR);

        valueLabel.setFont(AssetStyles.FONT_BOLD);
        valueLabel.setForeground(AssetStyles.FONT_COLOR_VALUE);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        rowsPanel.add(titleLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        rowsPanel.add(valueLabel, gbc);
    }

    private JComponent buildSkillsSection() {
        JPanel section = new JPanel(new BorderLayout(4, 4));
        section.setOpaque(false);
        section.setBorder(AssetStyles.INTERNAL_BORDER);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        skillsHeadingLabel.setText(LanguageStrings.get(LanguageStrings.HELP_COMBAT_SKILLS));
        skillsHeadingLabel.setFont(AssetStyles.FONT_BOLD);
        skillsHeadingLabel.setForeground(AssetStyles.FONT_COLOR_HEADER);
        header.add(skillsHeadingLabel, BorderLayout.WEST);

        skillsListPanel.setOpaque(false);
        skillsListPanel.setLayout(new BoxLayout(skillsListPanel, BoxLayout.Y_AXIS));
        skillsListPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));

        section.add(header, BorderLayout.NORTH);
        section.add(skillsListPanel, BorderLayout.CENTER);
        return section;
    }

    void setTitle(String title) {
        titleLabel.setText(title);
    }

    void setOnSelectionChanged(Runnable onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }

    Species getSelectedCritter() {
        Object selected = critterCombo.getSelectedItem();
        if (selected instanceof Species sp) {
            return sp;
        }
        return GameConstants.TYPE_COCKROACH;
    }

    void setSelectedCritter(Species species) {
        if (species != null) {
            critterCombo.setSelectedItem(species);
        }
        updateCritterPreview();
    }

    void resetToDefaults() {
        List<Species> critters = GameConstants.getCombatCritterSpecies();
        if (critters.contains(GameConstants.TYPE_COCKROACH)) {
            critterCombo.setSelectedItem(GameConstants.TYPE_COCKROACH);
        } else if (!critters.isEmpty()) {
            critterCombo.setSelectedIndex(0);
        }
        updateCritterPreview();
    }

    void randomize() {
        List<Species> critters = GameConstants.getCombatCritterSpecies();
        if (!critters.isEmpty()) {
            critterCombo.setSelectedItem(critters.get(GameRandom.nextInt(critters.size())));
        }
        updateCritterPreview();
    }

    void refreshTranslations() {
        speciesLabel.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_SPECIES) + ":");
        statsHeadingLabel.setText(LanguageStrings.get(LanguageStrings.BATTLE_SIM_CRITTER));
        hpTitleLabel.setText(LanguageStrings.get(LanguageStrings.UNIT_STAT_HEALTH));
        attackTitleLabel.setText(LanguageStrings.get(LanguageStrings.UNIT_STAT_ATTACK));
        defenseTitleLabel.setText(LanguageStrings.get(LanguageStrings.UNIT_STAT_DEFENSE));
        speedTitleLabel.setText(LanguageStrings.get(LanguageStrings.UNIT_STAT_SPEED));
        skillsHeadingLabel.setText(LanguageStrings.get(LanguageStrings.HELP_COMBAT_SKILLS));
        critterCombo.repaint();
        updateCritterPreview();
    }

    private void updateCritterPreview() {
        Species species = getSelectedCritter();
        spriteLabel.setIcon(scaleSpriteIcon(GameConstants.getCritterSprite(species), PREVIEW_MAX_SIZE));
        nameLabel.setText(species != null ? species.getName() : "");
        scientificNameLabel.setText(species != null ? species.getScientificName() : "");
        encounterTypeLabel.setText(encounterLabelFor(species));

        float maxHp = HuntCreatureCombatService.resolveBugMaxHealth(species);
        float atk = HuntCreatureCombatService.resolveBugAttack(species);
        float def = HuntCreatureCombatService.resolveBugDefense(species);
        int spd = HuntCreatureCombatService.resolveBugAttackSpeed(species);

        hpValueLabel.setText(AssetStyles.formatNumber(Math.round(maxHp)));
        attackValueLabel.setText(AssetStyles.formatNumber(Math.round(atk)));
        defenseValueLabel.setText(Math.round(def) + "%");
        speedValueLabel.setText(String.valueOf(spd));

        skillsListPanel.removeAll();
        List<Skill> attackSkills = resolveAttackSkills(species);
        for (Skill skill : attackSkills) {
            String details = String.format("%s (%.1fx, d6 >= %d, %d)",
                    skill.getName(),
                    skill.getDamageMult(),
                    skill.getD6Target(),
                    Math.max(1, skill.getTargetCount()));
            JLabel skillRow = new JLabel(details, skill.getIcon(), SwingConstants.LEFT);
            skillRow.setFont(AssetStyles.FONT_NORMAL);
            skillRow.setForeground(AssetStyles.FONT_COLOR);
            skillRow.setIconTextGap(6);
            skillRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            skillRow.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            skillsListPanel.add(skillRow);
        }
        skillsListPanel.revalidate();
        skillsListPanel.repaint();
        if (onSelectionChanged != null) {
            onSelectionChanged.run();
        }
    }

    private static List<Skill> resolveAttackSkills(Species species) {
        List<Skill> attacks = new ArrayList<>();
        if (species != null) {
            for (Skill skill : species.getBaseSkills()) {
                if (skill != null && skill.isAttack()) {
                    attacks.add(skill);
                }
            }
        }
        if (attacks.isEmpty()) {
            attacks.add(GameConstants.SKILL_BASIC_BITE);
        }
        return attacks;
    }

    private static String encounterLabelFor(Species species) {
        if (species != null && species.hasBugRole(BugRole.INVASION)) {
            return LanguageStrings.get(LanguageStrings.DIALOG_INVASION_BATTLE_TITLE);
        }
        return LanguageStrings.get(LanguageStrings.DIALOG_HUNT_BATTLE_TITLE);
    }

    private static ImageIcon scaleSpriteIcon(ImageIcon source, int maxSize) {
        if (source == null || source.getImage() == null || source.getIconWidth() <= 0 || source.getIconHeight() <= 0) {
            return source;
        }
        int w = source.getIconWidth();
        int h = source.getIconHeight();
        int maxDim = Math.max(w, h);
        if (maxDim <= maxSize) {
            return source;
        }
        double scale = (double) maxSize / maxDim;
        int scaledW = Math.max(1, (int) Math.round(w * scale));
        int scaledH = Math.max(1, (int) Math.round(h * scale));
        return new ImageIcon(source.getImage().getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH));
    }
}
