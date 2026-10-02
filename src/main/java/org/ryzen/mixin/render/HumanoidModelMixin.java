package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10034;
import net.minecraft.class_572;
import org.ryzen.utils.render.EmotionAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_572.class)
public abstract class HumanoidModelMixin {
   @Inject(method = "method_17087(Lnet/minecraft/class_10034;)V", at = @At("TAIL"))
   private void ryzen$applyEmotions(class_10034 state, CallbackInfo ci) {
      EmotionAnimator.apply((class_572)(Object)this, state);
   }
}
