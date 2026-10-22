package com.grimidk.formicempire.classes.constants;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;
import com.grimidk.formicempire.classes.infrasctructure.registries.GameConstants;

import javax.swing.ImageIcon;

public class Constant {
    private final int id;
    private final String nameKey;
    private final ImageIcon icon;

    public Constant(int id, String nameKey, ImageIcon icon) {
        this.id = id;
        this.nameKey = nameKey;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return LanguageStrings.get(nameKey);
    }

    public String getNameKey() {
        return nameKey;
    }
    
    public ImageIcon getIcon() {
        if (icon != null) {
            return icon;
        }
        return GameConstants.ICON_UNKNOWN;
    }
}
