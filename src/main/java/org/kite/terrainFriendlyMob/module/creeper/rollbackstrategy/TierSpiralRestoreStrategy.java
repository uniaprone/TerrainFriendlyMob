package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import java.util.ArrayList;
import java.util.List;

/**
 * 改进的螺旋恢复策略
 * 实现“每圈从下到上”的恢复效果
 * 优先按Y坐标（从低到高）排序，然后在同层内按螺旋顺序恢复
 */
public class TierSpiralRestoreStrategy implements RollbackStrategy {
    // 可调节参数：定义“一圈”的厚度（距离单位）。
    private static final double SPIRAL_STEP = 1;

    @Override
    public List<BlockState> sortAffectedBlockKList(
            List<BlockState> blocks, Location explosionCenter) {

        // 为每个方块计算一个包含“螺旋水平顺序”和“高度”的复合键值
        List<BlockWithSpiralKey> blockKeys = new ArrayList<>();
        for (BlockState block : blocks) {
            blockKeys.add(new BlockWithSpiralKey(block, explosionCenter));
        }

        // 排序：先确保整体是水平螺旋顺序，在螺旋顺序相同的情况下，再按高度从低到高排序。
        blockKeys.sort((a, b) -> {
            // 1. 首先比较水平螺旋键值（决定从哪圈开始、按什么角度方向）
            int spiralKeyCompare = Double.compare(a.horizontalSpiralKey, b.horizontalSpiralKey);
            if (spiralKeyCompare != 0) {
                return spiralKeyCompare;
            }
            // 2. 如果处于螺旋中的同一水平位置（距离和角度都极其接近），则按Y坐标从低到高排序
            return Integer.compare(a.block.getY(), b.block.getY());
        });

        // 提取结果
        List<BlockState> result = new ArrayList<>();
        for (BlockWithSpiralKey bk : blockKeys) {
            result.add(bk.block);
        }
        return result;
    }

    /**
     * 辅助类：存储方块及其计算出的键值
     */
    private static class BlockWithSpiralKey {
        final BlockState block;
        final double horizontalSpiralKey; // 核心：决定水平面上的螺旋顺序

        BlockWithSpiralKey(BlockState block, Location center) {
            this.block = block;
            this.horizontalSpiralKey = calculateHorizontalSpiralKey(block.getLocation(), center);
        }

        /**
         * 计算决定水平螺旋顺序的键值。
         * 此键值确保恢复在水平面上呈现从外圈到内圈、沿固定方向旋转的效果。
         */
        private double calculateHorizontalSpiralKey(Location blockLoc, Location center) {
            double dx = blockLoc.getX() - center.getX();
            double dz = blockLoc.getZ() - center.getZ();

            double distance = Math.sqrt(dx * dx + dz * dz);
            double angle = Math.atan2(dz, dx); // [-PI, PI]

            // 角度归一化到 [0, 2π)
            double normalizedAngle = angle;
            if (normalizedAngle < 0) {
                normalizedAngle += 2 * Math.PI;
            }

            // 将连续距离离散化为“圈数”
            double circleIndex = Math.floor(distance / SPIRAL_STEP);

            // 构建水平螺旋键值。
            // 键值越小，恢复越靠前。
            // `-circleIndex`：确保外圈（圈数大）的键值更小，先恢复。
            // `+ (normalizedAngle / (2 * Math.PI))`：确保同圈内按角度顺序恢复。
            double horizontalSpiralKey = -circleIndex + (normalizedAngle / (2 * Math.PI));

            return horizontalSpiralKey;
        }
    }
}