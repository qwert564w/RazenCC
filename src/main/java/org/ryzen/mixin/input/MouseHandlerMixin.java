package org.ryzen.mixin.input;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11910;
import net.minecraft.class_312;
import net.minecraft.class_746;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventManager;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.feature.impl.combat.AimAssistFeature;
import org.ryzen.menu.core.MenuOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_312.class)
public abstract class MouseHandlerMixin {
   @Inject(method = "method_1601", at = @At("HEAD"), cancellable = true)
   private void onMouseButton(long window, class_11910 buttonInfo, int action, CallbackInfo ci) {
      if (EventManager.hasListeners(MouseInputEvent.class)) {
         MouseInputEvent event = EventManager.call(new MouseInputEvent(window, buttonInfo.comp_4801(), action, buttonInfo.comp_4797()));
         if (event.isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "method_1598", at = @At("HEAD"), cancellable = true)
   private void blockWorldScrollWhileMenuOpen(long window, double horizontal, double vertical, CallbackInfo ci) {
      if (MenuOverlay.blocksInput()) {
         MenuOverlay.handleScroll(vertical);
         ci.cancel();
      }
   }

   @Redirect(method = "method_1606", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5872(DD)V"))
   private void onTurnPlayer(class_746 player, double yawDelta, double pitchDelta) {
      if (!RotationContext.onMouseTurn(yawDelta, pitchDelta)) {
         AimAssistFeature assist = AimAssistFeature.getEnabled();
         if (assist != null) {
            player.method_5872(assist.scaleYaw(player, yawDelta), assist.scalePitch(player, pitchDelta));
         } else {
            player.method_5872(yawDelta, pitchDelta);
         }
      }
   }
}
