package tech.sebazcrc.mobcap.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobCapTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("mobcapinfo")) {
            return completions; // No hay argumentos para mobcapinfo
        }

        if (!command.getName().equalsIgnoreCase("mobcap")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("info", "set", "double", "reset", "reload");
            for (String subcommand : subcommands) {
                if (subcommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    if (!subcommand.equals("set") && !subcommand.equals("double") && 
                        !subcommand.equals("reset") && !subcommand.equals("reload") || 
                        sender.hasPermission("mobcap.admin")) {
                        completions.add(subcommand);
                    } else if (subcommand.equals("info")) {
                        completions.add(subcommand);
                    }
                }
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") && sender.hasPermission("mobcap.admin")) {
                completions.addAll(Arrays.asList("70", "140", "200", "280"));
            } else if (args[0].equalsIgnoreCase("double") && sender.hasPermission("mobcap.admin")) {
                completions.addAll(Arrays.asList("on", "off", "true", "false", "enable", "disable"));
            }
        }

        return completions;
    }
}