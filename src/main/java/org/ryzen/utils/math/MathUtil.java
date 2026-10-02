package org.ryzen.utils.math;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MathUtil {
   public static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
   }

   public static long clamp(long value, long min, long max) {
      return Math.max(min, Math.min(max, value));
   }

   public static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   public static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   public static float clamp01(float value) {
      return clamp(value, 0.0F, 1.0F);
   }

   public static double clamp01(double value) {
      return clamp(value, 0.0, 1.0);
   }

   public static int clampByte(int value) {
      return clamp(value, 0, 255);
   }

   public static float lerp(float from, float to, float delta) {
      return from + (to - from) * delta;
   }

   public static double lerp(double from, double to, double delta) {
      return from + (to - from) * delta;
   }

   public static float easeOutCubic(float value) {
      float clamped = clamp01(value);
      return 1.0F - (float)Math.pow(1.0F - clamped, 3.0);
   }

   public static double easeOutCubic(double value) {
      double clamped = clamp01(value);
      return 1.0 - Math.pow(1.0 - clamped, 3.0);
   }

   @Generated
   private MathUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
