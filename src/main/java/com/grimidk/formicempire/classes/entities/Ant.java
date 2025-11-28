package com.grimidk.formicempire.classes.entities;

import com.grimidk.formicempire.classes.constants.ant.AntRole;
import com.grimidk.formicempire.classes.constants.ant.AntSubType;
import com.grimidk.formicempire.classes.constants.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.infrasctructure.repositories.GameConstants;

public class Ant extends Bug {
    private AntType type;
    private AntSubType subType;
    private AntRole role;
    private float tempRes;    
    private ResourceType carrying;

    public Ant(Colony colony, AntType type) {
        super(GameConstants.TYPE_ANT); 
        
        this.type = type;
        this.subType = null;
        this.role = null;
        this.carrying = null;
        
        this.setStatus(GameConstants.STATUS_ALIVE);
        this.setMaxHealth((int)(colony.getBaseHealth() * type.getHealtMult())); 
        this.setHealth(this.getMaxHealth());
        this.setAge(0);
        
        this.tempRes = colony.getBaseTempRes();
        
        this.setRegen((int)(colony.getBaseRegen() * type.getRegenMult()));
        this.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult());
        this.setAttack((int)(colony.getBaseAttack() * type.getAttackMult()));
        this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        this.setDefense((int)(colony.getBaseDefense() * type.getDefenseMult()));        
        this.setSpeed(colony.getBaseSpeed() * type.getSpeedMult());
        this.setSize((int)(colony.getBaseSize() * type.getSizeMult()));
        
        this.setDimension(0);
    }
    
    public AntType getAntType() { return type; }
    public void setAntType(AntType type) { this.type = type; }

    public AntSubType getSubType() { return subType; }
    public void setSubType(AntSubType subType) { this.subType = subType; }

    public AntRole getRole() { return role; }
    public void setRole(AntRole role) { this.role = role; }

    public float getTempRes() { return tempRes; }
    public void setTempRes(float tempRes) { this.tempRes = tempRes; }

    public ResourceType getCarrying() { return carrying; }
    public void setCarrying(ResourceType carrying) { this.carrying = carrying; }

    @Override
    public void goDie() {
        this.type = GameConstants.TYPE_DEAD; 
        this.carrying = null; 
        super.goDie();
    }

    public void transform(Colony colony, AntType newType) {
        this.type = newType;
        this.setMaxHealth((int)(colony.getBaseHealth() * newType.getHealtMult()));
        this.setHealth(this.getMaxHealth());
        this.setAge(0);
        this.setRegen((int)(colony.getBaseRegen() * newType.getRegenMult()));
        this.setConsumption(colony.getBaseConsumption() * newType.getConsumptionMult());
        this.setAttack((int)(colony.getBaseAttack() * newType.getAttackMult()));
        this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * newType.getAttackSpeedMult()));
        this.setDefense((int)(colony.getBaseDefense() * newType.getDefenseMult()));
        this.setSpeed(colony.getBaseSpeed() * newType.getSpeedMult());
        this.setSize((int)(colony.getBaseSize() * newType.getSizeMult()));
    }
}