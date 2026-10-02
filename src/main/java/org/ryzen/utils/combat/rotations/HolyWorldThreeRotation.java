package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class HolyWorldThreeRotation implements AuraRotation {
   private static final float YAW_EASE = 0.45F;
   private static final float PITCH_EASE = 0.35F;
   private static final float SETTLE_DEGREES = 1.5F;

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
      float yaw = attackLikely ? wantedYaw : currentYaw + ease(yawDiff, 0.45F);
      float pitch = attackLikely ? wantedPitch : currentPitch + ease(pitchDiff, 0.35F);
      RotationContext.setRotation(yaw, class_3532.method_15363(pitch, -90.0F, 90.0F));
   }

   private static float ease(float difference, float factor) {
      return Math.abs(difference) <= 1.5F ? difference : difference * factor;
   }
}
