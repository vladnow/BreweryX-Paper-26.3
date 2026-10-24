/* SPDX-License-Identifier: GPL-3.0-or-later; see LICENSE.txt and PORT-NOTICE.md. */
package com.dre.brewery.listeners;

import com.dre.brewery.BreweryPlugin;
import com.dre.brewery.localization.ItemLanguage;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;

/** Lazy text refresh: unopened/offline inventories need no world scan. */
public final class ItemLanguageListener implements Listener {
    private final java.util.Set<java.util.UUID> pending = new java.util.HashSet<>();
    private void later(Player player) {
        if (!pending.add(player.getUniqueId())) return;
        BreweryPlugin plugin = BreweryPlugin.getInstance();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            pending.remove(player.getUniqueId());
            if (!player.isOnline()) return;
            ItemLanguage.refresh(player.getInventory());
            ItemLanguage.refresh(player.getOpenInventory().getTopInventory());
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) { ItemLanguage.refresh(event.getPlayer().getInventory()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void open(InventoryOpenEvent event) {
        ItemLanguage.refresh(event.getInventory());
        ItemLanguage.refresh(event.getPlayer().getInventory());
        var holder = event.getInventory().getHolder();
        var lang = com.dre.brewery.configuration.ConfigManager.getConfig(com.dre.brewery.configuration.files.Lang.class);
        if (holder instanceof com.dre.brewery.Barrel) event.getView().setTitle(lang.getEntry("Etc_Barrel"));
        else if (holder instanceof com.dre.brewery.BSealer) event.getView().setTitle(lang.getEntry("Etc_SealingTable"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) later(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) later(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) later(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void held(PlayerItemHeldEvent event) { later(event.getPlayer()); }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interact(PlayerInteractEvent event) { ItemLanguage.refresh(event.getItem()); }

    @EventHandler
    public void craft(PrepareItemCraftEvent event) {
        var result = event.getInventory().getResult();
        if (ItemLanguage.refresh(result)) event.getInventory().setResult(result);
    }
}
