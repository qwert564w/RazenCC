package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_897;
import org.ryzen.feature.impl.visual.NameTagsFeature;
import org.ryzen.utils.text.NameProtectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_897.class)
public abstract class EntityRendererMixin {
   @Inject(method = "method_62426", at = @At("RETURN"), cancellable = true)
   private void protectNameTag(class_1297 entity, CallbackInfoReturnable<class_2561> cir) {
      if (entity instanceof class_1657 && NameTagsFeature.shouldHideVanillaTag()) {
         cir.setReturnValue(null);
      } else {
         cir.setReturnValue(NameProtectUtil.protect((class_2561)cir.getReturnValue()));
      }
   }
}
