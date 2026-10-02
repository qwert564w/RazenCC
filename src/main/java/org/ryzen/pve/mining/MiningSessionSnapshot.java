package org.ryzen.pve.mining;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class MiningSessionSnapshot {
   private UUID playerId;
   private int selectedSlot = -1;
   private float yaw;
   private float pitch;
   private boolean captured;

   public void capture(class_746 player) {
      if (player != null && !this.captured) {
         this.playerId = player.method_5667();
         this.selectedSlot = player.method_31548().method_67532();
         this.yaw = player.method_36454();
         this.pitch = player.method_36455();
         this.captured = true;
      }
   }

   public void restore(class_310 client) {
      if (this.captured) {
         class_746 player = client == null ? null : client.field_1724;
         if (player != null && player.method_5667().equals(this.playerId)) {
            if (this.selectedSlot >= 0 && this.selectedSlot < 9 && player.method_31548().method_67532() != this.selectedSlot) {
               player.method_31548().method_61496(this.selectedSlot);
               player.field_3944.method_52787(new class_2868(this.selectedSlot));
            }

            player.method_36456(this.yaw);
            player.method_36457(this.pitch);
         }

         this.playerId = null;
         this.selectedSlot = -1;
         this.captured = false;
      }
   }
}
