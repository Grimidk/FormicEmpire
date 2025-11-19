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
        if (colony.hasUpgrade(GameUnlocks.ROLE_LAYER)) {return 1f;
        } else {return 0;}
    }
    public float getConversionRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_FARMER)) {return 0.1f;
        } else {return 0;}
    }
    public float getNursingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_NURSE)) {return 10f;
        } else {return 0;}
    }
    public float getGravingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_GRAVER)) {return 5f;
        } else {return 0;}
    }
    public float getCollectingRate(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_FORAGER)) {return 1f;
        } else {return 0;}
    }
    public float getParasiteDetection(Colony colony) {
        if (colony.hasUpgrade(GameUnlocks.ROLE_POLICE)) {return 10f;
        } else {return 0;}
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

    // --- Aggregates ---
    public int getTotalConsumption(Colony colony){
        double totalConsumption = 0;
        for (Map.Entry<AntType, List<Ant>> entry : colony.getAntGroups().entrySet()) {
            totalConsumption += (double) entry.getValue().size() * entry.getKey().getConsumptionMult() * getBaseConsumption(colony);
        }
        return (int) totalConsumption;
    }

    public int getTotalProduction(Colony colony){
        int foragerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FORAGER);
        int hunterCount = colony.getAssignedRoleCount(GameConstants.ROLE_HUNTER);
        int farmerCount = colony.getAssignedRoleCount(GameConstants.ROLE_FARMER);

        double plantCollection = foragerCount * 0.5 * getCollectingRate(colony);
        double proteinCollection = hunterCount * getCollectingRate(colony);
        int collectionPerHour = (int) (plantCollection + proteinCollection);
        int totalProductionRate = (int) (Math.min((getConversionRate(colony) * farmerCount) * 60, collectionPerHour)) * 3 * 24; 
        
        return Math.min(totalProductionRate, getMushroomsCapacity(colony));
    }
}