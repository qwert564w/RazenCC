package org.ryzen.utils.render.target;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3532;

@Environment(EnvType.CLIENT)
final class TargetEffectMotion {
   private TargetEffectMotion() {
   }

   static float assembly(float globalProgress, int index, int count) {
      float position = count <= 1 ? 0.0F : (float)index / (count - 1);
      float delay = position * 0.22F;
      float progress = class_3532.method_15363((globalProgress - delay) / (1.0F - delay), 0.0F, 1.0F);
      return progress * progress * (3.0F - 2.0F * progress);
   }

   static double scatter(int seed, int axis, double distance, float assembly) {
      int hash = seed * -1640531527 + axis * 2135587861;
      hash ^= hash >>> 16;
      hash *= -2048144789;
      hash ^= hash >>> 13;
      float signed = (hash >>> 8 & 65535) / 32767.5F - 1.0F;
      return signed * distance * (1.0F - assembly);
   }
}
