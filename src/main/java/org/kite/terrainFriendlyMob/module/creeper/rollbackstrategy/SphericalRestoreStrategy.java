package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D球形恢复策略
 * 从爆炸中心向外恢复，先恢复远的，后恢复近的
 */
public class SphericalRestoreStrategy implements RollbackStrategy {

    @Override
    public List<BlockState> sortAffectedBlockKList(List<BlockState> blocks, Location explosionCenter) {
        List<BlockWith3DDistance> blockDataList = new ArrayList<>();

        // 计算每个方块到爆炸中心的距离
        for (BlockState block : blocks) {
            double distance = calculate3DDistance(block.getLocation(), explosionCenter);

            blockDataList.add(new BlockWith3DDistance(block, distance));
        }

        // 按距离从大到小排序（从远到近）
        blockDataList.sort((a, b) -> Double.compare(b.distance, a.distance));

        // 提取排序后的Block列表
        List<BlockState> result = new ArrayList<>();
        for (BlockWith3DDistance bd : blockDataList) {
            result.add(bd.block);
        }

        return result;
    }

    /**
     * 计算三维欧几里得距离
     */
    private double calculate3DDistance(Location blockLoc, Location center) {
        double dx = blockLoc.getX() - center.getX();
        double dy = blockLoc.getY() - center.getY();
        double dz = blockLoc.getZ() - center.getZ();

        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * 辅助类：存储方块数据及其距离
     */
    private static class BlockWith3DDistance {
        final BlockState block;
        final double distance;

        BlockWith3DDistance(BlockState block, double distance) {
            this.block = block;
            this.distance = distance;
        }
    }
}
