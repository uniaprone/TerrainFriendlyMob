package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.List;

public interface RollbackStrategy {
    List<BlockState> sortAffectedBlockKList(List<BlockState> affectedBlockStateKList, Location explosionCenter);
}
