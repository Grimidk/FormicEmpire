package com.grimidk.formicempire.classes.entities.dynasty;

import com.grimidk.formicempire.classes.constants.dynasty.WarStagePhase;
import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class War {

    public static final int ACTIVE_END_MONTH = -1;

    private final int id;
    private final int dynastyIdA;
    private final int dynastyIdB;
    private final int startedWorldMonth;
    private final int declaredByDynastyId;
    private final String displayName;
    private final int militaryPowerAtStartA;
    private final int militaryPowerAtStartB;

    private int endedWorldMonth = ACTIVE_END_MONTH;
    private int winnerDynastyId;
    private String conclusionKey;
    private int pendingPeaceOfferFromDynastyId;

    private float progressPercent = 50f;
    private int totalStages;
    private int aggressorStagesCaptured;
    private int defenderStagesCaptured;
    private float stageProgress;
    private WarStagePhase stagePhase = GameConstants.WAR_STAGE_ACTIVE_CLASH;
    private int contestedColonyId;
    private int stageAttackerDynastyId;
    private int deployedActiveAttacker;
    private int deployedActiveDefender;
    private int deployedReserveDefender;
    private int aggressorCapitalColonyId;
    private int defenderCapitalColonyId;
    private int redeployHoursRemaining;
    private int stageStartActiveAggressor;
    private int stageStartActiveDefender;
    private boolean rebellionWar;

    private final List<Integer> capturedColonyIds = new ArrayList<>();
    private final List<Integer> capturedByDynastyIds = new ArrayList<>();

    public War(int id, int dynastyIdOne, int dynastyIdTwo, int startedWorldMonth, int declaredByDynastyId,
            String displayName, int militaryPowerAtStartA, int militaryPowerAtStartB) {
        this.id = id;
        int[] pair = canonicalPair(dynastyIdOne, dynastyIdTwo);
        this.dynastyIdA = pair[0];
        this.dynastyIdB = pair[1];
        this.startedWorldMonth = startedWorldMonth;
        this.declaredByDynastyId = declaredByDynastyId;
        this.displayName = displayName != null ? displayName : "";
        this.militaryPowerAtStartA = militaryPowerAtStartA;
        this.militaryPowerAtStartB = militaryPowerAtStartB;
    }

    public War(Savefile.SavedWar saved) {
        this(saved.id, saved.dynastyIdA, saved.dynastyIdB, saved.startedWorldMonth, saved.declaredByDynastyId,
                saved.displayName, saved.militaryPowerAtStartA, saved.militaryPowerAtStartB);
        this.endedWorldMonth = saved.endedWorldMonth;
        this.winnerDynastyId = saved.winnerDynastyId;
        this.conclusionKey = saved.conclusionKey;
        this.pendingPeaceOfferFromDynastyId = saved.pendingPeaceOfferFromDynastyId;
        this.progressPercent = saved.totalStages > 0 ? saved.progressPercent : 50f;
        this.totalStages = saved.totalStages;
        this.aggressorStagesCaptured = saved.aggressorStagesCaptured;
        this.defenderStagesCaptured = saved.defenderStagesCaptured;
        this.stageProgress = saved.stageProgress;
        this.stagePhase = parseStagePhase(saved.stagePhaseKey);
        this.contestedColonyId = saved.contestedColonyId;
        this.stageAttackerDynastyId = saved.stageAttackerDynastyId;
        this.deployedActiveAttacker = saved.deployedActiveAttacker;
        this.deployedActiveDefender = saved.deployedActiveDefender;
        this.deployedReserveDefender = saved.deployedReserveDefender;
        this.aggressorCapitalColonyId = saved.aggressorCapitalColonyId;
        this.defenderCapitalColonyId = saved.defenderCapitalColonyId;
        this.redeployHoursRemaining = saved.redeployHoursRemaining;
        this.stageStartActiveAggressor = saved.stageStartActiveAggressor;
        this.stageStartActiveDefender = saved.stageStartActiveDefender;
        this.capturedColonyIds.addAll(parseIdList(saved.capturedColonyIds));
        this.capturedByDynastyIds.addAll(parseIdList(saved.capturedByDynastyIds));
        this.rebellionWar = saved.rebellionWar;
    }

    public static int[] canonicalPair(int dynastyIdOne, int dynastyIdTwo) {
        if (dynastyIdOne <= dynastyIdTwo) {
            return new int[] { dynastyIdOne, dynastyIdTwo };
        }
        return new int[] { dynastyIdTwo, dynastyIdOne };
    }

    public int getId() {
        return id;
    }

    public int getDynastyIdA() {
        return dynastyIdA;
    }

    public int getDynastyIdB() {
        return dynastyIdB;
    }

    public int getStartedWorldMonth() {
        return startedWorldMonth;
    }

    public int getDeclaredByDynastyId() {
        return declaredByDynastyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMilitaryPowerAtStartA() {
        return militaryPowerAtStartA;
    }

    public int getMilitaryPowerAtStartB() {
        return militaryPowerAtStartB;
    }

    public boolean isRebellionWar() {
        return rebellionWar;
    }

    public void setRebellionWar(boolean rebellionWar) {
        this.rebellionWar = rebellionWar;
    }

    public int getMilitaryPowerAtStart(int dynastyId) {
        if (dynastyId == dynastyIdA) {
            return militaryPowerAtStartA;
        }
        if (dynastyId == dynastyIdB) {
            return militaryPowerAtStartB;
        }
        return 0;
    }

    public int getEndedWorldMonth() {
        return endedWorldMonth;
    }

    public int getWinnerDynastyId() {
        return winnerDynastyId;
    }

    public String getConclusionKey() {
        return conclusionKey;
    }

    public int getPendingPeaceOfferFromDynastyId() {
        return pendingPeaceOfferFromDynastyId;
    }

    public void setPendingPeaceOfferFromDynastyId(int dynastyId) {
        this.pendingPeaceOfferFromDynastyId = dynastyId;
    }

    public void clearPendingPeaceOffer() {
        this.pendingPeaceOfferFromDynastyId = 0;
    }

    public float getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(float progressPercent) {
        this.progressPercent = Math.max(0f, Math.min(100f, progressPercent));
    }

    public int getTotalStages() {
        return totalStages;
    }

    public void setTotalStages(int totalStages) {
        this.totalStages = Math.max(0, totalStages);
    }

    public int getAggressorStagesCaptured() {
        return aggressorStagesCaptured;
    }

    public void setAggressorStagesCaptured(int aggressorStagesCaptured) {
        this.aggressorStagesCaptured = Math.max(0, aggressorStagesCaptured);
    }

    public int getDefenderStagesCaptured() {
        return defenderStagesCaptured;
    }

    public void setDefenderStagesCaptured(int defenderStagesCaptured) {
        this.defenderStagesCaptured = Math.max(0, defenderStagesCaptured);
    }

    public float getStageProgress() {
        return stageProgress;
    }

    public void setStageProgress(float stageProgress) {
        this.stageProgress = Math.max(0f, Math.min(1f, stageProgress));
    }

    public WarStagePhase getStagePhase() {
        return stagePhase;
    }

    public void setStagePhase(WarStagePhase stagePhase) {
        this.stagePhase = stagePhase != null ? stagePhase : GameConstants.WAR_STAGE_ACTIVE_CLASH;
    }

    public int getContestedColonyId() {
        return contestedColonyId;
    }

    public void setContestedColonyId(int contestedColonyId) {
        this.contestedColonyId = contestedColonyId;
    }

    public int getStageAttackerDynastyId() {
        return stageAttackerDynastyId;
    }

    public void setStageAttackerDynastyId(int stageAttackerDynastyId) {
        this.stageAttackerDynastyId = stageAttackerDynastyId;
    }

    public int getDeployedActiveAttacker() {
        return deployedActiveAttacker;
    }

    public void setDeployedActiveAttacker(int deployedActiveAttacker) {
        this.deployedActiveAttacker = Math.max(0, deployedActiveAttacker);
    }

    public int getDeployedActiveDefender() {
        return deployedActiveDefender;
    }

    public void setDeployedActiveDefender(int deployedActiveDefender) {
        this.deployedActiveDefender = Math.max(0, deployedActiveDefender);
    }

    public int getDeployedReserveDefender() {
        return deployedReserveDefender;
    }

    public void setDeployedReserveDefender(int deployedReserveDefender) {
        this.deployedReserveDefender = Math.max(0, deployedReserveDefender);
    }

    public int getAggressorCapitalColonyId() {
        return aggressorCapitalColonyId;
    }

    public void setAggressorCapitalColonyId(int aggressorCapitalColonyId) {
        this.aggressorCapitalColonyId = aggressorCapitalColonyId;
    }

    public int getDefenderCapitalColonyId() {
        return defenderCapitalColonyId;
    }

    public void setDefenderCapitalColonyId(int defenderCapitalColonyId) {
        this.defenderCapitalColonyId = defenderCapitalColonyId;
    }

    public int getRedeployHoursRemaining() {
        return redeployHoursRemaining;
    }

    public void setRedeployHoursRemaining(int redeployHoursRemaining) {
        this.redeployHoursRemaining = Math.max(0, redeployHoursRemaining);
    }

    public int getStageStartActiveAggressor() {
        return stageStartActiveAggressor;
    }

    public void setStageStartActiveAggressor(int stageStartActiveAggressor) {
        this.stageStartActiveAggressor = Math.max(0, stageStartActiveAggressor);
    }

    public int getStageStartActiveDefender() {
        return stageStartActiveDefender;
    }

    public void setStageStartActiveDefender(int stageStartActiveDefender) {
        this.stageStartActiveDefender = Math.max(0, stageStartActiveDefender);
    }

    public int getStageStartActiveFor(int dynastyId) {
        if (dynastyId == getAggressorDynastyId()) {
            return stageStartActiveAggressor;
        }
        if (dynastyId == getDefenderDynastyId()) {
            return stageStartActiveDefender;
        }
        return 0;
    }

    public boolean isCampaignInitialized() {
        return totalStages > 0 && contestedColonyId > 0;
    }

    public int getAggressorDynastyId() {
        return declaredByDynastyId;
    }

    public int getDefenderDynastyId() {
        return getOtherDynastyId(declaredByDynastyId);
    }

    public void recomputeProgressPercent() {
        if (totalStages <= 0) {
            progressPercent = 50f;
            return;
        }
        float delta = (aggressorStagesCaptured - defenderStagesCaptured + stageProgress)
                * (50f / totalStages);
        setProgressPercent(50f + delta);
    }

    public String formatProgressPercent() {
        return LanguageStrings.format(LanguageStrings.WAR_PROGRESS_FMT, Math.round(progressPercent));
    }

    public boolean isActive() {
        return endedWorldMonth < 0;
    }

    public static boolean isValidRecord(int dynastyIdA, int dynastyIdB) {
        return dynastyIdA > 0 && dynastyIdB > 0 && dynastyIdA != dynastyIdB;
    }

    public boolean isValidRecord() {
        return isValidRecord(dynastyIdA, dynastyIdB);
    }

    public boolean involves(int dynastyId) {
        return dynastyId == dynastyIdA || dynastyId == dynastyIdB;
    }

    public int getOtherDynastyId(int dynastyId) {
        if (dynastyId == dynastyIdA) {
            return dynastyIdB;
        }
        if (dynastyId == dynastyIdB) {
            return dynastyIdA;
        }
        return -1;
    }

    public int getDurationMonths(int currentWorldMonth) {
        int end = isActive() ? currentWorldMonth : endedWorldMonth;
        return Math.max(0, end - startedWorldMonth);
    }

    public String formatStartedDate() {
        return formatWorldMonth(startedWorldMonth);
    }

    public String formatEndedDate() {
        if (isActive()) {
            return LanguageStrings.get(LanguageStrings.WAR_ONGOING);
        }
        return formatWorldMonth(endedWorldMonth);
    }

    public String formatConclusion() {
        if (conclusionKey == null || conclusionKey.isEmpty()) {
            return LanguageStrings.get(LanguageStrings.WAR_CONCLUSION_UNKNOWN);
        }
        return LanguageStrings.get(conclusionKey);
    }

    public static String formatWorldMonth(int worldMonth) {
        int year = worldMonth / 12;
        int month = worldMonth % 12;
        if (month == 0) {
            month = 12;
            year--;
        }
        return LanguageStrings.format(LanguageStrings.WAR_WORLD_MONTH_FMT, year, month);
    }

    public void recordCapture(int victorDynastyId, int colonyId) {
        if (victorDynastyId <= 0 || colonyId <= 0) {
            return;
        }
        capturedColonyIds.add(colonyId);
        capturedByDynastyIds.add(victorDynastyId);
    }

    public List<Integer> getColoniesCapturedBy(int dynastyId) {
        if (dynastyId <= 0) {
            return Collections.emptyList();
        }
        List<Integer> colonies = new ArrayList<>();
        for (int i = 0; i < capturedColonyIds.size(); i++) {
            if (capturedByDynastyIds.get(i) == dynastyId) {
                colonies.add(capturedColonyIds.get(i));
            }
        }
        return colonies;
    }

    public void conclude(int endedWorldMonth, int winnerDynastyId, String conclusionKey) {
        this.endedWorldMonth = endedWorldMonth;
        this.winnerDynastyId = winnerDynastyId;
        this.conclusionKey = conclusionKey;
        this.pendingPeaceOfferFromDynastyId = 0;
    }

    public Savefile.SavedWar toSave() {
        Savefile.SavedWar saved = new Savefile.SavedWar();
        saved.id = id;
        saved.dynastyIdA = dynastyIdA;
        saved.dynastyIdB = dynastyIdB;
        saved.startedWorldMonth = startedWorldMonth;
        saved.declaredByDynastyId = declaredByDynastyId;
        saved.displayName = displayName;
        saved.militaryPowerAtStartA = militaryPowerAtStartA;
        saved.militaryPowerAtStartB = militaryPowerAtStartB;
        saved.endedWorldMonth = endedWorldMonth;
        saved.winnerDynastyId = winnerDynastyId;
        saved.conclusionKey = conclusionKey;
        saved.pendingPeaceOfferFromDynastyId = pendingPeaceOfferFromDynastyId;
        saved.progressPercent = progressPercent;
        saved.totalStages = totalStages;
        saved.aggressorStagesCaptured = aggressorStagesCaptured;
        saved.defenderStagesCaptured = defenderStagesCaptured;
        saved.stageProgress = stageProgress;
        saved.stagePhaseKey = stagePhase != null
                ? stagePhase.getPersistenceKey()
                : GameConstants.WAR_STAGE_ACTIVE_CLASH.getPersistenceKey();
        saved.contestedColonyId = contestedColonyId;
        saved.stageAttackerDynastyId = stageAttackerDynastyId;
        saved.deployedActiveAttacker = deployedActiveAttacker;
        saved.deployedActiveDefender = deployedActiveDefender;
        saved.deployedReserveDefender = deployedReserveDefender;
        saved.aggressorCapitalColonyId = aggressorCapitalColonyId;
        saved.defenderCapitalColonyId = defenderCapitalColonyId;
        saved.redeployHoursRemaining = redeployHoursRemaining;
        saved.stageStartActiveAggressor = stageStartActiveAggressor;
        saved.stageStartActiveDefender = stageStartActiveDefender;
        saved.capturedColonyIds = joinIdList(capturedColonyIds);
        saved.capturedByDynastyIds = joinIdList(capturedByDynastyIds);
        saved.rebellionWar = rebellionWar;
        return saved;
    }

    private static List<Integer> parseIdList(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return ids;
        }
        for (String part : csv.split(",")) {
            try {
                int id = Integer.parseInt(part.trim());
                if (id > 0) {
                    ids.add(id);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    private static String joinIdList(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
        return sb.toString();
    }

    private static WarStagePhase parseStagePhase(String key) {
        return GameConstants.getWarStagePhaseByPersistenceKey(key);
    }
}
