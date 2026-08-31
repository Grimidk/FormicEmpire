package com.grimidk.formicempire.classes.constants.critter;

import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BugRoleTest {

    @Test
    void speciesExposeGameplayBugRoles() {
        assertNull(GameConstants.TYPE_ANT.getBugRole());
        assertTrue(GameConstants.TYPE_APHID.hasBugRole(BugRole.PET));
        assertTrue(GameConstants.TYPE_SYMBIOTIC_MITE.hasBugRole(BugRole.PET));
        assertTrue(GameConstants.TYPE_DERMESTID.hasBugRole(BugRole.PET));
        assertTrue(GameConstants.TYPE_PARASITE_ANT.hasBugRole(BugRole.PARASITE));
        assertTrue(GameConstants.TYPE_PARASITIC_MITE.hasBugRole(BugRole.PARASITE));
        assertTrue(GameConstants.TYPE_COCKROACH.hasBugRole(BugRole.HUNT));
        assertTrue(GameConstants.TYPE_BOMBARDIER_BEETLE.hasBugRole(BugRole.HUNT));
        assertTrue(GameConstants.TYPE_ANT_LION.hasBugRole(BugRole.INVASION));
        assertFalse(GameConstants.TYPE_ANT_LION.hasBugRole(BugRole.HUNT));
        assertTrue(GameConstants.TYPE_SPIDER.hasBugRole(BugRole.HUNT));
        assertTrue(GameConstants.TYPE_TARANTULA.hasBugRole(BugRole.HUNT));
        assertFalse(GameConstants.TYPE_COCKROACH.hasBugRole(BugRole.INVASION));
    }

    @Test
    void nativeBiomeListsFollowBugRoles() {
        assertEquals(3, GameConstants.BIOME_PLAINS.getNativeBugs().size());
        assertTrue(GameConstants.BIOME_PLAINS.getNativeBugs().stream().allMatch(s -> s.hasBugRole(BugRole.PET)));
        assertTrue(GameConstants.BIOME_JUNGLE.getNativeParasites().stream().allMatch(s -> s.hasBugRole(BugRole.PARASITE)));
    }

    @Test
    void activeHuntSpeciesAreCockroachOnlyForNow() {
        assertEquals(1, GameConstants.getActiveHuntSpeciesForBiome(GameConstants.BIOME_URBAN).size());
        assertEquals(GameConstants.TYPE_COCKROACH,
                GameConstants.getActiveHuntSpeciesForBiome(GameConstants.BIOME_URBAN).get(0));
        assertTrue(GameConstants.getActiveHuntSpeciesForBiome(GameConstants.BIOME_DESERT).isEmpty());
    }

    @Test
    void huntSpeciesAreFilteredByBiome() {
        assertEquals(4, GameConstants.getHuntSpeciesForBiome(GameConstants.BIOME_URBAN).size());
        assertTrue(GameConstants.getHuntSpeciesForBiome(GameConstants.BIOME_URBAN)
                .stream()
                .allMatch(s -> s.hasBugRole(BugRole.HUNT)));
        assertTrue(GameConstants.getHuntSpeciesForBiome(GameConstants.BIOME_DESERT).isEmpty());
        assertTrue(GameConstants.isHuntBiome(GameConstants.BIOME_FOREST));
    }
}
