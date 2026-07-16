package com.grimidk.formicempire.classes.constants.misc;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.ImageIcon;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.constants.unlocks.Assimilation;
import com.grimidk.formicempire.classes.constants.unlocks.Upgrade;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class Species extends Constant {
    private final String scientific;
    private final String directory;
    private final Assimilation assimilation;
    private final Set<Upgrade> baseUpgrades;
    /** LanguageStrings keys for preferred dynasty theme names (not display text). */
    private final Set<String> preferredNameKeys;

    public Species(int id, String name, String scientific, String directory, Assimilation assimilation,
            Set<Upgrade> baseUpgrades, Set<String> preferredNameKeys, ImageIcon icon) {
        super(id, name, icon);
        this.scientific = scientific;
        this.directory = directory;
        this.assimilation = assimilation;
        this.baseUpgrades = baseUpgrades;
        this.preferredNameKeys = preferredNameKeys == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(preferredNameKeys));
    }

    public Species(int id, String name, String scientific, String directory, Assimilation assimilation,
            Set<Upgrade> baseUpgrades, Set<String> preferredNameKeys) {
        this(id, name, scientific, directory, assimilation, baseUpgrades, preferredNameKeys, null);
    }

    public String getScientific() {
        return LanguageStrings.get(scientific);
    }

    public String getDirectory() {
        return directory;
    }

    public String getZeroDroneSpriteFileName() {
        if (directory == null || directory.isEmpty()) {
            return "Omni.png";
        }
        String base = directory.endsWith("/") ? directory.substring(0, directory.length() - 1) : directory;
        if ("shiningblack".equals(base)) {
            return "ShiningBlack.png";
        }
        return Character.toUpperCase(base.charAt(0)) + base.substring(1) + ".png";
    }

    public Assimilation getAssimilation() {
        return assimilation;
    }

    public Set<Upgrade> getBaseUpgrades() {
        return baseUpgrades;
    }

    public Set<String> getPreferredNameKeys() {
        return preferredNameKeys;
    }
}
