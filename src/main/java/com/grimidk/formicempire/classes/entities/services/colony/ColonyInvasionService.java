package com.grimidk.formicempire.classes.entities.services.colony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.grimidk.formicempire.classes.constants.critter.Species;
import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.invasion.InvasionAlert;
import com.grimidk.formicempire.classes.entities.invasion.InvasionDefense;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

public final class ColonyInvasionService {

    public static final class InvasionDispatchPreview {
        public final int partySize;
        public final float winChance;
        public final int hoursRemaining;

        InvasionDispatchPreview(int partySize, float winChance, int hoursRemaining) {
            this.partySize = partySize;
            this.winChance = winChance;
            this.hoursRemaining = hoursRemaining;
        }
    }

    private static final List<AntType> INVASION_ANT_TYPES = List.of(
            GameConstants.TYPE_WORKER,
            GameConstants.TYPE_SOLDIER,
            GameConstants.TYPE_MAJOR,
            GameConstants.TYPE_PRINCESS);

    private ColonyInvasionService() {
    }

    public static List<InvasionAlert> getVisibleAlerts(Colony colony) {
        List<InvasionAlert> visible = new ArrayList<>();
        if (colony == null) {
            return visible;
        }
        collectVisibleFromHost(colony, colony, visible);
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null) {
            Colony capital = dynasty.getCapital();
            if (capital != null && capital != colony) {
                collectVisibleFromHost(colony, capital, visible);
            }
        }
        return visible;
    }

    private static void collectVisibleFromHost(Colony viewer, Colony host, List<InvasionAlert> visible) {
        for (InvasionAlert alert : host.getInvasionAlerts()) {
            if (alert == null || alert.isDefenseDispatched()) {
                continue;
            }
            if (alert.getScope() == InvasionAlert.Scope.DYNASTY) {
                visible.add(alert);
                continue;
            }
            if (alert.getTargetColonyId() == viewer.getId()) {
                visible.add(alert);
            }
        }
    }

    public static Map<AntType, Integer> getAvailableDefenderCountsByType(Colony colony) {
        return ColonyUnassignedAntService.countUnassignedByTypes(colony, INVASION_ANT_TYPES,
                ColonyUnassignedAntService.resolveEngine(colony));
    }

    public static int countAvailableDefenders(Colony colony) {
        Map<AntType, Integer> counts = getAvailableDefenderCountsByType(colony);
        int total = 0;
        for (Integer count : counts.values()) {
            total += count != null ? count : 0;
        }
        return total;
    }

    public static InvasionDispatchPreview previewDefense(Colony colony, InvasionAlert alert,
            Map<AntType, Integer> partyCounts, int worldDay, int worldHour) {
        List<Ant> party = buildPartyFromCounts(colony, partyCounts);
        Species species = alert != null ? alert.getSpecies() : null;
        if (species == null) {
            species = GameConstants.TYPE_ANT_LION;
        }
        float winChance = ColonyHuntService.estimateWinChance(species, party);
        int hoursRemaining = alert != null ? alert.hoursRemaining(worldDay, worldHour) : 0;
        return new InvasionDispatchPreview(party.size(), winChance, hoursRemaining);
    }

    public static boolean canDispatchDefense(Colony colony, InvasionAlert alert) {
        if (colony == null || alert == null || alert.isDefenseDispatched()) {
            return false;
        }
        if (alert.getTargetColonyId() != colony.getId()) {
            return false;
        }
        if (colony.getInvasionDefenseForAlert(alert.getId()) != null) {
            return false;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null && dynasty.isAtWar()) {
            return false;
        }
        return countAvailableDefenders(colony) > 0;
    }

    public static boolean dispatchDefense(Colony colony, int alertId, Map<AntType, Integer> partyCounts,
            int worldDay, int worldHour) {
        InvasionAlert alert = findAlert(colony, alertId);
        if (alert == null || !canDispatchDefense(colony, alert)) {
            return false;
        }
        List<Ant> party = buildPartyFromCounts(colony, partyCounts);
        if (party.isEmpty()) {
            return false;
        }
        Map<AntType, Integer> available = getAvailableDefenderCountsByType(colony);
        for (Map.Entry<AntType, Integer> entry : partyCounts.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            if (entry.getValue() > available.getOrDefault(entry.getKey(), 0)) {
                return false;
            }
        }
        Species species = alert.getSpecies();
        if (species == null) {
            species = GameConstants.TYPE_ANT_LION;
        }
        String bugName = species.getName();
        alert.setDefenseDispatched(true);
        colony.addInvasionDefense(new InvasionDefense(alertId, party));
        HuntCreatureCombatService.startInvasionBattle(colony, alert, party);
        colony.logEvent(ColonyLogPrefixes.INVASION + " "
                + LanguageStrings.format(LanguageStrings.LOG_INVASION_COMBAT_STARTED_FMT, party.size(), bugName));
        return true;
    }

    public static void tickInvasions(Colony colony, int worldDay, int worldHour) {
        if (colony == null) {
            return;
        }
        for (InvasionDefense defense : List.copyOf(colony.getActiveInvasionDefenses())) {
            tickOneDefense(colony, defense);
        }
        Iterator<InvasionAlert> it = colony.getInvasionAlerts().iterator();
        while (it.hasNext()) {
            InvasionAlert alert = it.next();
            if (alert == null || alert.isDefenseDispatched()) {
                continue;
            }
            if (alert.getTargetColonyId() != colony.getId()) {
                continue;
            }
            if (!alert.isExpired(worldDay, worldHour)) {
                continue;
            }
            Species species = alert.getSpecies();
            String bugName = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_ANT_LION);
            colony.logEvent(ColonyLogPrefixes.INVASION + " "
                    + LanguageStrings.format(LanguageStrings.LOG_INVASION_RAID_FMT, bugName));
            applyRaidDamage(colony, species, false);
            it.remove();
        }
        trySpawnRandomAlert(colony, worldDay, worldHour);
    }

    private static void tickOneDefense(Colony colony, InvasionDefense defense) {
        if (colony == null || defense == null) {
            return;
        }
        InvasionAlert alert = findAlert(colony, defense.getAlertId());
        if (alert == null) {
            cancelDefense(colony, defense.getAlertId());
            return;
        }
        HuntCreatureCombatService.TickOutcome outcome =
                HuntCreatureCombatService.tickInvasion(colony, defense.getAlertId());
        if (outcome == HuntCreatureCombatService.TickOutcome.HUNTERS_WIN) {
            resolveDefenseWin(colony, alert, defense);
        } else if (outcome == HuntCreatureCombatService.TickOutcome.BUG_WINS) {
            resolveDefenseLoss(colony, alert, defense);
        }
    }

    private static void resolveDefenseWin(Colony colony, InvasionAlert alert, InvasionDefense defense) {
        Species species = alert.getSpecies();
        if (species == null) {
            species = GameConstants.TYPE_ANT_LION;
        }
        String bugName = species.getName();
        colony.logEvent(ColonyLogPrefixes.INVASION_SUCCESS + " "
                + LanguageStrings.format(LanguageStrings.LOG_INVASION_DEFENDED_FMT, defense.getParty().size(), bugName));
        removeAlert(colony, alert.getId());
        cancelDefense(colony, alert.getId());
    }

    private static void resolveDefenseLoss(Colony colony, InvasionAlert alert, InvasionDefense defense) {
        Species species = alert.getSpecies();
        if (species == null) {
            species = GameConstants.TYPE_ANT_LION;
        }
        String bugName = species.getName();
        colony.logEvent(ColonyLogPrefixes.INVASION_FAILURE + " "
                + LanguageStrings.format(LanguageStrings.LOG_INVASION_DEFENSE_LOST_FMT, defense.getParty().size(), bugName));
        removeAlert(colony, alert.getId());
        cancelDefense(colony, alert.getId());
        applyRaidDamage(colony, species, true);
    }

    private static void cancelDefense(Colony colony, int alertId) {
        HuntCreatureCombatService.clearInvasion(colony, alertId);
        colony.removeInvasionDefense(alertId);
    }

    private static void trySpawnRandomAlert(Colony colony, int worldDay, int worldHour) {
        if (colony == null || !colony.isPlayer() || !colony.runsFullSimulation()) {
            return;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || dynasty.isAtWar()) {
            return;
        }
        if (hasPendingAlert(colony)) {
            return;
        }
        if (GameRandom.nextFloat() >= GameNumbers.INVASION_RANDOM_CHANCE_PER_HOUR) {
            return;
        }
        InvasionAlert.Scope scope = GameRandom.nextBoolean()
                ? InvasionAlert.Scope.COLONY
                : InvasionAlert.Scope.DYNASTY;
        if (scope == InvasionAlert.Scope.DYNASTY && !colony.isCapital()) {
            return;
        }
        int targetColonyId = colony.getId();
        int deadline = InvasionAlert.toAbsoluteHour(worldDay, worldHour) + GameNumbers.INVASION_RESPONSE_HOURS;
        InvasionAlert alert = new InvasionAlert(
                colony.nextInvasionAlertId(),
                GameConstants.TYPE_ANT_LION.getId(),
                scope,
                targetColonyId,
                deadline);
        colony.getInvasionAlerts().add(alert);
        String scopeLabel = scope == InvasionAlert.Scope.DYNASTY
                ? LanguageStrings.get(LanguageStrings.INVASION_SCOPE_DYNASTY)
                : LanguageStrings.get(LanguageStrings.INVASION_SCOPE_COLONY);
        colony.logEvent(ColonyLogPrefixes.INVASION + " "
                + LanguageStrings.format(LanguageStrings.LOG_INVASION_ALERT_FMT,
                        GameConstants.TYPE_ANT_LION.getName(),
                        scopeLabel,
                        GameNumbers.INVASION_RESPONSE_HOURS));
    }

    private static boolean hasPendingAlert(Colony colony) {
        for (InvasionAlert alert : colony.getInvasionAlerts()) {
            if (alert != null && !alert.isDefenseDispatched()) {
                return true;
            }
        }
        return false;
    }

    private static void applyRaidDamage(Colony colony, Species species, boolean afterFailedDefense) {
        if (colony == null) {
            return;
        }
        stealResources(colony);
        killUnassignedRaidVictims(colony);
        if (afterFailedDefense) {
            return;
        }
        String bugName = species != null ? species.getName() : LanguageStrings.get(LanguageStrings.BUG_ANT_LION);
        colony.logEvent(ColonyLogPrefixes.INVASION + " "
                + LanguageStrings.format(LanguageStrings.LOG_INVASION_RAID_DAMAGE_FMT, bugName));
    }

    private static void stealResources(Colony colony) {
        float pct = GameNumbers.INVASION_RAID_RESOURCE_STEAL_PCT;
        colony.setMushrooms(colony.getMushroomsPrecise() * (1f - pct));
        colony.setProtein(colony.getProteinPrecise() * (1f - pct));
        colony.setResins(colony.getResinsPrecise() * (1f - pct));
        colony.setMinerals(colony.getMineralsPrecise() * (1f - pct));
        colony.setSyrups(colony.getSyrupsPrecise() * (1f - pct));
        int rpLoss = Math.max(1, (int) (colony.getResearchPoints() * pct));
        colony.addResearchPoints(-rpLoss);
    }

    private static void killUnassignedRaidVictims(Colony colony) {
        Engine engine = ColonyUnassignedAntService.resolveEngine(colony);
        List<Ant> candidates = new ArrayList<>();
        for (AntType type : INVASION_ANT_TYPES) {
            List<Ant> ants = colony.getAntsByType(type);
            if (ants == null) {
                continue;
            }
            for (Ant ant : ants) {
                if (ColonyUnassignedAntService.isUnassigned(ant, colony, engine)) {
                    candidates.add(ant);
                }
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        int killCount = Math.max(1, Math.round(candidates.size() * GameNumbers.INVASION_RAID_UNASSIGNED_KILL_PCT));
        killCount = Math.min(killCount, candidates.size());
        for (int i = 0; i < killCount; i++) {
            int index = GameRandom.nextInt(candidates.size());
            Ant victim = candidates.remove(index);
            if (victim == null || !victim.isAlive()) {
                continue;
            }
            AntType type = victim.getAntType();
            victim.goDie(colony, DeathCause.CONFLICT);
            colony.recordAntDeath(victim, DeathCause.CONFLICT);
            List<Ant> list = colony.getAntsByType(type);
            if (list != null) {
                list.remove(victim);
            }
        }
    }

    private static List<Ant> buildPartyFromCounts(Colony colony, Map<AntType, Integer> partyCounts) {
        return ColonyUnassignedAntService.buildPartyFromCounts(colony, partyCounts,
                ColonyUnassignedAntService.resolveEngine(colony));
    }

    private static InvasionAlert findAlert(Colony colony, int alertId) {
        if (colony == null) {
            return null;
        }
        for (InvasionAlert alert : colony.getInvasionAlerts()) {
            if (alert != null && alert.getId() == alertId) {
                return alert;
            }
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null) {
            Colony capital = dynasty.getCapital();
            if (capital != null && capital != colony) {
                for (InvasionAlert alert : capital.getInvasionAlerts()) {
                    if (alert != null && alert.getId() == alertId) {
                        return alert;
                    }
                }
            }
        }
        return null;
    }

    private static void removeAlert(Colony colony, int alertId) {
        if (colony == null) {
            return;
        }
        colony.getInvasionAlerts().removeIf(alert -> alert != null && alert.getId() == alertId);
        Dynasty dynasty = colony.getDynasty();
        if (dynasty != null) {
            Colony capital = dynasty.getCapital();
            if (capital != null && capital != colony) {
                capital.getInvasionAlerts().removeIf(alert -> alert != null && alert.getId() == alertId);
            }
        }
    }

    public static List<Savefile.SavedInvasionAlert> toSavedAlerts(Colony colony) {
        List<Savefile.SavedInvasionAlert> saved = new ArrayList<>();
        if (colony == null) {
            return saved;
        }
        for (InvasionAlert alert : colony.getInvasionAlerts()) {
            if (alert == null) {
                continue;
            }
            saved.add(new Savefile.SavedInvasionAlert(
                    alert.getId(),
                    alert.getSpeciesId(),
                    alert.getScope().name(),
                    alert.getTargetColonyId(),
                    alert.getDeadlineAbsoluteHour(),
                    alert.isDefenseDispatched()));
        }
        return saved;
    }

    public static void restoreAlertsFromSave(Colony colony, List<Savefile.SavedInvasionAlert> savedAlerts,
            int nextAlertIdSeed) {
        if (colony == null) {
            return;
        }
        colony.getInvasionAlerts().clear();
        colony.setNextInvasionAlertIdSeed(nextAlertIdSeed);
        if (savedAlerts == null) {
            return;
        }
        for (Savefile.SavedInvasionAlert saved : savedAlerts) {
            if (saved == null) {
                continue;
            }
            InvasionAlert.Scope scope;
            try {
                scope = InvasionAlert.Scope.valueOf(saved.scope);
            } catch (IllegalArgumentException ex) {
                scope = InvasionAlert.Scope.COLONY;
            }
            InvasionAlert alert = new InvasionAlert(
                    saved.id,
                    saved.speciesId,
                    scope,
                    saved.targetColonyId,
                    saved.deadlineAbsoluteHour);
            alert.setDefenseDispatched(saved.defenseDispatched);
            colony.getInvasionAlerts().add(alert);
        }
    }

    public static List<Savefile.SavedInvasionDefense> toSavedDefenses(Colony colony) {
        List<Savefile.SavedInvasionDefense> saved = new ArrayList<>();
        if (colony == null) {
            return saved;
        }
        for (InvasionDefense defense : colony.getActiveInvasionDefenses()) {
            Savefile.SavedInvasionDefense entry = toSavedDefense(colony, defense);
            if (entry != null) {
                saved.add(entry);
            }
        }
        return saved;
    }

    private static Savefile.SavedInvasionDefense toSavedDefense(Colony colony, InvasionDefense defense) {
        if (colony == null || defense == null) {
            return null;
        }
        HuntBattleState battle = HuntCreatureCombatService.getInvasionState(colony, defense.getAlertId());
        float bugHealth = battle != null ? battle.getBugHealth() : 0f;
        int tickIndex = battle != null ? battle.getTickIndex() : 0;
        int focusTargetIndex = battle != null ? battle.getFocusTargetIndex() : 0;
        Savefile.SavedInvasionDefense saved = new Savefile.SavedInvasionDefense(
                defense.getAlertId(), bugHealth, tickIndex, focusTargetIndex);
        for (Ant ant : defense.getParty()) {
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

    public static void restoreDefensesFromSave(Colony colony, Savefile.SavedColony savedColony) {
        if (colony == null || savedColony == null) {
            return;
        }
        colony.clearInvasionDefenses();
        if (savedColony.activeInvasionDefenses == null || savedColony.activeInvasionDefenses.isEmpty()) {
            return;
        }
        for (Savefile.SavedInvasionDefense savedDefense : savedColony.activeInvasionDefenses) {
            restoreDefenseFromSave(colony, savedDefense);
        }
    }

    private static void restoreDefenseFromSave(Colony colony, Savefile.SavedInvasionDefense savedDefense) {
        if (colony == null || savedDefense == null) {
            return;
        }
        HuntCreatureCombatService.clearInvasion(colony, savedDefense.alertId);
        InvasionAlert alert = findAlert(colony, savedDefense.alertId);
        if (alert == null) {
            return;
        }
        List<Ant> party = resolvePartyFromSave(colony, savedDefense.party);
        if (party.isEmpty()) {
            return;
        }
        alert.setDefenseDispatched(true);
        colony.addInvasionDefense(InvasionDefense.restore(savedDefense.alertId, party));
        List<HuntBattleParticipant> defenders = buildRestoredBattleParticipants(party, savedDefense.party);
        if (defenders.isEmpty()) {
            colony.removeInvasionDefense(savedDefense.alertId);
            return;
        }
        HuntCreatureCombatService.restoreInvasionBattle(
                colony,
                alert,
                defenders,
                savedDefense.bugHealth,
                savedDefense.tickIndex,
                savedDefense.focusTargetIndex);
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
        List<HuntBattleParticipant> defenders = new ArrayList<>();
        for (int i = 0; i < party.size(); i++) {
            Ant ant = party.get(i);
            Savefile.SavedHuntPartyMember savedMember = i < savedParty.size() ? savedParty.get(i) : null;
            if (savedMember != null && savedMember.battleMaxHealth > 0f && savedMember.battleHealth >= 0f) {
                defenders.add(HuntBattleParticipant.restore(
                        ant, savedMember.battleMaxHealth, savedMember.battleHealth));
            } else {
                defenders.add(new HuntBattleParticipant(ant));
            }
        }
        return defenders;
    }

    private static Ant findPartyAnt(Colony colony, Savefile.SavedHuntPartyMember savedMember, Set<Ant> used) {
        int seen = 0;
        for (List<Ant> source : invasionAntLists(colony)) {
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
        for (List<Ant> source : invasionAntLists(colony)) {
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

    private static List<List<Ant>> invasionAntLists(Colony colony) {
        List<List<Ant>> lists = new ArrayList<>(4);
        lists.add(colony.getWorkers());
        lists.add(colony.getSoldiers());
        lists.add(colony.getMajors());
        lists.add(colony.getPrincesses());
        return lists;
    }
}
