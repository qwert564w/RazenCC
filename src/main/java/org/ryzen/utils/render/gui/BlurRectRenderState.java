package org.ryzen.utils.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11231;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2fc;
import org.ryzen.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class BlurRectRenderState extends UiElementRenderState {
   private final float x0;
   private final float y0;
   private final float x1;
   private final float y1;
   private final int tintColor;
   private final float radius;
   private final float opacity;
   private final class_11231 textureSetup;

   BlurRectRenderState(
      Matrix3x2fc pose,
      float x0,
      float y0,
      float x1,
      float y1,
      int tintColor,
      float radius,
      float blurRadiusPx,
      float opacity,
      GpuTextureView backdropView,
      class_8030 scissor
   ) {
      super(pose, scissor, x0, y0, x1 - x0, y1 - y0);
      this.x0 = x0;
      this.y0 = y0;
      this.x1 = x1;
      this.y1 = y1;
      this.tintColor = tintColor;
      this.radius = radius;
      this.opacity = opacity;
      this.textureSetup = class_11231.method_70900(backdropView, RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR));
      GuiBackdrop.requestBlurRadius(blurRadiusPx);
   }

   public void method_70917(class_4588 vertexConsumer) {
      float halfWidth = (this.x1 - this.x0) * 0.5F;
      float halfHeight = (this.y1 - this.y0) * 0.5F;
      int packedSizeX = UiVertexPacking.packSize(halfWidth * 2.0F);
      int packedSizeY = UiVertexPacking.packSize(halfHeight * 2.0F);
      int packedRadius = UiVertexPacking.packRadius(this.radius);
      int packedOpacity = MathUtil.clampByte(Math.round(this.opacity * 255.0F));
      this.addVertex(vertexConsumer, this.x0, this.y0, -halfWidth, -halfHeight, packedSizeX, packedSizeY, packedRadius, packedOpacity);
      this.addVertex(vertexConsumer, this.x0, this.y1, -halfWidth, halfHeight, packedSizeX, packedSizeY, packedRadius, packedOpacity);
      this.addVertex(vertexConsumer, this.x1, this.y1, halfWidth, halfHeight, packedSizeX, packedSizeY, packedRadius, packedOpacity);
      this.addVertex(vertexConsumer, this.x1, this.y0, halfWidth, -halfHeight, packedSizeX, packedSizeY, packedRadius, packedOpacity);
   }

   private void addVertex(
      class_4588 vertexConsumer, float x, float y, float localX, float localY, int packedSizeX, int packedSizeY, int packedRadius, int packedOpacity
   ) {
      vertexConsumer.method_22912(this.transformX(x, y), this.transformY(x, y), 0.0F)
         .method_39415(this.tintColor)
         .method_22913(localX, localY)
         .method_60796(packedSizeX, packedSizeY)
         .method_22921(packedRadius, packedOpacity)
         .method_22914(0.0F, 0.0F, 1.0F)
         .method_75298(0.0F);
   }

   public RenderPipeline comp_4055() {
      return GuiPipelines.BLUR_RECT;
   }

   public class_11231 comp_4056() {
      return this.textureSetup;
   }
}
