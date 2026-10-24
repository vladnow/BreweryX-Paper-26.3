package com.dre.brewery.configuration.configurer;

import com.dre.brewery.configuration.files.Lang;
import eu.okaeri.configs.annotation.CustomKey;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

class TranslationResourcesTest {
    private static Map<String,Object> read(String path) throws Exception {
        try(InputStream in=TranslationResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(in,path);
            return new Yaml().load(in);
        }
    }
    private static Set<String> placeholders(Object value) {
        Set<String> result=new TreeSet<>();
        var matcher=Pattern.compile("&v[0-9]+").matcher(String.valueOf(value));
        while(matcher.find()) result.add(matcher.group());
        return result;
    }
    private static Map<String,Object> flatten(Map<String,Object> data,String prefix) {
        Map<String,Object> result=new TreeMap<>();
        data.forEach((key,value)-> {
            String path=prefix+key;
            if(value instanceof Map<?,?> child) result.putAll(flatten((Map<String,Object>)child,path+"."));
            else result.put(path,value);
        });
        return result;
    }
    @Test void englishAndRussianHaveTheSameMessageKeys() throws Exception {
        assertEquals(read("/languages/en.yml").keySet(),read("/languages/ru.yml").keySet());
    }
    @Test void messagesKeepTheSameReplacementParameters() throws Exception {
        var en=read("/languages/en.yml");var ru=read("/languages/ru.yml");
        for(var key:en.keySet()) assertEquals(placeholders(en.get(key)),placeholders(ru.get(key)),key);
    }
    @Test void russianMessagesHaveRussianText() throws Exception {
        for(var entry:read("/languages/ru.yml").entrySet())
            assertTrue(Pattern.compile("[А-Яа-яЁё]").matcher(String.valueOf(entry.getValue())).find(),entry.getKey());
    }
    @Test void everyDeclaredMessageIsAvailableInBothLanguages() throws Exception {
        var en=read("/languages/en.yml");var ru=read("/languages/ru.yml");
        for(var field:Lang.class.getDeclaredFields()) {
            var key=field.getAnnotation(CustomKey.class);
            if(key!=null) {
                assertNotNull(en.get(key.value()),key.value()+" en");
                assertNotNull(ru.get(key.value()),key.value()+" ru");
            }
        }
    }
    @Test void russianConfigCommentsCoverEnglishKeys() throws Exception {
        var en=flatten(read("/config-langs/en.yml"),"");var ru=flatten(read("/config-langs/ru.yml"),"");
        Set<String> missing=new TreeSet<>(en.keySet());missing.removeAll(ru.keySet());
        assertTrue(missing.isEmpty(),"Missing Russian config comments: "+missing);
    }
}
