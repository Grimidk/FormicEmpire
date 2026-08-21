package com.grimidk.formicempire.classes.interfaces.menu;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSpecies;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.dynasty.BattleLine;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.services.colony.ConvoyScene;
import com.grimidk.formicempire.classes.entities.services.world.WarBattleScene;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals;
import com.grimidk.formicempire.classes.interfaces.game.rendering.RouteViewVisuals.ConvoyResourceProp;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class MenuChaoticWorld {

    private static final float COLONY_ENTRANCE_X = 0.5f;
    private static final float COLONY_ENTRANCE_Y = 0.5f;

    public static final class ShowcaseAnt {
        public final AntType type;
        public final AntSpecies species;
        public final AntSubtypeProfile profile;
        public final boolean attackerSide;
        public final BattleLine battleLine;
        public final boolean reserve;
        public float xNorm;
        public float yNorm;
        public float vx;
        public float vy;
        public float laneY;
        public float laneJitter;
        public float wobblePhase;
        public float motionRate;
        public float flyPhase;
        public float offsetX;
        public float offsetY;
        public float wanderTimer;
        public Image cachedSprite;
        public int cachedLegFrame = -1;
        public int cachedJawFrame = -1;
        public int cachedWingFrame = -1;
        public int cachedAntennaFrame = -1;
        public int cachedDrawW;
        public int cachedDrawH;

        private ShowcaseAnt(MenuChaoticAntEntry entry, Random random) {
            type = entry.type();
            species = entry.species();
            profile = AntSubtypeProfile.fromCode(entry.profileCode());
            attackerSide = entry.attackerSide();
            battleLine = entry.battleLine();
            reserve = entry.reserve();
            wobblePhase = random.nextFloat() * (float) (Math.PI * 2);
            motionRate = 0.85f + random.nextFloat() * 0.3f;
            flyPhase = random.nextFloat();
            laneJitter = random.nextFloat();
            wanderTimer = random.nextFloat() * 2f;
        }
    }

    public static final class ShowcaseCritter {
        public final Species species;
        public float xNorm;
        public float yNorm;
        public float vx;
        public float vy;
        public float wobblePhase;
        public float motionRate;

        private ShowcaseCritter(MenuChaoticCritterEntry entry, Random random) {
            species = entry.species();
            wobblePhase = random.nextFloat() * (float) (Math.PI * 2);
            motionRate = 0.8f + random.nextFloat() * 0.35f;
        }
    }

    private final MenuChaoticDefinition definition;
    private final List<ShowcaseAnt> ants;
    private final List<ShowcaseCritter> critters;
    private boolean convoyTravelingRight;
    private final List<ConvoyResourceProp> convoyResources;
    private float contactLineRatio = 0.5f;
    private float targetContactLineRatio = 0.52f;
    private float contactLineDirection = 1f;

    private MenuChaoticWorld(MenuChaoticDefinition definition, List<ShowcaseAnt> ants, List<ShowcaseCritter> critters,
            boolean convoyTravelingRight, List<ConvoyResourceProp> convoyResources) {
        this.definition = definition;
        this.ants = ants;
        this.critters = critters;
        this.convoyTravelingRight = convoyTravelingRight;
        this.convoyResources = convoyResources;
        layoutInitialPositions();
    }

    public static MenuChaoticWorld fromDefinition(MenuChaoticDefinition definition) {
        Random random = new Random(definition.layoutSeed());
        List<ShowcaseAnt> ants = new ArrayList<>(definition.antCount());
        List<MenuChaoticAntEntry> template = definition.antTemplate();
        if (!template.isEmpty()) {
            int antCount = Math.min(definition.antCount(), MenuChaoticCatalog.MAX_ANTS);
            for (int i = 0; i < antCount; i++) {
                MenuChaoticAntEntry entry = template.get(i % template.size());
                ants.add(new ShowcaseAnt(entry, random));
            }
        }
        List<ShowcaseCritter> critters = new ArrayList<>(definition.critterCount());
        List<MenuChaoticCritterEntry> critterTemplate = definition.critterTemplate();
        if (!critterTemplate.isEmpty()) {
            for (int i = 0; i < definition.critterCount(); i++) {
                critters.add(new ShowcaseCritter(critterTemplate.get(i % critterTemplate.size()), random));
            }
        }
        boolean convoyTravelingRight = definition.kind() != MenuChaoticKind.CONVOY
                || definition.convoyTravelingRight();
        List<ConvoyResourceProp> convoyResources = definition.kind() == MenuChaoticKind.CONVOY
                ? RouteViewVisuals.buildConvoyResourceProps(definition.layoutSeed(), definition.convoyBackground())
                : List.of();
        return new MenuChaoticWorld(definition, List.copyOf(ants), List.copyOf(critters), convoyTravelingRight,
                convoyResources);
    }

    public MenuChaoticDefinition getDefinition() {
        return definition;
    }

    public List<ShowcaseAnt> getAnts() {
        return ants;
    }

    public List<ShowcaseCritter> getCritters() {
        return critters;
    }

    public float getContactLineRatio() {
        return contactLineRatio;
    }

    public boolean isConvoyTravelingRight() {
        return convoyTravelingRight;
    }

    public void setConvoyTravelingRight(boolean travelingRight) {
        this.convoyTravelingRight = travelingRight;
    }

    public void randomizeConvoyTravelDirection(Random random) {
        if (definition.kind() != MenuChaoticKind.CONVOY || random == null) {
            return;
        }
        convoyTravelingRight = random.nextBoolean();
    }

    public List<ConvoyResourceProp> getConvoyResources() {
        return convoyResources;
    }

    public void update(float deltaSeconds, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        switch (definition.kind()) {
            case OVERWORLD -> updateOverworld(deltaSeconds);
            case COLONY -> updateColony(deltaSeconds);
            case BATTLE -> updateBattle(deltaSeconds);
            case CONVOY -> updateConvoy(deltaSeconds);
        }
    }

    private void layoutInitialPositions() {
        Random random = new Random(definition.layoutSeed() ^ 0x9E37_79B9L);
        switch (definition.kind()) {
            case OVERWORLD -> layoutOverworld(random);
            case COLONY -> layoutColony(random);
            case BATTLE -> layoutBattle(random);
            case CONVOY -> layoutConvoy(random);
        }
    }

    private void layoutOverworld(Random random) {
        for (ShowcaseAnt ant : ants) {
            ant.xNorm = random.nextFloat();
            ant.yNorm = random.nextFloat();
            assignWanderVelocity(ant, random);
        }
        for (ShowcaseCritter critter : critters) {
            critter.xNorm = 0.1f + random.nextFloat() * 0.8f;
            critter.yNorm = 0.1f + random.nextFloat() * 0.8f;
            float angle = random.nextFloat() * (float) (Math.PI * 2);
            float speed = 0.05f + random.nextFloat() * 0.08f;
            critter.vx = (float) Math.cos(angle) * speed;
            critter.vy = (float) Math.sin(angle) * speed;
        }
    }

    private void layoutColony(Random random) {
        for (ShowcaseAnt ant : ants) {
            ant.xNorm = COLONY_ENTRANCE_X;
            ant.yNorm = COLONY_ENTRANCE_Y;
            assignWanderVelocity(ant, random);
            ant.wanderTimer = random.nextFloat() * 0.4f;
        }
    }

    private void layoutBattle(Random random) {
        int attackerCount = 0;
        int defenderCount = 0;
        for (ShowcaseAnt ant : ants) {
            if (ant.attackerSide) {
                attackerCount++;
            } else {
                defenderCount++;
            }
        }
        int attackerIndex = 0;
        int defenderIndex = 0;
        for (ShowcaseAnt ant : ants) {
            int lineTotal;
            int indexInLine;
            if (ant.attackerSide) {
                lineTotal = Math.max(1, attackerCount);
                indexInLine = attackerIndex++;
            } else {
                lineTotal = Math.max(1, defenderCount);
                indexInLine = defenderIndex++;
            }
            ant.laneY = lineTotal <= 1 ? 0.5f : indexInLine / (float) (lineTotal - 1);
            ant.laneY = Math.max(0.02f, Math.min(0.98f, ant.laneY + (random.nextFloat() - 0.5f) * 0.04f));
        }
    }

    private void layoutConvoy(Random random) {
        for (ShowcaseAnt ant : ants) {
            float angle = random.nextFloat() * (float) (Math.PI * 2);
            float dist = (float) Math.sqrt(random.nextFloat());
            ant.offsetX = (float) Math.cos(angle) * dist;
            ant.offsetY = (float) Math.sin(angle) * dist;
        }
    }

    private void updateOverworld(float deltaSeconds) {
        float speedScale = MenuChaoticCatalog.OVERWORLD_SPEED_SCALE;
        for (ShowcaseAnt ant : ants) {
            tickWander(ant, deltaSeconds);
            moveAntWrapped(ant, deltaSeconds, speedScale);
        }
        for (ShowcaseCritter critter : critters) {
            critter.xNorm += critter.vx * deltaSeconds * speedScale;
            critter.yNorm += critter.vy * deltaSeconds * speedScale;
            if (critter.xNorm < 0f || critter.xNorm > 1f) {
                critter.vx *= -1f;
                critter.xNorm = Math.max(0.02f, Math.min(0.98f, critter.xNorm));
            }
            if (critter.yNorm < 0f || critter.yNorm > 1f) {
                critter.vy *= -1f;
                critter.yNorm = Math.max(0.02f, Math.min(0.98f, critter.yNorm));
            }
        }
    }

    private void updateColony(float deltaSeconds) {
        float speedScale = MenuChaoticCatalog.COLONY_SPEED_SCALE;
        Random random = null;
        for (int i = 0; i < ants.size(); i++) {
            ShowcaseAnt ant = ants.get(i);
            tickWander(ant, deltaSeconds);
            ant.xNorm += ant.vx * deltaSeconds * speedScale;
            ant.yNorm += ant.vy * deltaSeconds * speedScale;
            if (ant.xNorm < 0f || ant.xNorm > 1f || ant.yNorm < 0f || ant.yNorm > 1f) {
                ant.xNorm = COLONY_ENTRANCE_X;
                ant.yNorm = COLONY_ENTRANCE_Y;
                if (random == null) {
                    random = new Random();
                }
                random.setSeed(definition.layoutSeed() ^ (i * 0x9E37_79B9L) ^ (long) (ant.wobblePhase * 1000));
                assignWanderVelocity(ant, random);
                ant.wanderTimer = 0.2f + ant.laneJitter * 0.6f;
            }
        }
    }

    private void updateBattle(float deltaSeconds) {
        float delta = targetContactLineRatio - contactLineRatio;
        if (Math.abs(delta) > 0.0005f) {
            contactLineRatio += delta * 0.06f;
        } else {
            contactLineRatio = targetContactLineRatio;
        }
        targetContactLineRatio += contactLineDirection * deltaSeconds * MenuChaoticCatalog.BATTLE_LINE_DRIFT_PER_SEC;
        if (targetContactLineRatio > 0.58f) {
            targetContactLineRatio = 0.58f;
            contactLineDirection = -1f;
        } else if (targetContactLineRatio < 0.42f) {
            targetContactLineRatio = 0.42f;
            contactLineDirection = 1f;
        }
    }

    private void updateConvoy(float deltaSeconds) {
        for (ShowcaseAnt ant : ants) {
            ant.wobblePhase += deltaSeconds * 2.5f * ant.motionRate;
        }
    }

    private static void tickWander(ShowcaseAnt ant, float deltaSeconds) {
        ant.wanderTimer -= deltaSeconds;
        if (ant.wanderTimer <= 0f) {
            float angle = ant.wobblePhase * 2.37f + ant.motionRate * 5.1f;
            float speed = 0.08f + (ant.motionRate - 0.85f) * 0.4f;
            ant.vx = (float) Math.cos(angle) * speed;
            ant.vy = (float) Math.sin(angle) * speed;
            ant.wanderTimer = 0.6f + ant.laneJitter * 1.8f;
            ant.wobblePhase += 1.37f;
        }
    }

    private static void assignWanderVelocity(ShowcaseAnt ant, Random random) {
        float angle = random.nextFloat() * (float) (Math.PI * 2);
        float speed = 0.08f + random.nextFloat() * 0.12f;
        ant.vx = (float) Math.cos(angle) * speed;
        ant.vy = (float) Math.sin(angle) * speed;
    }

    private static void moveAntWrapped(ShowcaseAnt ant, float deltaSeconds, float speedScale) {
        ant.xNorm += ant.vx * deltaSeconds * speedScale;
        ant.yNorm += ant.vy * deltaSeconds * speedScale;
        if (ant.xNorm < 0f) {
            ant.xNorm += 1f;
        } else if (ant.xNorm > 1f) {
            ant.xNorm -= 1f;
        }
        if (ant.yNorm < 0f) {
            ant.yNorm += 1f;
        } else if (ant.yNorm > 1f) {
            ant.yNorm -= 1f;
        }
    }

    public Biome getPrimaryBiome() {
        return definition.primaryBiome();
    }

    public Biome getSecondaryBiome() {
        return definition.secondaryBiome();
    }

    public ConvoyScene.BackgroundKind getConvoyBackground() {
        return definition.convoyBackground();
    }

    public MenuChaoticKind getKind() {
        return definition.kind();
    }

    public static float colonyEntranceXNorm() {
        return COLONY_ENTRANCE_X;
    }

    public static float colonyEntranceYNorm() {
        return COLONY_ENTRANCE_Y;
    }

    public static int battleLineOffsetPx(BattleLine line, boolean reserve) {
        if (reserve) {
            return WarBattleScene.RESERVE_LINE_OFFSET_PX;
        }
        if (line == GameConstants.BATTLE_LINE_ARTILLERY) {
            return WarBattleScene.ARTILLERY_LINE_OFFSET_PX;
        }
        if (line == GameConstants.BATTLE_LINE_AIR_SUPPORT) {
            return WarBattleScene.AIR_SUPPORT_LINE_OFFSET_PX;
        }
        return 0;
    }
}
