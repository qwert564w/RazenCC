package org.ryzen.mixin.world;

import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_3414;
import net.minecraft.class_3419;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.ryzen.utils.render.world.DynamicLightManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_638.class)
public abstract class ClientLevelMixin {
   @Shadow
   @Final
   private Deque<Runnable> field_34804;
   private boolean hadQueuedLightUpdates;

   @Inject(method = "method_38534", at = @At("HEAD"))
   private void captureQueuedLightUpdates(CallbackInfo ci) {
      this.hadQueuedLightUpdates = !this.field_34804.isEmpty();
   }

   @Inject(method = "method_38534", at = @At("TAIL"))
   private void restoreDynamicLightsAfterPackets(CallbackInfo ci) {
      if (this.hadQueuedLightUpdates) {
         DynamicLightManager.INSTANCE.revalidateAfterVanillaUpdates((class_638)(Object)this);
      }
   }

   @Inject(
      method = "method_8449(Lnet/minecraft/class_1297;Lnet/minecraft/class_1297;Lnet/minecraft/class_6880;Lnet/minecraft/class_3419;FFJ)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void onPlaySeededEntitySound(
      class_1297 except, class_1297 sourceEntity, class_6880<class_3414> sound, class_3419 source, float volume, float pitch, long seed, CallbackInfo ci
   ) {
      if (RemovalsFeature.shouldRemoveSound((class_3414)sound.comp_349())) {
         ci.cancel();
      }
   }

   @Inject(method = "method_8465(Lnet/minecraft/class_1297;DDDLnet/minecraft/class_6880;Lnet/minecraft/class_3419;FFJ)V", at = @At("HEAD"), cancellable = true)
   private void onPlaySeededPositionedSound(
      class_1297 except, double x, double y, double z, class_6880<class_3414> sound, class_3419 source, float volume, float pitch, long seed, CallbackInfo ci
   ) {
      if (RemovalsFeature.shouldRemoveSound((class_3414)sound.comp_349())) {
         ci.cancel();
      }
   }

   @Inject(method = "method_55116(Lnet/minecraft/class_1297;Lnet/minecraft/class_3414;Lnet/minecraft/class_3419;FF)V", at = @At("HEAD"), cancellable = true)
   private void onPlayLocalEntitySound(class_1297 sourceEntity, class_3414 sound, class_3419 source, float volume, float pitch, CallbackInfo ci) {
      if (RemovalsFeature.shouldRemoveSound(sound)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_67392", at = @At("HEAD"), cancellable = true)
   private void onPlayPlayerSound(class_3414 sound, class_3419 source, float volume, float pitch, CallbackInfo ci) {
      if (RemovalsFeature.shouldRemoveSound(sound)) {
         ci.cancel();
      }
   }

   @Inject(method = "method_8486(DDDLnet/minecraft/class_3414;Lnet/minecraft/class_3419;FFZ)V", at = @At("HEAD"), cancellable = true)
   private void onPlayLocalPositionedSound(
      double x, double y, double z, class_3414 sound, class_3419 source, float volume, float pitch, boolean distanceDelay, CallbackInfo ci
   ) {
      if (RemovalsFeature.shouldRemoveSound(sound)) {
         ci.cancel();
      }
   }
}
