package com.grimidk.formicempire.classes.infrasctructure.repositories.translations;

import java.util.Map;

public interface Translation {
    String getLanguageName();
    Map<String, String> getStrings();
}
