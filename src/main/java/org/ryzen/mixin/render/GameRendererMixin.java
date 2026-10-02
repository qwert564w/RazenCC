package org.ryzen.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11659;
import net.minecraft.class_1297;
import net.minecraft.class_239;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3966;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import net.minecraft.class_757;
import net.minecraft.class_759;
import net.minecraft.class_9779;
import org.joml.Matrix4f;
import org.ryzen.context.RenderContext;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.render.FinalGuiRenderEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.NoEntityTraceFeature;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.ryzen.feature.impl.visual.ShaderHandsFeature;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.world.ShaderHandsRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_757.class)
public abstract class GameRendererMixin {
   private static ShaderHandsRenderer shaderHandsRenderer;
   @Shadow
   @Final
   private class_310 field_4015;

   @ModifyArg(
      method = "method_3188",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11286;method_71123(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;")
   )
   private Matrix4f captureLevelProjection(Matrix4f projectionMatrix) {
      Render3DUtil.captureLevelProjection(projectionMatrix);
      return projectionMatrix;
   }

   @Inject(method = "method_3198", at = @At("HEAD"), cancellable = true)
   private void onBobHurt(class_4587 poseStack, float partialTick, CallbackInfo ci) {
      if (RemovalsFeature.shouldRemoveShaking()) {
         ci.cancel();
      }
   }

   @WrapOperation(
      method = "method_3190",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_76762(FLnet/minecraft/class_1297;)Lnet/minecraft/class_239;")
   )
   private class_239 skipEntityTrace(class_746 localPlayer, float partialTick, class_1297 cameraEntity, Operation<class_239> original) {
      class_239 result = (class_239)original.call(new Object[]{localPlayer, partialTick, cameraEntity});
      return result instanceof class_3966 && NoEntityTraceFeature.shouldSkipEntities()
         ? cameraEntity.method_5745(localPlayer.method_55754(), partialTick, false)
         : result;
   }

   @WrapOperation(
      method = "method_3172",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_759;method_22976(FLnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_746;I)V"
      )
   )
   private void renderShaderHands(
      class_759 itemInHandRenderer, float partialTick, class_4587 poseStack, class_11659 collector, class_746 player, int packedLight, Operation<Void> original
   ) {
      ShaderHandsFeature feature = FeatureManager.INSTANCE.getEnabled(ShaderHandsFeature.class);
      if (feature == null) {
         if (shaderHandsRenderer != null) {
            shaderHandsRenderer.release();
            shaderHandsRenderer = null;
         }

         original.call(new Object[]{itemInHandRenderer, partialTick, poseStack, collector, player, packedLight});
      } else {
         if (shaderHandsRenderer == null) {
            shaderHandsRenderer = new ShaderHandsRenderer();
         }

         shaderHandsRenderer.render(feature, () -> original.call(new Object[]{itemInHandRenderer, partialTick, poseStack, collector, player, packedLight}));
      }
   }

   @Inject(
      method = "method_3192",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11228;method_70890(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V")
   )
   private void onFinalGuiRender(class_9779 deltaTracker, boolean renderWorld, CallbackInfo ci, @Local class_332 guiGraphics) {
      if (EventManager.hasListeners(FinalGuiRenderEvent.class)) {
         boolean gameReady = this.field_4015.method_53466();
         boolean renderHud = gameReady && renderWorld && this.field_4015.field_1687 != null;
         boolean renderScreen = gameReady && (this.field_4015.method_18506() != null || this.field_4015.field_1755 != null);
         int mouseX = (int)this.field_4015.field_1729.method_68879(this.field_4015.method_22683());
         int mouseY = (int)this.field_4015.field_1729.method_68883(this.field_4015.method_22683());
         EventManager.call(
            Events.FINAL_GUI_RENDER.set(this.field_4015, this.field_4015.field_1705, guiGraphics, deltaTracker, renderHud, renderScreen, mouseX, mouseY)
         );
      }
   }

   @Inject(method = "method_3188", at = @At("TAIL"))
   private void onRender3D(class_9779 deltaTracker, CallbackInfo ci) {
      if (EventManager.hasListeners(Render3DEvent.class)) {
         RenderContext.enter3D((class_757)(Object)this, deltaTracker);

         try {
            EventManager.call(Events.RENDER_3D.set(this.field_4015, (class_757)(Object)this, deltaTracker));
         } finally {
            RenderContext.exit3D();
         }
      }
   }
}
