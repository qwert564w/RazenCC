package org.ryzen.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat.class_5595;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11228;
import net.minecraft.class_11244;
import net.minecraft.class_11245;
import net.minecraft.class_12136;
import net.minecraft.class_12137;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.ryzen.utils.render.gui.GuiBackdrop;
import org.ryzen.utils.render.gui.GuiPipelines;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_11228.class)
public abstract class GuiRendererMixin {
   @Shadow
   @Final
   private List<?> field_59915;
   @Unique
   private final IntList blade$glassSplits = new IntArrayList();
   @Unique
   private boolean blade$previousElementWasGlass;

   @Inject(method = "method_71290", at = @At("HEAD"))
   private void blade$resetGlassSplits(CallbackInfo ci) {
      this.blade$glassSplits.clear();
      this.blade$previousElementWasGlass = false;
   }

   @Inject(method = "method_71287", at = @At("HEAD"))
   private void blade$markGlassRuns(class_11244 elementState, CallbackInfo ci) {
      boolean glass = blade$isGlassPipeline(elementState.comp_4055());
      if (glass && !this.blade$previousElementWasGlass) {
         this.blade$glassSplits.add(this.field_59915.size());
      }

      this.blade$previousElementWasGlass = glass;
   }

   @WrapOperation(
      method = "method_71291",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/class_11228;method_71289(Ljava/util/function/Supplier;Lnet/minecraft/class_276;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$class_5595;II)V"
      )
   )
   private void blade$captureBackdropBeforeGlassRuns(
      class_11228 instance,
      Supplier<String> label,
      class_276 mainRenderTarget,
      GpuBufferSlice projectionMatrix,
      GpuBufferSlice fogBuffer,
      GpuBuffer vertexBuffer,
      class_5595 indexType,
      int startIndex,
      int endIndex,
      Operation<Void> original
   ) {
      int cursor = startIndex;

      for (int i = 0; i < this.blade$glassSplits.size(); i++) {
         int split = this.blade$glassSplits.getInt(i);
         if (split >= cursor && split < endIndex) {
            if (split > cursor) {
               original.call(new Object[]{instance, label, mainRenderTarget, projectionMatrix, fogBuffer, vertexBuffer, indexType, cursor, split});
            }

            GuiBackdrop.captureNow();
            cursor = split;
         }
      }

      if (cursor < endIndex) {
         original.call(new Object[]{instance, label, mainRenderTarget, projectionMatrix, fogBuffer, vertexBuffer, indexType, cursor, endIndex});
      }
   }

   @Unique
   private static boolean blade$isGlassPipeline(RenderPipeline pipeline) {
      return pipeline == GuiPipelines.BLUR_RECT || pipeline == GuiPipelines.GLASS_SHADOW;
   }

   @WrapOperation(
      method = "method_70887",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_12136;method_75297(Lcom/mojang/blaze3d/textures/FilterMode;)Lnet/minecraft/class_12137;")
   )
   private class_12137 blade$smoothDownscaledItems(
      class_12136 cache, FilterMode filterMode, Operation<class_12137> original, @Local(argsOnly = true) class_11245 itemState
   ) {
      return blade$isDownscaled(itemState)
         ? (class_12137)original.call(new Object[]{cache, FilterMode.LINEAR})
         : (class_12137)original.call(new Object[]{cache, filterMode});
   }

   @WrapOperation(
      method = "method_70887",
      at = @At(value = "FIELD", target = "Lnet/minecraft/class_10799;field_59968:Lcom/mojang/blaze3d/pipeline/RenderPipeline;")
   )
   private RenderPipeline blade$bicubicDownscalePipeline(Operation<RenderPipeline> original, @Local(argsOnly = true) class_11245 itemState) {
      return blade$isDownscaled(itemState)
         ? GuiPipelines.itemDownscale(class_310.method_1551().method_22683().method_4495())
         : (RenderPipeline)original.call(new Object[0]);
   }

   @Unique
   private static boolean blade$isDownscaled(class_11245 itemState) {
      return itemState.method_72120().m00 < 0.999F;
   }
}
