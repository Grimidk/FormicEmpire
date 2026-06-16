package com.grimidk.formicempire.classes.infrasctructure.repositories;

import java.awt.Image;
import java.awt.MediaTracker;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.BugType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.ResourceSource;

/**
 * Blocks until classpath sprite images are decoded so the first paint shows real art, not blanks.
 */
public final class GameSpritePreloader {

    private static final JPanel FALLBACK_TRACKER = new JPanel();

    private GameSpritePreloader() {
    }

    public static void ensureLoaded(Image image) {
        if (image == null) {
            return;
        }
        if (image.getWidth(FALLBACK_TRACKER) >= 0 && image.getHeight(FALLBACK_TRACKER) >= 0) {
            return;
        }
        MediaTracker tracker = new MediaTracker(FALLBACK_TRACKER);
        int id = 1;
        tracker.addImage(image, id);
        try {
            tracker.waitForID(id, 15_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void ensureLoaded(ImageIcon icon) {
        if (icon != null) {
            ensureLoaded(icon.getImage());
        }
    }

    public static void warmSession(Colony colony) {
        Set<Image> images = new LinkedHashSet<>();
        for (BugType bugType : GameConstants.getBugTypes()) {
            collectIcon(images, bugType.getSprite());
            collectIcon(images, bugType.getIcon());
        }
        for (ResourceType resourceType : GameConstants.getResources()) {
            collectIcon(images, resourceType.getIcon());
            collectIcon(images, resourceType.getSourceSpriteSmall());
            collectIcon(images, resourceType.getSourceSpriteMedium());
            collectIcon(images, resourceType.getSourceSpriteBig());
            collectIcon(images, resourceType.getSourceSpriteHuge());
        }
        Species colonySpecies = colony != null ? colony.getSpecies() : GameConstants.SPECIES_OMNI;
        if (colonySpecies != null) {
            for (AntType antType : GameConstants.getAntTypes()) {
                collectIcon(images, GameConstants.getAntSprite(antType, colonySpecies));
            }
        }
        if (colony != null && colony.getDynasty() != null) {
            for (Species species : GameConstants.getSpecies()) {
                if (species == colonySpecies) {
                    continue;
                }
                if (species.getAssimilation() != null
                        && colony.getDynasty().isAssimilationCompleted(species.getAssimilation())) {
                    collectIcon(images, GameConstants.getAntSprite(GameConstants.TYPE_DRONE, species));
                }
            }
        }
        collectIcon(images, GameConstants.ICON_PARASITIC_MITE);
        if (colony != null && colony.getLocationService() != null) {
            for (ResourceSource source : colony.getLocationService().getDiscoveredSources()) {
                collectIcon(images, source.getIconForDisplay());
            }
        }

        MediaTracker tracker = new MediaTracker(FALLBACK_TRACKER);
        int id = 0;
        for (Image image : images) {
            id++;
            tracker.addImage(image, id);
        }
        if (id == 0) {
            return;
        }
        try {
            tracker.waitForAll(30_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void collectIcon(Set<Image> images, ImageIcon icon) {
        if (icon != null) {
            images.add(icon.getImage());
        }
    }
}
