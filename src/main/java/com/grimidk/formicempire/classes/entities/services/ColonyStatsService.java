package com.grimidk.formicempire.classes.entities.services;

import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.world.Temperature;
import com.grimidk.formicempire.classes.entities.Ant;
import com.grimidk.formicempire.classes.entities.Colony;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameUnlocks;

import java.util.List;
import java.util.Map;

public class ColonyStatsService {
    // --- Capacities ---
    public int getPlantsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_2)) {return 25000;
        } else if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_1)) {return 10000;
        } else if (colony.hasBuilding(GameUnlocks.PLANT_CHAMBER_0)) {return 4000; 
        } else {return 0;}
    }
    public int getMushroomsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_2)) {return 40000;
        } else if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_1)) {return 15000;
        } else if (colony.hasBuilding(GameUnlocks.MUSHROOM_CHAMBER_0)) {return 8000; 
        } else {return 0;}
    }
    public int getProteinCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_2)) {return 15000;
        } else if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_1)) {return 5000;
        } else if (colony.hasBuilding(GameUnlocks.MEAT_CHAMBER_0)) {return 2000; 
        } else {return 0;}
    }
    public int getWaterCapacity(Colony colony) {
        if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_2)) {return 10000;
        } else if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_1)) {return 2500;
        } else if (colony.hasBuilding(GameUnlocks.WATER_RESERVOIR_0)) {return 1000; 
        } else {return 0;}
    }
    public int getSyrupsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_2)) {return 3500;
        } else if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_1)) {return 1200;
        } else if (colony.hasBuilding(GameUnlocks.SYRUP_RESERVOIR_0)) {return 500; 
        } else {return 0;}
    }
    public int getResinsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_2)) {return 1200;
        } else if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_1)) {return 500;
        } else if (colony.hasBuilding(GameUnlocks.RESIN_RESERVOIR_0)) {return 200; 
        } else {return 0;}
    }
    public int getMineralsCapacity(Colony colony) { 
        if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_2)) {return 750;
        } else if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_1)) {return 250;
        } else if (colony.hasBuilding(GameUnlocks.ROCK_WAREHOUSE_0)) {return 100; 
        } else {return 0;}
    }
    public int getEggsCapacity(Colony colony) {
        if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_2)) {return 150;
        } else if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_1)) {return 80;
        } else if (colony.hasBuilding(GameUnlocks.EGG_CHAMBER_0)) {return 50; 
        } else {return 0;}
    }
    public int getQueensCapacity(Colony colony) {
        if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_2)) {return 4;
        } else if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_1)) {return 2;
        } else if (colony.hasBuilding(GameUnlocks.ROYAL_CHAMBER_0)) {return 1; 
        } else {return 0;}
    }
    public int getAphidCapacity(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_RANCHER)) {return 10;
        } else {return 0;}
    }
    public int getSourceCapacity(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_LOGISTICS_1)) {return 5;
        } else {return 1;}
    }

    // --- Rates ---
    public int getResearchSpeed(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_RESEARCH_3)) {return 8;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_RESEARCH_2)) {return 4;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_RESEARCH_1)) {return 2;
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_RESEARCHER)) {return 1;
        } else {return 0;}
    }
    public int getGrowthTime(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_GROWTH_3)) {return 1;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_GROWTH_2)) {return 2;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_GROWTH_1)) {return 3;
        } else if (colony.hasUpgrade(GameUnlocks.TYPE_EGG)) {return 4;
        } else {return 0;}
    }
    public float getLayingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_LAYING_3)) {return 6f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_LAYING_2)) {return 4f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_LAYING_1)) {return 2f;
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_LAYER)) {return 1f;
        } else {return 0;}
    }
    public float getConversionRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_FARMING_3)) {return 0.8f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_FARMING_2)) {return 0.4f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_FARMING_1)) {return 0.2f;
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_FARMER)) {return 0.1f;
        } else {return 0;}
    }
    public float getNursingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_NURSE)) {return 10f;
        } else {return 0;}
    }
    public float getGravingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_GRAVING_3)) {return 24f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_GRAVING_2)) {return 16f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_GRAVING_1)) {return 10f;
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) {return 5f;
        } else {return 0;}
    }
    public float getCollectingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {return 1f;
        } else {return 0;}
    }
    public float getParasiteDetection(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_POLICING_3)) {return 0.55f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_POLICING_2)) {return 0.35f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_POLICING_1)) {return 0.2f;
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {return 0.1f;
        } else {return 0;}
    }
    public float getScoutingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_3)) {return 0.8f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_2)) {return 0.4f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_SCOUTING_1)) {return 0.2f;
        } else if (colony.hasUpgrade(GameUnlocks.ROLE_SCOUT)) {return 0.1f;
        } else {return 0;}
    }
    public float getContaminationMitigation(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_CONTAMINATION_3)) { return 0.4f; 
        } else if (colony.hasUpgrade(GameUnlocks.STAT_CONTAMINATION_2)) { return 0.6f;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_CONTAMINATION_1)) { return 0.8f;
        } else { return 1.0f; } 
    }
    public int getThirstResistance(Colony colony, Temperature temp) {
        int resistance = 20; 
        if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_3)) {
            resistance = 80;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_2)) {
            resistance = 60;
        } else if (colony.hasUpgrade(GameUnlocks.STAT_THIRST_1)) {
            resistance = 40;
        }
        if (temp != null) {
            if (temp == GameConstants.TEMP_WARM) {
                resistance /= 2;
            } else if (temp == GameConstants.TEMP_HOT) {
                resistance /= 4;
            } else if (temp == GameConstants.TEMP_BURNING) {
                resistance /= 8;
            }
        }
        return resistance;
    }

    // --- Stats ---
    public int getBaseHealth(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_SKELETON)) {return 100;
        } else {return 0;}
    }
    public int getBaseTempRes(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_LONGEVITY)) {return 25;
        } else {return 0;}
    }
    public int getBaseRegen(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_SKELETON)) {return 1;
        } else {return 0;}
    }
    public int getBaseConsumption(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_LONGEVITY)) {return 1;
        } else {return 0;}
    }
    public int getBaseAttack(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_ACID)) {return 10;
        } else {return 0;}
    }
    public int getBaseAttackSpeed(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_ACID)) {return 1;
        } else {return 0;}
    }
    public int getBaseDefense(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_SKELETON)) {return 5;
        } else {return 0;}
    }
    public int getBaseSpeed(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.STAT_ACID)) {return 1;
        } else {return 0;}
    }
    public int getBaseSize(Colony colony){
        if (colony.hasUpgrade(GameUnlocks.STAT_LONGEVITY)) {return 1;
        } else {return 0;}
    }
    
    // --- Granular Aggregates (Daily Estimates) ---
    public int getPlantProduction(Colony colony) {
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        double plantPower = foragerCount * getCollectingRate(colony) * 24.0;
        return (int) (plantPower * 0.5); 
    }
    public int getPlantConsumption(Colony colony) {
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        if (colony.hasBuilding(GameUnlocks.PASSIVE_FARM)) {
             if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) farmerCount += 2;
             else farmerCount += 1;
        }
        double demand = farmerCount * getConversionRate(colony) * 1440.0;
        int available = getPlantProduction(colony) + colony.getPlants();
        return Math.min((int)demand, available);
    }

    public int getWaterProduction(Colony colony) {
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        double foragePotential = foragerCount * getCollectingRate(colony) * 24.0;
        
        double passiveGeneration = 0;
        if (colony.hasBuilding(GameUnlocks.PASSIVE_WATER)) {
            double pct = colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1) ? 0.20 : 0.10;
            passiveGeneration = getWaterCapacity(colony) * pct * 24.0;
        }
        return (int) ((foragePotential * 0.5) + passiveGeneration);
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

    public int getProteinProduction(Colony colony) {
        int hunterCount = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
        return (int) (hunterCount * getCollectingRate(colony) * 24.0);
    }
    public int getProteinConsumption(Colony colony) {
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
         if (colony.hasBuilding(GameUnlocks.PASSIVE_FARM)) {
             if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) farmerCount += 2;
             else farmerCount += 1;
        }
        double demand = farmerCount * getConversionRate(colony) * 1440.0;
        int available = getProteinProduction(colony) + colony.getProtein();
        return Math.min((int)demand, available);
    }

    public int getMineralProduction(Colony colony) {
        int minerCount = colony.getAssignedRoleCount(GameConstants.ROLE_MINER);
        return (int) (minerCount * getCollectingRate(colony) * 24.0 );
    }
    public int getMineralConsumption(Colony colony) {
        return 0;
    }

    public int getTotalProduction(Colony colony){
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);
        if (colony.hasBuilding(GameUnlocks.PASSIVE_FARM)) {
            if (colony.hasUpgrade(GameUnlocks.STAT_PASSIVE_1)) farmerCount += 2;
            else farmerCount += 1;
        }
        
        double potentialOutput = farmerCount * getConversionRate(colony) * 1440.0;
        
        int sustainableInput = getPlantProduction(colony) + colony.getPlants() + ((getProteinProduction(colony) + colony.getProtein()) * 2);
                               
        return Math.min((int)potentialOutput, sustainableInput);
    }

    public int getTotalConsumption(Colony colony){
        double totalConsumption = 0;
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            double typeMult = entry.getKey().getConsumptionMult();
            int perAnt = (int)(typeMult * getBaseConsumption(colony));
            if (perAnt <= 0) perAnt = 1;
            
            totalConsumption += entry.getValue().size() * perAnt;
        }
        return (int) totalConsumption;
    }
}