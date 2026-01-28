package com.grimidk.formicempire.classes.constants.misc;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;

public class Species extends Constant {
    private final String scientific;
    private final Assimilation assimilation;
    private final Set<Upgrade> baseUpgrades;

    public Species(int id, String name, String scientific, Assimilation assimilation, Set<Upgrade> baseUpgrades, ImageIcon icon) {
        super(id, name, icon);
        this.scientific = scientific;
        this.assimilation = assimilation;
        this.baseUpgrades = baseUpgrades;
    }

    // (no icon)
    public Species(int id, String name, String scientific, Assimilation assimilation, Set<Upgrade> baseUpgrades) {
        this(id, name, scientific, assimilation, baseUpgrades, null);
    }

    public String getScientific() {
        return scientific;
    }

    public Assimilation getAssimilation() {
        return assimilation;
    }

    public Set<Upgrade> getBaseUpgrades() {
        return baseUpgrades;
    }

}
