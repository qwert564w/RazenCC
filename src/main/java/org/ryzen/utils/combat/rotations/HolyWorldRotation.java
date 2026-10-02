package org.ryzen.utils.combat.rotations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class HolyWorldRotation extends MatrixVulcanRotation {
   private static final int PRE_ATTACK_TICKS = 2;
   private int preAttackTicks;

   @Override
   public void tick(class_746 player, class_1309 target, class_243 targetEyePos, boolean attackLikely) {
      if (attackLikely) {
         this.preAttackTicks = 2;
      }

      if (this.preAttackTicks > 0) {
         this.rotateDirect(player, targetEyePos);
         this.preAttackTicks--;
      }
   }

   @Override
   public void onAttack() {
      this.preAttackTicks = 0;
   }

   @Override
   public void reset() {
      this.preAttackTicks = 0;
   }
}
