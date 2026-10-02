package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import org.ryzen.utils.text.NameProtectUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(class_332.class)
public abstract class GuiGraphicsExtractorMixin {
   @ModifyVariable(
      method = {
            "method_25303(Lnet/minecraft/class_327;Ljava/lang/String;III)V",
            "method_51433(Lnet/minecraft/class_327;Ljava/lang/String;IIIZ)V",
            "method_25300(Lnet/minecraft/class_327;Ljava/lang/String;III)V"
      },
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private String protectString(String text) {
      return NameProtectUtil.protect(text);
   }

   @ModifyVariable(
      method = {
            "method_27535(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;III)V",
            "method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V",
            "method_27534(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;III)V",
            "method_60649(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIII)V",
            "method_71276(Lnet/minecraft/class_2561;II)V",
            "method_51438(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;II)V",
            "method_64235(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IILnet/minecraft/class_2960;)V"
      },
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private class_2561 protectComponent(class_2561 component) {
      return NameProtectUtil.protect(component);
   }

   @ModifyVariable(
      method = {
            "method_35720(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;III)V",
            "method_51430(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;IIIZ)V",
            "method_35719(Lnet/minecraft/class_327;Lnet/minecraft/class_5481;III)V"
      },
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private class_5481 protectFormattedCharSequence(class_5481 sequence) {
      return NameProtectUtil.protect(sequence);
   }

   @ModifyVariable(
      method = {
            "method_65179(Lnet/minecraft/class_327;Lnet/minecraft/class_5348;IIII)V", "method_51440(Lnet/minecraft/class_327;Lnet/minecraft/class_5348;IIIIZ)V"
      },
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private class_5348 protectFormattedText(class_5348 text) {
      return NameProtectUtil.protect(text);
   }
}
