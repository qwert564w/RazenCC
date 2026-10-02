package org.ryzen.utils.render.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
final class UiVertexPacking {
   static final float SIZE_SCALE = 8.0F;
   static final float RADIUS_SCALE = 16.0F;
   static final float MAX_DUAL_RADIUS = 255.9375F;
   private static final int MODE_FLAG = 4096;

   private UiVertexPacking() {
   }

   static int packSize(float px) {
      return MathUtil.clamp(Math.round(px * 8.0F), 0, 32767);
   }

   static int packSignedSize(float px) {
      return MathUtil.clamp(Math.round(px * 8.0F), -32768, 32767);
   }

   static int packRadius(float px) {
      return MathUtil.clamp(Math.round(px * 16.0F), 0, 32767);
   }

   static int packU8Pair(int lo, int hi) {
      return MathUtil.clampByte(lo) | MathUtil.clampByte(hi) << 8;
   }

   static float packZ(float valuePx, int alpha8, boolean mode) {
      int units = MathUtil.clamp(Math.round(valuePx * 16.0F), 0, 4095);
      if (mode) {
         units |= 4096;
      }

      return units * 256.0F + MathUtil.clampByte(alpha8);
   }

   static float packDual12(float hiPx, float loPx) {
      int hi = MathUtil.clamp(Math.round(hiPx * 16.0F), 0, 4095);
      int lo = MathUtil.clamp(Math.round(loPx * 16.0F), 0, 4095);
      return hi * 4096.0F + lo;
   }

   static float packDual12Raw(int hi, int lo) {
      return MathUtil.clamp(hi, 0, 4095) * 4096.0F + MathUtil.clamp(lo, 0, 4095);
   }

   static float snormChannel(int channel) {
      return channel / 127.5F - 1.0F;
   }
}
