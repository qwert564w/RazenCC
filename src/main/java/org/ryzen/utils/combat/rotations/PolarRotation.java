package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class PolarRotation implements AuraRotation {
   private static final long RAMP_MS = 120L;
   private class_1309 lastTarget;
   private long rampStartMs;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      if (target != this.lastTarget) {
         this.lastTarget = target;
         this.rampStartMs = System.currentTimeMillis();
      }

      class_243 aimPoint = target == null ? targetEyePos : target.method_5829().method_1005();
      class_243 delta = aimPoint.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float wantedYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float wantedPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      float currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      float factor = attackLikely ? 1.0F : this.ramp();
      float yaw = currentYaw + class_3532.method_15393(wantedYaw - currentYaw) * factor;
      float pitch = currentPitch + (wantedPitch - currentPitch) * factor;
      RotationContext.setRotation(yaw, class_3532.method_15363(pitch, -89.0F, 89.0F));
   }

   private float ramp() {
      long elapsed = System.currentTimeMillis() - this.rampStartMs;
      return class_3532.method_15363((float)elapsed / 120.0F, 0.35F, 1.0F);
   }

   @Override
   public void reset() {
      this.lastTarget = null;
      this.rampStartMs = 0L;
   }
}
