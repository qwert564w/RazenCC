package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_4717;
import net.minecraft.class_5481;
import org.ryzen.command.CommandManager;
import org.ryzen.utils.irc.IrcChatRouter;
import org.ryzen.utils.text.SensitiveChatMask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_408.class)
public abstract class ChatScreenMixin extends class_437 {
   @Shadow
   protected class_342 field_2382;
   @Shadow
   private class_4717 field_21616;
   @Unique
   private class_4185 privacyButton;

   protected ChatScreenMixin(class_2561 title) {
      super(title);
   }

   @Inject(method = "method_25426", at = @At("TAIL"))
   private void addPrivacyButton(CallbackInfo ci) {
      this.privacyButton = class_4185.method_46430(privacyLabel(), ignored -> {
         SensitiveChatMask.toggle();
         this.privacyButton.method_25355(privacyLabel());
         this.field_21616.method_23934();
         this.focusInputAfterClick();
      }).method_46434(this.field_22789 - 150, this.field_22790 - 40, 146, 20).method_46431();
      this.method_37063(this.privacyButton);
   }

   @Inject(method = "method_73216", at = @At("HEAD"), cancellable = true)
   private void maskSensitiveText(String text, int offset, CallbackInfoReturnable<class_5481> cir) {
      class_5481 masked = SensitiveChatMask.format(this.field_2382.method_1882(), text, offset);
      if (masked != null) {
         cir.setReturnValue(masked);
      }
   }

   @Inject(method = "method_25404", at = @At("RETURN"))
   private void keepKeyboardFocusInInput(class_11908 event, CallbackInfoReturnable<Boolean> cir) {
      this.method_25395(this.field_2382);
      this.field_2382.method_25365(true);
   }

   @Inject(method = "method_44056", at = @At("HEAD"), cancellable = true)
   private void handleClientCommand(String message, boolean addToRecentChat, CallbackInfo ci) {
      class_408 screen = (class_408)(Object)this;
      String normalized = screen.method_44054(message);
      if (CommandManager.INSTANCE.handleChat(normalized)) {
         if (addToRecentChat) {
            class_310.method_1551().field_1705.method_1743().method_1803(normalized);
         }

         ci.cancel();
      } else {
         if (IrcChatRouter.routeOutgoing(normalized)) {
            if (addToRecentChat) {
               class_310.method_1551().field_1705.method_1743().method_1803(normalized);
            }

            ci.cancel();
         }
      }
   }

   @Unique
   private static class_2561 privacyLabel() {
      return class_2561.method_43470("Скрыть данные: " + (SensitiveChatMask.isEnabled() ? "True" : "False"));
   }

   @Unique
   private void focusInputAfterClick() {
      class_310 client = class_310.method_1551();
      client.execute(() -> {
         if (client.field_1755 == (class_408)(Object)this) {
            this.method_25395(this.field_2382);
            this.field_2382.method_25365(true);
         }
      });
   }
}
