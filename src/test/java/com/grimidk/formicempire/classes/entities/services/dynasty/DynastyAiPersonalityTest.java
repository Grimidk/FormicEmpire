package com.grimidk.formicempire.classes.entities.services.dynasty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.constants.dynasty.AiPersonality;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.World;
import com.grimidk.formicempire.classes.infrasctructure.managers.TradeManager;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

class DynastyAiPersonalityTest {

    private DynastyAiService aiService;
    private TradeManager tradeManager;
    private World world;

    @BeforeEach
    void setUp() {
        aiService = new DynastyAiService();
        tradeManager = new TradeManager();
        world = new World();
    }

    @Test
    void npcDynastyGetsRandomPersonalityPlayerDoesNot() {
        Dynasty npc = new Dynasty(1, "NPC", false, GameConstants.SPECIES_OMNI);
        Dynasty player = new Dynasty(2, "Player", true, GameConstants.SPECIES_OMNI);
        assertNotNull(npc.getAiPersonality());
        assertNull(player.getAiPersonality());
    }

    @Test
    void militaristPersonalityNerfsEffectiveReputation() {
        Dynasty viewer = new Dynasty(1, "Viewer", true, GameConstants.SPECIES_OMNI);
        Dynasty militarist = new Dynasty(2, "War Ants", false, GameConstants.SPECIES_OMNI);
        militarist.setAiPersonality(AiPersonality.MILITARIST);
        viewer.setDiplomaticReputation(militarist.getId(), GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION);
        militarist.setDiplomaticReputation(viewer.getId(), GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION);

        int effective = viewer.getDiplomacyService().getEffectiveDiplomaticReputation(militarist, world);
        assertEquals(GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION + GameNumbers.AI_MILITARIST_REPUTATION_DELTA,
                effective);
    }

    @Test
    void pacifistPersonalityBuffsEffectiveReputation() {
        Dynasty viewer = new Dynasty(1, "Viewer", true, GameConstants.SPECIES_OMNI);
        Dynasty pacifist = new Dynasty(2, "Peace Ants", false, GameConstants.SPECIES_OMNI);
        pacifist.setAiPersonality(AiPersonality.PACIFIST);
        viewer.setDiplomaticReputation(pacifist.getId(), GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION);

        int effective = viewer.getDiplomacyService().getEffectiveDiplomaticReputation(pacifist, world);
        assertEquals(GameNumbers.DEFAULT_DIPLOMATIC_REPUTATION + GameNumbers.AI_PACIFIST_REPUTATION_DELTA,
                effective);
    }

    @Test
    void researchPurchaseBuysPrincessWhenAffordable() {
        Dynasty npc = new Dynasty(1, "NPC", false, GameConstants.SPECIES_OMNI);
        npc.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
        npc.unlockUpgrade(GameUnlocks.TYPE_EGG);
        npc.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        npc.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
        npc.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        npc.unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        npc.unlockUpgrade(GameUnlocks.ROLE_RANCHER);
        npc.addResearchPoints(GameUnlocks.TYPE_PRINCESS.getCost());
        Colony capital = new Colony(10, "Capital", false);
        capital.setAge(10);
        for (int i = 0; i < 100; i++) {
            capital.getWorkers().add(new Ant(capital, GameConstants.TYPE_WORKER));
        }
        npc.addColony(capital);
        npc.setCapital(capital);
        npc.setRank(GameConstants.RANK_COUNTY);
        world.getDynastys().add(npc);

        assertEquals(GameUnlocks.TYPE_PRINCESS.getCost(), npc.getResearchPoints());
        Upgrade bought = DynastyAiPriorities.tryPurchaseNextUpgrade(npc);

        assertEquals(GameUnlocks.TYPE_PRINCESS, bought);
        assertTrue(npc.hasUpgrade(GameUnlocks.TYPE_PRINCESS));
        assertTrue(npc.hasUpgrade(GameUnlocks.TYPE_QUEEN));
    }

