package tech.sebazcrc.mobcap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class MobCapManager {
    private static MobCapManager instance;
    private final JavaPlugin plugin;
    private final Map<String, Integer> originalLimits;
    private final Map<String, Integer> currentLimits;
    private final MobCapOptimizer optimizer;
    
    private int baseMobCap = 70;
    private boolean doubleMobCapEnabled = false;
    private boolean isInitialized = false;
    private BukkitTask optimizationTask;

    private MobCapManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.originalLimits = new ConcurrentHashMap<>();
        this.currentLimits = new ConcurrentHashMap<>();
        this.optimizer = new MobCapOptimizer(plugin);
        initialize();
    }

    public static MobCapManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MobCapManager(plugin);
        }
        return instance;
    }

    private void initialize() {
        if (isInitialized) return;

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::initialize);
            return;
        }

        // Guardar límites originales
        for (World world : Bukkit.getWorlds()) {
            int originalLimit = world.getMonsterSpawnLimit();
            originalLimits.put(world.getName(), originalLimit);
            currentLimits.put(world.getName(), originalLimit);
        }

        // Iniciar tarea de optimización
        startOptimizationTask();
        
        isInitialized = true;
        plugin.getLogger().info("MobCapManager initialized successfully");
    }

    private void startOptimizationTask() {
        if (optimizationTask != null) {
            optimizationTask.cancel();
        }

        // Tarea que se ejecuta cada 30 segundos para optimizar
        optimizationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (shouldOptimize()) {
                optimizer.optimizeMobCap();
            }
        }, 600L, 600L); // 30 segundos inicial, luego cada 30 segundos
    }

    private boolean shouldOptimize() {
        int playerCount = Bukkit.getOnlinePlayers().size();
        int currentCap = getCurrentEffectiveMobCap();
        
        // Optimizar si hay más de 60 jugadores y mob cap es alto
        return playerCount > 60 && currentCap >= 140;
    }

    public synchronized void setBaseMobCap(int mobCapValue) {
        if (!isInitialized) {
            initialize();
        }

        if (this.baseMobCap == mobCapValue) {
            return;
        }

        this.baseMobCap = mobCapValue;
        applyMobCapToWorlds();
        
        plugin.getLogger().info("Base mob cap updated to: " + mobCapValue);
    }

    public synchronized void setDoubleMobCap(boolean enabled) {
        if (this.doubleMobCapEnabled == enabled) {
            return;
        }

        this.doubleMobCapEnabled = enabled;
        applyMobCapToWorlds();
        
        String status = enabled ? "enabled" : "disabled";
        plugin.getLogger().info("Double mob cap " + status);
        
        // Mensaje similar al de Permadeath
        if (enabled) {
            Bukkit.broadcastMessage("§e[MobCap] §eDoblando la mob-cap en todos los mundos.");
        } else {
            Bukkit.broadcastMessage("§e[MobCap] §eRestaurando mob-cap normal en todos los mundos.");
        }
    }

    private void applyMobCapToWorlds() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::applyMobCapToWorlds);
            return;
        }

        int effectiveMobCap = getCurrentEffectiveMobCap();

        for (World world : Bukkit.getWorlds()) {
            try {
                Integer currentLimit = currentLimits.get(world.getName());
                if (currentLimit == null || !currentLimit.equals(effectiveMobCap)) {
                    world.setMonsterSpawnLimit(effectiveMobCap);
                    currentLimits.put(world.getName(), effectiveMobCap);

                    plugin.getLogger().fine("Updated mob cap for " + world.getName() + 
                            " from " + currentLimit + " to " + effectiveMobCap);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, 
                    "Failed to update mob cap for world: " + world.getName(), e);
            }
        }
    }

    public int getCurrentEffectiveMobCap() {
        int effectiveCap = baseMobCap;
        if (doubleMobCapEnabled) {
            effectiveCap = baseMobCap * 2;
        }
        
        // Aplicar optimización si es necesario
        if (shouldOptimize()) {
            effectiveCap = optimizer.getOptimizedMobCap(effectiveCap);
        }
        
        return effectiveCap;
    }

    public void handleNewWorld(World world) {
        if (!isInitialized) return;

        int originalLimit = world.getMonsterSpawnLimit();
        originalLimits.put(world.getName(), originalLimit);

        int effectiveMobCap = getCurrentEffectiveMobCap();
        world.setMonsterSpawnLimit(effectiveMobCap);
        currentLimits.put(world.getName(), effectiveMobCap);
        
        plugin.getLogger().info("Applied mob cap to new world: " + world.getName() + 
                " (limit: " + effectiveMobCap + ")");
    }

    public void resetMobCap() {
        if (!isInitialized) return;

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, this::resetMobCap);
            return;
        }

        for (World world : Bukkit.getWorlds()) {
            Integer originalLimit = originalLimits.get(world.getName());
            if (originalLimit != null) {
                world.setMonsterSpawnLimit(originalLimit);
                currentLimits.put(world.getName(), originalLimit);
            }
        }
        
        baseMobCap = 70;
        doubleMobCapEnabled = false;
        
        plugin.getLogger().info("Mob cap reset to original values");
    }

    public MobCapInfo getMobCapInfo() {
        return new MobCapInfo(
            baseMobCap,
            doubleMobCapEnabled,
            getCurrentEffectiveMobCap(),
            originalLimits,
            currentLimits,
            Bukkit.getOnlinePlayers().size(),
            shouldOptimize()
        );
    }

    public void shutdown() {
        if (optimizationTask != null) {
            optimizationTask.cancel();
        }
        resetMobCap();
        instance = null;
        plugin.getLogger().info("MobCapManager shutdown completed");
    }

    // Getters
    public int getBaseMobCap() {
        return baseMobCap;
    }

    public boolean isDoubleMobCapEnabled() {
        return doubleMobCapEnabled;
    }

    public int getOriginalLimit(String worldName) {
        return originalLimits.getOrDefault(worldName, 70);
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}