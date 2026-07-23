package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtype;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeSlot;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.swing.ImageIcon;

class AntSubtypeServiceTest {

    @Test
    void standardProfileCodeIs1111() {
        assertEquals(1111, AntSubtypeProfile.standard().getCode());
    }

    @Test
    void trapjawStingerProfileIs2121() {
        AntSubtypeProfile profile = AntSubtypeProfile.fromCode(2121);
        assertEquals(2, profile.getDigit(AntSubtypeSlot.HEAD));
        assertEquals(1, profile.getDigit(AntSubtypeSlot.TORSO));
        assertEquals(2, profile.getDigit(AntSubtypeSlot.ABDOMEN));
        assertEquals(1, profile.getDigit(AntSubtypeSlot.OTHER));
    }

    @Test
    void stingerAbdomenBoostsAntAttackByOnePointFive() {
        Dynasty dynasty = new Dynasty(1, "Test", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(1, "C", true);
        colony.setDynasty(dynasty);

        Ant standard = new Ant(colony, GameConstants.TYPE_SOLDIER);
        AntSubtypeService.applySubtypeStats(standard, colony);
        Ant ant = new Ant(colony, GameConstants.TYPE_SOLDIER);
        ant.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 2, 1));
        AntSubtypeService.applySubtypeStats(ant, colony);

