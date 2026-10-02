package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_742;
import net.minecraft.class_8685;
import org.ryzen.utils.render.ClientCape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_742.class)
public abstract class AbstractClientPlayerMixin {
   @Inject(method = "method_52814", at = @At("RETURN"), cancellable = true)
   private void applyCape(CallbackInfoReturnable<class_8685> cir) {
      class_742 player = (class_742)(Object)this;
      if (ClientCape.shouldForceCape(player.method_5667())) {
         cir.setReturnValue(ClientCape.apply((class_8685)cir.getReturnValue()));
      }
   }
}
