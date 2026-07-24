package com.grimidk.formicempire.classes.entities.critter;

import java.util.LinkedList;
import java.util.Queue;

import com.grimidk.formicempire.classes.constants.critter.ant.AntRole;
import com.grimidk.formicempire.classes.constants.critter.ant.AntSubtypeProfile;
import com.grimidk.formicempire.classes.constants.critter.ant.AntType;
import com.grimidk.formicempire.classes.constants.misc.ResourceType;
import com.grimidk.formicempire.classes.entities.dynasty.Colony;
import com.grimidk.formicempire.classes.entities.services.colony.AntSubtypeService;
import com.grimidk.formicempire.classes.entities.spatial.NeoPoint;
import com.grimidk.formicempire.classes.infrasctructure.registries.DeathCause;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameNumbers;
import com.grimidk.formicempire.classes.infrasctructure.registries.WorldSpaces;

public class Ant extends Critter {
    private AntType type;
    private AntRole role;
    private AntSubtypeProfile subtypeProfile;
    private float tempRes;    
    private ResourceType carrying;
    private ResourceType carryingSec;
    private AntType carryingAnt;   
    private String causeOfDeath;
    private boolean isOnTrade;
    private boolean isNuptial;
    private boolean parasiticMiteInfected;
    /** Next redeploy heals with Boost Regen multiplier when true. */
    private boolean boostRegenPending;
    /** When true, this defender absorbs hits that would otherwise hit queens. */
    private boolean shieldingActive;

