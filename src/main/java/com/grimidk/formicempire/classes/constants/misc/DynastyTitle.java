package com.grimidk.formicempire.classes.constants.misc;

import com.grimidk.formicempire.classes.constants.Constant;
import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

public class DynastyTitle extends Constant {
    private final String formatKey;

    public DynastyTitle(int id, String nameKey, String formatKey) {
        super(id, nameKey, null);
        this.formatKey = formatKey;
    }

    public String getFormatKey() {
        return formatKey;
    }

    public String formatName(String baseName) {
        return String.format(LanguageStrings.get(formatKey), baseName);
    }
}
