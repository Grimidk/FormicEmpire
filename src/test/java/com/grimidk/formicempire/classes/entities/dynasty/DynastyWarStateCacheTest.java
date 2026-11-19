package com.grimidk.formicempire.classes.entities.dynasty;

import com.grimidk.formicempire.classes.infrasctructure.Savefile;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynastyWarStateCacheTest {

    @Test
    void isAtWarTracksAllDiplomaticModifierMutationsAndExpiry() {
        Dynasty dynasty = new Dynasty(1, "A", false, GameConstants.SPECIES_OMNI);
        String warKey = GameConstants.DIPLO_MODIFIER_WAR.getNameKey();
        String tradeKey = GameConstants.DIPLO_MODIFIER_TRADE.getNameKey();

        assertFalse(dynasty.isAtWar());

        dynasty.putDiplomaticModifierRemainingDays(2, warKey, GameNumbers.MODIFIER_PERMANENT);
        assertTrue(dynasty.isAtWar());

        dynasty.putDiplomaticModifierRemainingDays(2, warKey, GameNumbers.MODIFIER_PERMANENT);
        assertTrue(dynasty.isAtWar());

        dynasty.putDiplomaticModifierRemainingDays(3, warKey, 2);
        dynasty.putDiplomaticModifierRemainingDays(2, tradeKey, 5);

        dynasty.removeDiplomaticModifierKey(2, warKey);
        assertTrue(dynasty.isAtWar());

        dynasty.tickDiplomaticModifierDays();
        assertTrue(dynasty.isAtWar());

        dynasty.tickDiplomaticModifierDays();
        assertFalse(dynasty.isAtWar());

        dynasty.putDiplomaticModifierRemainingDays(4, warKey, GameNumbers.MODIFIER_PERMANENT);
        assertTrue(dynasty.isAtWar());
        dynasty.clearDiplomaticModifierKey(4);
        assertFalse(dynasty.isAtWar());

        dynasty.putDiplomaticModifierRemainingDays(5, warKey, GameNumbers.MODIFIER_PERMANENT);
        assertTrue(dynasty.isAtWar());
        dynasty.setDiplomaticModifierKey(5, null);
        assertFalse(dynasty.isAtWar());
    }

    @Test
    void isAtWarIsPopulatedOnSaveLoad() {
        Savefile.SavedDynasty saved = new Savefile.SavedDynasty();
        saved.id = 10;
        saved.name = "Loaded";
        saved.speciesId = GameConstants.SPECIES_OMNI.getId();
        Map<String, Integer> mods = new HashMap<>();
        mods.put(GameConstants.DIPLO_MODIFIER_WAR.getNameKey(), GameNumbers.MODIFIER_PERMANENT);
        saved.diplomaticModifierRemainingDays.put("11", mods);

        Dynasty loaded = new Dynasty(saved);
        assertTrue(loaded.isAtWar());

        loaded.removeDiplomaticModifierKey(11, GameConstants.DIPLO_MODIFIER_WAR.getNameKey());
        assertFalse(loaded.isAtWar());
    }
}
