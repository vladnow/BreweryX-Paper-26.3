/*
 * BreweryX Bukkit-Plugin for an alternate brewing process
 * Copyright (C) 2024 The Brewery Team
 *
 * This file is part of BreweryX.
 *
 * BreweryX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BreweryX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BreweryX. If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package com.dre.brewery.configuration.files;

import com.dre.brewery.configuration.AbstractOkaeriConfigFile;
import com.dre.brewery.configuration.ConfigManager;
import com.dre.brewery.configuration.annotation.Footer;
import com.dre.brewery.configuration.annotation.LocalizedComment;
import com.dre.brewery.configuration.annotation.OkaeriConfigFileOptions;
import com.dre.brewery.configuration.sector.RecipesSector;
import com.dre.brewery.configuration.sector.capsule.ConfigRecipe;
import eu.okaeri.configs.annotation.Header;
import eu.okaeri.configs.annotation.CustomKey;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.nio.file.Files;
import java.io.IOException;
import java.io.UncheckedIOException;

@Header("!!! IMPORTANT: BreweryX configuration files do NOT support external comments! If you add any comments, they will be overwritten !!!")
@Footer({ "",
    "More recipe ideas:",
    "Dandelion Liquor",
    "Beetroot Spirit",
    "Poppy Liquor: Macum/Grand Poppy",
    "Bamboo Liquor: Chu Yeh Ching",
    "Cachaca",
    "Cognac",
    "Sake",
    "Buorbon",
    "Moonshine",
    "Different Wines",
    "Brandy",
    "Amaretto",
    "etc. as well as variations like,",
    "Pumpkin Spice Beer",
    "Melon Vodka",
    "",
    "There are a lot of items in Minecraft like Vines, Milk and items added by plugins that would make great ingredients."
})
@OkaeriConfigFileOptions("recipes.yml")
@Setter
public class RecipesFile extends AbstractOkaeriConfigFile {


    @LocalizedComment("recipesFile.header")
    private Map<String, ConfigRecipe> recipes = new RecipesSector().getCapsules();

    @CustomKey("additional-recipes-version")
    private int additionalRecipesVersion = 0;

    /** Install this pack once; existing IDs (including disabled/legacy entries) always win. */
    public void installAdditionalRecipes() {
        if (additionalRecipesVersion >= 1) return;
        var legacy = ConfigManager.getConfig(Config.class).getRecipes();
        var defaults = new RecipesSector().getCapsules();
        var added = new java.util.ArrayList<String>();
        if (!isFirstCreation()) {
            var backup = getBindFile().resolveSibling("recipes.before-custom-drinks-beta4.yml.bak");
            try {
                if (!Files.exists(backup)) Files.copy(getBindFile(), backup);
            } catch (IOException exception) {
                throw new UncheckedIOException("Cannot back up recipes before adding drinks", exception);
            }
        }
        for (String id : List.of("liquid_crystal", "jagermeister", "crimson_drink", "royal_drink", "wildwood_mixture")) {
            if (!recipes.containsKey(id) && !legacy.containsKey(id)) {
                recipes.put(id, defaults.get(id));
                added.add(id);
            }
        }
        additionalRecipesVersion = 1;
        try {
            save();
        } catch (RuntimeException exception) {
            additionalRecipesVersion = 0;
            added.forEach(recipes::remove);
            throw exception;
        }
    }

    /** One-time ingredient correction for the two requested drinks, including legacy configs. */
    public void replaceTallGrass() {
        if (additionalRecipesVersion >= 2) return;
        Config legacyConfig = ConfigManager.getConfig(Config.class);
        var localChanges = grassChanges(recipes);
        var legacyChanges = grassChanges(legacyConfig.getRecipes());
        if (!localChanges.isEmpty()) backupBeforeGrassChange(this);
        if (!legacyChanges.isEmpty()) backupBeforeGrassChange(legacyConfig);
        var originals = new java.util.IdentityHashMap<ConfigRecipe, List<String>>();
        localChanges.keySet().forEach(recipe -> originals.put(recipe, recipe.getIngredients()));
        legacyChanges.keySet().forEach(recipe -> originals.put(recipe, recipe.getIngredients()));
        int previousVersion = additionalRecipesVersion;
        try {
            localChanges.forEach(ConfigRecipe::setIngredients);
            legacyChanges.forEach(ConfigRecipe::setIngredients);
            if (!legacyChanges.isEmpty()) legacyConfig.save();
            additionalRecipesVersion = 2;
            save();
        } catch (RuntimeException exception) {
            originals.forEach(ConfigRecipe::setIngredients);
            additionalRecipesVersion = previousVersion;
            throw exception;
        }
    }

    private static Map<ConfigRecipe, List<String>> grassChanges(Map<String, ConfigRecipe> entries) {
        var changes = new java.util.IdentityHashMap<ConfigRecipe, List<String>>();
        for (String id : List.of("jagermeister", "wildwood_mixture")) {
            ConfigRecipe recipe = entries.get(id);
            if (recipe == null || recipe.getIngredients() == null) continue;
            List<String> replacement = recipe.getIngredients().stream().map(ingredient -> {
                if (ingredient == null) return null;
                String[] parts = ingredient.split("/", 2);
                return parts[0].trim().equalsIgnoreCase("TALL_GRASS")
                    ? "SHORT_GRASS" + (parts.length == 2 ? "/" + parts[1] : "") : ingredient;
            }).toList();
            if (!replacement.equals(recipe.getIngredients())) changes.put(recipe, replacement);
        }
        return changes;
    }

    private static void backupBeforeGrassChange(AbstractOkaeriConfigFile file) {
        var path = file.getBindFile();
        var backup = path.resolveSibling(path.getFileName() + ".before-short-grass-beta5.bak");
        try {
            if (!Files.exists(backup)) Files.copy(path, backup);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot back up configuration before changing grass", exception);
        }
    }

    // Backwards compatibility, merges recipes from the default config
    public Map<String, ConfigRecipe> getRecipes() {
        Map<String, ConfigRecipe> map = new HashMap<>(this.recipes);
        map.putAll(ConfigManager.getConfig(Config.class).getRecipes());

        // Filter out the disabled ones now
        map.values().removeIf(recipe -> {
            if (recipe.getEnabled() == null) {
                return false;
            }
            return !recipe.getEnabled();
        });

        return map;
    }
}
