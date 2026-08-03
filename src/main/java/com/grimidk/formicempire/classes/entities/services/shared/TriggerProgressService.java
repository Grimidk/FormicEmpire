package com.grimidk.formicempire.classes.entities.services.shared;

import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.ResourceSource;
import com.grimidk.formicempire.classes.infrasctructure.Engine;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class TriggerProgressService {

    private TriggerProgressService() {
    }

    public static List<TriggerProgress> getVisible(Colony colony, Engine engine) {
        return getVisible(colony, engine, true);
    }

    public static List<TriggerProgress> getVisible(Colony colony, Engine engine, boolean includeCompleted) {
        List<TriggerProgress> visible = new ArrayList<>();
        for (TriggerProgress progress : buildAll(colony, engine)) {
            if (isVisible(progress, includeCompleted)) {
                visible.add(progress);
            }
        }
        visible.sort(Comparator
                .comparing(TriggerProgress::isUnlocked)
                .thenComparing((a, b) -> Double.compare(b.ratio(), a.ratio())));
        return visible;
    }

    public static TriggerProgress find(Colony colony, Engine engine, Upgrade upgrade) {
        if (colony == null || upgrade == null) {
            return null;
        }
        for (TriggerProgress progress : buildAll(colony, engine)) {
            if (progress.getUpgrade() == upgrade) {
                return progress;
            }
        }
        return null;
    }

    public static boolean isVisible(TriggerProgress progress) {
        return isVisible(progress, true);
    }

    public static boolean isVisible(TriggerProgress progress, boolean includeCompleted) {
        if (progress == null || !progress.isGateMet()) {
            return false;
        }
        if (progress.isUnlocked()) {
            return includeCompleted;
        }
        return progress.getCurrent() > 0;
    }

    private static List<TriggerProgress> buildAll(Colony colony, Engine engine) {
        List<TriggerProgress> entries = new ArrayList<>();
        if (colony == null) {
            return entries;
        }

        Dynasty dynasty = colony.getDynasty();
        World world = engine != null ? engine.getWorld() : null;
        TradeManager tradeManager = engine != null ? engine.getTradeManager() : null;

        entries.add(numeric(
                GameUnlocks.ROLE_RESEARCHER,
                LanguageStrings.TRIGGER_RESEARCHER_ROLE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_RESEARCHER,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_MONTHS,
                colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER),
                true,
                elapsedMonths(world),
                GameNumbers.TRIGGER_RESEARCHER_MIN_MONTHS));

        entries.add(numeric(
                GameUnlocks.ROLE_GRAVER,
                LanguageStrings.TRIGGER_GRAVER_ROLE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_GRAVER,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_CORPSES,
                colony.hasUpgrade(GameUnlocks.ROLE_GRAVER),
                true,
                colony.getDeadAnts() != null ? colony.getDeadAnts().size() : 0,
                GameNumbers.TRIGGER_GRAVER_DEAD_ANTS));

        entries.add(numeric(
                GameUnlocks.ABILITY_RESEARCH,
                LanguageStrings.TRIGGER_RESEARCH_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_RESEARCH,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_RP,
                colony.hasUpgrade(GameUnlocks.ABILITY_RESEARCH),
                true,
                colony.getResearchPoints(),
                GameNumbers.TRIGGER_RESEARCH_MIN_RP));

        entries.add(numeric(
                GameUnlocks.ROLE_POLICE,
                LanguageStrings.TRIGGER_POLICE_ROLE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_POLICE,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_RANK,
                colony.hasUpgrade(GameUnlocks.ROLE_POLICE),
                true,
                colony.getRank() != null ? colony.getRank().getId() : 0,
                GameConstants.RANK_DUCHY.getId()));

        int colonyCount = dynasty != null ? dynasty.getColonies().size() : 0;
        boolean hasDynasty = dynasty != null;

        entries.add(numeric(
                GameUnlocks.ABILITY_DYNASTY,
                LanguageStrings.TRIGGER_DYNASTY_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_COLONIES,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_COLONIES,
                colony.hasUpgrade(GameUnlocks.ABILITY_DYNASTY),
                hasDynasty,
                colonyCount,
                GameNumbers.TRIGGER_DYNASTY_MIN_COLONIES));

        entries.add(numeric(
                GameUnlocks.ABILITY_TRADE,
                LanguageStrings.TRIGGER_TRADE_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_COLONIES,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_COLONIES,
                colony.hasUpgrade(GameUnlocks.ABILITY_TRADE),
                hasDynasty,
                colonyCount,
                GameNumbers.TRIGGER_TRADE_MIN_COLONIES));

        entries.add(numeric(
                GameUnlocks.ABILITY_MANAGEMENT,
                LanguageStrings.TRIGGER_MANAGEMENT_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_COLONIES,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_COLONIES,
                colony.hasUpgrade(GameUnlocks.ABILITY_MANAGEMENT),
                hasDynasty,
                colonyCount,
                GameNumbers.TRIGGER_MANAGEMENT_MIN_COLONIES));

        entries.add(numeric(
                GameUnlocks.ABILITY_SPREAD_2,
                LanguageStrings.TRIGGER_SPREAD_2_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_COLONIES,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_COLONIES,
                colony.hasUpgrade(GameUnlocks.ABILITY_SPREAD_2),
                hasDynasty,
                colonyCount,
                GameNumbers.TRIGGER_SPREAD_2_MIN_COLONIES));

        entries.add(numeric(
                GameUnlocks.ABILITY_AUTOMATION,
                LanguageStrings.TRIGGER_AUTOMATION_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_COLONIES,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_COLONIES,
                colony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION),
                hasDynasty,
                colonyCount,
                GameNumbers.TRIGGER_AUTOMATION_MIN_COLONIES));

        int nuptials = dynasty != null ? dynasty.getTotalNuptialFlights() : 0;
        entries.add(numeric(
                GameUnlocks.ABILITY_MASS_FLIGHT,
                LanguageStrings.TRIGGER_MASS_FLIGHT_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_NUPTIALS,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_NUPTIALS,
                colony.hasUpgrade(GameUnlocks.ABILITY_MASS_FLIGHT),
                hasDynasty,
                nuptials,
                GameNumbers.TRIGGER_MASS_FLIGHT_MIN_NUPTIALS));

        int activeTrades = tradeManager != null ? tradeManager.getActiveTrades().size() : 0;
        entries.add(numeric(
                GameUnlocks.ABILITY_BILATERAL_TRADE,
                LanguageStrings.TRIGGER_BILATERAL_TRADE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_TRADES,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_TRADES,
                colony.hasUpgrade(GameUnlocks.ABILITY_BILATERAL_TRADE),
                true,
                activeTrades,
                GameNumbers.TRIGGER_BILATERAL_MIN_TRADES));

        boolean automationUnlocked = colony.hasUpgrade(GameUnlocks.ABILITY_AUTOMATION);
        int tunnels = dynasty != null ? dynasty.countCompleteTunnels() : 0;
        entries.add(numeric(
                GameUnlocks.ABILITY_AUTO_TUNNELS,
                LanguageStrings.TRIGGER_AUTO_TUNNELS_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_TUNNELS,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_TUNNELS,
                colony.hasUpgrade(GameUnlocks.ABILITY_AUTO_TUNNELS),
                automationUnlocked,
                tunnels,
                GameNumbers.AUTO_UPGRADE_MIN_COMPLETE_TUNNELS));

        int diplomatsSent = dynasty != null ? dynasty.getDiplomatsSentTotal() : 0;
        entries.add(numeric(
                GameUnlocks.ABILITY_AUTO_DIPLOMACY,
                LanguageStrings.TRIGGER_AUTO_DIPLOMACY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_DIPLOMATS,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_DIPLOMATS,
                colony.hasUpgrade(GameUnlocks.ABILITY_AUTO_DIPLOMACY),
                automationUnlocked,
                diplomatsSent,
                GameNumbers.AUTO_UPGRADE_MIN_DIPLOMATS_SENT));

        int absorbed = dynasty != null ? dynasty.getAbsorbedDynastyIds().size() : 0;
        entries.add(numeric(
                GameUnlocks.ABILITY_ASSIMILATION,
                LanguageStrings.TRIGGER_ASSIMILATION_ABILITY_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_ABSORBED,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_ABSORBED,
                colony.hasUpgrade(GameUnlocks.ABILITY_ASSIMILATION),
                hasDynasty,
                absorbed,
                GameNumbers.TRIGGER_ASSIMILATION_MIN_ABSORBED));

        int rankId = dynasty != null && dynasty.getRank() != null ? dynasty.getRank().getId() : 0;
        entries.add(numeric(
                GameUnlocks.ABILITY_CLONING,
                LanguageStrings.TRIGGER_CLONING_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_RANK,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_RANK,
                colony.hasUpgrade(GameUnlocks.ABILITY_CLONING),
                hasDynasty,
                rankId,
                GameConstants.RANK_EMPIRE.getId()));

        entries.add(numeric(
                GameUnlocks.ROLE_SCOUT,
                LanguageStrings.TRIGGER_SCOUT_ROLE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_SCOUT,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_PLANTS,
                colony.hasUpgrade(GameUnlocks.ROLE_SCOUT),
                true,
                plantHarvestProgress(colony),
                GameNumbers.TRIGGER_SCOUT_PLANT_COLLECTED));

        entries.add(numeric(
                GameUnlocks.ROLE_MINER,
                LanguageStrings.TRIGGER_MINER_ROLE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_MINER,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_TIER3_BUILDINGS,
                colony.hasUpgrade(GameUnlocks.ROLE_MINER),
                colony.hasUpgrade(GameUnlocks.TYPE_SOLDIER),
                GameUnlocks.countDynastyBuildingsOfTier(dynasty, GameConstants.TIER_3),
                GameNumbers.TRIGGER_MINER_TIER3_BUILDINGS));

        boolean parasiticMiteUnlocked = colony.hasUpgrade(GameUnlocks.ABILITY_PARASITIC_MITE_ALERT);
        entries.add(numeric(
                GameUnlocks.ABILITY_PARASITIC_MITE_ALERT,
                LanguageStrings.TRIGGER_PARASITIC_MITE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_PARASITIC_MITE,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_RESOURCES,
                parasiticMiteUnlocked,
                true,
                dynastyStoredResourceProgress(colony),
                GameNumbers.PARASITIC_MITE_RESOURCE_THRESHOLD));

        int warsParticipated = 0;
        if (dynasty != null && world != null && world.getWarService() != null) {
            warsParticipated = world.getWarService().countWarsForDynasty(dynasty.getId());
        }
        entries.add(numeric(
                GameUnlocks.ROLE_COMMANDER,
                LanguageStrings.TRIGGER_COMMANDER_ROLE_TITLE,
                LanguageStrings.TRIGGER_PROGRESS_HINT_COMMANDER,
                LanguageStrings.TRIGGER_PROGRESS_METRIC_WARS,
                colony.hasUpgrade(GameUnlocks.ROLE_COMMANDER),
                hasDynasty,
                warsParticipated,
                GameNumbers.TRIGGER_COMMANDER_MIN_WARS));

        return entries;
    }

    private static int dynastyStoredResourceProgress(Colony colony) {
        if (colony == null) {
            return 0;
        }
        Dynasty dynasty = colony.getDynasty();
        if (dynasty == null || dynasty.getColonies() == null || dynasty.getColonies().isEmpty()) {
            return storedResourceProgress(colony);
        }
        int best = 0;
        for (Colony member : dynasty.getColonies()) {
            best = Math.max(best, storedResourceProgress(member));
        }
        return best;
    }

    private static int storedResourceProgress(Colony colony) {
        if (colony == null || colony.getResourceService() == null) {
            return 0;
        }
        long stored = colony.getResourceService().getStoredResourceTotal(colony);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0L, stored));
    }

    private static int elapsedMonths(World world) {
        if (world == null) {
            return 0;
        }
        return world.getYear() * 12 + world.getMonth();
    }

    private static int plantHarvestProgress(Colony colony) {
        if (colony.getLocationService() == null || colony.getLocationService().getDiscoveredSources() == null) {
            return 0;
        }
        int best = 0;
        for (ResourceSource source : colony.getLocationService().getDiscoveredSources()) {
            if (source.getResourceType() != GameConstants.RESOURCE_PLANT) {
                continue;
            }
            int collected = Math.max(0, source.getInitialQuantity() - source.getQuantity());
            if (collected > best) {
                best = collected;
            }
        }
        return best;
    }

    private static TriggerProgress numeric(
            Upgrade upgrade,
            String titleKey,
            String hintKey,
            String metricKey,
            boolean unlocked,
            boolean gateMet,
            int current,
            int required) {
        int cappedCurrent = Math.max(0, current);
        int cappedRequired = Math.max(1, required);
        return new TriggerProgress(
                upgrade,
                titleKey,
                hintKey,
                metricKey,
                unlocked,
                gateMet,
                Math.min(cappedCurrent, cappedRequired),
                cappedRequired);
    }

    public static final class TriggerProgress {
        private final Upgrade upgrade;
        private final String titleKey;
        private final String hintKey;
        private final String metricKey;
        private final boolean unlocked;
        private final boolean gateMet;
        private final int current;
        private final int required;

        public TriggerProgress(
                Upgrade upgrade,
                String titleKey,
                String hintKey,
                String metricKey,
                boolean unlocked,
                boolean gateMet,
                int current,
                int required) {
            this.upgrade = upgrade;
            this.titleKey = titleKey;
            this.hintKey = hintKey;
            this.metricKey = metricKey;
            this.unlocked = unlocked;
            this.gateMet = gateMet;
            this.current = current;
            this.required = required;
        }

        public Upgrade getUpgrade() {
            return upgrade;
        }

        public String getTitle() {
            return LanguageStrings.get(titleKey);
        }

        public String getHint() {
            return LanguageStrings.get(hintKey);
        }

        public String getMetricLabel() {
            return LanguageStrings.get(metricKey);
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public boolean isGateMet() {
            return gateMet;
        }

        public int getCurrent() {
            return current;
        }

        public int getRequired() {
            return required;
        }

        public double ratio() {
            if (unlocked) {
                return 1.0;
            }
            return required <= 0 ? 0.0 : (double) current / (double) required;
        }
    }
}
