package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.feature.impl.player.NoInteractFeature;
import org.ryzen.feature.impl.visual.HitParticlesFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_636.class)
public abstract class MultiPlayerGameModeMixin {
   @Inject(method = "method_2896", at = @At("HEAD"), cancellable = true)
   private void ryzen$blockInteraction(class_746 player, class_1268 hand, class_3965 hit, CallbackInfoReturnable<class_1269> cir) {
      if (NoInteractFeature.shouldCancel(hit)) {
         cir.setReturnValue(class_1269.field_5814);
      }
   }

   @Inject(method = "method_2918", at = @At("HEAD"))
   private void onAttack(class_1657 player, class_1297 target, CallbackInfo ci) {
      if (player == MinecraftContext.mc.field_1724) {
         HitParticlesFeature hitParticles = HitParticlesFeature.getEnabled();
         if (hitParticles != null) {
            hitParticles.onAttack(target);
         }
      }
   }
}
