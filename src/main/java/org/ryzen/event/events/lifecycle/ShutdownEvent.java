package org.ryzen.event.events.lifecycle;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class ShutdownEvent extends Event {
   private class_310 client;

   public ShutdownEvent set(class_310 client) {
      this.client = client;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }
}
