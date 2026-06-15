package com.grimidk.formicempire.classes.constants.misc;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.repositories.LanguageStrings;

public class Species extends Constant {
    private final String scientific;
    private final String directory;
    private final Assimilation assimilation;
    private final Set<Upgrade> baseUpgrades;

    public Species(int id, String name, String scientific, String directory, Assimilation assimilation, Set<Upgrade> baseUpgrades, ImageIcon icon) {
        super(id, name, icon);
        this.scientific = scientific;
        this.directory = directory;
        this.assimilation = assimilation;
        this.baseUpgrades = baseUpgrades;
    }

    // (no icon)
    public Species(int id, String name, String scientific, String directory, Assimilation assimilation, Set<Upgrade> baseUpgrades) {
        this(id, name, scientific, directory, assimilation, baseUpgrades, null);
    }

    public String getScientific() {
        return LanguageStrings.get(scientific);
    }

    public String getDirectory() {
        return directory;
    }

    public Assimilation getAssimilation() {
        return assimilation;
    }

    public Set<Upgrade> getBaseUpgrades() {
        return baseUpgrades;
    }

}
