package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_4071;
import org.ryzen.context.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public abstract class GuiMixin {
   @Inject(method = "method_18502", at = @At("HEAD"))
   private void onSetOverlay(class_4071 overlay, CallbackInfo ci) {
      if (overlay == null) {
         RenderContext.overlayStartTime = -1L;
      }
   }
}
