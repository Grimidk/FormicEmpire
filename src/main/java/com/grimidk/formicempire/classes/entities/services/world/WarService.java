package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.entities.Dynasty;
import com.grimidk.formicempire.classes.entities.Hex;
import com.grimidk.formicempire.classes.entities.War;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyLabourService;
import com.grimidk.formicempire.classes.entities.services.colony.ColonyMilitaryService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyDiplomacyService;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyRebellionService;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.util.GameRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WarService {

    private final World world;
    private final List<War> activeWars = new ArrayList<>();
    private final List<War> historicWars = new ArrayList<>();
    private int nextWarId = 1;

    public WarService(World world) {
        this.world = world;
    }

    public War beginWar(Dynasty declarer, Dynasty target) {
        if (declarer == null || target == null || declarer == target
                || declarer.isDefeated() || target.isDefeated()) {
            return null;
        }
        int[] pair = War.canonicalPair(declarer.getId(), target.getId());
        War existing = findActiveWar(pair[0], pair[1]);
        if (existing != null) {
            return existing;
        }
        Dynasty dynastyA = world.findDynastyById(pair[0]);
        Dynasty dynastyB = world.findDynastyById(pair[1]);
        if (dynastyA == null || dynastyB == null) {
            return null;
        }
        War war = new War(
                nextWarId++,
                pair[0],
                pair[1],
                DynastyDiplomacyService.worldMonthIndex(world),
                declarer.getId(),
                generateDisplayName(dynastyA, dynastyB),
                dynastyA.getMilitaryPower(),
                dynastyB.getMilitaryPower());
        activeWars.add(war);
        Dynasty aggressor = declarer;
        Dynasty defender = declarer.getId() == pair[0] ? dynastyB : dynastyA;
        WarProgressService.initializeCampaign(world, war, aggressor, defender);
        return war;
    }

    public War beginRebellionWar(Dynasty parent, Dynasty rebellion) {
        if (parent == null || rebellion == null || parent == rebellion
                || parent.isDefeated() || rebellion.isDefeated()) {
            return null;
        }
        int[] pair = War.canonicalPair(parent.getId(), rebellion.getId());
        War existing = findActiveWar(pair[0], pair[1]);
        if (existing != null) {
            return existing;
        }
        Dynasty dynastyA = world.findDynastyById(pair[0]);
        Dynasty dynastyB = world.findDynastyById(pair[1]);
        if (dynastyA == null || dynastyB == null) {
            return null;
        }
        War war = new War(
                nextWarId++,
                pair[0],
                pair[1],
                DynastyDiplomacyService.worldMonthIndex(world),
                parent.getId(),
                DynastyRebellionService.generateRebellionWarName(world, parent),
                dynastyA.getMilitaryPower(),
                dynastyB.getMilitaryPower());
        war.setRebellionWar(true);
        activeWars.add(war);
        WarProgressService.initializeCampaign(world, war, parent, rebellion);
        return war;
    }

    public void endWar(War war) {
        concludeWar(war, 0, LanguageStrings.WAR_CONCLUSION_UNKNOWN);
    }

    public void concludeWar(War war, int winnerDynastyId, String conclusionKey) {
        if (war == null || !war.isActive() || !activeWars.remove(war)) {
            return;
        }
        war.conclude(DynastyDiplomacyService.worldMonthIndex(world), winnerDynastyId, conclusionKey);
        historicWars.add(war);
        if (war.isRebellionWar()) {
            DynastyRebellionService.onRebellionWarConcluded(world, war, winnerDynastyId);
        }
        transferRemainingLoserColonies(war, winnerDynastyId, conclusionKey);
        transferVictoryAssimilations(war, winnerDynastyId, conclusionKey);
        clearDiplomaticWarState(war);
        if (LanguageStrings.WAR_CONCLUSION_PEACE_TREATY.equals(conclusionKey)) {
            applyPeaceTreatyWasAtWarModifier(war);
        }
        colonizeCapturedColoniesFromCapital(war, winnerDynastyId, conclusionKey);
    }

    private void transferRemainingLoserColonies(War war, int winnerDynastyId, String conclusionKey) {
        if (!LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY.equals(conclusionKey)
                || winnerDynastyId <= 0 || war == null) {
            return;
        }
        Dynasty winner = world.findDynastyById(winnerDynastyId);
        int loserId = war.getOtherDynastyId(winnerDynastyId);
        Dynasty loser = loserId >= 0 ? world.findDynastyById(loserId) : null;
        if (winner == null || loser == null || loser == winner) {
            return;
        }

        List<Colony> remaining = new ArrayList<>(loser.getColonies());
        for (Colony colony : remaining) {
            if (colony.getDynasty() == loser) {
                WarProgressService.captureColony(world, this, war, colony, winner, loser, false);
            }
        }

        if (!loser.getColonies().isEmpty()) {
            return;
        }
        loser.setDefeated(true);
        endWarsInvolving(loser);
    }

    private void colonizeCapturedColoniesFromCapital(War war, int winnerDynastyId, String conclusionKey) {
        if (!transfersAssimilationsOnConclusion(conclusionKey) || winnerDynastyId <= 0 || war == null) {
            return;
        }
        Dynasty winner = world.findDynastyById(winnerDynastyId);
        if (winner == null) {
            return;
        }
        Colony capital = winner.getCapital();
        if (capital == null) {
            return;
        }

        List<Integer> capturedIds = war.getColoniesCapturedBy(winnerDynastyId);
        if (capturedIds.isEmpty()) {
            return;
        }

        int colonized = 0;
        for (int colonyId : capturedIds) {
            Colony target = findColonyById(colonyId);
            if (target == null || target.getDynasty() != winner || target == capital) {
                continue;
            }
            if (!target.getQueens().isEmpty()) {
                continue;
            }
            if (!ColonyLabourService.establishQueenFromBreederPair(capital, target)) {
                break;
            }
            colonized++;
            target.logEvent(ColonyLogPrefixes.WAR + " "
                    + LanguageStrings.format(LanguageStrings.WAR_QUEEN_FROM_CAPITAL_FMT, capital.getName()));
        }

        if (colonized > 0) {
            capital.runRoleAssignment(null);
            capital.logEvent(ColonyLogPrefixes.WAR + " "
                    + LanguageStrings.format(LanguageStrings.WAR_CAPITAL_COLONIZED_CAPTURES_FMT,
                            String.valueOf(colonized)));
        }
    }

    private Colony findColonyById(int colonyId) {
        if (colonyId <= 0 || world.getHexes() == null) {
            return null;
        }
        for (Hex hex : world.getHexes()) {
            if (hex.getColony() != null && hex.getColony().getId() == colonyId) {
                return hex.getColony();
            }
        }
        return null;
    }

    private void transferVictoryAssimilations(War war, int winnerDynastyId, String conclusionKey) {
        if (winnerDynastyId <= 0 || !transfersAssimilationsOnConclusion(conclusionKey)) {
            return;
        }
        Dynasty winner = world.findDynastyById(winnerDynastyId);
        int loserId = war.getOtherDynastyId(winnerDynastyId);
        Dynasty loser = loserId >= 0 ? world.findDynastyById(loserId) : null;
        if (winner == null || loser == null) {
            return;
        }
        int inherited = winner.inheritAssimilationsFrom(loser);
        if (inherited <= 0 || winner.getCapital() == null) {
            return;
        }
        winner.getCapital().logEvent(ColonyLogPrefixes.DYNASTY + " "
                + LanguageStrings.format(LanguageStrings.WAR_INHERITED_ASSIMILATIONS_FMT,
                        loser.getName(), String.valueOf(inherited)));
    }

    private static boolean transfersAssimilationsOnConclusion(String conclusionKey) {
        return LanguageStrings.WAR_CONCLUSION_ABSOLUTE_VICTORY.equals(conclusionKey)
                || LanguageStrings.WAR_CONCLUSION_DEFEAT.equals(conclusionKey);
    }

    public void concludeWarByDefeat(War war, Dynasty winner) {
        if (war == null || winner == null) {
            return;
        }
        concludeWar(war, winner.getId(), LanguageStrings.WAR_CONCLUSION_DEFEAT);
    }

    public void endWarsInvolving(Dynasty dynasty) {
        if (dynasty == null) {
            return;
        }
        List<War> toEnd = new ArrayList<>();
        for (War war : activeWars) {
            if (war.involves(dynasty.getId())) {
                toEnd.add(war);
            }
        }
        for (War war : toEnd) {
            int winnerId = war.getOtherDynastyId(dynasty.getId());
            concludeWar(war, winnerId, LanguageStrings.WAR_CONCLUSION_DEFEAT);
        }
    }

    public void pruneInvalidWars() {
        activeWars.removeIf(war -> war == null || !war.isValidRecord());
        historicWars.removeIf(war -> war == null || !war.isValidRecord());

        List<War> toArchive = new ArrayList<>();
        for (War war : activeWars) {
            Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
            Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
            if (dynastyA == null || dynastyB == null || dynastyA.isDefeated() || dynastyB.isDefeated()) {
                toArchive.add(war);
                continue;
            }
            if (!dynastyA.getDiplomacyService().isAtWarWith(dynastyB)) {
                toArchive.add(war);
            }
        }
        for (War war : toArchive) {
            if (!activeWars.remove(war) || !war.isActive()) {
                continue;
            }
            int winnerId = resolveArchiveWinnerId(war);
            String conclusion = winnerId > 0
                    ? LanguageStrings.WAR_CONCLUSION_DEFEAT
                    : LanguageStrings.WAR_CONCLUSION_UNKNOWN;
            war.conclude(DynastyDiplomacyService.worldMonthIndex(world), winnerId, conclusion);
            historicWars.add(war);
        }
    }

    private int resolveArchiveWinnerId(War war) {
        Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
        Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
        if (dynastyA != null && !dynastyA.isDefeated()
                && (dynastyB == null || dynastyB.isDefeated())) {
            return dynastyA.getId();
        }
        if (dynastyB != null && !dynastyB.isDefeated()
                && (dynastyA == null || dynastyA.isDefeated())) {
            return dynastyB.getId();
        }
        return 0;
    }

    public void syncFromDynasties() {
        if (world.getDynastys() == null) {
            return;
        }
        for (Dynasty dynasty : world.getDynastys()) {
            if (dynasty.isDefeated()) {
                continue;
            }
            for (int otherId : dynasty.copyActiveWarDynastyIds()) {
                Dynasty other = world.findDynastyById(otherId);
                if (other == null || other.isDefeated()) {
                    continue;
                }
                int[] pair = War.canonicalPair(dynasty.getId(), otherId);
                if (findActiveWar(pair[0], pair[1]) != null) {
                    continue;
                }
                Dynasty dynastyA = world.findDynastyById(pair[0]);
                Dynasty dynastyB = world.findDynastyById(pair[1]);
                if (dynastyA == null || dynastyB == null) {
                    continue;
                }
                activeWars.add(new War(
                        nextWarId++,
                        pair[0],
                        pair[1],
                        DynastyDiplomacyService.worldMonthIndex(world),
                        dynasty.getId(),
                        generateDisplayName(dynastyA, dynastyB),
                        dynastyA.getMilitaryPower(),
                        dynastyB.getMilitaryPower()));
                War backfilled = activeWars.get(activeWars.size() - 1);
                Dynasty aggressor = world.findDynastyById(backfilled.getDeclaredByDynastyId());
                Dynasty defender = world.findDynastyById(backfilled.getDefenderDynastyId());
                WarProgressService.initializeCampaign(world, backfilled, aggressor, defender);
            }
        }
        activeWars.sort(Comparator.comparingInt(War::getId));
        if (!activeWars.isEmpty()) {
            nextWarId = activeWars.get(activeWars.size() - 1).getId() + 1;
        }
    }

    public void loadFromSave(List<Savefile.SavedWar> savedWars) {
        activeWars.clear();
        historicWars.clear();
        nextWarId = 1;
        if (savedWars == null) {
            return;
        }
        for (Savefile.SavedWar saved : savedWars) {
            if (saved == null || !War.isValidRecord(saved.dynastyIdA, saved.dynastyIdB)) {
                continue;
            }
            War war = new War(saved);
            if (saved.id >= nextWarId) {
                nextWarId = saved.id + 1;
            }
            if (war.isActive()) {
                activeWars.add(war);
            } else {
                historicWars.add(war);
            }
        }
        for (War war : activeWars) {
            if (!war.isCampaignInitialized()) {
                Dynasty aggressor = world.findDynastyById(war.getAggressorDynastyId());
                Dynasty defender = world.findDynastyById(war.getDefenderDynastyId());
                WarProgressService.initializeCampaign(world, war, aggressor, defender);
            }
        }
    }

    public List<Savefile.SavedWar> toSavedWars() {
        List<Savefile.SavedWar> saved = new ArrayList<>();
        for (War war : activeWars) {
            if (war != null && war.isValidRecord()) {
                saved.add(war.toSave());
            }
        }
        for (War war : historicWars) {
            if (war != null && war.isValidRecord()) {
                saved.add(war.toSave());
            }
        }
        return saved;
    }

    public List<War> getActiveWars() {
        return new ArrayList<>(activeWars);
    }

    public List<War> getHistoricWars() {
        List<War> wars = new ArrayList<>(historicWars);
        wars.sort(Comparator.comparingInt(War::getEndedWorldMonth).reversed());
        return wars;
    }

    public List<War> getWarsForDynasty(int dynastyId) {
        List<War> wars = new ArrayList<>();
        for (War war : activeWars) {
            if (war.involves(dynastyId)) {
                wars.add(war);
            }
        }
        wars.sort(Comparator.comparing(War::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return wars;
    }

    public List<War> getHistoricWarsForDynasty(int dynastyId) {
        List<War> wars = new ArrayList<>();
        for (War war : historicWars) {
            if (war.involves(dynastyId)) {
                wars.add(war);
            }
        }
        wars.sort(Comparator.comparingInt(War::getEndedWorldMonth).reversed());
        return wars;
    }

    public boolean hasWarHistoryForDynasty(int dynastyId) {
        for (War war : historicWars) {
            if (war.involves(dynastyId)) {
                return true;
            }
        }
        return false;
    }

    public War findActiveWar(int dynastyIdOne, int dynastyIdTwo) {
        int[] pair = War.canonicalPair(dynastyIdOne, dynastyIdTwo);
        for (War war : activeWars) {
            if (war.getDynastyIdA() == pair[0] && war.getDynastyIdB() == pair[1]) {
                return war;
            }
        }
        return null;
    }

    public War findWar(int dynastyIdOne, int dynastyIdTwo) {
        return findActiveWar(dynastyIdOne, dynastyIdTwo);
    }

    public boolean hasActiveWars() {
        return !activeWars.isEmpty();
    }

    public Dynasty resolveOpponent(War war, Dynasty viewer) {
        if (war == null || viewer == null) {
            return null;
        }
        int otherId = war.getOtherDynastyId(viewer.getId());
        return otherId >= 0 ? world.findDynastyById(otherId) : null;
    }

    public Dynasty resolveDeclarer(War war) {
        if (war == null) {
            return null;
        }
        return world.findDynastyById(war.getDeclaredByDynastyId());
    }

    public Dynasty resolveWinner(War war) {
        if (war == null || war.getWinnerDynastyId() <= 0) {
            return null;
        }
        return world.findDynastyById(war.getWinnerDynastyId());
    }

    public String resolveWinnerDisplayName(War war, Dynasty viewer) {
        if (war == null) {
            return "";
        }
        Dynasty winner = resolveWinner(war);
        if (winner != null) {
            return winner.getName();
        }
        return LanguageStrings.get(LanguageStrings.WAR_WINNER_NONE);
    }

    public WarStanding getStandingForDynasty(War war, Dynasty viewer) {
        if (war == null || viewer == null || !war.involves(viewer.getId())) {
            return WarStanding.EVEN;
        }
        Dynasty opponent = resolveOpponent(war, viewer);
        if (opponent == null) {
            return WarStanding.EVEN;
        }
        return compareMilitaryStanding(
                ColonyMilitaryService.powerForWarStanding(viewer),
                ColonyMilitaryService.powerForWarStanding(opponent));
    }

    public Dynasty getLeadingDynasty(War war) {
        if (war == null) {
            return null;
        }
        Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
        Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
        if (dynastyA == null || dynastyB == null) {
            return null;
        }
        WarStanding fromA = compareMilitaryStanding(
                ColonyMilitaryService.powerForWarStanding(dynastyA),
                ColonyMilitaryService.powerForWarStanding(dynastyB));
        if (fromA == WarStanding.WINNING) {
            return dynastyA;
        }
        if (fromA == WarStanding.LOSING) {
            return dynastyB;
        }
        return null;
    }

    public boolean canOfferPeace(War war, Dynasty offerer) {
        if (war == null || offerer == null || !war.isActive() || !war.involves(offerer.getId())) {
            return false;
        }
        if (war.isRebellionWar()) {
            return false;
        }
        if (war.getPendingPeaceOfferFromDynastyId() != 0) {
            return false;
        }
        return getStandingForDynasty(war, offerer) == WarStanding.WINNING;
    }

    public boolean offerPeace(War war, Dynasty offerer) {
        if (!canOfferPeace(war, offerer)) {
            return false;
        }
        war.setPendingPeaceOfferFromDynastyId(offerer.getId());
        Dynasty opponent = resolveOpponent(war, offerer);
        if (opponent == null) {
            war.clearPendingPeaceOffer();
            return false;
        }
        if (!opponent.isPlayer()) {
            evaluateAiPeaceResponse(war, opponent, offerer);
        }
        return true;
    }

    public boolean canAcceptPeaceOffer(War war, Dynasty accepter) {
        if (war == null || accepter == null || !war.isActive() || !war.involves(accepter.getId())) {
            return false;
        }
        if (war.isRebellionWar()) {
            return false;
        }
        int offererId = war.getPendingPeaceOfferFromDynastyId();
        if (offererId <= 0 || offererId == accepter.getId()) {
            return false;
        }
        return getStandingForDynasty(war, accepter) == WarStanding.LOSING;
    }

    public void acceptPeaceOffer(War war, Dynasty accepter) {
        if (!canAcceptPeaceOffer(war, accepter)) {
            return;
        }
        Dynasty offerer = world.findDynastyById(war.getPendingPeaceOfferFromDynastyId());
        int winnerId = offerer != null ? offerer.getId() : 0;
        if (offerer == null || getStandingForDynasty(war, offerer) != WarStanding.WINNING) {
            Dynasty leader = getLeadingDynasty(war);
            winnerId = leader != null ? leader.getId() : 0;
        }
        concludeWar(war, winnerId, LanguageStrings.WAR_CONCLUSION_PEACE_TREATY);
    }

    public void declinePeaceOffer(War war, Dynasty decliner) {
        if (war == null || decliner == null || !war.involves(decliner.getId())) {
            return;
        }
        war.clearPendingPeaceOffer();
    }

    public boolean forfeitWarStage(War war, Dynasty forfeitier) {
        return WarProgressService.forfeitStage(world, this, war, forfeitier);
    }

    public boolean canForfeitStage(War war, Dynasty forfeitier) {
        return WarProgressService.canForfeitStage(world, war, forfeitier);
    }

    public void tickWarProgressHourly() {
        WarProgressService.tickActiveWarsHourly(world, this);
    }

    public void tickWarProgressDaily() {
        WarProgressService.tickActiveWarsDaily(world, this);
    }

    public void tickWarProgress() {
        tickWarProgressHourly();
        tickWarProgressDaily();
    }

    public String formatProgress(War war) {
        if (war == null) {
            return "";
        }
        return war.formatProgressPercent();
    }

    public String formatStanding(WarStanding standing) {
        if (standing == null) {
            return LanguageStrings.get(LanguageStrings.WAR_STANDING_EVEN);
        }
        return switch (standing) {
            case WINNING -> LanguageStrings.get(LanguageStrings.WAR_STANDING_WINNING);
            case LOSING -> LanguageStrings.get(LanguageStrings.WAR_STANDING_LOSING);
            case EVEN -> LanguageStrings.get(LanguageStrings.WAR_STANDING_EVEN);
        };
    }

    public String formatWarNameForDisplay(War war, Dynasty viewer) {
        if (war == null) {
            return "";
        }
        if (war.getDisplayName() != null && !war.getDisplayName().isEmpty()) {
            return war.getDisplayName();
        }
        Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
        Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
        if (dynastyA != null && dynastyB != null) {
            return generateDisplayName(dynastyA, dynastyB);
        }
        return LanguageStrings.format(LanguageStrings.MAP_ACTIVE_WAR_PAIR_FMT, "?", "?");
    }

    private void evaluateAiPeaceResponse(War war, Dynasty ai, Dynasty offerer) {
        if (getStandingForDynasty(war, ai) == WarStanding.LOSING
                && GameRandom.nextDouble() < GameConstants.AI_ACCEPT_PEACE_CHANCE) {
            acceptPeaceOffer(war, ai);
        } else {
            war.clearPendingPeaceOffer();
        }
    }

    private void clearDiplomaticWarState(War war) {
        Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
        Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
        TradeManager tradeManager = world.getEngine() != null ? world.getEngine().getTradeManager() : null;
        if (dynastyA != null && dynastyB != null) {
            dynastyA.getDiplomacyService().clearWarWith(dynastyB, tradeManager);
        }
    }

    private void applyPeaceTreatyWasAtWarModifier(War war) {
        Dynasty dynastyA = world.findDynastyById(war.getDynastyIdA());
        Dynasty dynastyB = world.findDynastyById(war.getDynastyIdB());
        if (dynastyA != null && dynastyB != null) {
            dynastyA.getDiplomacyService().applyWasAtWarModifier(dynastyB, world);
        }
    }

    private WarStanding compareMilitaryStanding(int powerA, int powerB) {
        if (powerA <= 0 && powerB <= 0) {
            return WarStanding.EVEN;
        }
        float ratio = GameConstants.WAR_STANDING_MILITARY_RATIO;
        if (powerA >= powerB * ratio) {
            return WarStanding.WINNING;
        }
        if (powerB >= powerA * ratio) {
            return WarStanding.LOSING;
        }
        return WarStanding.EVEN;
    }

    private String generateDisplayName(Dynasty dynastyA, Dynasty dynastyB) {
        int ordinal = countWarsBetweenPair(dynastyA.getId(), dynastyB.getId()) + 1;
        String themeA = themeFromDynasty(dynastyA);
        String themeB = themeFromDynasty(dynastyB);
        if (themeA.compareToIgnoreCase(themeB) > 0) {
            String swap = themeA;
            themeA = themeB;
            themeB = swap;
        }
        return LanguageStrings.format(LanguageStrings.WAR_NAME_FMT,
                LanguageStrings.getWarOrdinal(ordinal), themeA, themeB);
    }

    private int countWarsBetweenPair(int dynastyIdOne, int dynastyIdTwo) {
        int[] pair = War.canonicalPair(dynastyIdOne, dynastyIdTwo);
        int count = 0;
        for (War war : activeWars) {
            if (war.getDynastyIdA() == pair[0] && war.getDynastyIdB() == pair[1]) {
                count++;
            }
        }
        for (War war : historicWars) {
            if (war.getDynastyIdA() == pair[0] && war.getDynastyIdB() == pair[1]) {
                count++;
            }
        }
        return count;
    }

    private static String themeFromDynasty(Dynasty dynasty) {
        if (dynasty == null) {
            return "?";
        }
        String theme = dynasty.getThemeBase();
        if (theme == null || theme.isEmpty()) {
            theme = LanguageStrings.stripDynastyNameSuffix(dynasty.getName());
        }
        if (theme == null || theme.isEmpty()) {
            return dynasty.getName();
        }
        return theme;
    }
}
