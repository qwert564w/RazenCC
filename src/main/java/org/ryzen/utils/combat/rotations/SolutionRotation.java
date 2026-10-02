package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class SolutionRotation implements AuraRotation {
   private static final float BASE_STEP = 24.0F;
   private static final float PITCH_DIVISOR = 1.9F;
   private static final float ACCEL_RISE = 0.06F;
   private static final float ACCEL_DECAY = 0.12F;
   private static final float ACCEL_MIN = 0.35F;
   private static final float ACCEL_MAX = 1.6F;
   private static final float JITTER_AMPLITUDE = 0.8F;
   private static final float JITTER_SMOOTHING = 4.0F;
   private static final float ATTACK_SHIFT = 1.4F;
   private static final float ATTACK_DECAY = 0.6F;
   private float accel = 0.35F;
   private float jitterYaw;
   private float jitterPitch;
   private float shiftYaw;
   private float shiftPitch;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      class_243 aimPoint = target == null ? targetEyePos : target.method_5829().method_1005();
      class_243 delta = aimPoint.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float wantedYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float wantedPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      float currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      float yawDiff = class_3532.method_15393(wantedYaw - currentYaw);
      float pitchDiff = wantedPitch - currentPitch;
      boolean closing = Math.abs(yawDiff) > 24.0F * this.accel;
      this.accel = class_3532.method_15363(this.accel + (closing ? 0.06F : -0.12F), 0.35F, 1.6F);
      float yawStep = 24.0F * this.accel;
      float pitchStep = yawStep / 1.9F;
      float yaw = currentYaw + class_3532.method_15363(yawDiff, -yawStep, yawStep);
      float pitch = currentPitch + class_3532.method_15363(pitchDiff, -pitchStep, pitchStep);
      if (!attackLikely) {
         yaw += this.tickJitterYaw();
         pitch += this.tickJitterPitch();
      }

      yaw += this.shiftYaw;
      pitch += this.shiftPitch;
      this.shiftYaw *= 0.6F;
      this.shiftPitch *= 0.6F;
      RotationContext.setRotation(yaw, class_3532.method_15363(pitch, -89.0F, 89.0F));
   }

   private float tickJitterYaw() {
      float sample = ((float)Math.random() - 0.5F) * 2.0F * 0.8F;
      this.jitterYaw = class_3532.method_16439(0.25F, this.jitterYaw, sample);
      return this.jitterYaw;
   }

   private float tickJitterPitch() {
      float sample = ((float)Math.random() - 0.5F) * 2.0F * 0.8F;
      this.jitterPitch = class_3532.method_16439(0.25F, this.jitterPitch, sample);
      return this.jitterPitch;
   }

   @Override
   public void onAttack() {
      this.shiftYaw = ((float)Math.random() - 0.5F) * 2.0F * 1.4F;
      this.shiftPitch = ((float)Math.random() - 0.5F) * 1.4F;
   }

   @Override
   public void reset() {
      this.accel = 0.35F;
      this.jitterYaw = 0.0F;
      this.jitterPitch = 0.0F;
      this.shiftYaw = 0.0F;
      this.shiftPitch = 0.0F;
   }
}
