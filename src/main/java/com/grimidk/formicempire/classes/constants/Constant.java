package com.grimidk.formicempire.classes.constants;

import javax.swing.ImageIcon;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

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
        return icon;
    }
}
