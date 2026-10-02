package org.ryzen.utils.combat.rotations;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class BuilderRotation implements AuraRotation {
   private static final double POINT_HEIGHT = 0.72;
   private static final double SHOULDER_OFFSET = 1.0;
   private static final double SIDE_SPREAD = 0.38;
   private static final double DEPTH_SPREAD = 0.42;
   private static final long POINT_CHANGE_MIN_MS = 140L;
   private static final long POINT_CHANGE_MAX_MS = 380L;
   private static final float YAW_INERTIA = 0.78F;
   private static final float PITCH_INERTIA = 0.74F;
   private static final float YAW_TRACKING = 0.38F;
   private static final float PITCH_TRACKING = 0.3F;
   private static final float MAX_YAW_STEP = 6.0F;
   private static final float MAX_PITCH_STEP = 2.9F;
   private static final float YAW_ACCELERATION = 0.42F;
   private static final float PITCH_ACCELERATION = 0.28F;
   private static final float PITCH_DEAD_ZONE = 0.65F;
   private static final float YAW_JITTER = 0.14F;
   private static final float PITCH_JITTER = 0.11F;
   private static final float JITTER_RESPONSE = 0.48F;
   private static final float CHAOS = 0.14F;
   private static final float MICRO_PAUSE_CHANCE = 0.06F;
   private static final float JERK_SMOOTHING_YAW = 0.22F;
   private static final float JERK_SMOOTHING_PITCH = 0.14F;
   private float yawVelocity;
   private float pitchVelocity;
   private float yawAcceleration;
   private float pitchAcceleration;
   private long nextPointChangeAt;
   private double pointHeightFactor = 0.72;
   private double pointSide;
   private double pointDepth;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      float currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      if (!attackLikely && ThreadLocalRandom.current().nextFloat() < 0.06F) {
         RotationContext.setRotation(currentYaw + this.yawVelocity, clampPitch(currentPitch + this.pitchVelocity));
      } else {
         class_243 aim = this.aimPoint(target, targetEyePos);
         class_243 delta = aim.method_1020(player.method_33571());
         double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         float wantedYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         float wantedPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
         float yawError = class_3532.method_15393(wantedYaw - currentYaw);
         float pitchError = wantedPitch - currentPitch;
         if (Math.abs(pitchError) < 0.65F) {
            pitchError = 0.0F;
         }

         this.yawVelocity = this.step(this.yawVelocity, yawError, 0.38F, 0.78F, 0.42F, 6.0F, 0.22F, true);
         this.pitchVelocity = this.step(this.pitchVelocity, pitchError, 0.3F, 0.74F, 0.28F, 2.9F, 0.14F, false);
         float yaw = currentYaw + this.yawVelocity + jitter(0.14F, 1.8);
         float pitch = currentPitch + this.pitchVelocity + jitter(0.11F, 2.2);
         RotationContext.setRotation(yaw, clampPitch(pitch));
      }
   }

   private float step(float velocity, float error, float tracking, float inertia, float acceleration, float maxStep, float jerkSmoothing, boolean yawAxis) {
      float wanted = class_3532.method_15363(error * tracking, -maxStep, maxStep);
      float blended = velocity * inertia + wanted * (1.0F - inertia);
      float requestedAcceleration = blended - velocity;
      float previousAcceleration = yawAxis ? this.yawAcceleration : this.pitchAcceleration;
      float smoothed = class_3532.method_16439(jerkSmoothing, previousAcceleration, requestedAcceleration);
      smoothed = class_3532.method_15363(smoothed, -acceleration, acceleration);
      if (yawAxis) {
         this.yawAcceleration = smoothed;
      } else {
         this.pitchAcceleration = smoothed;
      }

      float next = velocity + smoothed;
      float ceiling = maxStep * (1.0F + (ThreadLocalRandom.current().nextFloat() - 0.5F) * 0.14F);
      next = class_3532.method_15363(next, -ceiling, ceiling);
      if (error > 0.0F && next > error) {
         next = error;
      } else if (error < 0.0F && next < error) {
         next = error;
      }

      return next;
   }

   private class_243 aimPoint(class_1309 target, class_243 targetEyePos) {
      long now = System.currentTimeMillis();
      if (now >= this.nextPointChangeAt) {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         this.nextPointChangeAt = now + random.nextLong(140L, 381L);
         this.pointHeightFactor = 0.72 + (random.nextDouble() - 0.5) * 0.2;
         this.pointSide = (random.nextDouble() * 2.0 - 1.0) * 0.38 * 1.0;
         this.pointDepth = (random.nextDouble() * 2.0 - 1.0) * 0.42;
      }

      double width = target.method_17681();
      class_243 feet = target.method_73189();
      class_243 base = feet.method_1031(0.0, (targetEyePos.field_1351 - feet.field_1351) * this.pointHeightFactor, 0.0);
      double yawRadians = Math.toRadians(target.method_36454());
      double rightX = Math.cos(yawRadians);
      double rightZ = Math.sin(yawRadians);
      return base.method_1031(
         rightX * this.pointSide * width - rightZ * this.pointDepth * width, 0.0, rightZ * this.pointSide * width + rightX * this.pointDepth * width
      );
   }

   private static float jitter(float amount, double rate) {
      if (amount <= 0.0F) {
         return 0.0F;
      }

      double phase = System.currentTimeMillis() / 1000.0 * rate;
      double wave = Math.sin(phase) * 0.04 + Math.sin(phase * 2.7) * 0.015;
      double noise = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.02;
      return (float)((wave + noise) * amount * 0.48F * 100.0);
   }

   private static float clampPitch(float pitch) {
      return class_3532.method_15363(pitch, -90.0F, 90.0F);
   }

   @Override
   public void onAttack() {
      this.yawVelocity *= 0.5F;
      this.pitchVelocity *= 0.5F;
      this.yawAcceleration = 0.0F;
      this.pitchAcceleration = 0.0F;
   }

   @Override
   public void reset() {
      this.yawVelocity = 0.0F;
      this.pitchVelocity = 0.0F;
      this.yawAcceleration = 0.0F;
      this.pitchAcceleration = 0.0F;
      this.nextPointChangeAt = 0L;
   }
}
