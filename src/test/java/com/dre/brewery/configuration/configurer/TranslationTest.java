package com.dre.brewery.configuration.configurer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TranslationTest {
    @Test void russianCanBeSelected() { assertEquals("ru.yml", Translation.getTranslation("ru").fileName()); }
    @Test void englishCanBeSelected() { assertEquals(Translation.EN, Translation.getTranslation("en")); }
    @Test void localeAliasesAndCaseAreAccepted() {
        for (String value : new String[]{" RU ", "ru_RU", "ru-RU", "Russian", "русский", "ru.yml"})
            assertEquals("ru.yml", Translation.getTranslation(value).fileName(), value);
        for (String value : new String[]{"EN", "en-US", "en_GB", "English", "en.yml"})
            assertEquals(Translation.EN, Translation.getTranslation(value), value);
    }
    @Test void absentValueDoesNotCrash() {
        assertEquals(Translation.EN, Translation.getTranslation(null));
        assertEquals(Translation.EN, Translation.getTranslation("  "));
    }
    @Test void languageCannotEscapeItsDirectory() {
        for (String value : new String[]{"../ru", "../../config", "C:\\ru", "/ru", "ru/../../en"})
            assertEquals(Translation.EN, Translation.getTranslation(value), value);
    }
    @Test void otherBundledAndCustomLanguagesStillWork() {
        assertEquals("de.yml", Translation.getTranslation("de").fileName());
        assertEquals("custom_test.yml", Translation.getTranslation("custom_test").fileName());
        assertEquals("custom-test.yml", Translation.getTranslation("custom-test").fileName());
    }
}
