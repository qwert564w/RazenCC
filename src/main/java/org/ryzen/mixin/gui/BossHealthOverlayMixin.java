package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_332;
import net.minecraft.class_337;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_337.class)
public abstract class BossHealthOverlayMixin {
   @Inject(method = "method_1796", at = @At("HEAD"), cancellable = true)
   private void onRender(class_332 guiGraphics, CallbackInfo ci) {
      if (RemovalsFeature.shouldRemoveBossBar()) {
         ci.cancel();
      }
   }
}
