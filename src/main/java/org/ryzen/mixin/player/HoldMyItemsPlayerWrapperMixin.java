package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_742;
import org.ryzen.feature.impl.visual.HoldMyItemsCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Pseudo
@Mixin(targets = "com/holdmylua/source/scripting/script_wrappers/P", remap = false)
public abstract class HoldMyItemsPlayerWrapperMixin {
   @Inject(method = "isOnGround", at = @At("HEAD"), cancellable = true, remap = false)
   private void alternateAuraAttackStyle(class_742 player, CallbackInfoReturnable<Boolean> callback) {
      if (HoldMyItemsCompat.shouldUseGroundAttackStyle(player)) {
         callback.setReturnValue(true);
      }
   }
}
