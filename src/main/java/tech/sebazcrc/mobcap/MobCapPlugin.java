package tech.sebazcrc.mobcap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import tech.sebazcrc.mobcap.commands.MobCapCommand;
import tech.sebazcrc.mobcap.commands.MobCapTabCompleter;

import java.util.Objects;

public class MobCapPlugin extends JavaPlugin implements Listener {
    private MobCapManager mobCapManager;

    @Override
    public void onEnable() {
        // Inicializar el manager
        mobCapManager = MobCapManager.getInstance(this);
        
        // Registrar comandos
        MobCapCommand commandExecutor = new MobCapCommand(mobCapManager);
        MobCapTabCompleter tabCompleter = new MobCapTabCompleter();
        
        Objects.requireNonNull(getCommand("mobcap")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("mobcap")).setTabCompleter(tabCompleter);
        Objects.requireNonNull(getCommand("mobcapinfo")).setExecutor(commandExecutor);
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, this);
        
        getLogger().info("MobCapPlugin enabled successfully!");
        
        // Mensaje de inicio similar al de Permadeath
        Bukkit.getConsoleSender().sendMessage("§f§m------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("             §e§lMOBCAP MANAGER");
        Bukkit.getConsoleSender().sendMessage("     §7- Versión: §e" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("     §7- Sistema de MobCap avanzado activado");
        Bukkit.getConsoleSender().sendMessage("§f§m------------------------------------------");
    }

    @Override
    public void onDisable() {
        if (mobCapManager != null) {
            mobCapManager.shutdown();
        }
        
        getLogger().info("MobCapPlugin disabled successfully!");
        
        Bukkit.getConsoleSender().sendMessage("§f§m------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("             §e§lMOBCAP MANAGER");
        Bukkit.getConsoleSender().sendMessage("     §7- Plugin desactivado correctamente");
        Bukkit.getConsoleSender().sendMessage("§f§m------------------------------------------");
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (mobCapManager != null && mobCapManager.isInitialized()) {
            // Aplicar mob cap a mundos recién cargados
            Bukkit.getScheduler().runTaskLater(this, () -> {
                mobCapManager.handleNewWorld(event.getWorld());
            }, 20L); // Esperar 1 segundo para que el mundo se cargue completamente
        }
    }

    public MobCapManager getMobCapManager() {
        return mobCapManager;
    }
}