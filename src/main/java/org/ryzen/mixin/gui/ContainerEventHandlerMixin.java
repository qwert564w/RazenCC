package org.ryzen.mixin.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_4069;
import net.minecraft.class_437;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.screen.ScreenKeyEvent;
import org.ryzen.event.events.screen.ScreenMouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_4069.class)
public interface ContainerEventHandlerMixin {
   @Inject(method = "method_16803", at = @At("HEAD"), cancellable = true)
   private void onKeyReleased(class_11908 keyEvent, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof class_437 screen && EventManager.hasListeners(ScreenKeyEvent.class)) {
         if (EventManager.call(Events.SCREEN_KEY.set(screen, keyEvent, ScreenKeyEvent.Action.RELEASE)).isCancelled()) {
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(method = "method_25402", at = @At("HEAD"), cancellable = true)
   private void onMouseClicked(class_11909 mouseButtonEvent, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof class_437 screen && EventManager.hasListeners(ScreenMouseButtonEvent.class)) {
         if (EventManager.call(Events.SCREEN_MOUSE_BUTTON.set(screen, mouseButtonEvent, ScreenMouseButtonEvent.Action.CLICK, 0.0, 0.0)).isCancelled()) {
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(method = "method_25406", at = @At("HEAD"), cancellable = true)
   private void onMouseReleased(class_11909 mouseButtonEvent, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof class_437 screen && EventManager.hasListeners(ScreenMouseButtonEvent.class)) {
         if (EventManager.call(Events.SCREEN_MOUSE_BUTTON.set(screen, mouseButtonEvent, ScreenMouseButtonEvent.Action.RELEASE, 0.0, 0.0)).isCancelled()) {
            cir.setReturnValue(true);
         }
      }
   }

   @Inject(method = "method_25403", at = @At("HEAD"), cancellable = true)
   private void onMouseDragged(class_11909 mouseButtonEvent, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
      if (this instanceof class_437 screen && EventManager.hasListeners(ScreenMouseButtonEvent.class)) {
         if (EventManager.call(Events.SCREEN_MOUSE_BUTTON.set(screen, mouseButtonEvent, ScreenMouseButtonEvent.Action.DRAG, dragX, dragY)).isCancelled()) {
            cir.setReturnValue(true);
         }
      }
   }
}
