package org.kite.terrainFriendlyMob.module.creeper;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CreeperExplosionManager {
    private final Plugin plugin;
    private final RandomFireworkGenerator randomFireworkGenerator;

    public CreeperExplosionManager(Plugin plugin, RandomFireworkGenerator randomFireworkGenerator) {
        this.plugin = plugin;
        this.randomFireworkGenerator = randomFireworkGenerator;
    }

    public void handleExplosion(List<BlockState> affectedBlockStateList, Location explosionCenter){
        if(affectedBlockStateList.isEmpty() || explosionCenter == null) return;
        //1.模拟爆炸效果
        Bukkit.getRegionScheduler().run(plugin, affectedBlockStateList.getFirst().getLocation(), task -> {
            for(BlockState blockState: affectedBlockStateList){
                blockState.getBlock().setType(Material.AIR, false);
            }
        });
        //2.回滚
        ExplosionRollbackService explosionRollbackService = new ExplosionRollbackService(plugin, affectedBlockStateList, explosionCenter);
        explosionRollbackService.rollback();
        //3.产生烟花效果
        randomFireworkGenerator.spawnRandomInstantFirework(explosionCenter);
    }
}
