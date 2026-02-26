package org.kite.terrainFriendlyMob.module.creeper.rollbackstrategy;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 螺旋恢复策略 (改进版)
 * 从底部开始，在每一层上，从一个固定方向开始，按指定方向（顺时针/逆时针）由外向内螺旋恢复。
 */
public class SpiralRestoreStrategy implements RollbackStrategy {

    // --- 可配置参数 ---
    private final double startAngleRad; // 螺旋起始角度（弧度），例如 0 表示正北方向
    private final boolean clockwise;    // 是否为顺时针方向

    public SpiralRestoreStrategy() {
        this.startAngleRad = normalizeAngle(Math.random() * 2 * Math.PI);
        this.clockwise = Math.random() > 0.5;
    }

    @Override
    public List<BlockState> sortAffectedBlockKList(
            List<BlockState> blocks, Location explosionCenter) {

        // 1. 按Y坐标分组
        Map<Integer, List<BlockWithPolar>> layers = new TreeMap<>();

        for (BlockState block : blocks) {
            int yLevel = block.getY();
            Location blockLoc = block.getLocation();

            // 计算极坐标：角度和水平距离
            double angle = calculateAngleRelativeToCenter(blockLoc, explosionCenter);
            double distance = calculateHorizontalDistance(blockLoc, explosionCenter);

            layers.computeIfAbsent(yLevel, k -> new ArrayList<>())
                    .add(new BlockWithPolar(block, angle, distance));
        }

        // 2. 逐层进行螺旋排序
        List<BlockState> result = new ArrayList<>();

        for (List<BlockWithPolar> layer : layers.values()) {
            // 对当前层的所有方块进行螺旋排序
            layer.sort(this::compareForSpiralOrder);
            // 将排序后的方块加入结果列表
            for (BlockWithPolar bp : layer) {
                result.add(bp.block);
            }
        }

        return result;
    }

    /**
     * 为核心比较逻辑：决定两个方块的恢复先后顺序。
     * 目标：实现由外向内、沿固定方向旋转的螺旋。
     */
    private int compareForSpiralOrder(BlockWithPolar a, BlockWithPolar b) {
        // --- 第一优先级：角度“扇区” ---
        // 我们将360度的圆环划分成若干个扇区（例如16个），优先恢复起始角度扇区及其后的扇区。
        // 这确保了从固定方向开始，按旋转方向依次恢复各角度的方块。
        int sectorA = getAngleSector(a.angle);
        int sectorB = getAngleSector(b.angle);

        int sectorCompare = Integer.compare(sectorA, sectorB);
        if (sectorCompare != 0) {
            return clockwise ? sectorCompare : -sectorCompare; // 根据旋转方向调整顺序
        }

        // --- 第二优先级：同一扇区内，距离从远到近 ---
        // 这实现了“由外向内”的恢复效果。
        int distanceCompare = Double.compare(b.distance, a.distance); // 降序
        if (distanceCompare != 0) {
            return distanceCompare;
        }

        // --- 第三优先级：距离完全相同的情况下，按原始角度微调 ---
        // 确保顺序完全确定，避免随机性。
        return Double.compare(a.angle, b.angle);
    }

    /**
     * 根据设定的起始角度和旋转方向，将绝对角度映射到扇区编号。
     * 扇区编号从起始角度开始，沿旋转方向递增。
     */
    private int getAngleSector(double absoluteAngle) {
        // 1. 将角度归一化到 [0, 2π) 范围
        double normalizedAngle = normalizeAngle(absoluteAngle);

        // 2. 计算相对于起始角度的偏移角度
        double offsetAngle = normalizedAngle - startAngleRad;
        if (offsetAngle < 0) {
            offsetAngle += 2 * Math.PI;
        }

        // 3. 如果不为顺时针，则偏移角度需要反向计算
        if (!clockwise) {
            offsetAngle = 2 * Math.PI - offsetAngle;
            if (offsetAngle >= 2 * Math.PI) {
                offsetAngle -= 2 * Math.PI;
            }
        }

        // 4. 将偏移角度划分为扇区（例如每22.5度一个扇区，共16个）
        final int SECTORS = 16;
        int sector = (int) (offsetAngle / (2 * Math.PI / SECTORS));
        // 确保扇区号在 [0, SECTORS-1] 范围内
        return Math.min(sector, SECTORS - 1);
    }

    /**
     * 计算方块相对于爆炸中心的水平角度（弧度）。
     * 返回值的范围是 (-π, π]，0表示正北（-Z方向），π/2表示正东（+X方向）。
     */
    private double calculateAngleRelativeToCenter(Location blockLoc, Location center) {
        double dx = blockLoc.getX() - center.getX();
        double dz = blockLoc.getZ() - center.getZ();
        // Math.atan2(dz, dx) 的返回值：-π 到 π
        // 其中，atan2(负, 负) = -π（西北）， atan2(负, 正) = -0（东北）
        //        atan2(正, 正) = π（东南）， atan2(正, 负) = π（西南）
        // 为了更直观（0度为正北），我们通常使用 atan2(dx, -dz) 或 atan2(-dz, dx)。
        // 这里我们调整到以正北为0度，顺时针增加的角度系统。
        return Math.atan2(dx, -dz); // 调整为：0=正北，π/2=正东，π=正南，-π/2=正西
    }

    /**
     * 计算水平距离（忽略Y轴）。
     */
    private double calculateHorizontalDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * 将任意角度归一化到 [0, 2π) 范围。
     */
    private double normalizeAngle(double angle) {
        angle = angle % (2 * Math.PI);
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    /**
     * 辅助类，存储方块及其极坐标信息。
     */
    private static class BlockWithPolar {
        final BlockState block;
        final double angle;   // 相对于爆炸中心的角度（弧度），已根据 calculateAngleRelativeToCenter 调整
        final double distance; // 水平距离

        BlockWithPolar(BlockState block, double angle, double distance) {
            this.block = block;
            this.angle = angle;
            this.distance = distance;
        }
    }
}