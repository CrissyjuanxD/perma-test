package tech.sebazcrc.mobcap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import tech.sebazcrc.mobcap.commands.MobCapCommand;
import tech.sebazcrc.mobcap.commands.MobCapTabCompleter;
import tech.sebazcrc.mobcap.config.MobCapConfig;
import tech.sebazcrc.mobcap.spawn.CustomSpawnManager;

import java.util.Objects;

public class MobCapPlugin extends JavaPlugin implements Listener {
    private MobCapManager mobCapManager;
    private MobCapConfig config;
    private CustomSpawnManager spawnManager;

    @Override
    public void onEnable() {
        // Crear configuración
        saveDefaultConfig();
        config = new MobCapConfig(this);
        
        // Inicializar managers
        mobCapManager = MobCapManager.getInstance(this, config);
        spawnManager = new CustomSpawnManager(this, config);
        
        // Registrar comandos
        MobCapCommand commandExecutor = new MobCapCommand(mobCapManager, config);
        MobCapTabCompleter tabCompleter = new MobCapTabCompleter();
        
        Objects.requireNonNull(getCommand("mobcap")).setExecutor(commandExecutor);
        Objects.requireNonNull(getCommand("mobcap")).setTabCompleter(tabCompleter);
        Objects.requireNonNull(getCommand("mobcapinfo")).setExecutor(commandExecutor);
        
        // Registrar eventos
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(spawnManager, this);
        
        getLogger().info("MobCapPlugin enabled successfully!");
        
        // Mensaje de inicio
        Bukkit.getConsoleSender().sendMessage("§f§m------------------------------------------");
        Bukkit.getConsoleSender().sendMessage("             §e§lMOBCAP MANAGER");
        Bukkit.getConsoleSender().sendMessage("     §7- Versión: §e" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("     §7- Sistema de MobCap avanzado activado");
        Bukkit.getConsoleSender().sendMessage("     §7- Spawn personalizado: §a" + (config.isCustomSpawnEnabled() ? "Activado" : "Desactivado"));
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
            Bukkit.getScheduler().runTaskLater(this, () -> {
                mobCapManager.handleNewWorld(event.getWorld());
            }, 20L);
        }
    }

    public MobCapManager getMobCapManager() {
        return mobCapManager;
    }
    
    public MobCapConfig getMobCapConfig() {
        return config;
    }
    
    public CustomSpawnManager getSpawnManager() {
        return spawnManager;
    }
}