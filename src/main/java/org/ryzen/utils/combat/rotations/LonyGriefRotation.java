package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class LonyGriefRotation implements AuraRotation {
   private static final float BUDGET = 40.0F;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      class_243 aimPoint = target == null ? targetEyePos : target.method_5829().method_1005();
      class_243 delta = aimPoint.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float wantedYaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float wantedPitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      float currentYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float currentPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      float yaw = currentYaw + class_3532.method_15363(class_3532.method_15393(wantedYaw - currentYaw), -40.0F, 40.0F);
      float pitch = currentPitch + class_3532.method_15363(wantedPitch - currentPitch, -40.0F, 40.0F);
      RotationContext.setRotation(yaw, class_3532.method_15363(pitch, -89.0F, 89.0F));
   }
}
