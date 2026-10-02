package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.screen.ScreenCloseEvent;
import org.ryzen.event.events.screen.ScreenKeyEvent;
import org.ryzen.event.events.screen.ScreenRenderEvent;
import org.ryzen.menu.core.MenuOverlay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_437.class)
public abstract class ScreenMixin {
   @Shadow
   @Final
   protected class_310 field_22787;

   @Inject(method = "method_25419", at = @At("HEAD"), cancellable = true)
   private void onClose(CallbackInfo ci) {
      if (EventManager.hasListeners(ScreenCloseEvent.class)) {
         if (EventManager.call(Events.SCREEN_CLOSE.set(this.field_22787, (class_437)(Object)this)).isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "method_25394", at = @At("HEAD"), cancellable = true)
   private void onRender(class_332 guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      if (EventManager.hasListeners(ScreenRenderEvent.class)) {
         if (EventManager.call(Events.SCREEN_RENDER.set((class_437)(Object)this, guiGraphics, mouseX, mouseY, partialTick)).isCancelled()) {
            ci.cancel();
         }
      }
   }

   @ModifyVariable(method = "method_25394", at = @At("HEAD"), ordinal = 0, argsOnly = true)
   private int maskMouseXWhenOverlayOpen(int mouseX) {
      return MenuOverlay.isOpen() ? -536870912 : mouseX;
   }

   @ModifyVariable(method = "method_25394", at = @At("HEAD"), ordinal = 1, argsOnly = true)
   private int maskMouseYWhenOverlayOpen(int mouseY) {
      return MenuOverlay.isOpen() ? -536870912 : mouseY;
   }

   @Inject(method = "method_25404", at = @At("HEAD"), cancellable = true)
   private void onKeyPressed(class_11908 keyEvent, CallbackInfoReturnable<Boolean> cir) {
      if (EventManager.hasListeners(ScreenKeyEvent.class)) {
         if (EventManager.call(Events.SCREEN_KEY.set((class_437)(Object)this, keyEvent, ScreenKeyEvent.Action.PRESS)).isCancelled()) {
            cir.setReturnValue(true);
         }
      }
   }
}
