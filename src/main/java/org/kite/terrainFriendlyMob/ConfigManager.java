package org.kite.terrainFriendlyMob;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        createConfig();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "modules.yml");
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            config = new YamlConfiguration();

            // 设置默认值
            config.set("modules.creeper.enabled", true);
            config.set("modules.enderman.enabled", true);
            config.set("modules.enderdragon.enabled", true);

            saveConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public boolean isModuleEnabled(String moduleId, boolean defaultValue) {
        return config.getBoolean("modules." + moduleId + ".enabled", defaultValue);
    }

    public void setModuleEnabled(String moduleId, boolean enabled) {
        config.set("modules." + moduleId + ".enabled", enabled);
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("保存配置文件失败: " + e.getMessage());
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}