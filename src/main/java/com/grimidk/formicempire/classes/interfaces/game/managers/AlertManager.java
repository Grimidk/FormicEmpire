package com.grimidk.formicempire.classes.interfaces.game.managers;

import com.grimidk.formicempire.classes.constants.unlocks.Building;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastyRebellionService;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.ColonyLogPrefixes;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel;
import com.grimidk.formicempire.classes.interfaces.game.gamepanels.AlertPanel.Alert;
import com.grimidk.formicempire.classes.interfaces.ui.AssetStyles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

public class AlertManager {
    private static final Pattern LEADING_NUMBER = Pattern.compile("(\\d+)");

    private Dynasty dynasty;
    private final AlertPanel panel;
    private final List<Alert> activeAlerts = new ArrayList<>();

    private final int durationDefault = 10000;

    public AlertManager(Dynasty dynasty, AlertPanel panel) {
        this.dynasty = dynasty;
        this.panel = panel;
    }

    public AlertManager(Colony playerColony, AlertPanel panel) {
        this(playerColony != null ? playerColony.getDynasty() : null, panel);
    }

    public void setDynasty(Dynasty dynasty) {
        this.dynasty = dynasty;
    }

    private boolean isPlayerDynastyReady() {
        return dynasty != null && dynasty.isPlayer() && !dynasty.isDefeated();
    }

    private List<Colony> playerColonies() {
        if (!isPlayerDynastyReady()) {
            return List.of();
        }
        return dynasty.getColonies();
    }

    private List<Colony> coloniesCapitalFirst() {
        List<Colony> colonies = playerColonies();
        if (colonies.isEmpty()) {
            return colonies;
        }
        Colony capital = dynasty.getCapital();
        if (capital == null || colonies.get(0) == capital) {
            return colonies;
        }
        List<Colony> ordered = new ArrayList<>(colonies.size());
        ordered.add(capital);
        for (Colony colony : colonies) {
            if (colony != capital) {
                ordered.add(colony);
            }
        }
        return ordered;
    }

    private boolean multiColony() {
        return playerColonies().size() > 1;
    }

    private String withColonyContext(String base, Colony colony) {
        if (colony == null || !multiColony()) {
            return base;
        }
        return base + " — " + colony.getName();
    }

    private static String stripPrefix(String msg, String prefix) {
        if (!msg.startsWith(prefix)) {
            return msg;
        }
        String rest = msg.substring(prefix.length());
        if (rest.startsWith(" ")) {
            return rest.substring(1);
        }
        return rest;
    }

    private static Icon iconOf(ImageIcon source) {
        return AlertPanel.scaleAlertIcon(source);
    }

    private static Icon iconForKey(String key) {
        switch (key) {
            case "DEATH":
            case "DEAD":
                return iconOf(GameConstants.STATUS_DEAD.getIcon());
            case "STARVE":
                return iconOf(GameConstants.ICON_STAT_NET_FOOD);
            case "RESEARCH":
                return iconOf(GameConstants.ICON_RESEARCH);
            case "BUILD":
            case "SUCC":
                return iconOf(GameConstants.ROLE_BUILDER.getIcon());
            case "COMP":
                return iconOf(GameConstants.ROOM_PASSIVE_COMPOSTER);
            case "REBEL":
                return iconOf(GameConstants.ICON_STAT_LOYALTY);
            case "WAR":
                return iconOf(GameConstants.ICON_STAT_MILITARY_POWER);
            case "TRADE":
                return iconOf(GameConstants.METHOD_LAND.getIcon());
            case "NUPTIAL":
            case "PROMO":
                return iconOf(GameConstants.TYPE_QUEEN.getIcon());
            case "WARN":
                return iconOf(GameConstants.REPUTATION_WARY.getIcon());
            case "FAIL":
                return iconOf(GameConstants.REPUTATION_AGGRESSIVE.getIcon());
            case "DYN":
                return iconOf(GameConstants.ICON_STAT_REPUTATION);
            case "AUTO":
                return iconOf(GameConstants.ICON_SOCIALISM);
            case "INFO":
            default:
                return iconOf(GameConstants.REPUTATION_NEUTRAL.getIcon());
        }
    }

