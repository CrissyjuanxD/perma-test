package tech.sebazcrc.mobcap.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tech.sebazcrc.mobcap.MobCapInfo;
import tech.sebazcrc.mobcap.MobCapManager;

public class MobCapCommand implements CommandExecutor {
    private final MobCapManager mobCapManager;

    public MobCapCommand(MobCapManager mobCapManager) {
        this.mobCapManager = mobCapManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("mobcapinfo")) {
            return handleMobCapInfo(sender);
        }

        if (!command.getName().equalsIgnoreCase("mobcap")) {
            return false;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                return handleMobCapInfo(sender);
            case "set":
                return handleSetMobCap(sender, args);
            case "double":
                return handleDoubleMobCap(sender, args);
            case "reset":
                return handleResetMobCap(sender);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleMobCapInfo(CommandSender sender) {
        MobCapInfo info = mobCapManager.getMobCapInfo();
        
        sender.sendMessage("§e§l=== MobCap Information ===");
        sender.sendMessage("§7Base MobCap: §a" + info.getBaseMobCap());
        sender.sendMessage("§7Double MobCap: " + (info.isDoubleMobCapEnabled() ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage("§7Effective MobCap: §b" + info.getEffectiveMobCap());
        sender.sendMessage("§7Online Players: §e" + info.getPlayerCount());
        sender.sendMessage("§7Optimization Active: " + (info.isOptimizationActive() ? "§aYes" : "§cNo"));
        sender.sendMessage("");
        sender.sendMessage("§e§lWorld Details:");
        
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            int original = info.getOriginalLimits().getOrDefault(worldName, -1);
            int current = info.getCurrentLimits().getOrDefault(worldName, -1);
            
            sender.sendMessage(String.format("§7%s: §f%d §7(original: §8%d§7)", 
                worldName, current, original));
        }
        
        return true;
    }

    private boolean handleSetMobCap(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobcap.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUso: /mobcap set <número>");
            return true;
        }

        try {
            int mobCap = Integer.parseInt(args[1]);
            if (mobCap < 1 || mobCap > 500) {
                sender.sendMessage("§cEl valor debe estar entre 1 y 500.");
                return true;
            }

            mobCapManager.setBaseMobCap(mobCap);
            sender.sendMessage("§aMobCap base establecido a: §b" + mobCap);
            
            if (sender instanceof Player) {
                Bukkit.getLogger().info(sender.getName() + " changed base mob cap to: " + mobCap);
            }
            
        } catch (NumberFormatException e) {
            sender.sendMessage("§cDebes introducir un número válido.");
        }

        return true;
    }

    private boolean handleDoubleMobCap(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobcap.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        if (args.length < 2) {
            boolean currentStatus = mobCapManager.isDoubleMobCapEnabled();
            mobCapManager.setDoubleMobCap(!currentStatus);
            String newStatus = !currentStatus ? "activado" : "desactivado";
            sender.sendMessage("§eDoble MobCap " + newStatus + ".");
        } else {
            String action = args[1].toLowerCase();
            boolean enable;
            
            if (action.equals("on") || action.equals("true") || action.equals("enable")) {
                enable = true;
            } else if (action.equals("off") || action.equals("false") || action.equals("disable")) {
                enable = false;
            } else {
                sender.sendMessage("§cUso: /mobcap double [on/off]");
                return true;
            }

            mobCapManager.setDoubleMobCap(enable);
            String status = enable ? "activado" : "desactivado";
            sender.sendMessage("§eDoble MobCap " + status + ".");
        }

        if (sender instanceof Player) {
            Bukkit.getLogger().info(sender.getName() + " toggled double mob cap: " + 
                mobCapManager.isDoubleMobCapEnabled());
        }

        return true;
    }

    private boolean handleResetMobCap(CommandSender sender) {
        if (!sender.hasPermission("mobcap.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        mobCapManager.resetMobCap();
        sender.sendMessage("§aMobCap restablecido a valores originales.");
        
        if (sender instanceof Player) {
            Bukkit.getLogger().info(sender.getName() + " reset mob cap to original values");
        }

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("mobcap.admin")) {
            sender.sendMessage("§cNo tienes permiso para usar este comando.");
            return true;
        }

        // Reaplicar configuración actual
        mobCapManager.setBaseMobCap(mobCapManager.getBaseMobCap());
        sender.sendMessage("§aMobCap recargado correctamente.");
        
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e§l=== MobCap Commands ===");
        sender.sendMessage("§7/mobcapinfo §f- Muestra información detallada del MobCap");
        sender.sendMessage("§7/mobcap info §f- Alias de mobcapinfo");
        
        if (sender.hasPermission("mobcap.admin")) {
            sender.sendMessage("§7/mobcap set <número> §f- Establece el MobCap base");
            sender.sendMessage("§7/mobcap double [on/off] §f- Activa/desactiva doble MobCap");
            sender.sendMessage("§7/mobcap reset §f- Restablece valores originales");
            sender.sendMessage("§7/mobcap reload §f- Recarga la configuración");
        }
    }
}