package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.dynasty.WarStagePhase;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WarBattleScene {

    /** Soft cap for reserve sprites only (active ants are drawn 1:1). */
    public static final int MAX_VISUAL_RESERVE_ANTS_PER_SIDE = 120;

    /** Pixel gap behind the infantry contact line for each deeper battle line. */
    public static final int ARTILLERY_LINE_OFFSET_PX = 200;
    public static final int AIR_SUPPORT_LINE_OFFSET_PX = 400;
    public static final int RESERVE_LINE_OFFSET_PX = 560;

    public record Side(int dynastyId, String dynastyName, AntSpecies species,
            Map<BattleLine, Map<AntType, Integer>> activeByLine,
            Map<BattleLine, Map<AntType, Integer>> reserveByLine,
            int livingArmy, int livingActive) {
        public Side {
            activeByLine = copyLineMaps(activeByLine);
            reserveByLine = copyLineMaps(reserveByLine);
        }

        public Map<AntType, Integer> activeTypeCounts() {
            return flatten(activeByLine);
        }

        public Map<AntType, Integer> reserveTypeCounts() {
            return flatten(reserveByLine);
        }

        /** Total living ants by type (active + reserve). */
        public Map<AntType, Integer> typeCounts() {
            Map<AntType, Integer> total = new HashMap<>(activeTypeCounts());
            for (Map.Entry<AntType, Integer> e : reserveTypeCounts().entrySet()) {
                total.merge(e.getKey(), e.getValue() != null ? e.getValue() : 0, Integer::sum);
            }
            return Collections.unmodifiableMap(total);
        }

        public int deployedPower() {
            return livingArmy;
        }

        private static Map<BattleLine, Map<AntType, Integer>> copyLineMaps(
                Map<BattleLine, Map<AntType, Integer>> source) {
            Map<BattleLine, Map<AntType, Integer>> out = new LinkedHashMap<>();
            if (source == null) {
                return Collections.unmodifiableMap(out);
            }
            for (Map.Entry<BattleLine, Map<AntType, Integer>> e : source.entrySet()) {
                if (e.getKey() == null) {
                    continue;
                }
                out.put(e.getKey(), e.getValue() == null
                        ? Map.of()
                        : Collections.unmodifiableMap(new HashMap<>(e.getValue())));
            }
            return Collections.unmodifiableMap(out);
        }

        private static Map<AntType, Integer> flatten(Map<BattleLine, Map<AntType, Integer>> byLine) {
            Map<AntType, Integer> total = new HashMap<>();
            if (byLine == null) {
                return total;
            }
            for (Map<AntType, Integer> counts : byLine.values()) {
                if (counts == null) {
                    continue;
                }
                for (Map.Entry<AntType, Integer> e : counts.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null && e.getValue() > 0) {
                        total.merge(e.getKey(), e.getValue(), Integer::sum);
                    }
                }
            }
            return total;
        }
    }

    private final String warName;
    private final String locationName;
    private final Biome attackerBiome;
    private final Biome defenderBiome;
    private final Side attacker;
    private final Side defender;
    private final WarStagePhase phase;
    private final float frontlineRatio;
    private final float stageProgress;
    private final float warProgressPercent;
    private final int redeployHoursRemaining;
    private final boolean defenseOnlyBiome;
    private final boolean available;

    public WarBattleScene(String warName, String locationName, Biome attackerBiome, Biome defenderBiome,
            Side attacker, Side defender, WarStagePhase phase, float frontlineRatio, float stageProgress,
            float warProgressPercent, int redeployHoursRemaining, boolean defenseOnlyBiome, boolean available) {
        this.warName = warName != null ? warName : "";
        this.locationName = locationName != null ? locationName : "";
        this.attackerBiome = attackerBiome;
        this.defenderBiome = defenderBiome;
        this.attacker = attacker;
        this.defender = defender;
        this.phase = phase != null ? phase : GameConstants.WAR_STAGE_ACTIVE_CLASH;
        this.frontlineRatio = Math.max(0.05f, Math.min(0.95f, frontlineRatio));
        this.stageProgress = Math.max(0f, Math.min(1f, stageProgress));
        this.warProgressPercent = warProgressPercent;
        this.redeployHoursRemaining = Math.max(0, redeployHoursRemaining);
        this.defenseOnlyBiome = defenseOnlyBiome;
        this.available = available;
    }

    public String getWarName() {
        return warName;
    }

    public String getLocationName() {
        return locationName;
    }

    public Biome getAttackerBiome() {
        return attackerBiome;
    }

    public Biome getDefenderBiome() {
        return defenderBiome;
    }

    public Side getAttacker() {
        return attacker;
    }

    public Side getDefender() {
        return defender;
    }

    public WarStagePhase getPhase() {
        return phase;
    }

    public float getFrontlineRatio() {
        return frontlineRatio;
    }

    public float getStageProgress() {
        return stageProgress;
    }

    public float getWarProgressPercent() {
        return warProgressPercent;
    }

    public int getRedeployHoursRemaining() {
        return redeployHoursRemaining;
    }

    public boolean isDefenseOnlyBiome() {
        return defenseOnlyBiome;
    }

    public boolean isAvailable() {
        return available;
    }
}
