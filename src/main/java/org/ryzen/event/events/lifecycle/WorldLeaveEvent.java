package org.ryzen.event.events.lifecycle;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_638;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class WorldLeaveEvent extends Event {
   private class_310 client;
   private class_638 level;

   public WorldLeaveEvent set(class_310 client, class_638 level) {
      this.client = client;
      this.level = level;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }

   @Generated
   public class_638 getLevel() {
      return this.level;
   }
}
