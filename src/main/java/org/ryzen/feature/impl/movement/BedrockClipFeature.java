package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10255;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2831;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public final class BedrockClipFeature extends Feature implements PlayerContext {
   private static final double SEARCH_RANGE_SQR = 6.25;
   private static final double MOUNT_RANGE = 3.0;
   private static final double CLIP_DROP = 135.0;
   private static final long MOUNT_COOLDOWN_MS = 300L;
   private static final long NO_PHYSICS_MS = 1000L;
   private static final float AIM_TOLERANCE = 15.0F;
   public final BooleanSetting autoMount = this.register(new BooleanSetting("Auto Mount", true));
   private boolean clipping;
   private long lastMountAttemptAt;
   private long clipStartedAt;

   public BedrockClipFeature() {
      super("BedrockClip", "Rides a boat and drops through the bedrock floor", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onDisable() {
      this.stopClipping();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.stopClipping();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player != null && level != null && client.field_1761 != null) {
         if (this.clipping) {
            if (System.currentTimeMillis() - this.clipStartedAt >= 1000L) {
               this.stopClipping();
            }
         } else {
            class_10255 boat = nearestBoat(player, level);
            if (boat != null) {
               if (player.method_5765() && player.method_5854() == boat) {
                  player.method_5814(player.method_23317(), player.method_23318() - 135.0, player.method_23321());
                  player.method_5848();
                  player.field_5960 = true;
                  player.field_6017 = 0.0;
                  this.clipping = true;
                  this.clipStartedAt = System.currentTimeMillis();
               } else {
                  if (this.autoMount.getValue() && player.method_5739(boat) < 3.0) {
                     this.tryMount(client, player, boat);
                  }
               }
            }
         }
      }
   }

   private void tryMount(class_310 client, class_746 player, class_10255 boat) {
      class_243 aim = boat.method_73189().method_1031(0.0, boat.method_17682() / 2.0, 0.0).method_1020(player.method_33571());
      float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(aim.field_1350, aim.field_1352)) - 90.0));
      float pitch = (float)(-Math.toDegrees(Math.atan2(aim.field_1351, Math.hypot(aim.field_1352, aim.field_1350))));
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2831(yaw, pitch, player.method_24828(), player.field_5976));
      }

      if (!(Math.abs(class_3532.method_15393(yaw - player.method_36454())) >= 15.0F) && !(Math.abs(pitch - player.method_36455()) >= 15.0F)) {
         long now = System.currentTimeMillis();
         if (now - this.lastMountAttemptAt >= 300L) {
            this.lastMountAttemptAt = now;
            client.field_1761.method_2905(player, boat, class_1268.field_5808);
         }
      }
   }

   private static class_10255 nearestBoat(class_746 player, class_638 level) {
      class_10255 nearest = null;
      double nearestDistance = Double.MAX_VALUE;

      for (class_1297 entity : level.method_18112()) {
         if (entity instanceof class_10255 boat) {
            double distance = player.method_5858(boat);
            if (distance < 6.25 && distance < nearestDistance) {
               nearestDistance = distance;
               nearest = boat;
            }
         }
      }

      return nearest;
   }

   private void stopClipping() {
      class_746 player = this.localPlayer();
      if (player != null) {
         player.field_5960 = false;
      }

      this.clipping = false;
      this.clipStartedAt = 0L;
      this.lastMountAttemptAt = 0L;
   }
}
