/* SPDX-License-Identifier: GPL-3.0-or-later; see LICENSE.txt and PORT-NOTICE.md. */
package com.dre.brewery.localization;

import com.dre.brewery.Brew;
import com.dre.brewery.BSealer;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.files.Lang;
import com.dre.brewery.lore.BrewLore;
import com.dre.brewery.recipe.BRecipe;
import com.dre.brewery.utility.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

/** Updates text only: no brewing, ageing, data serialization or effect changes. */
public final class ItemLanguage {
    private static final Map<String, Object> EN = language("en");
    private static final Map<String, Object> RU = language("ru");
    private static Map<BrewLore.Type, List<Rule>> rules = Map.of();

    private ItemLanguage() {}

    private static Map<String, Object> language(String language) {
        var input = Objects.requireNonNull(ItemLanguage.class.getResourceAsStream("/languages/" + language + ".yml"));
        try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return new Yaml().load(reader);
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }

    public static void reload() {
        Lang lang = ConfigManager.getConfig(Lang.class);
        Map<BrewLore.Type, List<Rule>> updated = new EnumMap<>(BrewLore.Type.class);
        for (Map<String, Object> source : List.of(EN, RU)) {
            add(updated, BrewLore.Type.INGR, source, lang, "Brew_Ingredients");
            add(updated, BrewLore.Type.WOOD, source, lang, "Brew_Woodtype");
            for (String key : List.of("Brew_Distilled", "Brew_LessDistilled", "Brew_times"))
                add(updated, BrewLore.Type.DISTILL, source, lang, key);
            for (String key : List.of("Brew_OneYear", "Brew_HundredsOfYears", "Brew_Years", "Brew_BarrelRiped"))
                add(updated, BrewLore.Type.AGE, source, lang, key);
            for (String key : List.of("Brew_Alc", "Brew_Alcoholic"))
                add(updated, BrewLore.Type.ALC, source, lang, key);
            add(updated, BrewLore.Type.BREWER, source, lang, "Brew_Brewer");
            for (boolean plural : List.of(false, true)) {
                String from = "&v1 " + source.get("Brew_minute")
                    + (plural ? source.get("Brew_MinutePluralPostfix") : "") + " " + source.get("Brew_fermented");
                String to = "&v1 " + lang.getEntry("Brew_minute")
                    + (plural ? lang.getEntry("Brew_MinutePluralPostfix") : "") + " " + lang.getEntry("Brew_fermented");
                add(updated, BrewLore.Type.COOK, from, to);
            }
            for (var entry : source.entrySet()) {
                if (entry.getKey().startsWith("Defect_") && entry.getValue() instanceof List<?> lines) {
                    List<String> targets = lang.getEntries(entry.getKey());
                    for (int i = 0; i < lines.size(); i++)
                        add(updated, BrewLore.Type.DEFECT, lines.get(i).toString(), targets.get(Math.min(i, targets.size() - 1)));
                }
            }
        }
        rules = updated;
        refreshOnline();
    }

    private static void add(Map<BrewLore.Type, List<Rule>> map, BrewLore.Type type,
                            Map<String, Object> source, Lang target, String key) {
        add(map, type, source.get(key).toString(), target.getEntry(key));
    }

    private static void add(Map<BrewLore.Type, List<Rule>> map, BrewLore.Type type, String from, String to) {
        String[] parts = BUtil.color(from).split("&v1", -1);
        String expression = Pattern.quote(parts[0]);
        if (parts.length == 2) {
            // Brewer names are opaque; never translate player names inside that capture.
            expression += (type == BrewLore.Type.BREWER ? "(.+)$" : "(.+?)") + Pattern.quote(parts[1]);
        }
        map.computeIfAbsent(type, ignored -> new ArrayList<>()).add(new Rule(Pattern.compile(expression), BUtil.color(to), parts.length == 2));
        // Long phrases must win over substrings such as "Distilled" in "Less Distilled".
        map.get(type).sort(Comparator.comparingInt((Rule rule) -> rule.pattern.pattern().length()).reversed());
    }

    private record Rule(Pattern pattern, String target, boolean capture) {
        String apply(String text) {
            Matcher matcher = pattern.matcher(text);
            if (!matcher.find()) return text;
            String replacement = capture ? target.replace("&v1", matcher.group(1)) : target;
            return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
        }
    }

    public static void refreshOnline() {
        for (var player : Bukkit.getOnlinePlayers()) {
            refresh(player.getInventory());
            refresh(player.getOpenInventory().getTopInventory());
            var view = player.getOpenInventory();
            var holder = view.getTopInventory().getHolder();
            Lang lang = ConfigManager.getConfig(Lang.class);
            if (holder instanceof com.dre.brewery.Barrel) view.setTitle(lang.getEntry("Etc_Barrel"));
            else if (holder instanceof BSealer) view.setTitle(lang.getEntry("Etc_SealingTable"));
            ItemStack cursor = player.getItemOnCursor();
            if (refresh(cursor)) player.setItemOnCursor(cursor);
        }
    }

