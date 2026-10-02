package org.ryzen.event.events.game;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_746;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class PlayerJumpEvent extends Event {
   private class_746 player;
   private class_243 position;

   public PlayerJumpEvent set(class_746 player, class_243 position) {
      this.player = player;
      this.position = position;
      return this;
   }

   @Generated
   public class_746 getPlayer() {
      return this.player;
   }

   @Generated
   public class_243 getPosition() {
      return this.position;
   }
}
