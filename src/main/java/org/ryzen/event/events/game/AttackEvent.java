package org.ryzen.event.events.game;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class AttackEvent extends CancellableEvent {
   private class_310 client;

   public AttackEvent set(class_310 client) {
      this.client = client;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }
}
