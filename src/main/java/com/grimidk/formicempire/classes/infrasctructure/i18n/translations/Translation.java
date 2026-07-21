package com.grimidk.formicempire.classes.infrasctructure.i18n.translations;

import java.util.Map;

public interface Translation {
    String getLanguageName();
    Map<String, String> getStrings();
}
