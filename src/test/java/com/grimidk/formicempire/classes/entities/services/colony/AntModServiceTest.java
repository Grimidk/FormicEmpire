package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntModProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntMod;
import com.grimidk.formicempire.classes.constants.critter.ant.AntModSlot;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.ImageIcon;

class AntModServiceTest {

    @Test
    void standardProfileCodeIs1111() {
        assertEquals(1111, AntModProfile.standard().getCode());
    }

    @Test
    void trapjawStingerProfileIs2121() {
        AntModProfile profile = AntModProfile.fromCode(2121);
        assertEquals(2, profile.getDigit(AntModSlot.HEAD));
        assertEquals(1, profile.getDigit(AntModSlot.TORSO));
        assertEquals(2, profile.getDigit(AntModSlot.ABDOMEN));
        assertEquals(1, profile.getDigit(AntModSlot.OTHER));
    }

    @Test
    void stingerAbdomenDoesNotBakeAttackIntoAntStats() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);

        Ant standard = new Ant(colony, GameConstants.CLASS_SOLDIER);
        AntModService.applyModStats(standard, colony);
        Ant ant = new Ant(colony, GameConstants.CLASS_SOLDIER);
        ant.setModProfile(AntModProfile.of(1, 1, 2, 1));
        AntModService.applyModStats(ant, colony);

        assertEquals(standard.getAttack(), ant.getAttack());
        assertEquals(1.5f, AntModService.combinedAttackMult(AntModProfile.of(1, 1, 2, 1)), 0.0001f);
    }

    @Test
    void trapjawAttackStacksAdditivelyWithStinger() {
        AntModProfile profile = AntModProfile.of(2, 1, 2, 1);
        assertEquals(3f, AntModService.combinedAttackMult(profile), 0.0001f);
    }

    @Test
    void trapjawAloneIsOnePointFiveAttack() {
        AntModProfile profile = AntModProfile.of(2, 1, 1, 1);
        assertEquals(1.5f, AntModService.combinedAttackMult(profile), 0.0001f);
    }

    @Test
    void doorheadAddsTwentyPercentDefense() {
        AntModProfile profile = AntModProfile.of(3, 1, 1, 1);
        assertEquals(20f, AntModService.combinedDefenseBonus(profile), 0.0001f);
    }

    @Test
    void honeypotQuadruplesForagePower() {
        Colony colony = new Colony(2, "C", true);
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setModProfile(AntModProfile.of(1, 1, 3, 1));
        assertEquals(4f, AntModService.forageMult(worker), 0.0001f);
        assertEquals(4, AntModService.forageCarrySlots(worker));
    }

    @Test
    void sumCollectingPowerUsesHoneypotMultiplier() {
        Colony colony = new Colony(3, "C", true);
        colony.setDynasty(new Dynasty(2, "D", true, GameConstants.SPECIES_OMNI));
        colony.getDynasty().unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setModProfile(AntModProfile.of(1, 1, 3, 1));
        List<Ant> foragers = List.of(worker);
        assertEquals(4, AntModService.sumCollectingPower(colony, foragers));
    }

    @Test
    void npcNaturalSpeciesSetsFiftyPercentSubtypeRate() {
        Colony colony = new Colony(4, "Trap", false);
        AntModService.applyNaturalSpeciesModRates(colony, GameConstants.SPECIES_TRAPJAW);
        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 2));
        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD,
                AntMod.DIGIT_NONE));
    }

    @Test
    void perTypeSubtypeRatesPersistIndependently() {
        Colony colony = new Colony(6, "C", true);
        colony.setModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 2, 25f);
        colony.setModHatchRate(GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, 2, 75f);
        assertEquals(25f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 2));
        assertEquals(75f, colony.getModHatchRate(GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, 2));
    }

    @Test
    void aggregateAndFlattenSubtypeData() {
        Colony colony = new Colony(5, "C", true);
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setModProfile(AntModProfile.fromCode(2121));
        colony.getWorkers().add(worker);

        Map<String, Integer> counts = AntModService.aggregateModCounts(colony.getWorkers());
        assertEquals(1, counts.get("2121"));

        colony.setModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 2, 25f);
        Map<String, Double> flat = AntModService.flattenModRates(colony.getModHatchRates());
        assertEquals(25.0, flat.get(GameConstants.CLASS_WORKER.getNameKey() + "|HEAD|2"));
    }

    @Test
    void unflattenAcceptsLegacyColonKeysAndPipeKeys() {
        Map<String, Double> legacy = Map.of(
                "TYPE_WORKER:HEAD:3", 1.6,
                "TYPE_WORKER:HEAD:1", 98.4);
        assertEquals(1.6f, AntModService.unflattenModRates(legacy)
                .get(GameConstants.CLASS_WORKER).get(AntModSlot.HEAD).get(3), 0.01f);

        Map<String, Double> pipes = Map.of(
                "TYPE_SOLDIER|HEAD|2", 2.0,
                "TYPE_SOLDIER|HEAD|1", 98.0);
        assertEquals(2.0f, AntModService.unflattenModRates(pipes)
                .get(GameConstants.CLASS_SOLDIER).get(AntModSlot.HEAD).get(2), 0.01f);
    }

    @Test
    void spriteFolderNamesCoverSinglesAndCombos() {
        assertNull(AntModProfile.standard().buildSpriteFolder());
        assertEquals("trapjaw", AntModProfile.of(2, 1, 1, 1).buildSpriteFolder());
        assertEquals("doorhead", AntModProfile.of(3, 1, 1, 1).buildSpriteFolder());
        assertEquals("bullet", AntModProfile.of(1, 1, 2, 1).buildSpriteFolder());
        assertEquals("honeypot", AntModProfile.of(1, 1, 3, 1).buildSpriteFolder());
        assertEquals("trapjaw-bullet", AntModProfile.of(2, 1, 2, 1).buildSpriteFolder());
        assertEquals("trapjaw-honeypot", AntModProfile.of(2, 1, 3, 1).buildSpriteFolder());
        assertEquals("doorhead-bullet", AntModProfile.of(3, 1, 2, 1).buildSpriteFolder());
        assertEquals("doorhead-honeypot", AntModProfile.of(3, 1, 3, 1).buildSpriteFolder());
    }

    @Test
    void omniComboSubtypeSpritesLoadFromClasspath() {
        ImageIcon trapjawStinger = GameConstants.getAntSprite(
                GameConstants.CLASS_SOLDIER, GameConstants.SPECIES_OMNI, AntModProfile.fromCode(2121));
        ImageIcon doorheadHoneypot = GameConstants.getAntSprite(
                GameConstants.CLASS_WORKER, GameConstants.SPECIES_OMNI, AntModProfile.fromCode(3131));
        assertNotNull(trapjawStinger);
        assertNotNull(doorheadHoneypot);
    }

    @Test
    void consumptionMultAddsFiftyPercentPerActiveSubtype() {
        assertEquals(1f, AntModService.consumptionMult(AntModProfile.standard()), 0.0001f);
        assertEquals(1.5f, AntModService.consumptionMult(AntModProfile.of(2, 1, 1, 1)), 0.0001f);
        assertEquals(2f, AntModService.consumptionMult(AntModProfile.of(2, 1, 2, 1)), 0.0001f);
    }

    @Test
    void automatedTrapjawAndBulletSoldiersUseFullComboRates() {
        Dynasty dynasty = new Dynasty(7, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(8, "C", true);
        colony.setDynasty(dynasty);
        colony.setMushrooms(1000);
        colony.setWater(100);

        AntModService.applyAutomatedModRates(colony);

        assertEquals(100f, colony.getModHatchRate(GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, 2));
        assertEquals(100f, colony.getModHatchRate(GameConstants.CLASS_SOLDIER, AntModSlot.ABDOMEN, 2));
        assertEquals(100f, colony.getModHatchRate(GameConstants.CLASS_MAJOR, AntModSlot.HEAD, 2));
        assertEquals(100f, colony.getModHatchRate(GameConstants.CLASS_MAJOR, AntModSlot.ABDOMEN, 2));
    }

    @Test
    void automatedHoneypotRatesWorkersAndPrincesses() {
        Dynasty dynasty = new Dynasty(8, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_PRINCESS);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        Colony colony = new Colony(9, "C", true);
        colony.setDynasty(dynasty);
        colony.setMushrooms(500);
        colony.setWater(100);

        AntModService.applyAutomatedModRates(colony);

        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.ABDOMEN, 3));
        assertEquals(10f, colony.getModHatchRate(GameConstants.CLASS_PRINCESS, AntModSlot.ABDOMEN, 3));
    }

    @Test
    void foodScarcityKeepsAutomatedTargetsButBlocksRolls() {
        Dynasty dynasty = new Dynasty(9, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        Colony colony = new Colony(10, "C", true);
        colony.setDynasty(dynasty);
        colony.setAutomationEnabled(true);
        colony.setMushrooms(10);
        colony.setWater(100);

        AntModService.applyAutomatedModRates(colony);

        assertEquals(100f, colony.getModHatchRate(GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, 2));
        assertEquals(0f, AntModService.modAutomationFoodScale(colony));
        for (int i = 0; i < 40; i++) {
            assertTrue(AntModService.rollProfile(colony, GameConstants.CLASS_SOLDIER).isStandard());
        }
    }

    @Test
    void foodScarcityDoesNotWipeManualSubtypeRates() {
        Dynasty dynasty = new Dynasty(19, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_DOORHEAD);
        Colony colony = new Colony(20, "C", true);
        dynasty.addColony(colony);
        colony.setAutomationEnabled(true);
        colony.setModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 3, 40f);
        colony.setModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, AntMod.DIGIT_NONE, 60f);
        colony.setMushrooms(5);
        colony.setWater(100);

        AntModService.applyAutomatedModRates(colony);

        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 3));
        colony.setMushrooms(5);
        colony.setWater(100);
        assertEquals(0f, AntModService.modAutomationFoodScale(colony));
        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 3));
    }

    @Test
    void trapjawAndStingerCombinedAttackMultIsThree() {
        AntModProfile profile = AntModProfile.of(2, 1, 2, 1);
        assertEquals(3f, AntModService.combinedAttackMult(profile), 0.0001f);

        Dynasty dynasty = new Dynasty(11, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(12, "C", true);
        colony.setDynasty(dynasty);

        Ant baseline = new Ant(colony, GameConstants.CLASS_SOLDIER);
        AntModService.applyModStats(baseline, colony);
        Ant combo = new Ant(colony, GameConstants.CLASS_SOLDIER);
        combo.setModProfile(profile);
        AntModService.applyModStats(combo, colony);

        assertEquals(baseline.getAttack(), combo.getAttack());
    }

    @Test
    void honeypotIncreasesRegenByFifteenPercent() {
        Dynasty dynasty = new Dynasty(13, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        Colony colony = new Colony(14, "C", true);
        colony.setDynasty(dynasty);

        Ant baseline = new Ant(colony, GameConstants.CLASS_WORKER);
        AntModService.applyModStats(baseline, colony);
        Ant honeypot = new Ant(colony, GameConstants.CLASS_WORKER);
        honeypot.setModProfile(AntModProfile.of(1, 1, 3, 1));
        AntModService.applyModStats(honeypot, colony);

        assertEquals(Math.round(baseline.getRegen() * 1.15f), Math.round(honeypot.getRegen()));
    }

    @Test
    void subtypeFoodConsumptionAppliedToAnt() {
        Dynasty dynasty = new Dynasty(10, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(11, "C", true);
        colony.setDynasty(dynasty);
        Ant soldier = new Ant(colony, GameConstants.CLASS_SOLDIER);
        soldier.setModProfile(AntModProfile.of(2, 1, 2, 1));
        AntModService.applyModStats(soldier, colony);
        Ant baseline = new Ant(colony, GameConstants.CLASS_SOLDIER);
        assertEquals(baseline.getConsumption() * 2f, soldier.getConsumption(), 0.0001f);
    }

    @Test
    void farsightAssimilationCountsAsSubtypeAssimilation() {
        Dynasty dynasty = new Dynasty(21, "D", true, GameConstants.SPECIES_OMNI);
        Colony colony = new Colony(21, "C", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);

        assertFalse(AntModService.hasModAssimilation(colony));

        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FARSIGHT);

        assertTrue(AntModService.hasModAssimilation(colony));
        assertTrue(AntModService.getAvailableMods(colony, AntModSlot.HEAD)
                .contains(GameConstants.MOD_HEAD_FARSIGHT));
        assertTrue(AntModService.listUnlockedSpecialMods(colony)
                .contains(GameConstants.MOD_HEAD_FARSIGHT));
    }

    @Test
    void automatedFarsightRatesSoldiersAndMajorsWhenNoTrapjaw() {
        Dynasty dynasty = new Dynasty(22, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_MAJOR);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FARSIGHT);
        Colony colony = new Colony(22, "C", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        colony.setMushrooms(1000);
        colony.setWater(100);

        AntModService.applyAutomatedModRates(colony);

        int digit = GameConstants.MOD_HEAD_FARSIGHT.getDigit();
        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_SOLDIER, AntModSlot.HEAD, digit));
        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_MAJOR, AntModSlot.HEAD, digit));
    }

    @Test
    void leafcutterMandiblesProfileMultipliers() {
        AntModProfile profile = AntModProfile.of(5, 1, 1, 1);
        assertEquals(1.25f, AntModService.combinedAttackMult(profile), 0.0001f);
        Colony colony = new Colony(23, "C", true);
        Ant worker = new Ant(colony, GameConstants.CLASS_WORKER);
        worker.setModProfile(profile);
        assertEquals(2f, AntModService.forageMult(worker), 0.0001f);
        assertEquals(2, AntModService.forageCarrySlots(worker));
    }

    @Test
    void automatedLeafcutterRates() {
        Dynasty dynasty = new Dynasty(24, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_WORKER);
        dynasty.unlockUpgrade(GameUnlocks.TYPE_SOLDIER);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_FARMING);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HEATRESIST);
        Colony colony = new Colony(24, "C", true);
        dynasty.addColony(colony);
        colony.setDynasty(dynasty);
        colony.setMushrooms(1000);
        colony.setWater(100);

        AntModService.applyAutomatedModRates(colony);

        assertEquals(50f, colony.getModHatchRate(GameConstants.CLASS_WORKER, AntModSlot.HEAD, 5));
    }
}
