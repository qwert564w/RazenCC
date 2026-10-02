package org.ryzen.mixin.gui;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_465;
import org.ryzen.feature.impl.misc.AuctionHelperFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_465.class)
public abstract class AbstractContainerScreenMixin {
   @Inject(method = "method_2385", at = @At("TAIL"))
   private void renderAuctionRank(class_332 graphics, class_1735 slot, int mouseX, int mouseY, CallbackInfo ci) {
      if (slot != null && slot.method_7681()) {
         class_465<?> screen = (class_465)(Object)this;
         int color = AuctionHelperFeature.slotOverlayColor(screen.method_25440().getString(), slot.method_7677());
         if (color != 0) {
            graphics.method_25294(slot.field_7873, slot.field_7872, slot.field_7873 + 16, slot.field_7872 + 16, color);
         }
      }
   }

   @Inject(method = "method_51454", at = @At("RETURN"), cancellable = true)
   private void augmentAuctionTooltip(class_1799 stack, CallbackInfoReturnable<List<class_2561>> cir) {
      class_465<?> screen = (class_465)(Object)this;
      cir.setReturnValue(AuctionHelperFeature.augmentTooltip(screen.method_25440().getString(), stack, (List<class_2561>)cir.getReturnValue()));
   }
}
