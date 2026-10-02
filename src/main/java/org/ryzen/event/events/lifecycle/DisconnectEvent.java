package org.ryzen.event.events.lifecycle;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class DisconnectEvent extends Event {
   private class_310 client;
   private class_437 screen;
   private boolean transferring;
   private boolean resetting;

   public DisconnectEvent set(class_310 client, class_437 screen, boolean transferring, boolean resetting) {
      this.client = client;
      this.screen = screen;
      this.transferring = transferring;
      this.resetting = resetting;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }

   @Generated
   public class_437 getScreen() {
      return this.screen;
   }

   @Generated
   public boolean isTransferring() {
      return this.transferring;
   }

   @Generated
   public boolean isResetting() {
      return this.resetting;
   }
}
