package org.kite.terrainFriendlyMob.module.enderdragon;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.kite.terrainFriendlyMob.Module;

public class EnderDragonModule implements Module, Listener {
    private Plugin plugin;
    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        plugin.getLogger().info("末影龙保护模块已禁用");
    }

    @Override
    public String getName() {
        return "EnderDragonModule";
    }

    @EventHandler
    public void onEnderDragonDestroy(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) return;

        event.setCancelled(true);
    }
}

