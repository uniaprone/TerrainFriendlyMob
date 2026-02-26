package org.kite.terrainFriendlyMob.module.enderman;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;
import org.kite.terrainFriendlyMob.Module;

public class EndermanModule implements Module, Listener {
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
        plugin.getLogger().info("末影人模块已禁用");
    }

    @Override
    public String getName() {
        return "EndermanModule";
    }

    @EventHandler
    public void onEndermanPickup(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.ENDERMAN) return;

        if (event.getTo() != org.bukkit.Material.AIR) {
            return;
        }

        event.setCancelled(true);
    }
}
