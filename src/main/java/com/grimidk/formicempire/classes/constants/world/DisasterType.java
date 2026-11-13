package com.grimidk.formicempire.classes.constants.world;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

import java.util.List;
import javax.swing.ImageIcon;

public class DisasterType extends Constant {
    private final String descriptionKey;
    private final List<String> spritePaths;

    public DisasterType(int id, String nameKey, String descriptionKey, ImageIcon icon, String... spritePaths) {
        super(id, nameKey, icon);
        this.descriptionKey = descriptionKey;
        this.spritePaths = spritePaths == null ? List.of() : List.of(spritePaths);
    }

    public String getDescription() {
        return LanguageStrings.get(descriptionKey);
    }

    public String getDescriptionKey() {
        return descriptionKey;
    }

    public String getSpritePath() {
        return spritePaths.isEmpty() ? null : spritePaths.get(0);
    }

    public List<String> getSpritePaths() {
        return spritePaths;
    }
}

