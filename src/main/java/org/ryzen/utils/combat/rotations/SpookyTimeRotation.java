package org.ryzen.utils.combat.rotations;

import java.security.SecureRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class SpookyTimeRotation implements AuraRotation {
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();
   private static final float YAW_JITTER = 1.5F;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      float fromYaw = player.method_36454();
      float fromPitch = player.method_36455();
      class_238 box = target.method_5829();
      double centerX = (box.field_1323 + box.field_1320) * 0.5;
      double centerZ = (box.field_1321 + box.field_1324) * 0.5;
      double bodyY = box.field_1322 + (box.field_1325 - box.field_1322) * 0.4;
      SpookyTimeRotation.Rotation goal = toPoint(player, new class_243(centerX, bodyY, centerZ));
      float goalYaw = goal.yaw() + legitRandom(-1.5F, 1.5F);
      float yaw = moveTowardsAngle(fromYaw, goalYaw, legitRandom(65.0F, 95.0F));
      float pitch = fromPitch;
      if (!onTarget(player, target, yaw, pitch)) {
         pitch += class_3532.method_15363((goal.pitch() - pitch) * 0.3F, -10.0F, 10.0F);
      }

      if (!onTarget(player, target, yaw, pitch)) {
         yaw = goal.yaw();
         pitch = goal.pitch();
      }

      SpookyTimeRotation.Rotation corrected = correctRotation(fromYaw, fromPitch, yaw, pitch);
      RotationContext.setRotation(corrected.yaw(), corrected.pitch());
   }

   private static SpookyTimeRotation.Rotation toPoint(class_746 player, class_243 point) {
      class_243 eye = player.method_33571();
      double dx = point.field_1352 - eye.field_1352;
      double dy = point.field_1351 - eye.field_1351;
      double dz = point.field_1350 - eye.field_1350;
      double horizontal = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = class_3532.method_15363((float)(-Math.toDegrees(Math.atan2(dy, horizontal))), -90.0F, 90.0F);
      return new SpookyTimeRotation.Rotation(yaw, pitch);
   }

   private static float moveTowardsAngle(float current, float target, float speed) {
      float difference = class_3532.method_15393(target - current);
      return Math.abs(difference) <= speed ? target : current + Math.signum(difference) * speed;
   }

   private static boolean onTarget(class_746 player, class_1309 target, float yaw, float pitch) {
      class_243 eye = player.method_33571();
      class_238 box = target.method_5829().method_1014(target.method_5871() + 0.02);
      double reach = eye.method_1022(box.method_1005()) + 1.0;
      class_243 end = eye.method_1019(class_243.method_1030(pitch, yaw).method_1021(reach));
      return box.method_992(eye, end).isPresent();
   }

   private static float legitRandom(float min, float max) {
      return switch (SECURE_RANDOM.nextInt(4)) {
         case 0 -> averageRandom(min, max);
         case 1 -> smoothRandom(min, max);
         case 2 -> min + (max - min) * (float)Math.random();
         default -> min + (max - min) * SECURE_RANDOM.nextFloat();
      };
   }

   private static float averageRandom(float min, float max) {
      float first = SECURE_RANDOM.nextFloat();
      float second = SECURE_RANDOM.nextFloat();
      return min + (max - min) * ((first + second) / 2.0F);
   }

   private static float smoothRandom(float min, float max) {
      double randomA = SECURE_RANDOM.nextDouble();
      double randomB = SECURE_RANDOM.nextDouble();
      double randomC = SECURE_RANDOM.nextGaussian() * 0.02F;
      double smoothFactor = Math.pow(randomA, 1.0 + SECURE_RANDOM.nextDouble() * 0.7);
      double mixFactor = (randomB * 0.8 + 0.1) * (Math.log1p(randomA * 3.0) * 0.5 + 0.5);
      return (float)(min + (max - min) * smoothFactor * mixFactor + randomC);
   }

   private static SpookyTimeRotation.Rotation correctRotation(float fromYaw, float fromPitch, float toYaw, float toPitch) {
      float step = gcdStep();
      float deltaYaw = class_3532.method_15393(toYaw - fromYaw);
      float deltaPitch = toPitch - fromPitch;
      deltaYaw = Math.round(deltaYaw / step) * step;
      deltaPitch = Math.round(deltaPitch / step) * step;
      return new SpookyTimeRotation.Rotation(fromYaw + deltaYaw, class_3532.method_15363(fromPitch + deltaPitch, -90.0F, 90.0F));
   }

   private static float gcdStep() {
      double sensitivity = (Double)class_310.method_1551().field_1690.method_42495().method_41753();
      double factor = sensitivity * 0.6 + 0.2;
      return (float)(factor * factor * factor * 1.2);
   }

   @Environment(EnvType.CLIENT)
   private record Rotation(float yaw, float pitch) {
   }
}
