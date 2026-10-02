package org.ryzen.mixin.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1937;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1937.class)
public abstract class LevelMixin {
   @Inject(method = "method_8430", at = @At("HEAD"), cancellable = true)
   private void onGetRainLevel(float partialTick, CallbackInfoReturnable<Float> cir) {
      if (RemovalsFeature.shouldRemoveWeather()) {
         cir.setReturnValue(0.0F);
      }
   }

   @Inject(method = "method_8478", at = @At("HEAD"), cancellable = true)
   private void onGetThunderLevel(float partialTick, CallbackInfoReturnable<Float> cir) {
      if (RemovalsFeature.shouldRemoveWeather()) {
         cir.setReturnValue(0.0F);
      }
   }
}
