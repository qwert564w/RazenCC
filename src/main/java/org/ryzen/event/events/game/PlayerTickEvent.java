package org.ryzen.event.events.game;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;
import org.ryzen.event.Event;

@Environment(EnvType.CLIENT)
public final class PlayerTickEvent extends Event {
   private class_746 player;
   private PlayerTickEvent.Phase phase;

   public PlayerTickEvent set(class_746 player, PlayerTickEvent.Phase phase) {
      this.player = player;
      this.phase = phase;
      return this;
   }

   public boolean isPre() {
      return this.phase == PlayerTickEvent.Phase.PRE;
   }

   public boolean isPost() {
      return this.phase == PlayerTickEvent.Phase.POST;
   }

   @Generated
   public class_746 getPlayer() {
      return this.player;
   }

   @Generated
   public PlayerTickEvent.Phase getPhase() {
      return this.phase;
   }

   @Environment(EnvType.CLIENT)
   public enum Phase {
      PRE,
      POST;
   }
}
