package org.ryzen.event.events.game;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class GameTickEvent extends Event {
   private class_310 client;
   private TickContext context;

   public GameTickEvent set(class_310 client, TickContext context) {
      this.client = client;
      this.context = context;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }

   @Generated
   public TickContext getContext() {
      return this.context;
   }
}
