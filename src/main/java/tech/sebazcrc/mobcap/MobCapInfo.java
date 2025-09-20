package tech.sebazcrc.mobcap;

import java.util.Map;

public class MobCapInfo {
    private final int baseMobCap;
    private final boolean doubleMobCapEnabled;
    private final int effectiveMobCap;
    private final Map<String, Integer> originalLimits;
    private final Map<String, Integer> currentLimits;
    private final int playerCount;
    private final boolean optimizationActive;

    public MobCapInfo(int baseMobCap, boolean doubleMobCapEnabled, int effectiveMobCap,
                     Map<String, Integer> originalLimits, Map<String, Integer> currentLimits,
                     int playerCount, boolean optimizationActive) {
        this.baseMobCap = baseMobCap;
        this.doubleMobCapEnabled = doubleMobCapEnabled;
        this.effectiveMobCap = effectiveMobCap;
        this.originalLimits = originalLimits;
        this.currentLimits = currentLimits;
        this.playerCount = playerCount;
        this.optimizationActive = optimizationActive;
    }

    // Getters
    public int getBaseMobCap() { return baseMobCap; }
    public boolean isDoubleMobCapEnabled() { return doubleMobCapEnabled; }
    public int getEffectiveMobCap() { return effectiveMobCap; }
    public Map<String, Integer> getOriginalLimits() { return originalLimits; }
    public Map<String, Integer> getCurrentLimits() { return currentLimits; }
    public int getPlayerCount() { return playerCount; }
    public boolean isOptimizationActive() { return optimizationActive; }
}