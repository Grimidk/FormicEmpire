package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.Component;

public final class HelpAnimatedSpriteLabel {
    private static final int FRAME_MS = 50;
    private static final float WOBBLE_PHASE = 0.65f;
    private static final float MOTION_RATE = 1f;
    private static final EmptyBorder SPRITE_BORDER = new EmptyBorder(5, 5, 5, 5);

    private HelpAnimatedSpriteLabel() {
    }

    public static Component forCritter(Species species) {
        if (species == null) {
            return bordered(new JLabel());
        }
        ImageIcon fallback = GameConstants.getCritterSprite(species, 1, 1);
        if (fallback == null) {
            fallback = species.getSprite();
        }
        if (fallback == null) {
            fallback = species.getIcon();
        }
        if (!species.hasLegWalkCycle() && !species.hasAntennaCycle()) {
            return bordered(new JLabel(fallback));
        }
        final ImageIcon staticIcon = fallback;
        return new AnimatedLabel(staticIcon, animationSeconds -> {
            int legFrame = RouteViewVisuals.resolveCritterLegFrame(
                    species, true, WOBBLE_PHASE, animationSeconds, MOTION_RATE);
            int antennaFrame = RouteViewVisuals.resolveCritterAntennaFrame(
                    species, WOBBLE_PHASE, animationSeconds, MOTION_RATE);
            ImageIcon icon = GameConstants.getCritterSprite(species, legFrame, antennaFrame);
            return icon != null ? icon : staticIcon;
        });
    }

    public static Component forRepresentativeAnt(AntSpecies species) {
        AntShowcase showcase = resolveAntShowcase(species);
        return forAnt(showcase.type(), showcase.species(), showcase.profile());
    }

    public static Component forAnt(AntType type, AntSpecies species, AntSubtypeProfile profile) {
        if (type == null) {
            return bordered(new JLabel());
        }
        AntSpecies resolvedSpecies = species != null ? species : GameConstants.SPECIES_OMNI;
        AntSubtypeProfile resolvedProfile = profile != null ? profile : AntSubtypeProfile.standard();
        ImageIcon fallback = GameConstants.getAntSprite(type, resolvedSpecies, resolvedProfile);
        if (fallback == null) {
            fallback = type.getIcon();
        }
        if (!usesAnimatedAntSprite(type)) {
            return bordered(new JLabel(fallback));
        }
        boolean winged = RouteViewVisuals.isWinged(type);
        final ImageIcon staticIcon = fallback;
        return new AnimatedLabel(staticIcon, animationSeconds -> {
            int legFrame = RouteViewVisuals.resolveLegFrame(
                    type, false, true, WOBBLE_PHASE, animationSeconds, MOTION_RATE);
            int jawFrame = RouteViewVisuals.resolveJawFrame(
                    type, false, WOBBLE_PHASE, animationSeconds, MOTION_RATE);
            int wingFrame = RouteViewVisuals.resolveWingFrame(
                    type, winged, WOBBLE_PHASE, animationSeconds, MOTION_RATE);
            int antennaFrame = RouteViewVisuals.resolveAntennaFrame(
                    type, WOBBLE_PHASE, animationSeconds, MOTION_RATE);
            ImageIcon icon = GameConstants.getAntSprite(
                    type, resolvedSpecies, resolvedProfile, legFrame, jawFrame, wingFrame, antennaFrame, false);
            return icon != null ? icon : staticIcon;
        });
    }

    private static boolean usesAnimatedAntSprite(AntType type) {
        return type != GameConstants.TYPE_EGG
                && type != GameConstants.TYPE_LARVA
                && type != GameConstants.TYPE_PUPA
                && type != GameConstants.TYPE_DEAD
                && type != GameConstants.TYPE_ZOMBIE;
    }

    private static AntShowcase resolveAntShowcase(AntSpecies species) {
        AntType type = GameConstants.TYPE_WORKER;
        AntSubtypeProfile profile = AntSubtypeProfile.standard();

        if (species != null && species.getBaseUpgrades().contains(GameUnlocks.TYPE_MAJOR)) {
            type = GameConstants.TYPE_MAJOR;
        }

        AntSubtype traitSubtype = findSpeciesTraitSubtype(species);
        if (traitSubtype != null) {
            profile = profileWithSubtype(traitSubtype);
            if (traitSubtype.getAttackMult() > 1f) {
                type = GameConstants.TYPE_SOLDIER;
            } else {
                type = GameConstants.TYPE_WORKER;
            }
        }

        return new AntShowcase(type, species, profile);
    }

    private static AntSubtype findSpeciesTraitSubtype(AntSpecies species) {
        if (species == null) {
            return null;
        }
        for (AntSubtype subtype : GameConstants.getAntSubtypes()) {
            if (subtype == null || subtype.isNone() || subtype.getRequiredUpgrade() == null) {
                continue;
            }
            if (species.getBaseUpgrades().contains(subtype.getRequiredUpgrade())) {
                return subtype;
            }
        }
        return null;
    }

    private static AntSubtypeProfile profileWithSubtype(AntSubtype subtype) {
        int head = AntSubtype.DIGIT_NONE;
        int torso = AntSubtype.DIGIT_NONE;
        int abdomen = AntSubtype.DIGIT_NONE;
        int other = AntSubtype.DIGIT_NONE;
        switch (subtype.getSlot()) {
            case HEAD -> head = subtype.getDigit();
            case TORSO -> torso = subtype.getDigit();
            case ABDOMEN -> abdomen = subtype.getDigit();
            case OTHER -> other = subtype.getDigit();
        }
        return AntSubtypeProfile.of(head, torso, abdomen, other);
    }

    private static JLabel bordered(JLabel label) {
        label.setBorder(SPRITE_BORDER);
        return label;
    }

    private record AntShowcase(AntType type, AntSpecies species, AntSubtypeProfile profile) {
    }

    private static final class AnimatedLabel extends JLabel {
        private final FrameRenderer renderer;
        private Timer timer;
        private float animationSeconds;

        private AnimatedLabel(ImageIcon initial, FrameRenderer renderer) {
            super(initial);
            this.renderer = renderer;
            setBorder(SPRITE_BORDER);
            addAncestorListener(new AncestorListener() {
                @Override
                public void ancestorAdded(AncestorEvent event) {
                    startTimer();
                }

                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    stopTimer();
                }

                @Override
                public void ancestorMoved(AncestorEvent event) {
                }
            });
        }

        private void startTimer() {
            if (timer != null) {
                return;
            }
            timer = new Timer(FRAME_MS, e -> {
                animationSeconds += FRAME_MS / 1000f;
                ImageIcon icon = renderer.frame(animationSeconds);
                if (icon != null) {
                    setIcon(icon);
                }
            });
            timer.setRepeats(true);
            timer.start();
        }

        private void stopTimer() {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
        }
    }

    @FunctionalInterface
    private interface FrameRenderer {
        ImageIcon frame(float animationSeconds);
    }
}