        assertEquals(Math.round(standard.getAttack() * 1.5f), ant.getAttack());
    }

    @Test
    void trapjawAttackStacksAdditivelyWithStinger() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(2, 1, 2, 1);
        assertEquals(3f, AntSubtypeService.combinedAttackMult(profile), 0.0001f);
    }

    @Test
    void trapjawAloneIsOnePointFiveAttack() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(2, 1, 1, 1);
        assertEquals(1.5f, AntSubtypeService.combinedAttackMult(profile), 0.0001f);
    }

    @Test
    void doorheadAddsTwentyPercentDefense() {
        AntSubtypeProfile profile = AntSubtypeProfile.of(3, 1, 1, 1);
        assertEquals(20f, AntSubtypeService.combinedDefenseBonus(profile), 0.0001f);
    }

    @Test
    void honeypotQuadruplesForagePower() {
        Colony colony = new Colony(2, "C", true);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 3, 1));
        assertEquals(4f, AntSubtypeService.forageMult(worker), 0.0001f);
        assertEquals(4, AntSubtypeService.forageCarrySlots(worker));
    }

    @Test
    void sumCollectingPowerUsesHoneypotMultiplier() {
        Colony colony = new Colony(3, "C", true);
        colony.setDynasty(new Dynasty(2, "D", true, GameConstants.SPECIES_OMNI));
        colony.getDynasty().unlockUpgrade(GameUnlocks.ROLE_FORAGER);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 3, 1));
        List<Ant> foragers = List.of(worker);
        assertEquals(4, AntSubtypeService.sumCollectingPower(colony, foragers));
    }

    @Test
    void npcNaturalSpeciesSetsFiftyPercentSubtypeRate() {
        Colony colony = new Colony(4, "Trap", false);
        AntSubtypeService.applyNaturalSpeciesSubtypeRates(colony, GameConstants.SPECIES_TRAPJAW);
        assertEquals(50f, colony.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 2));
        assertEquals(50f, colony.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD,
                AntSubtype.DIGIT_NONE));
    }

    @Test
    void perTypeSubtypeRatesPersistIndependently() {
        Colony colony = new Colony(6, "C", true);
        colony.setSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 2, 25f);
        colony.setSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.HEAD, 2, 75f);
        assertEquals(25f, colony.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 2));
        assertEquals(75f, colony.getSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.HEAD, 2));
    }

    @Test
    void aggregateAndFlattenSubtypeData() {
        Colony colony = new Colony(5, "C", true);
        Ant worker = new Ant(colony, GameConstants.TYPE_WORKER);
        worker.setSubtypeProfile(AntSubtypeProfile.fromCode(2121));
        colony.getWorkers().add(worker);

        Map<String, Integer> counts = AntSubtypeService.aggregateSubtypeCounts(colony.getWorkers());
        assertEquals(1, counts.get("2121"));

        colony.setSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.HEAD, 2, 25f);
        Map<String, Double> flat = AntSubtypeService.flattenSubtypeRates(colony.getSubtypeHatchRates());
        assertEquals(25.0, flat.get(GameConstants.TYPE_WORKER.getNameKey() + ":HEAD:2"));
    }

    @Test
    void spriteFolderNamesCoverSinglesAndCombos() {
        assertNull(AntSubtypeProfile.standard().buildSpriteFolder());
        assertEquals("trapjaw", AntSubtypeProfile.of(2, 1, 1, 1).buildSpriteFolder());
        assertEquals("doorhead", AntSubtypeProfile.of(3, 1, 1, 1).buildSpriteFolder());
        assertEquals("bullet", AntSubtypeProfile.of(1, 1, 2, 1).buildSpriteFolder());
        assertEquals("honeypot", AntSubtypeProfile.of(1, 1, 3, 1).buildSpriteFolder());
        assertEquals("trapjaw-bullet", AntSubtypeProfile.of(2, 1, 2, 1).buildSpriteFolder());
        assertEquals("trapjaw-honeypot", AntSubtypeProfile.of(2, 1, 3, 1).buildSpriteFolder());
        assertEquals("doorhead-bullet", AntSubtypeProfile.of(3, 1, 2, 1).buildSpriteFolder());
        assertEquals("doorhead-honeypot", AntSubtypeProfile.of(3, 1, 3, 1).buildSpriteFolder());
    }

    @Test
    void omniComboSubtypeSpritesLoadFromClasspath() {
        ImageIcon trapjawStinger = GameConstants.getAntSprite(
                GameConstants.TYPE_SOLDIER, GameConstants.SPECIES_OMNI, AntSubtypeProfile.fromCode(2121));
        ImageIcon doorheadHoneypot = GameConstants.getAntSprite(
                GameConstants.TYPE_WORKER, GameConstants.SPECIES_OMNI, AntSubtypeProfile.fromCode(3131));
        assertNotNull(trapjawStinger);
        assertNotNull(doorheadHoneypot);
    }

    @Test
    void consumptionMultAddsFiftyPercentPerActiveSubtype() {
        assertEquals(1f, AntSubtypeService.consumptionMult(AntSubtypeProfile.standard()), 0.0001f);
        assertEquals(1.5f, AntSubtypeService.consumptionMult(AntSubtypeProfile.of(2, 1, 1, 1)), 0.0001f);
        assertEquals(2f, AntSubtypeService.consumptionMult(AntSubtypeProfile.of(2, 1, 2, 1)), 0.0001f);
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

        AntSubtypeService.applyAutomatedSubtypeRates(colony);

        assertEquals(100f, colony.getSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.HEAD, 2));
        assertEquals(100f, colony.getSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.ABDOMEN, 2));
        assertEquals(100f, colony.getSubtypeHatchRate(GameConstants.TYPE_MAJOR, AntSubtypeSlot.HEAD, 2));
        assertEquals(100f, colony.getSubtypeHatchRate(GameConstants.TYPE_MAJOR, AntSubtypeSlot.ABDOMEN, 2));
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

        AntSubtypeService.applyAutomatedSubtypeRates(colony);

        assertEquals(50f, colony.getSubtypeHatchRate(GameConstants.TYPE_WORKER, AntSubtypeSlot.ABDOMEN, 3));
        assertEquals(10f, colony.getSubtypeHatchRate(GameConstants.TYPE_PRINCESS, AntSubtypeSlot.ABDOMEN, 3));
    }

    @Test
    void foodScarcityStopsAutomatedSubtypeUse() {
        Dynasty dynasty = new Dynasty(9, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        Colony colony = new Colony(10, "C", true);
        colony.setDynasty(dynasty);
        colony.setMushrooms(10);
        colony.setWater(100);

        AntSubtypeService.applyAutomatedSubtypeRates(colony);

        assertEquals(100f, colony.getSubtypeHatchRate(GameConstants.TYPE_SOLDIER, AntSubtypeSlot.HEAD,
                AntSubtype.DIGIT_NONE));
    }

    @Test
    void trapjawAndStingerCombinedAttackIsThreeTimesBase() {
        Dynasty dynasty = new Dynasty(11, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_ACID);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(12, "C", true);
        colony.setDynasty(dynasty);

        Ant baseline = new Ant(colony, GameConstants.TYPE_SOLDIER);
        AntSubtypeService.applySubtypeStats(baseline, colony);
        Ant combo = new Ant(colony, GameConstants.TYPE_SOLDIER);
        combo.setSubtypeProfile(AntSubtypeProfile.of(2, 1, 2, 1));
        AntSubtypeService.applySubtypeStats(combo, colony);

        assertEquals(baseline.getAttack() * 3, combo.getAttack());
    }

    @Test
    void honeypotIncreasesRegenByFifteenPercent() {
        Dynasty dynasty = new Dynasty(13, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.STAT_SKELETON);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_HONEYPOT);
        Colony colony = new Colony(14, "C", true);
        colony.setDynasty(dynasty);

        Ant baseline = new Ant(colony, GameConstants.TYPE_WORKER);
        AntSubtypeService.applySubtypeStats(baseline, colony);
        Ant honeypot = new Ant(colony, GameConstants.TYPE_WORKER);
        honeypot.setSubtypeProfile(AntSubtypeProfile.of(1, 1, 3, 1));
        AntSubtypeService.applySubtypeStats(honeypot, colony);

        assertEquals(Math.round(baseline.getRegen() * 1.15f), Math.round(honeypot.getRegen()));
    }

    @Test
    void subtypeFoodConsumptionAppliedToAnt() {
        Dynasty dynasty = new Dynasty(10, "D", true, GameConstants.SPECIES_OMNI);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_TRAPJAW);
        dynasty.unlockUpgrade(GameUnlocks.ASSIMILATED_STINGING);
        Colony colony = new Colony(11, "C", true);
        colony.setDynasty(dynasty);
        Ant soldier = new Ant(colony, GameConstants.TYPE_SOLDIER);
        soldier.setSubtypeProfile(AntSubtypeProfile.of(2, 1, 2, 1));
        AntSubtypeService.applySubtypeStats(soldier, colony);
        Ant baseline = new Ant(colony, GameConstants.TYPE_SOLDIER);
        assertEquals(baseline.getConsumption() * 2f, soldier.getConsumption(), 0.0001f);
    }
}
