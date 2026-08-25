package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.dynasty.IntelFact;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynastyIntelligenceService {

    private final Dynasty dynasty;

    public DynastyIntelligenceService(Dynasty dynasty) {
        this.dynasty = dynasty;
    }

    public int getSpyPowerPerAnt() {
        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_3)) {
            return GameNumbers.SPY_POWER_PRESSURE_3;
        }
        if (dynasty.hasUpgrade(GameUnlocks.ABILITY_DIPLOMAT_PRESSURE_2)) {
            return GameNumbers.SPY_POWER_PRESSURE_2;
        }
        return GameNumbers.SPY_POWER_BASE;
    }

    public int getMaxSpiesPerTarget() {
        return GameNumbers.SPY_MAX_PER_DYNASTY_MISSION;
    }

    public boolean hasSpyRole() {
        return dynasty.hasUpgrade(GameUnlocks.ROLE_SPY);
    }

    public boolean hasDiplomacyMenuAccess() {
        return dynasty.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT) || hasSpyRole();
    }

    public boolean canSee(Dynasty target, IntelFact fact) {
        if (fact == null || target == null || target == dynasty) {
            return true;
        }
        return fact.isUnlockedBy(dynasty.getIntelligenceToward(target.getId()));
    }

    public String formatOrRedact(Dynasty target, IntelFact fact, String visibleValue) {
        if (canSee(target, fact)) {
            return visibleValue != null ? visibleValue : "";
        }
        return LanguageStrings.get(LanguageStrings.INTEL_REDACTED);
    }

    public boolean colonyHasSpyRole(Colony colony) {
        return colony != null && colony.hasUpgrade(GameUnlocks.ROLE_SPY);
    }

    public int countAvailableSpies(Colony colony) {
        if (colony == null || !colonyHasSpyRole(colony)) {
            return 0;
        }
        return Math.max(0, colony.getAssignedRoleCount(GameConstants.ROLE_SPY) - colony.getDeployedSpyCount());
    }

    public int countDynastyWideAvailableSpies() {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += countAvailableSpies(colony);
        }
        return total;
    }

    public int countHomeSpies() {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            if (!colonyHasSpyRole(colony)) {
                continue;
            }
            int assigned = colony.getAssignedRoleCount(GameConstants.ROLE_SPY);
            int deployed = colony.getDeployedSpyCount();
            total += Math.max(0, assigned - deployed);
        }
        return total;
    }

    public int countAssignedSpies() {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            if (!colonyHasSpyRole(colony)) {
                continue;
            }
            total += colony.getAssignedRoleCount(GameConstants.ROLE_SPY);
        }
        return total;
    }

    public int countDeployedSpies() {
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += colony.getDeployedSpyCount();
        }
        return total;
    }

    public double computeCounterIntelligence() {
        return Math.min(
                GameNumbers.COUNTER_INTELLIGENCE_MAX,
                GameNumbers.COUNTER_INTELLIGENCE_START + countHomeSpies());
    }

    public int maxSpiesAbroadWhileKeepingCiParity() {
        return countAssignedSpies() / 2;
    }

    public int countSpiesToward(Dynasty other) {
        if (other == null) {
            return 0;
        }
        int deployed = 0;
        for (Colony colony : dynasty.getColonies()) {
            deployed += colony.getOutgoingDynastySpyMissions().getOrDefault(other.getId(), 0);
        }
        return deployed;
    }

    public int computeMaxSpiesToward(Dynasty other) {
        if (other == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += maxSpyMissionFromColony(colony);
        }
        return Math.min(getMaxSpiesPerTarget(), total);
    }

    private int maxSpyMissionFromColony(Colony colony) {
        if (colony == null || !colonyHasSpyRole(colony)) {
            return 0;
        }
        return colony.getAssignedRoleCount(GameConstants.ROLE_SPY);
    }

    public boolean canManageSpiesToward(Dynasty other, World world) {
        if (other == null || other == dynasty || world == null || !hasSpyRole()) {
            return false;
        }
        if (countSpiesToward(other) > 0) {
            return true;
        }
        return countDynastyWideAvailableSpies() > 0;
    }

    public void assignSpiesToward(Dynasty other, int targetTotal, World world) {
        if (other == null || other == dynasty || world == null || !hasSpyRole()) {
            return;
        }
        int capped = Math.max(0, Math.min(targetTotal, getMaxSpiesPerTarget()));
        capped = Math.min(capped, computeMaxSpiesToward(other));
        clearSpyMissionToward(other.getId());
        int remaining = capped;
        List<Colony> sources = listSpyDeploySources();
        sources.sort(Comparator.comparingInt(this::maxSpyMissionFromColony).reversed());
        for (Colony colony : sources) {
            if (remaining <= 0) {
                break;
            }
            int room = Math.min(remaining, countAvailableSpies(colony));
            if (room <= 0) {
                continue;
            }
            deploySpyMission(colony, other, room);
            remaining -= room;
        }
    }

    public void reconcileSpyDeployments(Colony source, int newAssigned) {
        if (source == null || source.getDynasty() != dynasty) {
            return;
        }
        int deployed = source.getDeployedSpyCount();
        if (deployed <= newAssigned) {
            return;
        }
        recallSpies(source, deployed - newAssigned);
    }

    public void reconcileSpyDeploymentsAfterCasualty(Colony source) {
        if (source == null || source.getDynasty() != dynasty) {
            return;
        }
        if (!colonyHasSpyRole(source) && source.getDeployedSpyCount() == 0) {
            return;
        }
        int liveSpies = countLiveSpies(source);
        int assigned = source.getAssignedRoleCount(GameConstants.ROLE_SPY);
        int maxSupport = Math.max(0, Math.min(assigned, liveSpies));
        int deployed = source.getDeployedSpyCount();
        if (deployed > maxSupport) {
            recallSpies(source, deployed - maxSupport);
        }
    }

    public void validateAllSpyDeployments() {
        for (Colony colony : dynasty.getColonies()) {
            reconcileSpyDeploymentsAfterCasualty(colony);
        }
    }

    public void runDailyIntelligence(World world) {
        if (dynasty.isDefeated() || world == null) {
            return;
        }
        if (!hasSpyRole()) {
            return;
        }
        int powerPer = getSpyPowerPerAnt();
        for (Dynasty other : world.getDynastys()) {
            if (other == null || other == dynasty || other.isDefeated()) {
                continue;
            }
            int spies = countSpiesToward(other);
            if (spies <= 0) {
                continue;
            }
            double raw = (spies * powerPer) / GameNumbers.SPY_INTEL_POWER_DIVISOR;
            double reduction = other.getCounterIntelligence() * GameNumbers.SPY_CI_REDUCTION_PER_POINT;
            double gain = raw - reduction;
            if (gain > 0) {
                dynasty.addIntelligenceToward(other.getId(), gain);
            }
            if (IntelFact.THEFT.isUnlockedBy(dynasty.getIntelligenceToward(other.getId())) && spies > 0) {
                performDailyTheft(other);
            }
        }
    }

    public void runMonthlyCounterIntelligence(World world) {
        if (dynasty.isDefeated() || world == null) {
            return;
        }
        double catchChancePercent = dynasty.getCounterIntelligence();
        for (Dynasty attacker : world.getDynastys()) {
            if (attacker == null || attacker == dynasty || attacker.isDefeated()) {
                continue;
            }
            DynastyIntelligenceService attackerIntel = attacker.getIntelligenceService();
            if (attackerIntel == null) {
                continue;
            }
            int spies = attackerIntel.countSpiesToward(dynasty);
            if (spies <= 0) {
                continue;
            }
            if (GameRandom.nextDouble() * 100.0 < catchChancePercent) {
                catchSpiesFrom(attacker);
            }
        }
    }

    private void performDailyTheft(Dynasty victim) {
        if (victim == null || victim == dynasty) {
            return;
        }
        int roll = GameRandom.nextInt(4);
        double fraction = GameNumbers.SPY_THEFT_FRACTION_MIN
                + GameRandom.nextDouble()
                        * (GameNumbers.SPY_THEFT_FRACTION_MAX - GameNumbers.SPY_THEFT_FRACTION_MIN);
        Colony logColony = dynasty.getCapital();
        if (roll == 0) {
            int stock = victim.getResearchPoints();
            int stolen = (int) Math.floor(stock * fraction);
            if (stolen <= 0) {
                return;
            }
            victim.addResearchPoints(-stolen);
            dynasty.addResearchPoints(stolen);
            if (logColony != null) {
                logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                        + LanguageStrings.format(LanguageStrings.LOG_SPY_THEFT_RP_FMT,
                                victim.getName(), LanguageStrings.formatNumber(stolen)));
            }
            return;
        }
        ResourceType type = switch (roll) {
            case 1 -> GameConstants.RESOURCE_RESIN;
            case 2 -> GameConstants.RESOURCE_ROCK;
            default -> GameConstants.RESOURCE_SYRUP;
        };
        int stolen = stealResourceFromDynasty(victim, type, fraction);
        if (stolen <= 0) {
            return;
        }
        depositResource(dynasty, type, stolen);
        if (logColony != null) {
            logColony.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_SPY_THEFT_RESOURCE_FMT,
                            victim.getName(), type.getName(), LanguageStrings.formatNumber(stolen)));
        }
    }

    private int stealResourceFromDynasty(Dynasty victim, ResourceType type, double fraction) {
        List<Colony> colonies = new ArrayList<>(victim.getColonies());
        int total = 0;
        for (Colony colony : colonies) {
            total += getResourceAmount(colony, type);
        }
        int toSteal = (int) Math.floor(total * fraction);
        if (toSteal <= 0) {
            return 0;
        }
        int remaining = toSteal;
        colonies.sort(Comparator.comparingInt((Colony c) -> getResourceAmount(c, type)).reversed());
        for (Colony colony : colonies) {
            if (remaining <= 0) {
                break;
            }
            int have = getResourceAmount(colony, type);
            int take = Math.min(have, remaining);
            if (take <= 0) {
                continue;
            }
            setResourceAmount(colony, type, have - take);
            remaining -= take;
        }
        return toSteal - remaining;
    }

    private void depositResource(Dynasty owner, ResourceType type, int amount) {
        if (amount <= 0) {
            return;
        }
        Colony capital = owner.getCapital();
        if (capital == null && !owner.getColonies().isEmpty()) {
            capital = owner.getColonies().get(0);
        }
        if (capital == null) {
            return;
        }
        setResourceAmount(capital, type, getResourceAmount(capital, type) + amount);
    }

    private static int getResourceAmount(Colony colony, ResourceType type) {
        if (colony == null || type == null) {
            return 0;
        }
        if (type == GameConstants.RESOURCE_RESIN) {
            return colony.getResins();
        }
        if (type == GameConstants.RESOURCE_ROCK) {
            return colony.getMinerals();
        }
        if (type == GameConstants.RESOURCE_SYRUP) {
            return colony.getSyrups();
        }
        return 0;
    }

    private static void setResourceAmount(Colony colony, ResourceType type, int amount) {
        if (colony == null || type == null) {
            return;
        }
        double value = Math.max(0, amount);
        if (type == GameConstants.RESOURCE_RESIN) {
            colony.setResins(value);
        } else if (type == GameConstants.RESOURCE_ROCK) {
            colony.setMinerals(value);
        } else if (type == GameConstants.RESOURCE_SYRUP) {
            colony.setSyrups(value);
        }
    }

    void catchSpiesFrom(Dynasty attacker) {
        if (attacker == null || attacker == dynasty) {
            return;
        }
        DynastyIntelligenceService attackerIntel = attacker.getIntelligenceService();
        if (attackerIntel == null) {
            return;
        }
        int killed = 0;
        for (Colony colony : new ArrayList<>(attacker.getColonies())) {
            int n = colony.getOutgoingDynastySpyMissions().getOrDefault(dynasty.getId(), 0);
            if (n <= 0) {
                continue;
            }
            attackerIntel.undeploySpyMission(colony, dynasty.getId(), n);
            killed += attackerIntel.killSpiesInColony(colony, n);
        }
        double intel = attacker.getIntelligenceToward(dynasty.getId());
        attacker.setIntelligenceToward(dynasty.getId(), intel * 0.5);

        dynasty.adjustDiplomaticReputation(
                attacker.getId(), GameConstants.DIPLO_MODIFIER_CAUGHT_SPYING.getReputationDelta());
        dynasty.addDiplomaticModifierKey(
                attacker.getId(), GameConstants.DIPLO_MODIFIER_CAUGHT_SPYING.getNameKey());

        Colony logVictim = dynasty.getCapital();
        if (logVictim != null && killed > 0) {
            logVictim.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_SPY_CAUGHT_FMT,
                            attacker.getName(), LanguageStrings.formatNumber(killed)));
        }
        Colony logAttacker = attacker.getCapital();
        if (logAttacker != null && killed > 0) {
            logAttacker.logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.LOG_SPY_CAUGHT_OWN_FMT,
                            dynasty.getName(), LanguageStrings.formatNumber(killed)));
        }
    }

    private int killSpiesInColony(Colony colony, int count) {
        if (colony == null || count <= 0) {
            return 0;
        }
        List<Ant> spies = new ArrayList<>();
        for (Ant ant : colony.getPrincesses()) {
            if (ant.isAlive() && ant.getRole() == GameConstants.ROLE_SPY) {
                spies.add(ant);
            }
        }
        int toKill = Math.min(count, spies.size());
        for (int i = 0; i < toKill; i++) {
            Ant ant = spies.get(i);
            var formerType = ant.getAntType();
            var formerRole = ant.getRole();
            boolean onTrade = ant.isOnTrade();
            ant.goDie(colony, DeathCause.CONFLICT);
            colony.getPrincesses().remove(ant);
            colony.recordAntDeath(ant, DeathCause.CONFLICT);
            colony.handleAntCasualtyAftermath(ant, formerType, formerRole, onTrade);
        }
        int assigned = colony.getAssignedRoleCount(GameConstants.ROLE_SPY);
        if (assigned > 0) {
            colony.setAssignedRoleCount(GameConstants.ROLE_SPY, Math.max(0, assigned - toKill));
        }
        return toKill;
    }

    private List<Colony> listSpyDeploySources() {
        List<Colony> sources = new ArrayList<>();
        for (Colony colony : dynasty.getColonies()) {
            if (colonyHasSpyRole(colony)) {
                sources.add(colony);
            }
        }
        return sources;
    }

    private void clearSpyMissionToward(int targetDynastyId) {
        for (Colony colony : dynasty.getColonies()) {
            int current = colony.getOutgoingDynastySpyMissions().getOrDefault(targetDynastyId, 0);
            if (current > 0) {
                undeploySpyMission(colony, targetDynastyId, current);
            }
        }
    }

    private void deploySpyMission(Colony from, Dynasty targetDynasty, int count) {
        from.getOutgoingDynastySpyMissions().merge(targetDynasty.getId(), count, Integer::sum);
        dynasty.addSpySupportTo(targetDynasty.getId(), count);
    }

    private void undeploySpyMission(Colony from, int targetDynastyId, int count) {
        int current = from.getOutgoingDynastySpyMissions().getOrDefault(targetDynastyId, 0);
        int toRemove = Math.min(count, current);
        if (toRemove <= 0) {
            return;
        }
        int next = current - toRemove;
        if (next == 0) {
            from.getOutgoingDynastySpyMissions().remove(targetDynastyId);
        } else {
            from.getOutgoingDynastySpyMissions().put(targetDynastyId, next);
        }
        dynasty.removeSpySupportTo(targetDynastyId, toRemove);
    }

    private void recallSpies(Colony source, int count) {
        int remaining = count;
        while (remaining > 0) {
            Map.Entry<Integer, Integer> largest = null;
            for (Map.Entry<Integer, Integer> entry : source.getOutgoingDynastySpyMissions().entrySet()) {
                if (entry.getValue() <= 0) {
                    continue;
                }
                if (largest == null || entry.getValue() > largest.getValue()) {
                    largest = entry;
                }
            }
            if (largest == null) {
                break;
            }
            undeploySpyMission(source, largest.getKey(), 1);
            remaining--;
        }
    }

    private int countLiveSpies(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int count = 0;
        for (Ant ant : colony.getPrincesses()) {
            if (ant.isAlive() && ant.getRole() == GameConstants.ROLE_SPY) {
                count++;
            }
        }
        return count;
    }

    public Map<Integer, Integer> copySpySupportSnapshot() {
        return new HashMap<>(dynasty.copySpySupportToDynasty());
    }
}
