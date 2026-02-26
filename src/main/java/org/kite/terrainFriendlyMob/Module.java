package org.kite.terrainFriendlyMob;

import org.bukkit.plugin.Plugin;

public interface Module {
    void init(Plugin plugin);
    void onEnable();
    void onDisable();
    String getName();
}
