package com.grimidk.formicempire.classes.interfaces.ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.swing.UIManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.grimidk.formicempire.classes.infrasctructure.i18n.LanguageStrings;

class UiOptionPaneLocalizationTest {

    @AfterEach
    void restoreEnglish() {
        LanguageStrings.setLanguage("en");
        UiOptionPane.applyLocalizedButtonTexts();
    }

    @Test
    void applyLocalizedButtonTextsUsesCurrentLanguage() {
        LanguageStrings.setLanguage("es");
        UiOptionPane.applyLocalizedButtonTexts();
        assertEquals(LanguageStrings.get(LanguageStrings.UI_YES), UIManager.getString("OptionPane.yesButtonText"));
        assertEquals(LanguageStrings.get(LanguageStrings.UI_NO), UIManager.getString("OptionPane.noButtonText"));
        assertEquals(LanguageStrings.get(LanguageStrings.UI_OK), UIManager.getString("OptionPane.okButtonText"));
        assertEquals(LanguageStrings.get(LanguageStrings.UI_CANCEL), UIManager.getString("OptionPane.cancelButtonText"));
        assertEquals("Sí", UIManager.getString("OptionPane.yesButtonText"));
        assertEquals("No", UIManager.getString("OptionPane.noButtonText"));
        assertEquals("Cancelar", UIManager.getString("OptionPane.cancelButtonText"));
    }
}
