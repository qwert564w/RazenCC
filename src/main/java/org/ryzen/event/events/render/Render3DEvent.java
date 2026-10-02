package org.ryzen.event.events.render;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class Render3DEvent extends Event {
   private class_310 client;
   private class_757 gameRenderer;
   private class_9779 deltaTracker;

   public Render3DEvent set(class_310 client, class_757 gameRenderer, class_9779 deltaTracker) {
      this.client = client;
      this.gameRenderer = gameRenderer;
      this.deltaTracker = deltaTracker;
      return this;
   }

   @Generated
   public class_310 getClient() {
      return this.client;
   }

   @Generated
   public class_757 getGameRenderer() {
      return this.gameRenderer;
   }

   @Generated
   public class_9779 getDeltaTracker() {
      return this.deltaTracker;
   }
}
