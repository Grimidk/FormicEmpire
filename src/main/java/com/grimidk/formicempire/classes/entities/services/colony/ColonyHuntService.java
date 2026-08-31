package com.grimidk.formicempire.classes.entities.services.colony;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.critter.BugRole;
import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Biome;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.hunt.HuntExpedition;
import com.grimidk.formicempire.classes.entities.hunt.KnownHuntTarget;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.entities.world.ResourceSourcePlacement;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public final class ColonyHuntService {

    public static final class HuntDispatchPreview {
        public final int partySize;
        public final float travelHours;
        public final float winChance;
        public final int rewardProtein;
        public final int rewardMushrooms;
        public final int rewardResearch;

        HuntDispatchPreview(int partySize, float travelHours, float winChance, int rewardProtein,
                int rewardMushrooms, int rewardResearch) {
            this.partySize = partySize;
            this.travelHours = travelHours;
            this.winChance = winChance;
            this.rewardProtein = rewardProtein;
            this.rewardMushrooms = rewardMushrooms;
            this.rewardResearch = rewardResearch;
        }
    }

    private static final List<AntType> HUNT_ANT_TYPES = List.of(
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS);

    private ColonyHuntService() {
    }

    public static int getKnownTargetCapacity(Colony colony) {
        return colony != null ? colony.getSourceCapacity() : 1;
    }

    public static boolean isKnownTargetCapacityFull(Colony colony) {
        return colony != null && colony.getKnownHuntTargets().size() >= getKnownTargetCapacity(colony);
    }

    public static int maxPartyCapacity(Colony colony) {
        Dynasty dynasty = colony != null ? colony.getDynasty() : null;
        int capacity = dynasty != null ? dynasty.getCombatCapacity() : GameNumbers.COMBAT_CAPACITY_BASE;
        return Math.max(1, capacity / GameNumbers.HUNT_MAX_PARTY_FROM_CAPACITY_DIVISOR);
    }

    public static Map<AntType, Integer> getAvailableHunterCountsByType(Colony colony) {
        return ColonyUnassignedAntService.countUnassignedByTypes(colony, HUNT_ANT_TYPES,
                ColonyUnassignedAntService.resolveEngine(colony));
    }

    public static Map<AntType, Integer> countPartyByType(List<Ant> party) {
        Map<AntType, Integer> counts = new HashMap<>();
        for (AntType type : HUNT_ANT_TYPES) {
            counts.put(type, 0);
        }
        if (party == null) {
            return counts;
        }
        for (Ant ant : party) {
            if (ant == null) {
                continue;
            }
            AntType type = ant.getAntType();
            if (type != null && counts.containsKey(type)) {
                counts.merge(type, 1, Integer::sum);
            }
        }
        return counts;
    }

    public static KnownHuntTarget findKnownTarget(Colony colony, int targetId) {
        return findTarget(colony, targetId);
    }

    public static HuntDispatchPreview previewDispatch(Colony colony, KnownHuntTarget target,
            Map<AntType, Integer> partyCounts) {
        List<Ant> party = buildPartyFromCounts(colony, partyCounts);
        Species species = target != null ? target.getSpecies() : null;
        if (species == null) {
            species = GameConstants.TYPE_COCKROACH;
        }
        float travelHours = computeTravelHours(colony, target, party);
        float winChance = estimateWinChance(species, party);
        int protein = 0;
        int mushrooms = 0;
        int research = 0;
        if (species.hasBugRole(BugRole.HUNT) && species == GameConstants.TYPE_COCKROACH) {
            protein = GameNumbers.HUNT_COCKROACH_REWARD_PROTEIN;
            mushrooms = GameNumbers.HUNT_COCKROACH_REWARD_MUSHROOMS;
            research = GameNumbers.HUNT_COCKROACH_REWARD_RP;
        }
        return new HuntDispatchPreview(party.size(), travelHours, winChance, protein, mushrooms, research);
    }

    public static float estimateWinChance(Species species, List<Ant> party) {
        if (species == null || party == null || party.isEmpty()) {
            return 0f;
        }
        float hunterPower = 0f;
        for (Ant ant : party) {
            if (ant == null || !ant.isAlive()) {
                continue;
            }
            hunterPower += Math.max(1f, ant.getAttack())
                    * Math.max(1f, ant.getAttackSpeed())
                    * Math.max(1f, ant.getHealth());
        }
        if (hunterPower <= 0f) {
            return 0f;
        }
        float bugPower = HuntCreatureCombatService.resolveBugMaxHealth(species)
                * HuntCreatureCombatService.resolveBugAttack(species)
                * Math.max(1, HuntCreatureCombatService.resolveBugAttackSpeed(species));
        if (bugPower <= 0f) {
            return 1f;
        }
        return GameNumbers.warBattleWinChance(hunterPower / bugPower);
    }

    public static void runScoutBugDiscovery(Colony colony, Biome biome, Hex currentHex, int worldDay) {
        if (colony == null || !colony.isPlayer() || !colony.runsFullSimulation()) {
            return;
        }
        if (!colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {
            return;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return;
        }
        if (biome == null || GameConstants.getActiveHuntSpeciesForBiome(biome).isEmpty()) {
            return;
        }
        if (isKnownTargetCapacityFull(colony)) {
            return;
        }

        int scoutCount = colony.getActiveRoleCount(GameConstants.ROLE_SCOUT);
        if (scoutCount <= 0) {
            return;
        }

        float chancePerScout = colony.getStatsService().getScoutingRate(colony) * GameNumbers.HUNT_SCOUT_DISCOVERY_MULT;
        float totalChance = scoutCount * chancePerScout;
        while (totalChance > 0f) {
            boolean found = false;
            if (totalChance >= 1f) {
                found = true;
                totalChance -= 1f;
            } else if (GameRandom.nextFloat() < totalChance) {
                found = true;
                totalChance = 0f;
            } else {
                totalChance = 0f;
            }
            if (found) {
                if (isKnownTargetCapacityFull(colony)) {
                    totalChance = 0f;
                } else {
                    registerDiscoveredBug(colony, biome, currentHex, worldDay);
                    if (isKnownTargetCapacityFull(colony)) {
                        totalChance = 0f;
                    }
                }
            }
        }
    }

    public static void tickEscapedTargets(Colony colony, int worldDay) {
        if (colony == null) {
            return;
        }
        Iterator<KnownHuntTarget> it = colony.getKnownHuntTargets().iterator();
        while (it.hasNext()) {
            KnownHuntTarget target = it.next();
            if (target.hasEscaped(worldDay)) {
                cancelExpeditionForTarget(colony, target.getId());
                it.remove();
                Species species = target.getSpecies();
                String name = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_COCKROACH);
                colony.logEvent(ColonyLogPrefixes.HUNT + " "
                        + LanguageStrings.format(LanguageStrings.LOG_HUNT_BUG_ESCAPED_FMT, name));
            }
        }
    }

    public static void tickExpeditions(Colony colony) {
        if (colony == null) {
            return;
        }
        purgeOrphanedExpeditions(colony);
        for (HuntExpedition expedition : List.copyOf(colony.getActiveHuntExpeditions())) {
            tickOneExpedition(colony, expedition);
        }
    }

    private static void tickOneExpedition(Colony colony, HuntExpedition expedition) {
        if (expedition == null) {
            return;
        }
        if (expedition.getPhase() == HuntExpedition.Phase.TRAVELING) {
            expedition.advanceTravelOneHour();
            if (expedition.isTravelComplete()) {
                beginCombat(colony, expedition);
            }
            return;
        }
        if (expedition.getPhase() == HuntExpedition.Phase.FIGHTING) {
            HuntCreatureCombatService.TickOutcome outcome =
                    HuntCreatureCombatService.tick(colony, expedition.getTargetId());
            if (outcome == HuntCreatureCombatService.TickOutcome.HUNTERS_WIN) {
                resolveHuntWin(colony, expedition);
            } else if (outcome == HuntCreatureCombatService.TickOutcome.BUG_WINS) {
                resolveHuntLoss(colony, expedition);
            }
        }
    }

    private static void purgeOrphanedExpeditions(Colony colony) {
        List<HuntExpedition> expeditions = colony.getActiveHuntExpeditions();
        if (expeditions.isEmpty()) {
            return;
        }
        for (HuntExpedition expedition : List.copyOf(expeditions)) {
            if (findTarget(colony, expedition.getTargetId()) == null) {
                cancelExpeditionForTarget(colony, expedition.getTargetId());
            }
        }
    }

    private static void cancelExpeditionForTarget(Colony colony, int targetId) {
        HuntCreatureCombatService.clear(colony, targetId);
        colony.removeHuntExpedition(targetId);
    }

    public static boolean canDispatchHunt(Colony colony) {
        if (colony == null || !colony.isPlayer()) {
            return false;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return false;
        }
        for (KnownHuntTarget target : colony.getKnownHuntTargets()) {
            if (canDispatchHunt(colony, target.getId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canDispatchHunt(Colony colony, int targetId) {
        if (colony == null || !colony.isPlayer()) {
            return false;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return false;
        }
        if (findTarget(colony, targetId) == null) {
            return false;
        }
        return colony.getHuntExpeditionForTarget(targetId) == null;
    }

    public static int countAvailableHunters(Colony colony) {
        return buildPartyFromCounts(colony, maxAvailableCounts(colony)).size();
    }

    private static Map<AntType, Integer> maxAvailableCounts(Colony colony) {
        Map<AntType, Integer> counts = new HashMap<>();
        if (colony == null) {
            return counts;
        }
        Map<AntType, Integer> available = getAvailableHunterCountsByType(colony);
        for (AntType type : HUNT_ANT_TYPES) {
            counts.put(type, available.getOrDefault(type, 0));
        }
        return counts;
    }

    public static int maxSuggestedPartySize(Colony colony) {
        return Math.min(maxPartyCapacity(colony), countAvailableHunters(colony));
    }

    public static boolean dispatchHunt(Colony colony, int targetId, Map<AntType, Integer> partyCounts) {
        if (colony == null || !canDispatchHunt(colony, targetId) || partyCounts == null) {
            return false;
        }
        KnownHuntTarget target = findTarget(colony, targetId);
        if (target == null) {
            return false;
        }
        List<Ant> party = buildPartyFromCounts(colony, partyCounts);
        if (party.isEmpty() || party.size() > maxPartyCapacity(colony)) {
            return false;
        }
        Map<AntType, Integer> available = getAvailableHunterCountsByType(colony);
        for (Map.Entry<AntType, Integer> entry : partyCounts.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            if (entry.getValue() > available.getOrDefault(entry.getKey(), 0)) {
                return false;
            }
        }
        float travelHours = computeTravelHours(colony, target, party);
        colony.addHuntExpedition(new HuntExpedition(target.getId(), party, travelHours));
        Species species = target.getSpecies();
        String bugName = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_COCKROACH);
        colony.logEvent(ColonyLogPrefixes.HUNT + " "
                + LanguageStrings.format(LanguageStrings.LOG_HUNT_DISPATCHED_FMT, party.size(), bugName));
        return true;
    }

    public static void registerDiscoveredBug(Colony colony, Biome biome, Hex currentHex, int worldDay) {
        if (isKnownTargetCapacityFull(colony)) {
            Species species = pickDiscoveredHuntSpecies(biome);
            String name = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_COCKROACH);
            colony.logEvent(ColonyLogPrefixes.HUNT + " "
                    + LanguageStrings.format(LanguageStrings.LOG_HUNT_TARGETS_FULL_FMT, name));
            return;
        }
        Species species = pickDiscoveredHuntSpecies(biome);
        if (species == null) {
            return;
        }
        Point spawn = pickBugLocation(colony, currentHex);
        if (spawn == null) {
            return;
        }
        int id = colony.nextHuntTargetId();
        KnownHuntTarget target = new KnownHuntTarget(
                id,
                species.getId(),
                spawn.x,
                spawn.y,
                worldDay,
                worldDay + GameNumbers.HUNT_BUG_ESCAPE_DAYS);
        colony.getKnownHuntTargets().add(target);
        colony.logEvent(ColonyLogPrefixes.HUNT + " "
                + LanguageStrings.format(LanguageStrings.LOG_HUNT_SCOUT_FOUND_FMT, species.getName()));
    }

    private static Species pickDiscoveredHuntSpecies(Biome biome) {
        List<Species> candidates = GameConstants.getActiveHuntSpeciesForBiome(biome);
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(0);
    }

    private static void beginCombat(Colony colony, HuntExpedition expedition) {
        KnownHuntTarget target = findTarget(colony, expedition.getTargetId());
        if (target == null) {
            cancelExpeditionForTarget(colony, expedition.getTargetId());
            return;
        }
        HuntCreatureCombatService.startBattle(colony, target, new ArrayList<>(expedition.getParty()));
        expedition.setPhase(HuntExpedition.Phase.FIGHTING);
        Species species = target.getSpecies();
        String bugName = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_COCKROACH);
        colony.logEvent(ColonyLogPrefixes.HUNT + " "
                + LanguageStrings.format(LanguageStrings.LOG_HUNT_COMBAT_STARTED_FMT, bugName));
    }

    private static void resolveHuntWin(Colony colony, HuntExpedition expedition) {
        KnownHuntTarget target = findTarget(colony, expedition.getTargetId());
        Species species = target != null ? target.getSpecies() : GameConstants.TYPE_COCKROACH;
        applyRewards(colony, species);
        if (target != null) {
            colony.getKnownHuntTargets().remove(target);
        }
        HuntCreatureCombatService.clear(colony, expedition.getTargetId());
        colony.removeHuntExpedition(expedition.getTargetId());
        String bugName = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_COCKROACH);
        colony.logEvent(ColonyLogPrefixes.HUNT_SUCCESS + " "
                + LanguageStrings.format(LanguageStrings.LOG_HUNT_VICTORY_FMT, bugName));
    }

    private static void resolveHuntLoss(Colony colony, HuntExpedition expedition) {
        KnownHuntTarget target = findTarget(colony, expedition.getTargetId());
        if (target != null) {
            colony.getKnownHuntTargets().remove(target);
        }
        HuntCreatureCombatService.clear(colony, expedition.getTargetId());
        colony.removeHuntExpedition(expedition.getTargetId());
        Species species = target != null ? target.getSpecies() : GameConstants.TYPE_COCKROACH;
        String bugName = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_COCKROACH);
        colony.logEvent(ColonyLogPrefixes.HUNT_FAILURE + " "
                + LanguageStrings.format(LanguageStrings.LOG_HUNT_DEFEAT_FMT, bugName));
    }

    private static void applyRewards(Colony colony, Species species) {
        if (colony == null) {
            return;
        }
        if (species != null && species.hasBugRole(BugRole.HUNT) && species == GameConstants.TYPE_COCKROACH) {
            colony.setProtein(colony.getProtein() + GameNumbers.HUNT_COCKROACH_REWARD_PROTEIN);
            colony.setMushrooms(colony.getMushrooms() + GameNumbers.HUNT_COCKROACH_REWARD_MUSHROOMS);
            colony.addResearchPoints(GameNumbers.HUNT_COCKROACH_REWARD_RP);
        }
    }

    private static KnownHuntTarget findTarget(Colony colony, int targetId) {
        for (KnownHuntTarget target : colony.getKnownHuntTargets()) {
            if (target.getId() == targetId) {
                return target;
            }
        }
        return null;
    }

    private static List<Ant> collectAvailableHunters(Colony colony, int maxCount) {
        Map<AntType, Integer> available = getAvailableHunterCountsByType(colony);
        Map<AntType, Integer> request = new HashMap<>();
        int remaining = maxCount;
        for (AntType type : HUNT_ANT_TYPES) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(remaining, available.getOrDefault(type, 0));
            if (take > 0) {
                request.put(type, take);
                remaining -= take;
            }
        }
        return buildPartyFromCounts(colony, request);
    }

    private static List<Ant> buildPartyFromCounts(Colony colony, Map<AntType, Integer> partyCounts) {
        return ColonyUnassignedAntService.buildPartyFromCounts(colony, partyCounts,
                ColonyUnassignedAntService.resolveEngine(colony));
    }

    private static float computeTravelHours(Colony colony, KnownHuntTarget target, List<Ant> party) {
        ColonyLocationService locations = colony.getLocationService();
        NeoPoint entrance = locations != null ? locations.getColonyEntrance(colony) : null;
        if (entrance == null || target == null || party.isEmpty()) {
            return 1f;
        }
        double dx = target.getOverworldX() - entrance.x;
        double dy = target.getOverworldY() - entrance.y;
        double distance = Math.hypot(dx, dy);
        float avgSpeed = 0f;
        for (Ant ant : party) {
            avgSpeed += Math.max(0.1f, ant.getSpeed());
        }
        avgSpeed /= party.size();
        float hours = (float) (distance / (avgSpeed * GameNumbers.HUNT_TRAVEL_PX_PER_SPEED_HOUR));
        return Math.max(1f, hours);
    }

    private static Point pickBugLocation(Colony colony, Hex currentHex) {
        ColonyLocationService locations = colony.getLocationService();
        if (locations == null) {
            return null;
        }
        NeoPoint entrance = locations.getColonyEntrance(colony);
        if (entrance == null) {
            return null;
        }
        int gameW = colony.getGameAreaWidth();
        int gameH = colony.getGameAreaHeight();
        if (gameW <= 100) {
            gameW = 2560;
        }
        if (gameH <= 100) {
            gameH = 1440;
        }
        int maxDepl = colony.hasUpgrade(GameUnlocks.STAT_HEX_SUSTAIN)
                ? GameNumbers.HEX_SUSTAIN_MAX_DEPLETION_PCT
                : 100;
        int depletionPct = currentHex != null ? currentHex.getResourceDepletionPercentCapped(maxDepl) : 0;
        int depletionExtra = (int) Math.round(
                (depletionPct / 100.0) * GameNumbers.HEX_DEPLETION_SPAWN_BUFFER_EXTRA_MAX);
        depletionExtra = Math.min(depletionExtra, GameNumbers.RESOURCE_SPAWN_BUFFER_EXTRA_CAP);
        int extraMin = GameNumbers.RESOURCE_SPAWN_EXTRA_DISTANCE_MIN + depletionExtra;
        int extraMax = GameNumbers.RESOURCE_SPAWN_EXTRA_DISTANCE_MAX + depletionExtra;
        int extraDistance = extraMin + GameRandom.nextInt(Math.max(1, extraMax - extraMin));
        return ResourceSourcePlacement.pickSpawnCenter(entrance, gameW, gameH, 80, extraDistance);
    }

    public static World resolveWorld(Colony colony) {
        Dynasty dynasty = colony != null ? colony.getDynasty() : null;
        return dynasty != null ? dynasty.getOwningWorld() : null;
    }

    public static List<Savefile.SavedHuntExpedition> toSavedExpeditions(Colony colony) {
        List<Savefile.SavedHuntExpedition> saved = new ArrayList<>();
        if (colony == null) {
            return saved;
        }
        for (HuntExpedition expedition : colony.getActiveHuntExpeditions()) {
            Savefile.SavedHuntExpedition entry = toSavedExpedition(colony, expedition);
            if (entry != null) {
                saved.add(entry);
            }
        }
        return saved;
    }

    private static Savefile.SavedHuntExpedition toSavedExpedition(Colony colony, HuntExpedition expedition) {
        if (expedition == null) {
            return null;
        }
        HuntBattleState battle = HuntCreatureCombatService.getState(colony, expedition.getTargetId());
        float bugHealth = battle != null ? battle.getBugHealth() : 0f;
        int tickIndex = battle != null ? battle.getTickIndex() : 0;
        int focusTargetIndex = battle != null ? battle.getFocusTargetIndex() : 0;
        Savefile.SavedHuntExpedition saved = new Savefile.SavedHuntExpedition(
                expedition.getTargetId(),
                expedition.getPhase().name(),
                expedition.getTravelHoursRemaining(),
                expedition.getTravelHoursTotal(),
                bugHealth,
                tickIndex,
                focusTargetIndex);
        for (Ant ant : expedition.getParty()) {
            if (ant == null || !ant.isAlive()) {
                continue;
            }
            saved.party.add(buildSavedPartyMember(colony, ant, battle));
        }
        if (saved.party.isEmpty()) {
            return null;
        }
        return saved;
    }

    public static void restoreExpeditionsFromSave(Colony colony, Savefile.SavedColony savedColony) {
        if (colony == null || savedColony == null) {
            return;
        }
        HuntCreatureCombatService.clear(colony);
        if (savedColony.activeHuntExpeditions != null && !savedColony.activeHuntExpeditions.isEmpty()) {
            for (Savefile.SavedHuntExpedition savedExpedition : savedColony.activeHuntExpeditions) {
                restoreExpeditionFromSave(colony, savedExpedition);
            }
            return;
        }
        if (savedColony.activeHuntExpedition != null) {
            restoreExpeditionFromSave(colony, savedColony.activeHuntExpedition);
        }
    }

    public static void restoreExpeditionFromSave(Colony colony, Savefile.SavedHuntExpedition savedExpedition) {
        if (colony == null || savedExpedition == null) {
            return;
        }
        HuntCreatureCombatService.clear(colony, savedExpedition.targetId);

        KnownHuntTarget target = findTarget(colony, savedExpedition.targetId);
        if (target == null) {
            return;
        }

        List<Ant> party = resolvePartyFromSave(colony, savedExpedition.party);
        if (party.isEmpty()) {
            return;
        }

        HuntExpedition.Phase phase;
        try {
            phase = HuntExpedition.Phase.valueOf(savedExpedition.phase);
        } catch (IllegalArgumentException e) {
            return;
        }

        HuntExpedition expedition = HuntExpedition.restore(
                savedExpedition.targetId,
                phase,
                party,
                savedExpedition.travelHoursRemaining,
                savedExpedition.travelHoursTotal);
        colony.addHuntExpedition(expedition);

        if (phase != HuntExpedition.Phase.FIGHTING) {
            return;
        }

        List<HuntBattleParticipant> hunters = buildRestoredBattleParticipants(party, savedExpedition.party);
        if (hunters.isEmpty()) {
            colony.removeHuntExpedition(savedExpedition.targetId);
            return;
        }
        HuntCreatureCombatService.restoreBattle(
                colony,
                target,
                hunters,
                savedExpedition.bugHealth,
                savedExpedition.tickIndex,
                savedExpedition.focusTargetIndex);
    }

    private static Savefile.SavedHuntPartyMember buildSavedPartyMember(Colony colony, Ant ant,
            HuntBattleState battle) {
        float battleHealth = -1f;
        float battleMaxHealth = -1f;
        if (battle != null) {
            for (HuntBattleParticipant participant : battle.getHunters()) {
                if (participant.getAnt() == ant) {
                    battleHealth = participant.getBattleHealth();
                    battleMaxHealth = participant.getBattleMaxHealth();
                    break;
                }
            }
        }
        AntRole role = ant.getRole();
        AntType type = ant.getAntType();
        return new Savefile.SavedHuntPartyMember(
                type != null ? type.getId() : 0,
                ant.getSubtypeProfile() != null ? ant.getSubtypeProfile().getCode() : 0,
                role != null ? role.getId() : 0,
                slotIndexForAnt(colony, ant),
                Math.round(ant.getHealth()),
                battleHealth,
                battleMaxHealth);
    }

    private static List<Ant> resolvePartyFromSave(Colony colony, List<Savefile.SavedHuntPartyMember> savedParty) {
        List<Ant> party = new ArrayList<>();
        if (colony == null || savedParty == null || savedParty.isEmpty()) {
            return party;
        }
        Set<Ant> used = new HashSet<>();
        for (Savefile.SavedHuntPartyMember savedMember : savedParty) {
            if (savedMember == null) {
                continue;
            }
            Ant ant = findPartyAnt(colony, savedMember, used);
            if (ant == null) {
                continue;
            }
            if (savedMember.health > 0) {
                ant.setHealth(Math.min(ant.getMaxHealth(), savedMember.health));
            }
            used.add(ant);
            party.add(ant);
        }
        return party;
    }

    private static List<HuntBattleParticipant> buildRestoredBattleParticipants(List<Ant> party,
            List<Savefile.SavedHuntPartyMember> savedParty) {
        List<HuntBattleParticipant> hunters = new ArrayList<>();
        for (int i = 0; i < party.size(); i++) {
            Ant ant = party.get(i);
            Savefile.SavedHuntPartyMember savedMember = i < savedParty.size() ? savedParty.get(i) : null;
            if (savedMember != null && savedMember.battleMaxHealth > 0f && savedMember.battleHealth >= 0f) {
                hunters.add(HuntBattleParticipant.restore(
                        ant, savedMember.battleMaxHealth, savedMember.battleHealth));
            } else {
                hunters.add(new HuntBattleParticipant(ant));
            }
        }
        return hunters;
    }

    private static Ant findPartyAnt(Colony colony, Savefile.SavedHuntPartyMember savedMember, Set<Ant> used) {
        int seen = 0;
        for (List<Ant> source : militaryAntLists(colony)) {
            for (Ant candidate : source) {
                if (candidate == null || used.contains(candidate) || !candidate.isAlive()) {
                    continue;
                }
                if (!matchesSavedPartyMember(candidate, savedMember)) {
                    continue;
                }
                if (seen == savedMember.slotIndex) {
                    return candidate;
                }
                seen++;
            }
        }
        return null;
    }

    private static boolean matchesSavedPartyMember(Ant ant, Savefile.SavedHuntPartyMember savedMember) {
        AntType type = ant.getAntType();
        AntRole role = ant.getRole();
        int typeId = type != null ? type.getId() : 0;
        int roleId = role != null ? role.getId() : 0;
        int subtypeCode = ant.getSubtypeProfile() != null ? ant.getSubtypeProfile().getCode() : 0;
        return typeId == savedMember.typeId
                && roleId == savedMember.roleId
                && subtypeCode == savedMember.subtypeCode;
    }

    private static int slotIndexForAnt(Colony colony, Ant ant) {
        int seen = 0;
        for (List<Ant> source : militaryAntLists(colony)) {
            for (Ant candidate : source) {
                if (candidate == ant) {
                    return seen;
                }
                if (candidate != null && matchesPartyDescriptor(candidate, ant)) {
                    seen++;
                }
            }
        }
        return 0;
    }

    private static boolean matchesPartyDescriptor(Ant candidate, Ant ant) {
        if (candidate == null || ant == null) {
            return false;
        }
        AntType candidateType = candidate.getAntType();
        AntType antType = ant.getAntType();
        AntRole candidateRole = candidate.getRole();
        AntRole antRole = ant.getRole();
        int candidateTypeId = candidateType != null ? candidateType.getId() : 0;
        int antTypeId = antType != null ? antType.getId() : 0;
        int candidateRoleId = candidateRole != null ? candidateRole.getId() : 0;
        int antRoleId = antRole != null ? antRole.getId() : 0;
        int candidateSubtype = candidate.getSubtypeProfile() != null ? candidate.getSubtypeProfile().getCode() : 0;
        int antSubtype = ant.getSubtypeProfile() != null ? ant.getSubtypeProfile().getCode() : 0;
        return candidateTypeId == antTypeId
                && candidateRoleId == antRoleId
                && candidateSubtype == antSubtype;
    }

    private static List<List<Ant>> militaryAntLists(Colony colony) {
        List<List<Ant>> lists = new ArrayList<>(3);
        lists.add(colony.getSoldiers());
        lists.add(colony.getMajors());
        lists.add(colony.getPrincesses());
        return lists;
    }
}
