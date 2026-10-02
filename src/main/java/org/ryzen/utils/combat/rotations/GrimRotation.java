package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class GrimRotation extends MatrixVulcanRotation {
   private static final int PRE_ATTACK_TICKS = 2;
   private static final int POST_ATTACK_TICKS = 2;
   private int preAttackTicks;
   private int postAttackTicks;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      if (attackLikely) {
         this.preAttackTicks = 2;
      }

      if (this.preAttackTicks <= 0 && this.postAttackTicks <= 0) {
         RotationContext.clear();
      } else {
         this.rotateDirect(player, targetEyePos);
      }

      if (this.preAttackTicks > 0) {
         this.preAttackTicks--;
      }

      if (this.postAttackTicks > 0) {
         this.postAttackTicks--;
      }
   }

   @Override
   public void onAttack() {
      this.preAttackTicks = 0;
      this.postAttackTicks = 2;
   }

   @Override
   public void reset() {
      this.preAttackTicks = 0;
      this.postAttackTicks = 0;
   }
}
