package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MenuAppearance {
   private static int blurMode = 1;
   private static MenuBackground background = MenuBackground.NONE;
   private static float backgroundDim = 0.35F;

   private MenuAppearance() {
   }

   public static MenuBackground background() {
      return background;
   }

   public static void setBackground(MenuBackground next) {
      background = next == null ? MenuBackground.NONE : next;
   }

   public static float backgroundDim() {
      return backgroundDim;
   }

   public static void setBackgroundDim(float dim) {
      backgroundDim = Math.max(0.0F, Math.min(1.0F, dim));
   }

   public static void setBlurMode(int mode) {
      blurMode = Math.max(0, Math.min(2, mode));
   }

   public static float glassBlurScale() {
      return switch (blurMode) {
         case 0 -> 0.0F;
         case 1 -> 0.5F;
         default -> 1.0F;
      };
   }
}
