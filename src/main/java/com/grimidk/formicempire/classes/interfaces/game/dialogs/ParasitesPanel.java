package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.constants.world.Season;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyCritterHandlingService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyPopulationService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class ParasitesPanel extends JPanel {

    private final Colony colony;
    private final Engine engine;
    private final JPanel contentPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    public ParasitesPanel(Colony colony, Engine engine) {
        this.colony = colony;
        this.engine = engine;

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
        AssetStyles.styleScrollPane(scrollPane);

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
            statusLabel.setText(LanguageStrings.get(LanguageStrings.CRITTER_PARASITES_UNAVAILABLE));
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        World world = engine != null ? engine.getWorld() : null;
        Biome biome = CritterPanelUtils.resolveColonyBiome(colony, engine);
        Season season = world != null ? world.getSeason() : null;
        ColonyCritterHandlingService bugs = colony.getBugHandlingService();
        ColonyPopulationService population = colony.getPopulationService();

        int parasiticMites = colony.getParasiticMites();
        int parasiteAnts = colony.getParasiteAnts();
        boolean fuzz = engine != null && engine.isFuzzParasiteAnts();
        boolean showMites = parasiticMites > 0
                || colony.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT)
                || (biome != null && !biome.getNativeParasites().isEmpty());
        boolean showParasiteAnts = colony.hasUpgrade(GameUnlocks.ROLE_POLICE) || parasiteAnts > 0;

        if (!showMites && !showParasiteAnts) {
            statusLabel.setText(LanguageStrings.get(LanguageStrings.CRITTER_PARASITES_CLEAR));
            CritterPanelUtils.addPadded(contentPanel,
                    CritterPanelUtils.buildBodyLabel(LanguageStrings.get(LanguageStrings.CRITTER_PARASITES_INTRO)),
                    0);
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        statusLabel.setText(LanguageStrings.format(
                LanguageStrings.CRITTER_PARASITES_STATUS_FMT,
                AssetStyles.formatNumber(parasiticMites),
                colony.getParasiteAntCountDisplay(fuzz)));

        if (biome != null) {
            CritterPanelUtils.addPadded(contentPanel,
                    CritterPanelUtils.buildNativeSpeciesLine(
                            LanguageStrings.CRITTER_NATIVE_PARASITES_HERE,
                            biome.getNativeParasites()),
                    10);
        }

        if (showMites) {
            contentPanel.add(buildParasiticMiteSection(bugs, biome, season));
            contentPanel.add(Box.createVerticalStrut(8));
        }
        if (showParasiteAnts) {
            contentPanel.add(buildParasiteAntSection(population, biome, season, fuzz));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel buildParasiticMiteSection(ColonyCritterHandlingService bugs, Biome biome, Season season) {
        JPanel section = CritterPanelUtils.buildSpeciesSection(GameConstants.TYPE_PARASITIC_MITE);
        section.setBorder(BorderFactory.createCompoundBorder(
                section.getBorder(),
                new EmptyBorder(8, 8, 8, 8)));

        int parasiticMites = colony.getParasiticMites();
        int slowed = colony.getParasiticMiteSlowedAntCount();
        int killPerDay = colony.getSymbioticMites() * bugs.getSymbioticMiteParasiticMiteKillPerDay(colony);

        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildSpeciesHeader(
                        GameConstants.TYPE_PARASITIC_MITE,
                        LanguageStrings.format(LanguageStrings.COLONY_PARASITIC_MITES, parasiticMites, slowed)),
                6);
        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildBodyLabel(LanguageStrings.format(
                        LanguageStrings.STAT_INSECT_PARASITIC_KILL_FMT, slowed, killPerDay)),
                4);
        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildMutedLabel(LanguageStrings.format(
                        LanguageStrings.STAT_INSECT_PARASITIC_CAP_FMT,
                        GameNumbers.PARASITIC_MITES_PER_SLOWED_ANT)),
                4);
        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildBodyLabel(LanguageStrings.get(LanguageStrings.HELP_BUG_PARASITIC_MITE_DESC)),
                4);

        if (biome != null && season != null) {
            int projected = bugs.projectParasiticMiteMonthlySpawn(colony, biome, season);
            if (projected > 0) {
                int required = bugs.requiredSymbioticMitesToPreventOutbreak(colony, biome, season);
                int symbiotic = colony.getSymbioticMites();
                String prevention = LanguageStrings.format(
                        LanguageStrings.STAT_OUTBREAK_PREV_FMT, symbiotic, required, projected);
                if (bugs.isParasiticMiteOutbreakPrevented(colony, biome, season)) {
                    prevention = LanguageStrings.get(LanguageStrings.STAT_OUTBREAK_PREV_BLOCKED) + " — " + prevention;
                }
                CritterPanelUtils.addPadded(section,
                        CritterPanelUtils.buildBodyLabel(prevention + " "
                                + LanguageStrings.format(LanguageStrings.STAT_OUTBREAK_PREV_PROJECTED_FMT, projected)),
                        0);
            }
        }
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    private JPanel buildParasiteAntSection(ColonyPopulationService population, Biome biome, Season season,
            boolean fuzz) {
        JPanel section = CritterPanelUtils.buildSpeciesSection(GameConstants.TYPE_PARASITE_ANT);
        section.setBorder(BorderFactory.createCompoundBorder(
                section.getBorder(),
                new EmptyBorder(8, 8, 8, 8)));

        int policeCount = colony.getAssignedRoleCount(GameConstants.ROLE_POLICE);
        float detection = colony.getStatsService().getParasiteDetection(colony);

        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildSpeciesHeader(
                        GameConstants.TYPE_PARASITE_ANT,
                        LanguageStrings.format(
                                LanguageStrings.COLONY_PARASITE_ANTS,
                                colony.getParasiteAntCountDisplay(fuzz))),
                6);
        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildBodyLabel(LanguageStrings.format(
                        LanguageStrings.COLONY_DETECTION_RATE,
                        Math.round(policeCount * detection))),
                4);
        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildMutedLabel(LanguageStrings.format(
                        LanguageStrings.CRITTER_PARASITE_POLICE_FMT,
                        AssetStyles.formatNumber(policeCount))),
                4);
        CritterPanelUtils.addPadded(section,
                CritterPanelUtils.buildBodyLabel(LanguageStrings.get(LanguageStrings.HELP_BUG_PARASITE_ANT_DESC)),
                4);

        if (biome != null && season != null) {
            int projected = population.projectParasiteAntMonthlySpawn(colony, biome, season);
            if (projected > 0) {
                int required = population.requiredPoliceToPreventParasiteAntOutbreak(colony, biome, season);
                String prevention = LanguageStrings.format(
                        LanguageStrings.STAT_OUTBREAK_PREV_FMT, policeCount, required, projected);
                if (population.isParasiteAntOutbreakPrevented(colony, biome, season)) {
                    prevention = LanguageStrings.get(LanguageStrings.STAT_OUTBREAK_PREV_BLOCKED) + " — " + prevention;
                }
                CritterPanelUtils.addPadded(section, CritterPanelUtils.buildBodyLabel(prevention), 0);
            }
        }
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }
}
