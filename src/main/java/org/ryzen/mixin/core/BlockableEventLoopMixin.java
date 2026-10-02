package org.ryzen.mixin.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1255;
import net.minecraft.class_156;
import net.minecraft.class_310;
import org.ryzen.context.RenderContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_1255.class)
public abstract class BlockableEventLoopMixin {
   @Inject(method = "method_5383", at = @At("HEAD"), cancellable = true)
   private void onRunAllTasks(CallbackInfo ci) {
      if (RenderContext.overlayStartTime > -1L) {
         long elapsed = class_156.method_658() - RenderContext.overlayStartTime;
         class_310 mc = class_310.method_1551();
         boolean isStartup = mc.field_1755 == null;
         long maxDeferTime = isStartup ? 3000L : 1800L;
         if (elapsed < maxDeferTime) {
            ci.cancel();
         }
      }
   }
}
