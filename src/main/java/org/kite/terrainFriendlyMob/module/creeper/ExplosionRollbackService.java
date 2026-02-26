package org.kite.terrainFriendlyMob.module.creeper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;
import org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ExplosionRollbackService {
    private final Plugin plugin;
    private final List<BlockState> affectedblockStateList;
    private final Location explosionCenter;
    private List<BlockState> sortedAffectedBlockStateList;
    private int currentIndex = 0;
    private int currentTickDelay; // 当前周期的延迟（单位：tick）

    // --- 可调节的参数 ---
    private final int INITIAL_DELAY_TICKS = 20; // 初始延迟：1秒
    private final double DECAY_FACTOR = 0.95;    // 衰减系数 (0~1)。越小，减速越快。
    private final int MIN_DELAY_TICKS = 1;      // 最小延迟：防止过快导致服务器卡顿
    private final int BATCH_SIZE = 1;           // 每批恢复的方块数

    private static final List<RollbackStrategy> AVAILABLE_STRATEGIES = Arrays.asList(
            new LayeredRestoreStrategy(),
            new LayeredSpiralRestoreStrategy(),
            new SphericalRestoreStrategy(),
            new SpiralRestoreStrategy(),
            new TierSpiralRestoreStrategy()
    );

    public ExplosionRollbackService(Plugin plugin, List<BlockState> affectedblockStateList, Location explosionCenter) {
        this.plugin = plugin;
        this.affectedblockStateList = affectedblockStateList;
        this.explosionCenter = explosionCenter;
        this.currentTickDelay = INITIAL_DELAY_TICKS;
    }

    public void rollback(){
        Random random = new Random();
        RollbackStrategy randomStrategy = AVAILABLE_STRATEGIES.get(random.nextInt(AVAILABLE_STRATEGIES.size()));
        RollbackContext rollbackContext = new RollbackContext(randomStrategy);
        sortedAffectedBlockStateList = rollbackContext.executeStrategy(affectedblockStateList, explosionCenter);
        Bukkit.getRegionScheduler().runDelayed(plugin, explosionCenter, task -> rollbackNextBatch(), INITIAL_DELAY_TICKS);
    }

    private void rollbackNextBatch() {
        if (currentIndex >= sortedAffectedBlockStateList.size()) {
            return ; // 所有方块已恢复
        }

        // 恢复当前批次
        int endIndex = Math.min(currentIndex + BATCH_SIZE, sortedAffectedBlockStateList.size());
        for (int i = currentIndex; i < endIndex; i++) {
            BlockState state = sortedAffectedBlockStateList.get(i);
            if(!state.getLocation().getBlock().getType().isAir()) continue;
            state.update(true, false);
            state.update(true, false);
            // 可选：在这里添加恢复音效或粒子效果
            state.getWorld().playSound(state.getLocation(), state.getBlock().getBlockSoundGroup().getPlaceSound(), 1, 1);
        }
        currentIndex = endIndex;

        // 计算并应用指数衰减延迟
        if (currentIndex < sortedAffectedBlockStateList.size()) {
            // 1. 计算下一个周期的延迟
            currentTickDelay = (int) Math.max(MIN_DELAY_TICKS, // 保持最小延迟
                    currentTickDelay * DECAY_FACTOR // 应用指数衰减
            );

            // 2. 通过临时取消并重新调度任务来实现可变延迟
            Bukkit.getRegionScheduler().runDelayed(plugin, explosionCenter, task -> {rollbackNextBatch();}, currentTickDelay);
        }
    }
}
