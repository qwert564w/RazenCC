package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1536;
import org.ryzen.feature.impl.movement.NoPushFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_1536.class)
public abstract class FishingHookMixin {
   @Inject(method = "method_6954(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true)
   private void cancelFishingHookPull(class_1297 entity, CallbackInfo ci) {
      if (NoPushFeature.shouldCancelFishingHookPull((class_1536)(Object)this, entity)) {
         ci.cancel();
      }
   }
}
