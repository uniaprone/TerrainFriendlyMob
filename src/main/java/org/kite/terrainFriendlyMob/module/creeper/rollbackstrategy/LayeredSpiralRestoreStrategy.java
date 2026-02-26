package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.*;

public class LayeredSpiralRestoreStrategy implements RollbackStrategy {

    private static final double SPIRAL_STEP = 1.0; // 水平螺旋的圈层厚度

    @Override
    public List<BlockState> sortAffectedBlockKList(List<BlockState> blocks, Location explosionCenter) {
        // 1. 按Y坐标分组
        Map<Integer, List<BlockState>> layers = new TreeMap<>(); // TreeMap自动按键(Y)升序排列
        for (BlockState block : blocks) {
            layers.computeIfAbsent(block.getY(), k -> new ArrayList<>()).add(block);
        }

        List<BlockState> finalOrder = new ArrayList<>();

        // 2. 从低到高，逐层处理
        for (List<BlockState> layerBlocks : layers.values()) {
            // 3. 对当前层的所有方块进行独立的水平螺旋排序
            List<BlockState> sortedLayer = sortSingleLayer(layerBlocks, explosionCenter);
            // 4. 将排序后的整层方块加入最终列表
            finalOrder.addAll(sortedLayer);
        }

        return finalOrder;
    }

    private List<BlockState> sortSingleLayer(List<BlockState> layerBlocks, Location center) {
        // 此方法逻辑与您提供的 `TierSpiralRestoreStrategy` 中对水平螺旋键值的计算和排序逻辑完全一致
        // 但它仅对同一Y层的方块进行排序，完全忽略Y值。
        List<BlockWithHorizontalKey> list = new ArrayList<>();
        for (BlockState block : layerBlocks) {
            list.add(new BlockWithHorizontalKey(block, center));
        }
        list.sort(Comparator.comparingDouble(a -> a.horizontalSpiralKey));

        List<BlockState> result = new ArrayList<>();
        for (BlockWithHorizontalKey bk : list) {
            result.add(bk.block);
        }
        return result;
    }

    // BlockWithHorizontalKey 辅助类的内部逻辑与您提供的代码中 `calculateHorizontalSpiralKey` 方法完全相同
    private static class BlockWithHorizontalKey {
        final BlockState block;
        final double horizontalSpiralKey; // 核心：决定水平面上的螺旋顺序
        BlockWithHorizontalKey(BlockState block, Location center) {
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