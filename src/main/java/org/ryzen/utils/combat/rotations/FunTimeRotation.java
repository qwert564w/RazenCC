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
public final class FunTimeRotation implements AuraRotation {
   private static final int PHASE_DIRECT = 0;
   private static final int PHASE_OVERSHOOT = 1;
   private static final int PHASE_RETURN = 2;
   private static final int PRE_ATTACK_TICKS = 2;
   private static final int POST_ATTACK_TICKS = 2;
   private static final double OVERSHOOT_MIN_DISTANCE = 25.0;
   private static final double FINISH_DISTANCE = 0.65;
   private static final double STOP_DISTANCE = 0.08;
   private int targetId = Integer.MIN_VALUE;
   private int phase = 0;
   private int preAttackTicks;
   private int postAttackTicks;
   private double velocityYaw;
   private double velocityPitch;
   private double windYaw;
   private double windPitch;
   private double speed;
   private double gravity;
   private double wind;
   private double threshold;
   private double overshootYaw;
   private double overshootPitch;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      if (target != null && target.method_5805()) {
         if (this.targetId != target.method_5628()) {
            this.resetMotion(target.method_5628());
         }

         double currentYaw = player.field_6241;
         double currentPitch = player.method_36455();
         double realYaw = currentYaw + class_3532.method_15338(targetYaw(player, targetEyePos) - currentYaw);
         double realPitch = targetPitch(player, targetEyePos);
         double realDistance = distance(currentYaw, currentPitch, realYaw, realPitch);
         if (attackLikely) {
            this.preAttackTicks = 2;
         }

         if (this.preAttackTicks <= 0 && this.postAttackTicks <= 0) {
            if (this.phase == 2) {
               this.tuneCorrection();
            } else {
               this.tuneIdle();
               this.maybeStartOvershoot(currentYaw, currentPitch, realYaw, realPitch, realDistance);
            }

            double targetYaw = this.phase == 1 ? this.overshootYaw : realYaw;
            double targetPitch = this.phase == 1 ? this.overshootPitch : realPitch;
            double deltaYaw = targetYaw - currentYaw;
            double deltaPitch = targetPitch - currentPitch;
            double targetDistance = distance(0.0, 0.0, deltaYaw, deltaPitch);
            if (targetDistance < 0.65 && this.phase == 1) {
               this.phase = 2;
               this.velocityYaw *= 0.45;
               this.velocityPitch *= 0.45;
               this.tuneCorrection();
               targetYaw = realYaw;
               targetPitch = realPitch;
               deltaYaw = targetYaw - currentYaw;
               deltaPitch = targetPitch - currentPitch;
               targetDistance = distance(0.0, 0.0, deltaYaw, deltaPitch);
            } else if (targetDistance < 0.65 && this.phase == 2) {
               this.phase = 0;
               this.velocityYaw *= 0.25;
               this.velocityPitch *= 0.25;
            }

            if (targetDistance < 0.08) {
               this.velocityYaw *= 0.35;
               this.velocityPitch *= 0.35;
               RotationContext.setRotation((float)targetYaw, (float)targetPitch);
            } else {
               FunTimeRotation.StepParams params = this.scaledParams(targetDistance);
               this.applyWind(params.wind());
               double gravityYaw = params.gravity() * deltaYaw / targetDistance;
               double gravityPitch = params.gravity() * deltaPitch / targetDistance;
               this.velocityYaw = this.velocityYaw + (this.windYaw + gravityYaw);
               this.velocityPitch = this.velocityPitch + (this.windPitch + gravityPitch);
               this.applyMicroCorrections(attackLikely, targetDistance);
               this.brakeWhenPassingTarget(deltaYaw, deltaPitch, targetDistance);
               this.clampVelocity(params.speed());
               double nextYaw = currentYaw + this.velocityYaw;
               double nextPitch = clamp(currentPitch + this.velocityPitch, -90.0, 90.0);
               RotationContext.setRotation((float)nextYaw, (float)nextPitch);
            }
         } else {
            this.phase = 0;
            this.velocityYaw = 0.0;
            this.velocityPitch = 0.0;
            this.windYaw = 0.0;
            this.windPitch = 0.0;
            this.tuneCombat();
            RotationContext.setRotation((float)realYaw, (float)realPitch);
            if (this.preAttackTicks > 0) {
               this.preAttackTicks--;
            }

            if (this.postAttackTicks > 0) {
               this.postAttackTicks--;
            }
         }
      } else {
         this.reset();
         RotationContext.clear();
      }
   }

   @Override
   public void onAttack() {
      this.preAttackTicks = 0;
      this.postAttackTicks = 2;
      this.phase = 0;
      this.velocityYaw *= 0.35;
      this.velocityPitch *= 0.35;
      this.windYaw *= 0.2;
      this.windPitch *= 0.2;
   }

   @Override
   public void reset() {
      this.targetId = Integer.MIN_VALUE;
      this.phase = 0;
      this.preAttackTicks = 0;
      this.postAttackTicks = 0;
      this.velocityYaw = 0.0;
      this.velocityPitch = 0.0;
      this.windYaw = 0.0;
      this.windPitch = 0.0;
      this.speed = 0.0;
      this.gravity = 0.0;
      this.wind = 0.0;
      this.threshold = 0.0;
      this.overshootYaw = 0.0;
      this.overshootPitch = 0.0;
   }

   private void resetMotion(int nextTargetId) {
      this.targetId = nextTargetId;
      this.phase = 0;
      this.preAttackTicks = 0;
      this.postAttackTicks = 0;
      this.velocityYaw = 0.0;
      this.velocityPitch = 0.0;
      this.windYaw = 0.0;
      this.windPitch = 0.0;
      this.speed = 0.0;
      this.gravity = 0.0;
      this.wind = 0.0;
      this.threshold = 0.0;
   }

   private void maybeStartOvershoot(double currentYaw, double currentPitch, double realYaw, double realPitch, double realDistance) {
      if (this.phase == 0 && !(realDistance < 25.0) && !(random().nextDouble() >= 0.75)) {
         double angle = Math.atan2(realPitch - currentPitch, realYaw - currentYaw);
         double amount = randomRange(4.0, Math.min(16.0, realDistance * 0.35));
         this.overshootYaw = realYaw + Math.cos(angle) * amount;
         this.overshootPitch = clamp(realPitch + Math.sin(angle) * amount, -89.0, 89.0);
         this.phase = 1;
      }
   }

   private void tuneCombat() {
      this.tune(randomRange(18.0, 26.0), randomRange(9.5, 14.0), randomRange(0.35, 1.25), 3.0, 0.32);
   }

   private void tuneIdle() {
      this.tune(randomRange(8.0, 14.0), randomRange(4.5, 8.0), randomRange(2.5, 5.5), 8.0, 0.2);
   }

   private void tuneCorrection() {
      this.tune(randomRange(4.0, 8.0), randomRange(7.0, 10.0), randomRange(0.35, 1.2), 2.5, 0.28);
   }

   private void tune(double nextSpeed, double nextGravity, double nextWind, double nextThreshold, double factor) {
      if (this.speed <= 0.0) {
         this.speed = nextSpeed;
         this.gravity = nextGravity;
         this.wind = nextWind;
         this.threshold = nextThreshold;
      } else {
         this.speed = lerp(this.speed, nextSpeed, factor);
         this.gravity = lerp(this.gravity, nextGravity, factor);
         this.wind = lerp(this.wind, nextWind, factor);
         this.threshold = lerp(this.threshold, nextThreshold, factor);
      }
   }

   private FunTimeRotation.StepParams scaledParams(double targetDistance) {
      if (targetDistance >= this.threshold) {
         return new FunTimeRotation.StepParams(this.speed, this.gravity, this.wind);
      }

      double progress = clamp(targetDistance / Math.max(this.threshold, 0.1), 0.0, 1.0);
      double localSpeed = Math.max(0.65, this.speed * (0.35 + progress * 0.65));
      double localGravity = Math.max(1.2, this.gravity * (0.55 + progress * 0.45));
      double localWind = Math.max(0.04, this.wind * (0.2 + progress * 0.8));
      return new FunTimeRotation.StepParams(localSpeed, localGravity, localWind);
   }

   private void applyWind(double localWind) {
      this.windYaw = this.windYaw * 0.86 + randomRange(-localWind, localWind) * 0.14;
      this.windPitch = this.windPitch * 0.86 + randomRange(-localWind, localWind) * 0.14;
   }

   private void applyMicroCorrections(boolean attackLikely, double targetDistance) {
      if (!(targetDistance > this.threshold)) {
         double scale = attackLikely ? 0.055 : 0.12;
         this.velocityYaw = this.velocityYaw + randomRange(-scale, scale);
         this.velocityPitch = this.velocityPitch + randomRange(-scale, scale);
      }
   }

   private void brakeWhenPassingTarget(double deltaYaw, double deltaPitch, double targetDistance) {
      if (!(targetDistance > this.threshold)) {
         double dot = this.velocityYaw * deltaYaw + this.velocityPitch * deltaPitch;
         if (dot < 0.0) {
            this.velocityYaw *= 0.58;
            this.velocityPitch *= 0.58;
         }
      }
   }

   private void clampVelocity(double maxSpeed) {
      double magnitude = distance(0.0, 0.0, this.velocityYaw, this.velocityPitch);
      if (!(magnitude <= maxSpeed) && !(magnitude <= 0.0)) {
         this.velocityYaw = this.velocityYaw / magnitude * maxSpeed;
         this.velocityPitch = this.velocityPitch / magnitude * maxSpeed;
      }
   }

   private static float targetYaw(class_746 player, class_243 targetEyePos) {
      class_243 delta = targetEyePos.method_1020(player.method_33571());
      return (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
   }

   private static float targetPitch(class_746 player, class_243 targetEyePos) {
      class_243 delta = targetEyePos.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      return (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
   }

   private static double distance(double fromYaw, double fromPitch, double toYaw, double toPitch) {
      double yaw = toYaw - fromYaw;
      double pitch = toPitch - fromPitch;
      return Math.sqrt(yaw * yaw + pitch * pitch);
   }

   private static double lerp(double current, double target, double factor) {
      return current + (target - current) * factor;
   }

   private static double randomRange(double min, double max) {
      return random().nextDouble(min, max);
   }

   private static ThreadLocalRandom random() {
      return ThreadLocalRandom.current();
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record StepParams(double speed, double gravity, double wind) {
   }
}
