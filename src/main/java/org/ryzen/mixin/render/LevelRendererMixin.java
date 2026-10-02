package org.ryzen.mixin.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11658;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import net.minecraft.class_4597.class_4598;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.ryzen.feature.impl.visual.BlockOutlineFeature;
import org.ryzen.feature.impl.visual.ChamsFeature;
import org.ryzen.utils.render.EntityEspStateCache;
import org.ryzen.utils.render.chams.ChamsTargetMatcher;
import org.ryzen.utils.render.world.WorldEffectContext;
import org.ryzen.utils.render.world.WorldEffects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_761.class)
public abstract class LevelRendererMixin {
   @Shadow
   @Final
   private class_11658 field_61737;

   @Inject(method = "method_72916", at = @At("HEAD"))
   private void captureEntityEspStates(class_4587 poseStack, class_11658 levelRenderState, class_11659 submitNodeCollector, CallbackInfo ci) {
      EntityEspStateCache.capture(levelRenderState.field_61735);
      ChamsFeature chams = ChamsFeature.getEnabled();
      if (chams != null && !chams.keepsOriginalModel() && !levelRenderState.field_61735.isEmpty()) {
         List<class_1297> targets = ChamsTargetMatcher.collectTargets(class_310.method_1551(), chams);
         if (!targets.isEmpty()) {
            levelRenderState.field_61735.removeIf(state -> ChamsTargetMatcher.matchingTarget(state, targets) != null);
         }
      }
   }

   @Inject(method = "method_62210", at = @At("HEAD"), cancellable = true)
   private void replaceVanillaBlockOutline(
      class_4598 bufferSource, class_4587 poseStack, boolean renderBlockOutline, class_11658 levelRenderState, CallbackInfo ci
   ) {
      BlockOutlineFeature feature = BlockOutlineFeature.getEnabled();
      if (feature != null && feature.usesShader()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_22710", at = @At("TAIL"))
   private void renderWorldEffects(
      class_9922 graphicsResourceAllocator,
      class_9779 deltaTracker,
      boolean renderBlockOutline,
      class_4184 camera,
      Matrix4f frustumMatrix,
      Matrix4f projectionMatrix,
      Matrix4f modelViewMatrix,
      GpuBufferSlice fogParameters,
      Vector4f skyColor,
      boolean hasCapturedFrustum,
      CallbackInfo ci
   ) {
      WorldEffects.render(new WorldEffectContext(this.field_61737, this.field_61737.field_63082, deltaTracker.method_60637(false), skyColor));
   }
}
