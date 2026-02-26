package org.kite.terrainFriendlyMob.module.creeper;

import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * 随机烟花效果生成器
 * 专门用于生成一次性爆炸效果的随机烟花
 */
public class RandomFireworkGenerator {

    private final Plugin plugin;
    private final Random random;

    // 预定义的常用颜色池（Bukkit的Color类）
//    private static final List<Color> PREDEFINED_COLORS = Arrays.asList(
//            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
//            Color.AQUA, Color.BLACK, Color.FUCHSIA, Color.GRAY,
//            Color.LIME, Color.MAROON, Color.NAVY, Color.OLIVE,
//            Color.ORANGE, Color.PURPLE, Color.SILVER, Color.TEAL,
//            Color.WHITE
//    );
    private static final List<Color> PREDEFINED_COLORS = Arrays.asList(
            Color.RED,  Color.YELLOW,
            Color.AQUA, Color.FUCHSIA,
            Color.LIME,
            Color.ORANGE, Color.PURPLE,
            Color.WHITE
    );

    // 所有可用的烟花爆炸类型
    private static final FireworkEffect.Type[] FIREWORK_TYPES = FireworkEffect.Type.values();

    public RandomFireworkGenerator(Plugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * 在指定位置生成一个随机的一次性爆炸烟花
     * @param location 爆炸位置
     */
    public void spawnRandomInstantFirework(Location location) {
        if(random.nextBoolean()) return;
        // 调整Y轴，使烟花在地面以上爆炸
        Location adjustedLocation = location.clone().add(0, 1, 0);
        Bukkit.getRegionScheduler().run(plugin, adjustedLocation, task -> {
            World world = adjustedLocation.getWorld();
            if (world == null) return;

            // 生成烟花实体
            Firework firework = (Firework) world.spawnEntity(adjustedLocation, EntityType.FIREWORK_ROCKET);

            // 获取并配置烟花元数据
            FireworkMeta meta = firework.getFireworkMeta();

            // 添加随机效果
            FireworkEffect effect = createRandomFireworkEffect();
            meta.addEffect(effect);

            // 设置飞行时间为0，使其立即爆炸
            meta.setPower(0);

            // 应用配置
            firework.setFireworkMeta(meta);

            // 立即引爆
            firework.detonate();
        });
    }

    /**
     * 创建一个完全随机的烟花效果
     * @return 随机烟花效果
     */
    public FireworkEffect createRandomFireworkEffect() {
        // 1. 随机选择烟花类型
        FireworkEffect.Type type = getRandomFireworkType();

        // 2. 随机生成1-3种主颜色
        List<Color> primaryColors = getRandomColors(1, 3);

        // 3. 随机生成0-2种消退颜色（可能为空）
        List<Color> fadeColors = getRandomColors(0, 2);

        // 4. 随机决定是否有闪烁效果（50%概率）
        boolean hasFlicker = random.nextBoolean();

        // 5. 随机决定是否有轨迹
        boolean hasTrail = false;   //random.nextBoolean();

        // 构建烟花效果-只使用球形
        FireworkEffect.Builder builder = FireworkEffect.builder()
                .with(getRandomFireworkType())
                .withColor(primaryColors)
                .flicker(hasFlicker)
                .trail(hasTrail);

        // 如果有消退颜色则添加
        if (!fadeColors.isEmpty()) {
            builder.withFade(fadeColors);
        }

        return builder.build();
    }

    /**
     * 获取随机的烟花爆炸类型
     */
    private FireworkEffect.Type getRandomFireworkType() {
        return FIREWORK_TYPES[random.nextInt(FIREWORK_TYPES.length)];
    }

    /**
     * 从预定义颜色池中获取随机颜色
     * @param minCount 最少颜色数量（包含）
     * @param maxCount 最多颜色数量（包含）
     * @return 随机颜色列表
     */
    private List<Color> getRandomColors(int minCount, int maxCount) {
        // 确保参数有效
        int actualMin = Math.max(0, Math.min(minCount, PREDEFINED_COLORS.size()));
        int actualMax = Math.max(actualMin, Math.min(maxCount, PREDEFINED_COLORS.size()));

        // 随机决定颜色数量
        int colorCount = actualMin + (actualMax > actualMin ? random.nextInt(actualMax - actualMin + 1) : 0);

        // 如果不需要颜色，返回空列表
        if (colorCount <= 0) {
            return Collections.emptyList();
        }

        // 复制颜色池并打乱顺序
        List<Color> shuffledColors = new ArrayList<>(PREDEFINED_COLORS);
        Collections.shuffle(shuffledColors, random);

        // 返回前N个颜色
        return new ArrayList<>(shuffledColors.subList(0, Math.min(colorCount, shuffledColors.size())));
    }

    /**
     * 生成指定类型和颜色的固定效果烟花（非随机）
     */
    public void spawnCustomFirework(Location location,
                                    FireworkEffect.Type type,
                                    List<Color> primaryColors,
                                    List<Color> fadeColors,
                                    boolean hasFlicker,
                                    boolean hasTrail) {

        Location adjustedLocation = location.clone().add(0, 1, 0);

        Bukkit.getRegionScheduler().run(plugin, adjustedLocation, task -> {
            World world = adjustedLocation.getWorld();
            if (world == null) return;

            Firework firework = (Firework) world.spawnEntity(adjustedLocation, EntityType.FIREWORK_ROCKET);
            FireworkMeta meta = firework.getFireworkMeta();

            FireworkEffect.Builder builder = FireworkEffect.builder()
                    .with(type)
                    .withColor(primaryColors)
                    .flicker(hasFlicker)
                    .trail(hasTrail);

            if (fadeColors != null && !fadeColors.isEmpty()) {
                builder.withFade(fadeColors);
            }

            meta.addEffect(builder.build());
            meta.setPower(0);
            firework.setFireworkMeta(meta);
            firework.detonate();
        });
    }
}
