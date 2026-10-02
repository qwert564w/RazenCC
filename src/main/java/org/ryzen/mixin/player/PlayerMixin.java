package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import org.ryzen.feature.impl.movement.NoPushFeature;
import org.ryzen.feature.impl.movement.NoWebFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1657.class)
public abstract class PlayerMixin {
   @Inject(method = "method_5844(Lnet/minecraft/class_2680;Lnet/minecraft/class_243;)V", at = @At("HEAD"), cancellable = true)
   private void cancelBlockPush(class_2680 state, class_243 multiplier, CallbackInfo ci) {
      class_1657 player = (class_1657)(Object)this;
      if (NoPushFeature.shouldCancelBlockPush(player, state) || NoWebFeature.shouldCancelWeb(player, state)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_5675()Z", at = @At("HEAD"), cancellable = true)
   private void cancelFluidPush(CallbackInfoReturnable<Boolean> cir) {
      if (NoPushFeature.shouldCancelFluidPush((class_1657)(Object)this)) {
         cir.setReturnValue(false);
      }
   }
}
