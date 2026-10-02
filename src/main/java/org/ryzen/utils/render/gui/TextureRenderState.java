package org.ryzen.utils.render.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11231;
import net.minecraft.class_2960;
import net.minecraft.class_4588;
import net.minecraft.class_8030;
import org.joml.Matrix3x2fc;
import org.ryzen.context.MinecraftContext;

@Environment(EnvType.CLIENT)
public final class TextureRenderState extends UiElementRenderState {
   private final GuiTexture texture;
   private final class_2960 managedTextureId;
   private final float x;
   private final float y;
   private final float width;
   private final float height;
   private final float u0;
   private final float v0;
   private final float u1;
   private final float v1;
   private final int color;
   private final float cornerRadius;
   private final float deviceScale;
   private final boolean smooth;

   public TextureRenderState(
      Matrix3x2fc pose,
      class_2960 textureId,
      boolean managed,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int color,
      float cornerRadius,
      class_8030 scissor
   ) {
      this(pose, textureId, managed, x, y, width, height, u0, v0, u1, v1, color, cornerRadius, false, scissor);
   }

   public TextureRenderState(
      Matrix3x2fc pose,
      class_2960 textureId,
      boolean managed,
      float x,
      float y,
      float width,
      float height,
      float u0,
      float v0,
      float u1,
      float v1,
      int color,
      float cornerRadius,
      boolean smooth,
      class_8030 scissor
   ) {
      super(pose, scissor, x, y, width, height);
      this.texture = managed ? null : GuiTexture.load(textureId);
      this.managedTextureId = managed ? textureId : null;
      this.smooth = smooth;
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.u0 = u0;
      this.v0 = v0;
      this.u1 = u1;
      this.v1 = v1;
      this.color = color;
      this.cornerRadius = cornerRadius;
      float poseScale = Math.max((float)Math.hypot(pose.m00(), pose.m01()), (float)Math.hypot(pose.m10(), pose.m11()));
      this.deviceScale = Math.max(1, MinecraftContext.mc.method_22683().method_4495()) * Math.max(poseScale, 0.01F);
   }

   public TextureRenderState(Matrix3x2fc pose, class_2960 textureId, float x, float y, float width, float height, int color, class_8030 scissor) {
      this(pose, textureId, false, x, y, width, height, 0.0F, 0.0F, 1.0F, 1.0F, color, 0.0F, scissor);
   }

   public void method_70917(class_4588 vertexConsumer) {
      float halfWidth = this.width * 0.5F;
      float halfHeight = this.height * 0.5F;
      int packedSizeX = UiVertexPacking.packSize(this.width);
      int packedSizeY = UiVertexPacking.packSize(this.height);
      this.addVertex(vertexConsumer, this.x, this.y, this.u0, this.v0, -halfWidth, -halfHeight, packedSizeX, packedSizeY);
      this.addVertex(vertexConsumer, this.x, this.y + this.height, this.u0, this.v1, -halfWidth, halfHeight, packedSizeX, packedSizeY);
      this.addVertex(vertexConsumer, this.x + this.width, this.y + this.height, this.u1, this.v1, halfWidth, halfHeight, packedSizeX, packedSizeY);
      this.addVertex(vertexConsumer, this.x + this.width, this.y, this.u1, this.v0, halfWidth, -halfHeight, packedSizeX, packedSizeY);
   }

   private void addVertex(class_4588 vertexConsumer, float x, float y, float u, float v, float localX, float localY, int packedSizeX, int packedSizeY) {
      vertexConsumer.method_22912(this.transformX(x, y), this.transformY(x, y), 0.0F)
         .method_39415(this.color)
         .method_22913(u, v)
         .method_60796(UiVertexPacking.packSignedSize(localX), UiVertexPacking.packSignedSize(localY))
         .method_22921(packedSizeX, packedSizeY)
         .method_22914(0.0F, 0.0F, 1.0F)
         .method_75298(Math.max(this.cornerRadius, 0.0F));
   }

   public RenderPipeline comp_4055() {
      return GuiPipelines.TEXTURE;
   }

   public class_11231 comp_4056() {
      if (this.texture != null) {
         float uSpan = Math.max(Math.abs(this.u1 - this.u0), 0.01F);
         float vSpan = Math.max(Math.abs(this.v1 - this.v0), 0.01F);
         return this.texture.textureSetup(this.width * this.deviceScale / uSpan, this.height * this.deviceScale / vSpan);
      } else {
         return class_11231.method_70900(
            MinecraftContext.mc.method_1531().method_4619(this.managedTextureId).method_71659(),
            RenderSystem.getSamplerCache().method_76520(this.smooth ? FilterMode.LINEAR : FilterMode.NEAREST, false)
         );
      }
   }
}
