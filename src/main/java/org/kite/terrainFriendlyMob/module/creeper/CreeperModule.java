package org.kite.terrainFriendlyMob.module.creeper;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.kite.terrainFriendlyMob.Module;

public class CreeperModule implements Module {
    private Plugin plugin;
    private CreeperExplosionManager creeperExplosionManager;
    private RandomFireworkGenerator randomFireworkGenerator;
    private CreeperExplodeListener creeperExplodeListener;

    @Override
    public void init(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        randomFireworkGenerator = new RandomFireworkGenerator(plugin);
        creeperExplosionManager = new CreeperExplosionManager(plugin, randomFireworkGenerator);
        creeperExplodeListener = new CreeperExplodeListener(creeperExplosionManager);
        Bukkit.getPluginManager().registerEvents(creeperExplodeListener, plugin);
    }

    @Override
    public void onDisable() {
        if (creeperExplodeListener != null) {
            HandlerList.unregisterAll(creeperExplodeListener);
        }

        plugin.getLogger().info("苦力怕模块已禁用");
    }

    @Override
    public String getName() {
        return "CreeperModule";
    }
}
