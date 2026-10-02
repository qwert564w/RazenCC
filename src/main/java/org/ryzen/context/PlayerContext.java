package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1661;
import net.minecraft.class_243;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public interface PlayerContext extends WorldContext {
   double MOVEMENT_EPSILON = 1.0E-4;

   default class_746 localPlayer() {
      return this.player();
   }

   default class_1661 inventory() {
      class_746 player = this.localPlayer();
      return player != null ? player.method_31548() : null;
   }

   default boolean hasPlayer() {
      return this.localPlayer() != null;
   }

   default boolean isMoving() {
      class_746 player = this.localPlayer();
      if (player == null) {
         return false;
      }

      class_243 movement = player.method_18798();
      return movement.method_37268() > 1.0E-4;
   }

   default boolean hasMovementInput() {
      class_746 player = this.localPlayer();
      return player != null
         && (
            player.field_3913.field_54155.comp_3159()
               || player.field_3913.field_54155.comp_3160()
               || player.field_3913.field_54155.comp_3161()
               || player.field_3913.field_54155.comp_3162()
         );
   }
}
