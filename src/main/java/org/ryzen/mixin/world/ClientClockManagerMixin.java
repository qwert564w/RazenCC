package org.ryzen.mixin.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1937;
import net.minecraft.class_638;
import org.ryzen.feature.impl.visual.WorldTweaksFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1937.class)
public abstract class ClientClockManagerMixin {
   @Inject(method = "method_8532", at = @At("HEAD"), cancellable = true)
   private void onGetDayTime(CallbackInfoReturnable<Long> cir) {
      if ((Object)this instanceof class_638 level && level.method_27983() == class_1937.field_25179) {
         WorldTweaksFeature worldTweaks = WorldTweaksFeature.getEnabled();
         if (worldTweaks != null && worldTweaks.changeTime.getValue()) {
            cir.setReturnValue(worldTweaks.getCustomTime());
         }
      }
   }
}
