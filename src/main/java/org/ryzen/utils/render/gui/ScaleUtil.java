package org.ryzen.utils.render.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ScaleUtil {
   private ScaleUtil() {
   }

   public static int toGuiPixels(int pixels, double guiScale) {
      validateGuiScale(guiScale);
      if (pixels == 0) {
         return 0;
      }

      int scaled = (int)Math.round(pixels / guiScale);
      return pixels > 0 ? Math.max(1, scaled) : Math.min(-1, scaled);
   }

   public static float toGuiPixels(float pixels, double guiScale) {
      validateGuiScale(guiScale);
      return (float)(pixels / guiScale);
   }

   private static void validateGuiScale(double guiScale) {
      if (!Double.isFinite(guiScale) || guiScale <= 0.0) {
         throw new IllegalArgumentException("GUI scale must be finite and positive");
      }
   }
}
