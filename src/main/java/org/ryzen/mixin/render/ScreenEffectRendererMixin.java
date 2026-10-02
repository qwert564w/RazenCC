package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1058;
import net.minecraft.class_1799;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4603;
import net.minecraft.class_5819;
import net.minecraft.class_9334;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_4603.class)
public abstract class ScreenEffectRendererMixin {
   @Inject(method = "method_23070", at = @At("HEAD"), cancellable = true)
   private static void onRenderFire(class_4587 poseStack, class_4597 bufferSource, class_1058 sprite, CallbackInfo ci) {
      if (RemovalsFeature.shouldRemoveFireOverlay()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_70938", at = @At("HEAD"), cancellable = true)
   private void onDisplayItemActivation(class_1799 itemStack, class_5819 random, CallbackInfo ci) {
      if (itemStack.method_57826(class_9334.field_54274) && RemovalsFeature.shouldRemoveTotemOverlay()) {
         ci.cancel();
      }
   }
}
