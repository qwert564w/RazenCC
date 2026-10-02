package org.ryzen.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_9779.class_9781;
import org.ryzen.injection.TimerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(class_9781.class)
public abstract class DeltaTrackerTimerMixin implements TimerAccess {
   @Unique
   private float ryzen$speedMultiplier = 1.0F;

   @ModifyExpressionValue(method = "method_60639", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/floats/FloatUnaryOperator;apply(F)F"))
   private float ryzen$scaleMspt(float effectiveMsPerTick) {
      return this.ryzen$speedMultiplier == 1.0F ? effectiveMsPerTick : effectiveMsPerTick / this.ryzen$speedMultiplier;
   }

   @Override
   public void ryzen$setSpeedMultiplier(float multiplier) {
      this.ryzen$speedMultiplier = multiplier;
   }

   @Override
   public float ryzen$getSpeedMultiplier() {
      return this.ryzen$speedMultiplier;
   }
}