    public Ant(Colony colony, AntType type) {
        super(GameConstants.TYPE_ANT); 
        
        this.type = type;
        this.role = null;
        this.subtypeProfile = AntSubtypeProfile.standard();
        this.carrying = null;
        this.carryingSec = null;
        this.carryingAnt = null;
        this.causeOfDeath = "Unknown"; 
        this.isOnTrade = false;
        this.isNuptial = false;
        
        this.setStatus(GameConstants.STATUS_ALIVE);
        this.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * type.getHealtMult()))); 
        this.setHealth(this.getMaxHealth());
        this.setAge(0);
        
        this.tempRes = colony.getBaseTempRes();
        
        this.setRegen((int)(colony.getBaseRegen() * type.getRegenMult()));
        this.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult());
        this.setAttack((int)(colony.getBaseAttack() * type.getAttackMult()));
        this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        this.setDefense(GameNumbers.clampDefensePercent(type.getDefenseMult()));        
        this.setSpeed(colony.getBaseSpeed() * type.getSpeedMult());
        
        this.setDimension(WorldSpaces.OVERWORLD);
        if (AntSubtypeService.isEligibleType(type)) {
            AntSubtypeService.applySubtypeStats(this, colony);
        }
    }
    
    public AntType getAntType() { return type; }
    public void setAntType(AntType type) { this.type = type; }

    public AntRole getRole() { return role; }
    public void setRole(AntRole role) { this.role = role; }

    public AntSubtypeProfile getSubtypeProfile() { return subtypeProfile; }
    public void setSubtypeProfile(AntSubtypeProfile subtypeProfile) {
        this.subtypeProfile = subtypeProfile != null ? subtypeProfile : AntSubtypeProfile.standard();
    }

    public float getTempRes() { return tempRes; }
    public void setTempRes(float tempRes) { this.tempRes = tempRes; }

    public ResourceType getCarrying() { return carrying; }
    public void setCarrying(ResourceType carrying) { this.carrying = carrying; }

    public ResourceType getCarryingSec() { return carryingSec; }
    public void setCarryingSec(ResourceType carryingSec) { this.carryingSec = carryingSec; }

    public AntType getCarryingAnt() { return carryingAnt; }
    public void setCarryingAnt(AntType carryingAnt) { this.carryingAnt = carryingAnt; }
    
    public String getCauseOfDeath() { return causeOfDeath; }

    public boolean isOnTrade() { return isOnTrade; }
    public void setOnTrade(boolean onTrade) { this.isOnTrade = onTrade; }

    public boolean isNuptial() { return isNuptial; }
    public void setNuptial(boolean isNuptial) { this.isNuptial = isNuptial; }

    public boolean isParasiticMiteInfected() { return parasiticMiteInfected; }
    public void setParasiticMiteInfected(boolean parasiticMiteInfected) {
        this.parasiticMiteInfected = parasiticMiteInfected;
    }

    public boolean isBoostRegenPending() {
        return boostRegenPending;
    }

    public void setBoostRegenPending(boolean boostRegenPending) {
        this.boostRegenPending = boostRegenPending;
    }

    public boolean isShieldingActive() {
        return shieldingActive;
    }

    public void setShieldingActive(boolean shieldingActive) {
        this.shieldingActive = shieldingActive;
    }

    // --- Navigation Queue ---
    private Queue<NeoPoint> route = new LinkedList<>();

    public void setRoute(Queue<NeoPoint> route) {
        this.route = route;
    }
    
    public Queue<NeoPoint> getRoute() {
        return this.route;
    }
    
    public boolean hasRoute() {
        return this.route != null && !this.route.isEmpty();
    }
    
    public NeoPoint getNextRoutePoint() {
        if (this.route != null && !this.route.isEmpty()) {
            return this.route.poll();
        }
        return null;
    }
    
    public void clearRoute() {
        if (this.route != null) {
            this.route.clear();
        }
    }

    public void clearLoad() {
        this.carrying = null;
        this.carryingSec = null;
        this.carryingAnt = null;
    }

    @Override
    public void goDie() {
        goDie(null, DeathCause.OTHER);
    }

    public void goDie(Colony colony, String reason) {
        this.type = GameConstants.TYPE_DEAD; 
        this.causeOfDeath = reason;
        
        this.clearLoad();        
        this.clearRoute();
        this.parasiticMiteInfected = false;
        super.goDie();
    }

    public void transform(Colony colony, AntType newType) {
        AntSubtypeProfile preserved = subtypeProfile;
        this.type = newType;
        if (AntSubtypeService.isEligibleType(newType)) {
            this.subtypeProfile = preserved != null ? preserved : AntSubtypeProfile.standard();
            this.setAge(0);
            AntSubtypeService.applySubtypeStats(this, colony);
        } else {
            this.subtypeProfile = AntSubtypeProfile.standard();
            this.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * newType.getHealtMult())));
            this.setHealth(this.getMaxHealth());
            this.setAge(0);
            this.setRegen((int)(colony.getBaseRegen() * newType.getRegenMult()));
            this.setConsumption(colony.getBaseConsumption() * newType.getConsumptionMult());
            this.setAttack((int)(colony.getBaseAttack() * newType.getAttackMult()));
            this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * newType.getAttackSpeedMult()));
            this.setDefense(GameNumbers.clampDefensePercent(newType.getDefenseMult()));
            this.setSpeed(colony.getBaseSpeed() * newType.getSpeedMult());
        }
    }

    public void updateStatsFromColony(Colony colony) {
        if (AntSubtypeService.isEligibleType(type)) {
            AntSubtypeService.applySubtypeStats(this, colony);
            return;
        }
        this.setMaxHealth(Math.max(0, Math.round(colony.getBaseHealth() * type.getHealtMult())));
        
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        
        this.tempRes = colony.getBaseTempRes();
        this.setRegen((int)(colony.getBaseRegen() * type.getRegenMult()));
        this.setConsumption(colony.getBaseConsumption() * type.getConsumptionMult());
        this.setAttack((int)(colony.getBaseAttack() * type.getAttackMult()));
        this.setAttackSpeed((int)(colony.getBaseAttackSpeed() * type.getAttackSpeedMult()));
        this.setDefense(GameNumbers.clampDefensePercent(type.getDefenseMult()));        
        this.setSpeed(colony.getBaseSpeed() * type.getSpeedMult());
    }
}
