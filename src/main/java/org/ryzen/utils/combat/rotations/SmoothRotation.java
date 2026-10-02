package org.ryzen.utils.combat.rotations;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class SmoothRotation implements AuraRotation {
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();
   private static final float YAW_BUDGET = 60.0F;
   private static final float PITCH_BUDGET = 40.0F;
   private static final float WOBBLE_MIN = 2.0F;
   private static final float WOBBLE_MAX = 5.0F;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      float currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      class_243 delta = targetEyePos.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float wantedYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float wantedPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      float yawDiff = class_3532.method_15393(wantedYaw - currentYaw);
      float pitchDiff = wantedPitch - currentPitch;
      float hypot = (float)Math.hypot(Math.abs(yawDiff), Math.abs(pitchDiff));
      if (hypot <= 1.0E-4F) {
         RotationContext.setRotation(wantedYaw, class_3532.method_15363(wantedPitch, -90.0F, 90.0F));
      } else {
         float yawCap = Math.abs(yawDiff / hypot) * 60.0F;
         float pitchCap = Math.abs(pitchDiff / hypot) * 40.0F;
         float steppedYaw = class_3532.method_15363(yawDiff, -yawCap, yawCap) + (attackLikely ? 0.0F : wobble());
         float steppedPitch = class_3532.method_15363(pitchDiff, -pitchCap, pitchCap);
         float yaw = (float)class_3532.method_16436(0.6 + ThreadLocalRandom.current().nextDouble() * 0.6, currentYaw, currentYaw + steppedYaw);
         float pitch = (float)class_3532.method_16436(0.5 + ThreadLocalRandom.current().nextDouble() * 0.5, currentPitch, currentPitch + steppedPitch);
         RotationContext.setRotation(yaw, class_3532.method_15363(pitch, -90.0F, 90.0F));
      }
   }

   private static float wobble() {
      float amplitude = class_3532.method_16439(SECURE_RANDOM.nextFloat(), 2.0F, 5.0F);
      return (float)Math.ceil(amplitude * Math.sin(System.currentTimeMillis() / 40.0));
   }
}
