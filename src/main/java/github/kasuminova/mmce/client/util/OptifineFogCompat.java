package github.kasuminova.mmce.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;

import java.lang.reflect.Field;

public final class OptifineFogCompat {

    private static final Field FOG_TYPE;
    private static final Field FOG_STANDARD;

    static {
        Field fogType = null;
        Field fogStandard = null;
        try {
            fogType = GameSettings.class.getField("ofFogType");
            fogStandard = EntityRenderer.class.getField("fogStandard");
        } catch (NoSuchFieldException ignored) {
            // OptiFine 为可选依赖；缺少字段时不干预原有雾效。
        }
        FOG_TYPE = fogType;
        FOG_STANDARD = fogStandard;
    }

    private OptifineFogCompat() {
    }

    public static boolean shouldDisableFog(Minecraft minecraft) {
        if (FOG_TYPE == null || FOG_STANDARD == null) {
            return false;
        }
        try {
            // 与 OptiFine RenderGlobal 一致：仅关闭普通距离雾，保留水下、熔岩和失明雾效。
            return FOG_TYPE.getInt(minecraft.gameSettings) == 3
                && FOG_STANDARD.getBoolean(minecraft.entityRenderer);
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

}
