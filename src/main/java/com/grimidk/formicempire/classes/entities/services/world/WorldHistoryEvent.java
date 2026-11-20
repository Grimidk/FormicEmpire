package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.dynasty.Rank;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.dynasty.War;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WorldHistoryEvent {

    public static final String ARG_DYNASTY = "d:";
    public static final String ARG_COLONY = "c:";
    public static final String ARG_KEY = "k:";
    public static final String ARG_ASSIMILATION = "a:";
    public static final String ARG_RANK = "r:";

    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final WorldHistoryEventType type;
    private final String messageKey;
    private final List<String> args;
    private final int relatedDynastyId;
    private final int relatedColonyId;
    private final int relatedWarId;

    public WorldHistoryEvent(int year, int month, int day, int hour, int minute,
            WorldHistoryEventType type, String messageKey, List<String> args,
            int relatedDynastyId, int relatedColonyId, int relatedWarId) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.type = type;
        this.messageKey = messageKey != null ? messageKey : "";
        this.args = args != null ? Collections.unmodifiableList(new ArrayList<>(args)) : List.of();
        this.relatedDynastyId = relatedDynastyId;
        this.relatedColonyId = relatedColonyId;
        this.relatedWarId = relatedWarId;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getDay() { return day; }
    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public WorldHistoryEventType getType() { return type; }
    public String getMessageKey() { return messageKey; }
    public List<String> getArgs() { return args; }
    public int getRelatedDynastyId() { return relatedDynastyId; }
    public int getRelatedColonyId() { return relatedColonyId; }
    public int getRelatedWarId() { return relatedWarId; }

    public boolean involvesDynasty(World world, int dynastyId) {
        if (dynastyId <= 0) {
            return false;
        }
        if (relatedDynastyId == dynastyId) {
            return true;
        }
        String dynastyToken = ARG_DYNASTY + dynastyId;
        for (String arg : args) {
            if (dynastyToken.equals(arg)) {
                return true;
            }
        }
        if (relatedWarId > 0 && world != null && world.getWarService() != null) {
            War war = world.getWarService().findWarById(relatedWarId);
            if (war != null && war.involves(dynastyId)) {
                return true;
            }
        }
        return false;
    }

    public String formatDateTime() {
        return String.format("%02d:%02d %02d/%02d/%04d", hour, minute, day, month, year);
    }

    public String formatCategoryLabel() {
        if (type == null) {
            return "";
        }
        return LanguageStrings.get(type.getCategory().getLabelKey());
    }

    public String formatMessage(World world) {
        return formatMessage(world, null);
    }

    public String formatMessage(World world, Dynasty viewer) {
        if (messageKey.isEmpty()) {
            return "";
        }
        Object[] resolved = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) {
            resolved[i] = resolveArg(world, args.get(i), viewer);
        }
        return LanguageStrings.format(messageKey, resolved);
    }

    public boolean isVisibleToViewer(World world, Dynasty viewer) {
        if (world == null || viewer == null) {
            return true;
        }
        boolean hasEntityRef = false;
        if (relatedDynastyId > 0) {
            hasEntityRef = true;
            Dynasty related = world.findDynastyById(relatedDynastyId);
            if (isDynastyVisible(world, viewer, related)) {
                return true;
            }
        }
        if (relatedColonyId > 0) {
            hasEntityRef = true;
            Colony colony = findColony(world, relatedColonyId);
            if (colony != null && isDynastyVisible(world, viewer, colony.getDynasty())) {
                return true;
            }
        }
        if (relatedWarId > 0 && world.getWarService() != null) {
            War war = world.getWarService().findWarById(relatedWarId);
            if (war != null) {
                hasEntityRef = true;
                Dynasty dA = world.findDynastyById(war.getDynastyIdA());
                Dynasty dB = world.findDynastyById(war.getDynastyIdB());
                if (isDynastyVisible(world, viewer, dA) || isDynastyVisible(world, viewer, dB)) {
                    return true;
                }
            }
        }
        for (String arg : args) {
            if (arg == null || arg.isEmpty()) {
                continue;
            }
            if (arg.startsWith(ARG_DYNASTY)) {
                hasEntityRef = true;
                int id = parseId(arg, ARG_DYNASTY.length());
                Dynasty dynasty = world.findDynastyById(id);
                if (isDynastyVisible(world, viewer, dynasty)) {
                    return true;
                }
            } else if (arg.startsWith(ARG_COLONY)) {
                hasEntityRef = true;
                int id = parseId(arg, ARG_COLONY.length());
                Colony colony = findColony(world, id);
                if (colony != null && isDynastyVisible(world, viewer, colony.getDynasty())) {
                    return true;
                }
            }
        }
        return !hasEntityRef;
    }

    private static boolean isDynastyVisible(World world, Dynasty viewer, Dynasty target) {
        if (viewer == null) {
            return true;
        }
        if (target == null) {
            return false;
        }
        if (target == viewer) {
            return true;
        }
        if (world.getHexes() == null || world.getHexes().isEmpty()) {
            return true;
        }
        if (viewer.getKnownHexCount() == 0) {
            viewer.initKnownHexes(world);
        }
        return viewer.hasDiscoveredDynasty(target, world);
    }

    public static String dynastyArg(int dynastyId) {
        return ARG_DYNASTY + dynastyId;
    }

    public static String colonyArg(int colonyId) {
        return ARG_COLONY + colonyId;
    }

    public static String keyArg(String languageKey) {
        return ARG_KEY + (languageKey != null ? languageKey : "");
    }

    public static String assimilationArg(int assimilationId) {
        return ARG_ASSIMILATION + assimilationId;
    }

    public static String rankArg(Rank rank) {
        if (rank == null || rank.getNameKey() == null) {
            return ARG_RANK;
        }
        return ARG_RANK + rank.getNameKey();
    }

    public static String plainArg(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private static String resolveArg(World world, String arg, Dynasty viewer) {
        if (arg == null || arg.isEmpty()) {
            return "";
        }
        if (arg.startsWith(ARG_DYNASTY)) {
            int id = parseId(arg, ARG_DYNASTY.length());
            if (world != null) {
                Dynasty dynasty = world.findDynastyById(id);
                if (dynasty != null) {
                    if (viewer != null && !isDynastyVisible(world, viewer, dynasty)) {
                        return LanguageStrings.get(LanguageStrings.DIPLO_UNKNOWN_DYNASTY);
                    }
                    return dynasty.getName();
                }
            }
            return LanguageStrings.format(LanguageStrings.HISTORY_UNKNOWN_DYNASTY_FMT, id);
        }
        if (arg.startsWith(ARG_COLONY)) {
            int id = parseId(arg, ARG_COLONY.length());
            if (world != null) {
                Colony colony = findColony(world, id);
                if (colony != null) {
                    if (viewer != null && colony.getDynasty() != null
                            && !isDynastyVisible(world, viewer, colony.getDynasty())) {
                        return LanguageStrings.get(LanguageStrings.DIPLO_UNKNOWN_DYNASTY);
                    }
                    return colony.getName();
                }
            }
            return LanguageStrings.format(LanguageStrings.HISTORY_UNKNOWN_COLONY_FMT, id);
        }
        if (arg.startsWith(ARG_KEY)) {
            String key = arg.substring(ARG_KEY.length());
            return key.isEmpty() ? "" : LanguageStrings.get(key);
        }
        if (arg.startsWith(ARG_ASSIMILATION)) {
            int id = parseId(arg, ARG_ASSIMILATION.length());
            for (Assimilation assimilation : GameUnlocks.getAssimilations()) {
                if (assimilation.getId() == id) {
                    return assimilation.getName();
                }
            }
            return LanguageStrings.format(LanguageStrings.HISTORY_UNKNOWN_ASSIMILATION_FMT, id);
        }
        if (arg.startsWith(ARG_RANK)) {
            String key = arg.substring(ARG_RANK.length());
            if (key.isEmpty()) {
                return "";
            }
            for (Rank rank : GameConstants.getColonyRanks()) {
                if (key.equals(rank.getNameKey())) {
                    return rank.getName();
                }
            }
            return LanguageStrings.get(key);
        }
        return arg;
    }

    private static int parseId(String arg, int prefixLen) {
        try {
            return Integer.parseInt(arg.substring(prefixLen));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Colony findColony(World world, int colonyId) {
        if (world == null || world.getHexes() == null) {
            return null;
        }
        for (var hex : world.getHexes()) {
            Colony colony = hex.getColony();
            if (colony != null && colony.getId() == colonyId) {
                return colony;
            }
        }
        if (world.getDynastys() != null) {
            for (Dynasty dynasty : world.getDynastys()) {
                for (Colony colony : dynasty.getColonies()) {
                    if (colony.getId() == colonyId) {
                        return colony;
                    }
                }
            }
        }
        return null;
    }
}
