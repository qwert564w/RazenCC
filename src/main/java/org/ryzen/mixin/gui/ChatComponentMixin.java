package org.ryzen.mixin.gui;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_303;
import net.minecraft.class_338;
import org.ryzen.feature.impl.misc.ChatHelperFeature;
import org.ryzen.utils.text.NameProtectUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_338.class)
public abstract class ChatComponentMixin {
   @Shadow
   @Final
   private List<class_303> field_2061;
   @Unique
   private String ryzen$lastCollapsedText;
   @Unique
   private int ryzen$repeatCount;

   @Shadow
   protected abstract void method_44813();

   @ModifyVariable(
      method = "method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V",
      at = @At("HEAD"),
      ordinal = 0,
      argsOnly = true
   )
   private class_2561 ryzen$rewriteMessage(class_2561 message) {
      class_2561 protectedMessage = NameProtectUtil.protect(message);
      int window = ChatHelperFeature.antiSpamWindow();
      if (window <= 0) {
         this.ryzen$lastCollapsedText = null;
         this.ryzen$repeatCount = 0;
         return protectedMessage;
      }

      String text = protectedMessage.getString();
      int limit = Math.min(window, this.field_2061.size());

      for (int index = 0; index < limit; index++) {
         if (this.ryzen$matchesCollapsed(this.field_2061.get(index).comp_893().getString(), text)) {
            this.field_2061.remove(index);
            this.ryzen$repeatCount = text.equals(this.ryzen$lastCollapsedText) ? this.ryzen$repeatCount + 1 : 2;
            this.ryzen$lastCollapsedText = text;
            this.method_44813();
            return ChatHelperFeature.withRepeatCounter(protectedMessage, this.ryzen$repeatCount);
         }
      }

      this.ryzen$lastCollapsedText = null;
      this.ryzen$repeatCount = 0;
      return protectedMessage;
   }

   @Unique
   private boolean ryzen$matchesCollapsed(String existing, String incoming) {
      if (existing.equals(incoming)) {
         return true;
      }

      int marker = existing.lastIndexOf(" (x");
      return marker > 0 && existing.endsWith(")") && existing.substring(0, marker).equals(incoming);
   }

   @Inject(method = "method_1808", at = @At("HEAD"), cancellable = true)
   private void ryzen$keepHistory(boolean clearSentHistory, CallbackInfo ci) {
      if (!clearSentHistory && ChatHelperFeature.shouldKeepHistory()) {
         ci.cancel();
      }
   }
}
