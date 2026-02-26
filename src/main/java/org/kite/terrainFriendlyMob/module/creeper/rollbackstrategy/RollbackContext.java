package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.List;

public class RollbackContext {
    private RollbackStrategy rollbackStrategy;

    public RollbackContext(RollbackStrategy rollbackStrategy) {
        this.rollbackStrategy = rollbackStrategy;
    }

    public List<BlockState> executeStrategy(List<BlockState> affectedBlockStateList, Location explosionCenter){
        return rollbackStrategy.sortAffectedBlockKList(affectedBlockStateList, explosionCenter);
    }
}
