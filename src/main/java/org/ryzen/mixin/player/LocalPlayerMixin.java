package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.feature.impl.movement.NoPushFeature;
import org.ryzen.feature.impl.movement.NoSlowFeature;
import org.ryzen.utils.combat.LocalPlayerHistory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_746.class)
public abstract class LocalPlayerMixin {
   @Shadow
   @Final
   private class_310 field_3937;

   @Inject(method = "method_5773", at = @At("HEAD"))
   private void onTickPre(CallbackInfo ci) {
      if (EventManager.hasListeners(PlayerTickEvent.class)) {
         EventManager.call(Events.PLAYER_TICK.set((class_746)(Object)this, PlayerTickEvent.Phase.PRE));
      }
   }

   @Inject(method = "method_5773", at = @At("TAIL"))
   private void onTickPost(CallbackInfo ci) {
      class_746 player = (class_746)(Object)this;
      LocalPlayerHistory.record(player);
      if (EventManager.hasListeners(PlayerTickEvent.class)) {
         EventManager.call(Events.PLAYER_TICK.set(player, PlayerTickEvent.Phase.POST));
      }
   }

   @Inject(method = "method_30673(DD)V", at = @At("HEAD"), cancellable = true)
   private void cancelClosestSpacePush(double x, double z, CallbackInfo ci) {
      if (NoPushFeature.shouldCancelClosestSpacePush((class_746)(Object)this)) {
         ci.cancel();
      }
   }

   @Redirect(method = "method_66282", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_36455()F"))
   private float useCameraPitchForHandBob(class_746 player) {
      return this.field_3937.field_1690.method_31044().method_31034() ? this.field_3937.field_1773.method_19418().method_19329() : player.method_36455();
   }

   @Redirect(method = "method_66282", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_36454()F"))
   private float useCameraYawForHandBob(class_746 player) {
      return this.field_3937.field_1690.method_31044().method_31034() ? this.field_3937.field_1773.method_19418().method_19330() : player.method_36454();
   }

   @Inject(method = "method_75410()F", at = @At("HEAD"), cancellable = true)
   private void ryzen$keepSpeedWhileUsing(CallbackInfoReturnable<Float> cir) {
      NoSlowFeature noSlow = NoSlowFeature.getEnabled();
      if (noSlow != null && noSlow.shouldKeepSpeed((class_746)(Object)this)) {
         cir.setReturnValue(1.0F);
      }
   }
}
