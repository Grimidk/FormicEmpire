package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.constants.world.Biome;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class WarBattleScene {

    public static final int MAX_VISUAL_ANTS_PER_SIDE = 200;

    public record Side(int dynastyId, String dynastyName, Species species, Map<AntType, Integer> typeCounts,
            int deployedPower) {
        public Side {
            typeCounts = typeCounts == null
                    ? Map.of()
                    : Collections.unmodifiableMap(new HashMap<>(typeCounts));
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
        this.phase = phase != null ? phase : WarStagePhase.ACTIVE_CLASH;
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
