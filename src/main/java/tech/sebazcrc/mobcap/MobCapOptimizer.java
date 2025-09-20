package tech.sebazcrc.mobcap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicInteger;

public class MobCapOptimizer {
    private final JavaPlugin plugin;
    private static final int HIGH_PLAYER_THRESHOLD = 60;
    private static final int HIGH_MOBCAP_THRESHOLD = 140;
    private static final double OPTIMIZATION_FACTOR = 0.75; // Reducir al 75%

    public MobCapOptimizer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int getOptimizedMobCap(int originalMobCap) {
        int playerCount = Bukkit.getOnlinePlayers().size();
        
        if (playerCount <= HIGH_PLAYER_THRESHOLD || originalMobCap < HIGH_MOBCAP_THRESHOLD) {
            return originalMobCap;
        }

        // Calcular factor de optimización basado en número de jugadores
        double optimizationFactor = calculateOptimizationFactor(playerCount);
        int optimizedCap = (int) (originalMobCap * optimizationFactor);
        
        // Asegurar un mínimo razonable
        int minimumCap = Math.max(70, originalMobCap / 3);
        optimizedCap = Math.max(optimizedCap, minimumCap);
        
        plugin.getLogger().info(String.format(
            "Optimizing mob cap: %d -> %d (players: %d, factor: %.2f)",
            originalMobCap, optimizedCap, playerCount, optimizationFactor
        ));
        
        return optimizedCap;
    }

    private double calculateOptimizationFactor(int playerCount) {
        if (playerCount <= HIGH_PLAYER_THRESHOLD) {
            return 1.0;
        }
        
        // Reducción gradual: más jugadores = más reducción
        // 60 jugadores = 100%, 80 jugadores = 75%, 100+ jugadores = 60%
        double reductionPerPlayer = 0.01; // 1% por jugador adicional
        int excessPlayers = playerCount - HIGH_PLAYER_THRESHOLD;
        double reduction = Math.min(0.4, excessPlayers * reductionPerPlayer); // Máximo 40% de reducción
        
        return Math.max(0.6, 1.0 - reduction); // Mínimo 60% del valor original
    }

    public void optimizeMobCap() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                int totalMobs = getTotalMobCount();
                int playerCount = Bukkit.getOnlinePlayers().size();
                
                // Si hay demasiados mobs por jugador, aplicar limpieza suave
                double mobsPerPlayer = playerCount > 0 ? (double) totalMobs / playerCount : totalMobs;
                
                if (mobsPerPlayer > 15) { // Más de 15 mobs por jugador
                    performSoftMobCleanup();
                }
                
            } catch (Exception e) {
                plugin.getLogger().warning("Error during mob cap optimization: " + e.getMessage());
            }
        });
    }

    private int getTotalMobCount() {
        AtomicInteger totalMobs = new AtomicInteger(0);
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Monster) {
                    totalMobs.incrementAndGet();
                }
            }
        }
        
        return totalMobs.get();
    }

    private void performSoftMobCleanup() {
        int removedMobs = 0;
        
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Monster && !(entity instanceof LivingEntity && 
                    ((LivingEntity) entity).getCustomName() != null)) {
                    
                    // Solo remover mobs que estén lejos de jugadores
                    if (entity.getNearbyEntities(32, 32, 32).stream()
                        .noneMatch(e -> e instanceof org.bukkit.entity.Player)) {
                        
                        entity.remove();
                        removedMobs++;
                        
                        // Limitar la cantidad de mobs removidos por ciclo
                        if (removedMobs >= 50) {
                            break;
                        }
                    }
                }
            }
            
            if (removedMobs >= 50) {
                break;
            }
        }
        
        if (removedMobs > 0) {
            plugin.getLogger().info("Soft mob cleanup: removed " + removedMobs + " distant mobs");
        }
    }
}