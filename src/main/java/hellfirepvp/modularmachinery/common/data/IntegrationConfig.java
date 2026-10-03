package hellfirepvp.modularmachinery.common.data;

import net.minecraftforge.common.config.Configuration;

/** 联动仓口与选区设置，使用本体配置文件。 */
public final class IntegrationConfig {
    public static int minIntervalMs = 500;
    public static int maxIntervalMs = 30_000;
    public static int potentialEnergyHatchesCapacity = 4096;
    public static double laserHatchesCapacity = Integer.MAX_VALUE;
    public static double heatHatchesCapacity = 65536;
    public static int dragonBreathChargesCapacity = 8;
    public static boolean constructNBT = false;

    private IntegrationConfig() {}

    public static void loadFromConfig(Configuration cfg) {
        String category = "integration";
        minIntervalMs = cfg.getInt("snapshot-min-interval-ms", category, 500, 1, Integer.MAX_VALUE,
            "Minimum interval between resource snapshots, in milliseconds.");
        maxIntervalMs = cfg.getInt("snapshot-max-interval-ms", category, 30000, minIntervalMs, Integer.MAX_VALUE,
            "Maximum interval between resource snapshots, in milliseconds.");
        potentialEnergyHatchesCapacity = cfg.getInt("potential-energy-capacity", category, 4096, 1, Integer.MAX_VALUE,
            "Potential energy stored in each hatch.");
        laserHatchesCapacity = cfg.get(category, "laser-capacity", (double) Integer.MAX_VALUE).getDouble();
        heatHatchesCapacity = cfg.get(category, "heat-capacity", 65536D).getDouble();
        dragonBreathChargesCapacity = cfg.getInt("dragon-breath-capacity", category, 8, 1, Integer.MAX_VALUE,
            "Dragon breath charges stored in each hatch.");
        constructNBT = cfg.getBoolean("include-nbt", "selection", false,
            "Include tile entity NBT in exported machine structures.");
    }
}