    private static String firstNumberFormatted(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = LEADING_NUMBER.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return AssetStyles.formatNumber(Long.parseLong(matcher.group(1)));
        } catch (NumberFormatException ignored) {
            return matcher.group(1);
        }
    }

    public void checkStatus() {
        if (!isPlayerDynastyReady()) {
            return;
        }
        Iterator<Alert> it = activeAlerts.iterator();
        while (it.hasNext()) {
            if (it.next().isExpired()) {
                it.remove();
            }
        }

        for (Colony colony : playerColonies()) {
            List<String> events = colony.consumeEvents();
            for (String msg : events) {
                ingestLogEvent(msg);
            }
        }

        checkResourceWarning();
        checkAvailableResearch();
        checkAvailableBuildings();
        checkBodyPile();

        SwingUtilities.invokeLater(() -> panel.updateAlerts(new ArrayList<>(activeAlerts)));
    }

    public void checkRebellionRisk(World world, TradeManager tradeManager) {
        if (!isPlayerDynastyReady() || world == null) {
            return;
        }
        for (Colony candidate : dynasty.getColonies()) {
            if (candidate.isCapital()) {
                continue;
            }
            if (DynastyRebellionService.isRebellionRisk(candidate, tradeManager, world)) {
                String name = candidate.getName();
                addAlert("REBEL", name,
                        LanguageStrings.format(LanguageStrings.ALERT_REBELLION_RISK_FMT, name),
                        durationDefault);
                return;
            }
        }
    }

    public void ingestLogEvent(String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        if (msg.startsWith(ColonyLogPrefixes.DEATH)) {
            String body = stripPrefix(msg, ColonyLogPrefixes.DEATH);
            String count = firstNumberFormatted(body);
            addAlert("DEATH", count != null ? count : body, body, durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.WARNING)) {
            addAlert("WARN", stripPrefix(msg, ColonyLogPrefixes.WARNING), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.SUCCESS)) {
            String body = stripPrefix(msg, ColonyLogPrefixes.SUCCESS);
            addAlert("SUCC", body, durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.COMPOST)) {
            String body = stripPrefix(msg, ColonyLogPrefixes.COMPOST);
            String count = firstNumberFormatted(body);
            addAlert("COMP", count != null ? count : body, body, durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.NUPTIAL)) {
            String body = stripPrefix(msg, ColonyLogPrefixes.NUPTIAL);
            String count = firstNumberFormatted(body);
            String shortMsg = count != null
                    ? count
                    : LanguageStrings.get(LanguageStrings.ALERT_NUPTIAL_FLIGHT);
            addAlert("NUPTIAL", shortMsg, body, durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.TRADE)) {
            addAlert("TRADE", stripPrefix(msg, ColonyLogPrefixes.TRADE), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.DYNASTY)) {
            addAlert("DYN", stripPrefix(msg, ColonyLogPrefixes.DYNASTY), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.WAR)) {
            addAlert("WAR", stripPrefix(msg, ColonyLogPrefixes.WAR), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.FAILURE)) {
            addAlert("FAIL", stripPrefix(msg, ColonyLogPrefixes.FAILURE), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.AUTOMATION)) {
            addAlert("AUTO", stripPrefix(msg, ColonyLogPrefixes.AUTOMATION), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.PROMOTION)) {
            addAlert("PROMO", stripPrefix(msg, ColonyLogPrefixes.PROMOTION), durationDefault);
        } else if (msg.startsWith(ColonyLogPrefixes.INFO)) {
            addAlert("INFO", stripPrefix(msg, ColonyLogPrefixes.INFO), durationDefault);
        } else {
            addAlert("INFO", msg, durationDefault);
        }
    }

    private static String detailKeyFor(String alertKey) {
        switch (alertKey) {
            case "DEATH":
                return LanguageStrings.ALERT_DETAIL_DEATH;
            case "DEAD":
                return LanguageStrings.ALERT_DETAIL_DEAD;
            case "STARVE":
                return LanguageStrings.ALERT_DETAIL_STARVE;
            case "RESEARCH":
                return LanguageStrings.ALERT_DETAIL_RESEARCH;
            case "BUILD":
                return LanguageStrings.ALERT_DETAIL_BUILD;
            case "SUCC":
                return LanguageStrings.ALERT_DETAIL_SUCC;
            case "COMP":
                return LanguageStrings.ALERT_DETAIL_COMP;
            case "NUPTIAL":
                return LanguageStrings.ALERT_DETAIL_NUPTIAL;
            case "REBEL":
                return LanguageStrings.ALERT_DETAIL_REBEL;
            case "WAR":
                return LanguageStrings.ALERT_DETAIL_WAR;
            case "TRADE":
                return LanguageStrings.ALERT_DETAIL_TRADE;
            case "DYN":
                return LanguageStrings.ALERT_DETAIL_DYN;
            case "WARN":
                return LanguageStrings.ALERT_DETAIL_WARN;
            case "FAIL":
                return LanguageStrings.ALERT_DETAIL_FAIL;
            case "AUTO":
                return LanguageStrings.ALERT_DETAIL_AUTO;
            case "PROMO":
                return LanguageStrings.ALERT_DETAIL_PROMO;
            case "INFO":
            default:
                return LanguageStrings.ALERT_DETAIL_INFO;
        }
    }

    private static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String composeTooltip(String alertKey, String detail) {
        String category = LanguageStrings.get(detailKeyFor(alertKey));
        String cleanedDetail = detail == null ? "" : detail.trim();
        if (cleanedDetail.isEmpty() || cleanedDetail.equals(category)) {
            return "<html>" + escapeHtml(category) + "</html>";
        }
        return "<html>" + escapeHtml(category) + "<br>" + escapeHtml(cleanedDetail) + "</html>";
    }

    private void addAlert(String key, String msg, long duration) {
        addAlert(key, msg, msg, iconForKey(key), duration);
    }

    private void addAlert(String key, String msg, String detail, long duration) {
        addAlert(key, msg, detail, iconForKey(key), duration);
    }

    private void addAlert(String key, String msg, String detail, Icon icon, long duration) {
        String tooltip = composeTooltip(key, detail);
        for (int i = 0; i < activeAlerts.size(); i++) {
            Alert existing = activeAlerts.get(i);
            if (!existing.key.equals(key)) {
                continue;
            }
            existing.message = msg;
            existing.tooltip = tooltip;
            existing.icon = icon;
            existing.bumpExpiry(duration);
            if (i != 0) {
                activeAlerts.remove(i);
                activeAlerts.add(0, existing);
            }
            return;
        }
        activeAlerts.add(0, new Alert(key, msg, icon, tooltip, duration));
    }

    public boolean hasActiveAlert(String key) {
        for (Alert alert : activeAlerts) {
            if (alert.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    Alert findAlert(String key) {
        for (Alert alert : activeAlerts) {
            if (alert.key.equals(key)) {
                return alert;
            }
        }
        return null;
    }

    private static boolean isStarving(Colony colony) {
        int production = colony.getTotalProduction();
        int consumption = colony.getTotalConsumption();
        return consumption > production && colony.getMushrooms() < consumption * 24L;
    }

    private void checkResourceWarning() {
        for (Colony colony : coloniesCapitalFirst()) {
            if (isStarving(colony)) {
                String msg = LanguageStrings.get(LanguageStrings.ALERT_STARVATION_RISK);
                addAlert("STARVE", msg, withColonyContext(msg, colony), durationDefault);
                return;
            }
        }
    }

    private void checkAvailableResearch() {
        for (Colony colony : coloniesCapitalFirst()) {
            if (colony.hasAffordableResearch()) {
                String msg = LanguageStrings.get(LanguageStrings.ALERT_NEW_RESEARCH);
                addAlert("RESEARCH", msg, withColonyContext(msg, colony), durationDefault);
                return;
            }
        }
    }

    private void checkAvailableBuildings() {
        for (Colony colony : coloniesCapitalFirst()) {
            Building building = colony.getAffordableBuildingForAlert();
            if (building != null) {
                String name = building.getName();
                Icon icon = building.getIcon() != null
                        ? iconOf(building.getIcon())
                        : iconForKey("BUILD");
                addAlert("BUILD", name,
                        withColonyContext(
                                LanguageStrings.format(LanguageStrings.ALERT_CAN_BUILD_FMT, name),
                                colony),
                        icon, durationDefault);
                return;
            }
        }
    }

    private void checkBodyPile() {
        for (Colony colony : coloniesCapitalFirst()) {
            int deadCount = colony.getDeadAnts().size();
            if (deadCount >= 500) {
                String count = AssetStyles.formatNumber(deadCount);
                addAlert("DEAD", count,
                        withColonyContext(
                                LanguageStrings.format(LanguageStrings.ALERT_BODY_PILE_FMT, count),
                                colony),
                        durationDefault);
                return;
            }
        }
    }
}
