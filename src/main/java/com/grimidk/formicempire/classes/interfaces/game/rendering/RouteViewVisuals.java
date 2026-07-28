package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class RouteViewVisuals {

    private static final float ATTACK_JAW_SNAP_SPEED = 4.2f;
    private static final float WING_FLICKER_CYCLE_SEC = 5.5f;
    private static final float WING_FLICKER_CLOSED_START_SEC = 4.2f;
    private static final float WING_FLICKER_CLOSED_END_SEC = 5.2f;

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
            props.add(new ConvoyResourceProp(
                    type,
                    quantity,
                    random.nextFloat(),
                    yNorm,
                    random.nextInt(360)));
        }
        return List.copyOf(props);
    }
}
