package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1671;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ElytraTargetFeature extends Feature implements MinecraftContext {
   private static final double OVERTAKE_DOT_MIN = -0.4;
   private static final double OVERTAKE_SPEED_SCALE = 1.25;
   private static final double MAX_AIM_DISTANCE = 2.7;
   private static final double GLIDE_DRAG = 2.7;
   private static final double AIM_HEIGHT_FRAC = 0.4;
   private static final double PITCH_HORIZONTAL = 0.35;
   private static final double TICK_VERTICAL = 0.65;
   private static final double VERTICAL_CLAMP = 0.6;
   private static final float INSTANT_ROTATION_STEP = 38.0F;
   private static final double FIREWORK_SCAN = 256.0;
   public final NumberSetting aimDistance = this.register(new NumberSetting("Aim Distance", 30.0, 5.0, 100.0, 5.0, " b"));
   public final BooleanSetting overtake = this.register(new BooleanSetting("Overtake", true));
   public final NumberSetting predictBlocks = this.register(new NumberSetting("Predict Blocks", 3.0, 1.0, 6.0, 0.1, " b").visibleWhen(this.overtake::getValue));
   public final BooleanSetting instantResponse = this.register(new BooleanSetting("Instant Response", false).visibleWhen(this.overtake::getValue));

   public ElytraTargetFeature() {
      super("ElytraTarget", "Chases a gliding player while flying an elytra", FeatureCategory.COMBAT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null) {
         class_1309 target = this.findTarget(player);
         if (target != null) {
            this.aimAt(player, target);
            if (this.overtake.getValue()) {
               this.overtake(player, target);
            }
         }
      }
   }

   private void aimAt(class_746 player, class_1309 target) {
      class_243 aim = this.aimPoint(player, target);
      class_243 eyes = player.method_33571();
      if (!this.instantResponse.getValue() || !(eyes.method_1025(aim) > 7.290000000000001)) {
         class_243 delta = aim.method_1020(eyes);
         double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         float yaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
         if (this.instantResponse.getValue()) {
            yaw = player.method_36454() + class_3532.method_15363(class_3532.method_15393(yaw - player.method_36454()), -38.0F, 38.0F);
            pitch = player.method_36455() + class_3532.method_15363(pitch - player.method_36455(), -38.0F, 38.0F);
         }

         RotationContext.setRotation(yaw, pitch);
      }
   }

   private void overtake(class_746 player, class_1309 target) {
      class_243 own = player.method_18798();
      class_243 theirs = target.method_18798();
      double wanted = Math.sqrt(theirs.field_1352 * theirs.field_1352 + theirs.field_1350 * theirs.field_1350);
      double current = Math.sqrt(own.field_1352 * own.field_1352 + own.field_1350 * own.field_1350);
      if (!(current <= 0.0) && !(current >= wanted)) {
         double scale = wanted / current * 1.25;
         player.method_18800(own.field_1352 * scale, own.field_1351, own.field_1350 * scale);
      }
   }

   private class_243 aimPoint(class_746 player, class_1309 target) {
      return this.overtake.getValue() && this.isTarget(player, target) && this.passesOvertakeAlignment(player, target)
         ? this.baseOffset(target)
         : this.baseBodyPoint(target);
   }

   private class_243 baseOffset(class_1309 target) {
      class_243 base = this.baseBodyPoint(target);
      double blocks = this.predictBlocks.getValue();
      if (blocks <= 0.0) {
         return base;
      }

      class_243 center = target.method_5829().method_1005();
      double centerYOffset = center.field_1351 - target.method_23318();
      double tickVertical = target.method_23318() - target.field_5971;
      class_243 velocity = target.method_18798();
      double horizontal = Math.hypot(velocity.field_1352, velocity.field_1350);
      if (horizontal < 1.0E-4) {
         horizontal = Math.hypot(target.method_23317() - target.field_6038, target.method_23321() - target.field_5989);
      }

      if (horizontal < 1.0E-4) {
         return base;
      }

      double ticks = blocks / Math.max(horizontal, 0.05);
      double drag = Math.pow(0.37037037037037035, ticks);
      class_243 extrapolated = velocity.method_1021(ticks * drag * 1.25);
      double pitchRad = Math.toRadians(target.method_36455());
      double pitchLead = -Math.sin(pitchRad) * horizontal * 0.35;
      double verticalStep = class_3532.method_15350((tickVertical * 0.65 + pitchLead) * blocks, -0.6, 0.6);
      return base.method_1031(extrapolated.field_1352, extrapolated.field_1351 + centerYOffset + verticalStep, extrapolated.field_1350);
   }

   private class_243 baseBodyPoint(class_1309 target) {
      return target.method_73189().method_1031(0.0, target.method_17682() * 0.4, 0.0);
   }

   private boolean passesOvertakeAlignment(class_746 player, class_1309 target) {
      class_243 toTarget = target.method_73189().method_1020(player.method_73189());
      class_243 flatToTarget = new class_243(toTarget.field_1352, 0.0, toTarget.field_1350);
      if (flatToTarget.method_1027() < 1.0E-6) {
         return true;
      }

      class_243 own = player.method_18798();
      class_243 flatOwn = new class_243(own.field_1352, 0.0, own.field_1350).method_1029();
      class_243 flatTarget = new class_243(target.method_18798().field_1352, 0.0, target.method_18798().field_1350).method_1029();
      double closing = flatOwn.method_1026(flatTarget);
      double approach = flatOwn.method_1026(flatToTarget.method_1029());
      return closing >= -0.4 && approach >= -0.4;
   }

   private class_1309 findTarget(class_746 player) {
      if (mc.field_1687 != null && player.method_6128()) {
         class_1309 best = null;
         double bestDistance = this.aimDistance.getValue() * this.aimDistance.getValue();

         for (class_1657 other : mc.field_1687.method_18456()) {
            if (other != player && this.isTarget(player, other)) {
               double distance = other.method_5858(player);
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = other;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   private boolean isTarget(class_746 player, class_1309 entity) {
      return this.overtake.getValue() && player.method_6128() && entity != null && entity.method_6128() && this.hasOwnFirework(player);
   }

   public boolean isChasing(class_1309 entity) {
      return this.isEnabled() && mc.field_1724 != null && this.isTarget(mc.field_1724, entity);
   }

   private boolean hasOwnFirework(class_746 player) {
      if (mc.field_1687 == null) {
         return false;
      }

      for (class_1671 rocket : mc.field_1687.method_18467(class_1671.class, player.method_5829().method_1014(256.0))) {
         if (rocket.method_24921() == player) {
            return true;
         }
      }

      return false;
   }
}
