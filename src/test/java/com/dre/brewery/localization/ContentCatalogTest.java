/* SPDX-License-Identifier: GPL-3.0-or-later; see LICENSE.txt and PORT-NOTICE.md. */
package com.dre.brewery.localization;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import static org.junit.jupiter.api.Assertions.*;

class ContentCatalogTest {
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Map<String, Object>>> read(String language) throws Exception {
        try (var in = getClass().getResourceAsStream("/content/" + language + ".yml")) {
            assertNotNull(in);
            return new Yaml().load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }
    @Test void everyStandardRecipeAndCauldronEntryHasTheSameTranslatedFields() throws Exception {
        var en=read("en"); var ru=read("ru");
        assertEquals(en.keySet(), ru.keySet());
        for (String group: en.keySet()) {
            assertEquals(en.get(group).keySet(),ru.get(group).keySet());
            for (String id:en.get(group).keySet()) {
                var a=en.get(group).get(id);var b=ru.get(group).get(id);
                assertEquals(a.keySet(),b.keySet(),id);
                for(String key:a.keySet()) {
                    assertTrue(Set.of("name","lore","drinkmessage","drinktitle").contains(key));
                    if(a.get(key) instanceof List<?> list) assertEquals(list.size(),((List<?>)b.get(key)).size(),id);
                    else assertEquals(a.get(key).toString().split("/").length,b.get(key).toString().split("/").length,id);
                }
            }
        }
    }
    @Test void catalogsCoverActualUpstreamDefaultsRatherThanOnlySomeDrinks() throws Exception {
        var en=read("en");
        for(var entry:Map.of("recipes","RecipesSector.java","cauldron","CauldronSector.java").entrySet()) {
            String source=Files.readString(Path.of("src/main/java/com/dre/brewery/configuration/sector",entry.getValue()));
            Matcher matcher=Pattern.compile("Config\\w+ (\\w+) = .*?\\.name\\(\"([^\"]+)\"\\)",Pattern.DOTALL).matcher(source);
            Set<String> actual=new HashSet<>();
            while(matcher.find()) {
                actual.add(matcher.group(1));
                assertEquals(matcher.group(2),en.get(entry.getKey()).get(matcher.group(1)).get("name"));
            }
            assertEquals(actual,en.get(entry.getKey()).keySet());
        }
    }
    @Test void everyRussianValueContainsCyrillic() throws Exception {
        for(var group:read("ru").values()) for(var fields:group.values()) for(Object value:fields.values()) {
            for(Object text:value instanceof List<?> list?list:List.of(value))
                assertTrue(text.equals("Jägermeister") || Pattern.compile("[А-Яа-яЁё]").matcher(text.toString()).find(),text.toString());
        }
    }
    @Test void qualitySelectorsAndFormattingCodesArePreserved() throws Exception {
        var en=read("en");var ru=read("ru");
        Pattern codes=Pattern.compile("^\\+*|&[0-9a-fk-or]");
        for(String group:en.keySet()) for(String id:en.get(group).keySet()) for(String key:en.get(group).get(id).keySet()) {
            Object a=en.get(group).get(id).get(key);Object b=ru.get(group).get(id).get(key);
            List<?> first=a instanceof List<?> list?list:List.of(a);
            List<?> second=b instanceof List<?> list?list:List.of(b);
            for(int i=0;i<first.size();i++) {
                assertEquals(codes.matcher(first.get(i).toString()).results().map(MatchResult::group).toList(),
                    codes.matcher(second.get(i).toString()).results().map(MatchResult::group).toList(),id+" "+key);
            }
        }
    }
}
