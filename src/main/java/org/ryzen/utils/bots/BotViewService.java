package org.ryzen.utils.bots;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class BotViewService {
   public static final BotViewService INSTANCE = new BotViewService();
   private volatile boolean active;

   private BotViewService() {
   }

   public boolean isActive() {
      return this.active;
   }

   public void setActive(boolean active) {
      this.active = active;
   }

   public boolean activate(String botName) {
      if (!BotService.INSTANCE.isConnected(botName)) {
         return false;
      }

      this.active = true;
      return true;
   }

   public void deactivate() {
      this.active = false;
   }
}
