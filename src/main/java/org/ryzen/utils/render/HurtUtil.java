package org.ryzen.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_3532;
import org.ryzen.utils.ColorUtil;

@Environment(EnvType.CLIENT)
public final class HurtUtil {
   private static final int HURT_COLOR = -44459;
   private static final float HURT_TICKS = 10.0F;

   private HurtUtil() {
   }

   public static float factor(class_1297 entity) {
      return entity instanceof class_1309 living && living.field_6235 > 0 ? class_3532.method_15363(living.field_6235 / 10.0F, 0.0F, 1.0F) : 0.0F;
   }

   public static float easedFactor(class_1297 entity) {
      float factor = factor(entity);
      return 1.0F - (1.0F - factor) * (1.0F - factor);
   }

   public static int blend(int baseColor, class_1297 entity, float alpha) {
      return blend(baseColor, easedFactor(entity), alpha);
   }

   public static int blend(int baseColor, float factor, float alpha) {
      factor = class_3532.method_15363(factor, 0.0F, 1.0F);
      int normal = ColorUtil.multiplyAlpha(baseColor, alpha);
      if (factor <= 0.0F) {
         return normal;
      }

      int hurt = ColorUtil.multiplyAlpha(-44459, alpha);
      return ColorUtil.lerp(normal, hurt, factor);
   }

   public static float scale(class_1297 entity, float intensity) {
      return 1.0F + easedFactor(entity) * Math.max(0.0F, intensity);
   }
}
