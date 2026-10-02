package org.ryzen.feature.impl.misc.autobuy;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutoBuyItemCategory {
   KRUSH("Crusher"),
   SPHERES("Spheres"),
   TALISMANS("Talismans"),
   POTIONS("Potions"),
   HOLYWORLD("HolyWorld"),
   MISC("Misc");

   private final String displayName;

   AutoBuyItemCategory(String displayName) {
      this.displayName = displayName;
   }

   public boolean isHolyWorld() {
      return this == HOLYWORLD;
   }

   public boolean matchesServer(String serverMode) {
      return serverMode != null && !serverMode.isBlank() ? this.isHolyWorld() == AutoBuyServer.isHolyFamily(serverMode) : !this.isHolyWorld();
   }

   @Generated
   public String getDisplayName() {
      return this.displayName;
   }
}
