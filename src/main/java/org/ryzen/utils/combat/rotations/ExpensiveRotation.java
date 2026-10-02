package org.ryzen.utils.combat.rotations;

import java.security.SecureRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;

@Environment(EnvType.CLIENT)
public final class ExpensiveRotation implements AuraRotation {
   private static final SecureRandom SECURE_RANDOM = new SecureRandom();
   private final float minFactor;
   private final float maxFactor;
   private final float recoilFactor;
   private final long recoilWindowMs;
   private final boolean snapOnAttack;
   private long lastAttackMs;

   public static ExpensiveRotation grim() {
      return new ExpensiveRotation(0.4F, 0.7F, -0.2F, 50L, false);
   }

   public static ExpensiveRotation funTime() {
      return new ExpensiveRotation(0.2F, 0.4F, -0.2F, 500L, true);
   }

   public static ExpensiveRotation spookyTime() {
      return new ExpensiveRotation(0.3F, 0.5F, -0.2F, 500L, true);
   }

   private ExpensiveRotation(float minFactor, float maxFactor, float recoilFactor, long recoilWindowMs, boolean snapOnAttack) {
      this.minFactor = minFactor;
      this.maxFactor = maxFactor;
      this.recoilFactor = recoilFactor;
      this.recoilWindowMs = recoilWindowMs;
      this.snapOnAttack = snapOnAttack;
   }

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
      if (attackLikely && this.snapOnAttack) {
         RotationContext.setRotation(wantedYaw, class_3532.method_15363(wantedPitch, -90.0F, 90.0F));
      } else {
         float factor = this.factor(attackLikely);
         RotationContext.setRotation(currentYaw + yawDiff * factor, class_3532.method_15363(currentPitch + pitchDiff * factor, -90.0F, 90.0F));
      }
   }

   private float factor(boolean attackLikely) {
      if (attackLikely) {
         return 1.0F;
      } else {
         return System.currentTimeMillis() - this.lastAttackMs < this.recoilWindowMs
            ? this.recoilFactor
            : class_3532.method_16439(SECURE_RANDOM.nextFloat(), this.minFactor, this.maxFactor);
      }
   }

   @Override
   public void onAttack() {
      this.lastAttackMs = System.currentTimeMillis();
   }

   @Override
   public void reset() {
      this.lastAttackMs = 0L;
   }
}
