package com.grimidk.formicempire.classes.infrasctructure.assets;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import java.awt.Image;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;

public final class GameSpritePreloader {

    private static final int LOAD_WAIT_MS = 15_000;
    private static final int WARM_SESSION_WAIT_MS = 30_000;

    private GameSpritePreloader() {
    }

    public static void ensureLoaded(Image image) {
        if (image == null) {
            return;
        }
        waitForImage(image, LOAD_WAIT_MS);
    }

    public static void ensureLoaded(ImageIcon icon) {
        if (icon != null) {
            ensureLoaded(icon.getImage());
        }
    }

    public static void warmSession(Colony colony) {
        warmSession(colony, null);
    }

    public static void warmSession(Colony colony, BooleanSupplier cancelled) {
        Set<Image> images = new LinkedHashSet<>();
        for (Species bugType : GameConstants.getCritterSpecies()) {
            if (bugType.hasComposedSprite()) {
                for (int leg = 1; leg <= GameNumbers.ANT_LEG_FRAME_COUNT; leg++) {
                    collectIcon(images, GameConstants.getCritterSprite(bugType, leg));
                }
            } else {
                collectIcon(images, bugType.getSprite());
            }
            collectIcon(images, bugType.getIcon());
        }
        for (ResourceType resourceType : GameConstants.getResources()) {
            collectIcon(images, resourceType.getIcon());
            collectIcon(images, resourceType.getSourceSpriteSmall());
            collectIcon(images, resourceType.getSourceSpriteMedium());
            collectIcon(images, resourceType.getSourceSpriteBig());
            collectIcon(images, resourceType.getSourceSpriteHuge());
        }
        AntSpecies colonySpecies = colony != null ? colony.getSpecies() : GameConstants.SPECIES_OMNI;
        if (colonySpecies != null) {
            AntSubtypeProfile standardProfile = AntSubtypeProfile.standard();
            for (AntType antType : GameConstants.getAntTypes()) {
                collectIcon(images, GameConstants.getAntSprite(antType, colonySpecies));
                if (AntSpriteCompositor.canCompose(antType)) {
                    collectIcon(images, GameConstants.getAntSprite(
                            antType, colonySpecies, standardProfile, 1, 2, 1, 1, false));
                    collectIcon(images, GameConstants.getAntSprite(
                            antType, colonySpecies, standardProfile, 1, 1, 1, 2, false));
                }
            }
        }
        if (colony != null && colony.getDynasty() != null) {
            for (AntSpecies species : GameConstants.getSpecies()) {
                if (species == colonySpecies) {
                    continue;
                }
                if (species.getAssimilation() != null
                        && colony.getDynasty().hasUpgrade(GameUnlocks.ABILITY_CLONING)
                        && colony.getDynasty().isAssimilationCompleted(species.getAssimilation())
                        && GameConstants.hasAssimilatedDroneSprite(species)) {
                    collectIcon(images, GameConstants.getAssimilatedDroneSprite(species));
                }
            }
        }
        collectIcon(images, GameConstants.ICON_PARASITIC_MITE);
        if (colony != null && colony.getLocationService() != null) {
            for (ResourceSource source : colony.getLocationService().getDiscoveredSources()) {
                collectIcon(images, source.getIconForDisplay());
            }
        }

        for (Image image : images) {
            if (cancelled != null && cancelled.getAsBoolean()) {
                return;
            }
            waitForImage(image, WARM_SESSION_WAIT_MS);
        }
    }

    private static void waitForImage(Image image, int timeoutMs) {
        if (image.getWidth(null) >= 0 && image.getHeight(null) >= 0) {
            return;
        }
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (Thread.currentThread().isInterrupted()) {
                Thread.currentThread().interrupt();
                return;
            }
            if (image.getWidth(null) >= 0 && image.getHeight(null) >= 0) {
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private static void collectIcon(Set<Image> images, ImageIcon icon) {
        if (icon != null) {
            images.add(icon.getImage());
        }
    }
}
