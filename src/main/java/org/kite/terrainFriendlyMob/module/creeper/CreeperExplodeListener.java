package org.kite.terrainFriendlyMob.module.creeper;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class CreeperExplodeListener implements Listener {
    private final CreeperExplosionManager creeperExplosionManager;

    public CreeperExplodeListener(CreeperExplosionManager creeperExplosionManager) {
        this.creeperExplosionManager = creeperExplosionManager;
    }

    @EventHandler
    public void CreeperExplodeEvent(EntityExplodeEvent event){
        if(event.getEntityType() != EntityType.CREEPER) return;
        List<BlockState> affectedBlockStateList = event.blockList().stream().map(Block::getState).toList();
        if(affectedBlockStateList.isEmpty()) return;
        event.blockList().clear();
        creeperExplosionManager.handleExplosion(affectedBlockStateList, event.getLocation());
    }
}
