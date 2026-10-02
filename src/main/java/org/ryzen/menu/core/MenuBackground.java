package org.ryzen.menu.core;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum MenuBackground {
   NONE("No background"),
   NEBULA("Nebula"),
   PLASMA("Plasma"),
   GYROID("Gyroid");

   private static final MenuBackground[] VALUES = values();
   private final String label;

   MenuBackground(String label) {
      this.label = label;
   }

   public String label() {
      return this.label;
   }

   public int shaderMode() {
      return this.ordinal();
   }

   public boolean isAnimated() {
      return this != NONE;
   }

   public static String[] labels() {
      String[] labels = new String[VALUES.length];

      for (int i = 0; i < VALUES.length; i++) {
         labels[i] = VALUES[i].label;
      }

      return labels;
   }

   public static MenuBackground byIndex(int index) {
      return index >= 0 && index < VALUES.length ? VALUES[index] : NONE;
   }

   public static MenuBackground byLabel(String label) {
      for (MenuBackground background : VALUES) {
         if (background.label.equalsIgnoreCase(label)) {
            return background;
         }
      }

      return NONE;
   }

   public String storageKey() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
