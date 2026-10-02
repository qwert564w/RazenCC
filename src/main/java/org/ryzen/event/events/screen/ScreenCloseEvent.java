package org.ryzen.event.events.screen;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_437;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class ScreenCloseEvent extends CancellableEvent {
   private class_310 client;
   private class_437 screen;

   public ScreenCloseEvent set(class_310 client, class_437 screen) {
      this.client = client;
      this.screen = screen;
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
}
