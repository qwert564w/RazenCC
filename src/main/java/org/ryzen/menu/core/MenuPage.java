package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum MenuPage {
   NONE,
   SEARCH,
   SETTINGS,
   FRIENDS,
   ACCOUNT_SWITCHER,
   CONFIGURATIONS,
   BINDS,
   COSMETICS,
   AUTOBUY,
   HUD;

   private static final MenuPage[] ACTION_PAGES = new MenuPage[]{CONFIGURATIONS, SETTINGS, BINDS};

   public static MenuPage[] actionPages() {
      return (MenuPage[])ACTION_PAGES.clone();
   }

   public static int actionCount() {
      return ACTION_PAGES.length;
   }

   public static MenuPage actionAt(int index) {
      return ACTION_PAGES[Math.floorMod(index, ACTION_PAGES.length)];
   }

   public int actionIndex() {
      for (int index = 0; index < ACTION_PAGES.length; index++) {
         if (ACTION_PAGES[index] == this) {
            return index;
         }
      }

      return -1;
   }
}