    @Test
    void foodBuildingsOutrankEggUpgrades() {
        assertTrue(DynastyAiPriorities.buildingPriorityScore(GameUnlocks.MUSHROOM_CHAMBER_1)
                < DynastyAiPriorities.buildingPriorityScore(GameUnlocks.EGG_CHAMBER_2));
        assertTrue(DynastyAiPriorities.buildingPriorityScore(GameUnlocks.PLANT_CHAMBER_1)
                < DynastyAiPriorities.buildingPriorityScore(GameUnlocks.EGG_CHAMBER_1));
        assertTrue(DynastyAiPriorities.buildingPriorityScore(GameUnlocks.WATER_RESERVOIR_1)
                < DynastyAiPriorities.buildingPriorityScore(GameUnlocks.ROYAL_CHAMBER_1));
    }

    @Test
    void researchSkipsFreeCostUpgrades() {
        Dynasty npc = new Dynasty(1, "NPC", false, GameConstants.SPECIES_OMNI);
        npc.setAiPersonality(AiPersonality.MILITARIST);
        npc.unlockUpgrade(GameUnlocks.ABILITY_RESEARCH);
        npc.unlockUpgrade(GameUnlocks.TYPE_EGG);
        npc.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        npc.unlockUpgrade(GameUnlocks.ROLE_BUILDER);
        npc.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        npc.addResearchPoints(50);
        Colony capital = new Colony(10, "Capital", false);
        capital.setAge(10);
        for (int i = 0; i < 100; i++) {
            capital.getWorkers().add(new Ant(capital, GameConstants.TYPE_WORKER));
        }
        npc.addColony(capital);
        npc.setCapital(capital);
        npc.setRank(GameConstants.RANK_COUNTY);

        assertNull(DynastyAiPriorities.tryPurchaseNextUpgrade(npc));
        assertFalse(npc.hasUpgrade(GameUnlocks.TYPE_MAJOR));
    }

    @Test
    void ensureBreedersAssignsPrincessesWhenUnlocked() {
        Dynasty npc = new Dynasty(1, "NPC", false, GameConstants.SPECIES_OMNI);
        npc.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        npc.unlockUpgrade(GameUnlocks.ROLE_BREEDER);
        Colony capital = new Colony(10, "Capital", false);
        capital.setAge(10);
        for (int i = 0; i < 4; i++) {
            capital.getPrincesses().add(new Ant(capital, GameConstants.TYPE_PRINCESS));
        }
        npc.addColony(capital);
        npc.setCapital(capital);
        world.getDynastys().add(npc);

        aiService.runDailyAi(npc, world, tradeManager);

        assertTrue(capital.getAssignedRoleCount(GameConstants.ROLE_BREEDER)
                >= GameNumbers.AI_MIN_BREEDERS_FOR_FLIGHT);
    }

    @Test
    void eggChamberOutranksCheapGenericBuildingInNpcPriority() {
        int egg = DynastyAiPriorities.buildingPriorityScore(GameUnlocks.EGG_CHAMBER_1);
        int compost = DynastyAiPriorities.buildingPriorityScore(GameUnlocks.BUILDING_COMPOSTER);
        assertTrue(egg < compost);
    }

    @Test
    void militaristExpansionTargetExceedsPacifist() {
        Dynasty militarist = new Dynasty(1, "M", false, GameConstants.SPECIES_OMNI);
        militarist.setAiPersonality(AiPersonality.MILITARIST);
        Dynasty pacifist = new Dynasty(2, "P", false, GameConstants.SPECIES_OMNI);
        pacifist.setAiPersonality(AiPersonality.PACIFIST);
        assertTrue(militarist.getAiExpansionColonyTarget() > pacifist.getAiExpansionColonyTarget());
        assertFalse(militarist.getAiExpansionColonyTarget() <= GameNumbers.AI_EXPANSION_COLONY_TARGET);
    }
}
