package org.kite.terrainFriendlyMob.command;

import org.kite.terrainFriendlyMob.Module;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.kite.terrainFriendlyMob.TerrainFriendlyMob;

import java.util.*;

public class ModuleCommandExecutor implements CommandExecutor, TabCompleter {
    private final TerrainFriendlyMob plugin;

    public ModuleCommandExecutor(TerrainFriendlyMob plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list":
                // 检查列表查看权限
                if (!sender.hasPermission("tfm.list") && !sender.hasPermission("tfm.admin")) {
                    sender.sendMessage("§c您没有权限查看模块列表。");
                    return true;
                }
                listModules(sender);
                break;

            case "help":
                sendHelp(sender);
                break;

            default:
                // 模块操作: <模块名> <enable|disable|status>
                if (args.length >= 2) {
                    handleModuleCommand(sender, subCommand, args[1].toLowerCase());
                } else {
                    sender.sendMessage("§c用法: /tfm <模块名> <enable|disable|status>");
                    sender.sendMessage("§c输入 /tfm list 查看所有模块");
                }
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> allModuleIds = new ArrayList<>(plugin.getAllModuleIds());

        if (args.length == 1) {
            // 第一級補全：可用的頂層命令
            // 只有擁有對應權限的用戶才看到相應的補全選項

            // 檢查是否有權限使用 list 命令
            if (sender.hasPermission("tfm.list")) {
                completions.add("list");
            }

            // 檢查是否有權限查看模塊狀態（status 命令）
            if (sender.hasPermission("tfm.module.status")) {
                // 如果用戶有權限查看模塊狀態，則顯示所有模塊名
                // 因為 /tfm <模塊名> status 需要模塊名
                completions.addAll(allModuleIds);
            }

            // 檢查是否有權限管理模塊（enable/disable）
            if (sender.hasPermission("tfm.module.manage")) {
                // 如果用戶有權限管理模塊，也顯示模塊名
                // 確保不重複添加
                for (String moduleId : allModuleIds) {
                    if (!completions.contains(moduleId)) {
                        completions.add(moduleId);
                    }
                }
            }

            // 幫助命令對所有用戶可見
            completions.add("help");
        }
        else if (args.length == 2) {
            String firstArg = args[0].toLowerCase();

            if (firstArg.equals("list") || firstArg.equals("help")) {
                // 這些命令沒有第二級參數
                return completions;
            }

            // 檢查第一個參數是否為有效的模塊名
            if (allModuleIds.contains(firstArg)) {
                // 根據用戶權限提供可用的操作補全
                boolean canManage = sender.hasPermission("tfm.module.manage");
                boolean canStatus = sender.hasPermission("tfm.module.status");

                if (canManage) {
                    completions.add("enable");
                    completions.add("disable");
                }

                if (canStatus) {
                    completions.add("status");
                }
            }
        }

        // 過濾已輸入的部分
        String currentArg = args[args.length - 1].toLowerCase();
        completions.removeIf(s -> !s.toLowerCase().startsWith(currentArg));

        return completions;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6======= 地形友好生物插件帮助 =======");

        // 根据权限显示可用的命令
        boolean hasListPerm = sender.hasPermission("tfm.list") || sender.hasPermission("tfm.admin");
        boolean hasManagePerm = sender.hasPermission("tfm.module.manage") || sender.hasPermission("tfm.admin");
        boolean hasStatusPerm = sender.hasPermission("tfm.module.status") || sender.hasPermission("tfm.admin");

        if (hasListPerm) {
            sender.sendMessage("§e/tfm list §7- 列出所有模块");
        }

        if (hasManagePerm) {
            sender.sendMessage("§e/tfm <模块名> enable §7- 启用模块");
            sender.sendMessage("§e/tfm <模块名> disable §7- 禁用模块");
        }

        if (hasStatusPerm) {
            sender.sendMessage("§e/tfm <模块名> status §7- 查看模块状态");
        }

        sender.sendMessage("§e/tfm help §7- 显示此帮助");

        if (hasListPerm || hasManagePerm || hasStatusPerm) {
            sender.sendMessage("§6可用模块: §acreeper, enderman, enderdragon");
        } else {
            sender.sendMessage("§c您没有任何可用的管理权限。");
        }
    }

    private void listModules(CommandSender sender) {
        sender.sendMessage("§6======= 模块列表 =======");
        for (String moduleId : plugin.getAllModuleIds()) {
            Module module = plugin.getModule(moduleId);
            boolean isActive = plugin.isModuleActive(moduleId);

            String status = isActive ? "§a● 启用" : "§c● 禁用";
            sender.sendMessage("§e" + moduleId + " §7(" + module.getName() + ") - " + status);
        }
        sender.sendMessage("§6使用 §e/tfm <模块名> <enable/disable> §6来切换状态");
    }

    private void handleModuleCommand(CommandSender sender, String moduleId, String action) {
        List<String> allModuleIds = plugin.getAllModuleIds();

        if (!allModuleIds.contains(moduleId)) {
            sender.sendMessage("§c未知模块: " + moduleId);
            sender.sendMessage("§c可用模块: " + String.join(", ", allModuleIds));
            return;
        }

        boolean isActive = plugin.isModuleActive(moduleId);

        switch (action) {
            case "enable":
            case "disable":
                // 检查模块管理权限
                if (!sender.hasPermission("tfm.module.manage") && !sender.hasPermission("tfm.admin")) {
                    sender.sendMessage("§c您没有权限启用或禁用模块。");
                    return;
                }

                if (action.equals("enable")) {
                    if (isActive) {
                        sender.sendMessage("§6模块 §e" + moduleId + " §6已经是启用状态");
                    } else if (plugin.enableModule(moduleId, true)) {
                        sender.sendMessage("§a模块 §e" + moduleId + " §a已成功启用");
                    } else {
                        sender.sendMessage("§c启用模块 §e" + moduleId + " §c失败，请查看控制台日志");
                    }
                } else {
                    if (!isActive) {
                        sender.sendMessage("§6模块 §e" + moduleId + " §6已经是禁用状态");
                    } else if (plugin.disableModule(moduleId, true)) {
                        sender.sendMessage("§a模块 §e" + moduleId + " §a已成功禁用");
                    } else {
                        sender.sendMessage("§c禁用模块 §e" + moduleId + " §c失败，请查看控制台日志");
                    }
                }
                break;

            case "status":
                // 检查状态查看权限
                if (!sender.hasPermission("tfm.module.status") && !sender.hasPermission("tfm.admin")) {
                    sender.sendMessage("§c您没有权限查看模块状态。");
                    return;
                }

                String status = isActive ? "§a启用" : "§c禁用";
                Module module = plugin.getModule(moduleId);
                sender.sendMessage("§6模块: §e" + module.getName());
                sender.sendMessage("§6ID: §e" + moduleId);
                sender.sendMessage("§6状态: " + status);
                break;

            default:
                sender.sendMessage("§c未知操作: " + action);
                sender.sendMessage("§c可用操作: enable, disable, status");
                break;
        }
    }
}