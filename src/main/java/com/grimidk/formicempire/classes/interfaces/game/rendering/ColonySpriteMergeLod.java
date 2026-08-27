package com.grimidk.formicempire.classes.interfaces.game.rendering;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.critter.Critter;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.spatial.Dimension;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public final class ColonySpriteMergeLod {

    public static final int ZONE_NONE = -1;
    public static final int ZONE_ENTRANCE = 0;
    public static final int ZONE_STORAGE = 1;
    public static final int ZONE_FARM = 2;
    public static final int ZONE_NURSERY = 3;
    public static final int ZONE_ROYAL = 4;
    public static final int ZONE_RANCHER = 5;
    public static final int ZONE_GRAVER = 6;
    public static final int ZONE_INSECT_PEN = 7;
    public static final int ZONE_BREEDER = 8;
    public static final int ZONE_TRANSIT = 9;
    public static final int ZONE_COUNT = 10;

    public record AntMergeBucket(
            int typeId,
            int roleId,
            int subtypeCode,
            boolean parasiticMite,
            int carryPrimaryId,
            int carrySecondaryId,
            int carryAntTypeId,
            int cellX,
            int cellY) {
    }

    public record CritterMergeBucket(int speciesId, int legFrame, int cellX, int cellY) {
    }

    public record MergeZoneLayout(
            Rectangle entrance,
            Rectangle storage,
            Rectangle farm,
            Rectangle nursery,
            Rectangle royal,
            Rectangle rancher,
            Rectangle graver,
            Rectangle insectPen,
            Rectangle breeder,
            Rectangle transit) {
    }

    public static final class ZoneMergeContext {
        private final MergeZoneLayout layout;
        private final Dimension currentDimension;
        private final int[] zoneOccupancy;
        private final boolean[] zoneMergeActive;

        private ZoneMergeContext(MergeZoneLayout layout, Dimension currentDimension, int[] occupancy) {
            this.layout = layout;
            this.currentDimension = currentDimension;
            zoneOccupancy = occupancy;
            zoneMergeActive = new boolean[ZONE_COUNT];
            for (int zone = 0; zone < ZONE_COUNT; zone++) {
                zoneMergeActive[zone] = occupancy[zone] > GameNumbers.SPRITE_MERGE_ZONE_THRESHOLD;
            }
        }

        public boolean shouldMergeAnt(Ant ant) {
            return isZoneMergeActive(resolveAntZoneId(ant, currentDimension, layout));
        }

        public boolean shouldMergeCritter(Critter critter, int spriteW, int spriteH) {
            return isZoneMergeActive(resolveCritterZoneId(critter, currentDimension, layout, spriteW, spriteH));
        }

        public boolean isZoneMergeEnabled(int zoneId) {
            return isZoneMergeActive(zoneId);
        }

        public int getZoneOccupancy(int zoneId) {
            if (zoneId < 0 || zoneId >= ZONE_COUNT) {
                return 0;
            }
            return zoneOccupancy[zoneId];
        }

        public int effectiveMaxGroupSizeForAnt(Ant ant, int colonyMaxGroupSize) {
            return effectiveMaxGroupSizeForZone(resolveAntZoneId(ant, currentDimension, layout), colonyMaxGroupSize);
        }

        public int effectiveMaxGroupSizeForCritter(Critter critter, int spriteW, int spriteH, int colonyMaxGroupSize) {
            return effectiveMaxGroupSizeForZone(
                    resolveCritterZoneId(critter, currentDimension, layout, spriteW, spriteH),
                    colonyMaxGroupSize);
        }

        public int effectiveMaxGroupSizeForZone(int zoneId, int colonyMaxGroupSize) {
            if (zoneId < 0) {
                return colonyMaxGroupSize;
            }
            return zoneCappedMaxGroupSize(zoneOccupancy[zoneId], colonyMaxGroupSize);
        }

        private boolean isZoneMergeActive(int zoneId) {
            return zoneId >= 0 && zoneId < ZONE_COUNT && zoneMergeActive[zoneId];
        }
    }

    private ColonySpriteMergeLod() {
    }

    public static boolean isMergeActive(int colonyAntTotal) {
        return colonyAntTotal > GameNumbers.SPRITE_MERGE_ANT_THRESHOLD;
    }

    public static int maxGroupSize(int colonyAntTotal) {
        if (colonyAntTotal > GameNumbers.SPRITE_MERGE_ANT_LARGE_THRESHOLD) {
            return GameNumbers.SPRITE_MERGE_LARGE_GROUP_SIZE;
        }
        return GameNumbers.SPRITE_MERGE_GROUP_SIZE;
    }

    public static int zoneCappedMaxGroupSize(int zoneOccupancy, int colonyMaxGroupSize) {
        if (zoneOccupancy <= 0) {
            return Math.max(1, colonyMaxGroupSize);
        }
        int minVisibleSprites = Math.max(
                1,
                (int) Math.ceil(zoneOccupancy * GameNumbers.SPRITE_MERGE_ZONE_MIN_VISIBLE_FRACTION));
        int maxGroupForVisibleFloor = Math.max(1, zoneOccupancy / minVisibleSprites);
        return Math.max(1, Math.min(colonyMaxGroupSize, maxGroupForVisibleFloor));
    }

    public static int minimumVisibleSpritesForZone(int zoneOccupancy) {
        if (zoneOccupancy <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(zoneOccupancy * GameNumbers.SPRITE_MERGE_ZONE_MIN_VISIBLE_FRACTION));
    }

    public static int spatialCell(int coord) {
        return Math.floorDiv(coord, GameNumbers.SPRITE_MERGE_POSITION_CELL_PX);
    }

    public static AntMergeBucket antBucket(Ant ant, AntType type) {
        int roleId = ant.getRole() != null ? ant.getRole().getId() : 0;
        int subtypeCode = ant.getSubtypeProfile().getCode();
        int carryPrimaryId = ant.getCarrying() != null ? ant.getCarrying().getId() : 0;
        int carrySecondaryId = ant.getCarryingSec() != null ? ant.getCarryingSec().getId() : 0;
        int carryAntTypeId = ant.getCarryingAnt() != null ? ant.getCarryingAnt().getId() : 0;
        return new AntMergeBucket(
                type.getId(),
                roleId,
                subtypeCode,
                ant.isParasiticMiteInfected(),
                carryPrimaryId,
                carrySecondaryId,
                carryAntTypeId,
                spatialCell(ant.getX()),
                spatialCell(ant.getY()));
    }

    public static CritterMergeBucket critterBucket(Critter critter) {
        return new CritterMergeBucket(
                critter.getSpecies().getId(),
                critter.getLegFrame(),
                spatialCell(critter.getX()),
                spatialCell(critter.getY()));
    }

    public static ZoneMergeContext buildZoneMergeContext(
            Colony colony,
            Dimension currentDimension,
            MergeZoneLayout layout) {
        int[] occupancy = new int[ZONE_COUNT];
        if (colony == null || layout == null || currentDimension == null) {
            return new ZoneMergeContext(layout, currentDimension, occupancy);
        }
        for (AntType type : GameConstants.getAntTypes()) {
            if (type == GameConstants.TYPE_DEAD) {
                continue;
            }
            for (Ant ant : colony.getAntsByType(type)) {
                if (ant.getDimension() != currentDimension || ant.getDimension() == WorldSpaces.TUNNEL_WORLD) {
                    continue;
                }
                int zoneId = resolveAntZoneId(ant, currentDimension, layout);
                if (zoneId >= 0) {
                    occupancy[zoneId]++;
                }
            }
        }
        for (Critter critter : colony.getCritters()) {
            if (critter.getDimension() != currentDimension) {
                continue;
            }
            int zoneId = resolveCritterZoneId(critter, currentDimension, layout, 16, 16);
            if (zoneId >= 0) {
                occupancy[zoneId]++;
            }
        }
        return new ZoneMergeContext(layout, currentDimension, occupancy);
    }

    public static int mergeCentroidX(List<? extends Critter> group) {
        if (group.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (Critter critter : group) {
            sum += critter.getX();
        }
        return (int) (sum / group.size());
    }

    public static int mergeCentroidY(List<? extends Critter> group) {
        if (group.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (Critter critter : group) {
            sum += critter.getY();
        }
        return (int) (sum / group.size());
    }

    public static void forEachAntGroup(
            Map<AntMergeBucket, List<Ant>> buckets,
            int maxGroupSize,
            BiConsumer<AntMergeBucket, List<Ant>> consumer) {
        forEachGroup(buckets, group -> maxGroupSize, consumer);
    }

    public static void forEachAntGroup(
            Map<AntMergeBucket, List<Ant>> buckets,
            ToIntFunction<List<Ant>> maxGroupSizeResolver,
            BiConsumer<AntMergeBucket, List<Ant>> consumer) {
        forEachGroup(buckets, maxGroupSizeResolver, consumer);
    }

    public static void forEachCritterGroup(
            Map<CritterMergeBucket, List<Critter>> buckets,
            int maxGroupSize,
            BiConsumer<CritterMergeBucket, List<Critter>> consumer) {
        forEachGroup(buckets, group -> maxGroupSize, consumer);
    }

    public static void forEachCritterGroup(
            Map<CritterMergeBucket, List<Critter>> buckets,
            ToIntFunction<List<Critter>> maxGroupSizeResolver,
            BiConsumer<CritterMergeBucket, List<Critter>> consumer) {
        forEachGroup(buckets, maxGroupSizeResolver, consumer);
    }

    private static <K, T> void forEachGroup(
            Map<K, List<T>> buckets,
            int maxGroupSize,
            BiConsumer<K, List<T>> consumer) {
        forEachGroup(buckets, group -> maxGroupSize, consumer);
    }

    private static <K, T> void forEachGroup(
            Map<K, List<T>> buckets,
            ToIntFunction<List<T>> maxGroupSizeResolver,
            BiConsumer<K, List<T>> consumer) {
        for (Map.Entry<K, List<T>> entry : buckets.entrySet()) {
            List<T> members = entry.getValue();
            if (members.isEmpty()) {
                continue;
            }
            int chunkSize = Math.max(1, maxGroupSizeResolver.applyAsInt(members));
            for (int start = 0; start < members.size(); start += chunkSize) {
                int end = Math.min(start + chunkSize, members.size());
                consumer.accept(entry.getKey(), members.subList(start, end));
            }
        }
    }

    static int resolveAntZoneId(Ant ant, Dimension currentDimension, MergeZoneLayout layout) {
        if (ant == null || layout == null || currentDimension == null || ant.getDimension() != currentDimension) {
            return ZONE_NONE;
        }
        if (currentDimension == WorldSpaces.UNDERWORLD) {
            return resolveUnderworldPointZoneId(
                    ant.getX(),
                    ant.getY(),
                    layout.entrance(),
                    layout.storage(),
                    layout.farm(),
                    layout.nursery(),
                    layout.royal(),
                    layout.breeder(),
                    layout.transit(),
                    layout.insectPen());
        }
        if (currentDimension == WorldSpaces.OVERWORLD) {
            return resolveOverworldPointZoneId(
                    ant.getX(),
                    ant.getY(),
                    layout.rancher(),
                    layout.graver(),
                    layout.insectPen());
        }
        return ZONE_NONE;
    }

    static int resolveCritterZoneId(
            Critter critter,
            Dimension currentDimension,
            MergeZoneLayout layout,
            int spriteW,
            int spriteH) {
        if (critter == null || layout == null || currentDimension == null || critter.getDimension() != currentDimension) {
            return ZONE_NONE;
        }
        int cx = critter.getX() + Math.max(1, spriteW) / 2;
        int cy = critter.getY() + Math.max(1, spriteH) / 2;
        if (currentDimension == WorldSpaces.UNDERWORLD) {
            return resolveUnderworldPointZoneId(
                    cx,
                    cy,
                    layout.entrance(),
                    layout.storage(),
                    layout.farm(),
                    layout.nursery(),
                    layout.royal(),
                    layout.breeder(),
                    layout.transit(),
                    layout.insectPen());
        }
        if (currentDimension == WorldSpaces.OVERWORLD) {
            return resolveOverworldPointZoneId(
                    cx,
                    cy,
                    layout.rancher(),
                    layout.graver(),
                    layout.insectPen());
        }
        return ZONE_NONE;
    }

    private static int resolveUnderworldPointZoneId(
            int x,
            int y,
            Rectangle entrance,
            Rectangle storage,
            Rectangle farm,
            Rectangle nursery,
            Rectangle royal,
            Rectangle breeder,
            Rectangle transit,
            Rectangle insectPen) {
        if (containsPoint(storage, x, y)) {
            return ZONE_STORAGE;
        }
        if (containsPoint(farm, x, y)) {
            return ZONE_FARM;
        }
        if (containsPoint(nursery, x, y)) {
            return ZONE_NURSERY;
        }
        if (containsPoint(royal, x, y)) {
            return ZONE_ROYAL;
        }
        if (containsPoint(breeder, x, y)) {
            return ZONE_BREEDER;
        }
        if (containsPoint(transit, x, y)) {
            return ZONE_TRANSIT;
        }
        if (containsPoint(insectPen, x, y)) {
            return ZONE_INSECT_PEN;
        }
        if (containsPoint(entrance, x, y)) {
            return ZONE_ENTRANCE;
        }
        return ZONE_NONE;
    }

    private static int resolveOverworldPointZoneId(
            int x,
            int y,
            Rectangle rancher,
            Rectangle graver,
            Rectangle insectPen) {
        if (containsPoint(rancher, x, y)) {
            return ZONE_RANCHER;
        }
        if (containsPoint(graver, x, y)) {
            return ZONE_GRAVER;
        }
        if (containsPoint(insectPen, x, y)) {
            return ZONE_INSECT_PEN;
        }
        return ZONE_NONE;
    }

    private static boolean containsPoint(Rectangle rect, int x, int y) {
        return rect != null && rect.contains(x, y);
    }
}
