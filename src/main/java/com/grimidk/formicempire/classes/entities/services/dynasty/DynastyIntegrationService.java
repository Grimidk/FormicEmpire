package com.grimidk.formicempire.classes.entities.services.dynasty;

import com.grimidk.formicempire.classes.constants.misc.Species;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.Tunnel;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyStarterService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DynastyIntegrationService {

    private DynastyIntegrationService() {
    }

    public static boolean meetsMilitaryRequirement(Dynasty overlord, Dynasty target) {
        if (overlord == null || target == null || overlord.isDefeated() || target.isDefeated()) {
            return false;
        }
        int targetPower = target.getMilitaryPower();
        if (targetPower <= 0) {
            return false;
        }
        return overlord.getMilitaryPower() >= targetPower * GameConstants.INTEGRATION_MILITARY_RATIO_REQUIRED;
    }

    public static boolean meetsReputationRequirement(Dynasty overlord, Dynasty target, World world) {
        if (overlord == null || target == null || world == null || overlord.getDiplomacyService() == null) {
            return false;
        }
        return overlord.getDiplomacyService().getEffectiveDiplomaticReputation(target, world)
                >= GameConstants.REPUTATION_FRIENDLY.getMinScore();
    }

    public static boolean canStartIntegration(Dynasty overlord, Dynasty target, World world, TradeManager tradeManager) {
        if (overlord == null || target == null || world == null || overlord == target) {
            return false;
        }
        if (!DynastyDiplomacyService.isDiplomaticallyContactable(overlord)
                || !DynastyDiplomacyService.isDiplomaticallyContactable(target)) {
            return false;
        }
        if (overlord.isDefeated() || target.isDefeated()) {
            return false;
        }
        if (overlord.hasActiveIntegration() || findIntegrationOverlord(world, target.getId()) != null) {
            return false;
        }
        DynastyDiplomacyService diplo = overlord.getDiplomacyService();
        if (diplo == null || diplo.isAtWarWith(target)) {
            return false;
        }
        if (!diplo.hasNonAggressionPact(target)) {
            return false;
        }
        if (!diplo.sharesBorderWith(target, world)) {
            return false;
        }
        if (!meetsMilitaryRequirement(overlord, target)) {
            return false;
        }
        if (!meetsReputationRequirement(overlord, target, world)) {
            return false;
        }
        if (countIntegrationDiplomatCapacity(overlord) < GameConstants.INTEGRATION_MIN_DIPLOMATS) {
            return false;
        }
        return !target.getColonies().isEmpty();
    }

    public static boolean maintainsIntegration(Dynasty overlord, Dynasty target, World world) {
        if (overlord == null || target == null || world == null || overlord.getDiplomacyService() == null) {
            return false;
        }
        if (overlord.isDefeated() || target.isDefeated()) {
            return false;
        }
        if (overlord.getIntegrationTargetDynastyId() != target.getId()) {
            return false;
        }
        DynastyDiplomacyService diplo = overlord.getDiplomacyService();
        if (diplo.isAtWarWith(target) || !diplo.hasNonAggressionPact(target)) {
            return false;
        }
        if (!diplo.sharesBorderWith(target, world)) {
            return false;
        }
        if (!meetsMilitaryRequirement(overlord, target)) {
            return false;
        }
        if (!meetsReputationRequirement(overlord, target, world)) {
            return false;
        }
        if (countIntegrationDiplomats(overlord) < GameConstants.INTEGRATION_MIN_DIPLOMATS) {
            return false;
        }
        return !target.getColonies().isEmpty();
    }

    public static boolean startIntegration(World world, Dynasty overlord, Dynasty target, TradeManager tradeManager) {
        if (!canStartIntegration(overlord, target, world, tradeManager)) {
            return false;
        }
        overlord.setIntegrationTargetDynastyId(target.getId());
        overlord.setIntegrationProgressDays(0);
        overlord.setIntegrationDiplomatsManual(false);
        assignIntegrationDiplomats(overlord, countMaxAssignableIntegrationDiplomats(overlord, world, tradeManager),
                false, world, tradeManager);
        logDynastyEvent(overlord, LanguageStrings.format(
                LanguageStrings.LOG_INTEGRATION_STARTED_FMT, target.getName()));
        return true;
    }

    public static void cancelIntegration(World world, Dynasty overlord, TradeManager tradeManager, boolean playerInitiated) {
        if (overlord == null || !overlord.hasActiveIntegration()) {
            return;
        }
        int targetId = overlord.getIntegrationTargetDynastyId();
        Dynasty target = world != null ? world.findDynastyById(targetId) : null;
        clearIntegrationDiplomatDeployment(overlord);
        overlord.clearIntegration();
        String targetName = target != null ? target.getName() : LanguageStrings.get(LanguageStrings.STAT_UNKNOWN);
        if (playerInitiated) {
            logDynastyEvent(overlord, LanguageStrings.format(
                    LanguageStrings.LOG_INTEGRATION_CANCELLED_PLAYER_FMT, targetName));
        } else {
            logDynastyEvent(overlord, LanguageStrings.format(
                    LanguageStrings.LOG_INTEGRATION_CANCELLED_FMT, targetName));
        }
    }

    public static void tickIntegrationsDaily(World world, TradeManager tradeManager) {
        if (world == null) {
            return;
        }
        for (Dynasty dynasty : new ArrayList<>(world.getDynastys())) {
            if (!dynasty.hasActiveIntegration()) {
                continue;
            }
            Dynasty target = world.findDynastyById(dynasty.getIntegrationTargetDynastyId());
            reconcileIntegrationDiplomatDeployment(dynasty, world, tradeManager);
            if (target == null || !maintainsIntegration(dynasty, target, world)) {
                cancelIntegration(world, dynasty, tradeManager, false);
                continue;
            }
            dynasty.addIntegrationProgressDays(1);
            if (dynasty.getIntegrationProgressDays() >= computeTotalIntegrationDays(dynasty, target)) {
                completeIntegration(world, dynasty, target, tradeManager);
            }
        }
    }

    public static double computeTotalIntegrationMonths(Dynasty overlord, Dynasty target) {
        if (target == null || target.getColonies().isEmpty()) {
            return 0;
        }
        int colonyCount = target.getColonies().size();
        int diplomats = countIntegrationDiplomats(overlord);
        double diplomatsPerColony = diplomats / (double) colonyCount;
        return colonyCount * GameConstants.computeIntegrationMonthsPerColony(diplomatsPerColony);
    }

    public static double computeTotalIntegrationDays(Dynasty overlord, Dynasty target) {
        return computeTotalIntegrationMonths(overlord, target) * GameConstants.DAYS_PER_MONTH;
    }

    public static double getIntegrationProgressPercent(Dynasty overlord, Dynasty target) {
        if (overlord == null || target == null || !overlord.isIntegratingDynasty(target)) {
            return 0;
        }
        double total = computeTotalIntegrationDays(overlord, target);
        if (total <= 0) {
            return 0;
        }
        return Math.min(100.0, (overlord.getIntegrationProgressDays() / total) * 100.0);
    }

    public static int getIntegrationDaysRemaining(Dynasty overlord, Dynasty target) {
        if (overlord == null || target == null || !overlord.isIntegratingDynasty(target)) {
            return 0;
        }
        double total = computeTotalIntegrationDays(overlord, target);
        return (int) Math.ceil(Math.max(0, total - overlord.getIntegrationProgressDays()));
    }

    public static String getIntegrationEstimatedCompletionDate(World world, Dynasty overlord, Dynasty target) {
        if (world == null || overlord == null || target == null || !overlord.isIntegratingDynasty(target)) {
            return "";
        }
        int daysRemaining = getIntegrationDaysRemaining(overlord, target);
        return formatWorldDate(worldDayIndex(world) + daysRemaining);
    }

    public static int worldDayIndex(World world) {
        if (world == null) {
            return 0;
        }
        return (world.getYear() * 12 + (world.getMonth() - 1)) * GameConstants.DAYS_PER_MONTH + (world.getDay() - 1);
    }

    public static String formatWorldDate(int worldDayIndex) {
        int dayOfMonth = (worldDayIndex % GameConstants.DAYS_PER_MONTH) + 1;
        int monthIndex = worldDayIndex / GameConstants.DAYS_PER_MONTH;
        int year = monthIndex / 12;
        int month = (monthIndex % 12) + 1;
        return LanguageStrings.format(
                LanguageStrings.WORLD_DATE_FMT,
                String.format("%02d", dayOfMonth),
                String.format("%02d", month),
                String.format("%04d", year));
    }

    public static Dynasty findIntegrationOverlord(World world, int targetDynastyId) {
        if (world == null || targetDynastyId <= 0) {
            return null;
        }
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.getIntegrationTargetDynastyId() == targetDynastyId) {
                return dynasty;
            }
        }
        return null;
    }

    public static int countTotalAvailableDiplomats(Dynasty dynasty, World world, TradeManager tradeManager) {
        return countIntegrationDiplomatCapacity(dynasty);
    }

    public static int countIntegrationDiplomatCapacity(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : listIntegrationDiplomatColonies(dynasty)) {
            total += integrationDiplomatCapacity(colony);
        }
        return total;
    }

    public static int countIntegrationDiplomats(Dynasty dynasty) {
        if (dynasty == null) {
            return 0;
        }
        int total = 0;
        for (Colony colony : dynasty.getColonies()) {
            total += colony.getIntegrationDiplomatsDeployed();
        }
        return total;
    }

    public static int countMaxAssignableIntegrationDiplomats(Dynasty dynasty, World world, TradeManager tradeManager) {
        return countIntegrationDiplomatCapacity(dynasty);
    }

    public static List<Colony> listIntegrationDiplomatColonies(Dynasty dynasty) {
        List<Colony> colonies = new ArrayList<>();
        if (dynasty == null) {
            return colonies;
        }
        for (Colony colony : dynasty.getColonies()) {
            if (colony.getAge() < 7 || !colony.hasUpgrade(GameUnlocks.ROLE_DIPLOMAT)) {
                continue;
            }
            if (integrationDiplomatCapacity(colony) > 0) {
                colonies.add(colony);
            }
        }
        return colonies;
    }

    public static void assignIntegrationDiplomats(Dynasty overlord, int requestedTotal, boolean manual,
            World world, TradeManager tradeManager) {
        if (overlord == null || world == null || tradeManager == null) {
            return;
        }
        clearIntegrationDiplomatDeployment(overlord);
        int remaining = Math.max(0, Math.min(requestedTotal, countIntegrationDiplomatCapacity(overlord)));
        List<Colony> sources = new ArrayList<>(listIntegrationDiplomatColonies(overlord));
        sources.sort(Comparator.comparingInt(DynastyIntegrationService::integrationDiplomatCapacity).reversed());
        for (Colony colony : sources) {
            if (remaining <= 0) {
                break;
            }
            int capacity = integrationDiplomatCapacity(colony);
            int deploy = Math.min(remaining, capacity);
            colony.setIntegrationDiplomatsDeployed(deploy);
            remaining -= deploy;
        }
        overlord.setIntegrationDiplomatsManual(manual);
    }

    public static void reconcileIntegrationDiplomatDeployment(Dynasty overlord, World world, TradeManager tradeManager) {
        if (overlord == null || !overlord.hasActiveIntegration() || world == null || tradeManager == null) {
            return;
        }
        int current = countIntegrationDiplomats(overlord);
        int max = countMaxAssignableIntegrationDiplomats(overlord, world, tradeManager);
        if (current > max) {
            assignIntegrationDiplomats(overlord, max, overlord.isIntegrationDiplomatsManual(), world, tradeManager);
        } else if (!overlord.isIntegrationDiplomatsManual()) {
            assignIntegrationDiplomats(overlord, max, false, world, tradeManager);
        }
    }

    public static void refreshIntegrationDiplomatDeployment(Dynasty overlord, World world, TradeManager tradeManager) {
        reconcileIntegrationDiplomatDeployment(overlord, world, tradeManager);
    }

    private static int integrationDiplomatCapacity(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int assigned = colony.getAssignedRoleCount(GameConstants.ROLE_DIPLOMAT);
        int otherMissions = colony.getOutgoingColonyDiplomatMissions().values().stream()
                .mapToInt(Integer::intValue).sum()
                + colony.getOutgoingDynastyDiplomatMissions().values().stream()
                .mapToInt(Integer::intValue).sum();
        return Math.max(0, assigned - otherMissions);
    }

    private static void clearIntegrationDiplomatDeployment(Dynasty overlord) {
        if (overlord == null) {
            return;
        }
        for (Colony colony : overlord.getColonies()) {
            colony.setIntegrationDiplomatsDeployed(0);
        }
    }

    private static void completeIntegration(World world, Dynasty overlord, Dynasty target, TradeManager tradeManager) {
        List<Colony> colonies = new ArrayList<>(target.getColonies());
        Species targetSpecies = target.getSpecies();
        clearIntegrationDiplomatDeployment(overlord);
        overlord.clearIntegration();

        int inherited = overlord.inheritAssimilationsFrom(target);

        for (Colony colony : colonies) {
            if (targetSpecies != null) {
                colony.setNativeSpeciesId(targetSpecies.getId());
            }
            target.removeColony(colony);
            colony.setDynasty(overlord);
            colony.setCapital(false);
            ColonyStarterService.shared().inheritIntegratedColonyFromOverlord(overlord, colony);
            colony.setRecentlyIntegratedMonthsRemaining(GameConstants.RECENTLY_INTEGRATED_LOYALTY_MONTHS);
            ColonyMilitaryService.refreshColonyMilitaryPower(colony);
        }

        transferTunnels(target, overlord);
        target.setDefeated(true);
        world.getWarService().endWarsInvolving(target);

        if (inherited > 0 && overlord.getCapital() != null) {
            overlord.getCapital().logEvent(ColonyLogPrefixes.DYNASTY + " "
                    + LanguageStrings.format(LanguageStrings.WAR_INHERITED_ASSIMILATIONS_FMT,
                            target.getName(), String.valueOf(inherited)));
        }
        logDynastyEvent(overlord, LanguageStrings.format(
                LanguageStrings.LOG_INTEGRATION_COMPLETED_FMT, target.getName()));
        if (overlord.isPlayer()) {
            overlord.addPendingIntegrationCompletedAlert(target.getId());
        }
        ColonyMilitaryService.refreshDynastyMilitaryPower(overlord);
        ColonyMilitaryService.refreshDynastyMilitaryPower(target);
        if (overlord.getCapital() == null) {
            overlord.promoteNewCapital();
        }
    }

    private static void transferTunnels(Dynasty from, Dynasty to) {
        if (from == null || to == null) {
            return;
        }
        for (Tunnel tunnel : new ArrayList<>(from.getTunnels())) {
            to.addTunnel(tunnel);
        }
        from.getTunnels().clear();
    }

    public static List<Dynasty> listIntegrationCandidates(Dynasty overlord, World world) {
        List<Dynasty> candidates = new ArrayList<>();
        if (overlord == null || world == null) {
            return candidates;
        }
        for (Dynasty other : world.getDynastys()) {
            if (other != overlord && other.isActiveForDiplomacy()) {
                candidates.add(other);
            }
        }
        DynastyDiplomacyService diplo = overlord.getDiplomacyService();
        if (diplo != null) {
            candidates.sort(Comparator
                    .comparingInt((Dynasty d) -> diplo.sharesBorderWith(d, world) ? 0 : 1)
                    .thenComparingInt(Dynasty::getMilitaryPower));
        }
        return candidates;
    }

    private static void logDynastyEvent(Dynasty dynasty, String message) {
        if (dynasty == null || message == null) {
            return;
        }
        Colony capital = dynasty.getCapital();
        if (capital != null) {
            capital.logEvent(ColonyLogPrefixes.DYNASTY + " " + message);
        }
    }
}
