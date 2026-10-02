package org.ryzen.mixin.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import org.ryzen.event.EventManager;
import org.ryzen.event.events.input.CharacterInputEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_309.class)
public abstract class KeyboardHandlerMixin {
   @Inject(method = "method_1457", at = @At("HEAD"), cancellable = true)
   private void onCharacterTyped(long window, class_11905 characterEvent, CallbackInfo ci) {
      if (characterEvent.method_74227() && EventManager.hasListeners(CharacterInputEvent.class)) {
         CharacterInputEvent event = EventManager.call(new CharacterInputEvent(window, characterEvent.comp_4793()));
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "method_1466", at = @At("HEAD"), cancellable = true)
   private void onKeyPress(long window, int action, class_11908 keyEvent, CallbackInfo ci) {
      if (EventManager.hasListeners(KeyboardInputEvent.class)) {
         KeyboardInputEvent event = EventManager.call(new KeyboardInputEvent(window, keyEvent.comp_4795(), keyEvent.comp_4796(), action, keyEvent.comp_4797()));
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }
}
