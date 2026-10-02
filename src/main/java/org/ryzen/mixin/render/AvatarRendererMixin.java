package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1007;
import net.minecraft.class_11890;
import org.ryzen.feature.impl.visual.NameTagsFeature;
import org.ryzen.utils.render.ClientCape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1007.class)
public abstract class AvatarRendererMixin {
   @Inject(method = "method_62604(Lnet/minecraft/class_11890;Lnet/minecraft/class_10055;F)V", at = @At("TAIL"))
   private void forceCapeVisible(class_11890 avatar, class_10055 state, float tickDelta, CallbackInfo ci) {
      if (ClientCape.shouldForceCape(avatar.method_5667())) {
         state.field_53532 = true;
      }
   }

   @Inject(method = "method_74935(Lnet/minecraft/class_11890;D)Z", at = @At("HEAD"), cancellable = true)
   private void hideVanillaNameTag(class_11890 avatar, double distanceSqr, CallbackInfoReturnable<Boolean> cir) {
      if (NameTagsFeature.shouldHideVanillaTag()) {
         cir.setReturnValue(false);
      }
   }
}
