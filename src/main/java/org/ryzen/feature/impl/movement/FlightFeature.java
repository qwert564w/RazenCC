package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.TimerUtil;

@Environment(EnvType.CLIENT)
public final class FlightFeature extends Feature implements PlayerContext {
   private static final String MODE_GLIDE = "Glide";
   private static final String MODE_NORMAL = "Normal";
   private static final String MODE_MOTION = "Motion";
   private static final String MODE_JUMP = "Jump";
   private static final String MODE_DRAGON = "Dragon";
   private static final String MODE_ELYTRA_Y = "Elytra Y";
   private static final double GLIDE_SINK = -0.005;
   private static final double DRAGON_SPEED = 0.74;
   private static final float DRAGON_MOVING_TIMER = 0.7F;
   private static final float DRAGON_IDLE_TIMER = 1.05F;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Normal", "Glide", "Normal", "Motion", "Jump", "Dragon", "Elytra Y"));
   public final NumberSetting timer = this.register(new NumberSetting("Timer", 1.5, 0.1, 10.0, 0.1, "x").visibleWhen(this::usesTimer));
   public final NumberSetting verticalSpeed = this.register(new NumberSetting("Vertical Speed", 1.5, 0.1, 10.0, 0.1, "").visibleWhen(this::usesVerticalSpeed));
   public final NumberSetting elytraSpeed = this.register(
      new NumberSetting("Elytra Y Speed", 0.1, 0.05, 0.5, 0.05, "").visibleWhen(() -> this.mode.is("Elytra Y"))
   );
   private boolean timerActive;

   public FlightFeature() {
      super("Flight", "Free vertical movement", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onDisable() {
      this.releaseTimer();
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPre()) {
         class_310 client = class_310.method_1551();
         class_746 player = event.getPlayer();
         if (player != null && player.field_3913 != null) {
            boolean down = client.field_1690.field_1832.method_1434();
            boolean up = client.field_1690.field_1903.method_1434();
            double speed = this.verticalSpeed.getValue();
            boolean hasHorizontalInput = player.field_3913.method_3128().method_35587() > 0.0F;
            switch ((String)this.mode.getValue()) {
               case "Glide":
                  this.applyVertical(player, verticalOr(down, up, speed, -0.005), hasHorizontalInput, true);
                  break;
               case "Normal":
                  this.applyVertical(player, verticalOr(down, up, speed, 0.0), hasHorizontalInput, true);
                  break;
               case "Motion":
                  this.applyVertical(player, verticalOr(down, up, speed, player.method_18798().field_1351), hasHorizontalInput, false);
                  break;
               case "Jump":
                  this.releaseTimer();
                  if (up && player.method_24828()) {
                     player.method_6043();
                  }
                  break;
               case "Dragon":
                  this.applyDragon(player, down, up);
                  break;
               case "Elytra Y":
                  this.applyElytraLift(client, player);
                  break;
               default:
                  this.releaseTimer();
            }
         } else {
            this.releaseTimer();
         }
      }
   }

   private void applyVertical(class_746 player, double vertical, boolean hasHorizontalInput, boolean jumpOffGround) {
      if (jumpOffGround && player.method_24828()) {
         player.method_6043();
      }

      this.setTimer(this.timer.getValue().floatValue());
      class_243 movement = player.method_18798();
      player.method_18800(hasHorizontalInput ? movement.field_1352 : 0.0, vertical, hasHorizontalInput ? movement.field_1350 : 0.0);
   }

   private void applyDragon(class_746 player, boolean down, boolean up) {
      if (!player.method_31549().field_7479) {
         this.releaseTimer();
      } else {
         double vertical = down ? -0.74 : (up ? 0.74 : 0.0);
         this.setTimer(!down && !up ? 1.05F : 0.7F);
         class_243 movement = player.method_18798();
         player.method_18800(movement.field_1352, vertical, movement.field_1350);
      }
   }

   private void applyElytraLift(class_310 client, class_746 player) {
      this.releaseTimer();
      if (player.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833) && player.method_6128()) {
         player.method_18800(0.0, player.method_18798().field_1351 + this.elytraSpeed.getValue(), 0.0);
         client.field_1690.field_1903.method_23481(false);
      }
   }

   private static double verticalOr(boolean down, boolean up, double speed, double fallback) {
      if (down) {
         return -speed;
      } else {
         return up ? speed : fallback;
      }
   }

   private void setTimer(float multiplier) {
      TimerUtil.setTimer(multiplier);
      this.timerActive = true;
   }

   private void releaseTimer() {
      if (this.timerActive) {
         TimerUtil.resetTimer();
         this.timerActive = false;
      }
   }

   private boolean usesTimer() {
      return this.mode.is("Glide") || this.mode.is("Normal") || this.mode.is("Motion");
   }

   private boolean usesVerticalSpeed() {
      return this.usesTimer();
   }
}
