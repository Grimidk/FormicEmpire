package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldHistoryService {

    public static final int MAX_EVENTS = 2000;

    private final World world;
    private final List<WorldHistoryEvent> events = new ArrayList<>();

    private boolean recordingEnabled = true;

    public WorldHistoryService(World world) {
        this.world = world;
    }

    public void setRecordingEnabled(boolean recordingEnabled) {
        this.recordingEnabled = recordingEnabled;
    }

    public void clear() {
        events.clear();
    }

    public void record(WorldHistoryEventType type, String messageKey, String... args) {
        record(type, messageKey, -1, -1, -1, args);
    }

    public void record(WorldHistoryEventType type, String messageKey,
            int relatedDynastyId, int relatedColonyId, int relatedWarId, String... args) {
        if (!recordingEnabled || type == null || messageKey == null || messageKey.isEmpty()) {
            return;
        }
        List<String> argList = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                argList.add(arg != null ? arg : "");
            }
        }
        int year = world != null ? world.getYear() : 0;
        int month = world != null ? world.getMonth() : 1;
        int day = world != null ? world.getDay() : 1;
        int hour = world != null ? world.getHour() : 0;
        int minute = world != null ? world.getMinute() : 0;
        events.add(new WorldHistoryEvent(
                year, month, day, hour, minute,
                type, messageKey, argList,
                relatedDynastyId, relatedColonyId, relatedWarId));
        trimToCap();
    }

    public List<WorldHistoryEvent> getEvents() {
        List<WorldHistoryEvent> copy = new ArrayList<>(events);
        Collections.reverse(copy);
        return copy;
    }

    public List<WorldHistoryEvent> getEvents(WorldHistoryEventType.WorldHistoryCategory category) {
        if (category == null) {
            return getEvents();
        }
        List<WorldHistoryEvent> filtered = new ArrayList<>();
        for (int i = events.size() - 1; i >= 0; i--) {
            WorldHistoryEvent event = events.get(i);
            if (event.getType() != null && event.getType().getCategory() == category) {
                filtered.add(event);
            }
        }
        return filtered;
    }

    public int size() {
        return events.size();
    }

    public List<Savefile.SavedWorldHistoryEvent> toSaved() {
        List<Savefile.SavedWorldHistoryEvent> saved = new ArrayList<>(events.size());
        for (WorldHistoryEvent event : events) {
            Savefile.SavedWorldHistoryEvent row = new Savefile.SavedWorldHistoryEvent();
            row.year = event.getYear();
            row.month = event.getMonth();
            row.day = event.getDay();
            row.hour = event.getHour();
            row.minute = event.getMinute();
            row.type = event.getType() != null ? event.getType().name() : "";
            row.messageKey = event.getMessageKey();
            row.args = new ArrayList<>(event.getArgs());
            row.relatedDynastyId = event.getRelatedDynastyId();
            row.relatedColonyId = event.getRelatedColonyId();
            row.relatedWarId = event.getRelatedWarId();
            saved.add(row);
        }
        return saved;
    }

    public void loadFromSave(List<Savefile.SavedWorldHistoryEvent> savedEvents) {
        events.clear();
        if (savedEvents == null) {
            return;
        }
        for (Savefile.SavedWorldHistoryEvent row : savedEvents) {
            if (row == null) {
                continue;
            }
            WorldHistoryEventType type = WorldHistoryEventType.fromName(row.type);
            if (type == null || row.messageKey == null || row.messageKey.isEmpty()) {
                continue;
            }
            events.add(new WorldHistoryEvent(
                    row.year, row.month, row.day, row.hour, row.minute,
                    type, row.messageKey,
                    row.args != null ? row.args : List.of(),
                    row.relatedDynastyId, row.relatedColonyId, row.relatedWarId));
        }
        trimToCap();
    }

    private void trimToCap() {
        while (events.size() > MAX_EVENTS) {
            events.remove(0);
        }
    }
}
