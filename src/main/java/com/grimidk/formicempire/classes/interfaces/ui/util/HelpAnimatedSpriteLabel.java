package com.grimidk.formicempire.classes.interfaces.ui.util;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntClass;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
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

    public static Component forAnt(AntClass type, AntSpecies species, AntModProfile profile) {
        if (type == null) {
            return bordered(new JLabel());
        }
        AntSpecies resolvedSpecies = species != null ? species : GameConstants.SPECIES_OMNI;
        AntModProfile resolvedProfile = profile != null ? profile : AntModProfile.standard();
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

    private static boolean usesAnimatedAntSprite(AntClass type) {
        return type != GameConstants.CLASS_EGG
                && type != GameConstants.CLASS_LARVA
                && type != GameConstants.CLASS_PUPA
                && type != GameConstants.CLASS_DEAD
                && type != GameConstants.CLASS_ZOMBIE;
    }

    private static AntShowcase resolveAntShowcase(AntSpecies species) {
        AntClass type = GameConstants.CLASS_WORKER;
        AntModProfile profile = AntModProfile.standard();

        if (species != null && species.getBaseUpgrades().contains(GameUnlocks.TYPE_MAJOR)) {
            type = GameConstants.CLASS_MAJOR;
        }

        AntMod traitMod = findSpeciesTraitMod(species);
        if (traitMod != null) {
            profile = profileWithMod(traitMod);
            if (traitMod.getAttackMult() > 1f) {
                type = GameConstants.CLASS_SOLDIER;
            } else {
                type = GameConstants.CLASS_WORKER;
            }
        }

        return new AntShowcase(type, species, profile);
    }

    private static AntMod findSpeciesTraitMod(AntSpecies species) {
        if (species == null) {
            return null;
        }
        for (AntMod mod : GameConstants.getAntMods()) {
            if (mod == null || mod.isNone() || mod.getRequiredUpgrade() == null) {
                continue;
            }
            if (species.getBaseUpgrades().contains(mod.getRequiredUpgrade())) {
                return mod;
            }
        }
        return null;
    }

    private static AntModProfile profileWithMod(AntMod mod) {
        int head = AntMod.DIGIT_NONE;
        int torso = AntMod.DIGIT_NONE;
        int abdomen = AntMod.DIGIT_NONE;
        int other = AntMod.DIGIT_NONE;
        switch (mod.getSlot()) {
            case HEAD -> head = mod.getDigit();
            case TORSO -> torso = mod.getDigit();
            case ABDOMEN -> abdomen = mod.getDigit();
            case OTHER -> other = mod.getDigit();
        }
        return AntModProfile.of(head, torso, abdomen, other);
    }

    private static JLabel bordered(JLabel label) {
        label.setBorder(SPRITE_BORDER);
        return label;
    }

    private record AntShowcase(AntClass type, AntSpecies species, AntModProfile profile) {
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