    public static int refresh(Inventory inventory) {
        int changed = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (refresh(item)) { inventory.setItem(slot, item); changed++; }
        }
        return changed;
    }

    public static boolean refresh(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        ItemMeta before = meta.clone();
        Lang lang = ConfigManager.getConfig(Lang.class);
        if (meta.getPersistentDataContainer().has(BSealer.TAG_KEY, PersistentDataType.BYTE)) {
            if (meta.hasDisplayName() && knownServiceName(meta.getDisplayName(), "Etc_SealingTable"))
                setName(meta, "§e" + lang.getEntry("Etc_SealingTable"));
        } else if (meta instanceof PotionMeta) {
            // Reading the meta avoids the legacy ItemStack loader's data conversion side effects.
            Brew brew = Brew.get(meta);
            if (brew == null) return false;
            BRecipe recipe = brew.getCurrentRecipe();
            if (meta.hasDisplayName()) {
                String name = meta.getDisplayName();
                if (recipe != null && (ContentTranslations.stockName("recipes", recipe.getId(), name)
                    || Arrays.stream(recipe.getName()).anyMatch(raw -> ContentTranslations.withoutWhite(raw).equals(ContentTranslations.withoutWhite(name))))) {
                    setName(meta, BUtil.color("&f" + recipe.getName(brew.getQuality())));
                } else {
                    String translated = ContentTranslations.translateCauldronName(name);
                    if (!translated.equals(name)) setName(meta, BUtil.color("&f" + translated));
                    else for (String key : List.of("Brew_BadPotion", "Brew_DistillUndefined", "Brew_ThickBrew", "Brew_Undefined")) {
                        if (knownServiceName(name, key)) { setName(meta, "§f" + lang.getEntry(key)); break; }
                    }
                }
            }
            if (meta.hasLore()) {
                List<String> lines = new ArrayList<>(meta.getLore());
                List<String> originalLines = List.copyOf(lines);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    BrewLore.Type type = BrewLore.Type.get(line);
                    if (type == BrewLore.Type.CUSTOM) {
                        lines.set(i, type.id + ContentTranslations.translateStockLore(recipe == null ? null : recipe.getId(), line.substring(2)));
                    } else if (type != null) {
                        // Apply each kind once, from the longest matching template. No cascading replacements.
                        String translated = line;
                        for (Rule rule : rules.getOrDefault(type, List.of())) {
                            String candidate = rule.apply(line);
                            if (!candidate.equals(line)) { translated = candidate; break; }
                        }
                        // Age and distillation lines contain multiple independent words.
                        if (type == BrewLore.Type.AGE || type == BrewLore.Type.DISTILL) {
                            translated = translateFragments(type, line, lang);
                        }
                        if (type == BrewLore.Type.DEFECT) {
                            // Only the stock WrongWood templates contain this placeholder.
                            for (Map<String, Object> source : List.of(EN, RU)) {
                                for (Object template : (List<?>) source.get("Defect_WrongWood")) {
                                    String[] parts = template.toString().split("&v1", -1);
                                    Matcher wood = Pattern.compile(Pattern.quote(BUtil.color(parts[0])) + "(.+?)" + Pattern.quote(BUtil.color(parts[1]))).matcher(line);
                                    if (wood.find()) translated = translated.replace(wood.group(1), ContentTranslations.woodName(wood.group(1)));
                                }
                            }
                        }
                        lines.set(i, translated);
                    }
                }
                if (!lines.equals(originalLines)) {
                    // Preserve unmodified rich lines from other plugins, including font,
                    // hover/click events and decorations that the legacy view cannot represent.
                    var components = new ArrayList<>(Objects.requireNonNull(meta.lore()));
                    var serializer = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();
                    for (int i = 0; i < lines.size(); i++) {
                        if (!lines.get(i).equals(originalLines.get(i))) components.set(i, serializer.deserialize(lines.get(i)));
                    }
                    meta.lore(components);
                }
            }
        } else return false;
        if (meta.equals(before)) return false;
        item.setItemMeta(meta);
        return true;
    }

    private static String translateFragments(BrewLore.Type type, String text, Lang lang) {
        List<String> keys = type == BrewLore.Type.AGE
            ? List.of("Brew_OneYear", "Brew_HundredsOfYears", "Brew_BarrelRiped", "Brew_Years")
            : List.of("Brew_LessDistilled", "Brew_Distilled", "Brew_times");
        Map<String, String> replacements = new HashMap<>();
        for (Map<String, Object> source : List.of(EN, RU)) for (String key : keys)
            replacements.put(BUtil.color(source.get(key).toString()), lang.getEntry(key));
        List<String> tokens = replacements.keySet().stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
        Matcher matcher = Pattern.compile(tokens.stream().map(Pattern::quote).collect(java.util.stream.Collectors.joining("|"))).matcher(text);
        StringBuilder output = new StringBuilder();
        while (matcher.find()) matcher.appendReplacement(output, Matcher.quoteReplacement(replacements.get(matcher.group())));
        matcher.appendTail(output);
        return output.toString();
    }

    private static boolean knownServiceName(String name, String key) {
        String plain = org.bukkit.ChatColor.stripColor(name);
        return plain.equals(org.bukkit.ChatColor.stripColor(BUtil.color(EN.get(key).toString())))
            || plain.equals(org.bukkit.ChatColor.stripColor(BUtil.color(RU.get(key).toString())));
    }

    private static void setName(ItemMeta meta, String name) {
        if (!name.equals(meta.getDisplayName())) meta.setDisplayName(name);
    }
}
