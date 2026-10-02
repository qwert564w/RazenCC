package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public class MatrixVulcanRotation implements AuraRotation {
   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      this.rotateDirect(player, targetEyePos);
   }

   protected final void rotateDirect(class_746 player, class_243 targetEyePos) {
      RotationContext.rotateToPosition(player, targetEyePos);
   }
}
