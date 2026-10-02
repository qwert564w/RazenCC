package org.ryzen.mixin.gui;

import java.io.InputStream;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_3262;
import net.minecraft.class_7367;
import net.minecraft.class_8518;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_8518.class)
public class IconSetMixin {
   @Inject(method = "method_51418", at = @At("HEAD"), cancellable = true)
   private void onGetStandardIcons(class_3262 resources, CallbackInfoReturnable<List<class_7367<InputStream>>> cir) {
      try {
         class_7367<InputStream> icon16 = () -> IconSetMixin.class.getResourceAsStream("/assets/ryzen/textures/gui/icon_16.png");
         class_7367<InputStream> icon32 = () -> IconSetMixin.class.getResourceAsStream("/assets/ryzen/textures/gui/icon_32.png");
         if (icon16.get() != null && icon32.get() != null) {
            cir.setReturnValue(List.of(icon16, icon32));
         }
      } catch (Exception e) {
         System.err.println("[Ryzen] Failed to load custom standard icons: " + e.getMessage());
      }
   }

   @Inject(method = "method_51420", at = @At("HEAD"), cancellable = true)
   private void onGetMacIcon(class_3262 resources, CallbackInfoReturnable<class_7367<InputStream>> cir) {
      try {
         class_7367<InputStream> macIcon = () -> IconSetMixin.class.getResourceAsStream("/assets/ryzen/textures/gui/icon_mac.png");
         if (macIcon.get() != null) {
            cir.setReturnValue(macIcon);
         }
      } catch (Exception e) {
         System.err.println("[Ryzen] Failed to load custom macOS app icon: " + e.getMessage());
      }
   }
}
