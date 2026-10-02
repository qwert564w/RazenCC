package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2394;
import net.minecraft.class_702;
import net.minecraft.class_703;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_702.class)
public abstract class ParticleEngineMixin {
   @Inject(method = "method_3056", at = @At("HEAD"), cancellable = true)
   private void onCreateParticle(class_2394 options, double x, double y, double z, double xa, double ya, double za, CallbackInfoReturnable<class_703> cir) {
      if (RemovalsFeature.shouldRemoveParticle(options)) {
         cir.setReturnValue(null);
      }
   }
}
