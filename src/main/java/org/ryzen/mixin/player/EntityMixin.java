package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import org.ryzen.feature.impl.combat.HitBoxesFeature;
import org.ryzen.feature.impl.movement.NoPushFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1297.class)
public abstract class EntityMixin {
   @Inject(method = "method_5697(Lnet/minecraft/class_1297;)V", at = @At("HEAD"), cancellable = true)
   private void cancelEntityPush(class_1297 entity, CallbackInfo ci) {
      if (NoPushFeature.shouldCancelEntityPush((class_1297)(Object)this, entity)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_5871", at = @At("RETURN"), cancellable = true)
   private void expandPickRadius(CallbackInfoReturnable<Float> cir) {
      HitBoxesFeature hitBoxes = HitBoxesFeature.getEnabled();
      if (hitBoxes != null && hitBoxes.appliesTo((class_1297)(Object)this)) {
         cir.setReturnValue((Float)cir.getReturnValue() + (float)hitBoxes.getHorizontalExpansion());
      }
   }
}
