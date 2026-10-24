/* SPDX-License-Identifier: GPL-3.0-or-later; see LICENSE.txt and PORT-NOTICE.md. */
package com.dre.brewery.localization;

import com.dre.brewery.configuration.configurer.TranslationManager;
import com.dre.brewery.utility.BUtil;
import com.dre.brewery.utility.StringParser;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.util.*;

/** Presentation catalogs only. Never changes recipe identifiers or saved brew data. */
public final class ContentTranslations {
    private static final Map<String, Object> EN = load("en");
    private static final Map<String, Object> RU = load("ru");

    private ContentTranslations() {}

    public static boolean russian() {
        return TranslationManager.getInstance().getActiveTranslation().fileName().equals("ru.yml");
    }

    private static Map<String, Object> load(String language) {
        try (InputStream input = ContentTranslations.class.getResourceAsStream("/content/" + language + ".yml")) {
            if (input == null) throw new IllegalStateException("Missing content catalog: " + language);
            return new Yaml().load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read content catalog: " + language, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> entry(Map<String, Object> catalog, String group, String id) {
        Map<String, Object> entries = (Map<String, Object>) catalog.get(group);
        return entries == null ? Map.of() : (Map<String, Object>) entries.getOrDefault(id, Map.of());
    }

    private static List<String> texts(Map<String, Object> catalog, String group, String id, String field) {
        Object value = entry(catalog, group, id).get(field);
        if (value instanceof String text) {
            return field.equals("name") ? Arrays.asList(text.split("/")) : List.of(text);
        }
        if (value instanceof List<?> list) return list.stream().map(Object::toString).toList();
        return List.of();
    }

    private static String rendered(String field, String text) {
        return field.equals("lore") ? StringParser.parseQuality(text, StringParser.ParseType.LORE).second() : BUtil.color(text);
    }

    public static String translate(String group, String id, String field, String text) {
        return translate(group, id, field, text, russian());
    }

    /** Exact stock values are translated; administrator replacements stay as written. */
    public static String translate(String group, String id, String field, String text, boolean russian) {
        if (id == null || text == null) return text;
        List<String> english = texts(EN, group, id, field);
        List<String> russianTexts = texts(RU, group, id, field);
        for (int i = 0; i < Math.min(english.size(), russianTexts.size()); i++) {
            String en = rendered(field, english.get(i));
            String ru = rendered(field, russianTexts.get(i));
            if (BUtil.color(text).equals(en) || BUtil.color(text).equals(ru)) return russian ? ru : en;
        }
        return text;
    }

    public static boolean stockName(String group, String id, String displayedName) {
        if (id == null || displayedName == null) return false;
        String candidate = withoutWhite(displayedName);
        for (Map<String, Object> catalog : List.of(EN, RU)) {
            for (String name : texts(catalog, group, id, "name")) {
                if (candidate.equals(withoutWhite(name))) return true;
            }
        }
        return false;
    }

    public static String withoutWhite(String text) {
        String colored = BUtil.color(text);
        return colored.startsWith("§f") ? colored.substring(2) : colored;
    }

    public static String translateCauldronName(String name) {
        for (String id : groupIds("cauldron")) {
            if (stockName("cauldron", id, name)) {
                return translate("cauldron", id, "name", withoutWhite(name));
            }
        }
        return name;
    }

    /** Existing tagged custom-lore lines, including the example cauldron recipe. */
    public static String translateStockLore(String recipeId, String line) {
        String result = translate("recipes", recipeId, "lore", line);
        if (!Objects.equals(result, line)) return result;
        for (String id : groupIds("cauldron")) {
            result = translate("cauldron", id, "lore", line);
            if (!Objects.equals(result, line)) return result;
        }
        return line;
    }

    @SuppressWarnings("unchecked")
    public static Set<String> groupIds(String group) {
        return Collections.unmodifiableSet(((Map<String, Object>) EN.get(group)).keySet());
    }

    public static String woodName(String original) {
        List<String> en = List.of("any", "birch", "oak", "jungle", "spruce", "acacia", "dark oak",
            "crimson", "warped", "mangrove", "cherry", "bamboo", "cut copper", "pale oak", "none");
        List<String> ru = List.of("любая древесина", "берёза", "дуб", "тропическое дерево", "ель", "акация", "тёмный дуб",
            "багровый гриб", "искажённый гриб", "мангровое дерево", "вишня", "бамбук", "резная медь", "бледный дуб", "нет древесины");
        for (int i = 0; i < en.size(); i++) {
            if (original.equalsIgnoreCase(en.get(i)) || original.equalsIgnoreCase(ru.get(i)))
                return russian() ? ru.get(i) : en.get(i);
        }
        return original;
    }
}
