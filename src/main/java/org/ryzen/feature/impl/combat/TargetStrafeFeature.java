package org.ryzen.feature.impl.combat;

import java.util.Map;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class TargetStrafeFeature extends Feature implements PlayerContext {
   private static final String MODE_COLLISION = "Collision";
   private static final String MODE_STEER = "Steer";
   private static final float AIR_FRICTION = 0.91F;
   private static final float AIR_DRAG = 0.99F;
   private static final double LEAD_MIN_SPEED = 4.0;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Collision", "Collision", "Steer"));
   public final NumberSetting radius = this.register(new NumberSetting("Radius", 1.5, 0.5, 5.0, 0.1, " blocks").visibleWhen(() -> this.mode.is("Collision")));
   public final NumberSetting strength = this.register(new NumberSetting("Strength", 0.05, 0.01, 0.1, 0.01, "").visibleWhen(() -> this.mode.is("Collision")));
   public final BooleanSetting lead = this.register(new BooleanSetting("Lead Target", true).visibleWhen(() -> this.mode.is("Steer")));
   private final Map<class_1309, class_243> lastPositions = new WeakHashMap<>();

   public TargetStrafeFeature() {
      super("TargetStrafe", "Circles and sticks to the aura's target", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.lastPositions.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.lastPositions.clear();
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPre() && this.mode.is("Collision")) {
         class_746 player = event.getPlayer();
         class_1309 target = this.currentTarget();
         if (player != null && target != null && !(player.method_5739(target) > this.radius.getValue())) {
            class_243 toTarget = target.method_73189().method_1020(player.method_73189());
            if (!(toTarget.method_37268() < 1.0E-6)) {
               class_243 direction = new class_243(toTarget.field_1352, 0.0, toTarget.field_1350).method_1029();
               double push = this.strength.getValue() * closeInFalloff(player.method_5739(target));
               if (!player.method_24828()) {
                  push *= 1.0879121F;
               }

               class_243 movement = player.method_18798();
               player.method_18800(movement.field_1352 + direction.field_1352 * push, movement.field_1351, movement.field_1350 + direction.field_1350 * push);
            }
         }
      }
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      if (this.mode.is("Steer")) {
         class_746 player = this.localPlayer();
         class_1309 target = this.currentTarget();
         if (player != null && target != null) {
            class_243 aim = this.lead.getValue() ? this.leadPosition(target) : target.method_73189();
            double desiredYaw = class_3532.method_15338(
               Math.toDegrees(Math.atan2(aim.field_1350 - player.method_23321(), aim.field_1352 - player.method_23317())) - 90.0
            );
            float yaw = player.method_36454();
            float bestForward = 0.0F;
            float bestStrafe = 0.0F;
            double bestError = Double.MAX_VALUE;

            for (float forward = -1.0F; forward <= 1.0F; forward++) {
               for (float strafe = -1.0F; strafe <= 1.0F; strafe++) {
                  if (forward != 0.0F || strafe != 0.0F) {
                     double error = Math.abs(class_3532.method_15338(desiredYaw - movementYaw(yaw, forward, strafe)));
                     if (error < bestError) {
                        bestError = error;
                        bestForward = forward;
                        bestStrafe = strafe;
                     }
                  }
               }
            }

            event.setMoveVector(new class_241(bestStrafe, bestForward).method_35581());
            event.setDirections(bestForward > 0.0F, bestForward < 0.0F, bestStrafe > 0.0F, bestStrafe < 0.0F);
            event.setSprint(event.getKeyPresses().comp_3165() && bestForward > 0.0F);
         }
      }
   }

   private class_243 leadPosition(class_1309 target) {
      class_243 position = target.method_73189();
      class_243 previous = this.lastPositions.getOrDefault(target, position);
      this.lastPositions.put(target, position);
      double deltaX = position.field_1352 - previous.field_1352;
      double deltaZ = position.field_1350 - previous.field_1350;
      double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20.0;
      double step = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
      if (!(speed < 4.0) && !(step <= 0.01)) {
         double offset = speed >= 7.0 ? 1.25 : (speed >= 6.0 ? 1.0 : (speed >= 5.0 ? 0.75 : 0.5));
         return position.method_1031(deltaX / step * offset, 0.0, deltaZ / step * offset);
      } else {
         return position;
      }
   }

   private static double closeInFalloff(double distance) {
      if (distance < 0.8) {
         return 0.3;
      } else {
         return distance < 1.0 ? 0.65 : 1.0;
      }
   }

   private static double movementYaw(float yaw, float forward, float strafe) {
      double radians = Math.toRadians(yaw);
      double sin = Math.sin(radians);
      double cos = Math.cos(radians);
      double dirX = strafe * cos - forward * sin;
      double dirZ = forward * cos + strafe * sin;
      return class_3532.method_15338(Math.toDegrees(Math.atan2(dirZ, dirX)) - 90.0);
   }

   private class_1309 currentTarget() {
      AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
      if (aura == null) {
         return null;
      }

      class_1309 target = aura.getCurrentTarget();
      return target != null && target.method_5805() ? target : null;
   }
}
