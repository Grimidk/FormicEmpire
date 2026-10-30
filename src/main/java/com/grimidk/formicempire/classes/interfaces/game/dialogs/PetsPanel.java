package com.grimidk.formicempire.classes.interfaces.game.dialogs;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyCritterHandlingService;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class PetsPanel extends JPanel {

    private final Colony colony;
    private final Engine engine;
    private final JPanel contentPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(" ");

    public PetsPanel(Colony colony, Engine engine) {
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
            statusLabel.setText(LanguageStrings.get(LanguageStrings.CRITTER_PETS_UNAVAILABLE));
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        ColonyCritterHandlingService bugs = colony.getBugHandlingService();
        Biome biome = CritterPanelUtils.resolveColonyBiome(colony, engine);
        int poolUsed = bugs.getUnlockedPetCount(colony);
        int poolMax = bugs.getUnlockedPetCapacityMax(colony);
        statusLabel.setText(LanguageStrings.format(LanguageStrings.COLONY_PET_INSECTS, poolUsed, poolMax));

        if (biome != null) {
            CritterPanelUtils.addPadded(contentPanel,
                    CritterPanelUtils.buildNativeSpeciesLine(
                            LanguageStrings.CRITTER_NATIVE_PETS_HERE,
                            biome.getNativeBugs()),
                    10);
        }

        boolean anyVisible = false;
        for (Species species : ColonyCritterHandlingService.getPetTypes()) {
            if (appendPetSection(bugs, biome, species)) {
                anyVisible = true;
            }
            contentPanel.add(Box.createVerticalStrut(8));
        }

        if (bugs.canCatchPetBug(colony, GameConstants.TYPE_SYMBIOTIC_MITE)
                || bugs.canCatchPetBug(colony, GameConstants.TYPE_DERMESTID)
                || colony.hasUpgrade(GameUnlocks.ROLE_CATCHER)) {
            JPanel poolPanel = CritterPanelUtils.buildSectionPanel(LanguageStrings.get(LanguageStrings.STAT_INSECT_POOL));
            poolPanel.setBorder(BorderFactory.createCompoundBorder(
                    poolPanel.getBorder(),
                    new EmptyBorder(8, 8, 8, 8)));
            int catcherPoolUsed = bugs.getUnlockedCatcherPoolPetCount(colony);
            int catcherPoolMax = bugs.getUnlockedCatcherPoolCapacityMax(colony);
            CritterPanelUtils.addPadded(poolPanel,
                    CritterPanelUtils.buildBodyLabel(LanguageStrings.format(
                            LanguageStrings.CRITTER_PETS_POOL_FMT,
                            AssetStyles.formatNumber(catcherPoolUsed),
                            AssetStyles.formatNumber(catcherPoolMax))),
                    4);
            CritterPanelUtils.addPadded(poolPanel,
                    CritterPanelUtils.buildMutedLabel(LanguageStrings.format(
                            LanguageStrings.STAT_INSECT_CATCHERS_FMT,
                            AssetStyles.formatNumber(colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER)))),
                    0);
            poolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(poolPanel);
            anyVisible = true;
        }

        if (!anyVisible && poolMax <= 0) {
            CritterPanelUtils.addPadded(contentPanel,
                    CritterPanelUtils.buildBodyLabel(LanguageStrings.get(LanguageStrings.CRITTER_PETS_EMPTY)),
                    0);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private boolean appendPetSection(ColonyCritterHandlingService bugs, Biome biome, Species species) {
        boolean nativeHere = biome != null && biome.getNativeBugs().contains(species);
        boolean unlocked = bugs.canCatchPetBug(colony, species);
        int count = bugs.getCount(colony, species);
        if (!unlocked && count <= 0 && !nativeHere) {
            return false;
        }

        JPanel section = CritterPanelUtils.buildSpeciesSection(species);
        section.setBorder(BorderFactory.createCompoundBorder(
                section.getBorder(),
                new EmptyBorder(8, 8, 8, 8)));

        String countLine = unlocked
                ? LanguageStrings.format(
                        LanguageStrings.CRITTER_PETS_COUNT_FMT,
                        AssetStyles.formatNumber(count),
                        AssetStyles.formatNumber(bugs.getMaxCapacity(colony, species)))
                : LanguageStrings.get(LanguageStrings.CRITTER_PETS_LOCKED);
        CritterPanelUtils.addPadded(section, CritterPanelUtils.buildSpeciesHeader(species, countLine), 6);

        CritterPanelUtils.addPadded(section, CritterPanelUtils.buildMutedLabel(buildTenderLine(species)), 4);
        CritterPanelUtils.addPadded(section, CritterPanelUtils.buildBodyLabel(buildBenefitLine(bugs, species)), 4);

        if (!nativeHere) {
            CritterPanelUtils.addPadded(section,
                    CritterPanelUtils.buildMutedLabel(LanguageStrings.get(LanguageStrings.CRITTER_PETS_NOT_NATIVE)),
                    0);
        } else if (!unlocked) {
            CritterPanelUtils.addPadded(section,
                    CritterPanelUtils.buildUnlockRequirementRow(requiredUnlock(species)),
                    0);
        }

        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(section);
        return true;
    }

    private static Upgrade requiredUnlock(Species species) {
        if (species == GameConstants.TYPE_APHID) {
            return GameUnlocks.ROLE_RANCHER;
        }
        if (species == GameConstants.TYPE_SYMBIOTIC_MITE) {
            return GameUnlocks.ABILITY_CATCH_SYMBIOTIC_MITE;
        }
        if (species == GameConstants.TYPE_DERMESTID) {
            return GameUnlocks.ABILITY_CATCH_DERMESTID;
        }
        return null;
    }

    private String buildTenderLine(Species species) {
        if (species == GameConstants.TYPE_APHID) {
            return LanguageStrings.format(
                    LanguageStrings.STAT_RATE_RANCHERS_FMT,
                    AssetStyles.formatNumber(colony.getAssignedRoleCount(GameConstants.ROLE_RANCHER)));
        }
        if (species == GameConstants.TYPE_DERMESTID) {
            return LanguageStrings.format(
                    LanguageStrings.STAT_RATE_GRAVERS_FMT,
                    AssetStyles.formatNumber(colony.getAssignedRoleCount(GameConstants.ROLE_GRAVER)));
        }
        return LanguageStrings.format(
                LanguageStrings.STAT_INSECT_CATCHERS_FMT,
                AssetStyles.formatNumber(colony.getAssignedRoleCount(GameConstants.ROLE_CATCHER)));
    }

    private String buildBenefitLine(ColonyCritterHandlingService bugs, Species species) {
        if (species == GameConstants.TYPE_APHID) {
            return LanguageStrings.get(LanguageStrings.CRITTER_PET_APHID_DETAIL);
        }
        if (species == GameConstants.TYPE_SYMBIOTIC_MITE) {
            int killPer = bugs.getSymbioticMiteParasiticMiteKillPerDay(colony);
            return LanguageStrings.format(LanguageStrings.CRITTER_PET_SYMBIOTIC_DETAIL, killPer);
        }
        if (species == GameConstants.TYPE_DERMESTID) {
            int bonus = bugs.getDermestidGraveBonus(colony);
            return LanguageStrings.format(LanguageStrings.CRITTER_PET_DERMESTID_DETAIL, bonus);
        }
        return "";
    }
}
