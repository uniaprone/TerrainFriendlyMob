package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LayeredRestoreStrategy implements RollbackStrategy {

    @Override
    public List<BlockState> sortAffectedBlockKList(List<BlockState> blocks, Location explosionCenter) {
        // 1. 按Y坐标分层
        Map<Integer, List<BlockDataWithDistance>> layers = new TreeMap<>();

        for (BlockState block : blocks) {
            int yLevel = block.getY();
            double horizontalDistance = calculateHorizontalDistance(block.getLocation(), explosionCenter);
            layers.computeIfAbsent(yLevel, k -> new ArrayList<>())
                    .add(new BlockDataWithDistance(block, horizontalDistance));
        }

        // 2. 逐层处理：从低到高
        List<BlockState> result = new ArrayList<>();

        for (List<BlockDataWithDistance> layer : layers.values()) {
            // 每层内按水平距离从大到小排序
            layer.sort((a, b) -> Double.compare(b.distance, a.distance));

            for (BlockDataWithDistance bd : layer) {
                result.add(bd.blockState);
            }
        }

        return result;
    }

    /**
     * 计算水平距离（忽略Y轴）
     */
    private double calculateHorizontalDistance(Location blockLoc, Location center) {
        double dx = blockLoc.getX() - center.getX();
        double dz = blockLoc.getZ() - center.getZ();

        return Math.sqrt(dx*dx + dz*dz);
    }

    private static class BlockDataWithDistance {
        final BlockState blockState;
        final double distance;

        BlockDataWithDistance(BlockState blockState, double distance) {
            this.blockState = blockState;
            this.distance = distance;
        }
    }
}
