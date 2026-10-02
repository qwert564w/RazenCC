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
public final class HolyWorldTwoRotation implements AuraRotation {
   private static final float YAW_SPEED_MIN = 50.0F;
   private static final float YAW_SPEED_MAX = 70.0F;
   private static final float PITCH_SPEED_MIN = 10.0F;
   private static final float PITCH_SPEED_MAX = 20.0F;
   private static final float ATTACK_YAW_SPEED = 70.0F;
   private static final float ATTACK_PITCH_SPEED = 24.0F;
   private static final float YAW_RANDOM = 2.0F;
   private static final float PITCH_RANDOM = 2.0F;
   private static final float OSCILLATE_X = 0.2F;
   private static final float OSCILLATE_Y = 0.3F;
   private static final float OSCILLATE_SPEED = 0.7F;
   private float oscillation;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      class_243 aimPoint = target == null ? targetEyePos : target.method_5829().method_1005();
      class_243 delta = aimPoint.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float wantedYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float wantedPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      this.oscillation += 0.7F;
      wantedYaw += (float)Math.sin(this.oscillation) * 0.2F + random(2.0F);
      wantedPitch += (float)Math.cos(this.oscillation) * 0.3F + random(2.0F);
      float currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      float yawStep = attackLikely ? 70.0F : speed(50.0F, 70.0F);
      float pitchStep = attackLikely ? 24.0F : speed(10.0F, 20.0F);
      float yaw = currentYaw + clampStep(class_3532.method_15393(wantedYaw - currentYaw), yawStep);
      float pitch = class_3532.method_15363(currentPitch + clampStep(wantedPitch - currentPitch, pitchStep), -90.0F, 90.0F);
      RotationContext.setRotation(yaw, pitch);
   }

   @Override
   public void reset() {
      this.oscillation = 0.0F;
   }

   private static float clampStep(float difference, float limit) {
      return class_3532.method_15363(difference, -limit, limit);
   }

   private static float speed(float min, float max) {
      return min >= max ? min : (float)ThreadLocalRandom.current().nextDouble(min, max);
   }

   private static float random(float range) {
      return range <= 0.0F ? 0.0F : (float)ThreadLocalRandom.current().nextDouble(-range, range);
   }
}
