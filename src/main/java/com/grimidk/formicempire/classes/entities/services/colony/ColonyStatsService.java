package com.grimidk.formicempire.classes.entities.services.colony;

import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.entities.critter.Ant;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.dynasty.Dynasty;
import com.grimidk.formicempire.classes.entities.services.dynasty.DynastySynergyService;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameUnlocks;

import java.util.List;
import java.util.Map;

public class ColonyStatsService {
    
    // --- Capacities ---
    public int getPlantsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_3)) return 60000;
        if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_2)) return 25000;
        if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_1)) return 10000;
        if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_0)) return 4000; 
        return 0;
    }
    public int getMushroomsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_3)) return 100000;
        if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_2)) return 40000;
        if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_1)) return 15000;
        if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_0)) return 8000; 
        return 0;
    }
    public int getProteinCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_3)) return 40000;
        if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_2)) return 15000;
        if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_1)) return 5000;
        if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_0)) return 2000; 
        return 0;
    }
    public int getWaterCapacity(Colony colony) {
        if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_3)) return 25000;
        if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_2)) return 10000;
        if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_1)) return 2500;
        if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_0)) return 1000; 
        return 0;
    }
    public int getSyrupsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_3)) return 10000;
        if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_2)) return 3500;
        if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_1)) return 1200;
        if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_0)) return 500; 
        return 0;
    }
    public int getResinsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_3)) return 3000;
        if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_2)) return 1200;
        if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_1)) return 500;
        if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_0)) return 200; 
        return 0;
    }
    public int getMineralsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_3)) return 2500;
        if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_2)) return 750;
        if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_1)) return 250;
        if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_0)) return 100; 
        return 0;
    }
    public int getEggsCapacity(Colony colony) {
        if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_3)) return 500;
        if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_2)) return 150;
        if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_1)) return 80;
        if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_0)) return 50; 
        return 0;
    }
    public int getQueensCapacity(Colony colony) {
        boolean canMultiQueen = colony.hasUpgrade(GameUnlocks.ASSIMILATED_MULTIQUEEN);
        
        if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_3)) return canMultiQueen ? 10 : 1;
        if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_2)) return canMultiQueen ? 4 : 1;
        if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_1)) return canMultiQueen ? 2 : 1;
        if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_0)) return 1; 
        return 0;
    }
    public int getAphidCapacity(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) return 10;
        return 0;
    }
    public int getSourceCapacity(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_LOGISTICS_1)) return 5;
        return 1;
    }

    // --- Trade ---
    public double getBaseTradeCapacity(Colony colony) { return 50.0; }

    public double getBaseTradeSecurity(Colony colony) { return 0.5; }

    // --- Limits ---
    public int getSpreadingLimit(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ABILITY_SPREAD_2)) return 9999;
        if (colony.hasUpgrade(GameUnlocks.ABILITY_SPREAD)) return 1;
        return 0;
    }

    // --- Rates ---
    public int getResearchSpeed(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_RESEARCH_3)) return 8;
        if (colony.hasUpgrade(GameUnlocks.STAT_RESEARCH_2)) return 4;
        if (colony.hasUpgrade(GameUnlocks.STAT_RESEARCH_1)) return 2;
        if (colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) return 1;
        return 0;
    }

    public int getPassiveLabResearcherBonus(Colony colony) {
        if (colony == null || !colony.hasBuilding(GameUnlocks.PASSIVE_LAB)) {
            return 0;
        }
        return colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1) ? 2 : 1;
    }

    public int getEffectiveResearcherCount(Colony colony) {
        if (colony == null) {
            return 0;
        }
        return colony.getAssignedRoleCount(GameConstants.ROLE_RESEARCHER) + getPassiveLabResearcherBonus(colony);
    }

    public int getHourlyResearchPoints(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int researchers = getEffectiveResearcherCount(colony);
        int assistants = colony.getAssignedRoleCount(GameConstants.ROLE_ASSISTANT);
        if (researchers <= 0 && assistants <= 0) {
            return 0;
        }
        int speed = getResearchSpeed(colony);
        int queenGain = researchers * speed;
        int assistantGain = (int) (assistants * (speed / (double) GameNumbers.RESEARCH_ASSISTANT_EFFICIENCY_DIVISOR));
        return queenGain + assistantGain;
    }

    public int getDailyResearchPoints(Colony colony) {
        return getHourlyResearchPoints(colony) * 24;
    }
    public int getGrowthTime(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_GROWTH_3)) return 1;
        if (colony.hasUpgrade(GameUnlocks.STAT_GROWTH_2)) return 2;
        if (colony.hasUpgrade(GameUnlocks.STAT_GROWTH_1)) return 3;
        if (colony.hasUpgrade(GameUnlocks.TYPE_EGG)) return 4;
        return 0;
    }
    public float getLayingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_LAYING_3)) return 6f;
        if (colony.hasUpgrade(GameUnlocks.STAT_LAYING_2)) return 4f;
        if (colony.hasUpgrade(GameUnlocks.STAT_LAYING_1)) return 2f;
        if (colony.hasUpgrade(GameUnlocks.ROLE_LAYER)) return 1f;
        return 0f;
    }
    public float getConversionRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_FARMING_3)) return 0.8f;
        if (colony.hasUpgrade(GameUnlocks.STAT_FARMING_2)) return 0.4f;
        if (colony.hasUpgrade(GameUnlocks.STAT_FARMING_1)) return 0.2f;
        if (colony.hasUpgrade(GameUnlocks.ROLE_FARMER)) return 0.1f;
        return 0f;
    }
    public float getNursingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_NURSE)) return 10f;
        return 0f;
    }
    public float getGravingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_GRAVING_3)) return 24f;
        if (colony.hasUpgrade(GameUnlocks.STAT_GRAVING_2)) return 16f;
        if (colony.hasUpgrade(GameUnlocks.STAT_GRAVING_1)) return 10f;
        if (colony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) return 5f;
        return 0f;
    }
    public float getCollectingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) return 1f;
        return 0f;
    }
    public float getParasiteDetection(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_POLICING_3)) return 0.55f;
        if (colony.hasUpgrade(GameUnlocks.STAT_POLICING_2)) return 0.35f;
        if (colony.hasUpgrade(GameUnlocks.STAT_POLICING_1)) return 0.2f;
        if (colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) return 0.1f;
        return 0f;
    }
    public float getScoutingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_3)) return 0.8f;
        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_2)) return 0.4f;
        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_1)) return 0.2f;
        if (colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) return 0.1f;
        return 0f;
    }

    public double getConstructionEfficiency(Colony colony) {
        int builderCount = colony.getAssignedRoleCount(GameConstants.ROLE_BUILDER);
        int craneCount = 0;
        if (colony.hasUpgrade(GameUnlocks.ROLE_CRANE)) {
            craneCount = colony.getAssignedRoleCount(GameConstants.ROLE_CRANE);
        }
        int totalPower = builderCount + (craneCount * 25);
        return totalPower / 100.0;
    }

    public float getContaminationMitigation(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_CONTAMINATION_3)) return 0.4f; 
        if (colony.hasUpgrade(GameUnlocks.STAT_CONTAMINATION_2)) return 0.6f;
        if (colony.hasUpgrade(GameUnlocks.STAT_CONTAMINATION_1)) return 0.8f;
        return 1.0f; 
    }
    public int getThirstResistance(Colony colony, Temperature temp) {
        int resistance = 20; 
        if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_3)) resistance = 80;
        else if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_2)) resistance = 60;
        else if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_1)) resistance = 40;
        
        if (temp != null) {
            if (temp == GameConstants.TEMP_WARM) resistance /= 2;
            else if (temp == GameConstants.TEMP_HOT) resistance /= 4;
            else if (temp == GameConstants.TEMP_BURNING) resistance /= 8;
        }
        return resistance;
    }

    // --- Stats ---
    public int getBaseHealth(Colony colony) { return colony.hasUpgrade(GameUnlocks.STAT_SKELETON) ? 100 : 0; }
    public int getBaseTempRes(Colony colony) { return colony.hasUpgrade(GameUnlocks.STAT_LONGEVITY) ? 25 : 0; }
    public int getBaseRegen(Colony colony) {
        return colony.hasUpgrade(GameUnlocks.STAT_SKELETON) ? GameNumbers.ANT_REGEN_PERCENT_BASE : 0;
    }
    public int getBaseConsumption(Colony colony) { return colony.hasUpgrade(GameUnlocks.STAT_LONGEVITY) ? 1 : 0; }

    public static float getAssimilatedDamageMultiplier(Dynasty dynasty) {
        if (dynasty == null) {
            return 1f;
        }
        if (DynastySynergyService.isActive(dynasty, GameUnlocks.SUPER_VENOM_SYNERGY)) {
            return GameNumbers.ASSIMILATED_DAMAGE_SYNERGY_FIRE_DEADLY;
        }
        float mult = 1f;
        if (dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_FIREVENOM)) {
            mult += GameNumbers.ASSIMILATED_DAMAGE_ADD_FIRE;
        }
        if (dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_DEADLYVENOM)) {
            mult += GameNumbers.ASSIMILATED_DAMAGE_ADD_DEADLY;
        }
        return mult;
    }

    public static float getAssimilatedAttackSpeedMultiplier(Dynasty dynasty) {
        if (dynasty != null && dynasty.hasUpgrade(GameUnlocks.ASSIMILATED_FASTBITE)) {
            return GameNumbers.ASSIMILATED_ATTACK_SPEED_MULT_FASTBITE;
        }
        return 1f;
    }

    public int getBaseAttack(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.STAT_ACID)) {
            return 0;
        }
        float mult = getAssimilatedDamageMultiplier(colony.getDynasty());
        return Math.round(GameNumbers.MILITARY_BASELINE_ATTACK * mult);
    }

    public int getBaseAttackSpeed(Colony colony) {
        if (!colony.hasUpgrade(GameUnlocks.STAT_ACID)) {
            return 0;
        }
        float mult = getAssimilatedAttackSpeedMultiplier(colony.getDynasty());
        return Math.round(GameNumbers.MILITARY_BASELINE_ATTACK_SPEED * mult);
    }

    public int getBaseDefense(Colony colony) {
        return 0;
    }
    public int getBaseSpeed(Colony colony) { return colony.hasUpgrade(GameUnlocks.STAT_ACID) ? 1 : 0; }
    public int getBaseSize(Colony colony){ return colony.hasUpgrade(GameUnlocks.STAT_LONGEVITY) ? 1 : 0; }
    
    // --- Production/Consumption Aggregates ---
    public float[] getForagerSplit(Colony colony) {
        int farmerCount = getEffectiveFarmerCount(colony);
        float convertRate = getConversionRate(colony);
        int plantsNeededPerHour = (int)(farmerCount * convertRate * 60); 
        
        boolean lowPlants = colony.getPlants() < (plantsNeededPerHour * 24);
        boolean waterFull = colony.getWater() >= getWaterCapacity(colony);
        boolean plantsFull = colony.getPlants() >= getPlantsCapacity(colony);

        if ((lowPlants && !plantsFull) || (!plantsFull && waterFull)) {
            return new float[]{0.9f, 0.1f}; 
        } else if (plantsFull && !waterFull) {
            return new float[]{0.1f, 0.9f}; 
        }
        return new float[]{0.5f, 0.5f};
    }
    
    public int getEffectiveFarmerCount(Colony colony) {
        int count = colony.runsFullSimulation()
                ? colony.getActiveRoleCount(GameConstants.ROLE_FARMER)
                : colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        if (colony.hasBuilding(GameUnlocks.PASSIVE_FARM)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) count += 2;
            else count += 1;
        }
        return count;
    }

    public double getPlantProductionHourly(Colony colony) {
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        float split = getForagerSplit(colony)[0];
        return foragerCount * split * getCollectingRate(colony);
    }
    
    public double getWaterProductionHourly(Colony colony) {
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        float split = getForagerSplit(colony)[1];
        
        double passive = 0;
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            double dailyPct = colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1) ? 0.20 : 0.10;
            passive = (getWaterCapacity(colony) * dailyPct) / 24.0;
        }
        
        return (foragerCount * split * getCollectingRate(colony)) + passive;
    }

    public double getProteinProductionHourly(Colony colony) {
        int hunterCount = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
        return hunterCount * getCollectingRate(colony);
    }

    public double getMineralProductionHourly(Colony colony) {
        int minerCount = colony.getAssignedRoleCount(GameConstants.ROLE_MINER);
        return minerCount * getCollectingRate(colony);
    }

    
    public int getPlantProduction(Colony colony) {
        return (int) (getPlantProductionHourly(colony) * 24.0);
    }

    public int getWaterProduction(Colony colony) {
        return (int) (getWaterProductionHourly(colony) * 24.0);
    }

    public int getProteinProduction(Colony colony) {
        return (int) (getProteinProductionHourly(colony) * 24.0);
    }
    
    public int getMineralProduction(Colony colony) {
        return (int) (getMineralProductionHourly(colony) * 24.0);
    }
    
    public int getMineralConsumption(Colony colony) {
        return 0;
    }
    
    public int getPlantConsumption(Colony colony) {
        int farmerCount = getEffectiveFarmerCount(colony);
        double demand = farmerCount * getConversionRate(colony) * 1440.0;
        int available = getPlantProduction(colony) + colony.getPlants();
        return Math.min((int)demand, available);
    }
    
    public int getWaterConsumption(Colony colony) {
        int adultAnts = 0;
        adultAnts += colony.getWorkers().size();
        adultAnts += colony.getSoldiers().size();
        adultAnts += colony.getMajors().size();
        adultAnts += colony.getDrones().size();
        adultAnts += colony.getPrincesses().size();
        adultAnts += colony.getQueens().size();

        double baseResistance = 0.20;
        if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_3)) baseResistance = 0.80;
        else if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_2)) baseResistance = 0.60;
        else if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_1)) baseResistance = 0.40;
        
        double consumption = adultAnts * (1.0 - baseResistance);
        if (adultAnts > 0 && consumption < 1.0) return 1;
        
        return (int) Math.ceil(consumption);
    }

    public int getProteinConsumption(Colony colony) {
        int farmerCount = getEffectiveFarmerCount(colony);
        double demand = farmerCount * getConversionRate(colony) * 1440.0;
        int available = getProteinProduction(colony) + colony.getProtein();
        return Math.min((int)demand, available);
    }

    public int getTotalProduction(Colony colony){
        int farmerCount = getEffectiveFarmerCount(colony);
        double potentialOutput = farmerCount * getConversionRate(colony) * 1440.0;
        
        int sustainableInput = getPlantProduction(colony) + colony.getPlants() + ((getProteinProduction(colony) + colony.getProtein()) * 2);
                               
        return Math.min((int)potentialOutput, sustainableInput);
    }

    public int getTotalConsumption(Colony colony){
        int totalConsumption = 0;
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            for (Ant ant : entry.getValue()) {
                if (ant == null || !ant.isAlive()) {
                    continue;
                }
                totalConsumption += Math.max(1, (int) Math.ceil(ant.getConsumption()));
            }
        }
        return totalConsumption;
    }

    public int getSubtypeFoodOverhead(Colony colony) {
        if (colony == null) {
            return 0;
        }
        int base = getBaseConsumption(colony);
        int overhead = 0;
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            AntType type = entry.getKey();
            if (type == null || !AntSubtypeService.isEligibleType(type)) {
                continue;
            }
            float baseline = base * type.getConsumptionMult();
            for (Ant ant : entry.getValue()) {
                if (ant == null || !ant.isAlive()) {
                    continue;
                }
                overhead += Math.max(0, (int) Math.ceil(ant.getConsumption() - baseline));
            }
        }
        return overhead;
    }
}
