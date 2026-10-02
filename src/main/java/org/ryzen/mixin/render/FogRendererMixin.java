package org.ryzen.mixin.render;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_4184;
import net.minecraft.class_5636;
import net.minecraft.class_638;
import net.minecraft.class_7285;
import net.minecraft.class_758;
import net.minecraft.class_9779;
import org.joml.Vector4f;
import org.ryzen.feature.impl.visual.WorldTweaksFeature;
import org.ryzen.utils.ColorUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_758.class)
public abstract class FogRendererMixin {
   @Inject(
      method = "method_3211",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_758;method_71110(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V")
   )
   private void applyFogDistances(
      class_4184 camera,
      int renderDistanceInChunks,
      class_9779 deltaTracker,
      float darkenWorldAmount,
      class_638 level,
      CallbackInfoReturnable<Vector4f> cir,
      @Local class_7285 fog
   ) {
      if (camera.method_19334() == class_5636.field_27888) {
         WorldTweaksFeature worldTweaks = WorldTweaksFeature.getEnabled();
         if (worldTweaks != null && worldTweaks.changeFog.getValue()) {
            worldTweaks.applyFog(fog);
         }
      }
   }

   @Inject(method = "method_62185", at = @At("RETURN"), cancellable = true)
   private void applyFogColor(
      class_4184 camera, float partialTick, class_638 level, int renderDistanceInChunks, float darkenWorldAmount, CallbackInfoReturnable<Vector4f> cir
   ) {
      if (camera.method_19334() == class_5636.field_27888) {
         WorldTweaksFeature worldTweaks = WorldTweaksFeature.getEnabled();
         if (worldTweaks != null && worldTweaks.changeFog.getValue()) {
            int color = worldTweaks.resolvedFogColor();
            Vector4f original = (Vector4f)cir.getReturnValue();
            cir.setReturnValue(new Vector4f(ColorUtil.red(color) / 255.0F, ColorUtil.green(color) / 255.0F, ColorUtil.blue(color) / 255.0F, original.w));
         }
      }
   }
}
