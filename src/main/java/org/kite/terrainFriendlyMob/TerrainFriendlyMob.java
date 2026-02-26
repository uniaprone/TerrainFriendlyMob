package org.kite.terrainFriendlyMob;

import org.bukkit.plugin.java.JavaPlugin;
import org.kite.terrainFriendlyMob.command.ModuleCommandExecutor;
import org.kite.terrainFriendlyMob.module.creeper.CreeperModule;
import org.kite.terrainFriendlyMob.module.enderdragon.EnderDragonModule;
import org.kite.terrainFriendlyMob.module.enderman.EndermanModule;

import java.util.*;

public final class TerrainFriendlyMob extends JavaPlugin {
    private final Map<String, Module> allModules = new HashMap<>();
    private final Map<String, Module> activeModules = new LinkedHashMap<>();
    private ConfigManager configManager;
    private ModuleCommandExecutor commandExecutor;

    @Override
    public void onEnable() {
        // 初始化配置管理器
        configManager = new ConfigManager(this);

        // 初始化所有可用模块
        registerModule("creeper", new CreeperModule());
        registerModule("enderman", new EndermanModule());
        registerModule("enderdragon", new EnderDragonModule());

        // 加载配置并启用应启用的模块
        loadModuleStates();

        // 初始化命令执行器
        commandExecutor = new ModuleCommandExecutor(this);

        // 注册命令
        Objects.requireNonNull(getCommand("tfm")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("tfm")).setTabCompleter(commandExecutor);

        getLogger().info("地形友好生物插件已启用！");
    }

    @Override
    public void onDisable() {
        // 禁用所有活动模块
        for (Module module : activeModules.values()) {
            try {
                module.onDisable();
                getLogger().info("模块 " + module.getName() + " 已禁用");
            } catch (Exception e) {
                getLogger().severe("模块 " + module.getName() + " 禁用失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        activeModules.clear();

        getLogger().info("地形友好生物插件已禁用！");
    }

    // ========== 模块管理方法（供命令执行器调用） ==========

    public List<String> getAllModuleIds() {
        return new ArrayList<>(allModules.keySet());
    }

    public Module getModule(String moduleId) {
        return allModules.get(moduleId);
    }

    public boolean isModuleActive(String moduleId) {
        return activeModules.containsKey(moduleId);
    }

    public boolean enableModule(String moduleId, boolean saveConfig) {
        Module module = allModules.get(moduleId);
        if (module == null || activeModules.containsKey(moduleId)) {
            return false;
        }

        try {
            module.onEnable();
            activeModules.put(moduleId, module);

            if (saveConfig) {
                configManager.setModuleEnabled(moduleId, true);
            }

            getLogger().info("模块 " + module.getName() + " 已启用");
            return true;
        } catch (Exception e) {
            getLogger().severe("启用模块 " + module.getName() + " 失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean disableModule(String moduleId, boolean saveConfig) {
        Module module = activeModules.get(moduleId);
        if (module == null) {
            return false;
        }

        try {
            module.onDisable();
            activeModules.remove(moduleId);

            if (saveConfig) {
                configManager.setModuleEnabled(moduleId, false);
            }

            getLogger().info("模块 " + module.getName() + " 已禁用");
            return true;
        } catch (Exception e) {
            getLogger().severe("禁用模块 " + module.getName() + " 失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========== 内部方法 ==========

    private void registerModule(String id, Module module) {
        module.init(this);
        allModules.put(id.toLowerCase(), module);
        getLogger().info("注册模块: " + module.getName() + " (ID: " + id + ")");
    }

    private void loadModuleStates() {
        for (Map.Entry<String, Module> entry : allModules.entrySet()) {
            String moduleId = entry.getKey();
            Module module = entry.getValue();

            // 从配置读取模块状态，默认启用
            boolean enabled = configManager.isModuleEnabled(moduleId, true);

            if (enabled) {
                enableModule(moduleId, false); // 不保存到配置（因为正在加载）
            } else {
                getLogger().info("模块 " + module.getName() + " 在配置中被禁用");
            }
        }
    }
}