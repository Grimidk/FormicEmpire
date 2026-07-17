package com.grimidk.formicempire.classes.entities.services.world;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldHistoryServiceTest {

    @Test
    void recordFilterAndNewestFirst() {
        World world = new World();
        world.setYear(1);
        world.setMonth(2);
        world.setDay(3);
        world.setHour(4);
        world.setMinute(5);
        WorldHistoryService history = world.getHistoryService();

        history.record(WorldHistoryEventType.COLONY_FOUNDED, LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                WorldHistoryEvent.plainArg("A"), WorldHistoryEvent.plainArg("B"));
        history.record(WorldHistoryEventType.WAR_STARTED, LanguageStrings.HISTORY_WAR_STARTED_FMT,
                WorldHistoryEvent.plainArg("1"), WorldHistoryEvent.plainArg("2"), WorldHistoryEvent.plainArg("9"));
        history.record(WorldHistoryEventType.NUPTIAL_FLIGHT, LanguageStrings.HISTORY_NUPTIAL_FLIGHT_FMT,
                WorldHistoryEvent.plainArg("C"), WorldHistoryEvent.plainArg("1"), WorldHistoryEvent.plainArg("0"));

        List<WorldHistoryEvent> all = history.getEvents();
        assertEquals(3, all.size());
        assertEquals(WorldHistoryEventType.NUPTIAL_FLIGHT, all.get(0).getType());

        List<WorldHistoryEvent> wars = history.getEvents(WorldHistoryEventType.WorldHistoryCategory.WAR);
        assertEquals(1, wars.size());
        assertEquals(WorldHistoryEventType.WAR_STARTED, wars.get(0).getType());
        assertEquals("04:05 03/02/0001", wars.get(0).formatDateTime());
    }

    @Test
    void softCapTrimsOldest() {
        World world = new World();
        WorldHistoryService history = world.getHistoryService();
        for (int i = 0; i < WorldHistoryService.MAX_EVENTS + 25; i++) {
            history.record(WorldHistoryEventType.COLONY_FOUNDED, LanguageStrings.HISTORY_COLONY_FOUNDED_FMT,
                    WorldHistoryEvent.plainArg("c" + i), WorldHistoryEvent.plainArg("d"));
        }
        assertEquals(WorldHistoryService.MAX_EVENTS, history.size());
        List<WorldHistoryEvent> events = history.getEvents();
        assertTrue(events.get(events.size() - 1).getArgs().get(0).contains("c25")
                || events.get(events.size() - 1).getArgs().get(0).equals("c25"));
    }

    @Test
    void saveRoundTripPreservesEvents() {
        World world = new World();
        world.setYear(10);
        world.setMonth(6);
        world.setDay(15);
        WorldHistoryService history = world.getHistoryService();
        history.record(WorldHistoryEventType.DYNASTY_DIED, LanguageStrings.HISTORY_DYNASTY_DIED_FMT,
                7, -1, -1,
                WorldHistoryEvent.dynastyArg(7),
                WorldHistoryEvent.keyArg(LanguageStrings.HISTORY_CAUSE_EXTINCT));

        List<Savefile.SavedWorldHistoryEvent> saved = history.toSaved();
        assertEquals(1, saved.size());
        assertEquals("DYNASTY_DIED", saved.get(0).type);

        WorldHistoryService restored = new WorldHistoryService(world);
        restored.loadFromSave(saved);
        assertEquals(1, restored.size());
        WorldHistoryEvent event = restored.getEvents().get(0);
        assertEquals(WorldHistoryEventType.DYNASTY_DIED, event.getType());
        assertEquals(10, event.getYear());
        assertEquals(7, event.getRelatedDynastyId());
        assertEquals(LanguageStrings.HISTORY_DYNASTY_DIED_FMT, event.getMessageKey());
    }

    @Test
    void recordingDisabledSkipsEvents() {
        World world = new World();
        WorldHistoryService history = world.getHistoryService();
        history.setRecordingEnabled(false);
        history.record(WorldHistoryEventType.WAR_STARTED, LanguageStrings.HISTORY_WAR_STARTED_FMT,
                WorldHistoryEvent.plainArg("a"), WorldHistoryEvent.plainArg("b"), WorldHistoryEvent.plainArg("1"));
        assertEquals(0, history.size());
        history.setRecordingEnabled(true);
        history.record(WorldHistoryEventType.WAR_STARTED, LanguageStrings.HISTORY_WAR_STARTED_FMT,
                WorldHistoryEvent.plainArg("a"), WorldHistoryEvent.plainArg("b"), WorldHistoryEvent.plainArg("1"));
        assertEquals(1, history.size());
    }

    @Test
    void involvesDynastyMatchesRelatedIdAndArgs() {
        World world = new World();
        world.getHistoryService().record(WorldHistoryEventType.DYNASTY_FORMED, LanguageStrings.HISTORY_DYNASTY_FORMED_FMT,
                3, -1, -1, WorldHistoryEvent.dynastyArg(3));
        world.getHistoryService().record(WorldHistoryEventType.DYNASTY_ABSORBED, LanguageStrings.HISTORY_DYNASTY_ABSORBED_FMT,
                1, -1, -1, WorldHistoryEvent.dynastyArg(1), WorldHistoryEvent.dynastyArg(9));

        List<WorldHistoryEvent> events = world.getHistoryService().getEvents();
        assertTrue(events.get(1).involvesDynasty(world, 3));
        assertFalse(events.get(1).involvesDynasty(world, 1));
        assertTrue(events.get(0).involvesDynasty(world, 1));
        assertTrue(events.get(0).involvesDynasty(world, 9));
    }
}
