package org.ryzen.mixin.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1309;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.game.PlayerJumpEvent;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.ryzen.feature.impl.visual.SwingAnimationFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_1309.class)
public abstract class LivingEntityMixin {
   @Inject(method = "method_6043", at = @At("HEAD"))
   private void onPlayerJump(CallbackInfo ci) {
      class_746 player = MinecraftContext.mc.field_1724;
      if (player != null && (Object)this == (Object)player && EventManager.hasListeners(PlayerJumpEvent.class)) {
         EventManager.call(Events.PLAYER_JUMP.set(player, player.method_73189()));
      }
   }

   @Inject(method = "method_66279", at = @At("HEAD"), cancellable = true)
   private void onGetEffectBlendFactor(class_6880<class_1291> effect, float partialTick, CallbackInfoReturnable<Float> cir) {
      if ((Object)this == (Object)MinecraftContext.mc.field_1724 && RemovalsFeature.shouldRemoveBadEffectsVisuals()) {
         cir.setReturnValue(0.0F);
      }
   }

   @Inject(method = "method_6028", at = @At("HEAD"), cancellable = true)
   private void customSwingDuration(CallbackInfoReturnable<Integer> cir) {
      if ((Object)this == (Object)MinecraftContext.mc.field_1724) {
         SwingAnimationFeature swing = FeatureManager.INSTANCE.getEnabled(SwingAnimationFeature.class);
         if (swing != null) {
            cir.setReturnValue(swing.swingDurationTicks());
         }
      }
   }
}
