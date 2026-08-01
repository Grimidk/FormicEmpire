package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RouteViewVisuals {

    private static final float ATTACK_JAW_SNAP_SPEED = 4.2f;
    private static final float WING_FLICKER_CYCLE_SEC = 5.5f;
    private static final float WING_FLICKER_CLOSED_START_SEC = 4.2f;
    private static final float WING_FLICKER_CLOSED_END_SEC = 5.2f;
    private static final float LEG_WALK_CYCLE_SPEED = 6.5f;
    public static final int CONVOY_RESOURCE_LOOP_PX = 960;

    public record ConvoyResourceProp(
            ResourceType resourceType,
            int quantity,
            float xPhase,
            float yNorm,
            float rotationDegrees) {
    }

    private RouteViewVisuals() {
    }

    public static float movementFacingDegrees(float vx, float vy) {
        double theta = Math.atan2(vy, vx);
        return (float) ((Math.toDegrees(theta) + 90 + 360) % 360);
    }

    public static float airSupportFacingDegrees(boolean flyingRight) {
        return movementFacingDegrees(flyingRight ? 1f : -1f, 0f);
    }

    public static float convoyFacingDegrees(boolean travelingRight) {
        return movementFacingDegrees(travelingRight ? 1f : -1f, 0f);
    }

    public static int resolveJawFrame(AntType type, boolean reserve, float wobblePhase, float animationSeconds,
            float motionRate) {
        if (reserve || type == null || type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS) {
            return 1;
        }
        double attackPulse = Math.sin(wobblePhase + animationSeconds * ATTACK_JAW_SNAP_SPEED * motionRate);
        return attackPulse > 0.82 ? 2 : 1;
    }

    public static int resolveLegFrame(AntType type, boolean flying, boolean moving, float wobblePhase,
            float animationSeconds, float motionRate) {
        if (flying && isWinged(type)) {
            return GameNumbers.ANT_LEG_FRAME_FLYING;
        }
        if (!moving) {
            return 1;
        }
        double phase = wobblePhase + animationSeconds * LEG_WALK_CYCLE_SPEED * Math.max(0.01f, motionRate);
        int index = (int) Math.floor(phase) % GameNumbers.ANT_LEG_FRAME_COUNT;
        if (index < 0) {
            index += GameNumbers.ANT_LEG_FRAME_COUNT;
        }
        return index + 1;
    }

    public static int resolveWingFrame(AntType type, boolean preferOpenWings, float wobblePhase, float animationSeconds,
            float motionRate) {
        if (type == null || (type != GameConstants.TYPE_DRONE && type != GameConstants.TYPE_PRINCESS)) {
            return 1;
        }
        if (preferOpenWings) {
            float cycle = (animationSeconds * motionRate + wobblePhase) % WING_FLICKER_CYCLE_SEC;
            if (cycle >= WING_FLICKER_CLOSED_START_SEC && cycle < WING_FLICKER_CLOSED_END_SEC) {
                return 1;
            }
            return 2;
        }
        double wingPulse = Math.sin(wobblePhase * 0.7f + animationSeconds * 1.3f * motionRate);
        return wingPulse > 0.92 ? 2 : 1;
    }

    public static boolean isWinged(AntType type) {
        return type == GameConstants.TYPE_DRONE || type == GameConstants.TYPE_PRINCESS;
    }

    public static List<ConvoyResourceProp> buildConvoyResourceProps(long seed, ConvoyScene.BackgroundKind backgroundKind) {
        Random random = new Random(seed ^ 0x434F4E564F59L);
        if (backgroundKind == ConvoyScene.BackgroundKind.SKY) {
            return List.of();
        }
        if (backgroundKind == ConvoyScene.BackgroundKind.SEA && random.nextFloat() > 0.55f) {
            return List.of();
        }
        int count = switch (backgroundKind) {
            case LAND_BIOME, TUNNEL -> 2 + random.nextInt(2);
            case SEA -> 1 + random.nextInt(2);
            default -> 0;
        };
        if (count <= 0) {
            return List.of();
        }
        ResourceType[] pool = {
                GameConstants.RESOURCE_PLANT,
                GameConstants.RESOURCE_WATER,
                GameConstants.RESOURCE_ROCK,
                GameConstants.RESOURCE_MEAT,
                GameConstants.RESOURCE_FUNGI,
                GameConstants.RESOURCE_RESIN
        };
        List<ConvoyResourceProp> props = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceType type = pool[random.nextInt(pool.length)];
            int quantity = switch (random.nextInt(3)) {
                case 0 -> ResourceType.SOURCE_QTY_SMALL;
                case 1 -> ResourceType.SOURCE_QTY_MEDIUM;
                default -> ResourceType.SOURCE_QTY_BIG;
            };
            boolean upperBand = random.nextBoolean();
            float yNorm = upperBand ? 0.14f + random.nextFloat() * 0.1f : 0.76f + random.nextFloat() * 0.1f;
            float slot = (i + 0.5f) / count;
            float xPhase = (slot + (random.nextFloat() - 0.5f) * 0.18f + 1f) % 1f;
            props.add(new ConvoyResourceProp(
                    type,
                    quantity,
                    xPhase,
                    yNorm,
                    random.nextInt(360)));
        }
        return List.copyOf(props);
    }

    public static void paintConvoyResources(Graphics2D g2d, List<ConvoyResourceProp> props, int fieldX, int fieldY,
            int fieldW, int fieldH, float scrollPixels, boolean travelingRight, ImageObserver observer) {
        if (props == null || props.isEmpty() || fieldW <= 0 || fieldH <= 0) {
            return;
        }
        int loop = Math.max(CONVOY_RESOURCE_LOOP_PX, fieldW + 160);
        float scrolled = travelingRight ? scrollPixels : -scrollPixels;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        for (ConvoyResourceProp prop : props) {
            if (prop.resourceType() == null) {
                continue;
            }
            ImageIcon icon = prop.resourceType().getIconForSourceQuantity(prop.quantity());
            if (icon == null) {
                continue;
            }
            Image image = icon.getImage();
            if (image == null) {
                continue;
            }
            int displayPx = prop.resourceType().getDisplaySizeForSourceQuantity(prop.quantity());
            int worldX = Math.round(prop.xPhase() * loop);
            int baseX = fieldX + Math.floorMod(worldX - Math.round(scrolled), loop);
            int drawY = fieldY + Math.round(prop.yNorm() * fieldH) - displayPx / 2;
            for (int x = baseX - loop; x < fieldX + fieldW + displayPx; x += loop) {
                if (x + displayPx < fieldX || x > fieldX + fieldW) {
                    continue;
                }
                AffineTransform old = g2d.getTransform();
                g2d.translate(x + displayPx / 2.0, drawY + displayPx / 2.0);
                g2d.rotate(Math.toRadians(prop.rotationDegrees()));
                g2d.drawImage(image, -displayPx / 2, -displayPx / 2, displayPx, displayPx, observer);
                g2d.setTransform(old);
            }
        }
    }
}
