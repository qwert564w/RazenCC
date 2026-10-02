package org.ryzen.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.injection.TimerAccess;

@Environment(EnvType.CLIENT)
public final class TimerUtil {
   private TimerUtil() {
   }

   public static void setTimer(float multiplier) {
      if (class_310.method_1551().method_61966() instanceof TimerAccess timer) {
         timer.ryzen$setSpeedMultiplier(multiplier);
      }
   }

   public static void resetTimer() {
      setTimer(1.0F);
   }

   public static float getTimer() {
      return class_310.method_1551().method_61966() instanceof TimerAccess timer ? timer.ryzen$getSpeedMultiplier() : 1.0F;
   }
}
