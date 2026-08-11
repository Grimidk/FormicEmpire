package com.grimidk.formicempire.classes.constants.dynasty;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;

public class Rank extends Constant {
    private final long population;
    private final String triggerTitleKey;
    private final String triggerMessageKey;
    private Upgrade unlockOnAnnounce;

    public Rank(int id, String name, long population, ImageIcon icon) {
        this(id, name, population, null, null, icon);
    }

    public Rank(int id, String name, long population, String triggerTitleKey, String triggerMessageKey, ImageIcon icon) {
        super(id, name, icon);
        this.population = population;
        this.triggerTitleKey = triggerTitleKey;
        this.triggerMessageKey = triggerMessageKey;
    }

    public long getPopulation() {
        return population;
    }

    public String getTriggerTitleKey() {
        return triggerTitleKey;
    }

    public String getTriggerMessageKey() {
        return triggerMessageKey;
    }

    public boolean hasTriggerPopup() {
        return triggerTitleKey != null && triggerMessageKey != null;
    }

    public Upgrade getUnlockOnAnnounce() {
        return unlockOnAnnounce;
    }

    public void setUnlockOnAnnounce(Upgrade unlockOnAnnounce) {
        this.unlockOnAnnounce = unlockOnAnnounce;
    }

    public boolean meetsOrExceeds(Rank other) {
        return other != null && getId() >= other.getId();
    }
}
