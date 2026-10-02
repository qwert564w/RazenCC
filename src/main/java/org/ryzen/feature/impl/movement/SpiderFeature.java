package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public final class SpiderFeature extends Feature implements PlayerContext {
   private static final String MODE_WATER = "Water";
   private static final String MODE_HEAD = "Head";
   private static final long HEAD_PLACE_COOLDOWN_MS = 150L;
   private static final double HEAD_REACH = 4.0;
   private static final double WATER_BOOST = 0.36;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Water", "Water", "Head"));
   private long lastHeadPlaceAt;
   private int originalSlot = -1;
   private int bucketSlot = -1;

   public SpiderFeature() {
      super("Spider", "Climbs walls with a water bucket or a head", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onDisable() {
      class_746 player = this.localPlayer();
      if (player != null && this.originalSlot != -1 && (this.bucketSlot == -1 || player.method_31548().method_67532() == this.bucketSlot)) {
         select(player, this.originalSlot);
      }

      this.originalSlot = -1;
      this.bucketSlot = -1;
      this.lastHeadPlaceAt = 0L;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         if (this.mode.is("Water")) {
            this.climbWithWater(client, player);
         } else {
            this.climbWithHead(client, player);
         }
      }
   }

   private void climbWithWater(class_310 client, class_746 player) {
      if (player.field_5976) {
         int bucket = findHotbar(player, class_1802.field_8705);
         if (bucket != -1) {
            if (!player.method_6047().method_31574(class_1802.field_8705)) {
               if (this.originalSlot == -1) {
                  this.originalSlot = player.method_31548().method_67532();
               }

               this.bucketSlot = bucket;
               select(player, bucket);
            }

            client.field_1761.method_2919(player, class_1268.field_5808);
            class_243 movement = player.method_18798();
            player.method_18800(movement.field_1352, 0.36, movement.field_1350);
         }
      }
   }

   private void climbWithHead(class_310 client, class_746 player) {
      if (player.field_5976) {
         if (player.method_24828()) {
            player.method_6043();
         }

         long now = System.currentTimeMillis();
         if (now - this.lastHeadPlaceAt >= 150L) {
            class_1799 offhand = player.method_6079();
            if (offhand.method_31574(class_1802.field_8575)) {
               class_243 eye = player.method_33571();
               class_243 end = eye.method_1019(player.method_5828(1.0F).method_1021(4.0));
               class_3965 hit = client.field_1687.method_17742(new class_3959(eye, end, class_3960.field_17559, class_242.field_1348, player));
               if (hit.method_17783() == class_240.field_1332) {
                  class_2338 pos = hit.method_17777();
                  if (!client.field_1687.method_8320(pos).method_26215() && !client.field_1687.method_8320(pos).method_45474()) {
                     if (player.field_3944 != null) {
                        player.field_3944.method_52787(new class_2831(player.method_36454(), player.method_36455(), player.method_24828(), player.field_5976));
                     }

                     player.method_6104(class_1268.field_5810);
                     client.field_1761.method_2896(player, class_1268.field_5810, hit);
                     player.field_6017 = 0.0;
                     this.lastHeadPlaceAt = now;
                  }
               }
            }
         }
      }
   }

   private static int findHotbar(class_746 player, class_1792 item) {
      for (int slot = 0; slot < 9; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(item)) {
            return slot;
         }
      }

      return -1;
   }

   private static void select(class_746 player, int slot) {
      player.method_31548().method_61496(slot);
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2868(slot));
      }
   }
}
